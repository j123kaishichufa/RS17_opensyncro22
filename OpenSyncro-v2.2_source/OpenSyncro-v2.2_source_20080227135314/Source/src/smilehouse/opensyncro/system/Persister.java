/* OpenSyncro - A web-based enterprise application integration tool
 * Copyright (C) 2008 Smilehouse Oy, support@opensyncro.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package smilehouse.opensyncro.system;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import smilehouse.opensyncro.pipes.ConverterListItem;
import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.component.ComponentClassNotFoundException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.pipes.log.LogEntry;
import smilehouse.opensyncro.pipes.log.LogMessageEntry;
import smilehouse.opensyncro.servlets.PipeComponentCreationException;
import smilehouse.opensyncro.user.User;
import smilehouse.util.HibernateConfiguration;

/**
 * Persister.java
 * 
 * Created: Fri Feb 6 13:51:40 2004
 */

public class Persister {

    /**
     * Thrown to indicate that some action to the database failed.
     */
    public static class DatabaseConnectionException extends RuntimeException {
        private String database;

        public DatabaseConnectionException(String database, String message, Exception e) {
            super(message+" [database='" + database + "']", e);
            this.database = database;
        }

        public String getDatabaseName() {
            return database;
        }
    }

    
    private static final Class[] PERSISTENT_CLASSES = {
            User.class,
            LogEntry.class,
            Pipe.class,
            PipeComponentData.class,
            ConverterListItem.class,
            LogMessageEntry.class,
            PipeExecutionRequest.class
            };
    private static SessionFactory sessionFactory;
    private static ComponentLoader componentLoader;
    static {
        try {
            //System.out.println("Initializing HibernateConfiguration");
            //configure
            sessionFactory = createConfiguration().getSessionFactory();
            componentLoader = new ComponentLoader();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    private Session session;
    private Environment environment;
    private String database;

   
    
    private static HibernateConfiguration createConfiguration() throws Exception{
        return new HibernateConfiguration(PERSISTENT_CLASSES);
    }

    public Persister(String database) throws DatabaseConnectionException {
    	this(database, false);
    }
    
    public Persister(String database, boolean suppressDatabaseConnectionException)
    												throws DatabaseConnectionException{
            Connection conn = null;
            try {
                this.environment = Environment.getInstance();
                if(suppressDatabaseConnectionException)
                	conn = environment.getConnectionSuppressException(database);
                else
                    conn = environment.getConnection(database);
                this.session = sessionFactory.openSession(conn);
                this.database = database;
            } catch(Exception e) {
                try {
                    if(session!=null)
                        session.close();
                    if(conn != null)
                        environment.freeConnection(conn);
                }catch(Exception sqle) {
                    environment.log("Exception while trying to close resources", sqle);  
                }
                throw new DatabaseConnectionException(database, "couldn't access database", e);
            }	
    }

    public String getDatabaseName() {
        return database;
    }

    public void close() {
        try {
            Environment.getInstance().freeConnection(session.close());
        } catch(Exception e) {
            environment.log("Exception while trying to close the hibernatesession", e);
        }
    }
    
    /**
     * Determines whether the connection used by this persister has been closed
     * 
     * @return <b>true</b> if connection is open, <b>false</b> otherwise
     * @throws HibernateException
     * @throws SQLException
     */
    public boolean isClosed() {
		try {
			if (session.isOpen())
				return session.connection().isClosed();
			else
				return true;
		} catch (Exception ex) {
			return false;
		}
	}
  


    private Long save(Object o){
        Long id = null;
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            id = (Long) session.save(o);
            session.flush();
            ts.commit();
        } catch(HibernateException e) {
            if(ts != null) {
                try {
                    ts.rollback();
                }catch(HibernateException se) {
                    environment.log("cannot rollback after failure", se); 
                }
            }
            throw new DatabaseConnectionException(database, "saving object to database failed", e);
        }
        return id;
    }

    private void update(Object o) {
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            session.update(o);
            session.flush();
            ts.commit();
        } catch(HibernateException e) {
            if(ts != null) {
                try {
                    ts.rollback();
                }catch(HibernateException se) {
                    environment.log("cannot rollback after failure", se); 
                }
            }
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            throw new DatabaseConnectionException(database, "updating object to database failed", e);
        }
    }



    private void delete(Object o) {
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            session.delete(o);
            session.flush();
            ts.commit();
        } catch(HibernateException e) {
            if(ts != null) {
                try {
                    ts.rollback();
                }catch(HibernateException se) {
                    environment.log("cannot rollback after failure", se); 
                }
            }
            throw new DatabaseConnectionException(database, "deleting object from database failed", e);
        }
    }

    
    
    private Object load(Class clas, Serializable id) {
        try {
            return session.get(clas, id);
        } catch(ObjectNotFoundException notFound) {
            //if not found, return null (ok?)
            return null;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error loading object", he);
        }
    }


    // ----
    // Pipe
    // ----

    public Pipe loadPipe(Long id){
        return (Pipe) load(Pipe.class, id);
    }

    public List loadAllPipes() {
        try {
            Query q = session
            	.createQuery("from smilehouse.opensyncro.pipes.Pipe pipe order by pipe.id asc");
            return q.list();
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }

    public Pipe loadFirstPipeYouFind(){
        try {
        Query q = session
            .createQuery("from smilehouse.opensyncro.pipes.Pipe pipe order by pipe.id asc");
        q.setMaxResults(1);
        List list = q.list();        
        if(!list.isEmpty())
            return (Pipe) list.get(0);
        else
            return null;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }

    public Pipe findPipeByName(String name){
        try {
        Query q = session.createQuery("from Pipe pipe where pipe.name = :name");
        q.setString("name", name);
        List list = q.list();
        if(!list.isEmpty())
            return (Pipe) list.get(0);
        else
            return null;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }

    public Long save(Pipe pipe) {
        return save((Object) pipe);
    }

    
    public void update(Pipe pipe) {
        update((Object) pipe);
    }

    public void delete(Pipe pipe)  {
        delete((Object) pipe);
    }

    public Long save(LogMessageEntry entry){
    	return save((Object) entry);
    }
    public void update(LogMessageEntry entry){
    	update((Object) entry);
    }
    public void delete(LogMessageEntry entry){
    	delete ((Object) entry);
    }
    public Long save(LogEntry entry){
    	return save((Object) entry);
    }
    public void update(LogEntry entry){
    	update((Object) entry);
    }
    public void delete(LogEntry entry){
    	delete ((Object) entry);
    }
    /**
     * Save given PipeExecutionRequest
     * @param per PipeExecutionRequest to save
     * @return id of the saved PipeExecutionRequest
     */
    public Long save(PipeExecutionRequest per) {
    	return save((Object) per);
    }
    /**
     * Update given PipeExecutionRequest
     * @param per PipeExecutionRequest to update
     */
    public void update(PipeExecutionRequest per) {
    	update((Object) per);
    }
    /**
     * Delete given PipeExecutionRequest
     * @param per PipeExecutionRequest to delete
     */
    public void delete(PipeExecutionRequest per){
    	delete ((Object) per);
    }
    
    // -------------
    // PipeComponent
    // -------------
    
    /*
    public PipeComponentIF loadPipeComponent(String id) {
        return getInstance(id);
    }
    */
    
    public PipeComponentIF loadPipeComponent(String id, Long PipeComponentDataId) {
        PipeComponentData pcdata = (PipeComponentData) load(PipeComponentData.class, PipeComponentDataId);
        return getInstance(id, pcdata);
        
    }

    public PipeComponentData loadPipeComponentData(Long pcdataId) {
        return (PipeComponentData) load(PipeComponentData.class, pcdataId);
    }
    
    public Long save(PipeComponentData pcdata) {
        return save((Object) pcdata);
    }

    public void update(PipeComponentData pcdata) {
        update((Object) pcdata);
    }

    public void delete(PipeComponentData pcdata)  {
        delete((Object) pcdata);
    }

    public Long save(ConverterListItem converterListItem) {
        return save((Object) converterListItem);
    }

    public void update(ConverterListItem converterListItem) {
        update((Object) converterListItem);
    }

    public void delete(ConverterListItem converterListItem)  {
        delete((Object) converterListItem);
    }

    public Long save(List list) {
        return save((Object) list);
    }

    public void update(List list) {
        update((Object) list);
    }

    public void delete(List list)  {
        delete((Object) list);
    }

    /**
     * Updates the pipe component. All the persistent data is in the internal
     * PipeComponentData-object which is actually updated.
     */

    public void update(PipeComponentIF component) {
        update(component.getData());
    }

    public void delete(PipeComponentIF component){
        delete(component.getData());
    }

    // ---------------------------
    // PipeComponentImplementation
    // ---------------------------

    public static boolean implementationExists(String className) {
        try {
            componentLoader.getImplementationClass(className);
            return true;
        } catch(ComponentClassNotFoundException cce) {
            return false;
        }
    }
    public static PipeComponentIF getInstance(String componentID) throws PipeComponentCreationException{
        return componentLoader.getInstance(componentID);
    }

    public static PipeComponentIF getInstance(String componentID, PipeComponentData data) throws PipeComponentCreationException{
        return componentLoader.getInstance(componentID, data);
    }

    public List loadComponentImplementations()  {
       return componentLoader.loadComponents();
    }
    public List loadComponentImplementations(int componentType){
        List filtered=new LinkedList();
        List components = loadComponentImplementations();
        for(Iterator iter = components.iterator(); iter.hasNext();) {
            PipeComponentIF component = (PipeComponentIF) iter.next();
            if(component.getType()==componentType)
                filtered.add(component);
        }
        return filtered;
    }

    public void reloadComponents() {
        componentLoader.refresh();
    }
    public boolean isDynamicComponent(PipeComponentIF component){
        return componentLoader.isDynamicComponent(component);
    }
    
    /**
     * This should be needed just for testing, I think...
     */   
    public void save(PipeComponentIF impl){ }
    
    // ---
    // Log
    // ---

    public List getLogEntries(int firstResult, int maxResults, Long pipeId) {
        try {
        Query q = null;
        if(pipeId != null) {
            q = session
                .createQuery("from LogEntry entry where entry.pipe.id=? order by entry.time desc");
            q.setLong(0, pipeId.longValue());
        } else
            q = session.createQuery("from LogEntry entry order by entry.time desc");
        q.setFirstResult(firstResult);
       	q.setMaxResults(maxResults);
        return q.list();
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }

    public int getNumberOfLogEntries(Long pipeId){
        try {
        Query q = null;
        if(pipeId != null) {
            q = session
                .createQuery("select count(entry) from LogEntry entry where entry.pipe.id=?");
            q.setLong(0, pipeId.longValue());
        } else
            q = session.createQuery("select count(entry) from LogEntry entry");
        Iterator i = q.iterate();
        if(i.hasNext()) {
            Integer number = (Integer) i.next();
            return number.intValue();
        } else
            return 0;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }
    
    /**
     * Returns the number of LogMessageEntries of a Pipe. If Pipe is not given,
     * returns total number of LogMessageEntries.
     * @param pipeId  Id of the pipe for which to find the number of LogMessageEntries.
     * @return Number of LogMessageEntries. 
     */
    public int getNumberOfLogMessageEntries(Long pipeId){
        try {
        Query q = null;
        
          if(pipeId != null) {
            q = session
                .createQuery("select count(entry) from LogMessageEntry entry where entry.pipe.id=?");
            q.setLong(0, pipeId.longValue());
        } else
            q = session.createQuery("select count(entry) from LogMessageEntry entry");
          
        Iterator i = q.iterate();
        if(i.hasNext()) {
            Integer number = (Integer) i.next();
            return number.intValue();
        } else
            return 0;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }

    public List getLogMessageEntries(Long logID,int verbosity) {
        try {
        Query q = null;
        q = session.createQuery("select entry from LogMessageEntry entry where entry.log=? and entry.messageType<=? order by entry.id asc");
        q.setLong(0, logID.longValue());
        q.setInteger(1, verbosity);
        return q.list();
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }
    
    /**
     * Returns a required portion of the list of LogMessageEntries.
     * @param firstResult  The first LogMessageEntry to be returned.
     * @param maxResults  The number of entries to be returned.
     * @return Required number of results from required position of the LogMessageEntries list. 
     */
    public List getAllLogMessageEntries(int firstResult, int maxResults) {   
        try {
            Query q = null;
            q = session.createQuery("from LogMessageEntry entry order by entry.log desc, entry.id asc");
            q.setFirstResult(firstResult);
           	q.setMaxResults(maxResults);
            return q.list();
            } catch(HibernateException he) {
                throw new DatabaseConnectionException(database, "Hibernate error on query", he);
            }
    }

    /**
     * Returns the greatest messageIndex of a LogEntry.
     * @param logEntry  Id of the log entry.
     * @return Value of the greatest log message index of this log entry. 
     */
    public int getMaxMessageIndex(Long logEntry) {   
        try {
            Query q = null;
            q = session.createQuery("select max(entry.index) from LogMessageEntry entry where entry.log=?");
            q.setLong(0, logEntry.longValue());
            return (Integer)q.uniqueResult();
            } catch(HibernateException he) {
                throw new DatabaseConnectionException(database, "Hibernate error on query", he);
            }
    }
    
    /**
     * Returns a list of PipeExecutionRequest corresponding to the specified pipe.
     * If no pipe id is given, returns all PipeExecutionRequests in the database.
     * @param pipeID  Id of the pipe for which to find PipeExecutionRequests. 
     * @return All PipeExecutionRequests for the given pipe. 
     */
    public List getPipeExecutionRequests(Long pipeID) {
		try {
			Query q = null;
			if (pipeID != null) {
				q = session
						.createQuery("select per from PipeExecutionRequest per where per.pipe=? order by requestDate asc");
				q.setLong(0, pipeID.longValue());
			} else
				q = session
						.createQuery("select per from PipeExecutionRequest per order by requestDate asc");
			return q.list();
		} catch (HibernateException he) {
			throw new DatabaseConnectionException(database,
					"Hibernate error on query", he);
		}
    }
    
    /*
	 * Clear TransferLog
	 */
    public void deleteAllLogEntries() {
        try {
        
        Transaction ts = session.beginTransaction();
        
        String hqlDelete1 = "DELETE FROM LogEntry";
        String hqlDelete2 = "DELETE FROM LogMessageEntry";
        session.createQuery( hqlDelete1 ).executeUpdate();
        session.createQuery( hqlDelete2 ).executeUpdate();
        session.flush();
        ts.commit();
       
        return;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on deleting records", he);
        }
    }
    
    /**
     * Deletes all PipeExecutionRequests referring to the given pipe.
     * If id is null, deletes all PipeExecutionRequests in the database
     * @param pipeID Id of the pipe. PipeExecutionRequests for this pipe will be deleted 
     */
    public void deleteAllPipeExecutionRequests(Long pipeID) {
        
    	try {
        Query q = null;
        if(pipeID==null){
        	q = session.createQuery("from PipeExecutionRequest per");
        }
        else{
        	q = session.createQuery("from PipeExecutionRequest per where per.pipe=?)");
        	q.setLong(0, pipeID.longValue());
        }
        Iterator i = q.iterate();
        Transaction ts = session.beginTransaction();
        while(i.hasNext()) {
            Object o = i.next();
            session.delete(o);            
        }
       

        session.flush();
        ts.commit();
        return;
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on deleting records", he);
        }
    }
    
    /**
     * Deletes the PipeExecutionRequest determined by the given id
     * @param perID Id of of the PipeExecutionRequest to delete
     */
    public void deletePipeExecutionRequest(Long perID) {

		try {
			Query q = null;
			q = session
					.createQuery("from PipeExecutionRequest per where per.id=?)");
			q.setLong(0, perID.longValue());

			Iterator i = q.iterate();
			Transaction ts = session.beginTransaction();
			while (i.hasNext()) {
				Object o = i.next();
				session.delete(o);
			}

			session.flush();
			ts.commit();
			return;
		} catch (HibernateException he) {
			throw new DatabaseConnectionException(database,
					"Hibernate error on deleting records", he);
		}
	}
    
    // -----
    // Users
    // -----

    public User loadUser(String login) {
        return (User) load(User.class, login);
    }

    public List loadAllUsers() {
        try {
        Query q = session.createQuery("from smilehouse.opensyncro.user.User user");
        return q.list();
        } catch(HibernateException he) {
            throw new DatabaseConnectionException(database, "Hibernate error on query", he);
        }
    }

    public boolean userExists(String login) {
        User user = (User) load(User.class, login);
        if( user != null ) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean loginOk(String user, String password){
       try {
        Query q = session
            .createQuery("select user.login from User user where user.login=:l and user.password=:pw");
        q.setString("l", user);
        q.setString("pw", password);
        List result = q.list();
        return result != null && result.size() > 0;
       } catch(HibernateException he) {
           throw new DatabaseConnectionException(database, "Hibernate error on query", he);
       }
    }

    public void update(User user){
        update((Object) user);
    }

    public void save(User user){
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            session.save(user.getLogin(),user);
            session.flush();
            ts.commit();
            
        } catch(HibernateException e) {
            if(ts != null) {
                try {
                    ts.rollback();
                }catch(HibernateException se) {
                    environment.log("cannot rollback after failure", se); 
                }
            }
            throw new DatabaseConnectionException(database, "saving object to database failed", e);
        }
    }


    public void delete(User user){
        delete((Object) user);
    }

    public static void createSchema(String databaseName, Connection conn) throws Exception {
        createConfiguration().createSchema(conn);
    }

    public static void dropSchema(String databaseName, Connection conn) throws Exception {
        createConfiguration().dropSchema(conn);
    }

    /*
     * public static void main(String[] args) { try { if(args.length >= 2 &&
     * args[0].equalsIgnoreCase("create")) { createSchema(args[1]); } else if(args.length >= 2 &&
     * args[0].equalsIgnoreCase("drop")) { dropSchema(args[1]); } else {
     * System.out.println("USAGE:\nPersister [create|drop] DATABASE"); } } catch(Throwable t) {
     * t.printStackTrace(); } }
     */

} // Persister
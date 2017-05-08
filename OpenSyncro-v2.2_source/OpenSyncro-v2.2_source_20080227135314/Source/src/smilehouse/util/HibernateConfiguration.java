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

package smilehouse.util;

import java.sql.Connection;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;


/**
 * HibernateConfiguration.java
 * 
 * Created: Wed Feb 11 16:19:43 2004
 */

public class HibernateConfiguration {

    private Configuration cfg;

    private SessionFactory sessionFactory;

    public HibernateConfiguration(Class[] persistentClasses) throws Exception {
        this.cfg = new Configuration();
        for(int i = 0; i < persistentClasses.length; i++)
            cfg.addClass(persistentClasses[i]);
        sessionFactory = cfg.buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session openSession(Connection conn) {
        return sessionFactory.openSession(conn);
    }

    public void createSchema(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        String[] statements = cfg.generateSchemaCreationScript(Dialect.getDialect(cfg
            .getProperties()));
        for(int i = 0; i < statements.length; i++) {
            stmt.executeUpdate(statements[i]);
        }

        stmt.close();
    }

    public void dropSchema(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        String[] statements = cfg.generateDropSchemaScript(Dialect.getDialect(cfg.getProperties()));
        for(int i = 0; i < statements.length; i++) {
            stmt.executeUpdate(statements[i]);
        }
        stmt.close();
    }


    public Long save(Object o, Session session) throws Exception {
        Long id = null;
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            id = (Long) session.save(o);
            session.flush();
            ts.commit();
        } catch(Exception e) {
            if(ts != null)
                ts.rollback();
            throw e;
        }
        return id;
    }

    public void update(Object o, Session session) throws Exception {
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            session.update(o);
            session.flush();
            ts.commit();
        } catch(Exception e) {
            if(ts != null)
                ts.rollback();
            throw e;
        }
    }

    public void delete(Object o, Session session) throws Exception {
        Transaction ts = null;
        try {
            ts = session.beginTransaction();
            session.delete(o);
            session.flush();
            ts.commit();
        } catch(Exception e) {
            if(ts != null)
                ts.rollback();
            throw e;
        }
    }

} // HibernateConfiguration
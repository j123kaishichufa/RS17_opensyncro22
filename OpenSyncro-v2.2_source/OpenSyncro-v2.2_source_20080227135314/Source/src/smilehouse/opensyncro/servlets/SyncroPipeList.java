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

package smilehouse.opensyncro.servlets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.ecs.html.Form;

import smilehouse.gui.html.fieldbased.ContainerFieldInfo;
import smilehouse.gui.html.fieldbased.Field;
import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.HighlightingContainerEditor;
import smilehouse.gui.html.fieldbased.editor.LinkButtonEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.editor.UneditingEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.IntrospectionPropertySetter;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.gui.html.fieldbased.model.PropertyBasedModelModifier;
import smilehouse.gui.html.fieldbased.model.PropertySetter;
import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.metadata.TransferInfo;
import smilehouse.opensyncro.system.Persister;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;
import smilehouse.util.RecallingList;

/**
 * SyncroPipeList.java
 * 
 * Created: Tue Mar 2 15:35:03 2004
 */

public class SyncroPipeList extends SyncroServlet {

    private static ContainerFieldInfo pipeListFieldInfo;
    private static Set pipeSet;
    private static final String DURATION_FORMAT = "H:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PIPE_ID = "pipeid";
    private static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final String PIPE_RUNNING="pipe_running";
    /**
     * Init called before this servlet serves any requests. This implementation initialises the
     * GUIContext ant the testobject instance variables.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        
    }


    public void initParameters(ParameterManager pm) {
        pm.addParameter(ACTION, ParameterManager.INT);
        pm.addParameter(PIPE_ID, ParameterManager.LONG);
    }

    public int getTabSetId() {
        return 2;
    }

    public int getTabId() {
        return 1;
    }


    public String handleRequest(HttpServletRequest req,
                                HttpSession session,
                                ParameterManager parameters,
                                Persister pers,
                                LabelResource labels,
                                int requestType) {
    	
    	//Get executing pipes (started via HttpStart) from webapp context. 
        //This allows executing pipes to be highlighted in the pipe list. 
        Object pipesAndThreadsObject = this.getServletContext().getAttribute(SyncroHttpStart.PIPES_AND_THREADS);
        Map pipest=null;
        final String language=environment.getLanguage(session);
        if(pipesAndThreadsObject!=null){
        	pipest=(Map)this.getServletContext().getAttribute(SyncroHttpStart.PIPES_AND_THREADS);
        	//Pipes are actually map keys.
        	pipeSet=pipest.keySet();
        	
        }
        
        try {

            // Well, here goes for nothing...

            ModelModifier modifier = new DefaultModelModifier() {
                public Object getModelValue(Object model) throws Exception {
                    return ((ListModel) model).getList();
                }

                public void setModelValue(Object model, Object value) throws Exception {
                    ((ListModel) model).setList((RecallingList) value);
                }
            };
            HighlightingContainerEditor hlContainerEditor = new HighlightingContainerEditor();
            hlContainerEditor.enableDelete("delete", "confirm_delete_pipe");
            hlContainerEditor.enableClone(Pipe.class,"clone");
            hlContainerEditor.enableAdd(Pipe.class, "add");
            hlContainerEditor.setActivePipes(pipeSet);
            
            pipeListFieldInfo = new ContainerFieldInfo("list", "pipes", modifier, hlContainerEditor);
            {
                // Editor for Pipe name
                String id1 = "name";
                String label1 = "name";
                String property1 = "name";
                PropertySetter propertySetter1 = new IntrospectionPropertySetter(Pipe.class);
                ModelModifier modifier1 = new PropertyBasedModelModifier(property1, propertySetter1);
                TextEditor editor1 = new TextEditor();
                editor1.setSize(50);
                FieldInfo fieldInfo1 = new FieldInfo(id1, label1, modifier1, editor1);
                pipeListFieldInfo.addColumn(fieldInfo1);
                
            }
            {
                // -----------------
                // Start time 
                // -----------------
                String id3 = "starttime";
                String label3 = "starttimelist";
                ModelModifier modifier3 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                    	Date start =((Pipe) model).getStartTime();
                    	if(start==null){
                    		return "";
                    	}
                        return dateFormat.format(start);
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    }
                };
                UneditingEditor editor3=new UneditingEditor();
                
                FieldInfo linkFieldInfo = new FieldInfo(id3, label3, modifier3, editor3);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
            {
                // -----------------
                // Finish time 
                // -----------------
                String id4 = "endtime";
                String label4 = "endtimelist";
                ModelModifier modifier4 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                    	Date end=((Pipe) model).getEndTime();
                        if(end==null||(pipeSet!=null&&pipeSet.contains((Pipe)model))){
                    		return "";
                    	}
                        return dateFormat.format(end);
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    }
                };
                UneditingEditor editor4=new UneditingEditor();
                
                FieldInfo linkFieldInfo = new FieldInfo(id4, label4, modifier4, editor4);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
            {
                // -----------------
                // Duration of pipe execution 
                // -----------------
                String id4 = "duration";
                String label4 = "duration";
                ModelModifier modifier4 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                    	Long duration=((Pipe)model).getDuration();
                    	if(duration==0)
                    		return "";
                    	else if(pipeSet!=null&&pipeSet.contains((Pipe)model))
                    		return environment.getLabel(PIPE_RUNNING, language );
                    	else
                    		return DurationFormatUtils.formatDuration(duration,DURATION_FORMAT);
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    }
                };
                UneditingEditor editor4=new UneditingEditor();
                
                FieldInfo linkFieldInfo = new FieldInfo(id4, label4, modifier4, editor4);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
            
            {
                // -----------------
                // Last status 
                // -----------------
                String id4 = "status";
                String label4 = "laststatus";
                ModelModifier modifier4 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                    	String status=((Pipe) model).getLastStatus();
                    	if(status!=null && (pipeSet==null||!pipeSet.contains((Pipe)model)))
                    		return status;
                    	return "";
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    }
                };
                UneditingEditor editor4=new UneditingEditor();
                
                FieldInfo linkFieldInfo = new FieldInfo(id4, label4, modifier4, editor4);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
            {
                // -----------------
                // Last user 
                // -----------------
                String id6 = "lastuser";
                String label6 = "lastuser";
                ModelModifier modifier6 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                    	String user=((Pipe) model).getUser();
                    	if(user!=null && (pipeSet==null||!pipeSet.contains((Pipe)model)))
                    		return user;
                    	return "";
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    }
                };
                UneditingEditor editor6=new UneditingEditor();
                
                FieldInfo linkFieldInfo = new FieldInfo(id6, label6, modifier6, editor6);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
            
            
            {
                // -----------------
                // Edit button field
                // -----------------
                String id2 = "edit";
                String label2 = "edit";
                ModelModifier modifier2 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getId();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    // We don't want to change it, we just want too see it
                    }
                };
                // Just want to show it...
                LinkButtonEditor editor2 = new LinkButtonEditor();
                editor2.setHref("EditPipe");
                editor2.setParameterName(PIPE_ID);
                editor2.setTarget("_self");
                FieldInfo linkFieldInfo = new FieldInfo(id2, label2, modifier2, editor2);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
            {
                // -----------------
                // Start button field
                // -----------------
                String id5 = "start";
                String label5 = "start";
                ModelModifier modifier5 = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getId();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    }
                };
                LinkButtonEditor editor5 = new LinkButtonEditor();
                editor5.setHref("PipeList");
                editor5.setParameterName(PIPE_ID);
                editor5.setTarget("_self");
                FieldInfo linkFieldInfo = new FieldInfo(id5, label5, modifier5, editor5);
                pipeListFieldInfo.addColumn(linkFieldInfo);
            }
           
        } catch(Exception e) {
            environment.log("Problems initializing GUI: \n"+e.getMessage());
        }

        ListModel pipes = new ListModel(pers.loadAllPipes());
        Field listField = pipeListFieldInfo.getField(
            pipes,
            environment.getLabelResource(session),
            req);
        
        //Try to get pipe id from the request. 
        Long pipeId=new Long(parameters.getLong(PIPE_ID));
        if(pipeId!=0){
        	//If the start button of one of the pipes was pressed, execute the pipe
        	Pipe startedPipe=pers.loadPipe(pipeId);
        	startedPipe.transfer(new TransferInfo(pers.getDatabaseName(),(String)session.getAttribute("syncro.user")));
        	pers.update(startedPipe);
        }
        if(requestType == POST_REQUEST) {
            if(parameters.getInt(ACTION) == 1) {

                listField.commit();

                for(Iterator added = pipes.getList().addedIterator(); added.hasNext();) {
                    Pipe pipe = (Pipe) added.next();
                    pers.save(pipe);
                    //System.out.println("Saved pipe #" + pipe.getId().toString());
                    //environment.log("Saved pipe #" + pipe.getId().toString());
                }
                /*for(Iterator cloned = pipes.getList().clonedIterator(); cloned.hasNext();) {
                    Pipe pipe = (Pipe) cloned.next();
                    pers.save(pipe);
                }*/
                for(Iterator current = pipes.getList().iterator(); current.hasNext();) {
                    Pipe pipe = (Pipe) current.next();
                    pers.update(pipe);
                    //System.out.println("Updated pipe #" + pipe.getId().toString());
                    //environment.log("Updated pipe #" + pipe.getId().toString());
                }

                for(Iterator removed = pipes.getList().removedIterator(); removed.hasNext();) {
                    Pipe pipe = (Pipe) removed.next();
                    //System.out.println("Removing pipe #" + pipe.getId().toString());
                    //environment.log("Removing pipe #" + pipe.getId().toString());
                    pers.delete(pipe);
                }
                
            } else
                listField.revert();
        }

        Form form = getForm("PipeList", listField.getEditor(), labels, false, true);

        return form.toString();
    }

    

    class ListModel {
        RecallingList list;

        public ListModel(List l) {
            this.list = new RecallingList(l);
        }

        public RecallingList getList() {
            return list;
        }

        public void setList(RecallingList list) {
            this.list = list;
            
        }
    }

} // SyncroPipeList
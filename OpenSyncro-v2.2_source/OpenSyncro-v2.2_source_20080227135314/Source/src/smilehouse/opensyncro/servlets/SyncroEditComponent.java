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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;

import smilehouse.gui.html.fieldbased.Field;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.opensyncro.pipes.component.ComponentClassNotFoundException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.system.Persister;
import smilehouse.tools.template.Template;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;

/**
 * SyncroEditComponent.java
 * 
 * Created: Thu Mar 11 11:25:16 2004
 */

public class SyncroEditComponent extends SyncroServlet {

    public int getTabId() {
        return 1;
    }

    public int getTabSetId() {
        return 100;
    }


    private static final String COMPONENT_ID = "compid";
    private static final String DATA_ID = "dataid";

    /** Used to retrieve the Pipe ID from session for creating a Back button
        to Pipe Editor */
    private static final String PIPE_ID_SESSION_KEY = "syncro.admin.pipe_id";
    
    public void initParameters(ParameterManager pm) {
        pm.addParameter(COMPONENT_ID, ParameterManager.STRING);
        pm.addParameter(DATA_ID, ParameterManager.LONG);
        pm.addParameter(ACTION, ParameterManager.INT);
    }

    public String handleRequest(HttpServletRequest req,
                                HttpSession session,
                                ParameterManager parameters,
                                Persister pers,
                                LabelResource labels,
                                int requestType) {

        PipeComponentIF component = null;
        PipeComponentData componentData = null;
        
        //component = pers.loadPipeComponent(new Long(parameters.getLong(COMPONENT_ID)));
        String componentId = parameters.getString(COMPONENT_ID);
        Long dataId = new Long(parameters.getLong(DATA_ID));

        //System.out.println("Servlet called with PipeComponentDataId " + dataId);
        
        // Query PipeComponentData from database
        componentData = pers.loadPipeComponentData(dataId);
        
        // Initialize a new PipeComponentData if database query returned null result
        if(componentData == null) {
            /*
            componentData = new PipeComponentData();
            componentData.setAttributes(new HashMap());
            pers.save(componentData);
            dataId = componentData.getId();
            
            //parameters.setParameter(DATA_ID, dataId.longValue());
            
            System.out.println("Retrieved Id " + dataId + " from saved new PipeComponentData");
            */
            return "<h3>Fatal: PipeComponentData ID " + dataId + " not found</h3>";
            
        }
        
        try {
           
            //System.out.println("EditComponent: " + componentId + ", Loaded pcdataId: " + componentData.getId());
            
            //component = pers.loadPipeComponent(componentId, dataId);
            component = Persister.getInstance(componentId, componentData);
        } catch(ComponentClassNotFoundException cnfe) {
            return "<br><b><font color=\"red\">"
                    + labels.getLabel("edited_component_class_not_found") + "</font></b><br>";
        }

        ElementContainer content = new ElementContainer().addElement(new BR()).addElement(
            new H1()
                .addElement(component.getName()));

        content.addElement(new HR());
        
        if( component instanceof GUIConfigurationIF) {
            
            GUIContext guiContext = ((GUIConfigurationIF) component).getGUIContext();

            if(guiContext != null) {

            // Component might have it's own labels...
                
            LabelResource customLabelResource = PipeComponentUtils.getCustomLabelResource(new Locale(
                environment.getLanguage(session)), labels, component.getClass());

            Map fields = guiContext.makeFields(component, customLabelResource, req);

            // -----------
            // Handle post
            // -----------
            if(requestType == POST_REQUEST && parameters.wasGiven(ACTION)) {
                int action = parameters.getInt(ACTION);
                if(action == 1) {
                    if(guiContext.hasBeenEdited(fields)) {
                        guiContext.commitFields(fields);
                        //System.out.println("Saving PipeComponentData, ID: " + componentData.getId());
                        pers.update(componentData);
                    }
                } else
                    guiContext.revertFields(fields);
            }

            // -----------------------
            // Print the field editors
            // -----------------------
            String guiHtml;

            // Use template if the component provides one
            String templateStr = ((GUIConfigurationIF) component).getGUITemplate();
            if(templateStr != null) {
                Template template = Template.createTemplate(templateStr);
                guiContext.writeEditors(fields, template);  
                template.write();
                guiHtml = template.toString();
            }
            // Else just print them...
            else {
                guiHtml = new String();
                for(Iterator i = fields.values().iterator(); i.hasNext();) {
                    Field field = (Field) i.next();
                    guiHtml += field.getEditor();
                }
            }


            // Create a back button to the Pipe Editor
            Long pipeId = (Long) session.getAttribute(PIPE_ID_SESSION_KEY);
            Input backButton = new Input(Input.BUTTON,
                						 "bb", labels.getLabel("back_to_pipe_editor"));
            backButton.setOnClick("document.location='EditPipe?pipeid=" + pipeId + "'");
            
            Form form = getFormWithCustomButton("EditComponent",
                							    guiHtml, backButton, labels, false, true);

            // Insert hidden fields carrying component and data IDs
            
            form.addElement(parameters.getInput(Input.HIDDEN, COMPONENT_ID));
            form.addElement(parameters.getInput(Input.HIDDEN, DATA_ID));

            content.addElement(form);
        }
            
        }
        
        else
            content.addElement(labels.getLabel("no_attributes_to_edit"));

        return content.toString();
    }

    public boolean hasCloseButton() {
        return true;
    }

} // SyncroEditComponent
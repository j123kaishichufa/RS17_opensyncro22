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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.saxon.event.PipelineConfiguration;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.ecs.AlignType;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.Entities;
import org.apache.ecs.html.B;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.gui.html.fieldbased.ContainerEditor;
import smilehouse.gui.html.fieldbased.ContainerFieldInfo;
import smilehouse.gui.html.fieldbased.Field;
import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.ConverterEditButtonEditor;
import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.gui.html.fieldbased.editor.UneditingEditor;
import smilehouse.gui.html.fieldbased.formatter.Formatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.ConverterListItem;
import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.DestinationIF;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.metadata.TransferInfo;
import smilehouse.opensyncro.system.Persister;
import smilehouse.opensyncro.system.StandaloneEnvironment;
import smilehouse.tools.template.Template;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;
import smilehouse.util.Utils;

/**
 * SyncroEditPipe.java
 * 
 * Created: Wed Mar 10 13:54:24 2004
 */

public class SyncroEditPipe extends SyncroServlet {

    private static final String PARAMETER_ENCODING = "UTF-8";

    private static final String COMPONENT_ID = "compid";
    private static final String DATA_ID = "dataid";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DURATION_FORMAT = "H:mm:ss";
    
    private static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    
    private static final String GENERAL_FIELDS_LAYOUT = "";

    private GUIContext generalFieldsContext;

    private ContainerFieldInfo converterFieldInfo;

    private Pipe pipe;

    public static class NullHidingFormatter implements Formatter {

        public NullHidingFormatter() {}

        public Object stringToValue(String string, EditorResources editorResources)
        // throws FormatterException
        {
            return string != null ? string : "";
        }
        
        public String valueToString(Object value, EditorResources editorResources)
        // throws FormatterException
        {
            return value != null ? value.toString() : "";
        }
    }

    public void init(ServletConfig config) throws ServletException {
        try {
            generalFieldsContext = new GUIContext();
            
            {
                // --------------------------------------------------------------------
                // Converter list field. Actually we handle PipeComponentData-instances
                // instead of PipeComponentIFs so that Hibernate doesn't get confused...
                // (Hibernate don't know anything about the PipeComponentIF interface)
                // --------------------------------------------------------------------
                String id = "converters";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getConverterList();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setConverterList((List) value);
                    }
                };
                ContainerEditor containerEditor = new ContainerEditor();
                containerEditor.enableDelete("delete", "confirm_delete_component2");
                containerEditor.enableMove("move", "pics/arrow_up_button.gif",
                    "move", "pics/arrow_down_button.gif", "move");

                converterFieldInfo = new ContainerFieldInfo(id, id, modifier, containerEditor);
                {
                    // ---------------------------------------------------
                    // Field for displaying the converter component's type
                    // ---------------------------------------------------
                    String id1 = "type";
                    String label1 = "converter_type";
                    ModelModifier modifier1 = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            //PipeComponentData componentData = (PipeComponentData) model;
                            ConverterListItem componentImpl = (ConverterListItem) model;
                            //if(!componentData.getImplementation().implementationExists()) {

                            // Assume component implementation exists
                            /*if(!componentImpl.implementationExists()) {
                             // If implementation is not there, show error...
                             return "!!! Class not found: "
                             + componentImpl.getClassName() + " !!!";
                             } else {
                             */
                            //PipeComponentIF impl = componentImpl.getInstance();
                            //return impl.getName();
                            return componentImpl.getConverter().getName();
                            //}
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                        // We don't want to change it, we just want to see it
                        }
                    };
                    // Just want to show it...
                    UneditingEditor editor1 = new UneditingEditor();
                    FieldInfo typeFieldInfo = new FieldInfo(id1, label1, modifier1, editor1);
                    converterFieldInfo.addColumn(typeFieldInfo);
                }

                // TODO: Converter component's Edit button should be disabled if the Converter does not
                //       implement GUIConfigurationIF
                {
                    // ---------------
                    // Edit link field
                    // ---------------
                    String id2 = "edit";
                    String label2 = "edit";
                    ModelModifier modifier2 = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            ConverterListItem componentData = (ConverterListItem) model;

                            HashMap m = new HashMap();
                            m.put(COMPONENT_ID, componentData.getConverter().getID());
                            m.put(DATA_ID, componentData.getConverterData().getId().toString());
                            return m;

                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                        // We don't want to change it, we just want too see it
                        }
                    };

                    // Just want to show it...

                    ConverterEditButtonEditor editor2 = new ConverterEditButtonEditor();
                        
                    editor2.setHref("EditComponent");
                    List paramNameList = new LinkedList();
                    paramNameList.add(COMPONENT_ID);
                    paramNameList.add(DATA_ID);

                    editor2.setParameterNames(paramNameList);

                    FieldInfo linkFieldInfo = new FieldInfo(id2, label2, modifier2, editor2);
                    converterFieldInfo.addColumn(linkFieldInfo);
                }
            }

        } catch(Exception e) {
            throw new ServletException(
                "Problem with initializing bean fields. Fields probably incorrectly defined.",
                e);
        }
    }

    private static final String PIPE_ID = "pipeid";
    private static final String CREATE_COMPONENT_OF_TYPE = "newcomptype";
    private static final String DELETE_COMPONENT_OF_TYPE = "delcomptype";
    private static final String PIPE_ID_SESSION_KEY = "syncro.admin.pipe_id";


    private static final int ACTION_CANCEL = 0;
    private static final int ACTION_OK = 1;
    private static final int ACTION_START = 2;

    public void initParameters(ParameterManager pm) {
        pm.addParameter(PIPE_ID, ParameterManager.LONG);
        pm.addParameter(ACTION, ParameterManager.INT);
        pm.addParameter(CREATE_COMPONENT_OF_TYPE, ParameterManager.INT);
        pm.addParameter(DELETE_COMPONENT_OF_TYPE, ParameterManager.INT);
    }

    public int getTabSetId() {
        return 2;
    }

    public int getTabId() {
        return 2;
    }


    /**
     * Tries determine which pipe is to be edited, first looking at the request parameters and then,
     * if no PIPE_ID parameter is given from the session. If this also fails the method returns the
     * pipe with smallest id. If a pipe found it's returned and it's id is saved to the session.
     * 
     * @param parameters Request parameters
     * @param session HttpSession
     * @param pers Persister for accessing the OpenSyncro database
     * 
     * @return The pipe in question or null if no pipe is found
     */
    private Pipe getPipe(ParameterManager parameters, HttpSession session, Persister pers) {
        Pipe pipe = null;
        // Was it given as a parameters?
        if(parameters.wasGiven(PIPE_ID) && !parameters.wasIllegal(PIPE_ID)) {
            Long pipeId = new Long(parameters.getLong(PIPE_ID));
            pipe = pers.loadPipe(pipeId);
        }
        // How about the session?
        if(pipe == null) {
            Long pipeId = (Long) session.getAttribute(PIPE_ID_SESSION_KEY);
            if(pipeId != null)
                pipe = pers.loadPipe(pipeId);
        }
        // Well... ANY PIPE will do!
        if(pipe == null) {
            pipe = pers.loadFirstPipeYouFind();
        }

        if(pipe != null)
            session.setAttribute(PIPE_ID_SESSION_KEY, pipe.getId());
        
        return pipe;
    }



    public String handleRequest(HttpServletRequest req,
                                HttpSession session,
                                ParameterManager parameters,
                                Persister pers,
                                LabelResource labels,
                                int requestType) {

        this.pipe = getPipe(parameters, session, pers);
        if(this.pipe == null)
            return "";

        //Field nameField = nameFieldInfo.getField(pipe, labels, req);

        Field converterField = converterFieldInfo.getField(pipe, labels, req);

        //Field verbosityLevelField = verbosityLevelFieldInfo.getField(pipe, labels, req);
        java.util.Map generalFields = generalFieldsContext.makeFields(pipe, labels, req);

        // -----------
        // Handle post
        // -----------
        if(requestType == POST_REQUEST) {
            if(parameters.getInt(ACTION) != ACTION_CANCEL) {

                // -----------------------------------------
                // Commit fields if somebody has edited them
                // -----------------------------------------
                // 	if(nameField.hasBeenEdited() && nameField.isEditValid()) {
                // 		    nameField.commit();
                // 		}
                if(converterField.hasBeenEdited() && converterField.isEditValid()) {
                    converterField.commit();
                }
                // 		if(verbosityLevelField.hasBeenEdited() && verbosityLevelField.isEditValid()) {
                // 		    verbosityLevelField.commit();
                // 		}
                generalFieldsContext.commitFields(generalFields);

                // ---------------------------
                // Create components if needed
                // ---------------------------
                if(parameters.wasGiven(CREATE_COMPONENT_OF_TYPE)
                        && parameters.getInt(CREATE_COMPONENT_OF_TYPE) != -1) {
                    // Find out the class name of the component to be created
                    int componentType = parameters.getInt(CREATE_COMPONENT_OF_TYPE);
                    String className = req.getParameter("compImpl" + componentType);
                    if(className != null) {
                        // Load the implementation description, create component instance and put it
                        // into it's place in the pipe
                        //PipeComponentImplementation impl = pers.loadComponentImplementation(className);

                        PipeComponentIF impl = Persister.getInstance(className);
                        if(impl != null) {
                            //PipeComponentIF newComponent = impl.getInstance();
                            PipeComponentIF newComponent = Persister.getInstance(className);
                            PipeComponentData pcdata;
                            pcdata = new PipeComponentData();
                            pcdata.setAttributes(new HashMap());
                            //Long dataId;

                            switch(componentType) {
                            case PipeComponentIF.TYPE_SOURCE:
                                pipe.setSource((SourceIF) newComponent);
                                pipe.setSourceID(newComponent.getID());

                                pcdata = new PipeComponentData();
                                pipe.setSourceData(pcdata);
                                newComponent.setData(pcdata);

                                pers.save(pcdata);

                                //dataId = pcdata.getId();
                                break;
                            case PipeComponentIF.TYPE_DESTINATION:
                                pipe.setDestination((DestinationIF) newComponent);
                                pipe.setDestinationID(newComponent.getID());

                                pcdata = new PipeComponentData();
                                pipe.setDestinationData(pcdata);
                                newComponent.setData(pcdata);

                                pers.save(pcdata);

                                //dataId = pcdata.getId();
                                break;
                            case PipeComponentIF.TYPE_CONVERTER:
                                pcdata = new PipeComponentData();
                                ConverterListItem cl = pipe.addConverter(
                                    (ConverterIF) newComponent,
                                    pcdata);
                                pers.save(cl);

                                pers.save(pipe);

                                //dataId = pcdata.getId();

                                /*System.out.println("Created new Converter (" + newComponent.getID() + ", " +
                                 className + "), dataId: " + dataId);*/

                                converterField = converterFieldInfo.getField(pipe, labels, req);
                                break;
                            }
                        }
                    }
                }

                // ------
                // Delete
                // ------
                if(parameters.wasGiven(DELETE_COMPONENT_OF_TYPE)
                        && parameters.getInt(DELETE_COMPONENT_OF_TYPE) != -1) {
                    switch(parameters.getInt(DELETE_COMPONENT_OF_TYPE)) {
                    case PipeComponentIF.TYPE_SOURCE:
                        PipeComponentIF source = pipe.getCurrentSource();
                        String sourceID = pipe.getSourceID();
                        if(sourceID != null) {
                            PipeComponentData sourceData = this.pipe.getSourceData();

                            // We need to remove references to Source component's PipeComponentData before deleting it
                            if(source != null) {
                                source.setData(null);
                            }
                            pipe.setSourceData(null);

                            // Delete the Source component's PipeComponentData
                            pers.delete(sourceData);

                            // Clear Source component instance and ID
                            pipe.setSource(null);
                            pipe.setSourceID(null);

                        }
                        break;
                    case PipeComponentIF.TYPE_DESTINATION:
                        PipeComponentIF destination = pipe.getCurrentDestination();
                        String destinationID = pipe.getDestinationID();
                        if(destinationID != null) {
                            PipeComponentData destinationData = this.pipe.getDestinationData();

                            // We need to remove references to Destination component's PipeComponentData before deleting it
                            if(destination != null) {
                               destination.setData(null);    
                            }
                            
                            pipe.setDestinationData(null);

                            // Delete the Destination component's PipeComponentData
                            pers.delete(destinationData);

                            // Clear Source component instance and ID
                            pipe.setDestination(null);
                            pipe.setDestinationID(null);

                        }
                        break;
                    }
                }

                if(parameters.getInt(ACTION) == ACTION_START) {
                    pipe.transfer(new TransferInfo(pers.getDatabaseName(),
                    		(String)session.getAttribute(StandaloneEnvironment.SESSION_USER_ATTR)));
                }

                pers.update(pipe);
            }
            //else
            //		nameField.revert();
        }

        //String nameHtml = nameField.getEditor();
        //String verbosityLevelHtml = verbosityLevelField.getEditor();

        /** Create TD for Source Component */
        
        TD sourceCell = null;
        SourceIF pipeSourceComponent = pipe.getSource();

        if((pipeSourceComponent == null) && (pipe.getSourceID() != null)) {
            // Source component which cannot be loaded
            sourceCell = getUnknownComponentCell(
                pipe.getSourceID(),
                pers,
                PipeComponentIF.TYPE_SOURCE,
                labels);
        } else {
            sourceCell = getComponentCell(
                pipeSourceComponent,
                pers,
                PipeComponentIF.TYPE_SOURCE,
                labels);
        }

        /** Create TD for Destination Component */

        TD destinationCell = null;
        DestinationIF pipeDestinationComponent = pipe.getDestination();

        if((pipeDestinationComponent == null) && (pipe.getDestinationID() != null)) {
            // Destination component which cannot be loaded
            destinationCell = getUnknownComponentCell(
                pipe.getDestinationID(),
                pers,
                PipeComponentIF.TYPE_DESTINATION,
                labels);
        } else {
            destinationCell = getComponentCell(
                pipeDestinationComponent,
                pers,
                PipeComponentIF.TYPE_DESTINATION,
                labels);
        }

        /** TODO: If Converter component is unloadable, Converter list should display
                  label "converter_component_unloadable" (+ConverterID) instead of 'null'
        */
        TD convertersCell = getConvertersCell(converterField, pers, labels);

        Table componentTable = new Table()
            .setBorder(0)
            .setCellSpacing(0)
            .setCellPadding(5)
            .addElement(
                new TR()
                    .addElement(
                        new TD().addElement(new B(labels.getLabel("source"))).setClass("ttheader1"))
                    .addElement(
                        new TD().setRowSpan(2).setVAlign(AlignType.MIDDLE).addElement(new B("-->")))
                    .addElement(
                        new TD().addElement(new B(labels.getLabel("converters"))).setClass(
                            "ttheader1"))
                    .addElement(
                        new TD().setRowSpan(2).setVAlign(AlignType.MIDDLE).addElement(new B("-->")))
                    .addElement(
                        new TD().addElement(new B(labels.getLabel("destination"))).setClass(
                            "ttheader1")))
            .addElement(
                new TR()
                    .setVAlign(AlignType.TOP)
                    .addElement(sourceCell.setBgColor("FFFFFF"))
                    .addElement(convertersCell.setBgColor("FFFFFF"))
                    .addElement(destinationCell.setBgColor("FFFFFF")));

        // --------------------------
        // A button to start the pipe
        // --------------------------
        Input startButton = new Input(Input.BUTTON, "strt", labels.getLabel("start"));
        startButton.setOnClick("document.forms[0]." + ACTION + ".value=" + ACTION_START
                + ";document.forms[0].submit()");

        Table infoTable = new Table()
        .setBorder(0)
        .setCellSpacing(0)
        .setCellPadding(5)
        .addElement(
            new TR()
                .addElement(
                    new TD().addElement(startButton)));
                    
        //Add pipe execution information after pipe execution              
        if(parameters.getInt(ACTION) == ACTION_START) {        
                infoTable.addElement(
                    new TD().setRowSpan(1).setVAlign(AlignType.MIDDLE).addElement(new B(labels.getLabel("starttimeeditor")+":")))
                .addElement(
                    new TD().setRowSpan(2).setVAlign(AlignType.MIDDLE).addElement(dateFormat.format(pipe.getStartTime())))
                .addElement(
                    new TD().setRowSpan(1).setVAlign(AlignType.MIDDLE).addElement(new B(labels.getLabel("endtimeeditor")+":")))
                .addElement(
                    new TD().setRowSpan(2).setVAlign(AlignType.MIDDLE).addElement(dateFormat.format(pipe.getEndTime())))
                .addElement(
                    new TD().setRowSpan(1).setVAlign(AlignType.MIDDLE).addElement(new B(labels.getLabel("editorduration")+":")))
                .addElement(
                    new TD().setRowSpan(2).setVAlign(AlignType.MIDDLE).addElement(DurationFormatUtils.formatDuration(pipe.getDuration(),DURATION_FORMAT)))
                .addElement(
                    new TD().setRowSpan(1).setVAlign(AlignType.MIDDLE).addElement(new B(labels.getLabel("editorstatus")+":")))
                .addElement(
                    new TD().setRowSpan(2).setVAlign(AlignType.MIDDLE).addElement(pipe.getLastStatus()));
        }
        
        Template generalFieldsTemplate = Template.createTemplate(GENERAL_FIELDS_LAYOUT);

        generalFieldsContext.writeEditors(generalFields, generalFieldsTemplate);

        generalFieldsTemplate.write();

        ElementContainer content = new ElementContainer().addElement(new H1(pipe.getName())).addElement(
            generalFieldsTemplate.toString()).addElement(new BR()).addElement(new BR()).addElement(
            componentTable).addElement(new BR()).addElement(infoTable);

        
        Form form = getForm("EditPipe", content.toString(), labels, false, true).addElement(
            parameters.getInput(Input.HIDDEN, PIPE_ID)).addElement(
            new Input(Input.HIDDEN, CREATE_COMPONENT_OF_TYPE, "-1")).addElement(
            new Input(Input.HIDDEN, DELETE_COMPONENT_OF_TYPE, "-1"));

        return form.toString();
    }

    private TD getComponentCell(PipeComponentIF component,
                                Persister pers,
                                int componentType,
                                LabelResource labels) {
        TD cell = new TD();
        if(component != null) {

            cell.addElement(component.getName());

            // Insert Component Editor ('Edit') button if the component supports configuration GUI
            if(component instanceof GUIConfigurationIF) {

                try {
                    String compIdParameter = URLEncoder.encode(COMPONENT_ID, PARAMETER_ENCODING)
                            + "=" + URLEncoder.encode(component.getID(), PARAMETER_ENCODING);
                    //String dataIdParameter = URLEncoder.encode(DATA_ID) + "=" +
                    // component.getData().getId();
                    String dataIdParameter = URLEncoder.encode(DATA_ID, PARAMETER_ENCODING) + "=";
                    switch(componentType) {

                    case PipeComponentIF.TYPE_SOURCE:
                        dataIdParameter = dataIdParameter
                                + URLEncoder.encode(
                                    this.pipe.getSourceData().getId().toString(),
                                    PARAMETER_ENCODING);
                        break;

                    case PipeComponentIF.TYPE_DESTINATION:
                        dataIdParameter = dataIdParameter
                                + URLEncoder.encode(this.pipe
                                    .getDestinationData()
                                    .getId()
                                    .toString(), PARAMETER_ENCODING);
                        break;

                    // Converter type components not handled here
                    /*
                     * case PipeComponentIF.TYPE_CONVERTER: break;
                     */
                    }

                    // Add component edit button
                    Input editButton = new Input(Input.BUTTON, ("eb" + componentType),
                        						 labels.getLabel("edit"));
                    editButton.setOnClick("document.location='" +
                        				  "EditComponent?" + compIdParameter + "&" +
                        				  dataIdParameter + "'");
                    cell.addElement(Entities.NBSP).addElement(editButton);
                } catch(UnsupportedEncodingException e) {
                    throw new RuntimeException(
                        "Internal error: Unable to encode servlet parameters with "
                                + PARAMETER_ENCODING);
                }
            }

            // Add a Delete button
            cell.addElement(new BR()).addElement(getComponentDeleteButton(componentType, labels));

        } else {
            cell.addElement(getNewComponentSelect(
                pers,
                componentType,
                labels.getLabel("create"),
                labels.getLocale()));
        }
        return cell;
    }

    private Input getComponentDeleteButton(int componentType, LabelResource labels) {
        String confirmMessage = Utils.escapeJavaScriptString(labels
            .getLabel("confirm_delete_component" + componentType));
        Input deleteButton = new Input(Input.BUTTON, "db" + componentType, labels
            .getLabel("delete"));
        deleteButton.setOnClick("if(!confirm('" + confirmMessage + "')) return false;"
                + "document.forms[0]." + DELETE_COMPONENT_OF_TYPE + ".value=" + componentType
                + ";" + "document.forms[0]." + ACTION + ".value=" + ACTION_OK + ";"
                + "document.forms[0].submit()");
        return deleteButton;
    }

    private TD getUnknownComponentCell(String componentName,
                                       Persister pers,
                                       int componentType,
                                       LabelResource labels) {
        TD cell = new TD();

        switch(componentType) {

        case PipeComponentIF.TYPE_SOURCE:
            cell.addElement(new B(labels.getLabel("source_component_unloadable")));
            break;

        case PipeComponentIF.TYPE_DESTINATION:
            cell.addElement(new B(labels.getLabel("destination_component_unloadable")));
            break;

        }

        cell.addElement(new BR());
        cell.addElement(componentName);

        // Add a Delete button
        cell.addElement(new BR()).addElement(getComponentDeleteButton(componentType, labels));
        return cell;
    }

    private ElementContainer getNewComponentSelect(Persister pers,
                                                   int componentType,
                                                   String createButtonText,
                                                   Locale locale) {

        List componentImplChoices = pers.loadComponentImplementations(componentType);
        Select implSelect = new Select("compImpl" + componentType);
        
        // Sort components into alphabetical order by component names
        Collections.sort(componentImplChoices, new Comparator() {
            public int compare(Object o1, Object o2) {
                String componentName1 = ((PipeComponentIF) o1).getName();
                String componentName2 = ((PipeComponentIF) o2).getName();
                return componentName1.compareTo(componentName2);
            }
        });
        
        for(Iterator i = componentImplChoices.iterator(); i.hasNext();) {
            PipeComponentIF impl = (PipeComponentIF) i.next();
            implSelect.addElement(new Option(impl.getID()).addElement(impl.getName()));
        }

        Input createButton = new Input(Input.BUTTON, "cb", createButtonText);
        createButton.setOnClick("document.forms[0]." + CREATE_COMPONENT_OF_TYPE + ".value="
                + componentType + ";document.forms[0].action.value=" + ACTION_OK
                + ";document.forms[0].submit()");

        return new ElementContainer().addElement(implSelect).addElement(Entities.NBSP).addElement(
            createButton);
    }


    private TD getConvertersCell(Field converterField, Persister pers, LabelResource labels) {
        return new TD().addElement(converterField.getEditor()).addElement(new BR()).addElement(
            getNewComponentSelect(
                pers,
                PipeComponentIF.TYPE_CONVERTER,
                labels.getLabel("add"),
                labels.getLocale()));
    }

} // SyncroEditPipe
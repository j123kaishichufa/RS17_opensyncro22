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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.formatter.Formatter;
import smilehouse.gui.html.fieldbased.formatter.IntegerFormatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.system.Persister;
import smilehouse.tools.template.Template;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;

public class SyncroPipeSettings extends SyncroServlet {

	private GUIContext generalFieldsContext;
	private static final String NAME = "name";


    private static final String START_PASSWORD = "start_password";
    //private static final String ENABLE_RPC_START = "enable_rpc_start";
    private static final String ENABLE_HTTP_START = "enable_http_start";
    private static final String VERBOSITY_LEVEL = "verbosity_level";
    private static final String NOTIFICATION_LEVEL = "notification_level";
    private static final String RECIPIENT="recipient";
    private static final String MAIL_HOST="mail_host";
    private static final String ENABLE_ABORT_MAIL="send_abort_mail";
 
    private static final String PIPE_ID = "pipeid";
    private Pipe pipe;
    private static final String PIPE_ID_SESSION_KEY = "syncro.admin.pipe_id";

    private static final String GENERAL_FIELDS_LAYOUT = "<table><tr valign=\"top\"><td>$" + NAME
    + "$</td></table>" + "<table><tr><td>$" + START_PASSWORD + "$</td><td>$"
    + ENABLE_HTTP_START + "$</td><td align=\"right\">$" + VERBOSITY_LEVEL
    + "$</td></tr><tr><td>$"+MAIL_HOST+"$</td><td>$"+RECIPIENT+"$</td><td>$"
    +NOTIFICATION_LEVEL+"$<TD></tr><tr><td>$"+ENABLE_ABORT_MAIL+"$</td</tr></table>";
    private static final int ACTION_CANCEL = 0;
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
	public void init(ServletConfig config) throws ServletException {
        try {
            generalFieldsContext = new GUIContext();
            {
                // ----------
                // Name field
                // ----------
                String id = NAME;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getName();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setName((String) value);
                    }
                };
                TextEditor editor = new TextEditor();
                editor.setSize(75);
                editor.setFormatter(new NullHidingFormatter());
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }
            {
                // --------------
                // Password field
                // --------------
                String id = START_PASSWORD;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getStartPassword();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setStartPassword((String) value);
                    }
                };
                PasswordEditor editor = new PasswordEditor();
                editor.setSize(20);
                editor.setFormatter(new NullHidingFormatter());
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }
/*
            {
                // ----------------------
                // Enable RPC start field
                // ----------------------
                String id = ENABLE_RPC_START;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return new Boolean(((Pipe) model).isRpcStartEnabled());
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setRpcStartEnabled(((Boolean) value).booleanValue());
                    }
                };
                BooleanEditor editor = new BooleanEditor();
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }
*/
            {
                // ----------------------
                // Enable HTTP start field
                // ----------------------
                String id = ENABLE_HTTP_START;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return new Boolean(((Pipe) model).isHttpStartEnabled());
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setHttpStartEnabled(((Boolean) value).booleanValue());
                    }
                };
                BooleanEditor editor = new BooleanEditor();
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }
            {
                // -------------------------
                // Verbosity level drop down
                // -------------------------
                String id = VERBOSITY_LEVEL;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return new Integer(((Pipe) model).getLoggingVerbosityLevel());
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setLoggingVerbosityLevel(((Integer) value).intValue());
                    }
                };
                SelectEditor editor = new SelectEditor();
                editor.addOption(new DefaultSelectOption(
                    new Integer(MessageLogger.LOG_ERROR),
                    "log_errors"));
                editor.addOption(new DefaultSelectOption(
                    new Integer(MessageLogger.LOG_WARNING),
                    "log_warnings"));
                editor.addOption(new DefaultSelectOption(
                    new Integer(MessageLogger.LOG_DEBUG),
                    "log_debug"));
                editor.addOption(new DefaultSelectOption(
                        new Integer(MessageLogger.LOG_DYNAMIC),
                        "log_dynamic"));
                editor.setFormatter(new IntegerFormatter());

                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }
            {
                // -------------------------
                // Transfer log notification level drop down
                // -------------------------
                String id = NOTIFICATION_LEVEL;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return new Integer(((Pipe) model).getTransferLogNotificationLevel());
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setTransferLogNotificationLevel(((Integer) value).intValue());
                    }
                };
                SelectEditor editor = new SelectEditor();
                editor.addOption(new DefaultSelectOption(
                    new Integer(MessageLogger.MAIL_ERROR),
                    "log_errors"));
                editor.addOption(new DefaultSelectOption(
                    new Integer(MessageLogger.MAIL_WARNING),
                    "log_warnings"));
                editor.addOption(new DefaultSelectOption(
                    new Integer(MessageLogger.MAIL_DEBUG),
                    "log_debug"));
                editor.addOption(new DefaultSelectOption(
                		new Integer(MessageLogger.MAIL_NONE),
                	"mail_none"));
                editor.setFormatter(new IntegerFormatter());

                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }{
                // ----------
                // Mail server field
                // ----------
                String id = MAIL_HOST;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getMailHost();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setMailHost((String) value);
                    }
                };
                TextEditor editor = new TextEditor();
                editor.setSize(25);
                editor.setFormatter(new NullHidingFormatter());
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }{
                // ----------
                // Recipient name field
                // ----------
                String id = RECIPIENT;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).getRecipientAddress();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setRecipientAddress((String) value);
                    }
                };
                TextEditor editor = new TextEditor();
                editor.setSize(25);
                editor.setFormatter(new NullHidingFormatter());
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
            }{
                // ----------
                // Send-mail-when-pipe-aborts checkbox
                // ----------
                String id = ENABLE_ABORT_MAIL;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((Pipe) model).isAbortMailEnabled();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((Pipe) model).setAbortMailEnabled(((Boolean) value).booleanValue());
                    }
                };
                BooleanEditor editor = new BooleanEditor();
                generalFieldsContext.addFieldInfo(new FieldInfo(id, id, modifier, editor));
           
                }

        } catch(Exception e) {
            throw new ServletException(
                "Problem with initializing bean fields. Fields probably incorrectly defined.",
                e);
        }
    }

	public void initParameters(ParameterManager pm) {

        pm.addParameter(PIPE_ID, ParameterManager.LONG);
        pm.addParameter(ACTION, ParameterManager.INT);

	}


	public int getTabId() {

		return 3;
	}


	public int getTabSetId() {

		return 2;
	}


	public String handleRequest(HttpServletRequest req, HttpSession session,
			ParameterManager parameters, Persister pers, LabelResource labels,
			int requestType) {
        this.pipe = getPipe(parameters, session, pers);
        if(this.pipe == null)
            return "";
        
        
        java.util.Map generalFields = generalFieldsContext.makeFields(pipe, labels, req);
        
        if(requestType == POST_REQUEST) {
            if(parameters.getInt(ACTION) != ACTION_CANCEL) {
        generalFieldsContext.commitFields(generalFields);
        pers.update(pipe);
            }
        }
        Template generalFieldsTemplate = Template.createTemplate(GENERAL_FIELDS_LAYOUT);

        generalFieldsContext.writeEditors(generalFields, generalFieldsTemplate);

        generalFieldsTemplate.write();
        ElementContainer content = new ElementContainer().addElement(
                generalFieldsTemplate.toString()).addElement(new BR()).addElement(new BR());
        Form form = getForm("PipeSettings", content.toString(), labels, false, true).addElement(
                parameters.getInput(Input.HIDDEN, PIPE_ID));
        return form.toString();
	}

}

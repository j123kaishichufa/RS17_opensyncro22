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

package smilehouse.opensyncro.servlets.standalone;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.html.Form;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;

import smilehouse.gui.html.fieldbased.ContainerEditor;
import smilehouse.gui.html.fieldbased.ContainerFieldInfo;
import smilehouse.gui.html.fieldbased.Field;
import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.editor.UneditingEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.servlets.SyncroServlet;
import smilehouse.opensyncro.system.Persister;
import smilehouse.opensyncro.user.User;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;
import smilehouse.util.RecallingList;

/**
 * Users.java
 * 
 * Created: Thu Apr 22 15:26:55 2004
 */

public class Users extends SyncroServlet {

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";

    private static final String NEW_USER_ID = "newUser";

    private static final String NEW_USER_NAME = "New User";
    
    private static final String userLoginFormatRegex = "[\\p{L}\\p{Digit}]+";
    
    private static ContainerFieldInfo userListFieldInfo;

    /**
     * Init called before this servlet serves any requests. This implementation initialises the
     * GUIContext ant the testobject instance variables.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            ModelModifier userListModifier = new DefaultModelModifier() {
                public Object getModelValue(Object model) throws Exception {
                    return ((ListModel) model).getList();
                }

                public void setModelValue(Object model, Object value) throws Exception {
                    ((ListModel) model).setList((RecallingList) value);
                }
            };
            ContainerEditor userListEditor = new ContainerEditor();
            userListEditor.enableDelete("delete");
            //containerEditor.enableAdd(Pipe.class, "add");
            userListFieldInfo = new ContainerFieldInfo(
                "list",
                "users",
                userListModifier,
                userListEditor);
            {
                // -----
                // Login
                // -----
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((User) model).getLogin();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                    // We won't set it here...
                    }
                };
                UneditingEditor editor = new UneditingEditor();

                FieldInfo fieldInfo = new FieldInfo(LOGIN, LOGIN, modifier, editor);
                userListFieldInfo.addColumn(fieldInfo);

            }
            {
                // --------
                // Password
                // --------
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ""; // We won't show it
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        String strValue = (String) value;
                        if(strValue != null && strValue.length() > 0)
                            ((User) model).setPassword(strValue);
                    }
                };
                PasswordEditor editor = new PasswordEditor();
                editor.setSize(20);

                FieldInfo fieldInfo = new FieldInfo(PASSWORD, PASSWORD, modifier, editor);
                userListFieldInfo.addColumn(fieldInfo);
            }
            {
                // ----
                // Name
                // ----
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((User) model).getName();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((User) model).setName((String) value);
                    }
                };
                TextEditor editor = new TextEditor();
                editor.setSize(50);

                FieldInfo fieldInfo = new FieldInfo(NAME, NAME, modifier, editor);
                userListFieldInfo.addColumn(fieldInfo);
            }
        } catch(Exception e) {
            throw new ServletException(
                "problem with initialising bean fields. Fields probably incorrectly defined.",
                e);
        }
    }


    public void initParameters(ParameterManager pm) {
        pm.addParameter(ACTION, ParameterManager.INT);
        pm.addParameter(NEW_USER_ID, ParameterManager.STRING);
    }

    public int getTabSetId() {
        return 1;
    }

    public int getTabId() {
        return 1;
    }


    public String handleRequest(HttpServletRequest req,
                                HttpSession session,
                                ParameterManager parameters,
                                Persister pers,
                                LabelResource labels,
                                int requestType){

        boolean newUserAdded = false;
        boolean newUserLoginAlreadyExistsError = false;
        boolean invalidNewUserLoginError = false;
        
        // ------------------------------
        // Create a new user if asked for
        // ------------------------------
        String newUserId = parameters.getString(NEW_USER_ID);
        if(newUserId != null && newUserId.length() > 0) {
            if(pers.userExists(newUserId)) {
                newUserLoginAlreadyExistsError = true;                
            } else {
                
                Pattern pattern = Pattern.compile(userLoginFormatRegex);
                Matcher matcher = pattern.matcher(newUserId);
                if(matcher.matches()) {
                    User user = new User(newUserId);
                    user.setName(NEW_USER_NAME);
                    pers.save(user);
                    newUserAdded = true;
                } else {
                    invalidNewUserLoginError = true;
                }
                
            }
        }

        ListModel users = new ListModel(pers.loadAllUsers());
        Field listField = userListFieldInfo.getField(
            users,
            environment.getLabelResource(session),
            req);

        if(requestType == POST_REQUEST && parameters.getInt(ACTION) == 1) {

            if(!newUserAdded) {
                listField.commit();
            }

            listField.revert();
            
            for(Iterator current = users.getList().iterator(); current.hasNext();) {
                User user = (User) current.next();
                pers.update(user);
                //environment.log("Updated user #" + user.getId().toString());
            }

            for(Iterator removed = users.getList().removedIterator(); removed.hasNext();) {
                User user = (User) removed.next();
                //environment.log("Removing user #" + user.getId().toString());
                pers.delete(user);
            }

        }

        // ----------
        // New-button
        // ----------
        Input newButton = new Input(Input.BUTTON, "nwbttn", labels.getLabel("create"));
        newButton.setOnClick("addNewUser()");


        Form form = getFormWithCustomButton("Users", listField.getEditor(),
            								newButton, labels, false, true);
        form.addElement(new Input(Input.HIDDEN, NEW_USER_ID, ""));

        // Add Javascript alert box to display error message if a new user was created with
        // already existing login name
        
        String scriptString = new String("");
        if(invalidNewUserLoginError == true) {
            scriptString = getErrorAlertCommand(labels.getLabel("invalid_new_user_login"));
        } else if(newUserLoginAlreadyExistsError == true) {
            scriptString = getErrorAlertCommand(labels.getLabel("new_user_login_exists"));
        }
        
        return form.toString() + scriptString;
    }

    private String getErrorAlertCommand(String errorText) {
        
        // We use setTimeout to prevent browser's rendering from stopping before
        // the full page is rendered
        String errorString = "setTimeout(\"alert('" + errorText + "');\",1);";
        
        Script script = new Script()
        .setLanguage("JavaScript")
        .setType("text/javascript")
        .addElement(errorString);

        return script.toString();
    }

    public String getScript(LabelResource labels) {
        return "function addNewUser() {\n" + " var userId = prompt('"
                + labels.getLabel("new_user_prompt") + "');\n"
                + " if(userId != null && userId.length > 0) {\n" + "  document.forms[0]."
                + NEW_USER_ID + ".value=userId;\n" + "  document.forms[0]." + ACTION
                + ".value=1;\n" + "  document.forms[0].submit();\n" + " }\n" + "}";
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
} // Users
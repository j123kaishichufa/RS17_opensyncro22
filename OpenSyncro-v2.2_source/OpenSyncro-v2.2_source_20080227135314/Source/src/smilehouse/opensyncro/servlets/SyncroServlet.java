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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ecs.AlignType;
import org.apache.ecs.Entities;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.Persister;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterInitializer;
import smilehouse.util.ParameterManager;

/**
 * SyncroServlet.java
 * 
 * Created: Wed Mar 10 14:12:11 2004
 */

public abstract class SyncroServlet extends HttpServlet implements ParameterInitializer {

    protected static final int GET_REQUEST = 0;
    protected static final int POST_REQUEST = 1;

    protected static final String ACTION = "action";

    protected static final Environment environment = Environment.getInstance();

    public abstract void initParameters(ParameterManager pm);

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {
        doVoodoo(req, res, GET_REQUEST);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {
        doVoodoo(req, res, POST_REQUEST);
    }

    public void doVoodoo(HttpServletRequest req, HttpServletResponse res, int requestType)
            throws IOException, ServletException {
        HttpSession session = environment.getSession(req, res);
        if(session == null) // No session? Probably the authentication failed...
            return;

        String database = environment.getDatabaseName(session);

        ParameterManager parameters = new ParameterManager(this, req, environment.getCharsetWWW());

        Persister pers = null;
        PrintWriter out = null;
        try {
            res.setContentType(environment.getContentType());
            out = res.getWriter();
            pers = new Persister(database);
            LabelResource labels = environment.getLabelResource(session);

            Script script = null;
            String scriptString = getScript(labels);
            if(scriptString != null)
                script = new Script()
                    .setLanguage("JavaScript")
                    .setType("text/javascript")
                    .addElement(scriptString);

            String content = handleRequest(req, session, parameters, pers, labels, requestType);

            environment.outputPage(getTabSetId(), getTabId(), content, script != null ? script
                .toString() : null, out, session);
            
        } catch(Throwable e) {
            environment.outputErrorPage(e, out);
        } finally {
            if(pers != null)
                pers.close();
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }
    
    /** Creates a form with optional close and submit/cancel buttons */
    protected Form getForm(String action,
                           String content,
                           LabelResource labels,
                           boolean hasCloseButton, 
                           boolean hasSubmitAndCancelButtons) {

        return getFormWithCustomButton(action, 
            							content,
            							null,
            							labels,
            							hasCloseButton,
            							hasSubmitAndCancelButtons);
    }
    
    /** Creates a form with optional close, submit/cancel and a custom button.
        customButton will be left aligned, while the close, submit and
        cancel buttons are aligned to the right corner.
        */
    protected Form getFormWithCustomButton(String action,
                                            String content,
                                            Input customButton,
                                            LabelResource labels,
                                            boolean hasCloseButton, 
                                            boolean hasSubmitAndCancelButtons) {
        Form form = new Form(action, Form.POST, Form.ENC_DEFAULT).setAcceptCharset(
            environment.getCharsetWWW()).addElement(content);

        // See if buttons are to be added to the bottom of the tab
        if(hasCloseButton || hasSubmitAndCancelButtons || 
                (customButton != null)) {
            form.addElement(new HR());
            form.addElement(new Input(Input.HIDDEN, ACTION, "1"));

            TR buttonRow = new TR();
            
            if(customButton != null) {
                buttonRow.addElement(new TD().addElement(customButton));
            }

            if(hasCloseButton) {
                Input closeButton = new Input(Input.BUTTON, "clb", labels.getLabel("close_button"));
                closeButton.setOnClick("window.close()");
                buttonRow.addElement(new TD().addElement(closeButton));
            }

            //hmmm, might cause tr without a single td... is it bad?
            if(hasSubmitAndCancelButtons) {
                Input okButton = new Input(Input.BUTTON, "okb", labels.getLabel("ok_button"));
                okButton
                    .setOnClick("document.forms[0].action.value='1';document.forms[0].submit()");
                Input cancelButton = new Input(Input.BUTTON, "cancelb", labels
                    .getLabel("cancel_button"));
                cancelButton
                    .setOnClick("document.forms[0].action.value='0';document.forms[0].submit()");
                buttonRow.addElement(new TD()
                    .setAlign(AlignType.RIGHT)
                    .addElement(okButton)
                    .addElement(Entities.NBSP)
                    .addElement(cancelButton));
            }
            form.addElement(new Table().setWidth("100%").addElement(buttonRow));
        }
        return form;
    }

    public abstract int getTabId();

    public abstract int getTabSetId();

    public abstract String handleRequest(HttpServletRequest req,
                                         HttpSession session,
                                         ParameterManager parameters,
                                         Persister pers,
                                         LabelResource labels,
                                         int requestType);

    public String getScript(LabelResource labels) {
        return null;
    }
    
} // SyncroServlet
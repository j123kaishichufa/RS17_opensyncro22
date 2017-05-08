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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ecs.AlignType;
import org.apache.ecs.Doctype;
import org.apache.ecs.Document;
import org.apache.ecs.Element;
import org.apache.ecs.html.B;
import org.apache.ecs.html.Body;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Head;
import org.apache.ecs.html.Html;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Link;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.Title;

import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.Persister;
import smilehouse.opensyncro.system.StandaloneEnvironment;

import smilehouse.util.Utils;

/**
 * Login.java
 * 
 * Created: Mon Mar 29 17:22:23 2004
 */

public class Login extends HttpServlet {

    private static final String DATABASE = "db";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String LANGUAGE = "lang";

    private static final String[][] LANGUAGES = { {"English", "en"}, {"Suomi", "fi"}};

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        // -----------------------------------------------
        // If log out is requested, invalidate the session
        // ------------------------------------------------
        if(req.getParameter("logout") != null) {
            HttpSession session = req.getSession(false);
            if(session != null)
                session.invalidate();
        }

        String message = null;
        if(req.getParameter("failed") != null)
            message = "Login failed, please check your user name and password.";
        if(req.getParameter("error") != null)
            message = "INTERNAL ERROR! Contact the administrator. Stacktrace written to the log.";
        
        // Get database name URL query parameter to pre-fill the Database input field
        String database = req.getParameter(DATABASE);
        if(database == null) {
            database = "";
        } 
        
        // ---------------
        // Language select
        // ---------------
        Select languageSelect = new Select(LANGUAGE);
        for(int i = 0; i < LANGUAGES.length; i++)
            languageSelect.addElement(new Option(LANGUAGES[i][1]).addElement(LANGUAGES[i][0]));

        Table table = new Table()
            .addElement(
                new TR().addElement(
                    new TD().setAlign(AlignType.RIGHT).addElement(new B("Database"))).addElement(
                    new TD().addElement(new Input(Input.TEXT, DATABASE,
                        Utils.htmlentities(database)).setSize(20))))
            .addElement(
                new TR()
                    .addElement(new TD().setAlign(AlignType.RIGHT).addElement(new B("User")))
                    .addElement(new TD().addElement(new Input(Input.TEXT, USER, "").setSize(20))))
            .addElement(
                new TR().addElement(
                    new TD().setAlign(AlignType.RIGHT).addElement(new B("Password"))).addElement(
                    new TD().addElement(new Input(Input.PASSWORD, PASSWORD, "").setSize(20))))
            .addElement(
                new TR().addElement(
                    new TD().setAlign(AlignType.RIGHT).addElement(new B("Language"))).addElement(
                    new TD().addElement(languageSelect)))
            .addElement(
                new TR().addElement(new TD().setColSpan(2).setAlign(AlignType.CENTER).addElement(
                    new HR()).addElement(new Input(Input.SUBMIT, "login", "Login"))));
        Body body = new Body().addElement(getBox("pics/tab/", new Form(
            "Login",
            Form.POST,
            Form.ENC_DEFAULT).addElement(table), "Smilehouse OpenSyncro Login"));

        // --------------
        // Message pop up
        // --------------
        if(message != null) {
            body.addElement(new Script().setLanguage("JavaScript").addElement(
                "alert('" + message + "')"));
        }

        Document document = new Document().setDoctype(new Doctype.Html40Transitional()).setHtml(
            new Html()
                .addElement(
                    new Head().addElement(new Title("Smilehouse OpenSyncro")).addElement(
                        new Link().setRel("stylesheet").setType("text/css").setHref(
                            "smilestyle.css")))
                .addElement(body));

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        try {
            document.output(out);
        } catch(Exception e) {
            Environment.getInstance().log("Exception in login", e);
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }

    }

    // Throws also ServletException?
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String database = req.getParameter(DATABASE);
        String user = req.getParameter(USER);
        String password = req.getParameter(PASSWORD);
        String lang = req.getParameter(LANGUAGE);

        if(database == null || database.length() == 0) {
            res.sendRedirect("Login?");
        } else {
            Persister pers = null;
            try {
                pers = new Persister(database, true);
                if(pers.loginOk(user, password)) {
                    StandaloneEnvironment.logInUser(req, user, database, lang);
                    res.sendRedirect("Index");
                } else
                    res.sendRedirect("Login?failed=y");
            } catch(Persister.DatabaseConnectionException dbce) {
                Environment.getInstance().log("Error: Attempt to log in to an unknown database '" + database + "'");
                res.sendRedirect("Login?failed=y");
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Exception while trying to log in the database '" + database + "'",
                    e);
                res.sendRedirect("Login?error=y");
            } finally {
                if(pers != null)
                    pers.close();
            }
        }
    }


    /**
     * Makes a table like getCardtable in adminhouse, but without the cards.
     * 
     * @param picRoot the picture root directory.
     * @param element The contents for this table
     * @param header Header text. A bolded text over the table. May be null.
     * @return The shopTable value
     */
    public static Table getBox(String picRoot, Element element, String header) {
        String bgColor = "#EEEEEE";
        if(header == null) {
            header = "&nbsp;";
        }

        Table table = new Table()
            .setBorder(0)
            .setAlign("center")
            .setCellSpacing(0)
            .setCellPadding(0)
            .addElement(
                new TR()
                    .addElement(
                        new TD().addElement(new IMG()
                            .setSrc(picRoot + "corner_topleft.gif")
                            .setWidth(5)
                            .setBorder(0)))
                    .addElement(
                        new TD()
                            .setBackground(picRoot + "emptytab_bg.gif")
                            .setAlign("center")
                            .addElement(new B().addElement(header))
                            .setClass("tab"))
                    .addElement(
                        new TD().setWidth(2).addElement(
                            new IMG()
                                .setSrc(picRoot + "corner_righttop.gif")
                                .setWidth(5)
                                .setBorder(0))))
            .addElement(
                new TR().addElement(
                    new TD().setBackground(picRoot + "left_bg.gif").setWidth(5).addElement(
                        new IMG()
                            .setSrc(picRoot + "transparent.gif")
                            .setHeight(5)
                            .setWidth(5)
                            .setBorder(0))).addElement(
                    new TD().setBgColor(bgColor).setAlign("left").addElement(element).setClass(
                        "text")).addElement(
                    new TD().setBackground(picRoot + "right_bg.gif").addElement(
                        new IMG()
                            .setSrc(picRoot + "transparent.gif")
                            .setHeight(5)
                            .setWidth(5)
                            .setBorder(0))))
            .addElement(
                new TR().addElement(
                    new TD().addElement(new IMG()
                        .setSrc(picRoot + "corner_bottomleft.gif")
                        .setWidth(5)
                        .setBorder(0))).addElement(
                    new TD().setBackground(picRoot + "bottom_bg.gif").addElement(
                        new IMG()
                            .setSrc(picRoot + "transparent.gif")
                            .setHeight(1)
                            .setWidth(1)
                            .setBorder(0))).addElement(
                    new TD().addElement(new IMG()
                        .setSrc(picRoot + "corner_bottomright.gif")
                        .setWidth(5)
                        .setBorder(0))));
        return table;
    }

} // Login
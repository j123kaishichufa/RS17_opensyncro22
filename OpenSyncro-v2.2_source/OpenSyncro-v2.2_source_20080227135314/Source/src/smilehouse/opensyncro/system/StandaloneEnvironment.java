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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ecs.Doctype;
import org.apache.ecs.Document;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.Body;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.Head;
import org.apache.ecs.html.Html;
import org.apache.ecs.html.Link;
import org.apache.ecs.html.Meta;
import org.apache.ecs.html.PRE;

import smilehouse.tools.ui.web.TabGenerator;
import smilehouse.util.Log;
import smilehouse.util.Utils;

/**
 * StandaloneEnvironment.java
 * 
 * Created: Mon Mar 29 11:48:12 2004
 */

public class StandaloneEnvironment extends Environment {

    private static final String IMAGE_ROOT = "pics/tab/";
    private static final String MENU_FILE = "/smilehouse/opensyncro/system/opensyncro.menu";

    private static final String SESSION_LANGUAGE_ATTR = "syncro.lang";
    public static final String SESSION_USER_ATTR = "syncro.user";
    private static final String SESSION_DATABASE_ATTR = "syncro.database";

    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss:SSS";
    private static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    private String jdbcUri;
    private String jdbcUser;
    private String jdbcPassword;
    private String jdbcDriver;

    private String charsetWeb;
    private String charsetDatabase;

    private Log logger;
    private TabGenerator tabGenerator;

    public static HashSet standaloneZones = new HashSet();
    static {
        standaloneZones.add("syncro_standalone");
    }

    public StandaloneEnvironment() throws Exception {

        // --------------
        // Initialize log
        // --------------
        try {
            this.logger = new Log(getProperty("log.filename"));
        } catch(FileNotFoundException e) {
            System.err.println(dateFormat.format(new Date()) + " OpenSyncro: Couldn't open log file '"
                    + getProperty("log.filename") + "'");
        }

        // -----------------------------------
        // Get JDBC parameters from properties
        // -----------------------------------
        jdbcDriver = getProperty("jdbc.driver");
        jdbcUser = getProperty("jdbc.user");
        jdbcPassword = getProperty("jdbc.password");
        jdbcUri = getProperty("jdbc.uri");
        // Db driver...
        try {
            Class.forName(jdbcDriver);
        } catch(Exception e) {
            log("Cannot find JDBC driver " + jdbcDriver, e);
            throw e;
        }
        // ------------------------
        // Some other properties...
        // ------------------------
        charsetWeb = getProperty("charset.web");
        charsetDatabase = getProperty("charset.database");

        // -----------------------
        // Initialize TabGenerator
        // -----------------------
        this.tabGenerator = new TabGenerator(IMAGE_ROOT, this);
    }

    public Connection getConnection(String db) throws SQLException {
        try {
            return getBasicConnection(db);
        } catch(SQLException e) {
            log("Couldn't get database connection", e);
            throw e;
        }
    }
       
    public Connection getConnectionSuppressException(String db) throws SQLException {
        try {
            return getBasicConnection(db);
        } catch(SQLException e) {
            throw e;
        }
    }
    
    private Connection getBasicConnection(String db) throws SQLException {
        String uriGeller = jdbcUri + db;
        if(charsetDatabase != null && !charsetDatabase.equals(""))
            uriGeller += "?useUnicode=true&characterEncoding=" + charsetDatabase;
        return DriverManager.getConnection(uriGeller, jdbcUser, jdbcPassword);
    }

    public void freeConnection(Connection con) throws SQLException {
        con.close();
    }


    public String getContentType() {
        return "text/html; charset=" + charsetWeb;
    }

    public String getCharsetWWW() {
        return charsetWeb;
    }

    // Throws also ServletException?
    public HttpSession getSession(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // set charset
        req.setCharacterEncoding(charsetWeb);

        HttpSession session = req.getSession(true);

        // -----------------------------------
        // Check that there's a user logged in
        // -----------------------------------
        String userName = (String) session.getAttribute(SESSION_USER_ATTR);
        if(userName == null || userName.length() == 0) {
            res.sendRedirect("Login");
            return null;
        } else {
            try {
                // Don't even think of cacheing this page...
                res.setHeader("Pragma", "No-cache");
                res.setDateHeader("Expires", 0);
                res.setHeader("Cache-Control", "no-cache");

                return session;
            } catch(Exception e) {
                log("An error has occured in getSession-method", e);
                return session;
            }
        }
    }

    private Body getBody() {
        Body body = new Body();
        body.setOnLoad("if(parent.nav && parent.nav.createMenus) parent.nav.createMenus()");
        return body;
    }

    public void outputPage(int tabSetId,
                           int tabId,
                           String content,
                           String toHead,
                           PrintWriter out,
                           HttpSession session) throws Exception {

        String lang = (String) session.getAttribute(SESSION_LANGUAGE_ATTR);

        Body body = getBody();

        Head head = new Head().addElement(
            new Meta().setHttpEquiv("Content-Type").setContent(getContentType())).addElement(
            tabGenerator.getStyle()).addElement(
            new Link().setRel("stylesheet").setType("text/css").setHref("smilestyle.css"));
        if(toHead != null)
            head.addElement(toHead);

        Document doc = new Document().setDoctype(new Doctype.Html40Transitional()).setHtml(
            new Html().addElement(head).addElement(
                body.addElement(tabGenerator.getTabSet(tabSetId, tabId, new ElementContainer()
                    .addElement(content), MENU_FILE, standaloneZones, lang))));
        doc.output(out);
    }

    public void outputErrorPage(Throwable e, PrintWriter out) {
        Body body = getBody();
        body.addElement(new H1("ERROR!")).addElement(new PRE().addElement(Utils.getStackTrace(e))
//          .addElement(e.getCause().toString())
            );

        Document doc = new Document().setDoctype(new Doctype.Html40Transitional()).setHtml(
            new Html().addElement(new Head()).addElement(body));

        doc.output(out);
    }


    public void log(String message) {
        if(logger != null)
            logger.write(message);
        else
            System.err.println(dateFormat.format(new Date()) + " OpenSyncro (couldn't open log file '"
                    + getProperty("log.filename") + "'): " + message);
    }

    public void log(String message, Throwable t) {
        log(message + "\n" + Utils.getStackTrace(t));
    }

    public void log(String message, Exception e) {
        log(message, (Throwable) e);
    }

    public String getDatabaseName(HttpSession session) {
        return (String) session.getAttribute(SESSION_DATABASE_ATTR);
    }

    public String getLanguage(HttpSession session) {
        return (String) session.getAttribute(SESSION_LANGUAGE_ATTR);
    }

    public static void logInUser(HttpServletRequest req,
                                 String user,
                                 String database,
                                 String language) {
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_USER_ATTR, user);
        session.setAttribute(SESSION_LANGUAGE_ATTR, language);
        session.setAttribute(SESSION_DATABASE_ATTR, database);
    }

} // StandaloneEnvironment
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ecs.AlignType;
import org.apache.ecs.Doctype;
import org.apache.ecs.Document;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Body;
import org.apache.ecs.html.Head;
import org.apache.ecs.html.Html;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Link;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.StandaloneEnvironment;
import smilehouse.tools.ui.web.MenuGenerator;
import smilehouse.util.LabelResource;
import smilehouse.util.Utils;

/**
 * Nav.java
 * 
 * Created: Tue Mar 30 11:26:41 2004
 */

public class Nav extends HttpServlet {

    private String OPENSYNCRO_VERSION = "v2.2";
    
    private String MENU_NAME = "opensyncro";
    private String MENU_BG_IMAGE = "pics/greyline_bg.gif";
    private String TOPBAR_BG_IMAGE = "pics/top_bar_bg.gif";
    private String MENU_SPACER_IMAGE = "pics/tab/transparent.gif";

    private MenuGenerator menuGenerator;
    private static Environment environment = Environment.getInstance();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.menuGenerator = new MenuGenerator(
            "/smilehouse/opensyncro/system/",
            "dynapi/",
            "",
            "main",
            "pics/nuoli.gif",
            environment);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {
        HttpSession session = environment.getSession(req, res);
        if(session == null) // No session? Probably the authentication failed...
            return;

        LabelResource labels = environment.getLabelResource(session);
        
        String lang = environment.getLanguage(session);

        res.setContentType(environment.getContentType());
        PrintWriter out = res.getWriter();
        try {
            Table menuBar = menuGenerator.getMenuBar(
                MENU_NAME,
                null,
                MENU_BG_IMAGE,
                MENU_SPACER_IMAGE,
                lang);
            Head head = new Head().addElement(new Link()
                .setRel("stylesheet")
                .setType("text/css")
                .setHref("smilemenu.css"));
            menuGenerator.createMenu(MENU_NAME, head, StandaloneEnvironment.standaloneZones, lang);

            TD logoCell = new TD()
                .setVAlign(AlignType.TOP)
                .setBackground(TOPBAR_BG_IMAGE)
                .addElement(new IMG("pics/logo_smilehouse.gif"));

            TR topRow = new TR().addElement(logoCell).addElement(
                new TD().setBackground(TOPBAR_BG_IMAGE)).addElement(
                new TD().setBackground(TOPBAR_BG_IMAGE).setAlign(AlignType.RIGHT).addElement(
                    new Span().addElement("OpenSyncro " + OPENSYNCRO_VERSION).setClass("syncroTitle")));

            TR menuRow = new TR().setVAlign(AlignType.TOP).addElement(
                new TD().setBackground(MENU_BG_IMAGE).setVAlign(AlignType.BOTTOM).addElement(
                    menuBar)).addElement(
                new TD()
                    .setBackground(MENU_BG_IMAGE)
                    .setAlign(AlignType.RIGHT)
                    .setColSpan(2)
                    .addElement(
                        new Span().addElement(
                            new A("Login?logout=yes", "[" + labels.getLabel("logout") + "]")
                            .setTarget("_top")).setClass("syncroText")));

            Body body = new Body().addElement(new Table()
                .setWidth("100%")
                .setBorder(0)
                .setCellPadding(0)
                .setCellSpacing(0)
                .addElement(topRow)
                .addElement(menuRow));
            body.addAttribute("leftmargin", "0");
            body.addAttribute("topmargin", "0");
            body.addAttribute("marginwidth", "0");
            body.addAttribute("marginheight", "0");

            Document document = new Document()
                .setDoctype(new Doctype.Html40Transitional())
                .setHtml(new Html().addElement(head).addElement(body));

            document.output(out);
        } catch(Exception e) {
            out.println("<html><body><h3>Fatal error generating navigation frame content</h3><pre>"
                    + Utils.getStackTrace(e) + "</pre></body></html>");
        } finally {
            if(out != null)
                out.flush();
            out.close();
        }
    }

} // Nav
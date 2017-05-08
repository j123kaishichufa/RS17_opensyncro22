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

import org.apache.ecs.Doctype;
import org.apache.ecs.Document;
import org.apache.ecs.HtmlColor;
import org.apache.ecs.html.B;
import org.apache.ecs.html.Body;
import org.apache.ecs.html.Frame;
import org.apache.ecs.html.FrameSet;
import org.apache.ecs.html.Head;
import org.apache.ecs.html.Html;
import org.apache.ecs.html.NoFrames;
import org.apache.ecs.html.P;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.Title;

import smilehouse.opensyncro.system.Environment;
import smilehouse.util.Utils;

/**
 * Index.java
 * 
 * Created: Tue Mar 30 11:15:14 2004
 */

public class Index extends HttpServlet {

    private static Environment environment = Environment.getInstance();

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {
        HttpSession session = environment.getSession(req, res);
        if(session == null) // No session? Probably the authentication failed...
            return;

        res.setContentType(environment.getContentType());
        PrintWriter out = res.getWriter();
        try {
            FrameSet frameset = new FrameSet().setRows("34,*").addElement(
                new Frame()
                    .setName("nav")
                    .setSrc("Nav")
                    .setMarginWidth("0")
                    .setMarginHeight("0")
                    .setScrolling(Frame.no)
                    .setFrameBorder(false)
                    .setNoResize(true)).addElement(
                new Frame()
                    .setName("main")
                    .setSrc("PipeList")
                    .setMarginWidth("0")
                    .setMarginHeight("0")
                    .setScrolling(Frame.auto)
                    .setFrameBorder(false)
                    .setNoResize(true));
            frameset.addAttribute("border", "0");

            NoFrames noframes = new NoFrames().addElement(new Body()
                .setBgColor(HtmlColor.white)
                .setText(HtmlColor.black)
                .addElement(new B().addElement("Smilehouse OpenSyncro"))
                .addElement(new P())
                .addElement("Your browser can't handle frames. Please update your browser!"));

            Document document = new Document().setDoctype(new Doctype.Html40Frameset()).setHtml(
                new Html().addElement(
                    new Head().addElement(new Title("Smilehouse OpenSyncro (Standalone)"))
                    .addElement(new Script(
                        "if (top.location != location) top.location.href = document.location.href;")
                        .setLanguage("JavaScript")
                        )).addElement(
                    frameset).addElement(noframes));
            
            document.output(out);
        } catch(Exception e) {
            out.println("<html><body><h3>FATAL ERROR</h3><pre>" + Utils.getStackTrace(e)
                    + "</pre></body></html>");
        } finally {
            if(out != null)
                out.flush();
            out.close();
        }
    }

} // Index
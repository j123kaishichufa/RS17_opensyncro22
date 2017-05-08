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

package smilehouse.tools.ui.web;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Vector;

import org.apache.ecs.AlignType;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Font;
import org.apache.ecs.html.Head;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.util.LabelProvider;
import smilehouse.util.LabelResource;
import smilehouse.util.ResourceLabelProvider;
import smilehouse.util.SimpleParser;
import smilehouse.util.Utils;

/**
 * Class for generating javascripts needed for the dropdown menus. Created: Thu Mar 21 11:55:14 2002
 */

public class MenuGenerator {
    // ----------
    // Menu style
    // ----------
    /**
     * Description of the Field
     */
    public final static int ITEM_HEIGHT = 17;
    /**
     * Description of the Field
     */
    public final static String ENABLED_STYLE = "font-family: helvetica; font-size: 13px; color: black";
    /**
     * Description of the Field
     */
    public final static String DISABLED_STYLE = "font-family: helvetica; font-size: 13px; color: #808080";
    /**
     * Description of the Field
     */
    public final static String BG_COLOR = "#CECBCE";
    /**
     * Description of the Field
     */
    public final static String HIGHLIGHT_BG = "#EEEEEE";
    /**
     * Description of the Field
     */
    public final static String BORDER_COLOR = "black";
    /**
     * Description of the Field
     */
    public final static int BORDER_WIDTH = 1;

    /** This should be somewhere else... */
    static final String DISABLED_COLOR = "#909090";

    /**
     * Description of the Field
     */
    public final static int MENU_Y = 0;

    /**
     * Description of the Field
     */
    public final static String MENU_NAME_BASE = "menu";

    private LabelProvider labels;
    private String menuDirectory;
    private String mainWindowName;
    private String subMenuImageURL;

    private Script dynApiScript;
    private Script dynApiIncludeScript;

    // -----------------------------
    // Constants for keyword indexes
    // -----------------------------
    final static int MENU = 0;
    final static int WIDTH = 1;
    final static int XPOS = 2;
    final static int ITEM = 3;
    final static int ZONE = 4;
    final static int TEXT = 5;
    final static int LINK = 6;
    final static int CARDTABLE = 7;
    final static int TITLE = 10;
    final static int CTEXT = 13;
    final static int NOT_DROPDOWN = 9;
    final static int LABELRESOURCE = 14;

    // --------
    // Keywords
    // --------
    final static String[] KEYWORDS = {"menu",
    // 0
            "width",
            // 1
            "xpos",
            // 2
            "item",
            // 3
            "zone",
            // 4
            "text",
            // 5
            "link",
            // 6
            "tabset|cardtable",
            // 7
            "tab|card",
            // 8
            "notdropdown",
            // 9
            "title", "tablink", "help", "ctext", "labelresource"};


    // 13

    /**
     * Constructor.
     * 
     * @param menuDirectory The directory where the menu files are loaded.
     * @param dynApiRoot Path to the dynapi directory
     * @param smileMenuRoot Path to the smilemenu.js
     * @param mainWindowName Name of the main window
     * @param subMenuImageURL URL of the image marking the submenus
     * @param labels LabelProvider for the menu item texts
     */
    public MenuGenerator(String menuDirectory,
                         String dynApiRoot,
                         String smileMenuRoot,
                         String mainWindowName,
                         String subMenuImageURL,
                         LabelProvider labels) {
        this.menuDirectory = menuDirectory;
        this.mainWindowName = mainWindowName;
        this.subMenuImageURL = subMenuImageURL;
        this.labels = labels;

        // ------------------------------
        // Create some scripts in advance
        // ------------------------------
        dynApiScript = new Script().setType("text/javascript").setLanguage("JavaScript1.2").setSrc(
            dynApiRoot + "src/dynapi.js");

        dynApiIncludeScript = new Script().setLanguage("JavaScript1.2").addElement(
            "DynAPI.setLibraryPath('" + dynApiRoot + "src/lib/');\n"
                    + "DynAPI.include('dynapi.api.*');\n" + "DynAPI.include('dynapi.event.*');\n"
                    + "DynAPI.include(\"smilemenu.js\", \"" + smileMenuRoot + "\")\n");
    }


    /**
     * Constructs the scripts and adds them to the page header.
     * 
     * @param menuName The menufile name without the extension (.menu)
     * @param head Page header where to add the scripts
     * @param rights User's access rights
     * @param lang Language code
     * @return Table with the menubar... or maybe not.
     * @exception IOException Description of the Exception
     * @exception ParseException Description of the Exception
     */
    public Table createMenu(String menuName, Head head, HashSet rights, String lang)
            throws IOException, ParseException {
        // ----------------------------
        // Read the menus from the file
        // ----------------------------
        InputStream in = null;
        SimpleParser.Record[] menus = null;
        LabelResource labelResource = null;
        try {
            in = getClass().getResourceAsStream(menuDirectory + menuName + ".menu");
            SimpleParser parser = new SimpleParser(KEYWORDS, in);
            SimpleParser.Record all = parser.readAll();

            // --------------
            // Custom labels?
            // --------------
            SimpleParser.Entity labelEntity = all.getEntityByKeyword(LABELRESOURCE, false);
            if(labelEntity != null) {
                labelResource = getCustomLabelResource(labelEntity, lang);
            }
            if(labelResource == null)
                labelResource = new LabelResource(labels, lang);


            menus = all.getRecords(MENU);
        } finally {
            if(in != null)
                in.close();
        }

        // -------------------------------
        // Create the menu-creation script
        // -------------------------------
        String menuScript = constructMenuScript(menus, rights, labelResource);

        // ---------------------------
        // Add the scripts to the head
        // ---------------------------
        head.addElement(dynApiScript).addElement(dynApiIncludeScript).addElement(
            new Script().setLanguage("JavaScript1.2").addElement(menuScript).addElement(
                "\nDynAPI.onLoad = function() { createMenus(); };\n"));

        return null;
        //getMenuBar(menus, rights, lang);
    }


    protected static LabelResource getCustomLabelResource(SimpleParser.Entity labelEntity,
                                                          String lang) throws ParseException {

        LabelProvider customLabels = null;
        String[] labelParameters = labelEntity.getStringArray();
        if(labelParameters.length == 1)
            customLabels = new ResourceLabelProvider(labelParameters[0]);
        else if(labelParameters.length == 2)
            customLabels = new ResourceLabelProvider(labelParameters[0], labelParameters[1]);
        if(customLabels != null)
            return new LabelResource(customLabels, lang);
        else
            return null;
    }


    /**
     * Construct the menuscript.
     * 
     * @param menus Array of Records
     * @param access Accees rights
     * @return String containing the createMenus-script
     * @exception IOException Description of the Exception
     * @exception ParseException Description of the Exception
     */
    private String constructMenuScript(SimpleParser.Record[] menus,
                                       HashSet access,
                                       LabelResource labelResource) throws IOException,
            ParseException {
        // -------------------------------------------------------
        // Start the script with the creation of the MenuContainer
        // -------------------------------------------------------
        StringBuffer script = new StringBuffer("function createMenus() {\n"
                + "var menus = new MenuContainer(new MenuAppearance(" + ITEM_HEIGHT + ",'"
                + ENABLED_STYLE + "','" + DISABLED_STYLE + "','" + BG_COLOR + "','" + HIGHLIGHT_BG
                + "','" + BORDER_COLOR + "'," + BORDER_WIDTH + ", " + "'<IMG SRC=\""
                + subMenuImageURL + "\">'),\n" + MENU_Y + ", parent." + mainWindowName + ");\n"
                + "  parent." + mainWindowName
                + ".document.onmousedown = function() { menus.closeMenus(); };\n"
                + "  window.onunload = function() { menus.unloadMenus(); };\n" + "  parent."
                + mainWindowName + ".onunload = window.onunload;\n");

        // Separate buffer for the variable declarations placed outside the createMenus-function
        StringBuffer menuVariables = new StringBuffer("var menus;\n");

        // -------------------------------------------------------
        // Go through the menus and construct the creation scripts
        // -------------------------------------------------------
        for(int i = 0; i < menus.length; i++) {
            if(!menus[i].isSet(NOT_DROPDOWN)) {
                // Add the new declaration to menuVariables
                String menuName = MENU_NAME_BASE + menus[i].getInt(CARDTABLE);
                menuVariables.append("var ").append(menuName).append(";\n");

                // Construct the menu creation script
                script.append(menuName).append('=');
                readMenu(menus[i], false, access, script, labelResource);

                int xPos = 0;
                try {
                    xPos = menus[i].getInt(XPOS);
                } catch(ParseException e) {}
                script
                    .append(";\n")
                    .append("menus.addRootMenu(")
                    .append(menuName)
                    .append(',')
                    .append(xPos)
                    .append(");\n\n");
            }
        }

        script.append("menus.resetMenus();\n menus.menusLoaded();\n}\n");

        return menuVariables.toString() + script.toString();
    }


    /**
     * Writes one menu creation script into a StringBuffer.
     * 
     * @param menu The menu record
     * @param child Is this a child- or root-menu
     * @param access The access rights
     * @param script The StringBuffer where to append the script
     * @exception ParseException Description of the Exception
     */
    private void readMenu(SimpleParser.Record menu,
                          boolean child,
                          HashSet access,
                          StringBuffer script,
                          LabelResource labelResource) throws IOException, ParseException {
        // ----------
        // Menu width
        // ----------
        int width = 125;
        try {
            width = menu.getInt(WIDTH);
        } catch(ParseException e) {}
        script.append("new Menu(").append(width).append(")");

        // ------------------
        // Read the menuitems
        // ------------------
        boolean noItemsFound = true;
        SimpleParser.Record[] items = menu.getRecords(ITEM);
        for(int i = 0; items != null && i < items.length; i++) {
            // If zone is given, check that the user has access right for it
            String zone = items[i].getString(ZONE, false);
            if(zone != null && !access.contains(zone)) {
                continue;
            }

            script.append(".add(new MenuItem(");
            // Item text
            String text = items[i].getString(CTEXT, false);
            if(text == null) {
                text = labelResource.getLabel(items[i].getString(TEXT, true));
                if(text == null)
                    text = "";
            }
            script.append('"').append(Utils.htmlentities(text)).append('"');

            // Item link
            String link = items[i].getString(LINK, false);
            if(link != null) {
                script.append(",\"").append(link).append("\"");
            }

            script.append(")");
            // new MenuItem

            // Possible submenu
            SimpleParser.Record childMenu = items[i].getRecord(MENU, false);
            if(childMenu != null) {
                script.append(".setSubMenu(");
                readMenu(childMenu, true, access, script, labelResource);
                script.append(")");
                // .setSubMenu
            }
            script.append(")\n");
            // .add
        }
        if(noItemsFound) {
            // ...
        }
    }


    public Table getMenuBar(String menuName,
                            HashSet rights,
                            String bgImage,
                            String spacerImage,
                            String lang) throws IOException, ParseException {

        // ----------------------------
        // Read the menus from the file
        // ----------------------------
        InputStream in = null;
        SimpleParser.Record[] menus = null;
        try {
            in = getClass().getResourceAsStream(menuDirectory + menuName + ".menu");
            SimpleParser parser = new SimpleParser(KEYWORDS, in);
            menus = parser.readAll().getRecords(MENU);
        } finally {
            if(in != null)
                in.close();
        }

        Vector textVector = new Vector();
        Vector tabVector = new Vector();
        Vector widthVector = new Vector();

        int lastXPos = 0;

        for(int i = 0; i < menus.length; i++) {
            if(!menus[i].isSet(NOT_DROPDOWN)) {
                // Get the text (title)
                String text = menus[i].getString(TITLE, false);
                if(text == null)
                    text = "";
                else
                    text = labels.getLabel(text, lang);
                // Get the tab
                int cardTable = 0;
                SimpleParser.Entity entity = menus[i].getEntityByKeyword(CARDTABLE, false);
                if(entity != null)
                    cardTable = entity.getInt();
                // Get the xPos
                int xPos = 50 + lastXPos; // default
                entity = menus[i].getEntityByKeyword(XPOS, false);
                if(entity != null)
                    xPos = entity.getInt();

                // -----------------------------------
                // Check user's access rights to items
                // -----------------------------------
                if(rights != null) {
                    boolean accessibleItems = false;
                    SimpleParser.Record[] items = menus[i].getRecords(ITEM);
                    if(items != null)
                        for(int i2 = 0; i2 < items.length; i2++) {
                            String zone = items[i2].getString(ZONE, false);
                            if(zone == null || rights.contains(zone)) {
                                accessibleItems = true;
                                break;
                            }
                        }
                    if(!accessibleItems)
                        cardTable = 0;
                }
                // Save text, tab and width
                textVector.add(text);
                tabVector.add(new Integer(cardTable));
                widthVector.add(new Integer(xPos - lastXPos));
                lastXPos = xPos;
            }
        }

        // --------------------------
        // Table with the menu titles
        // --------------------------
        Table menuTable = new Table().setBorder(0).setCellSpacing(0).setCellPadding(0);
        menuTable.addAttribute("BACKGROUND", bgImage);

        if(widthVector.size() > 0) {
            // We'll count the total width of the table as we go
            int tableWidth = ((Integer) widthVector.get(0)).intValue();
            TR menuRow = new TR().setVAlign(AlignType.TOP)
            // An empty cell for padding
                .addElement(
                    new TD().setWidth(tableWidth).addElement(Entities.NBSP).addElement(
                        new IMG().setSrc(spacerImage).setWidth(tableWidth).setHeight(18)));
            // This is the width of the last cell...
            widthVector.addElement(new Integer(200));
            // Add the title cells
            for(int i = 0; i < textVector.size(); i++) {
                TD menuCell = new TD();
                int width = ((Integer) widthVector.get(i + 1)).intValue();
                int tab = ((Integer) tabVector.get(i)).intValue();
                String text = (String) textVector.get(i);
                tableWidth += width;
                menuCell.setWidth(width);

                Font menuTitle = new Font().setSize(2).setFace("Arial, Helvetica, sans-serif");

                if(tab != 0) {
                    A linkElement = new A("EmptyPage" /*
                                                       * + "?" + EmptyPage.MODULE + "=" + module +
                                                       * "&" + EmptyPage.MENU + "=" + tab + "&" +
                                                       * EmptyPage.TITLE + "=" +
                                                       * URLEncoder.encode(text,
                                                       * environment.getCharsetWWW())
                                                       */)
                        .setTarget("main")
                        .addElement(text);
                    String menuScriptName = MENU_NAME_BASE + tab;
                    linkElement.setOnClick("return openMenu(" + menuScriptName + ")");
                    menuCell.addElement(menuTitle.addElement(linkElement));
                } else
                    menuCell.addElement(menuTitle.addElement(text).setColor(DISABLED_COLOR));

                menuRow.addElement(menuCell);
            }
            menuTable.addElement(menuRow).setWidth(tableWidth + 2); // Don't ask me where the '+2'
                                                                    // comes from, but the last
                                                                    // menus don't work in netscape
                                                                    // without it...
        } else
            menuTable.addElement(new TR().addElement(new TD().addElement(Entities.NBSP)));
        return menuTable;
    }

}
// MenuGenerator



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
import java.util.Stack;
import java.util.Vector;

import org.apache.ecs.AlignType;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.A;
import org.apache.ecs.html.B;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.LI;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.Style;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.UL;

import smilehouse.util.LabelProvider;
import smilehouse.util.SimpleParser;

/**
 * TabGenerator.java Class for making tabsets based on menu-definition file. <br>
 * <b>Pictures that should be found from the image directory: </b> <table>
 * <tr>
 * <td>left_both.gif</td>
 * </tr>
 * <tr>
 * <td>right_both.gif</td>
 * </tr>
 * <tr>
 * <td>corner_righttop_1_outer.gif</td>
 * </tr>
 * <tr>
 * <td>corner_righttop_1_inner.gif</td>
 * </tr>
 * <tr>
 * <td>corner_righttop_2_outer.gif</td>
 * </tr>
 * <tr>
 * <td>corner_righttop_2_inner.gif</td>
 * </tr>
 * <tr>
 * <td>left_bg.gif</td>
 * </tr>
 * <tr>
 * <td>right_bg.gif</td>
 * </tr>
 * <tr>
 * <td>bottom_bg.gif</td>
 * </tr>
 * <tr>
 * <td>tabtopbg.gif</td>
 * </tr>
 * <tr>
 * <td>emptytab_bg.gif</td>
 * </tr>
 * <tr>
 * <td>corner_bottomleft.gif</td>
 * </tr>
 * <tr>
 * <td>corner_bottomright.gif</td>
 * </tr>
 * <tr>
 * <td>corner_topleft.gif</td>
 * </tr>
 * <tr>
 * <td>corner_righttop.gif</td>
 * </tr>
 * <tr>
 * <td>transparent.gif</td>
 * </tr>
 * </table> Created: Thu Oct 11 15:00:43 2001
 * 
 */

public class TabGenerator {
    LabelProvider labels;
    HelpLinkResource helpLinkResource;
    String imageRoot;
    Style style;

    /**
     * Description of the Field
     */
    public static String CARDTABLE_BG_COLOR = "#EEEEEE";
    final static int SPACE_ON_CARDTABLE = 5;

    // Constants for keyword indexes
    private final static int MENU = 0;
    private final static int ITEM = 3;
    private final static int ZONE = 4;
    private final static int TEXT = 5;
    private final static int LINK = 6;
    private final static int CARDTABLE = 7;
    private final static int CARD = 8;
    private final static int TITLE = 10;
    private final static int TAB_LINK = 11;
    private final static int HELP = 12;
    private final static int CTEXT = 13;

    /**
     * Constructor.
     * 
     * @param imageRoot URL to border images. I should write a list of the image-names somewhere,
     *        but currently only default WorkSpace images in sse.html/pics are used.
     * @param labels LabelProvider for localized texts.
     */
    public TabGenerator(String imageRoot, LabelProvider labels) {
        this(imageRoot, labels, null);
    }

    /**
     * Constructor.
     * 
     * @param imageRoot URL to border images. I should write a list of the image-names somewhere,
     *        but currently only default WorkSpace images in sse.html/pics are used.
     * @param labels LabelProvider for localized texts.
     * @param helpLinkResource HelpLinkResource for getting helpLinks
     */
    public TabGenerator(String imageRoot, LabelProvider labels, HelpLinkResource helpLinkResource) {
        this.imageRoot = imageRoot;
        this.labels = labels;
        this.helpLinkResource = helpLinkResource;
        style = new Style(Style.CSS)
            .addElement("#header {\n" + "   float:left;\n" + "   font-size:93%;\n"
                    + "   line-height:normal;\n" + "}\n" +

                    "#header ul {\n" + "	margin:0;\n" + "	padding:0px 0px 0;\n"
                    + "	list-style:none;\n" + "	background-color: #EEEEEE;\n" + "}\n" +

                    "#header li {\n" + "      float:left;\n" + "      background:url(\""
                    + imageRoot + "left_both.gif\") no-repeat left top;\n" + "      margin:0;\n"
                    + "      padding:0 0 0 5px;\n" + "      border-bottom:1px solid #765;\n"
                    + "}\n" +

                    "#header a {\n" + "      float:left;\n" + "      display:block;\n"
                    + "      background:url(\"" + imageRoot
                    + "right_both.gif\") no-repeat right top;\n"
                    + "      padding:6px 6px 4px 0px;\n" + "      text-decoration:none;\n"
                    + "      color:#000000;\n" + "}\n" +
                    
					"#header #dummy a {\n" + "      float:left;\n" + "      display:block;\n"
                    + "      background:url(\"" + imageRoot
                    + "right_both.gif\") no-repeat right top;\n"
                    + "      padding:6px 6px 4px 0px;\n" + "      text-decoration:none;\n"
                    + "      color:#FFFFFF;\n" + "}\n" +
					
                    "#header > ul a {width:auto;}\n"
                    + "    /* Commented Backslash Hack hides rule from IE5-Mac \\*/\n"
                    + "    #header a {float:none;}\n" + "    /* End IE5-Mac hack */\n" +

                    "#header a:hover {\n" + "	color:#666666;\n" + "}\n" +

                    "#header #current {\n" + "      background-position:0 -150px;\n"
                    + "      border-width:0;\n" + "}\n" +

                    "#header #current a {\n" + "      background-position:100% -150px;\n"
                    + "      padding-bottom:5px;\n" + "      color:#333;\n" + "}\n" +

                    "#header li:hover, #header li:hover a {\n"
                    + "      background-position:0% -147px;\n" + "      color:#333;\n" + "}\n" +

                    "#header li:hover a {\n" + "      background-position:100% -147px;\n" + "}\n" +

                    ".tabtopbg {\n" + "    background-image: url(\"" + imageRoot
                    + "tabtopbg.gif\");\n" + "    background-repeat: repeat-x;\n"
                    + "    background-position: bottom;\n" + "}");
    }


    public Style getStyle() {
        return style;
    }

    /**
     * Wraps a content element inside a tab(set).
     * 
     * @param tabSetId ID of the tabset (tabset-field in the menu-file)
     * @param up The card that's up (the card that 'contains' the content. tab-field in the
     *        menu-file)
     * @param content Tab's contents in an ECS element
     * @param menuFile Menufile-name. Relative to the classpath root. For example
     *        'smilehouse/sse/admin.menu'
     * @param rights User's access rights. If a zone is defined for a tab, it must be included in
     *        the rights for the tab to be included in the tabset. If null is given, no checking is
     *        performed.
     * @param lang Language code for localized texts
     * @return The tabset
     * @exception ParseException Description of the Exception
     * @exception IOException Description of the Exception
     */
    public ElementContainer getTabSet(int tabSetId,
                                      int up,
                                      Element content,
                                      String menuFile,
                                      HashSet rights,
                                      String lang) throws ParseException, IOException {
        return getTabSet(tabSetId, up, content, menuFile, rights, null, null, lang);
    }


    /**
     * Wraps a content element inside a tab(set).
     * 
     * @param tabSetId ID of the tabset (tabset-field in the menu-file)
     * @param up The card that's up (the card that 'contains' the content. tab-field in the
     *        menu-file)
     * @param content Tab's contents in an ECS element
     * @param menuFile Menufile-name. Relative to the classpath root. For example
     *        'smilehouse/sse/admin.menu'
     * @param rights User's access rights. If a zone is defined for a tab, it must be included in
     *        the rights for the tab to be included in the tabset. If null is given, no checking is
     *        performed.
     * @param parameters Additional parameters to be included in the links of the tabs
     * @param lang Language code for localized texts
     * @return The tabset
     * @exception ParseException Description of the Exception
     * @exception IOException Description of the Exception
     */
    public ElementContainer getTabSet(int tabSetId,
                                      int up,
                                      Element content,
                                      String menuFile,
                                      HashSet rights,
                                      String parameters,
                                      String width,
                                      String lang) throws ParseException, IOException {

        // ----------------------------------------
        // Get the menu corresponding to the tabset
        // ----------------------------------------
        // Get the path to the desired menu
        Stack upItems = new Stack();
        Stack menus = getMenuPath(tabSetId, upItems, menuFile);
        if(menus == null && menus.size() == 0) {
            return new ElementContainer().addElement(content);
        }

        // -------------------------
        // Make the menu path string
        // -------------------------
        SimpleParser.Record menu = (SimpleParser.Record) menus.peek();
        String pathString = "";
        String helpId = null;

        // The title of the main menu
        String label = ((SimpleParser.Record) menus.get(0)).getString(TITLE, false);
        pathString = (label == null ? "" : labels.getLabel(label, lang));
        // The titles of the middle menus
        for(int i = 0; i < upItems.size(); i++) {
            SimpleParser.Record item = (SimpleParser.Record) upItems.get(i);
            String text = null;
            label = item.getString(TEXT, false);
            if(label == null) {
                text = item.getString(CTEXT, false);
            } else {
                text = labels.getLabel(label, lang);
            }
            if(text != null) {
                pathString += " - " + text;
            }
        }

        // ------------------------------------------------------------------------------------
        // The last title is not in the stack, we'll have to seek it out amongst the menu items
        // ------------------------------------------------------------------------------------
        // Find the tab that's up
        SimpleParser.Record[] items = menu.getRecords(ITEM);
        SimpleParser.Record upTab = null;
        for(int i = 0; i < items.length; i++) {
            SimpleParser.Entity e = items[i].getEntityByKeyword(CARD, false);
            if(e != null && e.getInt() == up) {
                upTab = items[i];
                break;
            }
        }
        // Get the title and help id of the selected card
        if(upTab != null) {
            String text = null;
            label = upTab.getString(TEXT, false);
            if(label == null) {
                text = upTab.getString(CTEXT, false);
            } else {
                text = labels.getLabel(label, lang);
            }

            if(text != null) {
                pathString += " - " + text + " ";
            }
            helpId = upTab.getString(HELP, false);
        }

        // ----------------------
        // Construct the table(s)
        // ----------------------
        ElementContainer tabSet = null;
        if(menus.size() == 1) {
            // Just one menu. Contains the content and the path & help
            tabSet = getTabSet(
                (SimpleParser.Record) menus.pop(),
                up,
                content,
                rights,
                parameters,
                pathString,
                helpId,
                true,
                width != null ? width : "90%",
                lang);
            //	.setWidth("80%");
        } else {
            // More than one menu, first one (innermost) contains the content
            tabSet = getTabSet(
                (SimpleParser.Record) menus.pop(),
                up,
                content,
                rights,
                parameters,
                null,
                null,
                false,
                "98%",
                lang);
            //	.setWidth("98%");
            while(menus.size() > 1) {
                tabSet = getTabSet(
                    (SimpleParser.Record) menus.pop(),
                    ((SimpleParser.Record) upItems.pop()).getInt(CARD),
                    new BR().addElement(tabSet),
                    rights,
                    parameters,
                    null,
                    null,
                    false,
                    "98%",
                    lang);
                //    .setWidth("98%");
            }
            // The last one (outermost) contains the path & help
            tabSet = getTabSet(
                (SimpleParser.Record) menus.pop(),
                ((SimpleParser.Record) upItems.pop()).getInt(CARD),
                new BR().addElement(tabSet),
                rights,
                parameters,
                pathString,
                helpId,
                true,
                "90%",
                lang);
            //.setWidth("80%");
        }

        return tabSet;
    }



    /**
     * Private method for internal use. It is handed a Record containing the menu-definition and it
     * reads the data from it to arrays. These are handed to still another version of getTabSet that
     * does the actual construction of the table.
     * 
     * @param menu The Record-object read from the menu-file
     * @param up The card that's up
     * @param content Tab's contents
     * @param parameters Additional tablink parameters
     * @param path Path-string to be included on the top of the tab. May be null.
     * @param helpId Help ID (usually servlet's name)
     * @param outer Is this the outer tabset or is this inside some other tabset
     * @param lang Language code
     * @param rights Description of the Parameter
     * @return The tabset
     * @exception ParseException Description of the Exception
     */
    private ElementContainer getTabSet(SimpleParser.Record menu,
                                       int up,
                                       Element content,
                                       HashSet rights,
                                       String parameters,
                                       String path,
                                       String helpId,
                                       boolean outer,
                                       String width,
                                       String lang) throws ParseException {
        // ----------------------------------------------------------------
        // Get texts & links of the cards that the user has right to access
        // ----------------------------------------------------------------
        int acceptedCards = 0;
        int upIndex = -1;

        Vector texts = new Vector();
        Vector links = new Vector();

        SimpleParser.Record[] items = menu.getRecords(ITEM);
        for(int i = 0; i < items.length; i++) {
            // Check the access right
            String zone = items[i].getString(ZONE, false);
            if(zone != null && rights != null && !rights.contains(zone)) {
                continue;
            }
            // Is it the card that's up
            SimpleParser.Entity e = items[i].getEntityByKeyword(CARD, false);
            if(e != null && e.getInt() == up) {
                upIndex = acceptedCards;
            }

            String text = items[i].getString(TEXT, false);
            if(text == null) {
                text = items[i].getString(CTEXT, false);
            } else {
                text = labels.getLabel(text, lang);
            }
            texts.add(text);
            // Try to get tab link first
            String link = items[i].getString(TAB_LINK, false);
            if(link == null) {
                link = items[i].getString(LINK, false);
            }
            links.add(link);
            acceptedCards++;
        }

        return getTabSet(
            (String[]) texts.toArray(new String[texts.size()]),
            (String[]) links.toArray(new String[links.size()]),
            upIndex,
            parameters,
            content,
            path,
            helpId,
            outer,
            width,
            lang);
    }



    /**
     * The version of the tabset that actually constructs the table. Doesn't handle menu-files or
     * SimpleParser-records, instead all the data is given as arrays etc.
     * 
     * @param texts A String-array containing the tab texts
     * @param links A String-array containing the tab links
     * @param up The index of the card that's up
     * @param parameters Additional parameters to be added to the links
     * @param path Path-string to be included on the top of the tab. May be null.
     * @param helpId Help ID (usually servlet's name)
     * @param outer Is this the outer tabset or is this inside some other tabset
     * @param lang Language code
     * @param content Description of the Parameter
     * @return The tabset
     */
    public ElementContainer getTabSet(String[] texts,
                                      String[] links,
                                      int up,
                                      String parameters,
                                      Element content,
                                      String path,
                                      String helpId,
                                      boolean outer,
                                      String width,
                                      String lang) {

        if(width == null)
            width = "90%";

        // -----------
        // Title table
        // -----------
        Div titleDiv = new Div();
        titleDiv.addAttribute("align", AlignType.RIGHT);

        // Location path
        if(path != null)
            titleDiv.addElement(path);
        // Help link
        if(helpId != null && helpLinkResource != null) {
            try {
                A helpLink = helpLinkResource.getHelpLink(helpId, lang);
                if(helpLink != null)
                    titleDiv.addElement(helpLink);
            }
            //catch(java.sql.SQLException sqle) {
            //sqle.printStackTrace();
            //}
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        Table titleTable = new Table()
            .setWidth(width)
            .setBorder(0)
            .setAlign(AlignType.CENTER)
            .setCellPadding(0)
            .setCellSpacing(0)
            .addElement(new TR().addElement(new TD().addElement(titleDiv)));

        // ------------
        // Header table
        // ------------

        UL headerList = new UL();
        for(int i = 0; i < texts.length; i++) {
            LI listItem = new LI();
            listItem.setNeedClosingTag(true);
            if(i == up)
                listItem.setID("current");

            Span headerSpan = new Span();

            String link = links != null && links[i] != null ? links[i] : null;
            String text = texts[i] != null ? texts[i] : "";
            text = text.trim();

            if(link == null){
                link = "#";                
            } else if(link.length() == 0){
            	link = "#";
            	listItem.setID("dummy");
            }
            
            if(parameters != null) {
                link += parameters;
            }
            A anchor = new A(link.trim(), text);
            anchor.setOnMouseOver("window.status='" + text + "'; return true");
            anchor.setOnMouseOut("window.status=''; return true");
            headerSpan.addElement(anchor);

            headerList.addElement(listItem.addElement(headerSpan));
        }

        Table headerTable = new Table()
            .setWidth(width)
            .setBorder(0)
            .setAlign(AlignType.CENTER)
            .setCellSpacing(0)
            .setCellPadding(0)
            .addElement(
                new TR().addElement(
                    new TD().setColSpan(2).setVAlign(AlignType.BOTTOM).addElement(
                        new Table().setWidth("100%").setBorder(0).setCellSpacing(0).setCellPadding(
                            0).addElement(
                            new TR().addElement(new TD().addElement(new Div()
                                .addElement(headerList)
                                .setID("header")))))

                    .setClass("tabtopbg")).addElement(
                    new TD().setWidth(2).setVAlign(AlignType.BOTTOM).addElement(
                        new IMG(imageRoot
                                + (outer ? "corner_righttop_1_outer.gif"
                                        : "corner_righttop_1_inner.gif"))
                            .setWidth(5)
                            .setHeight(2)
                            .setBorder(0))));


        // -------------
        // Content table
        // -------------
        Table contentTable = new Table().setWidth(width).setAlign(AlignType.CENTER).setCellSpacing(
            0).setCellPadding(0).setBorder(0).addElement(
            new TR().addElement(
                new TD().setBackground(imageRoot + "left_bg.gif")
                //"b-l.gif")
                    .setWidth(5).setBgColor("#BFBFBF").addElement(
                        new IMG()
                            .setSrc(imageRoot + "transparent.gif")
                            .setHeight(5)
                            .setWidth(5)
                            .setBorder(0))).addElement(new TD().setWidth("100%")
            //.setBackground(imageRoot + "piste_BFBFBF.gif")
                .setBgColor(CARDTABLE_BG_COLOR).setAlign("left")
                //.setColSpan(3)
                .addElement("\n\n\n")
                //Added newlines to separate tab content from other content in pages source
                .addElement(content).addElement("\n\n\n")
                //.addElement(new BR())
                .setClass("text")).addElement(
                new TD()
                    .setBackground(imageRoot + "right_bg.gif")
                    .setVAlign(AlignType.TOP)
                    .setWidth(2)
                    //"b-r.gif")
                    .addElement(
                        new IMG()
                            .setSrc(
                                imageRoot
                                        + (outer ? "corner_righttop_2_outer.gif"
                                                : "corner_righttop_2_inner.gif"))
                            .setHeight(2)
                            .setWidth(5)
                            .setBorder(0)))).addElement(
            new TR().addElement(new TD()
            //.setWidth(5)
                .addElement(new IMG().setSrc(imageRoot + "corner_bottomleft.gif")
                //"b-bl.gif")
                    //.setHeight(5)
                    .setWidth(5).setBorder(0))).addElement(
                new TD().setBackground(imageRoot + "bottom_bg.gif")
                //"b-b.gif")
                    //.setColSpan(3)
                    .addElement(
                        new IMG()
                            .setSrc(imageRoot + "transparent.gif")
                            .setHeight(1)
                            .setWidth(1)
                            .setBorder(0))).addElement(new TD()
            //.setWidth(5)
                .addElement(new IMG().setSrc(imageRoot + "corner_bottomright.gif")
                //"b-br.gif")
                    //.setHeight(5)
                    .setWidth(5).setBorder(0))));

        ElementContainer tabSet = new ElementContainer().addElement(titleTable).addElement(
            headerTable).addElement(contentTable);

        return tabSet;
    }



    /**
     * Finds the menu with the given tabSetId and it's supermenus. Also collects menuitems in the
     * path to a stack. These can be used to track the active menus to construct the menu path shown
     * on the top of the tab.
     * 
     * @param tabSetId Tab set ID given in the menu definition file as 'cardtable'-parameter
     * @param upItems a stack where the active menuitems are collected
     * @param menuFile Menufile-name. Relative to the classpath root. For example
     *        'smilehouse/sse/admin.menu'
     * @return Active menus in a stack
     * @exception ParseException Description of the Exception
     * @exception IOException Description of the Exception
     */
    Stack getMenuPath(int tabSetId, Stack upItems, String menuFile) throws ParseException,
            IOException {
        String[] words = {
                "menu",
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
                "cardtable|tabset",
                "card|tab",
                "notdropdown",
                "title",
                "tablink",
                "help",
                "ctext",
                "labelresource"};
        InputStream in = null;
        // Stack where the menus are collected
        Stack menus = new Stack();
        try {
            // -------------------------------------------------------
            // Get the menu definition file and create a parser for it
            // -------------------------------------------------------
            in = getClass().getResourceAsStream(menuFile);
            SimpleParser parser = new SimpleParser(words, in);

            // ----------------------------------------------------------
            // Go recursively through the menus searching the desired one
            // ----------------------------------------------------------
            SimpleParser.Entity entity;
            while((entity = parser.readEntity()) != null) {
                if(entity.getKeyword() == MENU) {
                    // Get the next main menu and push it to the stack
                    SimpleParser.Record mainMenu = entity.getRecord();
                    menus.push(mainMenu);
                    if(searchMenuPath(menus, upItems, tabSetId)) {
                        return menus;
                    }
                    // FOUND !!
                    // Nothing was found...
                    menus.pop();
                }
            }
        } finally {
            in.close();
        }

        return null;
        // Not found... weird
    }


    /**
     * A helper method for getMenu. Searches menu with the given tabSetId amongst the childmenus.
     * 
     * @param menus Description of the Parameter
     * @param upItems Description of the Parameter
     * @param tabSetId Description of the Parameter
     * @return Description of the Return Value
     * @exception ParseException Description of the Exception
     * @exception IOException Description of the Exception
     */
    boolean searchMenuPath(Stack menus, Stack upItems, int tabSetId) throws ParseException,
            IOException {
        // --------------------------------------
        // Check the menu on the top of the stack
        // --------------------------------------
        SimpleParser.Record menu = (SimpleParser.Record) menus.peek();
        int cardTable = 0;
        boolean cardTableDefined = false;
        try {
            cardTable = menu.getInt(CARDTABLE);
            cardTableDefined = true;
        } catch(ParseException ignored) {}
        if(cardTableDefined && cardTable == tabSetId) {
            return true;
        }
        // FOUND! No need to go further

        // -------------------------------------
        // Get the menuitems and go through them
        // -------------------------------------
        SimpleParser.Record[] items = menu.getRecords(ITEM);
        for(int i = 0; i < items.length; i++) {
            SimpleParser.Record childMenu = items[i].getRecord(MENU, false);
            if(childMenu != null) {
                // Put the menu and the title to the stacks and search the menu
                menus.push(childMenu);
                upItems.push(items[i]);
                if(searchMenuPath(menus, upItems, tabSetId)) {
                    return true;
                }
                // Nothing was found --> discard the title
                menus.pop();
                upItems.pop();
            }
        }
        return false;
    }



    /**
     * Wraps given content inside a box.
     * 
     * @param element The content as an ECS-element
     * @param header Possible title to be placed on top of the box. May be null.
     * @return A stylish box
     */
    public Table getBox(Element element, String header) {
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
                new TR().addElement(
                    new TD().addElement(new IMG()
                        .setSrc(imageRoot + "corner_topleft.gif")
                        .setWidth(5)
                        .setBorder(0))).addElement(
                    new TD()
                        .setBackground(imageRoot + "emptytab_bg.gif")
                        .setAlign("center")
                        .addElement(new B().addElement(header))
                        //.addElement(header)
                        .setClass("tab")).addElement(
                    new TD().setWidth(2).addElement(
                        new IMG()
                            .setSrc(imageRoot + "corner_righttop.gif")
                            .setWidth(5)
                            .setBorder(0))))
            .addElement(
                new TR().addElement(
                    new TD().setBackground(imageRoot + "left_bg.gif").setWidth(5).addElement(
                        new IMG()
                            .setSrc(imageRoot + "transparent.gif")
                            .setHeight(5)
                            .setWidth(5)
                            .setBorder(0))).addElement(
                    new TD().setBgColor(bgColor).setAlign("left").addElement(element).setClass(
                        "text")).addElement(
                    new TD().setBackground(imageRoot + "right_bg.gif").addElement(
                        new IMG()
                            .setSrc(imageRoot + "transparent.gif")
                            .setHeight(5)
                            .setWidth(5)
                            .setBorder(0))))
            .addElement(
                new TR().addElement(
                    new TD().addElement(new IMG()
                        .setSrc(imageRoot + "corner_bottomleft.gif")
                        .setWidth(5)
                        .setBorder(0))).addElement(
                    new TD().setBackground(imageRoot + "bottom_bg.gif").addElement(
                        new IMG()
                            .setSrc(imageRoot + "transparent.gif")
                            .setHeight(1)
                            .setWidth(1)
                            .setBorder(0))).addElement(
                    new TD().addElement(new IMG()
                        .setSrc(imageRoot + "corner_bottomright.gif")
                        .setWidth(5)
                        .setBorder(0))));
        return table;
    }
} // TabGenerator
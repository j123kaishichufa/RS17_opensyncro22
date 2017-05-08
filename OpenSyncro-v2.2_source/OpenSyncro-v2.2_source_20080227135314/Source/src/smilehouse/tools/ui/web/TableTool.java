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

import org.apache.ecs.html.B;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

/**
 * A little class for making stylish tables with ECS
 */
public class TableTool {
    /**
     * Description of the Field
     */
    public final static String DEFAULT_HEADER_COLOR1 = "#AFC7DF";
    /**
     * Description of the Field
     */
    public final static String DEFAULT_HEADER_COLOR2 = "#DAE5F3";
    /**
     * Description of the Field
     */
    public final static String[] DEFAULT_ROW_COLORS = {"#FFFFFF", "#EEEEEE"};
    // Old green: "#CDECCD
    int innerBorder = 0;
    int outerBorder = 1;
    String borderColor = "#AAAAAA";
    String headerColor1 = DEFAULT_HEADER_COLOR1;
    String headerColor2 = DEFAULT_HEADER_COLOR2;
    String[] rowColors = DEFAULT_ROW_COLORS;
    int rowColorIndex = 0;
    boolean zebra = false;


    /**
     * Creates TableTool with default settings.
     */
    public TableTool() {}


    /**
     * Creates TableTool with given outerBorder and innerBorder settings.
     * 
     * @param outerBorder Thickness of the outer border. Note that the actual thickness is
     *        outerBorder + innerBorder!
     * @param innerBorder Thickness of the inner border
     */
    public TableTool(int outerBorder, int innerBorder) {
        this.outerBorder = outerBorder;
        this.innerBorder = innerBorder;
    }


    /**
     * Set the zebra striped rows on or off.
     * 
     * @param zebra true - stripes on <br>
     *        false - Stripes off
     */
    public void setZebra(boolean zebra) {
        this.zebra = zebra;
    }


    /**
     * Creates TableTool with given outerBorder, innerBorder and color settings.
     * 
     * @return The innerTable value
     */

    /**
     * Returns inner table. Add the rows to this one and then give it as parameter to getOuterTable
     * to get the actual table.
     * 
     * @return The innerTable value
     */
    public Table getInnerTable() {
        Table table = new Table()
            .setBgColor("#FFFFFF")
            .setBorder(0)
            .setCellSpacing(innerBorder)
            .setCellPadding(2);

        return table;
    }


    /**
     * Returns the outer table containing the innertable given as parameter.
     * 
     * @param innerTable Table to be contained inside the outer table.
     * @return The outerTable value
     */
    public Table getOuterTable(Table innerTable) {
        return new Table().setBorder(0).setBgColor(borderColor).setCellSpacing(0).setCellPadding(
            outerBorder).addElement(new TR().addElement(new TD().addElement(innerTable)));
    }


    /**
     * Returns a header row.
     * 
     * @return The headerRow value
     */
    public TR getHeaderRow() {
        return new TR().setBgColor(headerColor1);
    }


    /**
     * Returns a header row with given title.
     * 
     * @param title Table's title
     * @param colSpan Column span for the title cell
     * @return The headerRow value
     */
    public TR getHeaderRow(String title, int colSpan) {
        return new TR().setBgColor(headerColor1).addElement(
            new TD().setColSpan(colSpan).addElement(new B(title)));
    }


    /**
     * Returns a header row with given titles.
     * 
     * @param headers Description of the Parameter
     * @return The headerRow value
     */
    public TR getHeaderRow(String[] headers) {
        TR row = new TR().setBgColor(headerColor1);
        addHeaders(row, headers);
        return row;
    }


    /**
     * Returns a second title row (slightly different color).
     * 
     * @return The secondHeaderRow value
     */
    public TR getSecondHeaderRow() {
        return new TR().setBgColor(headerColor2);
    }


    /**
     * Returns a header row with given titles.
     * 
     * @param headers Description of the Parameter
     * @return The secondHeaderRow value
     */
    public TR getSecondHeaderRow(String[] headers) {
        TR row = new TR().setBgColor(headerColor2);
        addHeaders(row, headers);
        return row;
    }


    /**
     * Adds a feature to the Headers attribute of the TableTool object
     * 
     * @param row The feature to be added to the Headers attribute
     * @param headers The feature to be added to the Headers attribute
     */
    void addHeaders(TR row, String[] headers) {
        for(int i = 0; i < headers.length; i++) {
            row.addElement(new TD().addElement(new B().addElement(headers[i])));
        }
    }


    /**
     * Returns a table row. If zebra is set on, bg color changes.
     * 
     * @return The row value
     */
    public TR getRow() {
        String color = rowColors[rowColorIndex];
        if(zebra) {
            rowColorIndex = 1 - rowColorIndex;
        }
        return new TR().setBgColor(color);
    }


    /**
     * Description of the Method
     */
    public void reset() {
        rowColorIndex = 0;
    }
} // TableTool

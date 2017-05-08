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

/*
 * Created on 2.1.2005.
 */
package smilehouse.util.csv;

/**
 * Marks a format error in the read CSV.
 */
public class CSVFormatException extends RuntimeException {

    private int fieldStartLine;
    private int currentLine;
    private int record;
    private int field;
    
    
    /**
     * @param message
     * @param fieldStartLine
     * @param currentLine
     * @param record
     * @param field
     */
    CSVFormatException(String message, int fieldStartLine, int currentLine, int record, int field) {
        super(message);
        this.fieldStartLine = fieldStartLine;
        this.currentLine = currentLine;
        this.record = record;
        this.field = field;
    }
    
    
    /**
     * @return Returns the line where the Parser discovered the format error.
     */
    public int getCurrentLine() {
        return currentLine;
    }
    /**
     * @return Returns the line where the badly formatted field started.
     */
    public int getFieldStartLine() {
        return fieldStartLine;
    }
    /**
     * @return Returns the record number where the badly formatted field is.
     */
    public int getRecord() {
        return record;
    }
    /**
     * @return Returns the field number in the record which was badly formatted.
     */
    public int getField() {
        return field;
    }
}
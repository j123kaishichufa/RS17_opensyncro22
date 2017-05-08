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

package smilehouse.xml.chopper;

import java.io.IOException;
import java.io.Reader;

/**
 * CharInput implementation for the XMLChopper.
 * It saves the characters read in a buffer and keeps track
 * of row and column also.
 */
class ChopperCharInput implements CharInput {

    private Reader reader;
    private StringBuffer read;
    // How many chars has been pushed back.
    private int pushedBack;
    private int row;
    private int column;
    private int position;
    /** Number of EOSs (-1) read. We cannot append them to the StringBuffer, so
     *  we keep track of them using this counter. */
    private int eos;
    
    public ChopperCharInput(Reader reader) {
        this.reader = reader;
        this.read = new StringBuffer();
        this.pushedBack = 0;
        this.row = 1;
        this.column = 1;
        this.position = 0;
        this.eos = 0;
    }
    
    /**
     * @see smilehouse.xml.chopper.CharInput#nextChar()
     */
    public int nextChar() {                
        int ch;
        if(pushedBack == 0) {
            try {
                ch = reader.read();
            }
            catch(IOException ioe) {
                throw new CharInputException(ioe);
            }
            if(ch != -1) {
                read.append( (char)ch );                    
            }
            else {
                eos++;
            }
        }
        else {
            if(eos >= pushedBack) {
                // Pushed back EOS
                ch = -1;
            }
            else {
                ch = read.charAt(read.length() - pushedBack + eos);
            }
            pushedBack--;
        }
        if(ch == '\n') {
            row++;
            column = 1;
        }
        else {
            if(column != -1)
                column++;
        }
        position++;
        return ch;
    }

    /**
     * @see smilehouse.xml.chopper.CharInput#pushBack()
     */
    public void pushBack() {
        pushedBack++;
        position--;
        if(pushedBack > read.length()) {
            throw new CharInputException("Cannot push back more than has been read!!!");
        }
        if(column == 0) {
            row--;
            column = -1; // Gets too complicated, I give up...
        }
        else {
            column--;
        }
    }
    
    /**
     * Returns the characters read since the last call to clearRead. Doesn't return
     * characters that have been pushed back.
     * 
     * @return Characters read.
     */
    public String getRead() {
        return read.substring(0, read.length()-pushedBack);
    }
    
    /**
     * Clears the characters that are in the read buffer.
     * Doesn't clear the characters that have been pushed back.
     *
     */
    public void clearRead() {
        read.delete(0, read.length()-pushedBack);
    }

    /**
     * Returns the row number.
     * 
     * @return Row number starting at 1.
     */
    public int getRow() {
        return row;
    }
    
    
    /**
     * Returns the column number. Pushing back charactrers can make it hard to keep track of it,
     * so in some situations -1 may be returned meaning that we don't have a slightest idea
     * what column we are on.
     * 
     * @return Column number starting at 1, or -1 if unknown.
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return
     */
    public int getPosition() {
        return position;
    }
    
}

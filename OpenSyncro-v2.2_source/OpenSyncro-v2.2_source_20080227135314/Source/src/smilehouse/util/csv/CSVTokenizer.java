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
 * Created on 2.1.2005
 */
package smilehouse.util.csv;

import java.io.IOException;
import java.io.Reader;

/**
 * Tokenizes csv-content into tokens that are significant to the parsing of the csv. Like
 * java.io.StreamTokenizer but with hand-made special CSV tokens. Also handles cross-platform line
 * endings uniformly.
 */
public class CSVTokenizer {

    /** String token, represents anything else then the other tokens. */
    public static final int STR = 1;
    /** The token used as a quote in csv. */
    public static final int QUOTE = 2;
    /** The token used as a delimiter in csv. */
    public static final int DELIM = 3;
    /** End of line token. StreamTokenizer unifies the platform specific new line characters to this. */
    public static final int EOL = 4;
    /** End of file token. */
    public static final int EOF = -1;


    /** marks that a new character should be read to c-variable */
    private static final int NOT_READ = -2;


    //given settings
    private Reader reader;
    private char delim;
    private char quote;

    //internal status data
    private int c; //contains last read character
    private int lineNumber;
    private boolean pushedBack;
    private StringBuffer contentBuf; //content buffer for STR token content
    private StringBuffer tokenBuf; //actual token buffer



    /** Current token characters */
    public String token;
    /**
     * After a call to the nextToken method, this field contains the type of the token just read
     */
    public int ttype;

    /**
     * Creates a new CSVTokenizer with the given values
     * 
     * @param reader reader for reading csv-content
     * @param delim delimiter character used in the csv.
     * @param quote quote character used in the csv.
     */
    public CSVTokenizer(Reader reader, char delim, char quote) {
        this.reader = reader;
        this.delim = delim;
        this.quote = quote;

        this.c = NOT_READ;
        this.lineNumber = 1;
        this.pushedBack = false;
        this.contentBuf = new StringBuffer();
        this.tokenBuf = new StringBuffer();
    }

    /**
     * 
     * @return the current line number.
     */
    public int lineNumber() {
        return lineNumber;
    }

    /**
     * Causes the next call to the nextToken method of this tokenizer to return the current value in
     * the ttype field and not to modify the token field of this tokenizer.
     *  
     */
    public void pushBack() {
        this.pushedBack = true;
    }

    /**
     * Parses the next token from the input stream of this tokenizer. The type of the next token is
     * returned in the ttype field. The token's string content is also put to the token field of
     * this tokenizer.
     * 
     * @return the value of the ttype field
     * @throws IOException if an I/O error occurs.
     */
    public int nextToken() throws IOException {
        if(pushedBack) {
            pushedBack = false;
            return this.ttype;
        }
        getNextToken();

        /*
         * //debug print String ttypeStr="unknown"; if(ttype==DELIM) ttypeStr="DELIM";
         * if(ttype==STR) ttypeStr="STR"; if(ttype==QUOTE) ttypeStr="QUOTE"; if(ttype==EOL)
         * ttypeStr="EOL"; if(ttype==EOF) ttypeStr="EOF"; if(ttype==STR) System.out.println("Token:
         * "+ttypeStr+"{"+this.token+"}"); else System.out.println("Token: "+ttypeStr);
         */
        return this.ttype;
    }

    /** Actually returns the token. (public includes pushBack-functionality and possibility for debug. */
    private int getNextToken() throws IOException {
        //if last token was EOL this is a new line
        if(this.ttype == EOL)
            lineNumber++;

        //if token hasn't been read yet.
        if(c == NOT_READ)
            readCharacter();

        //eat chars to STR content buffer until token met.
        while(true) {
            if(c == delim) {
                return finishToken(DELIM);
            } else if(c == quote) {
                return finishToken(QUOTE);
            } else if(c == '\n' || c == '\r') {
                return finishToken(EOL);
            } else if(c == EOF) {
                return finishToken(EOF);
            } else {
                //any other char
                contentBuf.append((char) c);
                readCharacter();
            }
        }
    }

    /** Handles reporting of encountered tokens. */
    private int finishToken(int type) throws IOException {
        //if content buffer not yet given, give it
        if(contentBuf.length() != 0) {
            this.ttype = STR;
            this.token = contentBuf.toString();
            //clear buffer
            contentBuf.delete(0, contentBuf.length());
        } else {
            tokenBuf.append((char) c);
            this.ttype = type;
            this.c = NOT_READ;

            //handle multiple platform EOLs
            if(type == EOL) {
                //peak for another control char
                readCharacter();
                //windows platform uses \r\n
                if(c == '\n' && tokenBuf.charAt(0) == '\r') {
                    tokenBuf.append((char) c);
                    this.c = NOT_READ;
                }
            }

            //EOF token set artificially to -1 to obey API
            if(type == EOF) {
                tokenBuf.delete(0, tokenBuf.length());
                tokenBuf.append("-1");
            }

            this.token = tokenBuf.toString();
            tokenBuf.delete(0, tokenBuf.length());
        }
        return this.ttype;
    }
    
    /** Reads next character from the stream. */
    private void readCharacter() throws IOException {
        if(c == EOF)
            throw new IllegalStateException("Already at the end of stream.");
        c = reader.read();
    }


}
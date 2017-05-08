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
 * Created on 29.12.2004.
 */
package smilehouse.util.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for reading CSV files. It implements the flavor of CSV used by MS Excel for
 * example, and can also be used for parsing tab-delimited and other similar files.
 * 
 * CSV's consist of Records which contain Fields.
 * 
 * This class can handle data encoded in the following manner:
 * <ul>
 * <li>The data consists of one or more Records, each ending with an end-of-line character. The
 * last end-of-line is optional unless the last Record is empty.
 * <li>Each Record consists of one or more Fields separated by delimiter characters.
 * <li>Empty Record consists of one Field containing an empty string.
 * <li>Each Field may be quoted or unquoted.
 * <li>Unquoted Fields are literal strings, and may not contain special characters.
 * <li>Quoted Fields are surrounded by quote characters, and may contain any characters.
 * <li>Any quote characters within quoted fields are doubled.
 * <li>Special characters are quote, end-of-line and delimiter.
 * </ul>
 * 
 * The class also implements line precise error reporting on csv format errors. <br>
 * This parser is lenient by default and the previously described format has been implemented with
 * the exception that :<br>
 * <ul>
 * <li>unquoted Fields can contain quotes but a quote cannot be the first character of the field.
 * </ul>
 */
public class CSVReader {
    private CSVTokenizer tokenizer;

    private boolean hasNextField;
    //true if the next given field (called with nextField-method) will be the record's first field
    private boolean fieldStartsNewRecord;

    private int recordNum;
    private int fieldNum;
    private int fieldStartLineNum;



    /**
     * Creates a new CSVReader with delimiter ',' and quote '"' characters.
     * 
     * @param reader
     */
    public CSVReader(Reader reader) {
        this(reader, ',', '"');
    }

    /**
     * Creates a new CSVReader with the given delimiter and quote characters.
     * 
     * @param reader
     * @param delim
     * @param quote
     */
    public CSVReader(Reader reader, char delim, char quote) {
        if(delim == quote)
            throw new IllegalStateException("delim and quote characters cannot be equal");

        if(reader instanceof BufferedReader)
            this.tokenizer = new CSVTokenizer(reader, delim, quote);
        else
            this.tokenizer = new CSVTokenizer(new BufferedReader(reader), delim, quote);

        hasNextField = true;
        fieldStartsNewRecord = true;
        recordNum = 0;
        fieldNum = 0;
        fieldStartLineNum = 0;
    }

    /**
     * @return current line number in the csv source starting from 1. returns 0 if reading hasn't
     *         been started.
     */
    public int lineNumber() {
        //if next is new record, the tokenizer is already on the new line, but we are still on the
        // previous line.
        if(fieldStartsNewRecord)
            return tokenizer.lineNumber() - 1;
        else
            return tokenizer.lineNumber();
    }

    /**
     * @return Current csv record number (that the last given field belongs to), starting from 1.
     *         Returns 0 if any field hasn't been read yet. Differs from line number if the field
     *         values have multiple lines embedded.
     */
    public int recordNumber() {
        return recordNum;
    }

    /**
     * @return Current field number in the csv record (that the last given field belongs to),
     *         starting from 1.
     */
    public int fieldNumber() {
        return fieldNum;
    }

    /**
     * @return true if there is a next field. This can also be used to check if there is a next
     *         Record, if the getRecord-method is being used.
     */
    public boolean hasNext() {
        return hasNextField;
    }

    /**
     * Reads next field from the Reader
     * 
     * @return String content of the next field.
     * @throws IOException If there are problems reading the input with the given Reader.
     * @throws CSVFormatException in case of malformed csv.
     */
    public String nextField() throws IOException, CSVFormatException {
        if(!hasNext())
            throw new IllegalStateException("Doesn't have next!");

        //always start expecting a new field
        fieldStartLineNum = tokenizer.lineNumber();
        StringBuffer field = new StringBuffer();
        start_field.process(this.tokenizer, field);
        return field.toString();
    }

    /**
     * Reads the whole next record from the Reader. Note that this method can be called only from
     * the beginning of a record.
     * 
     * @return Record's fields.
     * @throws IOException If there are problems reading the input with the given Reader.
     * @throws CSVFormatException in case of malformed csv.
     *  
     */
    public String[] nextRecord() throws IOException, CSVFormatException {
        List fields = new ArrayList();
        if(!fieldStartsNewRecord)
            throw new IllegalStateException("In the middle of previous record.");
        if(hasNext())
            fields.add(nextField());
        while(hasNext() && !fieldStartsNewRecord) {
            fields.add(nextField());
        }
        return (String[]) fields.toArray(new String[fields.size()]);
    }


    /**
     * Reads all available records from the Reader. Note that this method can be called only if the
     * Reader hasn't been read already.
     * 
     * @return All read records
     * @throws IOException If there are problems reading the input with the given Reader.
     * @throws CSVFormatException in case of malformed csv..
     */
    public String[][] records() throws IOException, CSVFormatException {
        if(fieldNum != 0)
            throw new IllegalStateException("Already given records, cannot return all.");
        List records = new ArrayList();
        while(hasNext())
            records.add(nextRecord());
        String[][] result = new String[records.size()][];
        for(int i = 0; i < records.size(); i++) {
            result[i] = (String[]) records.get(i);
        }
        return result;
    }



    /** called by the parsing engine to note the end of csv */
    private void end() {
        this.hasNextField = false;
    }

    /** called by the parsing engine to note that new field has been found from the csv. */
    private void addField() {
        if(fieldStartsNewRecord) {
            this.recordNum++;
            fieldStartsNewRecord = false;
            fieldNum = 0;
        }
        fieldNum++;
    }

    /** called by the parsing engine to note that new record been found from the csv. */
    private void addRecord() {
        fieldStartsNewRecord = true;
    }

    /** called by the parsing engine to notify a format error in the read csv. */
    private void csvFormatError(String message) {
        String fullMessage = "Badly formatted CSV: " + message + " Field started on line "
                + fieldStartLineNum + ", problem detected on line " + tokenizer.lineNumber() + "."
                + "In csv record format it's the " + (fieldNum + 1) + ". field on record "
                + recordNum + ".";

        throw new CSVFormatException(
            fullMessage,
            fieldStartLineNum,
            tokenizer.lineNumber(),
            recordNum,
            fieldNum);
    }




    /**
     * Parsing engine is represent as a finite state machine with 6 states. The state machine parses
     * a single field from the csv. This class defines an abstract State which will be implemented
     * by the concrete states in the finite state machine.
     *  
     */
    private abstract class State {
        /**
         * Reads next CSV token from the tokenizer and gives it to processing method.
         * 
         * 
         * @param tokenizer Tokenizer that reads the input
         * @param result Field's final content will be gathered here
         * @throws IOException If there are problems reading the input with the given Reader.
         * @throws CSVFormatException in case of malformed csv.
         */
        public void process(CSVTokenizer tokenizer, StringBuffer result) throws IOException {
            tokenizer.nextToken();
            process(tokenizer, result, tokenizer.token);
        }

        /**
         * processes the given passovertoken according to state implementation. Field's value will
         * be gathered to the result buffer.
         * 
         * @param tokenizer Tokenizer that reads the input
         * @param result Field's final content will be gathered here
         * @param passoverToken The token to process.
         * @throws IOException If there are problems reading the input with the given Reader.
         * @throws CSVFormatException in case of malformed csv.
         */
        public abstract void process(CSVTokenizer tokenizer,
                                     StringBuffer result,
                                     String passoverToken) throws IOException;
    }

    /** Finite machine start state for parsing a single field from csv stream. */
    private final State start_field = new State() {
        public void process(CSVTokenizer tokenizer, StringBuffer result, String passoverToken)
                throws IOException {
            switch(tokenizer.ttype) {
            case (CSVTokenizer.STR):
                unquoted_field.process(tokenizer, result, passoverToken);
                break;
            case (CSVTokenizer.QUOTE):
                quoted_field.process(tokenizer, result);
                break;
            case (CSVTokenizer.DELIM):
                addField(); //was an empty field, stop processing
                break;
            case (CSVTokenizer.EOL):
                regular_end_line.process(tokenizer, result, passoverToken);
                break;
            case (CSVTokenizer.EOF):
                regular_end_file.process(tokenizer, result, passoverToken);
                break;
            default:
                throw new IllegalStateException("Unexpected state.");
            }
        }
    };
    /** state when parsing content of an unquoted field */
    private final State unquoted_field = new State() {
        public void process(CSVTokenizer tokenizer, StringBuffer result, String passoverToken)
                throws IOException {
            switch(tokenizer.ttype) {
            case (CSVTokenizer.STR):
            case (CSVTokenizer.QUOTE): //we could show error on quote too, now quotes are accepted
                // in middle of unquoted columns
                result.append(passoverToken); //add content to field
                this.process(tokenizer, result);
                break;
            case (CSVTokenizer.DELIM):
                addField(); //field processed, stop processing
                break;
            case (CSVTokenizer.EOL):
                regular_end_line.process(tokenizer, result, passoverToken);
                break;
            case (CSVTokenizer.EOF):
                regular_end_file.process(tokenizer, result, passoverToken);
                break;
            default:
                throw new IllegalStateException("Unexpected state.");
            }
        }
    };

    /** State when parsing content of a quoted field. */
    private final State quoted_field = new State() {
        public void process(CSVTokenizer tokenizer, StringBuffer result, String passoverToken)
                throws IOException {
            switch(tokenizer.ttype) {
            case (CSVTokenizer.STR):
            case (CSVTokenizer.EOL):
            case (CSVTokenizer.DELIM):
                result.append(passoverToken); //add content to field
                this.process(tokenizer, result);
                break;
            case (CSVTokenizer.QUOTE):
                possible_end_quote.process(tokenizer, result);
                break;
            case (CSVTokenizer.EOF):
                csvFormatError("Unterminated quoted field.");
            default:
                throw new IllegalStateException("Unexpected state.");
            }
        }
    };

    /** State when quote encountered in quoted field. Another quote continues field. */
    private final State possible_end_quote = new State() {
        public void process(CSVTokenizer tokenizer, StringBuffer result, String passoverToken)
                throws IOException {
            switch(tokenizer.ttype) {
            case (CSVTokenizer.STR):
                csvFormatError("Quoted field continues after end quote.");
            case (CSVTokenizer.QUOTE):
                result.append(passoverToken); //add one quote to the result
                quoted_field.process(tokenizer, result); //continue field parsing
                break;
            case (CSVTokenizer.DELIM):
                addField(); //field processed, stop processing
                break;
            case (CSVTokenizer.EOL):
                regular_end_line.process(tokenizer, result, passoverToken);
                break;
            case (CSVTokenizer.EOF):
                regular_end_file.process(tokenizer, result, passoverToken);
                break;
            default:
                throw new IllegalStateException("Unexpected state.");
            }
        }
    };


    /** corresponds to acceptable end of line states */
    private final State regular_end_line = new State() {
        public void process(CSVTokenizer tokenizer, StringBuffer result, String passoverToken)
                throws IOException {
            switch(tokenizer.ttype) {
            case (CSVTokenizer.EOL):
                addField();
                addRecord();
                //peak next token, in case of EOF
                tokenizer.nextToken();
                if(tokenizer.ttype == CSVTokenizer.EOF)
                    end(); //prevents the EOL state which would add the fields and records again.
                else
                    tokenizer.pushBack();
                break;
            default:
                throw new IllegalStateException("Unexpected state.");
            }
        }
    };
    /** corresponds to acceptable end of file state */
    private final State regular_end_file = new State() {
        public void process(CSVTokenizer tokenizer, StringBuffer result, String passoverToken)
                throws IOException {
            switch(tokenizer.ttype) {
            case (CSVTokenizer.EOF):
                addField();
                addRecord();
                end();
                break;
            default:
                throw new IllegalStateException("Unexpected state.");
            }
        }
    };
   
}
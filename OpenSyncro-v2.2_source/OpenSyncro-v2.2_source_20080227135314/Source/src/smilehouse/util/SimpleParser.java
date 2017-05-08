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

package smilehouse.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * SimpleParser.java A class for parsing files with the following syntax :
 * 
 * <PRE>
 * 
 * KEYWORD "STRING" [more strings...] KEYWORD NUMBER [more numbers...] KEYWORD { KEYWORD... ... ... }
 * KEYWORD... ...
 * 
 * </PRE>
 * 
 * The accepted keywords are given to the parser as an array. C and C++ style comments are
 * supported. Values are read in SimpleParser.Entity and SimpleParser.Record objects.
 * SimpleParser.Entity represents one entry starting with a keyword. Entity can contain a string or
 * a number (or arrays of them) or a SimpleParser.Record. SimpleParser.Record represents a block
 * inside '{' and '}' and contains Entities Created: Mon Aug 21 14:05:35 2000
 * 
 */
public class SimpleParser {
    /**
     * Class Entity represents a single entry of form KEYWORD [VALUE|RECORD]. Value can be a single
     * number or string or an array of numbers or strings provided that the two aren't mixed
     * together. Record is a block starting with '{' and ending with '}' and contains Entities.
     * 
     */
    public static class Entity {
        /**
         * Constant value indicating that the Entity contains number(s)
         */
        public final static int NUMBER = 0;
        /**
         * Constant value indicating that the Entity contains String(s)
         */
        public final static int STRING = 1;
        /**
         * Constant value indicating that the Entity contains a Record
         */
        public final static int RECORD = 2;
        /**
         * Constant value indicating that the entity is a flag
         */
        public final static int FLAG = 3;

        /**
         * Description of the Field
         */
        public int keyword;
        /**
         * Description of the Field
         */
        public int type;
        Object value;
        int lineno;


        /**
         * Creates an Entity that contains a String array.
         * 
         * @param keyword Entity's keyword
         * @param strings Entity's value
         * @param lineno Entity-definitions linenumber in the sourcefile
         */
        Entity(int keyword, String[] strings, int lineno) {
            this.keyword = keyword;
            type = STRING;
            value = strings;
            this.lineno = lineno;
        }


        /**
         * Creates an Entity that contains a String.
         * 
         * @param keyword Entity's keyword
         * @param string Entity's value
         * @param lineno Entity-definitions linenumber in the sourcefile
         */
        Entity(int keyword, String string, int lineno) {
            this.keyword = keyword;
            type = STRING;
            String[] strings = new String[1];
            strings[0] = string;
            value = strings;
            this.lineno = lineno;
        }


        /**
         * Creates an Entity that contains an int array.
         * 
         * @param keyword Entity's keyword
         * @param integers Entity's value
         * @param lineno Entity-definitions linenumber in the sourcefile
         */
        Entity(int keyword, int[] integers, int lineno) {
            this.keyword = keyword;
            type = NUMBER;
            value = integers;
            this.lineno = lineno;
        }


        /**
         * Creates an Entity that contains an int value.
         * 
         * @param keyword Entity's keyword
         * @param integer Entity's value
         * @param lineno Entity-definitions linenumber in the sourcefile
         */
        Entity(int keyword, int integer, int lineno) {
            this.keyword = keyword;
            type = NUMBER;
            int[] integers = new int[1];
            integers[0] = integer;
            value = integers;
            this.lineno = lineno;
        }


        /**
         * Creates an Entity that contains a Record.
         * 
         * @param keyword Entity's keyword
         * @param record Entity's value
         * @param lineno Entity-definitions linenumber in the sourcefile
         */
        Entity(int keyword, Record record, int lineno) {
            this.keyword = keyword;
            type = RECORD;
            value = record;
            this.lineno = lineno;
        }


        /**
         * Creates an Entity that's just a flag.
         * 
         * @param keyword Entity's keyword
         * @param lineno Entity-definitions linenumber in the sourcefile
         */
        Entity(int keyword, int lineno) {
            this.keyword = keyword;
            type = FLAG;
            value = null;
            this.lineno = lineno;
        }


        /**
         * How many values entity contains.
         * 
         * @return Description of the Return Value
         */
        public int numberOfValues() {
            switch(type) {
            case NUMBER:
                return ((int[]) value).length;
            case STRING:
                return ((String[]) value).length;
            default:
                if(value != null) {
                    return 1;
                }
            }
            return 0;
        }


        /**
         * Gets Entity's value as a String-array.
         * 
         * @return The stringArray value
         * @exception ParseException if the Entity's type is incompatible.
         */
        public String[] getStringArray() throws ParseException {
            if(type != STRING) {
                throw new ParseException("Error: String array expected", lineno);
            }
            return (String[]) value;
        }


        /**
         * Gets Entity's value as a String.
         * 
         * @param index Index of the required value.
         * @return The string value
         * @exception ParseException if the Entity's type is incompatible or there are too few
         *            values.
         */
        public String getString(int index) throws ParseException {
            if(type != STRING) {
                throw new ParseException("Error: String expected", lineno);
            }
            if(((String[]) value).length <= index) {
                throw new ParseException("Error: Too few values", lineno);
            }
            return ((String[]) value)[index];
        }


        /**
         * Gets Entity's value as a String.
         * 
         * @return The string value
         * @exception ParseException if the Entity's type is incompatible.
         */
        public String getString() throws ParseException {
            return getString(0);
        }


        /**
         * Gets Entity's value as an int-array.
         * 
         * @return The intArray value
         * @exception ParseException if the Entity's type is incompatible.
         */
        public int[] getIntArray() throws ParseException {
            if(type != NUMBER) {
                throw new ParseException("Error: Integer array expected", lineno);
            }
            return (int[]) value;
        }


        /**
         * Gets Entity's value as an int.
         * 
         * @param index Index of the required value.
         * @return The int value
         * @exception ParseException if the Entity's type is incompatible or there are too few
         *            values.
         */
        public int getInt(int index) throws ParseException {
            if(type != NUMBER) {
                throw new ParseException("Error: Integer expected", lineno);
            }
            return ((int[]) value)[index];
        }


        /**
         * Gets Entity's value as an int.
         * 
         * @return The int value
         * @exception ParseException if the Entity's type is incompatible.
         */
        public int getInt() throws ParseException {
            return getInt(0);
        }


        /**
         * Gets Entity's value as a Record.
         * 
         * @return The record value
         * @exception ParseException if the Entity's type is incompatible.
         */
        public Record getRecord() throws ParseException {
            if(type != RECORD) {
                throw new ParseException("Error: Record expected", lineno);
            }
            return (Record) value;
        }


        /**
         * Returns entity's keyword.
         * 
         * @return The keyword value
         */
        public int getKeyword() {
            return keyword;
        }
    }


    /**
     * Class Record is a collection of Entities separated by '{' and '}'. It can also contain the
     * whole file, if retrieved with SimpleParser.getAll()
     */
    public static class Record {
        int lineno;
        Entity[] entities;
        int nextIndex;


        /**
         * Creates Record with the given entities.
         * 
         * @param entities Array of Entities
         * @param lineno Record's starting linenumber in the source file
         */
        Record(Entity[] entities, int lineno) {
            this.entities = entities;
            this.lineno = lineno;
            nextIndex = 0;
        }


        /**
         * Creates Record with the given entities.
         * 
         * @param v Vector containing Entities
         * @param lineno Record's starting linenumber in the source file
         */
        Record(Vector v, int lineno) {
            entities = new Entity[v.size()];
            for(int i = 0; i < v.size(); i++) {
                entities[i] = (Entity) v.get(i);
            }
            this.lineno = lineno;
            nextIndex = 0;
        }


        /**
         * Returns the first occurence of the Entity of the given type.
         * 
         * @param keyword Keyword that we're interested in
         * @param required If true, a ParseException will be thrown if no Entity is found
         * @return The entityByKeyword value
         * @exception ParseException Description of the Exception
         */
        public Entity getEntityByKeyword(int keyword, boolean required) throws ParseException {
            for(int i = 0; i < entities.length; i++) {
                if(entities[i].keyword == keyword) {
                    return entities[i];
                }
            }
            if(required) {
                throw new ParseException("A required field missing.", lineno);
            }
            return null;
        }


        /**
         * Returns all the occurences of the entities of the given type.
         * 
         * @param keyword Keyword that we're interested in
         * @return The entityArrayByKeyword value
         */
        public Entity[] getEntityArrayByKeyword(int keyword) {
            Vector found = new Vector();
            int i;
            // Find the matching entities
            for(i = 0; i < entities.length; i++) {
                if(entities[i].keyword == keyword) {
                    found.add(entities[i]);
                }
            }
            if(found.size() == 0) {
                return null;
            }
            // Put them in an array and return them.
            Entity[] ret = new Entity[found.size()];
            for(i = 0; i < found.size(); i++) {
                ret[i] = (Entity) found.get(i);
            }
            return ret;
        }


        /**
         * Convenience method for checking flags.
         * 
         * @param keyWord Description of the Parameter
         * @return The set value
         */
        public boolean isSet(int keyWord) {
            Entity e = null;
            try {
                e = getEntityByKeyword(keyWord, false);
            } catch(ParseException neverOccures) {}
            return (e != null);
        }


        // ---------------------------------------------------------------------------------------
        // These are convenient methods for getting values of an entities inside record
        // If there are multiple entities with the given keyword, only the first one is considered
        // ---------------------------------------------------------------------------------------

        /**
         * Get value of a String-entity inside the record.
         * 
         * @param keyword Entity's keyword
         * @param required Should parse-exception be thrown, if no entity is found
         * @return The string value
         * @exception ParseException Description of the Exception
         */
        public String getString(int keyword, boolean required) throws ParseException {
            Entity e = getEntityByKeyword(keyword, required);
            if(e != null) {
                return e.getString();
            }
            return null;
        }


        /**
         * Get value of a String array-entity inside the record.
         * 
         * @param keyword Entity's keyword
         * @param required Should parse-exception be thrown, if no entity is found
         * @return The stringArray value
         * @exception ParseException Description of the Exception
         */
        public String[] getStringArray(int keyword, boolean required) throws ParseException {
            Entity e = getEntityByKeyword(keyword, required);
            if(e != null) {
                return e.getStringArray();
            }
            return null;
        }


        /**
         * Get value of an int-entity inside the record. Note that with this method entity is always
         * required and ParseException will be throws if it is not found.
         * 
         * @param keyword Entity's keyword
         * @return The int value
         * @exception ParseException Description of the Exception
         */
        public int getInt(int keyword) throws ParseException {
            Entity e = getEntityByKeyword(keyword, true);
            return e.getInt();
        }


        /**
         * Get value of an int array-entity inside the record.
         * 
         * @param keyword Entity's keyword
         * @param required Should parse-exception be thrown, if no entity is found
         * @return The intArray value
         * @exception ParseException Description of the Exception
         */
        public int[] getIntArray(int keyword, boolean required) throws ParseException {
            Entity e = getEntityByKeyword(keyword, required);
            if(e != null) {
                return e.getIntArray();
            }
            return null;
        }


        /**
         * Get value of a record-entity inside the record.
         * 
         * @param keyword Entity's keyword
         * @param required Should parse-exception be thrown, if no entity is found
         * @return The record value
         * @exception ParseException Description of the Exception
         */
        public Record getRecord(int keyword, boolean required) throws ParseException {
            Entity e = getEntityByKeyword(keyword, required);
            if(e != null) {
                return e.getRecord();
            }
            return null;
        }


        /**
         * Get all records in entities with the given keyword. Note that this method works
         * differently than the getXXXArray methods, which returned only the contents of the first
         * entity with the given keyword (which can have multiple values in case of string and int
         * entities)
         * 
         * @param keyword Entity's keyword
         * @return The records value
         * @exception ParseException Description of the Exception
         */
        public Record[] getRecords(int keyword) throws ParseException {
            Vector found = new Vector();
            int i;
            // Find the matching entities
            for(i = 0; i < entities.length; i++) {
                if(entities[i].keyword == keyword) {
                    found.add(entities[i]);
                }
            }
            if(found.size() == 0) {
                return new Record[0];
            }
            // Put them in an array and return them.
            Record[] ret = new Record[found.size()];
            for(i = 0; i < found.size(); i++) {
                ret[i] = ((Entity) found.get(i)).getRecord();
            }
            return ret;
        }


        /**
         * Returns the number of entities.
         * 
         * @return Description of the Return Value
         */
        public int numberOfEntities() {
            return entities.length;
        }



        /**
         * Tests if there is more Entities not yet retrieved with nextEntity().
         * 
         * @return Description of the Return Value
         */
        public boolean hasMoreEntities() {
            return nextIndex < entities.length;
        }


        /**
         * Returns the next Entity if there is more, or null if there isn't.
         * 
         * @return Description of the Return Value
         */
        public Entity nextEntity() {
            if(hasMoreEntities()) {
                return entities[nextIndex++];
            }
            return null;
        }
    }



    StreamTokenizer tokenizer;
    //String[] words;
    Hashtable keywords;


    /**
     * Creates a SimpleParser that accepts the given keywords.
     * 
     * @param words Accepted keywords
     */
    public SimpleParser(String[] words) {
        initKeywords(words);
    }


    /**
     * Creates a SimpleParser that accepts the given keywords and gets its input in the given
     * InputStream.
     * 
     * @param words Accepted keywords
     * @param in InputStream where to read from
     */
    public SimpleParser(String[] words, InputStream in) {
        Reader r = new BufferedReader(new InputStreamReader(in));
        tokenizer = new StreamTokenizer(r);
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        initKeywords(words);
    }


    /**
     * Description of the Method
     * 
     * @param words Description of the Parameter
     */
    void initKeywords(String[] words) {
        keywords = new Hashtable();
        for(int i = 0; i < words.length; i++) {
            Integer wordNumber = new Integer(i);
            String[] synonyms = Utils.split(words[i], "|");
            for(int s = 0; s < synonyms.length; s++) {
                keywords.put(synonyms[s], wordNumber);
            }
        }
    }


    /**
     * Tells the SimpleParser to take it's input from the given InputStream.
     * 
     * @param in InputStream where to read from
     */
    public void open(InputStream in) {
        Reader r = new BufferedReader(new InputStreamReader(in));
        tokenizer = new StreamTokenizer(r);
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);

    }


    /**
     * Returns the index of the keyword.
     * 
     * @param w Keyword
     * @return The wordNumber value
     * @exception ParseException Description of the Exception
     */
    int getWordNumber(String w) throws ParseException {
        Integer integer = (Integer) keywords.get(w);
        if(integer != null) {
            return integer.intValue();
        }
        throw new ParseException("Unrecognized keyword: " + w, tokenizer.lineno());
    }


    /**
     * Returns the linenumber where the SimpleParser currently is.
     * 
     * @return Description of the Return Value
     */
    public int lineno() {
        return tokenizer.lineno();
    }


    /**
     * Reads the contents of one entity. The keyword is already read and is given to the method as a
     * parameter.
     * 
     * @param keyword Entity's keyword
     * @return Description of the Return Value
     * @exception IOException Description of the Exception
     * @exception ParseException Description of the Exception
     */
    Entity readEntity(int keyword) throws IOException, ParseException {
        int i;
        int startLineNo;
        // Read the first value
        int ttype = tokenizer.nextToken();
        Vector v;
        switch(ttype) {
        case StreamTokenizer.TT_NUMBER:
            // Number(s)
            startLineNo = tokenizer.lineno();
            v = new Vector();
            while(ttype == StreamTokenizer.TT_NUMBER) {
                v.add(new Integer((int) tokenizer.nval));
                ttype = tokenizer.nextToken();
            }
            tokenizer.pushBack();
            int[] numbers = new int[v.size()];
            for(i = 0; i < v.size(); i++) {
                numbers[i] = ((Integer) v.get(i)).intValue();
            }
            return new Entity(keyword, numbers, startLineNo);
        case '"':
            // String(s)
            startLineNo = tokenizer.lineno();
            v = new Vector();
            while(ttype == '"') {
                v.add(tokenizer.sval);
                ttype = tokenizer.nextToken();
            }
            tokenizer.pushBack();
            String[] strings = new String[v.size()];
            for(i = 0; i < v.size(); i++) {
                strings[i] = (String) v.get(i);
            }
            return new Entity(keyword, strings, startLineNo);
        case '{':
            // Record
            startLineNo = tokenizer.lineno();
            return new Entity(keyword, readRecord('}'), startLineNo);
        case '}':
        // Flag (I guess...)
        case StreamTokenizer.TT_WORD:
            tokenizer.pushBack();
            return new Entity(keyword, tokenizer.lineno());
        default:
            throw new ParseException(
                "Syntax error: number, string or record was expected.",
                tokenizer.lineno());
        }
    }


    /**
     * Reads one entity.
     * 
     * @return Description of the Return Value
     * @exception ParseException
     * @exception IOException Description of the Exception
     */
    public Entity readEntity() throws IOException, ParseException {
        // Read the keyword
        int ttype = tokenizer.nextToken();
        if(ttype == StreamTokenizer.TT_EOF) {
            return null;
        }
        if(ttype != StreamTokenizer.TT_WORD) {
            throw new ParseException("Syntax error: keyword expected.", tokenizer.lineno());
        }
        int keyword = getWordNumber(tokenizer.sval);
        // Read the entity's contents
        return readEntity(keyword);
    }


    /**
     * Reads one Record.
     * 
     * @param endChar The character that ends the Record
     * @return Description of the Return Value
     * @exception ParseException
     * @exception IOException Description of the Exception
     */
    Record readRecord(int endChar) throws IOException, ParseException {
        int ttype;
        int startLineNo = tokenizer.lineno();
        Vector entities = new Vector();
        // Read entities until the end of the record is reached.
        while((ttype = tokenizer.nextToken()) != endChar) {
            if(ttype != StreamTokenizer.TT_WORD) {
                throw new ParseException("Syntax error: keyword expected.", tokenizer.lineno());
            }
            int keyword = getWordNumber(tokenizer.sval);
            entities.add(readEntity(keyword));
        }
        return new Record(entities, startLineNo);
    }


    /**
     * Reads the whole sourcefile into one Record-object. (If some entities are already read with
     * readEntity(), they are not read again)
     * 
     * @return Description of the Return Value
     * @exception ParseException
     * @exception IOException Description of the Exception
     */
    public Record readAll() throws IOException, ParseException {
        return readRecord(StreamTokenizer.TT_EOF);
    }
} // SimpleParser
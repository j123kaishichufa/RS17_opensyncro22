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
 * Created on Nov 9, 2005
 */
package smilehouse.opensyncro.defaultcomponents.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


class StatementReader {
    String sql;
    int index;

    public StatementReader(String sql) {
        this.sql = sql;
        this.index = 0;
    }

    public String nextStatement() {
        if(index >= sql.length())
            return null;
        int endIndex = findNextClosure(sql, index);
        String stmt = sql.substring(index, endIndex); //this is exclusive so the last char ; is removed
        index = endIndex+1;
        
        return stmt.trim();
    }

    private static int findNextClosure(String sql, int index) {
        //TODO Doesn't notice statement end correctly if some string literal end with the escaped \ char.
        //tofix: add inEscapeSequence boolean, or maybe it's time to build a real parser.
        boolean insQuotes = false;
        boolean inQuotes = false;
        char prev = ' ';
        int i = index;
        while(i < sql.length()) {
            char c = sql.charAt(i);
            switch(c) {
            case ('\''):
                if(prev != '\\' && !inQuotes)
                    insQuotes = (!insQuotes);
                break;
            case ('"'):
                if(prev != '\\' && !insQuotes)
                    inQuotes = (!inQuotes);
                break;
            case (';'):
                if(prev != '\\' && !insQuotes && !inQuotes)
                    return i;
                break;
            }
            i++;
            prev = c;
        }
        return i;
    }
    
    public static void main(String[] args) throws Exception{
        String sql = readerToString(new FileReader(new File(args[0])));
       StatementReader sr = new StatementReader(sql);
       String line = sr.nextStatement();
       int i=1;
       while(line!=null) {
           if(line.length()>0) {
               System.out.println(""+i+". "+line+"");
           }
           i++;
           line=sr.nextStatement();
       }
       
    }
    private static String readerToString(Reader reader) throws IOException {
        StringBuffer buf = new StringBuffer();
        BufferedReader buffered = new BufferedReader(reader);
        try {
            String lastLine = buffered.readLine();
            while(lastLine != null) {
                buf.append(lastLine + "\n");
                lastLine = buffered.readLine();
            }
        } finally {
            buffered.close();
            reader.close();
        }
        return buf.toString();
    }
}
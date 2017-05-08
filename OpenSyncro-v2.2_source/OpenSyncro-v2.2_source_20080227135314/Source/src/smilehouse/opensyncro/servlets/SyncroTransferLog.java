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

package smilehouse.opensyncro.servlets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.AlignType;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.A;
import org.apache.ecs.html.B;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.opensyncro.pipes.log.LogEntry;
import smilehouse.opensyncro.pipes.log.LogMessageEntry;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.system.Persister;
import smilehouse.tools.ui.web.TableTool;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;
import smilehouse.util.Utils;

/**
 * SyncroTransferLog.java
 * 
 * Created: Tue Mar 23 14:23:55 2004
 */

public class SyncroTransferLog extends SyncroServlet {

    private static final String[] ERROR_COLORS = {"#FF6666", "#FF4444"};
    private static final String[] ABORT_COLORS = {"#FFFF99", "#FFFF55"};

    private static final int LOG_ENTRIES_PER_PAGE = 100;
    private static final int PAGER_DIRECT_LINKS = 10;

    private static final String PAGE = "page";
    private static final String PIPE_ID = "pipe";

    private static final String SESSION_PREFIX = "syncro.log.";
    
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public void initParameters(ParameterManager pm) {
        pm.addParameter(ACTION, ParameterManager.INT);
        pm.addParameter(PAGE, ParameterManager.INT);
    }

    public int getTabSetId() {
        return 3;
    }

    public int getTabId() {
        return 1;
    }


    public String handleRequest(HttpServletRequest req,
                                HttpSession session,
                                ParameterManager parameters,
                                Persister pers,
                                LabelResource labels,
                                int requestType) {

        updateSession(session, parameters);
        int page = getPage(session);
        Long pipeId = getPipeId(session);

        DateFormat dFormat = new SimpleDateFormat(DATE_FORMAT);

        if(requestType == POST_REQUEST && parameters.getInt(ACTION) == 1) {
            pers.deleteAllLogEntries();
            page = 0;
            setPage(Integer.valueOf(page), session);
            // TODO: Display info text about log being cleared?
        }

        Input clearLogButton = new Input(Input.BUTTON, "clear_transferlog_button", labels
            .getLabel("clear_transferlog"));

        clearLogButton.setOnClick("if(!confirm('"
                + Utils.escapeJavaScriptString(labels.getLabel("confirm_cleartransferlog"))
                + "')) return false; document.forms[0]." + ACTION
                + ".value=1;document.forms[0].submit();");

        Form form = getForm("TransferLog", clearLogButton.toString(), labels, false, false);
        form.addElement(new Input(Input.HIDDEN, ACTION, "0"));

        List entries = pers
        			.getAllLogMessageEntries(page * LOG_ENTRIES_PER_PAGE, LOG_ENTRIES_PER_PAGE);
        
        int numberOfLogEntries = pers.getNumberOfLogMessageEntries(pipeId);
        int numberOfPages = numberOfLogEntries > 0 ? (int) Math.ceil(((double) numberOfLogEntries)
                / ((double) LOG_ENTRIES_PER_PAGE)) : 1;
        String pager = getPager(page, numberOfPages, labels);

        TableTool tt = new TableTool();
        tt.setZebra(true);
        Table logTable = tt.getInnerTable().addElement(
            tt.getHeaderRow(new String[] {
                    labels.getLabel("time"),
                    labels.getLabel("pipe"),
                    labels.getLabel("status"),
                    labels.getLabel("lastuser"),
                    labels.getLabel("messages")}));

        //colorIndex needed for abort and error colors
        int colorIndex = 0;
        LogEntry logEntryOld = null;
    	TD messageCell = new TD();
    	boolean firstEntry = true;
        for(Iterator i = entries.iterator(); i.hasNext();) {

            LogMessageEntry entry = (LogMessageEntry) i.next();
            
        	LogEntry logEntry = entry.getLog();
        	/*	
        	 * Rows are added to table immediately after logEntry has changed.
        	 * To detect the change, logEntryOld is used for comparison.
        	 * When entering the for-loop for the first time logEntryOld is set to logEntry.
        	 */
        	if (firstEntry){
        		logEntryOld = logEntry;
        		firstEntry = false;
        		//check if entry is continued from previous page
        		if(entry.getIndex() != 1){
        			messageCell.addElement("...");
        		    messageCell.addElement(new BR());
        		}
        	}
        	/*
        	 * When logEntry gets new value, the previously collected log message entries are written
        	 * to table and new TD() is created
        	 */
    		if(!logEntry.equals(logEntryOld)){
            	colorIndex = 1 - colorIndex;
    	        writeRow(logEntryOld, dFormat, colorIndex, tt, logTable, messageCell, labels);

    	        messageCell = new TD();
    	        messageCell.setAlign("left"); // IE6 would center the contents by default

    	        logEntryOld = logEntry;
        	}
    		
    		messageCell.addElement(entry.getMessage());
    		messageCell.addElement(new BR());
    		
	        //write last row (complete or incomplete entry)
    		if (!i.hasNext()){
    			//check if entry is continued on next page
    			if (entry.getIndex() < pers.getMaxMessageIndex(logEntry.getId()))
        			messageCell.addElement("...");
    			colorIndex = 1 - colorIndex;
    	        writeRow(logEntryOld, dFormat, colorIndex, tt, logTable, messageCell, labels);
    		}
    			
        }
        
        ElementContainer content = new ElementContainer()
            .addElement(new BR())
            .addElement(pager)
            .addElement(tt.getOuterTable(logTable))
            .addElement(pager)
            .addElement(new HR().addElement(form));

        return content.toString();
    }
    
    private void writeRow(LogEntry logEntry,
    						DateFormat dFormat,
    						int colorIndex,
    						TableTool tt,
    						Table logTable,
    						TD messageCell,
    						LabelResource labels ){

        TR row = tt.getRow();
    	
        //setting abort or error color to row
        if(logEntry.getStatusCode() == LogEntry.STATUS_ABORTED)
        	row.setBgColor(ABORT_COLORS[colorIndex]);
        else if(logEntry.getStatusCode() != LogEntry.STATUS_OK)
        	row.setBgColor(ERROR_COLORS[colorIndex]);
        	
        Date rowTime = logEntry.getTime();
        String formattedTime;
        if( rowTime != null ) {
            formattedTime = dFormat.format(rowTime);
        } else {
            formattedTime = "";
        }
        
        logTable.addElement(row
                .addElement(new TD().addElement(formattedTime))
                .addElement(
                	new TD().addElement(new A(
                		"EditPipe?pipeid=" + logEntry.getPipe().getId(),
                		logEntry.getPipe().getName())))
                .addElement(new TD().addElement(labels.getLabel("status" + logEntry.getStatusCode())))
                .addElement(new TD().addElement(logEntry.getUserName()))
                .addElement(messageCell));
    }
    
    private void updateSession(HttpSession session, ParameterManager parameters) {
        // Has the pipe id search criterion changed?
        Long pipeIdFromSession = (Long) session.getAttribute(SESSION_PREFIX + PIPE_ID);
        if(pipeIdFromSession != null && parameters.getInt(PIPE_ID) != pipeIdFromSession.intValue()) {
            session.setAttribute(SESSION_PREFIX + PIPE_ID, new Long(parameters.getInt(PIPE_ID)));
            session.removeAttribute(SESSION_PREFIX + PAGE);
        } else if(parameters.wasGiven(PAGE))
            session.setAttribute(SESSION_PREFIX + PAGE, new Integer(parameters.getInt(PAGE)));
    }

    private void setPage(Integer page, HttpSession session) {
        session.setAttribute(SESSION_PREFIX + PAGE, page);
    }
    
    private int getPage(HttpSession session) {
        Integer page = (Integer) session.getAttribute(SESSION_PREFIX + PAGE);
        return page != null ? page.intValue() : 0;
    }

    private Long getPipeId(HttpSession session) {
        return (Long) session.getAttribute(SESSION_PREFIX + PIPE_ID);
    }


    private String getPager(int currentPage, int pages, LabelResource labels) {
        P pager = new P().setAlign(AlignType.CENTER);

        if(currentPage > 0){
            pager
	            .addElement(new A("TransferLog?" + PAGE + "=" + (0), "<< " + labels.getLabel("first")))
	        	.addElement("&nbsp;")
	            .addElement(new A("TransferLog?" + PAGE + "=" + (currentPage - 1), "< " + labels.getLabel("previous")))
	        	.addElement("&nbsp;");
        }else{
            pager
	            .addElement("<< " + labels.getLabel("first"))
	        	.addElement("&nbsp;")
	            .addElement("< " + labels.getLabel("previous"))
	        	.addElement("&nbsp;");
        }

        for(int i = 0; i < pages; i++) {
            if(i == currentPage)
                pager.addElement(new B(String.valueOf(i + 1)));
            else if(Math.abs(i - currentPage) <= PAGER_DIRECT_LINKS)
                pager.addElement(new A("TransferLog?" + PAGE + "=" + i, String.valueOf(i + 1)));
            if(currentPage - i <= PAGER_DIRECT_LINKS && currentPage - i > -(PAGER_DIRECT_LINKS) && i != pages - 1)
                pager.addElement(" | ");
        }

        if(currentPage < pages - 1){
        	pager
	        	.addElement("&nbsp;")
	            .addElement(new A("TransferLog?" + PAGE + "=" + (currentPage + 1), labels.getLabel("next") + " >"))
	        	.addElement("&nbsp;")
	            .addElement(new A("TransferLog?" + PAGE + "=" + (pages-1), labels.getLabel("last") + " >>"));
        }else{
        	pager
	        	.addElement("&nbsp;")
	            .addElement(labels.getLabel("next") + " >")
	        	.addElement("&nbsp;")
	            .addElement(labels.getLabel("last") + " >>");
	    }
        
        pager.addElement("</p>");

        return pager.toString();
    }
} // SyncroTransferLog
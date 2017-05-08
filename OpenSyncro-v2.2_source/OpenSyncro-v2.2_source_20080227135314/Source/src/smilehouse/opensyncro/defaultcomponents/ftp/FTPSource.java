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
 * Created on Feb 7, 2005
 */
package smilehouse.opensyncro.defaultcomponents.ftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.formatter.IntegerFormatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.opensyncro.system.Environment;

/** A quick adaptation from FTPDestination component */

public class FTPSource implements SourceIF, GUIConfigurationIF {

    private static final String HOST_ATTR = "host";
    private static final String PORT_ATTR = "port";
    private static final String USER_ATTR = "user";
    private static final String PASSWORD_ATTR = "password";
    private static final String FILENAME_START_ATTR = "file_name_start";
    private static final String DATE_FORMAT_ATTR = "date_format";
    private static final String FILE_EXTENSION_ATTR = "file_extension";
    private static final String FILE_TYPE_ATTR = "file_type";
    private static final String DATE_DAYSINCREMENT = "date_daysincrement";
    private static final String CHARSET_ATTR = "charset";

    private static final String[] FILE_TYPE_LABELS = {"ascii", "binary"};

    private static final int FILE_TYPE_ASCII = 0;
    private static final int FILE_TYPE_BINARY = 1;
    
    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();
    private static final String DEFAULT_CHARSET = "UTF-8";

    //  --------------
    // GUI definition
    // --------------
    protected static FTPSourceGUI gui = new FTPSourceGUI();

    protected static class FTPSourceGUI extends GUIDefinition {

        public FTPSourceGUI() {
            try {
                addSimpleTextFieldForComponent(HOST_ATTR, HOST_ATTR, 70);
                addSimpleTextFieldForComponent(PORT_ATTR, PORT_ATTR, 10);
                addSimpleTextFieldForComponent(USER_ATTR, USER_ATTR, 10);
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return "";
                            //return ((FTPSource) model).getAttribute(PASSWORD_ATTR);
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            String valueStr = (String) value;
                            if(valueStr != null && valueStr.length() > 0)
                                ((FTPSource) model).data.setAttribute(PASSWORD_ATTR, valueStr);
                        }
                    };

                    PasswordEditor editor = new PasswordEditor();
                    editor.setSize(10);

                    FieldInfo fieldInfo = new FieldInfo(
                        PASSWORD_ATTR,
                        PASSWORD_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(PASSWORD_ATTR, fieldInfo);
                }
                addSimpleTextFieldForComponent(FILENAME_START_ATTR, FILENAME_START_ATTR, 20);
                addSimpleTextFieldForComponent(DATE_FORMAT_ATTR, DATE_FORMAT_ATTR, 20);
                addSimpleTextFieldForComponent(FILE_EXTENSION_ATTR, FILE_EXTENSION_ATTR, 5);
                addSimpleTextFieldForComponent(DATE_DAYSINCREMENT, DATE_DAYSINCREMENT, 5);

                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return new Integer(((FTPSource) model).getFileType());
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((FTPSource) model).setFileType(((Integer) value).intValue());
                        }
                    };

                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < FILE_TYPE_LABELS.length; i++)
                        editor.addOption(new DefaultSelectOption(
                            new Integer(i),
                            FILE_TYPE_LABELS[i]));

                    editor.setFormatter(new IntegerFormatter());
                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        FILE_TYPE_ATTR,
                        FILE_TYPE_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(FILE_TYPE_ATTR, fieldInfo);
                }
                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((FTPSource) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((FTPSource) model).getData().setAttribute(
                                CHARSET_ATTR,
                                (String) value);
                        }
                    };

                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < CHARSETS.length; i++)
                        editor.addOption(new DefaultSelectOption(CHARSETS[i], CHARSETS[i]));

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for FTPSource", e);
            }
           
        }


    }


    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td colspan=\"2\">$" + HOST_ATTR + "$</td><td>$"
                + PORT_ATTR + "$</td></tr>" + "<tr><td>$" + USER_ATTR + "$</td><td colspan=\"2\">$"
                + PASSWORD_ATTR + "$</td></tr>" + "<tr><td>$" + FILENAME_START_ATTR + "$</td><td>$"
                + DATE_FORMAT_ATTR + "$</td><td>$" + FILE_EXTENSION_ATTR + "$</td></tr>"
                + "<tr><td>$" + FILE_TYPE_ATTR + "$</td>"
                + "<td colspan=\"2\">$" + DATE_DAYSINCREMENT + "$</td>"
                + "</tr><tr><td colspan=\"3\">$"+CHARSET_ATTR+"$</td></tr></table>";
    }

    public FTPSource(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    private boolean allDataOutput;

    protected PipeComponentData data;

    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }

    public final int getType() {
        return TYPE_SOURCE;
    }

    public String getName() {
        return "FTPSource";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    // Dummy methods due to no iteration supported
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException {
        this.allDataOutput = false;
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) {}

    public int getFileType() {
        String fileTypeAttr = this.data.getAttribute(FILE_TYPE_ATTR);
        if(fileTypeAttr != null) {
            try {
                return Integer.parseInt(fileTypeAttr);
            } catch(NumberFormatException nfe) {
                // ignored...
            }
        }
        return FILE_TYPE_BINARY;
    }

    public void setFileType(int fileType) {
        this.data.setAttribute(FILE_TYPE_ATTR, String.valueOf(fileType));
    }

    // Add/substract days from a Date
    private Date addDaysToDate(Date date, int days) {
        Calendar cl= GregorianCalendar.getInstance();
        cl.setTime( new Date());
        cl.add(Calendar.DATE, days);
        return cl.getTime();
    }
    
    public String getFileName(MessageLogger logger) throws FailTransferException {
        int daysIncrement = 0;
        
        String daysIncrementParamValue = this.data.getAttribute(DATE_DAYSINCREMENT);

        // Use zero value if the parameter is not specified
        if(daysIncrementParamValue == null || daysIncrementParamValue.length() == 0) {
            daysIncrementParamValue = new String("0");
        }
        
        try {
            
            daysIncrement = new Integer(daysIncrementParamValue).intValue();
        } catch(NumberFormatException e) {
            logger.logMessage("Invalid day increment value (\"" +
                this.data.getAttribute(DATE_DAYSINCREMENT)
                + "\"), should be an integer", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        
        String fileName = this.data.getAttribute(FILENAME_START_ATTR);
        String dateFormatStr = this.data.getAttribute(DATE_FORMAT_ATTR);
        
        if(dateFormatStr != null && dateFormatStr.length() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
            fileName += dateFormat.format(addDaysToDate(new Date(), daysIncrement));
        }
        
        String extension = this.data.getAttribute(FILE_EXTENSION_ATTR);
        if(extension != null && extension.length() > 0)
            fileName += "." + extension;

        return fileName;
    }


    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {

        // This component does not support iteration, so we output all our data
        // once (and only once)
        if(this.allDataOutput == true)
            return null;
        else
            this.allDataOutput = true;

        
        String[] dataArray = new String[1];
        FTPClient ftp = new FTPClient();
        
        try {
            // -----------------
            // Try to connect...
            // -----------------
            String host = this.data.getAttribute(HOST_ATTR);
            int port = -1;
            String portStr = this.data.getAttribute(PORT_ATTR);
            if(portStr != null && portStr.length() > 0) {
                try {
                    port = Integer.parseInt(portStr);
                } catch(NumberFormatException nfe) {
                    logger.logMessage(
                        "Invalid value '" + portStr + "' for port.",
                        this,
                        MessageLogger.ERROR);
                    PipeComponentUtils.failTransfer();
                }
            }
            int reply;

            try {

                if(port != -1) {
                    ftp.connect(host, port);
                } else {
                    ftp.connect(host);
                }

            } catch(SocketException e) {
                logger.logMessage(
                    "SocketException while connecting to host " + host + ", aborting",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            } catch(IOException e) {
                logger.logMessage(
                    "IOException while connecting to host " + host + ", aborting",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                try {
                    ftp.disconnect();
                } catch(IOException e) {
                    /**
                     * ftp.disconnect() is called only as additional clean-up here, so we choose to
                     * ignore possible exceptions
                     */
                }
                logger.logMessage(
                    "Couldn't connect to the FTP server: " + ftp.getReplyString(),
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            // -----------
            // Then log in
            // -----------
            try {
                if(!ftp.login(this.data.getAttribute(USER_ATTR), this.data
                    .getAttribute(PASSWORD_ATTR))) {
                    logger.logMessage(
                        "Could not log in, check your username and password settings.",
                        this,
                        MessageLogger.ERROR);
                    ftp.logout();

                    PipeComponentUtils.failTransfer();
                }

            } catch(IOException e) {
                logger.logMessage(
                    "IOException while logging in to FTP server",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            // Use passive mode
            ftp.enterLocalPassiveMode();

            // -----------------
            // ASCII or binary ?
            // -----------------
            boolean fileTypeSetOk = false;
            try {
                switch(getFileType()) {
                case FILE_TYPE_ASCII:
                    fileTypeSetOk = ftp.setFileType(FTP.ASCII_FILE_TYPE);
                    break;
                case FILE_TYPE_BINARY:
                    fileTypeSetOk = ftp.setFileType(FTP.BINARY_FILE_TYPE);
                }
                if(!fileTypeSetOk) {
                    logger.logMessage(
                        "Could not set file type: " + ftp.getReplyString(),
                        this,
                        MessageLogger.WARNING);
                }
            } catch(IOException e) {
                logger.logMessage(
                    "IOException while setting file transfer type parameter",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            // -----------------
            // Retrieve the data
            // -----------------
            String fileName = getFileName(logger);
            logger.logMessage("Retrieving file: " + fileName, this, MessageLogger.DEBUG);
            OutputStream dataStream = new ByteArrayOutputStream();
            try {
                if(!ftp.retrieveFile(fileName, dataStream)) {
                    logger.logMessage("Could not retrieve file '" + fileName + "': "
                            + ftp.getReplyString(), this, MessageLogger.WARNING);
                    PipeComponentUtils.abortTransfer();
                }
                String charSet=this.data.getAttribute(CHARSET_ATTR);
                if(charSet == null || charSet.length() == 0)
                    charSet = DEFAULT_CHARSET;
                dataArray[0] = ((ByteArrayOutputStream)dataStream).toString(charSet);
            } catch(IOException e) {
                logger.logMessage(
                    "IOException while downloading file from FTP server",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }
        } finally {
            if(ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch(IOException f) {
                    // do nothing
                }
            }
        }
        return dataArray;
    }
}
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

package smilehouse.opensyncro.defaultcomponents.converter.xml.xslt;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextAreaEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.ConversionInfo;
import smilehouse.opensyncro.system.Environment;

/**
 * XSLTConverter.java
 * 
 * Performs XSLT 1.x compatible XSL transformations to input data block
 * using Xalan.
 * 
 * Created: Mon Mar 22 15:00:33 2004
 */

public class XSLTConverter implements ConverterIF, GUIConfigurationIF {

    private static final String XSLT_ATTRIBUTE = "xslt";
    
    private static TransformerFactory factory;

    static {
        try {
            factory = new org.apache.xalan.processor.TransformerFactoryImpl();
        } catch(TransformerFactoryConfigurationError e) {
            Environment.getInstance().log(
                "XSLTConverter: Unable to get XSL TransformerFactory (Xalan)", e.getException());
        }
    }

    // Prevent thread switching while we get the transformerFactory
    private static synchronized void initTransformerFactory() throws FailTransferException {

        // Save the original TransformerFactory system property value to avoid
        // interference with Xalan (XSLT 1.x) converters running at the same time
        String transformerFactoryBackup = System
            .getProperty("javax.xml.transform.TransformerFactory");
        System.setProperty(
            "javax.xml.transform.TransformerFactory",
            "org.apache.xalan.processor.TransformerFactoryImpl");

        try {
            factory = TransformerFactory.newInstance();
        } catch(TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            PipeComponentUtils.failTransfer();
        }

        // Restore the original TransformerFactory system property value
        if(transformerFactoryBackup == null) {
            // If we try to set a System property back to null, an Exception will occur.
            Properties p = System.getProperties();
            p.remove("javax.xml.transform.TransformerFactory");
            System.setProperties(p);
        } else {
            System.setProperty("javax.xml.transform.TransformerFactory", transformerFactoryBackup);
        }
    }
    
    // --------------
    // GUI definition
    // --------------
    private static GUIContextContainer guiContextContainer = new GUIContextContainer();

    private static class GUIContextContainer {

        private GUIContext context;

        public GUIContextContainer() {
            try {
                this.context = new GUIContext();
                //set unique id and description labelkey
                String id = "xslt";
                String label = "xslt";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                        return ((XSLTConverter) model).getXSLT();
                    }

                    public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                        ((XSLTConverter) model).setXSLT((String) value);
                    }
                };

                TextAreaEditor editor = new TextAreaEditor();
                editor.setCols(100);
                editor.setRows(30);

                //and finally create the configurationObject
                FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                //add the configuration to the context for usage in the http-requests.
                context.addFieldInfo(fieldInfo);
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for XSLTConverter", e);
            }
        }

        public GUIContext getGUIContext() {
            return context;
        }
    }

    public XSLTConverter() {}

    public String getXSLT() {
        String xslt = this.data.getAttribute(XSLT_ATTRIBUTE);
        if(xslt != null)
            return xslt;
        else
            return "";
    }

    public void setXSLT(String xslt) {
        this.data.setAttribute(XSLT_ATTRIBUTE, xslt);
    }
    
    public XSLTConverter( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    protected PipeComponentData data;
    
    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }
    
    public final int getType() {
        return TYPE_CONVERTER;
    }
    
    public String getName() {
        return "XSLTConverter";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }
    
    // Dummy methods due to no iteration supported
    public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) { }

    
    /**
     * The method actually called by pipe during the conversion. This default implementation uses
     * the convert-method to convert all the input records separately and is usually sufficient so
     * you only have to implement it. If you however need access to all the input when converting
     * (foer example Join-converter) you need to override this.
     */
    public String[] convertAll(String[] data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String[][] allResults = new String[data.length][];
        int resultCount = 0;
        for(int i = 0; i < data.length; i++) {
            allResults[i] = convert(data[i], info, logger);
            resultCount += allResults[i].length;
        }
        if(data.length == 1)
            return allResults[0];
        else {
            String[] combinedResult = new String[resultCount];
            int c = 0;
            for(int i = 0; i < allResults.length; i++) {
                for(int j = 0; j < allResults[i].length; j++, c++) {
                    combinedResult[c] = allResults[i][j];
                }
            }
            return combinedResult;
        }
    }
    
    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String xslt = getXSLT();
        if(xslt != null && xslt.length() > 0) {

            Transformer transformer = null;

            javax.xml.transform.Source xsltSource = new StreamSource(new StringReader(xslt));

            // Even though we don't know the source XSLT file location here, we need to set
            // some base URI to the script's StreamSource in order to enable XSLT access other
            // files in the file system. By setting the SystemId to an empty string, XSLT's
            // base URI gets the file:// path where OpenSyncro's servlet container was
            // launched from.
            xsltSource.setSystemId("");
            
            javax.xml.transform.Source source = new StreamSource(new StringReader(data));

            StringWriter resultWriter = new StringWriter();
            Result result = new StreamResult(resultWriter);

            try {
                logger.logMessage("Loading XSL Transformation", this, MessageLogger.DEBUG);
                factory.setErrorListener(new TransformerErrorListener(
                    "parsing XSLT script or input data to DOM",
                    logger,
                    this));
                transformer = factory.newTransformer(xsltSource);


            } catch(TransformerException e) {

                // Provide XSLT line and column numbers with error message if available
                /*
                 * SourceLocator loc = e.getLocator(); if(loc != null) { logger.logMessage("Error
                 * parsing XSLT script at line " + loc.getLineNumber() + ", column " +
                 * loc.getColumnNumber() + ": \"" + getExceptionErrorMessage(e) + "\". Aborting",
                 * this, MessageLogger.ERROR); } else { logger.logMessage("Error parsing XSLT
                 * script: \"" + getExceptionErrorMessage(e) + "\". Aborting", this,
                 * MessageLogger.ERROR); }
                 */
                logger.logMessage("Aborting XSLT and input data parsing", this, MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();

            }

            try {

                logger.logMessage("Starting XSL Transformation", this, MessageLogger.DEBUG);
                transformer.setErrorListener(new TransformerErrorListener(
                    "executing XSLT script",
                    logger,
                    this));
                transformer.transform(source, result);
            } catch(TransformerException e) {

                // Provide XSLT line and column numbers with error message if available
                /*
                 * SourceLocator loc = e.getLocator(); if(loc != null) { logger.logMessage("Error
                 * executing XSLT script at line " + loc.getLineNumber() + ", column " +
                 * loc.getColumnNumber() + ": \"" + getExceptionErrorMessage(e) + "\". Aborting",
                 * this, MessageLogger.ERROR); } else { logger.logMessage("Error executing XSLT
                 * script: \"" + getExceptionErrorMessage(e) + "\". Aborting", this,
                 * MessageLogger.ERROR); }
                 */
                logger.logMessage("Aborting XSLT execution", this, MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            resultWriter.flush();
            logger.logMessage("XSL Transformation complete", this, MessageLogger.DEBUG);

            return new String[] {resultWriter.toString()};
        } else {
            logger.logMessage(
                "Empty XSLT script, returning data unchanged",
                this,
                MessageLogger.WARNING);
            return new String[] {data};
        }
    }

    public GUIContext getGUIContext() {
        return guiContextContainer.getGUIContext();
    }

    public String getGUITemplate() {
        return null;
    }

}// XSLTConverter

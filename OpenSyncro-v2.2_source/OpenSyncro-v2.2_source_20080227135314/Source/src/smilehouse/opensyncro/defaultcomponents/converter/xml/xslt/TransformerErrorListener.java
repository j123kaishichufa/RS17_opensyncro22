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
 * Created on Aug 8, 2005
 * 
 * ToDo: Error and Fatal Error ErrorListeners do not seem to abort XSLT processing
 * by throwing a TransformerException, which sometimes causes multiple error
 * messages of the same type to be logged in sequence.
 * 
 */
package smilehouse.opensyncro.defaultcomponents.converter.xml.xslt;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.log.MessageLogger;


    /* ErrorListener for XSLT parsing and runtime warnings and errors.
     * Writes warning, error and fatal error messages to Transfer Log.
     */
    public class TransformerErrorListener implements ErrorListener {
        private MessageLogger logger;
        private String errorMessageActionText;
        ConverterIF parentClassReference;

        public TransformerErrorListener(String errorMessageActionText,
                                        MessageLogger messageLogger,
                                        ConverterIF parentClassReference) {
            this.errorMessageActionText = errorMessageActionText;
            this.logger = messageLogger;
            this.parentClassReference = parentClassReference;
        }

        public void warning(TransformerException exception) {
            SourceLocator loc = exception.getLocator();
            if(loc != null) {
                logger.logMessage(
                    "Warning while " + errorMessageActionText + " at line " + loc.getLineNumber()
                            + ", column " + loc.getColumnNumber() + ": "
                            + getExceptionErrorMessage(exception),
                    parentClassReference,
                    MessageLogger.WARNING);

            } else {
                logger.logMessage(
                    "Warning while " + errorMessageActionText + ": "
                            + getExceptionErrorMessage(exception),
                    parentClassReference,
                    MessageLogger.WARNING);

            }
        }

        public void error(TransformerException exception) throws TransformerException {

            SourceLocator loc = exception.getLocator();
            if(loc != null) {
                logger.logMessage(
                    "Error " + errorMessageActionText + " at line " + loc.getLineNumber()
                            + ", column " + loc.getColumnNumber() + ": "
                            + getExceptionErrorMessage(exception),
                    parentClassReference,
                    MessageLogger.ERROR);
            } else {
                logger.logMessage(
                    "Error " + errorMessageActionText + ": " + getExceptionErrorMessage(exception),
                    parentClassReference,
                    MessageLogger.ERROR);
            }

            /*
             * We try to abort XSLT processing at a fatal error, but throwing the
             * TransformerException does not seem to work(?)
             */
            throw exception;

        }

        public void fatalError(TransformerException exception) throws TransformerException {
            SourceLocator loc = exception.getLocator();
            if(loc != null) {
                logger.logMessage(
                    "Fatal error " + errorMessageActionText + " at line " + loc.getLineNumber()
                            + ", column " + loc.getColumnNumber() + ": "
                            + getExceptionErrorMessage(exception),
                    parentClassReference,
                    MessageLogger.ERROR);


            } else {
                logger.logMessage(
                    "Fatal error " + errorMessageActionText + ": "
                            + getExceptionErrorMessage(exception),
                    parentClassReference,
                    MessageLogger.ERROR);

            }
            /*
             * We should abort XSLT processing at a fatal error, but throwing the
             * TransformerException does not seem to work(?)
             */
            throw exception;
        }
        
        /*
         * Get a complete Exception error message and skip Exception names before the detailed error
         * message
         */
        private String getExceptionErrorMessage(Exception e) {
            int exceptionStringOffset;
            String errorMessage = e.getMessage();

            exceptionStringOffset = errorMessage.indexOf("Exception: ");
            while(exceptionStringOffset >= 0) {

                // Length of string "Exception: " == 11
                errorMessage = errorMessage
                    .substring(exceptionStringOffset + 11, errorMessage.length());
                exceptionStringOffset = errorMessage.indexOf("Exception: ");
            }
            return errorMessage;
        }
    }

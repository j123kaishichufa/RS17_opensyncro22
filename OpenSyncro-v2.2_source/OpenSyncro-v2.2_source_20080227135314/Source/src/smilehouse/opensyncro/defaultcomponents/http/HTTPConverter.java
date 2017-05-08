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

// TODO: Option to include response headers in output?

package smilehouse.opensyncro.defaultcomponents.http;

import java.net.URL;

import org.apache.commons.httpclient.HttpException;

import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.ConversionInfo;
import smilehouse.opensyncro.system.Environment;

public class HTTPConverter extends HTTPDestination implements ConverterIF {

    public HTTPConverter( Object pipeComponentData ) {
        super( pipeComponentData );
    }
    
    public String getName() {
        return "HTTPConverter";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public int getType() {
        return TYPE_CONVERTER;
    }

    
    // Dummy methods due to no iteration supported
    public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }
    
    
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
        
        // Get and validate parameters
        
        int port = getPort(logger);

        initializeProtocol(port);

        URL url = getURL(logger, port);

        String method = getValidatedMethod(data, logger);

        String[][] parameters = getParameters(method, data, logger);

        String charsetName = this.data.getAttribute(CHARSET_ATTR);
        if(charsetName == null || charsetName.length() == 0)
            charsetName = HTTPDestination.DEFAULT_CHARSET;
        
        String responseCharsetName = null;
        if (getChosenResponseCharset()) {
        	responseCharsetName = this.data.getAttribute(RESPONSE_CHARSET_ATTR);
            if(responseCharsetName == null || responseCharsetName.length() == 0)
                responseCharsetName = null;
    	}
        
        String contentType = this.data.getAttribute(CONTENT_TYPE_ATTR);
        if(contentType == null || contentType.length() == 0)
            contentType = null;
        
        // Proceed to send the HTTP request
        HTTPResponse response = null;
        try {
        	
            response = sendHTTPRequest(url, method, parameters, data,
                                       charsetName, responseCharsetName, contentType);
            if(getLogEnabled()) {
                Environment.getInstance().log(
                    this.getName() + ", server response:\n" + response.getResponseHeaders() + "\n"
                            + response.getResponseBody());
            }
            
            if(response.getResponseError() != null) {
                if(getResponseCodeCheckDisabled() == false) {
                    throw new HttpException(response.getResponseError());
                } else {
                    logger.logMessage("Ignoring HTTP error response: " +
                        response.getResponseError(), this, MessageLogger.DEBUG);
                }
            }
            
        } catch(Exception e) {
            logger.logMessage(e.toString(), this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        logger.logMessage("Request complete", this, MessageLogger.DEBUG);

        if( response.getResponseBody() != null ) {
            return new String[] { response.getResponseBody() };
        } else {
            logger.logMessage("Empty response body detected, aborting.",
                              this, MessageLogger.WARNING);
            PipeComponentUtils.abortTransfer();

            // This code shouldn't be reached
            return null;
        }
    }

}

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

package smilehouse.opensyncro.defaultcomponents.workspace;

import java.rmi.RemoteException;

import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ImportResult;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.ConversionInfo;
import smilehouse.opensyncro.system.Environment;

public class RemoteOrderConverter extends RemoteOrderDestination implements ConverterIF{
	
	public RemoteOrderConverter(Object pipeComponentData){
		super(pipeComponentData);
	}

	public String getID() {
		return this.getClass().getName();
	}
	
	public String getName() {
		return "RemoteOrderConverter";
	}

	public int getType() {
		return TYPE_CONVERTER;
	}
	
	public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
		// ------------------
        // Get the parameters
        // ------------------
    	
        getParameters(logger);
        
        // ---------------------------
        // Get the open interface stub
        // ---------------------------
        openInterface = WorkspaceOIUtils.getOpenInterfaceIF(oiDatabase, oiHost, logger, this);
		return ITERATION_OPEN_STATUS_OK;
	}
	
	public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
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
        String[] results = new String[1];
        
        try {
            ImportResult result = openInterface.importOrders(oiLoginInfo, data, oiImportMode, additiveAnswerUpdate, additiveBasketUpdate, invokeReceivedEvents);
            WorkspaceOIUtils.logImportResult(result, logger, this,  false);
        }
        catch(AccessDeniedException ade) {
            logger.logMessage("Access to Open Interface denied. Check your login information.", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        catch(OpenInterfaceException oie) {
            logger.logMessage("Open Interface reported error: " + oie.toString(), this, MessageLogger.ERROR);
            Environment.getInstance().log("Open Interface reported error", oie);
            PipeComponentUtils.failTransfer();
        }
        catch(RemoteException re) {
            logger.logMessage("Error contacting Open Interface: " + re.toString(), this, MessageLogger.ERROR);
            Environment.getInstance().log("Error Contacting OpenInterface", re);
            PipeComponentUtils.failTransfer();
        }
        results[0] = "";
        return results;
    }

}

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

package smilehouse.opensyncro.pipes.metadata;

import java.util.Properties;

/**
 * TransferInfo.java
 * 
 * Created: Tue Apr 27 11:55:35 2004
 */

public class TransferInfo implements SourceInfo, ConversionInfo, DestinationInfo {

    private String database;
    private Properties sourceParameters;
    private Properties conversionParameters;
    private Properties destinationParameters;
    private String userName;

    public TransferInfo(String database) {
        this(database, null, null, null, null);
    }
    
    public TransferInfo(String database,String userName){
    	this(database,null,null,null,userName);
    }
    
    public TransferInfo(String database,
            Properties sourceParameters,
            Properties conversionParameters,
            Properties destinationParameters) {
    	this(database,sourceParameters,conversionParameters,destinationParameters,null);
    }
    
    public TransferInfo(String database,
                        Properties sourceParameters,
                        Properties conversionParameters,
                        Properties destinationParameters,
                        String userName) {
        this.database = database;
        this.sourceParameters = sourceParameters;
        this.conversionParameters = conversionParameters;
        this.destinationParameters = destinationParameters;
        this.userName=userName;
    }

    public String getDatabaseName() {
        return database;
    }

    public Properties getSourceParameters() {
        return sourceParameters;
    }

    public Properties getConversionParameters() {
        return conversionParameters;
    }

    public Properties getDestinationParameters() {
        return destinationParameters;
    }
   
    public String getUserName(){
    	return userName;
    }
} // TransferInfo
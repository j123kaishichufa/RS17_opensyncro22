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
 * Created on May 6, 2005
 */
package smilehouse.opensyncro.pipes;

import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.system.Persister;

    public class ConverterListItem {
    
    private Long id;
    private Pipe parent;
    private String converterID;
    private ConverterIF converter;
    private PipeComponentData converterData;

    
    public ConverterListItem( ) { 
        this.id = null;
        }
    
    public ConverterListItem( String converterID, PipeComponentData converterData ) {
        this.id = null;
        this.converter = (ConverterIF) Persister.getInstance( converterID, converterData );
        this.converterID = converterID;
        this.converterData = converterData;
    }

    public ConverterListItem( ConverterIF converter, PipeComponentData converterData ) {
        this.id = null;
        this.converter = converter;
        this.converterID = converter.getID();
        this.converterData = converterData;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    protected void setParent(Pipe parent) {
        this.parent = parent;
    }

    public Pipe getParent() {
        return this.parent;
    }

    public String getConverterID() {
        return this.converterID;
    }
    public void setConverterID(String converterID) {
        this.converterID = converterID;
    }
   
    public ConverterIF getConverter() {
        if(this.converter == null) {
            this.converter = (ConverterIF) Persister.getInstance(getConverterID());    
        }
        return this.converter;
    }

    public void setConverter(ConverterIF converter) {
        this.converterID = converter.getID();
        this.converter = converter;
    }
   
    public PipeComponentData getConverterData() {
        return converterData;
    }
    public void setConverterData(PipeComponentData converterData) {
        this.converterData = converterData;
    }

}
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
 * Created on Apr 26, 2005
 */
package smilehouse.opensyncro.pipes.component;

import java.util.Locale;

public interface PipeComponentIF {

    	/** Component types */
        public static final int TYPE_SOURCE = 0;
        public static final int TYPE_DESTINATION = 1;
        public static final int TYPE_CONVERTER = 2;

        /** Extension used in custom label ResourceBundles for this component */
        public static final String LABEL_RESOURCE_EXTENSION = "Labels";

        public static final String COMPONENT_NAME_LABEL_KEY = "component_name";
        public static final String COMPONENT_DESCRIPTION_LABEL_KEY = "component_description";

        /** Iteration open/close return codes */
        public static final int ITERATION_OPEN_STATUS_OK = 1;
        public static final int ITERATION_OPEN_STATUS_ERROR = 0;
        public static final int ITERATION_CLOSE_STATUS_OK = 1;
        public static final int ITERATION_CLOSE_STATUS_ERROR = 0;
        
        /**
         * Setter for PipeComponent's data.
         * Used by the OpenSyncro framework to give parameters to Pipe Component.  
         * 
         * @param data PipeComponentData, containing all component's attributes (parameters).
         */
        public void setData(PipeComponentData data);
        
        /**
         * Getter for PipeComponent's data. 
         * 
         * @return PipeComponentData containing component's attributes (parameters).
         */
        public PipeComponentData getData();
        
        /**
         * Returns component ID.
         * The ID should the the full classname, i.e. this.getClass().getName()
         */
        public String getID();

        /** Returns a localized description text of the component */
        public String getDescription(Locale locale);

        /**
         * Returns name of the component.
         * The name should be the component's class name.
         */
        public String getName();

        /** Returns type of the component: either TYPE_SOURCE, TYPE_DESTINATION or TYPE_CONVERTER */
        public int getType();
        
}
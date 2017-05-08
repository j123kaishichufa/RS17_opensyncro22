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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.Entities;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.system.Persister;
import smilehouse.tools.ui.web.TableTool;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;

/**
 * SyncroComponents.java
 * 
 * Created: Thu Apr 22 15:26:55 2004
 */

public class SyncroComponents extends SyncroServlet {

    private static final String COMPONENT_ID = "component_id";
    private static final String NAME = "name";
    private static final String TYPE = "component_type";
    private static final String DESCRIPTION = "description";

    public void initParameters(ParameterManager pm) {
        pm.addParameter(ACTION, ParameterManager.INT);
    }

    public int getTabSetId() {
        return 1;
    }

    public int getTabId() {
        return 2;
    }

    public String handleRequest(HttpServletRequest req,
                                HttpSession session,
                                ParameterManager parameters,
                                Persister pers,
                                LabelResource labels,
                                int requestType){

        // Reload components if reload button was pressed
        if(parameters.getInt(ACTION) == 1) {
            pers.reloadComponents();
        }
        
        List components = pers.loadComponentImplementations();
        
        // Sort component list by component type (primary) and ID (secondary)
        Collections.sort(components, new Comparator() {
            public int compare(Object o1, Object o2) {
                int componentType1 = ((PipeComponentIF) o1).getType();
                int componentType2 = ((PipeComponentIF) o2).getType();
                if(componentType1 == componentType2) {
                    String componentID1 = ((PipeComponentIF) o1).getID();
                    String componentID2 = ((PipeComponentIF) o2).getID();
                    return componentID1.compareTo(componentID2);
                } else {
                    return componentType1 - componentType2;
                }
            }
        });
        
        TableTool tt = new TableTool();
        tt.setZebra(true);
        Table componentTable = tt.getInnerTable().setWidth("100%");
        componentTable.addElement(tt.getHeaderRow(new String[] {
                labels.getLabel(NAME),
                labels.getLabel(TYPE),
                labels.getLabel(COMPONENT_ID),
                labels.getLabel(DESCRIPTION),
                Entities.NBSP}));
        Locale locale = labels.getLocale();
        for(Iterator iter = components.iterator(); iter.hasNext();) {
            PipeComponentIF impl = (PipeComponentIF) iter.next();
            TR row = tt.getRow();
            row.addElement(new TD().addElement(impl.getName()))
                .addElement(
                    new TD().addElement(labels.getLabel("component_type"
                        + impl.getType())))
                        .addElement(new TD().addElement(impl.getID()))
                        .addElement(new TD().addElement(impl.getDescription(locale)));
            if(pers.isDynamicComponent(impl)) {
                row.setClass("dynamicComponent");
            }
            componentTable.addElement(row);
        }

        Input reloadComponentsButton = new Input(Input.BUTTON, "reload_components", labels
            .getLabel("reload_components"));
        reloadComponentsButton.setOnClick("document.forms[0]." + ACTION + ".value=1;document.forms[0].submit();");

       
        ElementContainer content = new ElementContainer().addElement(
            tt.getOuterTable(componentTable).setWidth("100%")).addElement(new HR()).
            addElement(reloadComponentsButton);

        
        Form form = getForm("Components", content.toString(), labels, false, false).addElement(
            new Input(Input.HIDDEN, ACTION, "0"));

        String result = form.toString();

        return result;
    }

} // SyncroComponents
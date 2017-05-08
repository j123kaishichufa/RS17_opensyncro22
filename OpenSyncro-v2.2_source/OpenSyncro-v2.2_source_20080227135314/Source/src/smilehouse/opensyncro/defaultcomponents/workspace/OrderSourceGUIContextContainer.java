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

import java.text.ParseException;
import java.util.Date;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.formatter.DoubleFormatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.system.Environment;

/**
 * OrderSourceGUIContextContainer.java
 * 
 * Created: Mon Apr 5 10:55:17 2004
 */

public class OrderSourceGUIContextContainer {

    protected GUIContext context;

    private static class ComponentAttributeModifier implements ModelModifier {

        private String attributeName;

        public ComponentAttributeModifier(String attributeName) {
            this.attributeName = attributeName;
        }

        public Object getValue(Object model)
        // throws LocalizedException
        {
            String value = ((PipeComponentIF) model).getData().getAttribute(attributeName);
            return value != null ? value : "";
        }

        public void setValue(Object model, Object value)
        // throws LocalizedException
        {
            ((PipeComponentIF) model).getData().setAttribute(attributeName, (String) value);
        }
    }

    private static final Object[][] SIMPLE_CASES = {
            {OrderSource.CUSTOMER_ID_IN_ATTR, new Integer(70)},
            {OrderSource.ID_GREATER_THAN_ATTR, new Integer(10)},
            {OrderSource.ID_LESS_THAN_ATTR, new Integer(10)},
            {OrderSource.ID_IN_ATTR, new Integer(70)},
            {OrderSource.HANDLING_STATUS_NAME_IN_ATTR, new Integer(70)},
            //{ OrderSource.HANDLING_STATUS_NAME_NOT_IN_ATTR, new Integer(70) },
            {OrderSource.PAYMENT_STATUS_NAME_IN_ATTR, new Integer(70)},
            {OrderSource.NEW_STATUS_NAME_ATTR, new Integer(30)}};
    		//{ OrderSource.PAYMENT_STATUS_NAME_NOT_IN_ATTR, new Integer(70) } };

    public OrderSourceGUIContextContainer() {
        try {
            this.context = new GUIContext();


            // --------------------------
            // First, the simple cases...
            // --------------------------
            for(int i = 0; i < SIMPLE_CASES.length; i++) {
                String attr = (String) SIMPLE_CASES[i][0];
                int size = ((Integer) SIMPLE_CASES[i][1]).intValue();
                ModelModifier modifier = new ComponentAttributeModifier(attr);
                TextEditor editor = new TextEditor();
                editor.setSize(size);
                FieldInfo fieldInfo = new FieldInfo(attr, attr, modifier, editor);
                context.addFieldInfo(fieldInfo);
            }

            // ---------------------------------
            // A couple more complicated ones...
            // ---------------------------------
            {
                // ----------------
                // Sum greater than
                // ----------------
                String id = OrderSource.SUM_GREATER_THAN_ATTR;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((OrderSource) model).getSumGreaterThan();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((OrderSource) model).setSumGreaterThan((Double) value);
                    }
                };
                DoubleFormatter formatter = new DoubleFormatter();
                formatter.setAcceptEmptyValues(true);
                TextEditor editor = new TextEditor();
                editor.setSize(10);
                editor.setFormatter(formatter);

                FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);
                context.addFieldInfo(fieldInfo);
            }
            {
                // -------------
                // Sum less than
                // -------------
                String id = OrderSource.SUM_LESS_THAN_ATTR;
                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        return ((OrderSource) model).getSumLessThan();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((OrderSource) model).setSumLessThan((Double) value);
                    }
                };
                DoubleFormatter formatter = new DoubleFormatter();
                formatter.setAcceptEmptyValues(true);
                TextEditor editor = new TextEditor();
                editor.setSize(10);
                editor.setFormatter(formatter);

                FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);
                context.addFieldInfo(fieldInfo);
            }
            {
                // ----------
                // Date after
                // ----------
                String id = "dateAfter";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        Date date = ((OrderSource) model).getDateAfter();
                        if(date != null)
                            return OrderSource.dateFormat.format(date);
                        else
                            return "";
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        String valueStr = (String) value;
                        Date date = null;
                        if(valueStr != null && valueStr.length() != 0) {
                            try {
                                date = OrderSource.dateFormat.parse(valueStr);
                            } catch(ParseException pe) { /* Ignore invalid dates */ }
                        }
                        ((OrderSource) model).setDateAfter(date);
                    }
                };

                TextEditor editor = new TextEditor();
                editor.setSize(50);

                //and finally create the configurationObject
                FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                //add the configuration to the context for usage in the http-requests.
                context.addFieldInfo(fieldInfo);
            }
            {
                // -----------
                // Date before
                // -----------
                String id = "dateBefore";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        Date date = ((OrderSource) model).getDateBefore();
                        if(date != null)
                            return OrderSource.dateFormat.format(date);
                        else
                            return "";
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        String valueStr = (String) value;
                        Date date = null;
                        if(valueStr != null && valueStr.length() != 0) {
                            try {
                                date = OrderSource.dateFormat.parse(valueStr);
                            } catch(ParseException pe) { /* Ignore invalid dates */ }
                        }
                        ((OrderSource) model).setDateBefore(date);
                    }
                };

                TextEditor editor = new TextEditor();
                editor.setSize(50);

                //and finally create the configurationObject
                FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                //add the configuration to the context for usage in the http-requests.
                context.addFieldInfo(fieldInfo);
            }

        } catch(Exception e) {
            Environment.getInstance().log("OrderSourceGUIContextContainer", e);
        }
    }

    public GUIContext getGUIContext() {
        return context;
    }

} // OrderSourceGUIContextContainer
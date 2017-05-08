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
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.system.Environment;

/**
 * CustomerSourceGUIContextContainer.java
 * 
 * Created: Thu Nov 23 10:55:17 2006
 */

public class CustomerSourceGUIContextContainer {

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
            {CustomerSource.CUSTOMER_ID_ATTR, new Integer(10)},
            {CustomerSource.ID_GREATER_THAN_ATTR, new Integer(10)},
            {CustomerSource.ID_LESS_THAN_ATTR, new Integer(10)},
            {CustomerSource.ID_IN_ATTR, new Integer(70)},
            {CustomerSource.PRIMARY_CUSTOMER_GROUP_ATTR, new Integer(50)},
            {CustomerSource.CUSTOMER_GROUP_ATTR, new Integer(50)}
            };

    public CustomerSourceGUIContextContainer() {
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
                // ----------
                // Date created after 
                // ----------
               String id = "dateCreatedAfter";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        Date date = ((CustomerSource) model).getDateCreatedAfter();
                        if(date != null)
                            return CustomerSource.dateFormat.format(date);
                        else
                            return "";
                    }

            
                    public void setModelValue(Object model, Object value) throws Exception {
                        String valueStr = (String) value;
                        Date date = null;
                        if(valueStr != null && valueStr.length() != 0) {
                            try {
                                date = CustomerSource.dateFormat.parse(valueStr);
                            } catch(ParseException pe) { 
                            /* Ignore invalid dates */ 
                    }
                        }
                        ((CustomerSource) model).setDateCreatedAfter(date);
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
                // Date created before
                // -----------
                String id = "dateCreatedBefore";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        Date date = ((CustomerSource) model).getDateCreatedBefore();
                        if(date != null)
                            return CustomerSource.dateFormat.format(date);
                        else
                            return "";
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        String valueStr = (String) value;
                        Date date = null;
                        if(valueStr != null && valueStr.length() != 0) {
                            try {
                                date = CustomerSource.dateFormat.parse(valueStr);
                            } catch(ParseException pe) {
                             /* Ignore invalid dates */ 
            }
                        }
                        ((CustomerSource) model).setDateCreatedBefore(date);
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
                // ----------
                // Date last visit after 
                // ----------
               String id = "dateLastVisitAfter";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        Date date = ((CustomerSource) model).getDateLastVisitAfter();
                        if(date != null)
                            return CustomerSource.dateFormat.format(date);
                        else
                            return "";
                    }

            
                    public void setModelValue(Object model, Object value) throws Exception {
                        String valueStr = (String) value;
                        Date date = null;
                        if(valueStr != null && valueStr.length() != 0) {
                            try {
                                date = CustomerSource.dateFormat.parse(valueStr);
                            } catch(ParseException pe) { 
                            /* Ignore invalid dates */ 
                    }
                        }
                        ((CustomerSource) model).setDateLastVisitAfter(date);
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
                // Date last visit before
                // -----------
                String id = "dateLastVisitBefore";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws Exception {
                        Date date = ((CustomerSource) model).getDateLastVisitBefore();
                        if(date != null)
                            return CustomerSource.dateFormat.format(date);
                        else
                            return "";
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        String valueStr = (String) value;
                        Date date = null;
                        if(valueStr != null && valueStr.length() != 0) {
                            try {
                                date = CustomerSource.dateFormat.parse(valueStr);
                            } catch(ParseException pe) {
                             /* Ignore invalid dates */ 
                            }
                        }
                        ((CustomerSource) model).setDateLastVisitBefore(date);
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
                // Datebox, customer modified before 
                // -----------
            	 String id = "customerModifiedBefore";
            	 
            	 ModelModifier modifier = new DefaultModelModifier() {
                     public Object getModelValue(Object model) throws Exception {
                         Date date = ((CustomerSource) model).getDateCustModifiedBefore();
                         if(date != null)
                             return CustomerSource.dateFormat.format(date);
                         else
                             return "";
                     }

                     public void setModelValue(Object model, Object value) throws Exception {
                         String valueStr = (String) value;
                         Date date = null;
                         if(valueStr != null && valueStr.length() != 0) {
                             try {
                                 date = CustomerSource.dateFormat.parse(valueStr);
                             } catch(ParseException pe) {
                              /* Ignore invalid dates */ 
                             }
                         }
                         ((CustomerSource) model).setDateCustModifiedBefore(date);
                     }
                 };
            	 
                 TextEditor editor = new TextEditor();
                 editor.setSize(40);
            	
            	FieldInfo fieldInfo = new FieldInfo(id,id,modifier,editor);
            	
            	context.addFieldInfo(fieldInfo);
            	
            }
            {
            	// -----------
                // Datebox customer modified after
                // -----------
            	 String id = "customerModifiedAfter";
            	 
            	 ModelModifier modifier = new DefaultModelModifier() {
                     public Object getModelValue(Object model) throws Exception {
                         Date date = ((CustomerSource) model).getDateCustModifiedAfter();
                         if(date != null)
                             return CustomerSource.dateFormat.format(date);
                         else
                             return "";
                     }

                     public void setModelValue(Object model, Object value) throws Exception {
                         String valueStr = (String) value;
                         Date date = null;
                         if(valueStr != null && valueStr.length() != 0) {
                             try {
                                 date = CustomerSource.dateFormat.parse(valueStr);
                             } catch(ParseException pe) {
                              /* Ignore invalid dates */ 
                             }
                         }
                         ((CustomerSource) model).setDateCustModifiedAfter(date);
                     }
                 };
            	 
            	TextEditor editor = new TextEditor();
            	editor.setSize(40);
            	
            	FieldInfo fieldInfo = new FieldInfo(id,id,modifier,editor);
            	
            	context.addFieldInfo(fieldInfo);
            	
            }
            {
//            	// -----------
                // Datebox admin modified before
                // -----------
            	 String id = "adminModifiedBefore";
            	 
            	 ModelModifier modifier = new DefaultModelModifier() {
                     public Object getModelValue(Object model) throws Exception {
                         Date date = ((CustomerSource) model).getDateAdminModifiedBefore();
                         if(date != null)
                             return CustomerSource.dateFormat.format(date);
                         else
                             return "";
                     }

                     public void setModelValue(Object model, Object value) throws Exception {
                         String valueStr = (String) value;
                         Date date = null;
                         if(valueStr != null && valueStr.length() != 0) {
                             try {
                                 date = CustomerSource.dateFormat.parse(valueStr);
                             } catch(ParseException pe) {
                              /* Ignore invalid dates */ 
                             }
                         }
                         ((CustomerSource) model).setDateAdminModifiedBefore(date);
                     }
                 };
            	 
                 TextEditor editor = new TextEditor();
                 editor.setSize(40);
            	
            	FieldInfo fieldInfo = new FieldInfo(id,id,modifier,editor);
            	
            	context.addFieldInfo(fieldInfo);
            	
            }
            {
//            	// -----------
                // Datebox admin modified after 
                // -----------
            	 String id = "adminModifiedAfter";
            	 
            	 ModelModifier modifier = new DefaultModelModifier() {
                     public Object getModelValue(Object model) throws Exception {
                         Date date = ((CustomerSource) model).getDateAdminModifiedAfter();
                         if(date != null)
                             return CustomerSource.dateFormat.format(date);
                         else
                             return "";
                     }

                     public void setModelValue(Object model, Object value) throws Exception {
                         String valueStr = (String) value;
                         Date date = null;
                         if(valueStr != null && valueStr.length() != 0) {
                             try {
                                 date = CustomerSource.dateFormat.parse(valueStr);
                             } catch(ParseException pe) {
                              /* Ignore invalid dates */ 
                             }
                         }
                         ((CustomerSource) model).setDateAdminModifiedAfter(date);
                     }
                 };
            	 
                 TextEditor editor = new TextEditor();
                 editor.setSize(40);
            	
            	FieldInfo fieldInfo = new FieldInfo(id,id,modifier,editor);
            	
            	context.addFieldInfo(fieldInfo);
            	
            }
            
            {
            	// -----------
                // what rule to apply (customer / admin / both)
                // -----------
            	//set unique id and description labelkey
                String id = "modifyOperation";

                ModelModifier modifier = new DefaultModelModifier() {
                    public Object getModelValue(Object model) throws FailTransferException,
                            AbortTransferException {
                    	String value = ((CustomerSource)model).getModifyOperation();
                    	
                    	if(value != null){
                    		
                    		return value;
                    	}
                    	else{
                    		value = "OR";
                    		return value;
                    	}
                    }

                    public void setModelValue(Object model, Object value)
                            throws FailTransferException, AbortTransferException {
                    	
                    	((CustomerSource)model).setModifyOperation((String)value);
                    }
                };
                
            	SelectEditor editor = new SelectEditor();
            	editor.addOption(new DefaultSelectOption("OR","OR"));
            	editor.addOption(new DefaultSelectOption("AND","AND"));
            	
            	FieldInfo fieldInfo = new FieldInfo(id,id,modifier,editor);
            	
            	context.addFieldInfo(fieldInfo);
            	
            }
            


        } catch(Exception e) {
            Environment.getInstance().log("CustomerSourceGUIContextContainer", e);
        }
    }

    public GUIContext getGUIContext() {
        return context;
    }

} // CustomerSourceGUIContextContainer
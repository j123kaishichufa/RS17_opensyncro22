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

package smilehouse.gui.html.fieldbased;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import smilehouse.gui.html.fieldbased.editor.AbstractEditor;
import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.gui.html.fieldbased.editor.WebEditor;
import smilehouse.tools.template.Template;

/**
 * A special kind of editor that can modify a Collection-type property. The Collection objects are
 * treated as rows and each row has columns, which contain fields for modifying the Collection
 * object's properties.
 */
public class ContainerEditor implements WebEditor {
    //  template read in the constructor
    protected Template baseTemplate;

    //  true if delete button is enabled
    protected boolean delete;
    //  the labelkey used to get the text for the delete button
    protected String deleteLabel;

    protected boolean clone;
    protected String cloneLabel;
    
    
    protected boolean add;//true if add button is enabled
    protected String addLabel;//the labelkey used to get the text for the add button
    protected String confirmLabel; //the labelkey used to get the text shown for confirming the delete
    
    
    //child class given when enabling add-button. new objects are created
    //with childClass.newInstance()
    private Class childClass;

    //  true if move button's is enabled (used to change the order of the
    // objects)
    protected boolean move;
    protected String moveLabel; //the labelkey used to get the text for the buttons column's header
    protected String upImgSrc; //src-attributes value for the up-button's image
    protected String upImgAltLabel; //the labelkey used to get the alt-text for the up button
    protected String doImgSrc; //src-attributes value for the down-button's image
    protected String doImgAltLabel; //the labelkey used to get the alt-text for the down button

    

    /**
     * Creates a containereditor with the default settings.
     */
    public ContainerEditor() {
        this.baseTemplate = Template.load(
            ContainerEditor.class
                .getResourceAsStream("editor/defaulttemplates/containereditor.html"),
            AbstractEditor.DEFAULT_TEMPLATE_ENCODING);
        this.delete = false;
        this.deleteLabel = null;
        this.add = false;
        this.addLabel = null;
        this.move = false;
        this.clone=false;
        this.cloneLabel=null;
    }

    /**
     * Creates a containereditor with the given template.
     */
    public ContainerEditor(Template template) {
        this.baseTemplate = template;
        this.delete = false;
        this.add = false;
        this.move = false;
        this.clone=false;
    }


    public ContainerEditor enableDelete(String deleteLabel, String confirmLabel) {
        this.delete = true;
        this.deleteLabel = deleteLabel;
        this.confirmLabel = confirmLabel;
        return this;
    }
    
   /***********************************************************************************************
     * Enables cloning of items in the collection. A clone button will be added for each row item in 
     * the editor.
     * 
     * @param classToClone The class of the object you want to clone. The class must implement Cloneable.
     * @param cloneLabel The labelkey used for the clone button
     * @return Returns itself
     * @exception CloneNotSupportedException - if classToClone does not implement Cloneable interface
     */

    public ContainerEditor enableClone(Class classToClone, String cloneLabel) throws CloneNotSupportedException {
    	
    	//Does the class we want to clone implement Cloneable interface?
   		if(Cloneable.class.isAssignableFrom(classToClone)){
   			this.clone=true;
   	    	this.cloneLabel=cloneLabel;
   	    	return this;
    		}
    	
    	throw new CloneNotSupportedException("Class does not implement Cloneable interface");
    }

    /**
     * Enables deletion of childs from the collection. A delete button is added as the last column
     * of the row. NOTE: the iterator of the modifier collection property must support the
     * remove-method.
     * 
     * @param deleteLabel The labelkey used to retrieve the text for the delete button.
     * @return Returns itself
     */
    public ContainerEditor enableDelete(String deleteLabel) {
        return enableDelete(deleteLabel, null);
    }

    /***********************************************************************************************
     * Enables adding of childs to the collection. An add button will be added to the editor as the
     * last row. NOTE: the given child class must have a constructor without parameters.
     * 
     * @param childClass The item object class in the collection. For example, you might have a
     *        collection of String-objects, so you would call this method with
     *        enableAdd(String.class, "addLabelKey").
     * @param addLabel The labelkey used for the add-button.
     * @return Returns itself
     * @exception IllegalAccessException - if the class or its nullary constructor is not
     *            accessible.
     * @exception InstantiationException - if this Class represents an abstract class, an interface,
     *            an array class, a primitive type, or void; or if the class has no nullary
     *            constructor; or if the instantiation fails for some other reason.
     */
    public ContainerEditor enableAdd(Class childClass, String addLabel) throws InstantiationException,
            IllegalAccessException {
        //check that we can create new childs
        childClass.newInstance();
        //precondition passed
        this.add = true;
        this.childClass = childClass;
        this.addLabel = addLabel;
        return this;

    }

    /**
     * Enable changing the order of the collection's items. To support this the Collection must
     * implement the java.util.List-interface.
     * 
     * @param moveLabel the labelkey used to get the text for the buttons column's header
     * @param upImgSrc src-attributes value for the up-button's image
     * @param upImgAltLabel the labelkey used to get the alt-text for the up button
     * @param doImgSrc src-attributes value for the down-button's image
     * @param doImgAltLabel the labelkey used to get the alt-text for the down button
     * @return Returns itself
     */
    public ContainerEditor enableMove(String moveLabel,
                           String upImgSrc,
                           String upImgAltLabel,
                           String doImgSrc,
                           String doImgAltLabel) {
        this.move = true;
        this.moveLabel = moveLabel;
        this.upImgSrc = upImgSrc;
        this.upImgAltLabel = upImgAltLabel;
        this.doImgSrc = doImgSrc;
        this.doImgAltLabel = doImgAltLabel;
        return this;
    }




    /***********************************************************************************************
     * 
     * @return the html-representation of this editor.
     *  
     */
    public String getEditor(EditorResources editorResources) {
        Template template = baseTemplate.copyStructure();
        template.setVariable("description", editorResources.getDescription());


        Collection columnFields = getColumnFields(editorResources);
        int cols = columnFields.size();
        if(delete)
            cols++;
        if(move)
            cols++;
        if(clone)
        	cols++;
        template.setVariable("cols", "" + cols);

        //write headers
        Template header = template.getBlock("header");
        Template headerColumn = header.getBlock("column");
        for(Iterator iter = columnFields.iterator(); iter.hasNext();) {
            FieldImpl column = (FieldImpl) iter.next();
            headerColumn.setVariable("description", column.getDescription());
            headerColumn.write();
        }
        if(move) {
            headerColumn.setVariable("description", editorResources.getResource().getLabel(
                this.moveLabel));
            headerColumn.write();
        }
        if(delete) {
            headerColumn.setVariable("description", editorResources.getResource().getLabel(
                this.deleteLabel));
            headerColumn.write();
        }
        if(clone) {
        	headerColumn.setVariable("description", editorResources.getResource().getLabel(
                    this.cloneLabel));
            headerColumn.write();
        }
        header.write();




        //write field editors
        ContainingFieldIterator allFields = getAllFields(editorResources);
        int rowNum = 0;
        String rowClass = "ttrow1";
        Template row = template.getBlock("row");
        Template rowColumn = row.getBlock("column");
        while(allFields.hasNext()) {
            FieldImpl column = (FieldImpl) allFields.next();

            // set css zeebra-class
            rowColumn.setVariable("rowClass", rowClass);

            //set editor
            rowColumn.setVariable("field", column.getEditor());
            rowColumn.write();
            //System.err.println("column id; "+column.getId());
            //  		System.err.println("column readId: "+column.getReadId());
            //  		System.err.println("column writeId: "+column.getWriteId());
            //  		System.err.println("");

            
            //do row change
            if(allFields.lastColumnInRow()) {
            	
                if(move) {
                    rowColumn.setVariable("rowClass", rowClass);
                    Template buttonB = rowColumn.getBlock("button");
                    if(rowNum != 0) {
                        buttonB.setVariable("id", editorResources.getWriteId() + "_up_" + rowNum);
                        buttonB.setVariable("src", this.upImgSrc);
                        buttonB.setVariable("description", ""
                                + editorResources.getResource().getLabel(this.upImgAltLabel));
                        buttonB.write();
                    }
                    if(!allFields.lastRow()) {
                        buttonB.setVariable("id", editorResources.getWriteId() + "_down_" + rowNum);
                        buttonB.setVariable("src", this.doImgSrc);
                        buttonB.setVariable("description", ""
                                + editorResources.getResource().getLabel(this.doImgAltLabel));
                        buttonB.write();
                    }
                    rowColumn.write();
                }
                if(delete) {
                    //write delete button: TODO: support for changing the buttons...
                    rowColumn.setVariable("rowClass", rowClass);
                    Template buttonB = rowColumn.getBlock("deletebutton");
                    buttonB.setVariable("id", editorResources.getWriteId() + "_delete_" + rowNum);
                    buttonB.setVariable("description", ""
                            + editorResources.getResource().getLabel(this.deleteLabel));
                    // Add a confirmation popup too if there's a label for it.
                    if(confirmLabel != null) {
                        Template confirmB = buttonB.getBlock("deleteconfirm");
                        confirmB.setVariable("confirmMessage", ""
                                + editorResources.getResource().getLabel(this.confirmLabel));
                        confirmB.write();
                    }

                    buttonB.write();
                    rowColumn.write();
                }
                if(clone){
                	rowColumn.setVariable("rowClass", rowClass);
                    Template buttonB = rowColumn.getBlock("deletebutton");
                    buttonB.setVariable("id", editorResources.getWriteId() + "_clone_" + rowNum);
                    buttonB.setVariable("description", ""
                            + editorResources.getResource().getLabel(this.cloneLabel));

                    buttonB.write();
                    rowColumn.write();
                }
                
                row.write();
                rowNum++;
                //set zeebra, note that we are counting from zero
                
                if(rowNum % 2 == 0)
                    rowClass = "ttrow1";
                else
                    rowClass = "ttrow2";
                
                
                
                
                
            }
        }
        //the last row must be written too
        row.write();

        //write possible add-button too
        if(add) {
            Template addB = template.getBlock("add");
            addB.setVariable("cols", "" + cols);
            addB.setVariable("id", editorResources.getWriteId() + "Add");
            addB.setVariable("description", editorResources.getResource().getLabel(this.addLabel));
            addB.write();
        }




        //AbstractEditor.writeEditorErrors(template, editorResources);

        template.write();
        return template.toString();
    }

    /**
     * @return true if any of the buttons have been pressed or any of the containing fields has been
     *         edited.
     */
    public boolean hasBeenEdited(EditorResources editorResources) {
        boolean edited = false;

        //check if add button pressed
        if(editorResources.getRequest().getParameter(editorResources.getReadId() + "Add") != null)
            return true;
        String a=editorResources.getReadId();
        String s=editorResources.getRequest().getParameter(a + "Clone");
         if(s != null)
            return true;

        //check if any of the containing fields has been edited
        ContainingFieldIterator allFields = getAllFields(editorResources);
        int rowNum = 0;
        while(allFields.hasNext()) {
            Field oneField = (Field) allFields.next();
            if(oneField.hasBeenEdited())
                return true;
            if(allFields.lastColumnInRow()) {
                //check if arrowup button pressed
                if(editorResources.getRequest().getParameter(
                    editorResources.getReadId() + "_up_" + rowNum + ".x") != null)
                    return true;
                //check if arrowdown button pressed
                if(editorResources.getRequest().getParameter(
                    editorResources.getReadId() + "_down_" + rowNum + ".x") != null)
                    return true;
                //check if delete button pressed
                if(editorResources.getRequest().getParameter(
                    editorResources.getReadId() + "_delete_" + rowNum) != null)
                    return true;

                rowNum++;
            }
        }
        return edited;

    }

    /**
     * Commits all valid edit's in this editor. Then modifies the collection if any of the buttons
     * were pressed (add,remove,move).
     */
    public boolean commit(EditorResources editorResources) {
        //first we try to commit all field edits
        ContainingFieldIterator allFields = getAllFields(editorResources);
        boolean someCommitted = false;
        while(allFields.hasNext()) {
            Field oneField = (Field) allFields.next();
            if(oneField.hasBeenEdited() && oneField.isEditValid()) {
                oneField.commit();
                someCommitted = true;
            }
        }

        //now we can try to modify the collection (if buttons are pressed)
        Collection col = (Collection) editorResources.getModelValue();

        //if add button pressed
        if(add
                && editorResources.getRequest().getParameter(editorResources.getReadId() + "Add") != null) {
            Object child = null;

            try {
                child = this.childClass.newInstance();
            } catch(Exception e) {
                /*
                 * this was checked when the add was enabled, let's still make sure that this
                 * doesn't silently fail.
                 */
                throw new RuntimeException(
                    "couldn't create a new child. The enableAdd should check this in configuration.");
            }
            List list = (List) col;
            list.add(child);

            ContainerFieldImpl cont = (ContainerFieldImpl) editorResources;
            List rows = cont.getRows();
            rows.add(new ContainerRow(rows.size(), child));

        }



        for(int i = 0; i < col.size(); i++) {
            //check if arrowup button pressed
            if(move
                    && editorResources.getRequest().getParameter(
                        editorResources.getReadId() + "_up_" + i + ".x") != null) {
                List list = (List) col;
                list.add(i - 1, list.remove(i));
                //update rows
                ContainerFieldImpl cont = (ContainerFieldImpl) editorResources;
                List rows = cont.getRows();
                rows.add(i - 1, rows.remove(i));

            }
            //check if arrowdown button pressed
            if(move
                    && editorResources.getRequest().getParameter(
                        editorResources.getReadId() + "_down_" + i + ".x") != null) {
                List list = (List) col;
                list.add(i + 1, list.remove(i));
                //update rows
                ContainerFieldImpl cont = (ContainerFieldImpl) editorResources;
                List rows = cont.getRows();
                rows.add(i + 1, rows.remove(i));
            }
            //check if delete button pressed
            if(delete
                    && editorResources.getRequest().getParameter(
                        editorResources.getReadId() + "_delete_" + i) != null) {
                Iterator it = col.iterator();
                int count = 0;
                while(it.hasNext() && count < i) {
                    it.next();
                    count++;
                }
                if(it.hasNext()) {
                    Object r = it.next();
                    col.remove(r);
                }
                //update rows
                ContainerFieldImpl cont = (ContainerFieldImpl) editorResources;
                List rows = cont.getRows();
                rows.remove(i);

            }
//          check if clone button was pressed
            if(clone && editorResources.getRequest().getParameter(
                        editorResources.getReadId() + "_clone_" + i) != null) {
                Iterator it = col.iterator();
                List list = (List) col;
                Object r=null;
                int count = 0;
                
                while(it.hasNext() && count < i) {
                    it.next();
                    count++;
                }
                Object newObject=null;
                if(it.hasNext()) {
                    r = it.next();
					try{
						Method objClone=r.getClass().getMethod("clone", (Class []) null);
						newObject=objClone.invoke(r, (Object[]) null);
					}
					catch(Exception e){
						//The object is supposed to have a clone method
						throw new RuntimeException(
	                    "The class doesn't have a clone method");
					}
                    list.add(i + 1, newObject);
                    
                }
                //update rows
                ContainerFieldImpl cont = (ContainerFieldImpl) editorResources;
                List rows = cont.getRows();
				rows.add(new ContainerRow(rows.size(), newObject));

            }

        }

        return someCommitted;

    }

    /** @return true if all containing fields are valid. */
    public boolean isEditValid(EditorResources editorResources) {
        boolean valid = true;
        //check if any of the containing fields are invalid
        Iterator allFields = getAllFields(editorResources);
        while(allFields.hasNext()) {
            Field oneField = (Field) allFields.next();
            if(oneField.hasBeenEdited() && !oneField.isEditValid())
                return false;
        }
        return valid;
    }




    /**
     * methods that have to be in a webeditor, but are not really usefull with a containereditor.
     * ContainerEditor should be used with the ContainerFieldInfo, which knows not to use these
     * methods. So these methods are called only if the editor is misused.
     */
    public String validate(EditorResources editorResources) {
        throw new IllegalStateException("container_editor_should_be_used_with_containerfieldinfo");
    }

    /**
     * methods that have to be in a webeditor, but are not really usefull with a containereditor.
     * ContainerEditor should be used with the ContainerFieldInfo, which knows not to use these
     * methods. So these methods are called only if the editor is misused.
     */
    public boolean hasEditValue(EditorResources editorResources) {
        throw new IllegalStateException("container_editor_should_be_used_with_containerfieldinfo");
    }

    /**
     * methods that have to be in a webeditor, but are not really usefull with a containereditor.
     * ContainerEditor should be used with the ContainerFieldInfo, which knows not to use these
     * methods. So these methods are called only if the editor is misused.
     */
    public Object getEditValue(EditorResources editorResources) {
        throw new IllegalStateException("container_editor_should_be_used_with_containerfieldinfo");
    }


    /**
     * Private helper that creates the column fields from the column fieldinfo's
     */
    protected Collection getColumnFields(EditorResources editorResources) {
        ContainerFieldImpl containerField = (ContainerFieldImpl) editorResources;
        Collection columnFields = containerField.getColumnFields();
        return columnFields;
    }

    /**
     * Private helper that is used to iterate trough the editor. This includes all rows and each
     * column in every row.
     */
    protected ContainingFieldIterator getAllFields(EditorResources editorResources) {
        ContainerFieldImpl cont = (ContainerFieldImpl) editorResources;
        return new ContainingFieldIterator(cont);
    }


    /**
     * Private helper class that is used to iterate trough the editor. This includes all rows and
     * each column in every row.
     */
    class ContainingFieldIterator implements Iterator {
        ContainerFieldImpl field;

        int rowNum;
        int colNum;
        Iterator rows;
        Iterator cols;
        ContainerRow currentRow;

        public ContainingFieldIterator(ContainerFieldImpl field) {
            if(field==null)
                throw new NullPointerException("field cannot be null");
            
            this.field = field;
            this.rowNum = 0;
            this.colNum = 0;

            this.rows = field.getRows().iterator();
            this.cols = field.getColumnFields().iterator();
            if(rows.hasNext())
                this.currentRow = (ContainerRow) rows.next();
        }

        public boolean lastColumnInRow() {
            return (!cols.hasNext());
        }

        public boolean lastRow() {
            return (!rows.hasNext());
        }

        public boolean hasNext() {
            if(cols.hasNext() && this.currentRow != null)
                return true;
            if(rows.hasNext())
                return true;
            //no more cols or rows
            return false;
        }

        public Object next() {
            //do row change
            if(!cols.hasNext()) {
                this.currentRow = (ContainerRow) rows.next();//model changes
                rowNum++; //don't forget this,
                this.cols = field.getColumnFields().iterator();//go again through the columns for
                // the new row
                colNum = 0; //or this
            }
            //get the field
            ContainableField column = (ContainableField) cols.next();

            //finally set all the fancy stuff, which is why this iterator was built in the first
            // place
            column.setModel(currentRow.getBean());
            column.setWriteId(field.getWriteId() + "_row" + rowNum + "_col" + colNum);//"_"+columnResources.getId()+
            column.setReadId(field.getReadId() + "_row" + currentRow.getReadIndex() + "_col"
                    + colNum);//+"_"+columnResources.getId()

            colNum++;
            return column;
        }

        public void remove() {
            throw new RuntimeException("not implemented.");
        }
    }




}



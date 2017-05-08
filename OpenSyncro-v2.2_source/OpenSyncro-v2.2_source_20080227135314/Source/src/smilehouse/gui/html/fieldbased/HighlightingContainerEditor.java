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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import smilehouse.gui.html.fieldbased.editor.AbstractEditor;
import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.tools.template.Template;
import smilehouse.util.RecallingList;

/**
 * Extension of ContainerEditor for active pipe highlighting
 */
public class HighlightingContainerEditor extends ContainerEditor {
	 private Set pipeSet;
	 
	 /**
     * @return The html-representation of the editor.
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

	        Object o=editorResources.getModelValue();
	        RecallingList pipeList=null;
	        if(o instanceof RecallingList){
	        	pipeList= (RecallingList) editorResources.getModelValue();
	        }

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
	            if (this.pipeSet != null && pipeList != null) {
					Pipe p = ( Pipe ) pipeList.get( rowNum );
					if (this.pipeSet.contains( p ))
						rowClass = "ttrow3";
	        	}
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
	    /**Make executing pipes known to the editor. 
		 * Needed for highlighting running pipes in pipe list
		 * @param pipeSet Set of active pipes
		 */
		public void setActivePipes(Set pipeSet) {
			this.pipeSet=pipeSet;

		}
}

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
 * Created on 28.1.2005
 */
package smilehouse.tools.ui.web;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.LI;
import org.apache.ecs.html.UL;

public class ServletMessageBox {

    private String errorCssId;
    private String noticeCssId;
    private String errorListAttribute;
    private String noticeListAttribute;
    
    public ServletMessageBox() {
        this.errorCssId = "error";
        this.noticeCssId = "notice";
        this.errorListAttribute = "ServletMessageBox_errors";
        this.noticeListAttribute = "ServletMessageBox_notices";
    }
    
    /**
     * Set the CSS clas that the error-box should use.
     * 
     * @param errorCssClass
     */
    public void setErrorCssId(String errorCssClass) {
        this.errorCssId = errorCssClass;
    }
    
    /**
     * Set the CSS clas that the notice-box should use.
     * 
     * @param noticeCssClass
     */
    public void setNoticeCssId(String noticeCssClass) {
        this.noticeCssId = noticeCssClass;
    }
    

    /**
     * Set the name of the attribute used for storing list of errors in the request-object.
     * The default is <tt>ServletMessageBox_errors</tt>.
     * 
     * @param errorListAttribute
     */
    public void setErrorListAttribute(String errorListAttribute) {
        this.errorListAttribute = errorListAttribute;
    }
    
    /**
     * Set the name of the attribute used for storing list of notices in the request-object.
     * The default is <tt>ServletMessageBox_notices</tt>.
     *
     * @param noticeListAttribute
     */
    public void setNoticeListAttribute(String noticeListAttribute) {
        this.noticeListAttribute = noticeListAttribute;
    }
    
    /**
     * Add error message to this request.
     * 
     * @param errorMessage Error message
     * @param req Current request
     */
    public void addError(String errorMessage, HttpServletRequest req) {
        List errors = (List) req.getAttribute(errorListAttribute);
        if(errors == null) {
            errors = new LinkedList();
            req.setAttribute(errorListAttribute, errors);
        }
        errors.add(errorMessage);
    }
    
    /**
     * Add notice to this request.
     * 
     * @param notice Notice
     * @param req Current request
     */
    public void addNotice(String notice, HttpServletRequest req) {
        List notices = (List) req.getAttribute(noticeListAttribute);
        if(notices == null) {
            notices = new LinkedList();
            req.setAttribute(noticeListAttribute, notices);
        }
        notices.add(notice);
        
    }
    
    /**
     * Includes error messages and notices in their own boxes to the start of the content (if there is any).
     * 
     * @param content Page content
     * @param req Current request
     * 
     * @return Page content with messages (if any)
     */
    public ConcreteElement includeMessageBoxes(ConcreteElement content, HttpServletRequest req) {
        List errors = (List) req.getAttribute(errorListAttribute);
        List notices = (List) req.getAttribute(noticeListAttribute);
        if( (errors == null || errors.size() == 0) && (notices == null || notices.size() == 0) ) {
            // Nothing to do...
            return content;
        }
        else {
            ElementContainer newContent = new ElementContainer().addElement(new BR());
            
            if(errors != null && errors.size() > 0) {
                UL errorList = new UL();
                for(Iterator it=errors.iterator(); it.hasNext();) {
                    errorList.addElement(new LI( (String) it.next() ));
                }
                Div errorDiv = new Div().addElement(errorList);
                errorDiv.setID(errorCssId);
                
                newContent.addElement(errorDiv);
            }
            if(notices != null && notices.size() > 0) {
                UL noticeList = new UL();
                for(Iterator it=notices.iterator(); it.hasNext();) {
                    noticeList.addElement(new LI( (String) it.next() ));
                }
                Div noticeDiv = new Div().addElement(noticeList);
                noticeDiv.setID(noticeCssId);
                
                newContent.addElement(noticeDiv);
            }
            
            newContent.addElement(content);
            return newContent;
        }
    }
}
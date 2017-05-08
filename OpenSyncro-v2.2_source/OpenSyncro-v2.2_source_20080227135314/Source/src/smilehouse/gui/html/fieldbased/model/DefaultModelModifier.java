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

package smilehouse.gui.html.fieldbased.model;

/**
 * This class defines a base class for modifying a value. It doesn't work by itself, but it should
 * be extended to provide a real ModelModifier. The extension should override the methods
 * getModelValue and setModelValue. This class acts as a wrapper around these methods. The wrapper
 * transforms the exceptions thrown by the extending class to LocalizedExceptions.
 */
public class DefaultModelModifier implements ModelModifier {

    /** Default constructor */
    public DefaultModelModifier() {}


    /**
     * Method that wraps the implementations exceptions to a LocalizedException.
     */
    public void setValue(Object model, Object value){
        try {

            setModelValue(model, value);
        } catch(Exception any) {
            throw new RuntimeException("Cannot set value to object", any);
        }
    }

    /**
     * Method that wraps the implementations exceptions to a LocalizedException.
     */
    public Object getValue(Object model) {
        try {

            return getModelValue(model);

        } catch(Exception any) {
            throw new RuntimeException("Cannot read value from object", any);
        }
    }

    /**
     * Replace this by extending the class!
     * 
     * @param model Object from which to get the value. The replacing method should do a cast to
     *        convert the given model to the type known by the subclass and then return the value
     *        from the model.
     */
    public Object getModelValue(Object model) throws Exception {
        throw new RuntimeException(
            "DefaultModelModifier should be extended to provide a real method here. Please override this method be extending this class. An easy way to do this is by an anonymous inner class.");
    }

    /**
     * Replace this by extending the class! The replacing method should do a cast to convert the
     * given model and the value to the types known by the subclass and then set the given value to
     * the given model.
     * 
     * @param model Object to set the value
     * @param value The value to set to the Object. In the GUI this is the value got from the
     *        WebEditor with the method getEditValue. The returned type depends on the Editor's
     *        Formatter. If there is no Formatter in the editor, the return type of getEditValue is
     *        a string.
     *  
     */
    public void setModelValue(Object model, Object value) throws Exception {
        throw new RuntimeException(
            "DefaultModelModifier should be extended to provide a real method here. Please override this method be extending this class. An easy way to do this is by an anonymous inner class.");
    }
}
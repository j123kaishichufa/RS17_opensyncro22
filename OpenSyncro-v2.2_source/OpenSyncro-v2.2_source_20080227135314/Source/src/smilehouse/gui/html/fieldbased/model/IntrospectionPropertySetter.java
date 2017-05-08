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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import smilehouse.gui.html.fieldbased.GUIException;

/**
 * Introspection-based PropertySetter for testing.
 * 
 * @see PropertySetter for information about PropertySetters.
 */
public class IntrospectionPropertySetter implements PropertySetter {

    private Class beanClass;
    private Map properties;

    /**
     * Basic constructor. Uses Introspection on the given class to create the propertySetter.
     * 
     * @param beanClass the beanClass to modify with this propertysetter
     */
    public IntrospectionPropertySetter(Class beanClass) throws IntrospectionException {
        this.beanClass = beanClass;
        this.properties = new HashMap();

        BeanInfo info = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        if(descriptors != null) {
            for(int i = 0; i < descriptors.length; i++) {
                PropertyDescriptor t = descriptors[i];
                properties.put(t.getName(), t);
            }
        }
    }

    /** @see PropertySetter */
    public void setValue(Object model, String property, Object value) throws GUIException {
        //System.out.println("setting property "+property+" to value "+value);
        PropertyDescriptor descript = getDescriptor(property);
        checkModel(model, property);

        Method write = descript.getWriteMethod();
        if(write == null)
            throw new GUIException("propertysetter_cannot_write_property");

        Object[] args = new Object[1];
        args[0] = value;
        
        //System.out.println("calling runMethod("+write+","+model+","+args+")");
        runMethod(
            write,
            model,
            args,
            "propertysetter_notallowedto_write_property",
            "property_not_accepted_by_model");
    }

    /** @see PropertySetter */
    public Object getValue(Object model, String property) throws GUIException {
        PropertyDescriptor descript = getDescriptor(property);
        checkModel(model, property);

        Method read = descript.getReadMethod();
        if(read == null)
            throw new GUIException("propertysetter_cannot_read_property");

        return runMethod(
            read,
            model,
            null,
            "propertysetter_notallowedto_read_property",
            "property_not_available_in_model");
    }

    /** @see PropertySetter */
    public Class getType(String property)  throws GUIException {
        PropertyDescriptor descript = getDescriptor(property);
        return descript.getPropertyType();
    }

    /** @see PropertySetter */
    public Class getType() {
        return this.beanClass;
    }


    /** Helper method to get the Descriptor and to the required classcast. */
    private PropertyDescriptor getDescriptor(String property) throws GUIException {
        PropertyDescriptor descript = (PropertyDescriptor) properties.get(property);
        if(descript == null)
            throw new GUIException("property '"+property+"'_not_found_in_propertysetter");
        return descript;
    }

    /**
     * Helper method check that the given model isn't null and is the same type of bean that this
     * propertysetter can modify..
     */
    private void checkModel(Object model, String property) throws GUIException {
        if(model == null)
            throw new GUIException("field_model_for_propertysetter_cannot_be_null");
        if(model.getClass() != beanClass)
            throw new GUIException("Wrong model type on property '"+property+"', expected '"+beanClass+"' got '"+model.getClass()+"'.");
    }

    /**
     * Helper method for actually invoking the bean methods.
     */
    private Object runMethod(Method method,
                             Object model,
                             Object[] args,
                             String illegalAccessErrorCode,
                             String invocationErrorCode) throws GUIException {

        try {
            return method.invoke(model, args);
        } catch(IllegalAccessException iae) {
            GUIException e = new GUIException(illegalAccessErrorCode);
            e.initCause(iae);
            throw e;
        } catch(InvocationTargetException ite) {
            throw new GUIException("cannot run method", ite);
        } catch(IllegalArgumentException iae) {
            throw new GUIException("cannot set '"+method.getName()+"' with type '"+args[0]+"'",iae);
        }
    }
}


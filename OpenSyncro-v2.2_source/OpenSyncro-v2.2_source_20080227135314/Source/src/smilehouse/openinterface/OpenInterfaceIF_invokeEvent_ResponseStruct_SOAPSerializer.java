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

// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1, build R59)

package smilehouse.openinterface;

import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.literal.DetailFragmentDeserializer;
import com.sun.xml.rpc.encoding.simpletype.*;
import com.sun.xml.rpc.encoding.soap.SOAPConstants;
import com.sun.xml.rpc.encoding.soap.SOAP12Constants;
import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.namespace.QName;

public class OpenInterfaceIF_invokeEvent_ResponseStruct_SOAPSerializer extends ObjectSerializerBase implements Initializable {
    
    public OpenInterfaceIF_invokeEvent_ResponseStruct_SOAPSerializer(QName type, boolean encodeType, boolean isNullable, String encodingStyle) {
        super(type, encodeType, isNullable, encodingStyle);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
    }
    
    public Object doDeserialize(SOAPDeserializationState state, XMLReader reader,
        SOAPDeserializationContext context) throws Exception {
        smilehouse.openinterface.OpenInterfaceIF_invokeEvent_ResponseStruct instance = new smilehouse.openinterface.OpenInterfaceIF_invokeEvent_ResponseStruct();
        Object member;
        boolean isComplete = true;
        QName elementName;
        
        reader.nextElementContent();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (isComplete ? (Object)instance : (Object)state);
    }
    
    public void doSerializeAttributes(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        smilehouse.openinterface.OpenInterfaceIF_invokeEvent_ResponseStruct instance = (smilehouse.openinterface.OpenInterfaceIF_invokeEvent_ResponseStruct)obj;
        
    }
    
    public void doSerializeInstance(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        smilehouse.openinterface.OpenInterfaceIF_invokeEvent_ResponseStruct instance = (smilehouse.openinterface.OpenInterfaceIF_invokeEvent_ResponseStruct)obj;
        
    }
    protected void verifyName(XMLReader reader, QName expectedName) throws Exception {
    }
}
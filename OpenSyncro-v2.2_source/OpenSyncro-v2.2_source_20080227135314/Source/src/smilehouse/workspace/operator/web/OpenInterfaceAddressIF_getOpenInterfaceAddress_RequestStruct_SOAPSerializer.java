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

package smilehouse.workspace.operator.web;

import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.literal.DetailFragmentDeserializer;
import com.sun.xml.rpc.encoding.simpletype.*;
import com.sun.xml.rpc.encoding.soap.SOAPConstants;
import com.sun.xml.rpc.encoding.soap.SOAP12Constants;
import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.namespace.QName;

public class OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct_SOAPSerializer extends ObjectSerializerBase implements Initializable {
    private static final QName ns1_String_1_QNAME = new QName("", "String_1");
    private static final QName ns2_string_TYPE_QNAME = SchemaConstants.QNAME_TYPE_STRING;
    private CombinedSerializer ns2_myns2_string__java_lang_String_String_Serializer;
    private static final int mySTRING_1_INDEX = 0;
    
    public OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct_SOAPSerializer(QName type, boolean encodeType, boolean isNullable, String encodingStyle) {
        super(type, encodeType, isNullable, encodingStyle);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns2_myns2_string__java_lang_String_String_Serializer = (CombinedSerializer)registry.getSerializer(SOAPConstants.NS_SOAP_ENCODING, java.lang.String.class, ns2_string_TYPE_QNAME);
    }
    
    public Object doDeserialize(SOAPDeserializationState state, XMLReader reader,
        SOAPDeserializationContext context) throws Exception {
        smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct instance = new smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct();
        smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct_SOAPBuilder builder = null;
        Object member;
        boolean isComplete = true;
        QName elementName;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_String_1_QNAME)) {
                member = ns2_myns2_string__java_lang_String_String_Serializer.deserialize(ns1_String_1_QNAME, reader, context);
                if (member instanceof SOAPDeserializationState) {
                    if (builder == null) {
                        builder = new smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct_SOAPBuilder();
                    }
                    state = registerWithMemberState(instance, state, member, mySTRING_1_INDEX, builder);
                    isComplete = false;
                } else {
                    instance.setString_1((java.lang.String)member);
                }
                reader.nextElementContent();
            } else {
                throw new DeserializationException("soap.unexpectedElementName", new Object[] {ns1_String_1_QNAME, elementName});
            }
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (isComplete ? (Object)instance : (Object)state);
    }
    
    public void doSerializeAttributes(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct instance = (smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct)obj;
        
    }
    
    public void doSerializeInstance(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct instance = (smilehouse.workspace.operator.web.OpenInterfaceAddressIF_getOpenInterfaceAddress_RequestStruct)obj;
        
        ns2_myns2_string__java_lang_String_String_Serializer.serialize(instance.getString_1(), ns1_String_1_QNAME, null, writer, context);
    }
}
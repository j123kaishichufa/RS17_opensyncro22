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


public class OpenInterfaceIF_exportOrders2_ResponseStruct {
    protected smilehouse.openinterface.ExportResult result;
    
    public OpenInterfaceIF_exportOrders2_ResponseStruct() {
    }
    
    public OpenInterfaceIF_exportOrders2_ResponseStruct(smilehouse.openinterface.ExportResult result) {
        this.result = result;
    }
    
    public smilehouse.openinterface.ExportResult getResult() {
        return result;
    }
    
    public void setResult(smilehouse.openinterface.ExportResult result) {
        this.result = result;
    }
}

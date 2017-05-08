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

package smilehouse.opensyncro.defaultcomponents.http;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Dummy class to be used with the class 'EasySSLProtocolSocketFactory' (belongs
 * to the Apache HttpClient contrib-library), to allow HTTPS to accept
 * self-signed certificates also. As can be seen, this class does not do
 * anything. That's the point. Usually the methods would make decisions about
 * which certificates to trust.
 * 
 * As the javadoc of the implemented interface states: "Instance of this
 * interface manage which X509 certificates may be used to authenticate the
 * remote side of a secure socket. Decisions may be based on trusted certificate
 * authorities, certificate revocation lists, online status checking or other
 * means."
 * 
 * 
 */
public class AcceptSelfSignedCertificatesX509TrustManager implements X509TrustManager {

	public AcceptSelfSignedCertificatesX509TrustManager(KeyStore keystore) {
		
	}
	
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// TODO Auto-generated method stub	
		
	}

	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}

}

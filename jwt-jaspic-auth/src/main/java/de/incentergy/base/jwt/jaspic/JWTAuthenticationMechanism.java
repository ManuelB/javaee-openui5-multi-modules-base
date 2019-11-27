/*
 * Copyright (c) 2017 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package de.incentergy.base.jwt.jaspic;

import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@RequestScoped
public class JWTAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger log = Logger.getLogger(JWTAuthenticationMechanism.class.getName());

    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    public static final String BEARER = "Bearer ";

	private PublicKey secretKey;
    
	public JWTAuthenticationMechanism() {
		
	}
	
	@PostConstruct
	public void init() {
		secretKey = getCertificateFromInternalKeystore().getPublicKey();
		log.log(Level.INFO, "Secrect Key for JWT validation: {0}", secretKey);
	}
	
    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext context) {

        log.log(Level.FINE, "validateRequest: {0}", request.getRequestURI());
        // Get the (caller) name and password from the request
        // NOTE: This is for the smallest possible example only. In practice
        // putting the password in a request query parameter is highly insecure
        String token = extractToken(context);

       if (token != null) {
            // validation of the jwt credential
            return validateToken(token, context);
        } else if (context.isProtected()) {
            // A protected resource is a resource for which a constraint has been defined.
            // if there are no credentials and the resource is protected, we response with unauthorized status
            return context.responseUnauthorized();
        }
        // there are no credentials AND the resource is not protected, 
        // SO Instructs the container to "do nothing"
        return context.doNothing();
    }

    /**
     * To validate the JWT token e.g Signature check, JWT claims
     * check(expiration) etc
     *
     * @param token The JWT access tokens
     * @param context
     * @return the AuthenticationStatus to notify the container
     */
    private AuthenticationStatus validateToken(String token, HttpMessageContext context) {
        try {
            if (validateToken(token)) {
                JWTCredential credential = getCredential(token);
                return context.notifyContainerAboutLogin(credential.getPrincipal(), credential.getGroups());
            }
            // if token invalid, response with unauthorized status
            return context.responseUnauthorized();
        } catch (ExpiredJwtException eje) {
            log.log(Level.WARNING, "Security exception for user {0} - {1}", new String[]{eje.getClaims().getSubject(), eje.getMessage()});
            return context.responseUnauthorized();
        }
    }
    
    @SuppressWarnings("unchecked")
	public JWTCredential getCredential(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        Set<String> groups = new HashSet<String>();
        
        try {
        	groups
                = ((List<String>)claims.get("groups"))
                        .stream()
                        .collect(Collectors.toSet());
        } catch(Exception e) {
        	log.log(Level.WARNING, "Could not get groups from JWT token: "+token, e);
        }

        return new JWTCredential(claims.getSubject(), groups);
    }

    /**
     * To extract the JWT from Authorization HTTP header
     *
     * @param context
     * @return The JWT access tokens
     */
    private String extractToken(HttpMessageContext context) {
        String authorizationHeader = context.getRequest().getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String token = authorizationHeader.substring(BEARER.length(), authorizationHeader.length());
            return token;
        }
        return null;
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.log(Level.INFO, "Invalid JWT signature: {0}", e.getMessage());
            return false;
        }
    }
    
    public X509Certificate getCertificateFromInternalKeystore() {
		try {
			MBeanServerConnection mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();
			ObjectName serverIdentityMBeanName = new ObjectName(
					"jboss.as:core-service=management,security-realm=ApplicationRealm,server-identity=ssl");
			String alias = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName, "alias");
			String keystorePassword = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName,
					"keystorePassword");
			String keystorePath = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName, "keystorePath");
			String keystoreProvider = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName,
					"keystoreProvider");
			String keystoreRelativeTo = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName,
					"keystoreRelativeTo");

			String absolutePath = exractMBeanServerPath(keystoreRelativeTo, mbeanServerConnection);
			log.fine(() -> String.format(
					"Alias: %s KeystorePassword: %s KeystorePath: %s KeystoreProvider: %s KeystoreRelativeTo: %s AbsolutePath: %s",
					alias, keystorePassword, keystorePath, keystoreProvider, keystoreRelativeTo, absolutePath));
			return extractCertificateFromInternalKeystore(keystoreProvider, absolutePath, keystorePath,
					keystorePassword, alias);

		} catch (Exception ex) {
			log.log(Level.WARNING, "Could not get certificate from internal key store.", ex);
		}

		return null;
	}

	public String exractMBeanServerPath(String keystoreRelativeTo, MBeanServerConnection mbeanServerConnection) {
		try {
			String mBeanPath = "jboss.as:path=" + keystoreRelativeTo;
			ObjectName pathMBean = new ObjectName(mBeanPath);
			return (String) mbeanServerConnection.getAttribute(pathMBean, "path");
		} catch (Exception ex) {
			log.log(Level.WARNING, "Could not get certificate from internal key store.", ex);
		}

		return null;
	}

	private X509Certificate extractCertificateFromInternalKeystore(String keystoreProvider, String absolutePath,
			String keystorePath, String keystorePassword, String alias) {
		try {
			KeyStore ks = KeyStore.getInstance(keystoreProvider);
			try (FileInputStream fis = new FileInputStream(Paths.get(absolutePath, keystorePath).toFile())) {
				ks.load(fis, keystorePassword.toCharArray());

				Certificate certificateFromStore = ks.getCertificate(alias);
				if (certificateFromStore instanceof X509Certificate) {
					return (X509Certificate) certificateFromStore;
				}
			}
		} catch (Exception ex) {
			log.log(Level.WARNING, "Could not get certificate from internal key store.", ex);
		}

		return null;
	}

}
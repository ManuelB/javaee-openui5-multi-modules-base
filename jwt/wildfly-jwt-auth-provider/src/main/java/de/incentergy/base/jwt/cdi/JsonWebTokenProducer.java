package de.incentergy.base.jwt.cdi;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.jwt.impl.DefaultJWTCallerPrincipal;
import org.eclipse.microprofile.jwt.principal.JWTAuthContextInfo;
import org.eclipse.microprofile.jwt.principal.JWTCallerPrincipal;
import org.eclipse.microprofile.jwt.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.principal.ParseException;
import org.jose4j.jwt.JwtClaims;

@RequestScoped
public class JsonWebTokenProducer {

	private static final Logger log = Logger.getLogger(JsonWebTokenProducer.class.getName());
	
	@Inject
	Instance<HttpServletRequest> httpRequestInstance;
	
	@Inject
	JWTAuthContextInfo authContextInfo;
	
	@Produces
	@RequestScoped
	public JsonWebToken produce() {
		// Try to get JsonWebToken from HttpRequest
		try {
			HttpServletRequest  httpRequest =  httpRequestInstance.get();
			String authHeaderVal = httpRequest.getHeader("Authorization");
	
			if (authHeaderVal != null && authHeaderVal.startsWith("Bearer")) {
				String bearerToken = authHeaderVal.substring(7);
				return getJsonWebTokenFromAuthorizationStringWithoutBearer(bearerToken, authContextInfo);
			}
			
			if (authHeaderVal != null && authHeaderVal.startsWith("Basic")) {
				String basicAuth = authHeaderVal.substring(6);
				String unencodedBasicAuth = new String(Base64.getDecoder().decode(basicAuth));
				// use the password as JWT token
				return getJsonWebTokenFromAuthorizationStringWithoutBearer(unencodedBasicAuth.split(":")[1], authContextInfo);
			}
		} catch(Exception ex) {
			// Caused by: org.jboss.weld.exceptions.IllegalStateException: WELD-000710: Cannot inject HttpServletRequest outside of a Servlet request
			log.log(Level.FINE, "Can't get JsonWebToken", ex);
		}
		
		return new DefaultJWTCallerPrincipal(null, null, new JwtClaims(), "anonymous");
	}

	public static JsonWebToken getJsonWebTokenFromAuthorizationStringWithoutBearer(String bearerToken, JWTAuthContextInfo authContextInfo) {
		JWTCallerPrincipalFactory factory = JWTCallerPrincipalFactory.instance();
		JWTCallerPrincipal callerPrincipal;
		try {
			callerPrincipal = factory.parse(bearerToken, authContextInfo);
		} catch (ParseException e) {
			log.log(Level.WARNING, "Can't parse JWT Token", e);
			return new DefaultJWTCallerPrincipal(bearerToken, null, new JwtClaims(), "anonymous");
		}
		log.fine(() -> String.format("Producing token from: %s", bearerToken));
		return callerPrincipal;
	}
}

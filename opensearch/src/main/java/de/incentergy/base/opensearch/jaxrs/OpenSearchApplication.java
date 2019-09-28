package de.incentergy.base.opensearch.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.jwt.jaxrs.JWTAuthFilter;

@ApplicationPath("/opensearch")
public class OpenSearchApplication extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<Class<?>>();
		set.add(Search.class);
		// set.add(JWTAuthFilter.class);
        return set;
    }
}

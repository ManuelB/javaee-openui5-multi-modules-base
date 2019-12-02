package de.incentergy.base.olingo.data;

import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.core.rest.app.AbstractODataApplication;
import org.eclipse.microprofile.jwt.jaxrs.JWTAuthFilter;

@ApplicationPath("/Data.svc")
public class DataApplication extends AbstractODataApplication {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = super.getClasses();
		set.add(JWTAuthFilter.class);
		return set;
	}

	@Override
	public Class<? extends ODataServiceFactory> getServiceFactoryClass() {
		return DataServiceFactory.class;
	}

}
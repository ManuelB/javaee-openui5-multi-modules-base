package de.incentergy.base.olingo;

import javax.ws.rs.ApplicationPath;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.core.rest.app.AbstractODataApplication;

@ApplicationPath("/Data.svc")
public class DataApplication extends AbstractODataApplication {

	@Override
	public Class<? extends ODataServiceFactory> getServiceFactoryClass() {
		return DataServiceFactory.class;
	}

}

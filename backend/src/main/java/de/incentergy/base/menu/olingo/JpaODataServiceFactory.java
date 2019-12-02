package de.incentergy.base.menu.olingo;

import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;

public class JpaODataServiceFactory extends de.incentergy.base.olingo.JpaODataServiceFactory {
	@Override
	public ODataSingleProcessor createCustomODataProcessor(ODataJPAContext oDataJPAContext) {
		return new FilterJpaProcessor(oDataJPAContext);
	}

	public String getPersistenceUnitName() {
		return "menu";
	}
}
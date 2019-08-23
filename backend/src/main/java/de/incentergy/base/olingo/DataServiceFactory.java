package de.incentergy.base.olingo;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;

public class DataServiceFactory extends ODataServiceFactory {

	@Override
	public ODataService createService(ODataContext ctx) throws ODataException {

		EdmProvider edmProvider = new DataEdmProvider();
		ODataSingleProcessor singleProcessor = new DataODataSingleProcessor();

		return createODataSingleProcessorService(edmProvider, singleProcessor);
	}
}
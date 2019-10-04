package de.incentergy.base.olingo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySetProcessor;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.expression.BinaryExpression;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.LiteralExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;

public class DataODataSingleProcessor extends ODataSingleProcessor {

	private static final Logger log = Logger.getLogger(DataODataSingleProcessor.class.getName());

	@Override
	public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
		List<Map<String, Object>> modules = getDeployedAppliationsFromWildfly(uriInfo.getFilter());
		return EntityProvider.writeFeed(contentType, uriInfo.getStartEntitySet(), modules,
				EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
	}

	/**
	 * @see EntitySetProcessor
	 */
	@Override
	public ODataResponse countEntitySet(final GetEntitySetCountUriInfo uriInfo, final String contentType)
			throws ODataException {
		List<Map<String, Object>> modules = getDeployedAppliationsFromWildfly(uriInfo.getFilter());
		return EntityProvider.writeText(String.valueOf(modules.size()));
	}

	@Override
	public ODataResponse executeBatch(final BatchHandler handler, final String contentType, final InputStream content)
			throws ODataException {

		List<BatchResponsePart> batchResponseParts = new ArrayList<BatchResponsePart>();
		PathInfo pathInfo = getContext().getPathInfo();
		EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
		List<BatchRequestPart> batchParts = EntityProvider.parseBatchRequest(contentType, content, batchProperties);
		for (BatchRequestPart batchPart : batchParts) {
			batchResponseParts.add(handler.handleBatchPart(batchPart));
		}
		return EntityProvider.writeBatchResponse(batchResponseParts);
	}

	private List<Map<String, Object>> getDeployedAppliationsFromWildfly(FilterExpression filterExpression) {
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			MBeanServerConnection mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();

			Set<ObjectName> appsAndLibs = new HashSet<>();

			ObjectName jbossAsObjectNameApps = new ObjectName("jboss.as:deployment=*-frontend*");
			appsAndLibs.addAll(mbeanServerConnection.queryNames(jbossAsObjectNameApps, null));

			ObjectName jbossAsObjectName = new ObjectName("jboss.as:deployment=*-lib*");
			appsAndLibs.addAll(mbeanServerConnection.queryNames(jbossAsObjectName, null));

			for (ObjectName deployedApplication : appsAndLibs) {
				try {
					Object moduleName = mbeanServerConnection.getAttribute(deployedApplication, "name");
					if (moduleName != null && !moduleName.toString().startsWith("base-frontend")) {
						Map<String, Object> map = new HashMap<>();
						map.put("Type", moduleName.toString().contains("-lib") ? "Library" : "Component");
						map.put("Name", moduleName.toString().replace("-frontend", "").replace("-lib", "").replace(".war", ""));
						list.add(map);
					}
				} catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException
						| ReflectionException e) {
					log.log(Level.SEVERE, "Can't get attribute from mbean", e);
				}
			}
		} catch (MalformedObjectNameException | IOException e) {
			log.log(Level.SEVERE, "Can't get modules from Wildfly server", e);
		}
		if (filterExpression != null) {
			CommonExpression commonExpression = filterExpression.getExpression();
			if (commonExpression instanceof BinaryExpression) {
				BinaryExpression binaryExpression = (BinaryExpression) commonExpression;
				if (binaryExpression.getLeftOperand() instanceof PropertyExpression
						&& binaryExpression.getRightOperand() instanceof LiteralExpression) {
					PropertyExpression propertyExpression = (PropertyExpression) binaryExpression.getLeftOperand();
					LiteralExpression literalExpression = (LiteralExpression) binaryExpression.getRightOperand();
					String propertyName = propertyExpression.getPropertyName();
					if (propertyName.equals("Type")) {
						list = list.stream()
								.filter(m -> ("'"+m.getOrDefault(propertyName, "")+"'").equals(literalExpression.getUriLiteral()))
								.collect(Collectors.toList());
					}

				}
			}
		}

		return list;
	}
}

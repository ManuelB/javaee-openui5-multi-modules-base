package de.incentergy.base.menu.olingo;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;

import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.core.uri.expression.ExpressionParserInternalError;
import org.apache.olingo.odata2.core.uri.expression.FilterParserImpl;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPADefaultProcessor;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class FilterJpaProcessor extends ODataJPADefaultProcessor {

	private static final Logger log = Logger.getLogger(FilterJpaProcessor.class.getName());

	public FilterJpaProcessor(ODataJPAContext oDataJPAContext) {
		super(oDataJPAContext);
	}

	@Override
	public ODataResponse readEntitySet(final GetEntitySetUriInfo uriParserResultView, final String contentType)
			throws ODataException {

		addOnlyRoleAllowed(uriParserResultView);

		ODataResponse oDataResponse = null;
		try {
			oDataJPAContext.setODataContext(getContext());
			List<Object> jpaEntities = jpaProcessor.process(uriParserResultView);
			oDataResponse = responseBuilder.build(uriParserResultView, jpaEntities, contentType);
		} finally {
			close();
		}
		return oDataResponse;
	}

	private void addOnlyRoleAllowed(GetEntitySetUriInfo uriParserResultView) throws ODataException {
		if (!(uriParserResultView instanceof UriInfoImpl)) {
			throw new IllegalArgumentException("uriParserResultView must be an " + UriInfoImpl.class.getName()
					+ " but is: " + uriParserResultView.getClass().getName());
		}
		var uriInfoImpl = (UriInfoImpl) uriParserResultView;
		var filter = uriInfoImpl.getFilter();
		var jwtToken = get(JsonWebToken.class);
		if(jwtToken == null) {
			throw new ODataException("jwtToken in addOnlyRoleAllowed is null");
		}
		try {
			var filterString = ((filter != null && filter.getUriLiteral() != null && !filter.getUriLiteral().isBlank())
					? "("+filter.getUriLiteral() + ") and "
					: "") + "(RoleAllowed eq null or RoleAllowed eq '"+jwtToken.getGroups().stream().collect( Collectors.joining( "' or RoleAllowed eq '" ) )+"')";
			filter = new FilterParserImpl((EdmEntityType) uriParserResultView.getTargetType()).parseFilterString(
					filterString);
			uriInfoImpl.setFilter(filter);
		} catch (ExpressionParserException | ExpressionParserInternalError e) {
			throw new ODataException("Problem during adding addOnlyRoleAllowed", e);
		}
	}

	public static BeanManager lookupBeanManager() {
		BeanManager beanManager = null;
		InitialContext context;
		try {
			context = new InitialContext();
			beanManager = (BeanManager) context.lookup("java:comp/BeanManager");
		} catch (Exception ex) {
			log.log(Level.FINE, "Problem finding BeanManager", ex);
		}
		return beanManager;
	}

	public static <T> T get(Class<T> clazz, BeanManager beanManager) {
		Set<Bean<?>> beans = beanManager.getBeans(clazz);
		Iterator<Bean<?>> beanIterator = beans.iterator();
		if (beanIterator.hasNext()) {
			@SuppressWarnings("unchecked")
			Bean<T> bean = (Bean<T>) beanIterator.next();
			CreationalContext<T> creationalBean = beanManager.createCreationalContext(bean);
			@SuppressWarnings("unchecked")
			T object = (T) beanManager.getReference(bean, clazz, creationalBean);
			return object;
		} else {
			return null;
		}
	}

	public static <T> T get(Class<T> clazz) {
		return get(clazz, lookupBeanManager());
	}

}

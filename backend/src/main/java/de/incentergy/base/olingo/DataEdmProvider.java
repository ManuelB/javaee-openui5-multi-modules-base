package de.incentergy.base.olingo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.exception.ODataException;

public class DataEdmProvider extends EdmProvider {
	public List<Schema> getSchemas() throws ODataException {
		List<Schema> schemas = new ArrayList<Schema>();

		Schema schema = new Schema();
		schema.setNamespace("base");

		schema.setEntityTypes(Arrays.asList(getEntityType(null)));

		List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
		EntityContainer entityContainer = new EntityContainer();
		entityContainer.setName("EntityContainer").setDefaultEntityContainer(true);

		entityContainer.setEntitySets(Arrays.asList(createEntitySetModules()));

		entityContainers.add(entityContainer);
		schema.setEntityContainers(entityContainers);

		schemas.add(schema);

		return schemas;
	}

	@Override
	public EntityType getEntityType(FullQualifiedName edmFQName) {
		return new EntityType().setName("Module")
				.setProperties(Arrays.asList(new SimpleProperty().setName("Name").setType(EdmSimpleTypeKind.String)
						.setFacets(new Facets().setNullable(false))))
				.setKey(new Key().setKeys(Arrays.asList(new PropertyRef().setName("Name"))));
	}

	private EntitySet createEntitySetModules() {
		return new EntitySet().setName("Modules").setEntityType(new FullQualifiedName("base", "Module"));
	}

	public EntityContainerInfo getEntityContainerInfo(String name) throws ODataException {
		return new EntityContainerInfo().setName("EntityContainer").setDefaultEntityContainer(true);
	}

	public EntitySet getEntitySet(String entityContainer, String name) throws ODataException {
		return name.equals("Modules") ? createEntitySetModules() : null;
	}
}

package de.incentergy.base.olingo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.olingo.odata2.jpa.processor.core.ODataJPAContextImpl;
import org.junit.jupiter.api.Test;

class StandardNavigationNamesJPAEdmMappingModelServiceTest {

	@Test
	void testIsMappingModelExists() {
		assertTrue(new StandardNavigationNamesJPAEdmMappingModelService(new ODataJPAContextImpl()).isMappingModelExists());
	}

	@Test
	void testMapJPARelationshipStringString() {
		assertEquals("Skills",
				new StandardNavigationNamesJPAEdmMappingModelService(new ODataJPAContextImpl()).mapJPARelationship("Employee", "skills"));
	}

	@Test
	void testMapJPAPersistenceUnitString() {
		assertNull(new StandardNavigationNamesJPAEdmMappingModelService(new ODataJPAContextImpl()).mapJPAPersistenceUnit(null));
	}

}

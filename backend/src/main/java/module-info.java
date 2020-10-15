module de.incentergy.base {
	
	requires java.management;
	requires java.logging;
	requires olingo.odata2.api;
	requires javaee.api;
	requires olingo.odata2.jpa.processor.api;
	requires olingo.odata2.core;
	requires java.xml.bind;
	requires java.naming;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.module.jaxb;
	requires microprofile.jwt.auth.api;
	requires base.olingo;
	requires jwt.auth.jaxrs.prototype;
	exports de.incentergy.base.olingo.data;
}
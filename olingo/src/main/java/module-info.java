module de.incentergy.base.olingo {
	requires olingo.odata2.api;
	requires olingo.odata2.jpa.processor.api;
	requires java.naming;
	requires java.logging;
	requires olingo.odata2.jpa.processor.core;
	requires javaee.api;
	exports de.incentergy.base.olingo;
}
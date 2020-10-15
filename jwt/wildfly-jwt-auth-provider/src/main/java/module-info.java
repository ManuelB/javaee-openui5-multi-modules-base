module de.incentergy.base.jwt.wildfly {
	requires java.management;
	requires java.logging;
	requires java.naming;
	requires javaee.api;
	requires jwt.auth.principal.prototype;
	requires org.jose4j;
	requires microprofile.jwt.auth.api;
	exports de.incentergy.base.jwt.wildfly;
	exports de.incentergy.base.jwt.cdi;
}
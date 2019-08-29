package de.incentergy.base.jwt.wildfly;

import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.eclipse.microprofile.jwt.principal.JWTAuthContextInfo;

/**
 * A simple producer for JWTAuthContextInfo needed for verify the test tokens
 */
public class JWTAuthContextInfoProvider {

	private static Logger log = Logger.getLogger(JWTAuthContextInfoProvider.class.getName());

	private JWTAuthContextInfo contextInfo;
	MBeanServerConnection mbeanServerConnection;

	/**
	 * Create the JWTAuthContextInfo using https://server.example.com as the issuer
	 * and the test resources publicKey.pem as the public key of the signer.
	 *
	 * @throws Exception on failure
	 */
	@PostConstruct
	void init() {
		contextInfo = new JWTAuthContextInfo();
		X509Certificate certificate = null;
		certificate = getCertificateFromInternalKeystore(mbeanServerConnection);

		RSAPublicKey key = (RSAPublicKey) certificate.getPublicKey();
		contextInfo.setIssuedBy("https://" + setCommonName(certificate));
		contextInfo.setSignerKey(key);
	}

	public String setCommonName(X509Certificate certificate) {
		try {
			return new LdapName(certificate.getSubjectDN().getName()).getRdns().stream()
					.filter(rdn -> rdn.getType().equalsIgnoreCase("cn")).map(rdn -> rdn.getValue().toString())
					.collect(Collectors.joining(""));
		} catch (InvalidNameException e) {
			throw new IllegalStateException("Could not get name from certificate", e);
		}
	}

	public X509Certificate getCertificateFromInternalKeystore(MBeanServerConnection mbeanServerConnection) {
		try {
			if (mbeanServerConnection == null) {
				mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();
			}
			ObjectName serverIdentityMBeanName = new ObjectName(
					"jboss.as:core-service=management,security-realm=ApplicationRealm,server-identity=ssl");
			String alias = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName, "alias");
			String keystorePassword = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName,
					"keystorePassword");
			String keystorePath = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName, "keystorePath");
			String keystoreProvider = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName,
					"keystoreProvider");
			String keystoreRelativeTo = (String) mbeanServerConnection.getAttribute(serverIdentityMBeanName,
					"keystoreRelativeTo");

			String absolutePath = exractMBeanServerPath(keystoreRelativeTo, mbeanServerConnection);
			log.fine(() -> String.format(
					"Alias: %s KeystorePassword: %s KeystorePath: %s KeystoreProvider: %s KeystoreRelativeTo: %s AbsolutePath: %s",
					alias, keystorePassword, keystorePath, keystoreProvider, keystoreRelativeTo, absolutePath));
			return extractCertificateFromInternalKeystore(keystoreProvider, absolutePath, keystorePath,
					keystorePassword, alias);

		} catch (Exception ex) {
			log.log(Level.WARNING, "Could not get certificate from internal key store.", ex);
		}

		return null;
	}

	public String exractMBeanServerPath(String keystoreRelativeTo, MBeanServerConnection mbeanServerConnection) {
		try {
			String mBeanPath = "jboss.as:path=" + keystoreRelativeTo;
			ObjectName pathMBean = new ObjectName(mBeanPath);
			return (String) mbeanServerConnection.getAttribute(pathMBean, "path");
		} catch (Exception ex) {
			log.log(Level.WARNING, "Could not get certificate from internal key store.", ex);
		}

		return null;
	}

	private X509Certificate extractCertificateFromInternalKeystore(String keystoreProvider, String absolutePath,
			String keystorePath, String keystorePassword, String alias) {
		try {
			KeyStore ks = KeyStore.getInstance(keystoreProvider);
			try (FileInputStream fis = new FileInputStream(Paths.get(absolutePath, keystorePath).toFile())) {
				ks.load(fis, keystorePassword.toCharArray());

				Certificate certificateFromStore = ks.getCertificate(alias);
				if (certificateFromStore instanceof X509Certificate) {
					return (X509Certificate) certificateFromStore;
				}
			}
		} catch (Exception ex) {
			log.log(Level.WARNING, "Could not get certificate from internal key store.", ex);
		}

		return null;
	}

	@Produces
	@Dependent
	JWTAuthContextInfo contextInfo() {
		return contextInfo;
	}
}
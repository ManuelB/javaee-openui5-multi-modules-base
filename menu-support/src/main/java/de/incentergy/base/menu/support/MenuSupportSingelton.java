package de.incentergy.base.menu.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Topic;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

@Singleton
@Startup
public class MenuSupportSingelton {

	@Inject
	private JMSContext context;

	@Resource(mappedName = "java:/jms/base/deployment")
	Topic topic;

	@Resource(lookup = "java:app/AppName")
	private String appName;

	private static final Logger log = Logger.getLogger(MenuSupportSingelton.class.getName());

	private static final Pattern APP_NAME_WITHOUT_FRONTEND = Pattern.compile("(.*)-frontend");

	@PostConstruct
	public void sendPostDeplomentEventToJms() {
		log.log(Level.INFO, "Sending post-deployment message for wep app: {0}", new Object[] { appName });
		context.createProducer().setProperty("AppName", appName).setProperty("lifecycle", "post-deployment").send(topic,
				generateDeploymentEventJsonFromFiles("POST_DEPLOYMENT"));
	}

	@PreDestroy
	public void sendPreUndeploymentEventToJms() {
		log.log(Level.INFO, "Sending pre-undeployment message for wep app: {0}", new Object[] { appName });
		context.createProducer().setProperty("AppName", appName).setProperty("lifecycle", "pre-undeployment")
				.send(topic, generateDeploymentEventJsonFromFiles("PRE_UNDEPLOYMENT"));
	}

	private Message generateDeploymentEventJsonFromFiles(String lifecycle) {

		String manifestJsonPath = getManifestJsonPath();

		JsonObjectBuilder deploymentEventJson = Json.createObjectBuilder();
		deploymentEventJson.add("lifecycle", lifecycle);
		deploymentEventJson.add("applicationName", appName);

		JsonArrayBuilder navigationListItemListItems = Json.createArrayBuilder();

		if (manifestJsonPath != null) {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			// e.g.
			// /home/manuel/wildfly-18.0.0.Final/standalone/deployments/employee-frontend.war/WEB-INF/lib/base-menu-support-1.0-SNAPSHOT.jar/META-INF/base-menu-support
			URL url = classLoader.getResource("/META-INF/base-menu-support");
			if(url == null) {
				log.warning("Marker file not found. Sending empty message.");
			} else {
			String pathOfMarkerFile = url.getPath();
			String webAppPath = pathOfMarkerFile.replaceAll("[^\\/]*/[^\\/]*/[^\\/]*/META-INF/base-menu-support", "");
			String manifestJsonFullPath = webAppPath + manifestJsonPath;
			try (InputStream input = new FileInputStream(manifestJsonFullPath)) {
				JsonObject manifestJson = Json.createReader(input).readObject();
				input.close();
				JsonArray routes = manifestJson.getJsonObject("sap.ui5").getJsonObject("routing")
						.getJsonArray("routes");
				for (JsonValue jsonValue : routes) {
					if (jsonValue instanceof JsonObject) {
						JsonObject route = (JsonObject) jsonValue;
						if (route.containsKey("navigationItem")) {
							JsonObjectBuilder javaNavigationItemBuilder = createNavigationItem(route);

							navigationListItemListItems.add(javaNavigationItemBuilder.build());
						}
					} else {
						log.log(Level.WARNING, "sap.ui5/routing/routes contains non objects: {0}", jsonValue);
					}
				}
			} catch (FileNotFoundException e) {
				log.log(Level.WARNING, "Did not find manifest.json", e);
			} catch (IOException e) {
				log.log(Level.WARNING, "Error on reading manifest.json", e);
			}
		}
		}

		deploymentEventJson.add("navigationListItemList",
				Json.createObjectBuilder().add("items", navigationListItemListItems));
		String json = deploymentEventJson.build().toString();
		log.fine(json);
		return context.createTextMessage(json);
	}

	private JsonObjectBuilder createNavigationItem(JsonObject route) {
		JsonObject navigationItem = route.getJsonObject("navigationItem");

		JsonObjectBuilder languages = Json.createObjectBuilder();

		JsonObject texts = navigationItem.getJsonObject("text");

		for (Entry<String, JsonValue> entry : texts.entrySet()) {
			languages.add(entry.getKey(),
					Json.createObjectBuilder().add("xsi:type", ".NavigationListItemLocalized")
							.add("locale", entry.getKey()).add("text", entry.getValue())
							.add("navigationListItem", navigationItem.getString("id")).build());
		}

		JsonObjectBuilder javaNavigationItemBuilder = Json.createObjectBuilder()
				.add("xsi:type", ".NavigationListItem").add("id", navigationItem.getString("id"))
				.add("children", Json.createArrayBuilder().build())
				.add("parent",
						navigationItem.containsKey("parent")
								? navigationItem.getJsonString("parent")
								: JsonValue.NULL)
				.add("navigationListItemLocalized", languages)
				.add("route",
						navigationItem.containsKey("route") ? navigationItem.getJsonString("route")
								: JsonValue.NULL)
				.add("topLevelItem", navigationItem.getBoolean("topLevelItem", true))
				.add("sort",
						navigationItem.containsKey("sort") ? navigationItem.getJsonNumber("sort")
								: JsonValue.NULL)
				
				.add("icon",
						navigationItem.containsKey("icon")
								? navigationItem.getJsonString("icon")
								: JsonValue.NULL);
		
		if(navigationItem.containsKey("roleAllowed")) {
			javaNavigationItemBuilder.add("roleAllowed",
				navigationItem.containsKey("roleAllowed")
						? navigationItem.getJsonString("roleAllowed")
						: JsonValue.NULL);
		}
		return javaNavigationItemBuilder;
	}

	private String getManifestJsonPath() {

		Matcher m = APP_NAME_WITHOUT_FRONTEND.matcher(appName);
		if (m.matches()) {
			return "incentergy/" + m.group(1).replaceAll("-", "") + "/manifest.json";
		} else {
			log.log(Level.WARNING,
					"Could not extract packageName because web app name does not contain -frontent is: {0}", appName);
		}

		return null;
	}

}

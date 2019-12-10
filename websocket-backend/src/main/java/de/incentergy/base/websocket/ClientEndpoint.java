package de.incentergy.base.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/base/client-websocket"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "") })
@ServerEndpoint("/client")
public class ClientEndpoint implements MessageListener {

	private static final Logger log = Logger.getLogger(ClientEndpoint.class.getName());

	private static final Set<Session> sessions = new HashSet<>();

	@OnOpen
	public void open(Session session) {
		sessions.add(session);
	}

	@OnClose
	public void close(Session session) {
		sessions.remove(session);
	}

	@OnError
	public void onError(Throwable error) {
		log.log(Level.WARNING, "Websocket", error);
	}

	@OnMessage
	public void handleWebSocketMessage(String message, Session session) {
		log.fine(message);
	}

	@Override
	public void onMessage(Message message) {
		try {
			String text = message.getBody(String.class);
			sessions.stream().forEach(session -> {
				try {
					session.getBasicRemote().sendText(text);
				} catch (IOException e) {
					log.log(Level.WARNING, "Exception during websocket sendText", e);
				}
			});
		} catch (JMSException e) {
			log.log(Level.WARNING, "JMS", e);
		}
	}
}

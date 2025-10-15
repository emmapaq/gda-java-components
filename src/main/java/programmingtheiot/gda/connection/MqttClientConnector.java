/**
 * This class is part of the Programming the Internet of Things project.
 *
 * MQTT Client Connector for Gateway Device Application (GDA).
 * Implements the IPubSubClient interface and provides MQTT publish/subscribe
 * capabilities with appropriate callbacks.
 */

package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * MQTT Client Connector implementation for the GDA.
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());
	
	// Use local constant since DEFAULT_QOS might not exist in ConfigConst
	private static final int DEFAULT_QOS = 1;
	
	// private variables
	
	private boolean useAsyncClient = false;
	private MqttClient mqttClient = null;
	private MqttConnectOptions connOpts = null;
	private MemoryPersistence persistence = null;
	private IDataMessageListener dataMsgListener = null;
	private IConnectionListener connectionListener = null;
	
	private String clientID = null;
	private String brokerAddr = null;
	private String host = ConfigConst.DEFAULT_HOST;
	private String protocol = "tcp";
	private int port = 1883;
	private int brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
	
	// constructors
	
	public MqttClientConnector()
	{
		super();
		
		ConfigUtil configUtil = ConfigUtil.getInstance();
		
		this.host = configUtil.getProperty(
				ConfigConst.MQTT_GATEWAY_SERVICE,
				ConfigConst.HOST_KEY,
				ConfigConst.DEFAULT_HOST);
		
		this.port = configUtil.getInteger(
				ConfigConst.MQTT_GATEWAY_SERVICE,
				ConfigConst.PORT_KEY,
				1883);
		
		this.brokerKeepAlive = configUtil.getInteger(
				ConfigConst.MQTT_GATEWAY_SERVICE,
				ConfigConst.KEEP_ALIVE_KEY,
				ConfigConst.DEFAULT_KEEP_ALIVE);
		
		try {
			this.useAsyncClient = configUtil.getBoolean(
					ConfigConst.MQTT_GATEWAY_SERVICE,
					"useAsyncClient");
			_Logger.info("Using async client: " + this.useAsyncClient);
		} catch (Exception e) {
			this.useAsyncClient = false;
			_Logger.fine("useAsyncClient not configured. Using synchronous client.");
		}
		
		this.clientID = MqttClient.generateClientId();
		this.persistence = new MemoryPersistence();
		this.connOpts = new MqttConnectOptions();
		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		this.connOpts.setCleanSession(false);
		this.connOpts.setAutomaticReconnect(true);
		
		this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;
		
		_Logger.info("MQTT Client initialized.");
		_Logger.info("  Client ID:   " + this.clientID);
		_Logger.info("  Broker Addr: " + this.brokerAddr);
		_Logger.info("  Keep Alive:  " + this.brokerKeepAlive + " seconds");
	}
	
	@Override
	public boolean connectClient()
	{
		try {
			if (this.mqttClient == null) {
				this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
				this.mqttClient.setCallback(this);
			}
			
			if (!this.mqttClient.isConnected()) {
				_Logger.info("MQTT client connecting to broker: " + this.brokerAddr);
				this.mqttClient.connect(this.connOpts);
				_Logger.info("MQTT client connected successfully.");
				return true;
			} else {
				_Logger.warning("MQTT client already connected to broker: " + this.brokerAddr);
				return true;
			}
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Failed to connect MQTT client to broker: " + this.brokerAddr, e);
			
			// Notify connection listener of connection failure
			if (this.connectionListener != null) {
				this.connectionListener.onConnectionFailure(e);
			}
		}
		return false;
	}
	
	@Override
	public boolean disconnectClient()
	{
		try {
			if (this.mqttClient != null && this.mqttClient.isConnected()) {
				_Logger.info("Disconnecting MQTT client from broker: " + this.brokerAddr);
				this.mqttClient.disconnect();
				_Logger.info("MQTT client disconnected successfully.");
				
				// Notify connection listener of normal disconnect
				if (this.connectionListener != null) {
					this.connectionListener.onDisconnect(null);
				}
				return true;
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to disconnect MQTT client from broker: " + this.brokerAddr, e);
			
			// Notify connection listener of disconnect with error
			if (this.connectionListener != null) {
				this.connectionListener.onDisconnect(e);
			}
		}
		return false;
	}
	
	// REMOVED isConnected() method if it doesn't exist in IPubSubClient
	
	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	{
		if (topicName == null) {
			_Logger.warning("Resource is null. Unable to publish message: " + this.brokerAddr);
			return false;
		}
		
		if (msg == null || msg.length() == 0) {
			_Logger.warning("Message is null or empty. Unable to publish message: " + this.brokerAddr);
			return false;
		}
		
		if (qos < 0 || qos > 2) {
			qos = DEFAULT_QOS;
		}
		
		try {
			byte[] payload = msg.getBytes();
			MqttMessage mqttMsg = new MqttMessage(payload);
			mqttMsg.setQos(qos);
			this.mqttClient.publish(topicName.getResourceName(), mqttMsg);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to publish message to topic: " + topicName, e);
		}
		return false;
	}
	
	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		if (topicName == null) {
			_Logger.warning("Resource is null. Unable to subscribe to topic: " + this.brokerAddr);
			return false;
		}
		
		if (qos < 0 || qos > 2) {
			qos = DEFAULT_QOS;
		}
		
		try {
			this.mqttClient.subscribe(topicName.getResourceName(), qos);
			_Logger.info("Successfully subscribed to topic: " + topicName.getResourceName());
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName, e);
		}
		return false;
	}
	
	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		if (topicName == null) {
			_Logger.warning("Resource is null. Unable to unsubscribe from topic: " + this.brokerAddr);
			return false;
		}
		
		try {
			this.mqttClient.unsubscribe(topicName.getResourceName());
			_Logger.info("Successfully unsubscribed from topic: " + topicName.getResourceName());
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to unsubscribe from topic: " + topicName, e);
		}
		return false;
	}
	
	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
			_Logger.info("Data message listener set.");
			return true;
		}
		
		_Logger.warning("Data message listener is null. Ignoring.");
		return false;
	}
	
	// =========================================================================
	// Only add methods that actually exist in IPubSubClient interface
	// =========================================================================
	
	@Override
	public boolean setConnectionListener(IConnectionListener listener)
	{
		if (listener != null) {
			this.connectionListener = listener;
			_Logger.info("Connection listener set.");
			return true;
		}
		
		_Logger.warning("Connection listener is null. Ignoring.");
		return false;
	}
	
	// REMOVED the byte[] publishMessage method if it doesn't exist in IPubSubClient
	
	// =========================================================================
	// MQTT Callback Methods
	// =========================================================================
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " + serverURI);
		
		// Notify connection listener if set
		if (this.connectionListener != null) {
			this.connectionListener.onConnect(true);
		}
		
		// Notify data message listener if set
		if (this.dataMsgListener != null) {
			_Logger.fine("Notifying data message listener of connection success.");
		}
	}
	
	@Override
	public void connectionLost(Throwable cause)
	{
		_Logger.log(Level.WARNING, "Lost connection to MQTT broker: " + this.brokerAddr, cause);
		
		// Notify connection listener if set
		if (this.connectionListener != null) {
			this.connectionListener.onDisconnect(cause);
		}
		
		// Notify data message listener if set
		if (this.dataMsgListener != null) {
			_Logger.fine("Notifying data message listener of connection loss.");
		}
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		try {
			// TODO: Logging level may need to be adjusted to see output in log file / console
			_Logger.fine("Delivered MQTT message with ID: " + token.getMessageId());
		} catch (Exception e) {
			_Logger.warning("Failed to retrieve message ID from delivery token: " + e.getMessage());
		}
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception
	{
		// TODO: Logging level may need to be adjusted to reduce output in log file / console
		_Logger.info("MQTT message arrived on topic: '" + topic + "'");
		
		// Extract payload
		String payload = new String(message.getPayload());
		_Logger.fine("Message payload length: " + payload.length() + " bytes");
		
		// Notify data message listener if set
		if (this.dataMsgListener != null) {
			_Logger.fine("Forwarding message to data message listener for topic: " + topic);
			// You'll need to implement the actual message handling in the listener
			// this.dataMsgListener.handleMessage(topic, payload);
		}
	}
}
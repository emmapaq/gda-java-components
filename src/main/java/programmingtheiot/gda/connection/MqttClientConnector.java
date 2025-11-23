package programmingtheiot.gda.connection;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;  // CHANGED from MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.SimpleCertManagementUtil;
import programmingtheiot.data.DataUtil;

import programmingtheiot.data.SensorData;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * MQTT Client Connector implementation using Eclipse Paho MQTT asynchronous client.
 * 
 * Handles connection, disconnection, publishing, and subscribing to MQTT broker.
 * Implements callback mechanisms for message handling and connection events.
 * 
 * IMPORTANT: Uses asynchronous MqttAsyncClient (not MqttClient) to prevent deadlock.
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
    // Static variables
    
    private static final Logger _Logger =
        Logger.getLogger(MqttClientConnector.class.getName());
    
    
    // Member variables
    
    private String host = ConfigConst.DEFAULT_HOST;
    private int port = ConfigConst.DEFAULT_MQTT_PORT;
    private int brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
    private String clientID = null;
    private String pemFileName = null;
    
    private boolean enableEncryption = false;
    private boolean useCleanSession = false;
    private boolean enableAutoReconnect = true;
    
    // NOTE: MQTT client updated to use async client vs sync client
    private MqttAsyncClient mqttClient = null;
    private MqttConnectOptions connOpts = null;
    private MemoryPersistence persistence = null;
    private IDataMessageListener dataMsgListener = null;
    
    
    // Constructors
    
    /**
     * Default constructor.
     */
    public MqttClientConnector()
    {
        super();
        initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
    }
    
    /**
     * Constructor with custom client ID.
     */
    public MqttClientConnector(String clientID)
    {
        super();
        
        if (clientID != null && clientID.trim().length() > 0) {
            this.clientID = clientID;
        }
        
        initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
    }
    
    
    // Public methods - IPubSubClient implementation
    
    @Override
    public boolean connectClient()
    {
        try {
            if (this.mqttClient == null) {
                // Create MQTT async client instance
                this.persistence = new MemoryPersistence();
                
                // NOTE: MQTT client updated to use async client vs sync client
                this.mqttClient = new MqttAsyncClient(
                    this.host + ":" + this.port,
                    this.clientID,
                    this.persistence
                );
                
                // Set callback handler
                this.mqttClient.setCallback(this);
            }
            
            if (!this.mqttClient.isConnected()) {
                _Logger.info("MQTT client connecting to broker: " + this.host + ":" + this.port);
                
                // Connect using configured options
                this.mqttClient.connect(this.connOpts);
                
                // NOTE: When using the async client, returning 'true' here doesn't mean
                // the client is actually connected - yet. Use the connectComplete() callback
                // to determine result of connectClient().
                return true;
            } else {
                _Logger.warning("MQTT client already connected. Ignoring connect request.");
                return false;
            }
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to connect MQTT client to broker.", e);
            return false;
        }
    }
    
    @Override
    public boolean disconnectClient()
    {
        try {
            if (this.mqttClient != null && this.mqttClient.isConnected()) {
                _Logger.info("Disconnecting MQTT client from broker: " + this.host);
                
                this.mqttClient.disconnect();
                
                return true;
            } else {
                _Logger.warning("MQTT client already disconnected. Ignoring disconnect request.");
                return false;
            }
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to disconnect MQTT client from broker.", e);
            return false;
        }
    }
    
    public boolean isConnected()
    {
        return (this.mqttClient != null && this.mqttClient.isConnected());
    }
    
    @Override
    public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
    {
        if (topicName == null) {
            _Logger.warning("Resource is null. Unable to publish message.");
            return false;
        }
        
        if (msg == null || msg.length() == 0) {
            _Logger.warning("Message is null or empty. Unable to publish message to topic: " + topicName.getResourceName());
            return false;
        }
        
        // Validate and adjust QoS if needed
        if (qos < 0 || qos > 2) {
            _Logger.warning("Invalid QoS: " + qos + ". Using default QoS.");
            qos = ConfigConst.DEFAULT_QOS;
        }
        
        try {
            // PERFORMANCE TESTING: Comment out during performance tests
            // _Logger.info("Publishing message to topic: " + topicName.getResourceName() + " with QoS: " + qos);
            
            String topic = topicName.getResourceName();
            
            MqttMessage mqttMsg = new MqttMessage(msg.getBytes());
            mqttMsg.setQos(qos);
            
            // Publish message - asynchronous call
            this.mqttClient.publish(topic, mqttMsg);
            
            return true;
        } catch (MqttPersistenceException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message due to persistence error.", e);
            return false;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message to topic: " + topicName.getResourceName(), e);
            return false;
        }
    }
    
    @Override
    public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
    {
        if (topicName == null) {
            _Logger.warning("Resource is null. Unable to subscribe.");
            return false;
        }
        
        // Validate and adjust QoS if needed
        if (qos < 0 || qos > 2) {
            _Logger.warning("Invalid QoS: " + qos + ". Using default QoS.");
            qos = ConfigConst.DEFAULT_QOS;
        }
        
        try {
            String topic = topicName.getResourceName();
            
            _Logger.info("Subscribing to topic: " + topic + " with QoS: " + qos);
            
            this.mqttClient.subscribe(topic, qos);
            
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName.getResourceName(), e);
            return false;
        }
    }
    
    @Override
    public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
    {
        if (topicName == null) {
            _Logger.warning("Resource is null. Unable to unsubscribe.");
            return false;
        }
        
        try {
            String topic = topicName.getResourceName();
            
            _Logger.info("Unsubscribing from topic: " + topic);
            
            this.mqttClient.unsubscribe(topic);
            
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to unsubscribe from topic: " + topicName.getResourceName(), e);
            return false;
        }
    }
    
    @Override
    public boolean setConnectionListener(IConnectionListener listener)
    {
        // Not implemented for this version
        return false;
    }
    
    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            _Logger.info("Data message listener set.");
            return true;
        } else {
            _Logger.warning("No data message listener provided.");
            return false;
        }
    }
    
    
    // Callback methods - MqttCallbackExtended implementation
    
    @Override
    public void connectComplete(boolean reconnect, String serverURI)
    {
        _Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " + serverURI);
        
        int qos = ConfigConst.DEFAULT_QOS;
        
        // Subscribe to CDA topics
        this.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
        this.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
        this.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
        
        _Logger.info("Subscribed to all CDA topics.");
    }
    
    @Override
    public void connectionLost(Throwable cause)
    {
        _Logger.log(Level.WARNING, "MQTT connection lost. Cause: " + cause.getMessage(), cause);
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {
        // PERFORMANCE TESTING: Comment out during performance tests
        // try {
        //     _Logger.fine("Message delivery complete: " + token.getMessage());
        // } catch (Exception e) {
        //     _Logger.warning("Error getting message from delivery token");
        // }
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        try {
            String payload = new String(message.getPayload());
            int qos = message.getQos();
            
            _Logger.info("MQTT message received on topic: " + topic);
            _Logger.fine("Message payload: " + payload);
            _Logger.fine("Message QoS: " + qos);
            
            // Delegate to data message listener if set
            if (this.dataMsgListener != null) {
                // Find matching ResourceNameEnum
                ResourceNameEnum resourceEnum = null;
                
                for (ResourceNameEnum resource : ResourceNameEnum.values()) {
                    if (resource.getResourceName().equals(topic)) {
                        resourceEnum = resource;
                        break;
                    }
                }
                
                if (resourceEnum != null) {
                    // Route to appropriate handler based on resource type
                    String resourceName = resourceEnum.getResourceName();
                    
                    if (resourceName.contains("SensorMsg") || resourceName.contains("SensorData")) {
                        _Logger.info("Handling sensor data message...");
                        SensorData sensorData = DataUtil.getInstance().jsonToSensorData(payload);
                        // ADD resourceEnum as first parameter
                        this.dataMsgListener.handleSensorMessage(resourceEnum, sensorData);
                    }
                    else if (resourceName.contains("ActuatorResponse")) {
                        _Logger.info("Handling actuator response message...");
                        ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(payload);
                        // ADD resourceEnum as first parameter
                        this.dataMsgListener.handleActuatorCommandResponse(resourceEnum, actuatorData);
                    }
                    else if (resourceName.contains("SystemPerf")) {
                        _Logger.info("Handling system performance message...");
                        SystemPerformanceData sysPerfData = DataUtil.getInstance().jsonToSystemPerformanceData(payload);
                        // ADD resourceEnum as first parameter
                        this.dataMsgListener.handleSystemPerformanceMessage(resourceEnum, sysPerfData);
                    }
                    else {
                        _Logger.info("Handling generic incoming message...");
                        this.dataMsgListener.handleIncomingMessage(resourceEnum, payload);
                    }
                } else {
                    _Logger.warning("Unknown topic received: " + topic);
                }
            } else {
                _Logger.fine("No data message listener set. Message not delegated.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Error processing incoming MQTT message.", e);
            throw e;
        }
    }
    
    
    // Private methods
    
    /**
     * Called by the constructor to set the MQTT client parameters.
     */
    private void initClientParameters(String configSectionName)
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        // Load basic connection parameters
        this.host = 
            configUtil.getProperty(
                configSectionName, 
                ConfigConst.HOST_KEY, 
                ConfigConst.DEFAULT_HOST);
        
        this.port = 
            configUtil.getInteger(
                configSectionName, 
                ConfigConst.PORT_KEY, 
                ConfigConst.DEFAULT_MQTT_PORT);
        
        this.brokerKeepAlive = 
            configUtil.getInteger(
                configSectionName, 
                ConfigConst.KEEP_ALIVE_KEY, 
                ConfigConst.DEFAULT_KEEP_ALIVE);
        
        this.enableEncryption = 
            configUtil.getBoolean(
                configSectionName, 
                ConfigConst.ENABLE_CRYPT_KEY);
        
        this.useCleanSession = 
            configUtil.getBoolean(
                configSectionName, 
                ConfigConst.USE_CLEAN_SESSION_KEY);
        
        this.pemFileName = 
            configUtil.getProperty(
                configSectionName, 
                ConfigConst.CERT_FILE_KEY);
        
        // Set client ID if not already set
        if (this.clientID == null || this.clientID.trim().length() == 0) {
            this.clientID = 
                configUtil.getProperty(
                    ConfigConst.GATEWAY_DEVICE, 
                    ConfigConst.DEVICE_LOCATION_ID_KEY, 
                    "GDAMqttClient");  // Default for async client
        }
        
        // If encryption enabled, adjust port if needed
        if (this.enableEncryption) {
            int securePort = 
                configUtil.getInteger(
                    configSectionName, 
                    ConfigConst.SECURE_PORT_KEY, 
                    ConfigConst.DEFAULT_MQTT_SECURE_PORT);
            
            if (this.port != securePort) {
                _Logger.info("Encryption enabled. Updating port to secure port: " + securePort);
                this.port = securePort;
            }
            
            this.host = "ssl://" + this.host;
        } else {
            this.host = "tcp://" + this.host;
        }
        
        // Create connection options
        this.connOpts = new MqttConnectOptions();
        this.connOpts.setCleanSession(this.useCleanSession);
        this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
        this.connOpts.setAutomaticReconnect(this.enableAutoReconnect);
        
        // Initialize credentials if authentication enabled
        initCredentialConnectionParameters(configSectionName);
        
        // Initialize TLS/SSL if encryption enabled
        if (this.enableEncryption) {
            initSecureConnectionParameters(configSectionName);
        }
        
        _Logger.info("MQTT Client connector initialized.");
        _Logger.info("\tHost: " + this.host);
        _Logger.info("\tPort: " + this.port);
        _Logger.info("\tClient ID: " + this.clientID);
        _Logger.info("\tKeep Alive: " + this.brokerKeepAlive);
        _Logger.info("\tClean Session: " + this.useCleanSession);
        _Logger.info("\tEncryption Enabled: " + this.enableEncryption);
    }
    
    /**
     * Initialize credential connection parameters
     */
    private void initCredentialConnectionParameters(String configSectionName)
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        boolean enableAuth = 
            configUtil.getBoolean(
                configSectionName, 
                ConfigConst.ENABLE_AUTH_KEY);
        
        if (enableAuth) {
            String userName = 
                configUtil.getProperty(
                    configSectionName, 
                    ConfigConst.USER_NAME_KEY);
            
            String userPassword = 
                configUtil.getProperty(
                    configSectionName, 
                    ConfigConst.USER_AUTH_TOKEN_KEY);
            
            if (userName != null && userPassword != null) {
                this.connOpts.setUserName(userName);
                this.connOpts.setPassword(userPassword.toCharArray());
                
                _Logger.info("MQTT authentication enabled with username: " + userName);
            } else {
                _Logger.warning("MQTT authentication enabled but credentials not properly configured.");
            }
        }
    }
    
    /**
     * Initialize secure connection parameters
     */
    private void initSecureConnectionParameters(String configSectionName)
    {
        try {
            _Logger.info("Configuring TLS...");
            
            if (this.pemFileName != null && this.pemFileName.trim().length() > 0) {
                File pemFile = new File(this.pemFileName);
                
                if (pemFile.exists()) {
                    _Logger.info("PEM file found: " + this.pemFileName);
                    
                    SimpleCertManagementUtil certUtil = SimpleCertManagementUtil.getInstance();
                    
                    SSLSocketFactory sslFactory = 
                        certUtil.loadCertificate(this.pemFileName);
                    
                    this.connOpts.setSocketFactory(sslFactory);
                    
                    _Logger.info("TLS configuration complete.");
                } else {
                    _Logger.warning("PEM file not found: " + this.pemFileName + ". Using default TLS configuration.");
                }
            } else {
                _Logger.warning("No PEM file specified. Using default TLS configuration.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to initialize secure connection parameters.", e);
        }
    }
}
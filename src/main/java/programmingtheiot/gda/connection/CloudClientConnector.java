package programmingtheiot.gda.connection;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Cloud client connector for MQTT-based cloud communication.
 * Supports Ubidots cloud platform using MQTT v2.0 protocol.
 */
public class CloudClientConnector implements ICloudClient, MqttCallback
{
    private static final Logger _Logger =
        Logger.getLogger(CloudClientConnector.class.getName());

    private MqttClient mqttClient = null;
    private MqttConnectOptions connOptions = null;

    private String host = null;
    private int port = ConfigConst.DEFAULT_MQTT_PORT;
    private String brokerURI = null;
    private String clientID = null;
    private int qos = ConfigConst.DEFAULT_QOS;

    private boolean isConnected = false;
    private IDataMessageListener dataMsgListener = null;

    // Device label for Ubidots
    private static final String DEVICE_LABEL = "gda-device-01";

    // -------------------------------------------------
    // Constructors
    // -------------------------------------------------

    /**
     * Default constructor - uses CLOUD_GATEWAY_SERVICE configuration section
     */
    public CloudClientConnector()
    {
        this(ConfigConst.CLOUD_GATEWAY_SERVICE);
    }

    /**
     * Constructor with custom configuration section
     * 
     * @param configSection The configuration section to use
     */
    public CloudClientConnector(String configSection)
    {
        super();

        ConfigUtil configUtil = ConfigUtil.getInstance();

        this.host = configUtil.getProperty(
            configSection,
            ConfigConst.HOST_KEY,
            ConfigConst.DEFAULT_HOST);

        this.port = configUtil.getInteger(
            configSection,
            ConfigConst.PORT_KEY,
            ConfigConst.DEFAULT_MQTT_PORT);

        this.clientID = MqttClient.generateClientId();

        this.qos = configUtil.getInteger(
            configSection,
            ConfigConst.DEFAULT_QOS_KEY,
            ConfigConst.DEFAULT_QOS);

        // Build broker URI with protocol
        this.brokerURI = "tcp://" + this.host + ":" + this.port;

        try {
            this.mqttClient = new MqttClient(this.brokerURI, this.clientID);
            this.mqttClient.setCallback(this);

            this.connOptions = new MqttConnectOptions();
            this.connOptions.setCleanSession(true);

            // Load authentication if enabled
            boolean enableAuth = configUtil.getBoolean(
                configSection,
                ConfigConst.ENABLE_AUTH_KEY);

            if (enableAuth) {
                _Logger.info("Authentication enabled - loading credentials");
                
                String credFile = configUtil.getProperty(
                    configSection,
                    ConfigConst.CRED_FILE_KEY);
                
                if (credFile != null && !credFile.isEmpty()) {
                    _Logger.info("Loading credentials from file: " + credFile);
                    
                    // Load credentials from separate file
                    try {
                        java.util.Properties credProps = new java.util.Properties();
                        java.io.FileInputStream fis = new java.io.FileInputStream(credFile);
                        credProps.load(fis);
                        fis.close();
                        
                        // Get username and password from the properties
                        String userName = credProps.getProperty("cloud.mqtt.userName");
                        String userPw = credProps.getProperty("cloud.mqtt.userPassword");
                        
                        if (userName != null && !userName.isEmpty()) {
                            _Logger.info("Setting MQTT authentication with username: " + 
                                userName.substring(0, Math.min(8, userName.length())) + "...");
                            this.connOptions.setUserName(userName);
                            
                            if (userPw != null && !userPw.isEmpty()) {
                                this.connOptions.setPassword(userPw.toCharArray());
                                _Logger.info("Password set from credentials");
                            } else {
                                this.connOptions.setPassword(new char[0]); // Empty password
                                _Logger.info("Using empty password");
                            }
                        } else {
                            _Logger.warning("Authentication enabled but username not found in credentials file");
                        }
                    } catch (Exception e) {
                        _Logger.log(Level.WARNING, "Failed to load credentials file: " + credFile, e);
                    }
                } else {
                    _Logger.warning("Authentication enabled but credential file path not configured");
                }
            } else {
                _Logger.info("Authentication disabled");
            }

            _Logger.info("CloudClientConnector initialized with broker: " + this.brokerURI);

        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to initialize Cloud MQTT client", e);
        }
    }

    // -------------------------------------------------
    // ICloudClient implementation
    // -------------------------------------------------

    /**
     * Connects to the cloud MQTT broker
     * 
     * @return true if connection successful, false otherwise
     */
    @Override
    public boolean connectClient()
    {
        try {
            if (!this.mqttClient.isConnected()) {
                _Logger.info("Connecting to cloud broker: " + this.brokerURI);
                this.mqttClient.connect(this.connOptions);
                this.isConnected = true;
                _Logger.info("Connected to Cloud MQTT Broker.");
            }
            return true;

        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Cloud MQTT connection failed", e);
            return false;
        }
    }

    /**
     * Disconnects from the cloud MQTT broker
     * 
     * @return true if disconnection successful, false otherwise
     */
    @Override
    public boolean disconnectClient()
    {
        try {
            if (this.mqttClient.isConnected()) {
                this.mqttClient.disconnect();
                this.isConnected = false;
                _Logger.info("Disconnected from Cloud MQTT Broker.");
            }
            return true;

        } catch (MqttException e) {
            _Logger.log(Level.WARNING, "Cloud MQTT disconnect failed", e);
            return false;
        }
    }

    /**
     * Checks if the client is currently connected
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected()
    {
        return (this.mqttClient != null && this.mqttClient.isConnected());
    }

    /**
     * Sets the data message listener for handling incoming messages
     * 
     * @param listener The data message listener
     * @return true if listener set successfully, false otherwise
     */
    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            return true;
        }
        return false;
    }

    /**
     * Sends sensor data to the cloud using Ubidots v2.0 MQTT protocol
     * 
     * @param resource The resource name enum
     * @param data The sensor data to send
     * @return true if publish successful, false otherwise
     */
    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
    {
        if (data != null) {
            // Ubidots MQTT v2.0 topic format: /v2.0/devices/{device-label}/{variable-label}
            String variableLabel = data.getName().toLowerCase().replace(" ", "-");
            String topic = "/v2.0/devices/" + DEVICE_LABEL + "/" + variableLabel;
            
            // Ubidots v2.0 accepts simple value format (most reliable)
            String payload = String.valueOf(data.getValue());
            
            _Logger.info("Publishing sensor data to topic: " + topic + " | Value: " + payload);
            
            return publish(topic, payload);
        }
        return false;
    }

    /**
     * Sends system performance data to the cloud using Ubidots v2.0 MQTT protocol
     * 
     * @param resource The resource name enum
     * @param data The system performance data to send
     * @return true if publish successful, false otherwise
     */
    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
    {
        if (data != null) {
            // Publish CPU utilization
            String cpuTopic = "/v2.0/devices/" + DEVICE_LABEL + "/cpu-utilization";
            String cpuPayload = String.valueOf(data.getCpuUtilization());
            _Logger.info("Publishing CPU data to topic: " + cpuTopic + " | Value: " + cpuPayload);
            boolean cpuSuccess = publish(cpuTopic, cpuPayload);
            
            // Publish Memory utilization
            String memTopic = "/v2.0/devices/" + DEVICE_LABEL + "/memory-utilization";
            String memPayload = String.valueOf(data.getMemoryUtilization());
            _Logger.info("Publishing Memory data to topic: " + memTopic + " | Value: " + memPayload);
            boolean memSuccess = publish(memTopic, memPayload);
            
            return (cpuSuccess && memSuccess);
        }
        return false;
    }

    /**
     * Internal method to publish a message to a topic
     * 
     * @param topic The MQTT topic
     * @param payload The message payload
     * @return true if publish successful, false otherwise
     */
    private boolean publish(String topic, String payload)
    {
        if (!this.isConnected || payload == null) {
            _Logger.warning("Cannot publish - not connected or payload is null");
            return false;
        }

        try {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(this.qos);
            this.mqttClient.publish(topic, msg);
            
            _Logger.info("✅ Successfully published to cloud: " + topic);

            return true;

        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Publish to cloud failed for topic: " + topic, e);
            return false;
        }
    }

    /**
     * Subscribes to cloud events/messages using Ubidots v2.0 protocol
     * 
     * @param resource The resource name enum
     * @return true if subscription successful, false otherwise
     */
    @Override
    public boolean subscribeToCloudEvents(ResourceNameEnum resource)
    {
        try {
            if (this.mqttClient.isConnected()) {
                // Ubidots v2.0 topic format for receiving data
                // Using wildcard to subscribe to all variables under this device
                String topic = "/v2.0/devices/" + DEVICE_LABEL + "/+/lv";
                
                this.mqttClient.subscribe(topic, this.qos);
                _Logger.info("Subscribed to cloud topic: " + topic);
                return true;
            } else {
                _Logger.warning("Cannot subscribe - client not connected");
            }

        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Cloud subscription failed", e);
        }

        return false;
    }

    /**
     * Unsubscribes from cloud events/messages
     * 
     * @param resource The resource name enum
     * @return true if unsubscription successful, false otherwise
     */
    @Override
    public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
    {
        try {
            if (this.mqttClient.isConnected()) {
                // Unsubscribe from the same topic pattern used in subscribe
                String topic = "/v2.0/devices/" + DEVICE_LABEL + "/+/lv";
                
                this.mqttClient.unsubscribe(topic);
                _Logger.info("Unsubscribed from cloud topic: " + topic);
                return true;
            } else {
                _Logger.warning("Cannot unsubscribe - client not connected");
            }
        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Cloud unsubscribe failed", e);
        }

        return false;
    }

    // -------------------------------------------------
    // MqttCallback implementation
    // -------------------------------------------------

    /**
     * Callback for when connection is lost
     * 
     * @param cause The cause of connection loss
     */
    @Override
    public void connectionLost(Throwable cause)
    {
        this.isConnected = false;
        _Logger.warning("Cloud MQTT connection lost: " + cause.getMessage());
    }

    /**
     * Callback for when a message arrives
     * 
     * @param topic The topic the message was received on
     * @param message The MQTT message
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        _Logger.info("Cloud message received on topic: " + topic);
        _Logger.info("Payload: " + payload);

        if (this.dataMsgListener == null) {
            _Logger.warning("No DataMessageListener registered.");
            return;
        }

        // Try to parse as ActuatorData (for cloud commands)
        try {
            ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(payload);
            if (actuatorData != null) {
                this.dataMsgListener.handleActuatorCommandRequest(
                    ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, actuatorData);
            }
        } catch (Exception e) {
            _Logger.fine("Message not ActuatorData, ignoring: " + e.getMessage());
        }
    }

    /**
     * Callback for when message delivery is complete
     * 
     * @param token The delivery token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {
        _Logger.fine("Cloud delivery complete.");
    }
}
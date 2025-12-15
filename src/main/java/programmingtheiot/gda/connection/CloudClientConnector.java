/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Cloud client connector for Ubidots using MQTT protocol.
 * Supports personal/educational Ubidots accounts.
 * 
 * @author Emma
 */
package programmingtheiot.gda.connection;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Cloud client connector for Ubidots MQTT integration.
 */
public class CloudClientConnector implements ICloudClient, MqttCallback
{
    // Static
    
    private static final Logger _Logger =
        Logger.getLogger(CloudClientConnector.class.getName());
    
    // Private variables
    
    private ConfigUtil configUtil = null;
    private MqttClient mqttClient = null;
    private MqttConnectOptions connOpts = null;
    private MemoryPersistence persistence = null;
    private IDataMessageListener dataMsgListener = null;
    
    // Connection parameters
    private String host = null;
    private String clientID = null;
    private String brokerAddr = null;
    private String deviceLabel = null;
    private String token = null;
    private int port = 0;
    private int keepAlive = 60;
    private int qos = 1;
    
    private boolean enableEncryption = true;
    
    private Gson gson = null;
    
    // Constructor
    
    /**
     * Default constructor.
     * Initializes connection parameters from configuration.
     */
    public CloudClientConnector()
    {
        super();
        
        this.configUtil = ConfigUtil.getInstance();
        this.gson = new Gson();
        
        initClientParameters();
    }
    
    
    // Public methods - ICloudClient implementation
    
    @Override
    public boolean connectClient()
    {
        try {
            if (this.mqttClient == null) {
                _Logger.warning("MQTT client is null. Cannot connect.");
                return false;
            }
            
            if (this.mqttClient.isConnected()) {
                _Logger.warning("MQTT client already connected to Ubidots.");
                return true;
            }
            
            _Logger.info("Connecting to Ubidots cloud: " + this.brokerAddr);
            
            this.mqttClient.connect(this.connOpts);
            
            if (this.mqttClient.isConnected()) {
                _Logger.info("Successfully connected to Ubidots cloud.");
                return true;
            } else {
                _Logger.warning("Failed to connect to Ubidots cloud.");
                return false;
            }
            
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to connect to Ubidots cloud.", e);
            return false;
        }
    }
    
    @Override
    public boolean disconnectClient()
    {
        try {
            if (this.mqttClient != null && this.mqttClient.isConnected()) {
                _Logger.info("Disconnecting from Ubidots cloud...");
                this.mqttClient.disconnect();
                _Logger.info("Disconnected from Ubidots cloud.");
                return true;
            } else {
                _Logger.warning("MQTT client not connected. Nothing to disconnect.");
                return false;
            }
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to disconnect from Ubidots.", e);
            return false;
        }
    }
    
    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            _Logger.info("Data message listener set for cloud client.");
            return true;
        } else {
            _Logger.warning("Data message listener is null.");
            return false;
        }
    }
    
    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
    {
        if (data == null) {
            _Logger.warning("SensorData is null. Cannot send to cloud.");
            return false;
        }
        
        // Convert SensorData to JSON for Ubidots
        String jsonData = DataUtil.getInstance().sensorDataToJson(data);
        
        return publishMessage(resource, jsonData, this.qos);
    }
    
    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
    {
        if (data == null) {
            _Logger.warning("SystemPerformanceData is null. Cannot send to cloud.");
            return false;
        }
        
        // Convert SystemPerformanceData to JSON for Ubidots
        String jsonData = DataUtil.getInstance().systemPerformanceDataToJson(data);
        
        return publishMessage(resource, jsonData, this.qos);
    }
    
    /**
     * Generic publish method that accepts JSON string.
     * Used by DeviceDataManager to send data to Ubidots.
     */
    public boolean publishMessage(ResourceNameEnum resource, String jsonData, int qos)
    {
        if (!isConnected()) {
            _Logger.warning("Not connected to Ubidots. Cannot publish message.");
            return false;
        }
        
        try {
            // For Ubidots personal account: single topic with JSON payload
            String topic = "/v1.6/devices/" + this.deviceLabel;
            
            // Convert the incoming JSON to Ubidots format
            String ubidotsPayload = convertToUbidotsFormat(jsonData);
            
            MqttMessage mqttMsg = new MqttMessage(ubidotsPayload.getBytes());
            mqttMsg.setQos(qos);
            
            this.mqttClient.publish(topic, mqttMsg);
            
            _Logger.info("Published message to Ubidots: " + topic);
            _Logger.fine("Payload: " + ubidotsPayload);
            
            return true;
            
        } catch (MqttPersistenceException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message (persistence error).", e);
            return false;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message to Ubidots.", e);
            return false;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Unexpected error publishing to Ubidots.", e);
            return false;
        }
    }
    
    @Override
    public boolean subscribeToCloudEvents(ResourceNameEnum resource)
    {
        // Subscribe to commands from Ubidots (optional for Lab 12)
        try {
            if (!isConnected()) {
                _Logger.warning("Not connected to Ubidots. Cannot subscribe.");
                return false;
            }
            
            String topic = "/v1.6/devices/" + this.deviceLabel + "/commands";
            
            this.mqttClient.subscribe(topic, this.qos);
            _Logger.info("Subscribed to Ubidots commands: " + topic);
            
            return true;
            
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to subscribe to Ubidots.", e);
            return false;
        }
    }
    
    @Override
    public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
    {
        try {
            if (!isConnected()) {
                _Logger.warning("Not connected to Ubidots. Cannot unsubscribe.");
                return false;
            }
            
            String topic = "/v1.6/devices/" + this.deviceLabel + "/commands";
            
            this.mqttClient.unsubscribe(topic);
            _Logger.info("Unsubscribed from Ubidots commands: " + topic);
            
            return true;
            
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to unsubscribe from Ubidots.", e);
            return false;
        }
    }
    
    /**
     * Check if connected to Ubidots.
     */
    public boolean isConnected()
    {
        return (this.mqttClient != null && this.mqttClient.isConnected());
    }
    
    
    // MqttCallback implementation
    
    @Override
    public void connectionLost(Throwable cause)
    {
        _Logger.log(Level.WARNING, "Connection to Ubidots lost.", cause);
        
        // TODO: Implement reconnection logic if needed
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        _Logger.info("Message arrived from Ubidots on topic: " + topic);
        _Logger.info("Message: " + new String(message.getPayload()));
        
        // Handle incoming commands from Ubidots if needed
        if (this.dataMsgListener != null) {
            String payload = new String(message.getPayload());
            // Parse and forward to listener
            this.dataMsgListener.handleIncomingMessage(
                ResourceNameEnum.CLOUD_INCOMING_MSG_RESOURCE, payload);
        }
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {
        _Logger.fine("Message delivery to Ubidots complete.");
    }
    
    
    // Private methods
    
/**
 * Initialize MQTT client parameters from configuration.
 */
private void initClientParameters()
{
    // Load cloud service configuration
    String cloudSection = "Cloud.GatewayService";
    
    this.host = this.configUtil.getProperty(cloudSection, "host");
    
    // Read port with fallback
    try {
        this.port = this.configUtil.getInteger(cloudSection, "port");
    } catch (Exception e) {
        try {
            this.port = this.configUtil.getInteger(cloudSection, "securePort");
        } catch (Exception e2) {
            this.port = 1883;
            _Logger.info("Port not found in config. Using default: 1883");
        }
    }
    
    // Device label
    this.deviceLabel = this.configUtil.getProperty(cloudSection, "deviceLabel");
    
    // Token
    this.token = this.configUtil.getProperty(cloudSection, "userName");
    
    // Keep alive with fallback
    try {
        this.keepAlive = this.configUtil.getInteger(cloudSection, "keepAlive");
    } catch (Exception e) {
        this.keepAlive = 60;
        _Logger.info("keepAlive not found in config. Using default: 60");
    }
    
    // QoS with fallback
    try {
        this.qos = this.configUtil.getInteger(cloudSection, "defaultQoS");
    } catch (Exception e) {
        this.qos = 0;
        _Logger.info("defaultQoS not found in config. Using default: 0");
    }
    
    // Validate critical parameters
    if (this.host == null || this.host.isEmpty()) {
        _Logger.severe("Cloud host is null or empty! Check PiotConfig.props [Cloud.GatewayService] section.");
        this.host = "industrial.api.ubidots.com"; // Changed fallback
    }
    
    if (this.deviceLabel == null || this.deviceLabel.isEmpty()) {
        _Logger.severe("Device label is null or empty! Check PiotConfig.props.");
        this.deviceLabel = "default-device";
    }
    
    if (this.token == null || this.token.isEmpty()) {
        _Logger.severe("Ubidots token is null or empty! Cannot connect to cloud.");
    }
    
    // Use device label as client ID
    this.clientID = this.deviceLabel + "-gda";
    
    // Build broker address - use tcp:// not ssl:// for STEM/Educational accounts
    String protocol = "tcp://";
    this.brokerAddr = protocol + this.host + ":" + this.port;
    
    _Logger.info("Cloud client parameters initialized:");
    _Logger.info("\tHost: " + this.host);
    _Logger.info("\tPort: " + this.port);
    _Logger.info("\tBroker: " + this.brokerAddr);
    _Logger.info("\tDevice Label: " + this.deviceLabel);
    _Logger.info("\tClient ID: " + this.clientID);
    _Logger.info("\tKeep Alive: " + this.keepAlive);
    _Logger.info("\tQoS: " + this.qos);
    
    // Initialize MQTT client
    try {
        this.persistence = new MemoryPersistence();
        this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
        this.mqttClient.setCallback(this);
        
        // Configure connection options
        this.connOpts = new MqttConnectOptions();
        this.connOpts.setCleanSession(true);
        this.connOpts.setKeepAliveInterval(this.keepAlive);
        this.connOpts.setUserName(this.token);
        this.connOpts.setPassword("".toCharArray());
        this.connOpts.setAutomaticReconnect(true);
        
        _Logger.info("MQTT client created successfully for Ubidots.");
        
    } catch (MqttException e) {
        _Logger.log(Level.SEVERE, "Failed to create MQTT client for Ubidots.", e);
    }
}
  
/**
 * Convert JSON data to Ubidots-compatible format.
 * Ubidots expects: {"variable_name": value, "variable_name": value}
 */
private String convertToUbidotsFormat(String jsonData)
{
    try {
        // Parse incoming JSON
        JsonObject incomingData = gson.fromJson(jsonData, JsonObject.class);
        JsonObject ubidotsPayload = new JsonObject();
        
        // Extract relevant fields and create Ubidots payload
        if (incomingData.has("name")) {
            String sensorType = incomingData.get("name").getAsString().toLowerCase();
            
            if (incomingData.has("value")) {
                float value = incomingData.get("value").getAsFloat();
                
                // Map sensor types to Ubidots variable names
                if (sensorType.contains("temp")) {
                    ubidotsPayload.addProperty("temperature", value);
                } else if (sensorType.contains("humid")) {
                    ubidotsPayload.addProperty("humidity", value);
                } else if (sensorType.contains("press")) {
                    ubidotsPayload.addProperty("pressure", value);
                } else if (sensorType.contains("yeast")) {
                    // Yeast bin status (0 = closed, 1 = open)
                    ubidotsPayload.addProperty("yeast_bin_status", value);
                }
            }
            
            // Add stateData as text variable for yeast bin messages
            if (sensorType.contains("yeast") && incomingData.has("stateData")) {
                String statusMessage = incomingData.get("stateData").getAsString();
                ubidotsPayload.addProperty("yeast_bin_message", statusMessage);
            }
        }
        
        // Add system performance data
        if (incomingData.has("cpuUtil")) {
            ubidotsPayload.addProperty("cpu_utilization", 
                incomingData.get("cpuUtil").getAsFloat());
        }
        if (incomingData.has("memUtil")) {
            ubidotsPayload.addProperty("memory_utilization", 
                incomingData.get("memUtil").getAsFloat());
        }
        
        // Add actuator data (typeID helps identify actuator type)
        if (incomingData.has("typeID")) {
            int typeID = incomingData.get("typeID").getAsInt();
            
            // Yeast bin actuator
            if (typeID == ConfigConst.YEAST_BIN_ACTUATOR_TYPE) {
                if (incomingData.has("value")) {
                    ubidotsPayload.addProperty("yeast_bin_status", 
                        incomingData.get("value").getAsFloat());
                }
                if (incomingData.has("stateData")) {
                    ubidotsPayload.addProperty("yeast_bin_message", 
                        incomingData.get("stateData").getAsString());
                }
            }
            
            // HVAC actuator status
            if (typeID == ConfigConst.HVAC_ACTUATOR_TYPE) {
                if (incomingData.has("command")) {
                    ubidotsPayload.addProperty("hvac_status", 
                        incomingData.get("command").getAsInt());
                }
                if (incomingData.has("stateData")) {
                    ubidotsPayload.addProperty("hvac_command", 
                        incomingData.get("stateData").getAsString());
                }
            }
            
            // Humidifier actuator status
            if (typeID == ConfigConst.HUMIDIFIER_ACTUATOR_TYPE) {
                if (incomingData.has("command")) {
                    ubidotsPayload.addProperty("humidifier_status", 
                        incomingData.get("command").getAsInt());
                }
            }
        }
        
        // If no data was extracted, return empty object
        if (ubidotsPayload.size() == 0) {
            _Logger.warning("No valid data to convert to Ubidots format.");
            ubidotsPayload.addProperty("status", "no_data");
        }
        
        return gson.toJson(ubidotsPayload);
        
    } catch (Exception e) {
        _Logger.log(Level.WARNING, "Failed to convert to Ubidots format. Returning original.", e);
        return jsonData;
    }
}

}
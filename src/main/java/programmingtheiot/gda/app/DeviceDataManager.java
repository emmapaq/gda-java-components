package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;

/**
 * DeviceDataManager orchestrates data flow between the CDA and various
 * cloud services, managing MQTT, CoAP, and Redis persistence.
 */
public class DeviceDataManager implements IDataMessageListener {
    
    private static final Logger _Logger = 
        Logger.getLogger(DeviceDataManager.class.getName());
    
    // Redis persistence client
    private RedisPersistenceAdapter redisClient = null;
    
    // MQTT client connector
    private MqttClientConnector mqttClient = null;
    
    // CoAP server gateway
    private CoapServerGateway coapServer = null;
    
    // Configuration
    private ConfigUtil configUtil = ConfigUtil.getInstance();
    private DataUtil dataUtil = DataUtil.getInstance();
    private boolean enablePersistence = false;
    private boolean enableMqttClient = false;
    private boolean enableCoapServer = false;
    
    /**
     * Constructor.
     * Initializes the DeviceDataManager with configured components.
     */
    public DeviceDataManager() {
        super();
        
        // Initialize Redis persistence client
        this.redisClient = new RedisPersistenceAdapter();
        
        // Check if persistence is enabled
        this.enablePersistence = configUtil.getBoolean(
            "gateway.GatewayDeviceApp",
            "enablePersistence");
        
        // Check if MQTT client is enabled
        this.enableMqttClient = configUtil.getBoolean(
            "gateway.GatewayDeviceApp",
            "enableMqttClient");
        
        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            this.mqttClient.setDataMessageListener(this);
        }
        
        // Check if CoAP server is enabled
        this.enableCoapServer = configUtil.getBoolean(
            "gateway.GatewayDeviceApp",
            "enableCoapServer");
        
        if (this.enableCoapServer) {
            this.coapServer = new CoapServerGateway(this);
        }
        
        _Logger.info("DeviceDataManager initialized.");
        _Logger.info("Persistence enabled: " + this.enablePersistence);
        _Logger.info("MQTT client enabled: " + this.enableMqttClient);
        _Logger.info("CoAP server enabled: " + this.enableCoapServer);
    }
    
    // ========================================================================
    // Public methods
    // ========================================================================
    
    /**
     * Starts the DeviceDataManager and all enabled components.
     */
    public void startManager() {
        _Logger.info("Starting DeviceDataManager...");
        
        // Connect Redis client if persistence is enabled
        if (this.enablePersistence && this.redisClient != null) {
            if (this.redisClient.connectClient()) {
                _Logger.info("Redis persistence client connected successfully.");
            } else {
                _Logger.warning("Failed to connect Redis persistence client.");
            }
        }
        
        // Start MQTT client if enabled
        if (this.enableMqttClient && this.mqttClient != null) {
            if (this.mqttClient.connectClient()) {
                _Logger.info("MQTT client connected successfully.");
            } else {
                _Logger.warning("Failed to connect MQTT client.");
            }
        }
        
        // Start CoAP server if enabled
        if (this.enableCoapServer && this.coapServer != null) {
            if (this.coapServer.startServer()) {
                _Logger.info("CoAP server started successfully.");
            } else {
                _Logger.warning("Failed to start CoAP server.");
            }
        }
        
        _Logger.info("DeviceDataManager started.");
    }
    
    /**
     * Stops the DeviceDataManager and all enabled components.
     */
    public void stopManager() {
        _Logger.info("Stopping DeviceDataManager...");
        
        // Disconnect MQTT client if enabled
        if (this.enableMqttClient && this.mqttClient != null) {
            if (this.mqttClient.disconnectClient()) {
                _Logger.info("MQTT client disconnected successfully.");
            } else {
                _Logger.warning("Failed to disconnect MQTT client.");
            }
        }
        
        // Stop CoAP server if enabled
        if (this.enableCoapServer && this.coapServer != null) {
            if (this.coapServer.stopServer()) {
                _Logger.info("CoAP server stopped successfully.");
            } else {
                _Logger.warning("Failed to stop CoAP server.");
            }
        }
        
        // Disconnect Redis client if persistence is enabled
        if (this.enablePersistence && this.redisClient != null) {
            if (this.redisClient.disconnectClient()) {
                _Logger.info("Redis persistence client disconnected successfully.");
            } else {
                _Logger.warning("Failed to disconnect Redis persistence client.");
            }
        }
        
        _Logger.info("DeviceDataManager stopped.");
    }
    
    // ========================================================================
    // IDataMessageListener implementation
    // ========================================================================
    
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
        if (data != null) {
            _Logger.info("Handling actuator command response: " + data.getName());
            
            // Store in Redis if persistence is enabled
            if (this.enablePersistence && this.redisClient != null) {
                try {
                    String topic = resourceName.getResourceName();
                    boolean success = this.redisClient.storeData(topic, 0, data);
                    
                    if (success) {
                        _Logger.info("ActuatorData stored successfully in Redis for topic: " + topic);
                    } else {
                        _Logger.warning("Failed to store ActuatorData in Redis for topic: " + topic);
                    }
                } catch (Exception e) {
                    _Logger.log(Level.SEVERE, "Error storing ActuatorData in Redis", e);
                }
            }
            
            // Publish to MQTT if enabled
            if (this.enableMqttClient && this.mqttClient != null) {
                String topic = resourceName.getResourceName();
                String jsonData = this.dataUtil.actuatorDataToJson(data);
                this.mqttClient.publishMessage(resourceName, jsonData, 0);
                _Logger.fine("Published ActuatorData to MQTT topic: " + topic);
            }
            
            return true;
        }
        
        _Logger.warning("Received null ActuatorData. Ignoring.");
        return false;
    }
    
    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data) {
        if (data != null) {
            _Logger.info("Handling actuator command request: " + data.getName());
            
            // Forward command to CDA via MQTT if enabled
            if (this.enableMqttClient && this.mqttClient != null) {
                String topic = resourceName.getResourceName();
                String jsonData = this.dataUtil.actuatorDataToJson(data);
                this.mqttClient.publishMessage(resourceName, jsonData, 0);
                _Logger.info("Forwarded ActuatorData command to CDA via MQTT topic: " + topic);
            }
            
            return true;
        }
        
        _Logger.warning("Received null ActuatorData command request. Ignoring.");
        return false;
    }
    
    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
        if (data != null) {
            _Logger.info("Handling sensor message: " + data.getName() + 
                        ", Value: " + data.getValue());
            
            // Store in Redis if persistence is enabled
            if (this.enablePersistence && this.redisClient != null) {
                try {
                    String topic = resourceName.getResourceName();
                    boolean success = this.redisClient.storeData(topic, 0, data);
                    
                    if (success) {
                        _Logger.info("SensorData stored successfully in Redis for topic: " + topic);
                    } else {
                        _Logger.warning("Failed to store SensorData in Redis for topic: " + topic);
                    }
                } catch (Exception e) {
                    _Logger.log(Level.SEVERE, "Error storing SensorData in Redis", e);
                }
            }
            
            // Publish to MQTT if enabled
            if (this.enableMqttClient && this.mqttClient != null) {
                String topic = resourceName.getResourceName();
                String jsonData = this.dataUtil.sensorDataToJson(data);
                this.mqttClient.publishMessage(resourceName, jsonData, 0);
                _Logger.fine("Published SensorData to MQTT topic: " + topic);
            }
            
            return true;
        }
        
        _Logger.warning("Received null SensorData. Ignoring.");
        return false;
    }
    
    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
        if (data != null) {
            _Logger.info("Handling system performance message: " + data.getName() + 
                        ", CPU: " + data.getCpuUtilization() + "%" +
                        ", Memory: " + data.getMemoryUtilization() + "%");
            
            // Store in Redis if persistence is enabled
            if (this.enablePersistence && this.redisClient != null) {
                try {
                    String topic = resourceName.getResourceName();
                    boolean success = this.redisClient.storeData(topic, 0, data);
                    
                    if (success) {
                        _Logger.info("SystemPerformanceData stored successfully in Redis for topic: " + topic);
                    } else {
                        _Logger.warning("Failed to store SystemPerformanceData in Redis for topic: " + topic);
                    }
                } catch (Exception e) {
                    _Logger.log(Level.SEVERE, "Error storing SystemPerformanceData in Redis", e);
                }
            }
            
            // Publish to MQTT if enabled
            if (this.enableMqttClient && this.mqttClient != null) {
                String topic = resourceName.getResourceName();
                String jsonData = this.dataUtil.systemPerformanceDataToJson(data);
                this.mqttClient.publishMessage(resourceName, jsonData, 0);
                _Logger.fine("Published SystemPerformanceData to MQTT topic: " + topic);
            }
            
            return true;
        }
        
        _Logger.warning("Received null SystemPerformanceData. Ignoring.");
        return false;
    }
    
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
        if (msg != null && !msg.isEmpty()) {
            _Logger.info("Handling incoming message for resource: " + resourceName.getResourceName());
            
            // This is a generic message handler - typically you'd parse the message
            // and route it to the appropriate handler based on content
            _Logger.fine("Message content: " + msg);
            
            return true;
        }
        
        _Logger.warning("Received null or empty message. Ignoring.");
        return false;
    }
    
    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener) {
        if (name != null && listener != null) {
            _Logger.info("Setting actuator data listener for: " + name);
            
            // Store the listener reference for actuator commands
            // Implementation depends on your actuator management structure
            
        } else {
            _Logger.warning("Invalid actuator data listener parameters. Ignoring.");
        }
    }
}
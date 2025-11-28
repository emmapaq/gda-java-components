package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CloudClientFactory;
import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.MqttClientConnector;
// import programmingtheiot.gda.connection.RedisPersistenceAdapter;  // Comment out if not available

import programmingtheiot.gda.system.SystemPerformanceManager;

/**
 * DeviceDataManager - Central management class for the Gateway Device Application (GDA).
 * Handles data flow between CDA, cloud services, and internal components.
 */
public class DeviceDataManager implements IDataMessageListener
{
    // static
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManager.class.getName());
    
    // private var's
    private boolean started = false;
    
    // Enable/disable flags
    private boolean enableMqttClient = false;
    private boolean enablePersistenceClient = false;
    private boolean enableCloudClient = false;
    private boolean enableSystemPerformance = true;
    
    // Manager and client references
    private SystemPerformanceManager sysPerfManager = null;
    private MqttClientConnector mqttClient = null;
    private IPersistenceClient persistenceClient = null;
    private ICloudClient cloudClient = null;
    
    // Actuator data listener (optional)
    private IActuatorDataListener actuatorDataListener = null;
    
    // constructors
    
    /**
     * Constructor.
     * Initializes all managers and clients based on configuration settings.
     */
    public DeviceDataManager()
    {
        super();
        
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        // Initialize SystemPerformanceManager
        this.enableSystemPerformance = configUtil.getBoolean(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);
        
        if (this.enableSystemPerformance) {
            this.sysPerfManager = new SystemPerformanceManager();
            this.sysPerfManager.setDataMessageListener(this);
            _Logger.info("System performance manager enabled and initialized.");
        } else {
            _Logger.info("System performance manager disabled.");
        }
        
        // Initialize MQTT client for CDA communication
        this.enableMqttClient = configUtil.getBoolean(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
        
        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            this.mqttClient.setDataMessageListener(this);
            _Logger.info("MQTT client enabled and initialized.");
        } else {
            _Logger.info("MQTT client disabled.");
        }
        
        // Initialize persistence client
        this.enablePersistenceClient = configUtil.getBoolean(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
        
        if (this.enablePersistenceClient) {
            // Comment out if RedisPersistenceAdapter not available
            // this.persistenceClient = new RedisPersistenceAdapter();
            _Logger.info("Persistence client enabled (but not initialized - RedisPersistenceAdapter not available).");
        } else {
            _Logger.info("Persistence client disabled.");
        }
        
        // Initialize cloud client
        this.enableCloudClient = configUtil.getBoolean(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
        
        if (this.enableCloudClient) {
            // Use CloudClientFactory to create appropriate cloud client
            this.cloudClient = CloudClientFactory.getInstance().createCloudClient();
            
            if (this.cloudClient != null) {
                // register DeviceDataManager as data message listener so cloud messages are delivered here
                this.cloudClient.setDataMessageListener(this);
                _Logger.info("Cloud client created via factory and initialized.");
            } else {
                _Logger.warning("Failed to create cloud client via factory. Cloud functionality disabled.");
                this.enableCloudClient = false;
            }
        } else {
            _Logger.info("Cloud client disabled.");
        }
    }
    
    /**
     * Constructor with specific enable flags.
     * 
     * @param enableMqttClient Enable MQTT client for CDA communication
     * @param enablePersistenceClient Enable persistence client
     */
    public DeviceDataManager(boolean enableMqttClient, boolean enablePersistenceClient)
    {
        super();
        
        this.enableMqttClient = enableMqttClient;
        this.enablePersistenceClient = enablePersistenceClient;
        
        // Initialize SystemPerformanceManager
        this.sysPerfManager = new SystemPerformanceManager();
        this.sysPerfManager.setDataMessageListener(this);
        
        // Initialize MQTT client if enabled
        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            this.mqttClient.setDataMessageListener(this);
        }
        
        // Initialize persistence client if enabled
        if (this.enablePersistenceClient) {
            // Comment out if RedisPersistenceAdapter not available
            // this.persistenceClient = new RedisPersistenceAdapter();
        }
        
        // Initialize cloud client based on configuration
        ConfigUtil configUtil = ConfigUtil.getInstance();
        this.enableCloudClient = configUtil.getBoolean(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
        
        if (this.enableCloudClient) {
            this.cloudClient = CloudClientFactory.getInstance().createCloudClient();
            if (this.cloudClient != null) {
                this.cloudClient.setDataMessageListener(this);
            } else {
                this.enableCloudClient = false;
            }
        }
        
        _Logger.info("DeviceDataManager initialized with custom flags.");
    }
    
    // public methods
    
    /**
     * Starts the DeviceDataManager and all enabled components.
     * Connection order: Cloud -> CDA -> SystemPerformanceManager
     */
    public void startManager()
    {
        if (! this.started) {
            _Logger.info("Starting DeviceDataManager...");
            
            // Connect to cloud client first (if enabled)
            if (this.enableCloudClient && this.cloudClient != null) {
                if (this.cloudClient.connectClient()) {
                    _Logger.info("Successfully connected to cloud service.");
                    
                    // Subscribe to cloud actuator commands so incoming cloud actuation messages are received
                    try {
                        this.cloudClient.subscribeToCloudEvents(ResourceNameEnum.CLOUD_ACTUATOR_CMD_RESOURCE);
                        _Logger.info("Subscribed to cloud actuator command topic.");
                    } catch (Exception e) {
                        _Logger.log(Level.WARNING, "Failed to subscribe to cloud actuator topic.", e);
                    }
                } else {
                    _Logger.warning("Failed to connect to cloud service.");
                }
            }
            
            // Connect to CDA via MQTT
            if (this.enableMqttClient && this.mqttClient != null) {
                if (this.mqttClient.connectClient()) {
                    _Logger.info("Successfully connected to CDA MQTT broker.");
                    
                    // Subscribe to actuator response topic
                    this.mqttClient.subscribeToTopic(
                        ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, 
                        ConfigConst.DEFAULT_QOS);
                    
                    // Subscribe to sensor data topic
                    this.mqttClient.subscribeToTopic(
                        ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, 
                        ConfigConst.DEFAULT_QOS);
                    
                    // Subscribe to system performance data topic
                    this.mqttClient.subscribeToTopic(
                        ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, 
                        ConfigConst.DEFAULT_QOS);
                    
                    _Logger.info("Subscribed to CDA topics.");
                } else {
                    _Logger.warning("Failed to connect to CDA MQTT broker.");
                }
            }
            
            // Start SystemPerformanceManager last
            if (this.enableSystemPerformance && this.sysPerfManager != null) {
                this.sysPerfManager.startManager();
                _Logger.info("Started SystemPerformanceManager.");
            }
            
            this.started = true;
            _Logger.info("DeviceDataManager started successfully.");
        } else {
            _Logger.warning("DeviceDataManager is already started.");
        }
    }
    
    /**
     * Stops the DeviceDataManager and all enabled components.
     */
    public void stopManager()
    {
        if (this.started) {
            _Logger.info("Stopping DeviceDataManager...");
            
            // Stop SystemPerformanceManager first
            if (this.enableSystemPerformance && this.sysPerfManager != null) {
                this.sysPerfManager.stopManager();
                _Logger.info("Stopped SystemPerformanceManager.");
            }
            
            // Unsubscribe and disconnect from cloud
            if (this.enableCloudClient && this.cloudClient != null) {
                try {
                    // Unsubscribe from cloud actuator commands
                    this.cloudClient.unsubscribeFromCloudEvents(ResourceNameEnum.CLOUD_ACTUATOR_CMD_RESOURCE);
                } catch (Exception e) {
                    _Logger.log(Level.WARNING, "Failed to unsubscribe from cloud actuator topic.", e);
                }
                
                if (this.cloudClient.disconnectClient()) {
                    _Logger.info("Successfully disconnected from cloud service.");
                } else {
                    _Logger.warning("Failed to disconnect from cloud service.");
                }
            }
            
            // Unsubscribe and disconnect from CDA
            if (this.enableMqttClient && this.mqttClient != null) {
                // Unsubscribe from all topics
                this.mqttClient.unsubscribeFromTopic(
                    ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
                this.mqttClient.unsubscribeFromTopic(
                    ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
                this.mqttClient.unsubscribeFromTopic(
                    ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
                
                if (this.mqttClient.disconnectClient()) {
                    _Logger.info("Successfully disconnected from CDA MQTT broker.");
                } else {
                    _Logger.warning("Failed to disconnect from CDA MQTT broker.");
                }
            }
            
            this.started = false;
            _Logger.info("DeviceDataManager stopped successfully.");
        } else {
            _Logger.warning("DeviceDataManager is already stopped.");
        }
    }
    
    // IDataMessageListener implementation
    
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
    {
        if (data != null) {
            _Logger.log(
                Level.INFO,
                "Actuator response received: {0}. Message: {1}",
                new Object[] {resourceName.getResourceName(), data.getStatusCode()});
            
            if (data.hasError()) {
                _Logger.warning("Error flag set for ActuatorData response.");
            }
            
            // Forward to actuator data listener if registered
            if (this.actuatorDataListener != null) {
                return this.actuatorDataListener.onActuatorDataUpdate(data);
            }
            
            return true;
        } else {
            _Logger.warning("Received null ActuatorData response.");
            return false;
        }
    }
    
    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
    {
        if (data != null) {
            _Logger.log(
                Level.FINE,
                "Actuator request received: {0}. Command: {1}",
                new Object[] {resourceName.getResourceName(), Integer.valueOf(data.getCommand())});
            
            if (data.hasError()) {
                _Logger.warning("Error flag set for ActuatorData instance.");
            }
            
            // Send actuator command to CDA
            this.sendActuatorCommandToCda(resourceName, data);
            
            return true;
        } else {
            _Logger.warning("Received null ActuatorData request.");
            return false;
        }
    }
    
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
    {
        if (resourceName != null && msg != null) {
            _Logger.log(
                Level.FINE,
                "Incoming message received: {0}. Message: {1}",
                new Object[] {resourceName.getResourceName(), msg});
            
            try {
                // Handle actuator commands from cloud
                if (resourceName == ResourceNameEnum.CLOUD_ACTUATOR_CMD_RESOURCE) {
                    _Logger.info("Handling incoming ActuatorData command from cloud: " + msg);
                    
                    // Convert JSON to ActuatorData for validation
                    ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(msg);
                    
                    if (actuatorData != null) {
                        // Validate the actuator command
                        if (actuatorData.getCommand() == ConfigConst.COMMAND_ON || 
                            actuatorData.getCommand() == ConfigConst.COMMAND_OFF) {
                            
                            _Logger.info("Valid actuator command received. Forwarding to CDA: " + 
                                actuatorData.getName() + " = " + actuatorData.getCommand());
                            
                            // Convert back to JSON for transmission to CDA
                            String jsonData = DataUtil.getInstance().actuatorDataToJson(actuatorData);
                            
                            // Send to CDA via MQTT
                            if (this.enableMqttClient && this.mqttClient != null && 
                                this.mqttClient.isConnected()) {
                                
                                int qos = ConfigUtil.getInstance().getInteger(
                                    ConfigConst.GATEWAY_DEVICE, 
                                    ConfigConst.DEFAULT_QOS_KEY, 
                                    ConfigConst.DEFAULT_QOS);
                                
                                _Logger.fine("Publishing actuator command to CDA via MQTT: " + jsonData);
                                return this.mqttClient.publishMessage(resourceName, jsonData, qos);
                            } else {
                                _Logger.warning("MQTT client not connected. Cannot forward actuator command to CDA.");
                                return false;
                            }
                        } else {
                            _Logger.warning("Invalid actuator command received: " + actuatorData.getCommand());
                            return false;
                        }
                    } else {
                        _Logger.warning("Failed to parse ActuatorData from JSON.");
                        return false;
                    }
                }
                // Process message based on resource type
                else if (resourceName == ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE) {
                    // Parse as SensorData
                    SensorData sensorData = DataUtil.getInstance().jsonToSensorData(msg);
                    if (sensorData != null) {
                        return handleSensorMessage(resourceName, sensorData);
                    }
                } else if (resourceName == ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE) {
                    // Parse as ActuatorData
                    ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(msg);
                    if (actuatorData != null) {
                        return handleActuatorCommandResponse(resourceName, actuatorData);
                    }
                } else if (resourceName == ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE) {
                    // Parse as SystemPerformanceData
                    SystemPerformanceData sysPerfData = 
                        DataUtil.getInstance().jsonToSystemPerformanceData(msg);
                    if (sysPerfData != null) {
                        return handleSystemPerformanceMessage(resourceName, sysPerfData);
                    }
                } else {
                    _Logger.warning("Unknown resource type: " + resourceName);
                    return false;
                }
            } catch (Exception e) {
                _Logger.log(Level.WARNING, "Failed to process incoming message for resource: " + resourceName, e);
                return false;
            }
            
            return true;
        } else {
            _Logger.warning("Received null resource name or message.");
            return false;
        }
    }
    
    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
    {
        if (data != null) {
            _Logger.fine("Handling sensor message: " + data.getName());
            
            if (data.hasError()) {
                _Logger.warning("Error flag set for SensorData instance.");
            }
            
            String jsonData = DataUtil.getInstance().sensorDataToJson(data);
            
            _Logger.fine("JSON [SensorData] -> " + jsonData);
            
            // Retrieve QoS from config file
            int qos = ConfigUtil.getInstance().getInteger(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.DEFAULT_QOS_KEY, 
                ConfigConst.DEFAULT_QOS);
            
            // Store in persistence client if enabled
            if (this.enablePersistenceClient && this.persistenceClient != null) {
                try {
                    this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
                    _Logger.fine("Stored sensor data in persistence client.");
                } catch (Exception e) {
                    _Logger.warning("Failed to store sensor data: " + e.getMessage());
                }
            }
            
            // Analyze incoming data
            this.handleIncomingDataAnalysis(resourceName, data);
            
            // Send to cloud
            this.handleUpstreamTransmission(resourceName, data, qos);
            
            return true;
        } else {
            _Logger.warning("Received null SensorData.");
            return false;
        }
    }
    
    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
    {
        if (data != null) {
            _Logger.info("Handling system performance message: " + data.getName());
            
            if (data.hasError()) {
                _Logger.warning("Error flag set for SystemPerformanceData instance.");
            }
            
            String jsonData = DataUtil.getInstance().systemPerformanceDataToJson(data);
            
            _Logger.fine("JSON [SystemPerformanceData] -> " + jsonData);
            
            // Retrieve QoS from config file
            int qos = ConfigUtil.getInstance().getInteger(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.DEFAULT_QOS_KEY, 
                ConfigConst.DEFAULT_QOS);
            
            // Store in persistence client if enabled (optional)
            if (this.enablePersistenceClient && this.persistenceClient != null) {
                try {
                    this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
                    _Logger.fine("Stored system performance data in persistence client.");
                } catch (Exception e) {
                    _Logger.warning("Failed to store system performance data: " + e.getMessage());
                }
            }
            
            // Analyze system performance data (optional)
            // this.handleIncomingDataAnalysis(resourceName, data);
            
            // Send to cloud
            this.handleUpstreamTransmission(resourceName, data, qos);
            
            return true;
        } else {
            _Logger.warning("Received null SystemPerformanceData.");
            return false;
        }
    }
    
    @Override
    public boolean setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        if (listener != null) {
            this.actuatorDataListener = listener;
            _Logger.info("Actuator data listener registered: " + name);
            return true;
        }
        
        _Logger.warning("Actuator data listener is null. Ignoring.");
        return false;
    }
    
    // private methods
    
    /**
     * Sends actuator command to CDA via MQTT.
     * 
     * @param resourceName The resource name for the actuator
     * @param data The actuator data to send
     * @return boolean True if successful, false otherwise
     */
    private boolean sendActuatorCommandToCda(ResourceNameEnum resourceName, ActuatorData data)
    {
        if (this.enableMqttClient && this.mqttClient != null && this.mqttClient.isConnected()) {
            String jsonData = DataUtil.getInstance().actuatorDataToJson(data);
            
            // Retrieve QoS from config file
            int qos = ConfigUtil.getInstance().getInteger(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.DEFAULT_QOS_KEY, 
                ConfigConst.DEFAULT_QOS);
            
            return this.mqttClient.publishMessage(
                resourceName, 
                jsonData, 
                qos);
        } else {
            _Logger.warning("MQTT client not connected. Unable to send actuator command to CDA.");
            return false;
        }
    }
    
    /**
     * Analyzes incoming sensor data and triggers actuator commands if needed.
     * 
     * @param resourceName The resource name
     * @param data The sensor data to analyze
     */
    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SensorData data)
    {
        // Example: Check temperature and trigger HVAC actuator
        if (data.getTypeID() == ConfigConst.TEMP_SENSOR_TYPE) {
            float temp = data.getValue();
            
            _Logger.fine("Analyzing temperature data: " + temp);
            
            // Simple threshold-based logic
            if (temp > 25.0f) {
                _Logger.info("Temperature exceeds threshold. Triggering cooling.");
                
                ActuatorData actuatorData = new ActuatorData();
                actuatorData.setTypeID(ConfigConst.HVAC_ACTUATOR_TYPE);
                actuatorData.setName(ConfigConst.HVAC_ACTUATOR_NAME);
                actuatorData.setCommand(ConfigConst.COMMAND_ON);
                actuatorData.setValue(20.0f); // Target temperature
                
                this.sendActuatorCommandToCda(
                    ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, 
                    actuatorData);
            } else if (temp < 18.0f) {
                _Logger.info("Temperature below threshold. Triggering heating.");
                
                ActuatorData actuatorData = new ActuatorData();
                actuatorData.setTypeID(ConfigConst.HVAC_ACTUATOR_TYPE);
                actuatorData.setName(ConfigConst.HVAC_ACTUATOR_NAME);
                actuatorData.setCommand(ConfigConst.COMMAND_ON);
                actuatorData.setValue(22.0f); // Target temperature
                
                this.sendActuatorCommandToCda(
                    ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, 
                    actuatorData);
            }
        }
        
        // Add additional analysis logic for other sensor types as needed
        // Example: humidity, pressure, etc.
    }
    
    /**
     * Handles upstream transmission of sensor data to cloud.
     * 
     * @param resource The resource name
     * @param data The sensor data to send
     * @param qos The quality of service level
     */
    private void handleUpstreamTransmission(ResourceNameEnum resource, SensorData data, int qos)
    {
        _Logger.fine("Sending sensor data to cloud service: " + resource);
        
        if (this.enableCloudClient && this.cloudClient != null) {
            if (this.cloudClient.sendEdgeDataToCloud(resource, data)) {
                _Logger.fine("Sent sensor data upstream to CSP.");
            } else {
                _Logger.warning("Failed to send sensor data to CSP.");
            }
        } else {
            _Logger.fine("Cloud client not enabled or not initialized. Skipping cloud transmission.");
        }
    }
    
    /**
     * Handles upstream transmission of system performance data to cloud.
     * 
     * @param resource The resource name
     * @param data The system performance data to send
     * @param qos The quality of service level
     */
    private void handleUpstreamTransmission(ResourceNameEnum resource, SystemPerformanceData data, int qos)
    {
        _Logger.fine("Sending system performance data to cloud service: " + resource);
        
        if (this.enableCloudClient && this.cloudClient != null) {
            if (this.cloudClient.sendEdgeDataToCloud(resource, data)) {
                _Logger.fine("Sent system performance data upstream to CSP.");
            } else {
                _Logger.warning("Failed to send system performance data to CSP.");
            }
        } else {
            _Logger.fine("Cloud client not enabled or not initialized. Skipping cloud transmission.");
        }
    }
}

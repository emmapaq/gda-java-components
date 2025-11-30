package programmingtheiot.gda.app;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.BaseIotData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;

/**
 * Main device data manager for the GDA.
 * 
 * Manages incoming messages from CDA, performs data analysis,
 * and triggers actuation events based on threshold crossings.
 * 
 * @author Emma
 */
public class DeviceDataManager implements IDataMessageListener
{
    // Static
    
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManager.class.getName());
    
    // Member variables
    
    private ConfigUtil configUtil = null;
    
    // Communication clients
    private boolean enableMqttClient = false;
    private boolean enableCoapServer = false;
    private boolean enablePersistenceClient = false;
    
    private MqttClientConnector mqttClient = null;
    private CoapServerGateway coapServer = null;
    private IActuatorDataListener actuatorDataListener = null;
    
    // Humidity threshold tracking
    private ActuatorData latestHumidifierActuatorData = null;
    private ActuatorData latestHumidifierActuatorResponse = null;
    private SensorData latestHumiditySensorData = null;
    private OffsetDateTime latestHumiditySensorTimeStamp = null;
    
    private FermentationProfileManager fermentationProfileMgr;
    
    private boolean handleHumidityChangeOnDevice = false;
    private int lastKnownHumidifierCommand = ConfigConst.COMMAND_OFF;
    
    // Humidity threshold configuration (loaded from PiotConfig.props)
    private long humidityMaxTimePastThreshold = 300; // seconds
    private float nominalHumiditySetting = 40.0f;
    private float triggerHumidifierFloor = 30.0f;
    private float triggerHumidifierCeiling = 50.0f;
    
    // Constructor
    
    /**
     * Default constructor.
     * 
     * Initializes all managers, loads configuration, and sets up
     * communication clients (MQTT and/or CoAP).
     */
    public DeviceDataManager()
    {
        super();
        
        this.configUtil = ConfigUtil.getInstance();
        
        // Load communication settings
        this.enableMqttClient = 
            this.configUtil.getBoolean(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.ENABLE_MQTT_CLIENT_KEY);
        
        this.enableCoapServer = 
            this.configUtil.getBoolean(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.ENABLE_COAP_SERVER_KEY);
        
        this.enablePersistenceClient = 
            this.configUtil.getBoolean(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
        
        // Parse config rules for local actuation events
        this.handleHumidityChangeOnDevice =
            this.configUtil.getBoolean(
                ConfigConst.GATEWAY_DEVICE, 
                "handleHumidityChangeOnDevice");
        
        this.humidityMaxTimePastThreshold =
            this.configUtil.getInteger(
                ConfigConst.GATEWAY_DEVICE, 
                "humidityMaxTimePastThreshold");
        
        this.nominalHumiditySetting =
            this.configUtil.getFloat(
                ConfigConst.GATEWAY_DEVICE, 
                "nominalHumiditySetting");
        
        this.triggerHumidifierFloor =
            this.configUtil.getFloat(
                ConfigConst.GATEWAY_DEVICE, 
                "triggerHumidifierFloor");
        
        this.triggerHumidifierCeiling =
            this.configUtil.getFloat(
                ConfigConst.GATEWAY_DEVICE, 
                "triggerHumidifierCeiling");
        
        // Validate timing - add other validators for remaining values if needed
        if (this.humidityMaxTimePastThreshold < 10 || this.humidityMaxTimePastThreshold > 7200) {
            this.humidityMaxTimePastThreshold = 300;
        }
        
        // Initialize MQTT client
        if (this.enableMqttClient) {
            _Logger.info("MQTT client enabled. Initializing MqttClientConnector...");
            this.mqttClient = new MqttClientConnector();
            this.mqttClient.setDataMessageListener(this);
        }
        
        // Initialize CoAP server
        if (this.enableCoapServer) {
            _Logger.info("CoAP server enabled. Initializing CoapServerGateway...");
            this.coapServer = new CoapServerGateway(this);
        }
        
        _Logger.info("DeviceDataManager initialized.");
        _Logger.info("\tHandle Humidity Changes: " + this.handleHumidityChangeOnDevice);
        _Logger.info("\tHumidity Max Time Past Threshold: " + this.humidityMaxTimePastThreshold + " seconds");
        _Logger.info("\tNominal Humidity Setting: " + this.nominalHumiditySetting + "%");
        _Logger.info("\tTrigger Humidifier Floor: " + this.triggerHumidifierFloor + "%");
        _Logger.info("\tTrigger Humidifier Ceiling: " + this.triggerHumidifierCeiling + "%");
        
        this.fermentationProfileMgr = new FermentationProfileManager();
    }
    
    
    // Public methods - Lifecycle
    
    /**
     * Starts the DeviceDataManager and all communication clients.
     */
    public void startManager()
    {
        _Logger.info("Starting DeviceDataManager...");
        
        if (this.mqttClient != null) {
            if (this.mqttClient.connectClient()) {
                _Logger.info("Successfully connected MQTT client to broker.");
                
                // Subscriptions now handled in connectComplete() callback
            } else {
                _Logger.severe("Failed to connect MQTT client to broker.");
            }
        }
        
        if (this.enableCoapServer && this.coapServer != null) {
            if (this.coapServer.startServer()) {
                _Logger.info("CoAP server started.");
            } else {
                _Logger.severe("Failed to start CoAP server. Check log file for details.");
            }
        }
        
        _Logger.info("DeviceDataManager started.");
    }
    
    /**
     * Stops the DeviceDataManager and all communication clients.
     */
    public void stopManager()
    {
        _Logger.info("Stopping DeviceDataManager...");
        
        if (this.mqttClient != null) {
            this.mqttClient.disconnectClient();
            _Logger.info("MQTT client disconnected.");
        }
        
        if (this.coapServer != null) {
            this.coapServer.stopServer();
            _Logger.info("CoAP server stopped.");
        }
        
        _Logger.info("DeviceDataManager stopped.");
    }
    
    
    // IDataMessageListener implementation
    
    /**
     * Sets the actuator data listener (interface method).
     * 
     * @param name The name of the actuator listener
     * @param listener The actuator data listener
     * @return boolean True if listener was set successfully
     */
    @Override
    public boolean setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        if (listener != null) {
            this.actuatorDataListener = listener;
            _Logger.info("Actuator data listener set for: " + name);
            return true;
        } else {
            _Logger.warning("Actuator data listener is null.");
            return false;
        }
    }
    
    /**
     * Sets the actuator data listener (convenience method without name).
     * 
     * @param listener The actuator data listener
     * @return boolean True if listener was set successfully
     */
    public boolean setActuatorDataListener(IActuatorDataListener listener)
    {
        return setActuatorDataListener("default", listener);
    }
    
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
    {
        if (data != null) {
            _Logger.info("Handling actuator response: " + data.getName());
            
            // Check if this is a profile change command FIRST
            if (data.getTypeID() == ConfigConst.FERMENTATION_PROFILE_ACTUATOR_TYPE) {
                String newProfile = data.getStateData();  // Get profile from stateData
                
                _Logger.info("Received fermentation profile change command: " + newProfile);
                
                // Update GDA's profile manager
                boolean profileChanged = this.fermentationProfileMgr.setProfile(newProfile);
                
                if (profileChanged) {
                    // Forward profile change to CDA
                    sendActuatorCommandtoCda(resourceName, data);
                    
                    _Logger.info("Profile change successful and forwarded to CDA: " + newProfile);
                    return true;
                } else {
                    _Logger.warning("Failed to change profile to: " + newProfile);
                    return false;
                }
            }
            
            // Store the latest humidifier response (for non-profile commands)
            if (data.getTypeID() == ConfigConst.HUMIDIFIER_ACTUATOR_TYPE) {
                this.latestHumidifierActuatorResponse = data;
                _Logger.info("Stored latest humidifier response: command=" + data.getCommand());
            }
            
            // Perform general data analysis
            this.handleIncomingDataAnalysis(resourceName, data);
            
            return true;
            
        } else {
            return false;
        }
    }
    
    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
    {
        if (data != null) {
            _Logger.info("Handling actuator command request: " + data.getName());
            
            // Check if this is a fermentation profile change request
            if (data.getTypeID() == ConfigConst.FERMENTATION_PROFILE_ACTUATOR_TYPE) {
                String newProfile = data.getStateData();
                
                if (newProfile != null && !newProfile.isEmpty()) {
                    _Logger.info("Processing profile change request: " + newProfile);
                    return this.changeFermentationProfile(newProfile);
                } else {
                    _Logger.warning("Profile change request has null or empty profile name");
                    return false;
                }
            }
            
            // For other actuator command requests, perform data analysis
            this.handleIncomingDataAnalysis(resourceName, data);
            
            // Optionally forward the request to CDA
            // sendActuatorCommandtoCda(resourceName, data);
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
    {
        _Logger.info("Handling incoming message on resource: " + resourceName.getResourceName());
        _Logger.fine("Message: " + msg);
        
        return true;
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
            
            int qos = ConfigConst.DEFAULT_QOS;
            
            // TODO: Store in persistence client if enabled
            // if (this.enablePersistenceClient && this.persistenceClient != null) {
            //     this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
            // }
            
            this.handleIncomingDataAnalysis(resourceName, data);
            
            this.handleUpstreamTransmission(resourceName, jsonData, qos);
            
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
    {
        if (data != null) {
            _Logger.fine("Handling system performance message.");
            
            String jsonData = DataUtil.getInstance().systemPerformanceDataToJson(data);
            
            _Logger.fine("JSON [SystemPerformanceData] -> " + jsonData);
            
            int qos = ConfigConst.DEFAULT_QOS;
            
            // TODO: Store in persistence client if enabled
            
            this.handleUpstreamTransmission(resourceName, jsonData, qos);
            
            return true;
        } else {
            return false;
        }
    }
    
    
    // Private methods - Data Analysis
    
    private void handleIncomingDataAnalysis(ResourceNameEnum resource, ActuatorData data)
    {
        _Logger.fine("Analyzing actuator data: " + data.getName());
        
        // For now, just log - will be implemented in Part 04
    }
    
    private void handleIncomingDataAnalysis(ResourceNameEnum resource, SensorData data)
    {
        _Logger.fine("Analyzing sensor data: " + data.getName());
        
        // Check sensor type and route to appropriate analysis
        if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {
            handleHumiditySensorAnalysis(resource, data);
        }
    }
    
    private void handleHumiditySensorAnalysis(ResourceNameEnum resource, SensorData data)
    {
        if (!this.handleHumidityChangeOnDevice) {
            return;
        }
        
        _Logger.fine("Analyzing humidity data from CDA: " + data.getLocationID() + ". Value: " + data.getValue());
        
        boolean isLow = data.getValue() < this.triggerHumidifierFloor;
        boolean isHigh = data.getValue() > this.triggerHumidifierCeiling;
        
        if (isLow || isHigh) {
            _Logger.fine("Humidity data from CDA exceeds nominal range.");
            
            if (this.latestHumiditySensorData == null) {
                // Set properties then exit - nothing more to do until the next sample
                this.latestHumiditySensorData = data;
                this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);
                
                _Logger.fine(
                    "Starting humidity nominal exception timer. Waiting for seconds: " +
                    this.humidityMaxTimePastThreshold);
                
                return;
            } else {
                OffsetDateTime curHumiditySensorTimeStamp = getDateTimeFromData(data);
                
                long diffSeconds =
                    ChronoUnit.SECONDS.between(
                        this.latestHumiditySensorTimeStamp, curHumiditySensorTimeStamp);
                
                _Logger.fine("Checking Humidity value exception time delta: " + diffSeconds);
                
                if (diffSeconds >= this.humidityMaxTimePastThreshold) {
                    ActuatorData ad = new ActuatorData();
                    ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
                    ad.setLocationID(data.getLocationID());
                    ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
                    ad.setValue(this.nominalHumiditySetting);
                    
                    if (isLow) {
                        ad.setCommand(ConfigConst.COMMAND_ON);
                        ad.setStateData("Humidity too low. Turning humidifier ON.");
                    } else if (isHigh) {
                        ad.setCommand(ConfigConst.COMMAND_OFF);
                        ad.setStateData("Humidity too high. Turning humidifier OFF.");
                    }
                    
                    _Logger.info(
                        "Humidity exceptional value reached. Sending actuation event to CDA: " +
                        ad);
                    
                    this.lastKnownHumidifierCommand = ad.getCommand();
                    sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ad);
                    
                    // Set ActuatorData and reset SensorData (and timestamp)
                    this.latestHumidifierActuatorData = ad;
                    this.latestHumiditySensorData = null;
                    this.latestHumiditySensorTimeStamp = null;
                }
            }
        } else if (this.lastKnownHumidifierCommand == ConfigConst.COMMAND_ON) {
            // Check if we need to turn off the humidifier
            if (this.latestHumidifierActuatorData != null) {
                // Check the value - if the humidifier is on, but not yet at nominal, keep it on
                if (data.getValue() >= this.nominalHumiditySetting) {
                    this.latestHumidifierActuatorData.setCommand(ConfigConst.COMMAND_OFF);
                    this.latestHumidifierActuatorData.setStateData("Humidity nominal reached. Turning OFF.");
                    
                    _Logger.info(
                        "Humidity nominal value reached. Sending OFF actuation event to CDA: " +
                        this.latestHumidifierActuatorData);
                    
                    sendActuatorCommandtoCda(
                        ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, 
                        this.latestHumidifierActuatorData);
                    
                    // Reset ActuatorData and SensorData (and timestamp)
                    this.lastKnownHumidifierCommand = this.latestHumidifierActuatorData.getCommand();
                    this.latestHumidifierActuatorData = null;
                    this.latestHumiditySensorData = null;
                    this.latestHumiditySensorTimeStamp = null;
                } else {
                    _Logger.fine("Humidifier is still on. Not yet at nominal levels (OK).");
                }
            } else {
                // Shouldn't happen, unless some other logic nullifies the ActuatorData instance
                _Logger.warning(
                    "ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");
            }
        }
    }
    
    private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data)
    {
        // NOTE: This is how an ActuatorData command will get passed to the CDA
        // when the GDA is providing the CoAP server and hosting the appropriate
        // ActuatorData resource. It will typically be used when the OBSERVE
        // client (the CDA) has sent an OBSERVE GET request to the ActuatorData resource.
        if (this.actuatorDataListener != null) {
            this.actuatorDataListener.onActuatorDataUpdate(data);
        }
        
        // NOTE: This is how an ActuatorData command will get passed to the CDA
        // when using MQTT to communicate between the GDA and CDA
        if (this.enableMqttClient && this.mqttClient != null) {
            String jsonData = DataUtil.getInstance().actuatorDataToJson(data);
            
            if (this.mqttClient.publishMessage(resource, jsonData, ConfigConst.DEFAULT_QOS)) {
                _Logger.info(
                    "Published ActuatorData command from GDA to CDA: " + data.getCommand());
            } else {
                _Logger.warning(
                    "Failed to publish ActuatorData command from GDA to CDA: " + data.getCommand());
            }
        }
    }
    
    private void handleUpstreamTransmission(ResourceNameEnum resource, String jsonData, int qos)
    {
        // NOTE: This will be implemented in Part 04 to send to cloud service
        _Logger.info("TODO: Send JSON data to cloud service: " + resource.getResourceName());
    }
    
    private OffsetDateTime getDateTimeFromData(BaseIotData data)
    {
        OffsetDateTime odt = null;
        
        try {
            // Get timestamp as epoch milliseconds
            long timestampMillis = data.getTimeStamp();
            
            // Convert epoch millis to OffsetDateTime
            odt = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestampMillis),
                java.time.ZoneId.systemDefault()
            );
        } catch (Exception e) {
            _Logger.warning(
                "Failed to extract timestamp from IoT data. Using local current time.");
            
            // Use current time as fallback
            odt = OffsetDateTime.now();
        }
        
        return odt;
    }
    
  
    
    /**
     * Public method to allow external profile changes (e.g., from cloud dashboard).
     */
    public boolean changeFermentationProfile(String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            _Logger.warning("Cannot change profile: profile name is null or empty");
            return false;
        }
        
        _Logger.info("Changing fermentation profile to: " + profileName);
        
        // Update GDA's profile manager
        if (this.fermentationProfileMgr.setProfile(profileName)) {
            // Generate command to send to CDA
            ActuatorData profileCmd = this.fermentationProfileMgr.generateProfileChangeCommand(profileName);
            
            if (profileCmd != null) {
                // TODO: Send to CDA using your existing mechanism
                // For now, just log it
                _Logger.info("Fermentation profile changed to: " + profileName);
                _Logger.info("TODO: Forward command to CDA");
                return true;
            }
        }
        
        _Logger.warning("Failed to change fermentation profile to: " + profileName);
        return false;
    }
}

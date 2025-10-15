package programmingtheiot.gda.app;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;

import programmingtheiot.common.IActuatorDataListener;
/**
 * Main manager class for handling device data and connections in the GDA.
 */
public class DeviceDataManager implements IDataMessageListener {
    
    // static
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManager.class.getName());
    
    // private variables
    private SystemPerformanceManager sysPerfMgr = null;
    private MqttClientConnector mqttClient = null;
    
    // Configuration flags
    private boolean enableMqttClient = true;
    private boolean enableSystemPerf = true;
    private boolean enableCoapServer = false;
    private boolean enableCloudClient = false;
    private boolean enablePersistenceClient = false;
    
    // constructors
    
    public DeviceDataManager()
    {
        super();
        
        // Initialize configuration
        initManager();
    }
    
    // public methods
    
    /**
     * Initialize the manager and its components
     */
    private void initManager()
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        // Load configuration flags - without default values
        try {
            this.enableSystemPerf =
                configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);
        } catch (Exception e) {
            this.enableSystemPerf = true; // default value
            _Logger.warning("Using default value for enableSystemPerf: " + this.enableSystemPerf);
        }
        
        try {
            this.enableMqttClient =
                configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, "enableMqttClient");
        } catch (Exception e) {
            this.enableMqttClient = true; // default value
            _Logger.warning("Using default value for enableMqttClient: " + this.enableMqttClient);
        }
        
        try {
            this.enableCoapServer =
                configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, "enableCoapServer");
        } catch (Exception e) {
            this.enableCoapServer = false; // default value
            _Logger.warning("Using default value for enableCoapServer: " + this.enableCoapServer);
        }
        
        try {
            this.enableCloudClient =
                configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, "enableCloudClient");
        } catch (Exception e) {
            this.enableCloudClient = false; // default value
            _Logger.warning("Using default value for enableCloudClient: " + this.enableCloudClient);
        }
        
        try {
            this.enablePersistenceClient =
                configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, "enablePersistenceClient");
        } catch (Exception e) {
            this.enablePersistenceClient = false; // default value
            _Logger.warning("Using default value for enablePersistenceClient: " + this.enablePersistenceClient);
        }
        
        _Logger.info("DeviceDataManager configuration:");
        _Logger.info("  enableMqttClient: " + this.enableMqttClient);
        _Logger.info("  enableSystemPerf: " + this.enableSystemPerf);
        _Logger.info("  enableCoapServer: " + this.enableCoapServer);
        _Logger.info("  enableCloudClient: " + this.enableCloudClient);
        _Logger.info("  enablePersistenceClient: " + this.enablePersistenceClient);
        
        // Initialize System Performance Manager if enabled
        if (this.enableSystemPerf) {
            this.sysPerfMgr = new SystemPerformanceManager();
            this.sysPerfMgr.setDataMessageListener(this);
            _Logger.info("SystemPerformanceManager initialized.");
        }
        
        // Initialize MQTT Client if enabled
        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            
            // Set this class as the data message listener
            this.mqttClient.setDataMessageListener(this);
            _Logger.info("MqttClientConnector initialized.");
        }
        
        // TODO: Initialize CoAP Server if enabled (Lab Module 8)
        if (this.enableCoapServer) {
            // TODO: implement this in Lab Module 8
            _Logger.info("CoAP Server configuration enabled (to be implemented in Lab Module 8).");
        }
        
        // TODO: Initialize Cloud Client if enabled (Lab Module 10)
        if (this.enableCloudClient) {
            // TODO: implement this in Lab Module 10
            _Logger.info("Cloud Client configuration enabled (to be implemented in Lab Module 10).");
        }
        
        // TODO: Initialize Persistence Client if enabled
        if (this.enablePersistenceClient) {
            // TODO: implement this as an optional exercise in Lab Module 5
            _Logger.info("Persistence Client configuration enabled (to be implemented as optional exercise).");
        }
    }
    
    /**
     * Start the manager and all enabled components
     */
    public void startManager()
    {
        _Logger.info("Starting DeviceDataManager...");
        
        // Start MQTT Client if enabled
        if (this.mqttClient != null) {
            if (this.mqttClient.connectClient()) {
                _Logger.info("Successfully connected MQTT client to broker.");
                
                // Add necessary subscriptions
                int qos = 1; // Using QoS 1 as default
                
                // Subscribe to required topics
                // TODO: check the return value for each and take appropriate action
                
                // IMPORTANT NOTE: The 'subscribeToTopic()' method calls shown
                // below will be moved to MqttClientConnector.connectComplete()
                // in Lab Module 10. For now, they can remain here.
                boolean sub1 = this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
                boolean sub2 = this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
                boolean sub3 = this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
                boolean sub4 = this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
                
                if (sub1 && sub2 && sub3 && sub4) {
                    _Logger.info("All MQTT topic subscriptions successful.");
                } else {
                    _Logger.warning("One or more MQTT topic subscriptions failed.");
                }
            } else {
                _Logger.severe("Failed to connect MQTT client to broker.");
                // TODO: take appropriate action (retry logic, fallback, etc.)
            }
        }
        
        // Start System Performance Manager if enabled
        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.startManager();
            _Logger.info("SystemPerformanceManager started.");
        }
        
        _Logger.info("DeviceDataManager started successfully.");
    }
    
    /**
     * Stop the manager and all enabled components
     */
    public void stopManager()
    {
        _Logger.info("Stopping DeviceDataManager...");
        
        // Stop System Performance Manager if enabled
        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.stopManager();
            _Logger.info("SystemPerformanceManager stopped.");
        }
        
        // Stop MQTT Client if enabled
        if (this.mqttClient != null) {
            // Add necessary un-subscribes
            
            // TODO: check the return value for each and take appropriate action
            
            // NOTE: The unsubscribeFromTopic() method calls below should match with
            // the subscribeToTopic() method calls from startManager(). Also, the
            // unsubscribe logic below can be moved to MqttClientConnector's
            // disconnectClient() call PRIOR to actually disconnecting from
            // the MQTT broker.
            boolean unsub1 = this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
            boolean unsub2 = this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
            boolean unsub3 = this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
            boolean unsub4 = this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
            
            if (unsub1 && unsub2 && unsub3 && unsub4) {
                _Logger.info("All MQTT topic unsubscriptions successful.");
            } else {
                _Logger.warning("One or more MQTT topic unsubscriptions failed.");
            }
            
            if (this.mqttClient.disconnectClient()) {
                _Logger.info("Successfully disconnected MQTT client from broker.");
            } else {
                _Logger.severe("Failed to disconnect MQTT client from broker.");
                // TODO: take appropriate action
            }
        }
        
        _Logger.info("DeviceDataManager stopped successfully.");
    }
    
    // =========================================================================
    // IDataMessageListener interface methods
    // =========================================================================
    
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resource, String message)
    {
        _Logger.info("Received incoming message for resource: " + resource);
        _Logger.fine("Message: " + message);
        
        // TODO: Process incoming message based on resource type
        // This will be implemented in later lab modules
        
        return true;
    }
    
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resource, ActuatorData data)
    {
        _Logger.info("Received actuator command response for resource: " + resource);
        _Logger.fine("Actuator data: " + data);
        
        // TODO: Process actuator command response
        // This will be implemented in later lab modules
        
        return true;
    }
    
    @Override
    public boolean handleSensorMessage(ResourceNameEnum resource, SensorData data)
    {
        _Logger.info("Received sensor message for resource: " + resource);
        _Logger.fine("Sensor data: " + data);
        
        // TODO: Process sensor data
        // This will be implemented in later lab modules
        
        return true;
    }
    
    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resource, SystemPerformanceData data)
    {
        _Logger.info("Received system performance message for resource: " + resource);
        _Logger.fine("System performance data: " + data);
        
        // TODO: Process system performance data
        // This will be implemented in later lab modules
        
        return true;
    }
    
    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resource, ActuatorData data)
    {
        _Logger.info("Received actuator command request for resource: " + resource);
        _Logger.fine("Actuator data: " + data);
        
        // TODO: Process actuator command request
        // This will be implemented in later lab modules
        
        return true;
    }
    
    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        _Logger.info("Setting actuator data listener for: " + name);
        
        // TODO: Implement actuator data listener registration
        // This will be implemented in later lab modules
    }
}
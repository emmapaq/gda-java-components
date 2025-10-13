package programmingtheiot.gda.app;

import java.util.HashMap;
import java.util.Map;
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
import programmingtheiot.data.SystemStateData;

import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.SmtpClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;

/**
 * Gateway Device Application (GDA) DeviceDataManager.
 *
 * Manages all local data processing for the GDA.
 */
public class DeviceDataManager implements IDataMessageListener {

    private static final Logger _Logger = Logger.getLogger(DeviceDataManager.class.getName());

    // Enable flags
    private boolean enableMqttClient = true;
    private boolean enableCoapServer = false;
    private boolean enableCloudClient = false;
    private boolean enablePersistenceClient = false;
    private boolean enableSystemPerf = false;

    // Managers / Connectors
    private IPubSubClient mqttClient = null;
    private CloudClientConnector cloudClient = null;
    private IPersistenceClient persistenceClient = null;
    private CoapServerGateway coapServer = null;
    private SystemPerformanceManager sysPerfMgr = null;

    private Map<String, IActuatorDataListener> actuatorListeners = new HashMap<>();
    private DataUtil dataUtil = DataUtil.getInstance();

    public DeviceDataManager() {
        _Logger.info("Initializing DeviceDataManager...");

        ConfigUtil configUtil = ConfigUtil.getInstance();

        this.enableMqttClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
                ConfigConst.ENABLE_MQTT_CLIENT_KEY);
        this.enableCoapServer = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
                ConfigConst.ENABLE_COAP_SERVER_KEY);
        this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
                ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
        this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
                ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
        this.enableSystemPerf = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
                ConfigConst.ENABLE_SYSTEM_PERF_KEY);

        initManager();
    }

    private void initManager() {
        _Logger.info("Initializing sub-managers and connectors...");

        if (this.enableSystemPerf) {
            this.sysPerfMgr = new SystemPerformanceManager();
            this.sysPerfMgr.setDataMessageListener(this);
            _Logger.info("SystemPerformanceManager enabled and initialized.");
        }

        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            _Logger.info("MQTT Client connector created.");
        }

        if (this.enableCoapServer) {
            this.coapServer = new CoapServerGateway(this);
            _Logger.info("CoAP Server gateway created.");
        }

        if (this.enableCloudClient) {
            this.cloudClient = new CloudClientConnector();
            _Logger.info("Cloud Client connector created.");
        }

        if (this.enablePersistenceClient) {
            this.persistenceClient = new RedisPersistenceAdapter();
            _Logger.info("Redis Persistence adapter created.");
        }
    }

    public void startManager() {
        _Logger.info("Starting DeviceDataManager...");

        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.startManager();
        }

        if (this.mqttClient != null) {
            this.mqttClient.connectClient();
        }

        if (this.cloudClient != null) {
            this.cloudClient.connectClient();
        }

        if (this.coapServer != null) {
            this.coapServer.startServer();
        }
    }

    public void stopManager() {
        _Logger.info("Stopping DeviceDataManager...");

        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.stopManager();
        }

        if (this.mqttClient != null) {
            this.mqttClient.disconnectClient();
        }

        if (this.cloudClient != null) {
            this.cloudClient.disconnectClient();
        }

        if (this.coapServer != null) {
            this.coapServer.stopServer();
        }
    }

    // ==========================================================================
    // IDataMessageListener methods
    // ==========================================================================

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data) {
        if (data != null) {
            _Logger.info("Handling actuator command request: " + data.getName());
            IActuatorDataListener listener = actuatorListeners.get(data.getName());
            if (listener != null) {
                // Call the correct method from IActuatorDataListener
                return listener.onActuatorDataUpdate(data);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
        if (data != null) {
            _Logger.info("Handling actuator command response: " + data.getName());
            if (data.hasError()) {
                _Logger.warning("ActuatorData error flag set.");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
        if (msg != null) {
            _Logger.info("Handling incoming message: " + msg);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
        if (data != null) {
            _Logger.info("Handling sensor message: " + data.getName());
            if (data.hasError()) {
                _Logger.warning("SensorData error flag set.");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
        if (data != null) {
            _Logger.info("Handling system performance message: " + data.getName());
            if (data.hasError()) {
                _Logger.warning("SystemPerformanceData error flag set.");
            }
            return true;
        }
        return false;
    }

    public boolean handleSystemStateMessage(ResourceNameEnum resourceName, SystemStateData data) {
        if (data != null) {
            _Logger.info("Handling system state message: " + data.getName());
            return true;
        }
        return false;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener) {
        if (name != null && listener != null) {
            actuatorListeners.put(name, listener);
            _Logger.info("Registered actuator listener: " + name);
        }
    }

    // ==========================================================================
    // Private helpers
    // ==========================================================================

    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData data) {
        _Logger.log(Level.FINE, "Analyzing incoming actuator data...");
    }

    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData data) {
        _Logger.log(Level.FINE, "Analyzing incoming system state data...");
    }

    private boolean handleUpstreamTransmission(ResourceNameEnum resourceName, String jsonData, int qos) {
        _Logger.log(Level.FINE, "Handling upstream transmission...");
        return false;
    }
}
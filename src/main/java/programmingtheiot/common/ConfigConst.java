package programmingtheiot.common;

/**
 * Configuration constants for the Programming the IoT (PIOT) Gateway Device Application.
 * This class centralizes all configuration keys, default values, and identifiers.
 */
public class ConfigConst {
    
    
    // ========================================
    // GENERAL CONFIGURATION
    // ========================================
    public static final String TEST_EMPTY_APP_KEY = "testEmptyApp";
    
    public static final String NOT_SET = "Not Set";
    public static final String DEFAULT_CONFIG_FILE_NAME = "config/PiotConfig.props";
    public static final String CONFIG_FILE_KEY = "configFile";
    public static final String CRED_FILE_KEY = "credFile";
    
    // ========================================
    // DEVICE IDENTIFIERS
    // ========================================
    
    public static final String PRODUCT_NAME = "PIOT";
    public static final String GATEWAY_DEVICE = "GatewayDevice";
    public static final String CONSTRAINED_DEVICE = "ConstrainedDevice";
    public static final String DEVICE_LOCATION_ID_KEY = "deviceLocationID";
    public static final String LOCATION_ID_PROP = "location.id";
    
    // ========================================
    // SERVICE IDENTIFIERS
    // ========================================
    
    public static final String CLOUD_GATEWAY_SERVICE = "CloudGatewayService";
    public static final String MQTT_GATEWAY_SERVICE = "Mqtt.GatewayService";
    public static final String COAP_GATEWAY_SERVICE = "Coap.GatewayService";
    public static final String SMTP_GATEWAY_SERVICE = "SmtpGatewayService";
    public static final String DATA_GATEWAY_SERVICE = "Data.GatewayService";
    
    // ========================================
    // NETWORK CONFIGURATION KEYS
    // ========================================
    
    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";
    public static final String SECURE_PORT_KEY = "securePort";
    public static final String KEEP_ALIVE_KEY = "keepAlive";
    
    // ========================================
    // DEFAULT NETWORK VALUES
    // ========================================
    
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_COAP_PORT = 5683;
    public static final String DEFAULT_COAP_PROTOCOL = "coap";
    public static final String DEFAULT_COAP_SECURE_PROTOCOL = "coaps";
    public static final int DEFAULT_COAP_SECURE_PORT = 5684;
    public static final int DEFAULT_KEEP_ALIVE = 60;
    public static final int DEFAULT_REDIS_PORT = 6379;
    
    // MQTT Default Values
    public static final int DEFAULT_MQTT_PORT = 1883;
    public static final int DEFAULT_MQTT_SECURE_PORT = 8883;
    public static final int DEFAULT_QOS = 1;
    
    // ========================================
    // SYSTEM CONFIGURATION KEYS
    // ========================================
    
    public static final String POLL_CYCLES_KEY = "pollCycleSecs";
    public static final String ENABLE_LOGGING_KEY = "enableLogging";
    public static final String ENABLE_CRYPT_KEY = "enableCrypt";
    public static final String ENABLE_RUN_FOREVER_KEY = "enableRunForever";
    public static final String ENABLE_MQTT_CLIENT_KEY = "enableMqttClient";
    public static final String ENABLE_COAP_SERVER_KEY = "enableCoapServer";
    public static final String ENABLE_COAP_CLIENT_KEY = "enableCoapClient";
    public static final String ENABLE_CLOUD_CLIENT_KEY = "enableCloudClient";
    public static final String ENABLE_PERSISTENCE_CLIENT_KEY = "enablePersistenceClient";
    public static final String ENABLE_PERSISTENCE_KEY = "enablePersistence";
    public static final String ENABLE_SYSTEM_PERF_KEY = "enableSystemPerformance";
    
     
    // ========================================
    // DEFAULT SYSTEM VALUES
    // ========================================
    
    public static final int DEFAULT_POLL_CYCLES = 60;
    public static final int DEFAULT_STATUS = 0;
    public static final int DEFAULT_TYPE_ID = 0;
    public static final int DEFAULT_TYPE = 0;
    public static final int DEFAULT_COMMAND = 0;
    public static final float DEFAULT_VAL = 0.0f;
    public static final int OFF_COMMAND = 0;
    
    public static final int SENSOR_TYPE_DEFAULT = 1;
    public static final int ACTUATOR_TYPE_DEFAULT = 2;
    public static final int SYS_TYPE_DEFAULT = 3;

    // ========================================
    // AUTHENTICATION KEYS
    // ========================================
    
    public static final String USER_NAME_TOKEN_KEY = "userToken";
    public static final String USER_AUTH_TOKEN_KEY = "authToken";
    
    // MQTT Authentication and Security Keys
    public static final String ENABLE_AUTH_KEY = "enableAuth";
    public static final String USER_NAME_KEY = "userName";
    public static final String CERT_FILE_KEY = "pemFileName";
    public static final String USE_CLEAN_SESSION_KEY = "useCleanSession";
    
    // ========================================
    // RESOURCE NAMES - Messages
    // ========================================
    
    public static final String UPDATE_NOTIFICATIONS_MSG = "UpdateMsg";
    public static final String MEDIA_MSG = "MediaMsg";
    public static final String SENSOR_MSG = "SensorMsg";
    public static final String ACTUATOR_CMD = "ActuatorCmd";
    public static final String ACTUATOR_RESPONSE = "ActuatorResponse";
    public static final String MGMT_STATUS_MSG = "MgmtStatusMsg";
    public static final String MGMT_STATUS_CMD = "MgmtStatusCmd";
    public static final String RESOURCE_REGISTRATION_REQUEST = "ResourceReg";
    public static final String SYSTEM_PERF_MSG = "SystemPerfMsg";
    
    // ========================================
    // DATA TYPE IDENTIFIERS
    // ========================================
    
    public static final String SYS_PERF_DATA = "SystemPerformanceData";
    public static final String SYS_STATE_DATA = "SystemStateData";
    
    // ========================================
    // SENSOR/ACTUATOR NAMES
    // ========================================
    
    public static final String TEMP_SENSOR_NAME = "TempSensor";
    public static final String DEFAULT_SENSOR_NAME = "DefaultSensor";
    public static final String DEFAULT_ACTUATOR_NAME = "Not Set";
    
    // ========================================
    // TEST CONFIGURATION
    // ========================================
    
    public static final String TEST_GDA_DATA_PATH_KEY = "testGdaDataPath";
    public static final String TEST_CDA_DATA_PATH_KEY = "testCdaDataPath";
    
    // ========================================
    // JSON PROPERTY NAMES
    // ========================================
    
    public static final String TIMESTAMP_PROP = "timeStamp";
    public static final String VALUE_PROP = "value";
    
 // Commands
    public static final int COMMAND_OFF = 0;

    public static final int COMMAND_ON = 1;
    public static final int ON_COMMAND = 1;

    // Actuator names
    public static final String HUMIDIFIER_ACTUATOR_NAME = "HumidifierActuator";
    public static final String HVAC_ACTUATOR_NAME = "HvacActuator";

    // Sensor types
    public static final int HUMIDITY_SENSOR_TYPE = 3;
    public static final int TEMP_SENSOR_TYPE = 1;

    // Actuator types
    public static final int HUMIDIFIER_ACTUATOR_TYPE = 2;
    public static final int HVAC_ACTUATOR_TYPE = 1;
    
    /**
     * Configuration constants for fermentation control system.
     * Add these to your existing ConfigConst.java file.
     */

    // ========================================================================
    // Fermentation Profile Types
    // ========================================================================

    public static final int FERMENTATION_PROFILE_ACTUATOR_TYPE = 20;

    public static final String FERMENTATION_PROFILE_ALE = "ALE";
    public static final String FERMENTATION_PROFILE_LAGER = "LAGER";
    public static final String FERMENTATION_PROFILE_CONDITIONING = "CONDITIONING";
    public static final String FERMENTATION_PROFILE_COLD_CRASH = "COLD_CRASH";
    
    public static final int PRESSURE_SENSOR_TYPE = 1012;  // Or whatever value you use

    // ========================================================================
    // ALE Profile Thresholds
    // ========================================================================

    public static final float ALE_TEMP_MIN = 68.0f;
    public static final float ALE_TEMP_MAX = 72.0f;
    public static final float ALE_TEMP_NOMINAL = 70.0f;
    public static final float ALE_HUMIDITY_MIN = 60.0f;
    public static final float ALE_HUMIDITY_MAX = 70.0f;
    public static final float ALE_HUMIDITY_NOMINAL = 65.0f;

    // ========================================================================
    // LAGER Profile Thresholds
    // ========================================================================

    public static final float LAGER_TEMP_MIN = 50.0f;
    public static final float LAGER_TEMP_MAX = 55.0f;
    public static final float LAGER_TEMP_NOMINAL = 52.0f;
    public static final float LAGER_HUMIDITY_MIN = 55.0f;
    public static final float LAGER_HUMIDITY_MAX = 65.0f;
    public static final float LAGER_HUMIDITY_NOMINAL = 60.0f;

    // ========================================================================
    // CONDITIONING Profile Thresholds
    // ========================================================================

    public static final float CONDITIONING_TEMP_MIN = 55.0f;
    public static final float CONDITIONING_TEMP_MAX = 65.0f;
    public static final float CONDITIONING_TEMP_NOMINAL = 60.0f;
    public static final float CONDITIONING_HUMIDITY_MIN = 50.0f;
    public static final float CONDITIONING_HUMIDITY_MAX = 60.0f;
    public static final float CONDITIONING_HUMIDITY_NOMINAL = 55.0f;

    // ========================================================================
    // COLD_CRASH Profile Thresholds
    // ========================================================================

    public static final float COLD_CRASH_TEMP_MIN = 35.0f;
    public static final float COLD_CRASH_TEMP_MAX = 40.0f;
    public static final float COLD_CRASH_TEMP_NOMINAL = 38.0f;
    public static final float COLD_CRASH_HUMIDITY_MIN = 50.0f;
    public static final float COLD_CRASH_HUMIDITY_MAX = 60.0f;
    public static final float COLD_CRASH_HUMIDITY_NOMINAL = 55.0f;

    // ========================================================================
    // Alert Thresholds
    // ========================================================================

    public static final float TEMP_SPIKE_THRESHOLD = 5.0f;
    public static final float TEMP_CRITICAL_HIGH = 80.0f;
    public static final float TEMP_CRITICAL_LOW = 32.0f;
    public static final float HUMIDITY_CRITICAL_LOW = 30.0f;
    public static final float HUMIDITY_CRITICAL_HIGH = 85.0f;

    // ========================================================================
    // Actuation Response Configuration
    // ========================================================================

    public static final int ACTUATION_RESPONSE_TIME_SECS = 30;

    // ========================================================================
    // HVAC Actuator Commands
    // ========================================================================

    public static final String HVAC_COOLING_CMD = "COOLING";
    public static final String HVAC_HEATING_CMD = "HEATING";
    public static final String HVAC_OFF_CMD = "OFF";

    // ========================================================================
    // Humidifier Actuator Commands
    // ========================================================================

    public static final String HUMIDIFIER_ON_CMD = "ON";
    public static final String HUMIDIFIER_OFF_CMD = "OFF";

    // ========================================================================
    // LED Display Actuator Commands
    // ========================================================================

    public static final String LED_OPTIMAL_CMD = "OPTIMAL";
    public static final String LED_ACTIVE_CMD = "ACTIVE";
    public static final String LED_ALERT_CMD = "ALERT";
    public static final String LED_OFF_CMD = "OFF";
    
    // ========================================
    // CONSTRUCTOR (private to prevent instantiation)
    // ========================================
    
    private ConfigConst() {
        // Private constructor to prevent instantiation
    }
}
package programmingtheiot.common;

/**
 * Configuration constants for the Programming the IoT (PIOT) Gateway Device Application.
 * This class centralizes all configuration keys, default values, and identifiers.
 */
public class ConfigConst {
    
    // ========================================
    // GENERAL CONFIGURATION
    // ========================================
    
    public static final String NOT_SET = "Not Set";
    public static final String DEFAULT_CONFIG_FILE_NAME = "PiotConfig.props";
    public static final String CONFIG_FILE_KEY = "configFile";
    public static final String CRED_FILE_KEY = "credFile";
    
    // ========================================
    // DEVICE IDENTIFIERS
    // ========================================
    
    public static final String PRODUCT_NAME = "PIOT";
    public static final String GATEWAY_DEVICE = "GatewayDevice";
    public static final String CONSTRAINED_DEVICE = "ConstrainedDevice";
    public static final String DEVICE_LOCATION_ID_KEY = "deviceLocationID";
    
    // ========================================
    // SERVICE IDENTIFIERS
    // ========================================
    
    public static final String CLOUD_GATEWAY_SERVICE = "CloudGatewayService";
    public static final String MQTT_GATEWAY_SERVICE = "MqttGatewayService";
    public static final String SMTP_GATEWAY_SERVICE = "SmtpGatewayService";
    
    // ========================================
    // NETWORK CONFIGURATION KEYS
    // ========================================
    
    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";
    public static final String KEEP_ALIVE_KEY = "keepAlive";
    
    // ========================================
    // DEFAULT NETWORK VALUES
    // ========================================
    
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_COAP_PORT = 5683;
    public static final String DEFAULT_COAP_PROTOCOL = "coap";
    public static final int DEFAULT_KEEP_ALIVE = 60;
    
    // ========================================
    // SYSTEM CONFIGURATION KEYS
    // ========================================
    
    public static final String POLL_CYCLES_KEY = "pollCycleSecs";
    public static final String ENABLE_LOGGING_KEY = "enableLogging";
    public static final String ENABLE_CRYPT_KEY = "enableCrypt";
    public static final String ENABLE_RUN_FOREVER_KEY = "enableRunForever";
    
    // ========================================
    // DEFAULT SYSTEM VALUES
    // ========================================
    
    public static final int DEFAULT_POLL_CYCLES = 60;
    public static final int DEFAULT_STATUS = 0;
    public static final int DEFAULT_TYPE_ID = 0;
    public static final int DEFAULT_COMMAND = 0;
    public static final float DEFAULT_VAL = 0.0f;
    public static final int OFF_COMMAND = 0;
    
    // ========================================
    // AUTHENTICATION KEYS
    // ========================================
    
    public static final String USER_NAME_TOKEN_KEY = "userToken";
    public static final String USER_AUTH_TOKEN_KEY = "authToken";
    
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
    public static final String DEFAULT_ACTUATOR_NAME = "DefaultActuator";
    public static final int DEFAULT_TYPE = 0;
    
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
    
    // ========================================
    // CONSTRUCTOR (private to prevent instantiation)
    // ========================================
    
    private ConfigConst() {
        // Private constructor to prevent instantiation
    }
}
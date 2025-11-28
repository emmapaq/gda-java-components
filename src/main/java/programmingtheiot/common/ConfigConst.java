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
    public static final String CLOUD_DEVICE = "CloudDevice";

    public static final String DEVICE_LOCATION_ID_KEY = "deviceLocationID";
    public static final String LOCATION_ID_PROP = "location.id";

    // ========================================
    // SERVICE IDENTIFIERS
    // ========================================

    public static final String CLOUD_GATEWAY_SERVICE = "Cloud.GatewayService";
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
    public static final String CLIENT_ID_KEY = "clientID";
    public static final String CLOUD_CLIENT_ID_KEY = "cloudClientID";

    // ========================================
    // DEFAULT NETWORK VALUES
    // ========================================

//    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_HOST = "tcp://localhost:1883";
    public static final String DEFAULT_MQTT_PROTOCOL = "tcp";
    public static final int DEFAULT_COAP_PORT = 5683;
    public static final String DEFAULT_COAP_PROTOCOL = "coap";
    public static final String DEFAULT_COAP_SECURE_PROTOCOL = "coaps";
    public static final int DEFAULT_COAP_SECURE_PORT = 5684;
    public static final int DEFAULT_KEEP_ALIVE = 60;
    public static final int DEFAULT_REDIS_PORT = 6379;

    public static final int DEFAULT_MQTT_PORT = 1883;
    public static final int DEFAULT_MQTT_SECURE_PORT = 8883;
    public static final int DEFAULT_QOS = 1;

    public static final String DEFAULT_CLOUD_CLIENT_ID = "CloudClient01";

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
    // SENSOR / ACTUATOR NAMES
    // ========================================

    public static final String TEMP_SENSOR_NAME = "TempSensor";
    public static final String HUMIDITY_SENSOR_NAME = "HumiditySensor";
    public static final String PRESSURE_SENSOR_NAME = "PressureSensor";
    public static final String DEFAULT_SENSOR_NAME = "DefaultSensor";

    public static final String DEFAULT_ACTUATOR_NAME = "Not Set";
    public static final String HUMIDIFIER_ACTUATOR_NAME = "HumidifierActuator";
    public static final String HVAC_ACTUATOR_NAME = "HvacActuator";
    public static final String LED_ACTUATOR_NAME = "LedActuator";
    public static final String SYSTEM_PERF_NAME = "SystemPerformanceMsg";

    // ========================================
    // SENSOR TYPES
    // ========================================

    public static final int TEMP_SENSOR_TYPE = 1;
    public static final int HUMIDITY_SENSOR_TYPE = 3;
    public static final int PRESSURE_SENSOR_TYPE = 4;

    // ========================================
    // ACTUATOR TYPES
    // ========================================

    public static final int HVAC_ACTUATOR_TYPE = 1;
    public static final int HUMIDIFIER_ACTUATOR_TYPE = 2;
    public static final int LED_ACTUATOR_TYPE = 3;

    // ========================================
    // COMMANDS
    // ========================================

    public static final int COMMAND_OFF = 0;
    public static final int COMMAND_ON = 1;
    public static final int ON_COMMAND = 1;

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
    // TOPIC AND QOS KEYS
    // ========================================

    public static final String BASE_TOPIC_KEY = "baseTopic";
    public static final String DEFAULT_QOS_KEY = "defaultQos";
    public static final String CLOUD_SERVICE_NAME_KEY = "cloudServiceName";

    // ========================================
    // SYSTEM PERFORMANCE METRIC NAMES
    // ========================================

    public static final String CPU_UTIL_NAME = "CpuUtil";
    public static final String MEM_UTIL_NAME = "MemUtil";

    // ========================================
    // CONSTRUCTOR
    // ========================================

    private ConfigConst() {
        // prevent instantiation
    }
}

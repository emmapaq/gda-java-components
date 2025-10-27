package programmingtheiot.gda.connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPersistenceListener;

/**
 * Redis-based persistence adapter for storing and retrieving IoT data.
 * Supports ActuatorData, SensorData, and SystemPerformanceData.
 */
public class RedisPersistenceAdapter implements IPersistenceClient {
    
    private static final Logger _Logger = 
        Logger.getLogger(RedisPersistenceAdapter.class.getName());
    
    // Default configuration values
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    
    private String host;
    private int port;
    private Jedis jedisClient;
    private boolean isConnected = false;
    private DataUtil dataUtil;
    
    /**
     * Constructor - initializes Redis client configuration from properties file.
     */
    public RedisPersistenceAdapter() {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        // Read configuration from Data.GatewayService section
        this.host = configUtil.getProperty(
            ConfigConst.DATA_GATEWAY_SERVICE, 
            ConfigConst.HOST_KEY, 
            DEFAULT_HOST);
        
        this.port = configUtil.getInteger(
            ConfigConst.DATA_GATEWAY_SERVICE, 
            ConfigConst.PORT_KEY, 
            DEFAULT_PORT);
        
        // Initialize DataUtil for JSON conversion
        this.dataUtil = DataUtil.getInstance();
        
        _Logger.info("Redis configuration loaded: " + this.host + ":" + this.port);
    }
    
    @Override
    public boolean connectClient() {
        if (this.isConnected) {
            _Logger.warning("Redis client already connected.");
            return true;
        }
        
        try {
            this.jedisClient = new Jedis(this.host, this.port);
            this.jedisClient.connect();
            
            // Test connection with ping
            String response = this.jedisClient.ping();
            
            if ("PONG".equalsIgnoreCase(response)) {
                this.isConnected = true;
                _Logger.info("Successfully connected to Redis at " + 
                    this.host + ":" + this.port);
                return true;
            } else {
                _Logger.warning("Redis connection test failed. Unexpected response: " + response);
                return false;
            }
            
        } catch (JedisException e) {
            _Logger.log(Level.SEVERE, "Failed to connect to Redis at " + 
                this.host + ":" + this.port, e);
            this.isConnected = false;
            return false;
        }
    }
    
    @Override
    public boolean disconnectClient() {
        if (!this.isConnected) {
            _Logger.warning("Redis client already disconnected.");
            return true;
        }
        
        try {
            if (this.jedisClient != null) {
                this.jedisClient.close();
                this.isConnected = false;
                _Logger.info("Successfully disconnected from Redis.");
                return true;
            }
            return true;
            
        } catch (JedisException e) {
            _Logger.log(Level.SEVERE, "Failed to disconnect from Redis.", e);
            return false;
        }
    }
    
    @Override
    public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate) {
        if (!this.isConnected) {
            _Logger.warning("Cannot retrieve data - Redis client not connected.");
            return new ActuatorData[0];
        }
        
        List<ActuatorData> dataList = new ArrayList<>();
        
        try {
            String pattern = generateTopicKey(topic) + ":actuator:*";
            Set<String> keys = this.jedisClient.keys(pattern);
            
            for (String key : keys) {
                String jsonData = this.jedisClient.get(key);
                if (jsonData != null && !jsonData.isEmpty()) {
                    ActuatorData data = this.dataUtil.jsonToActuatorData(jsonData);
                    
                    if (data != null) {
                        // Filter by date range if provided
                        if (isWithinDateRange(data.getTimeStamp(), startDate, endDate)) {
                            dataList.add(data);
                        }
                    }
                }
            }
            
            _Logger.info("Retrieved " + dataList.size() + " ActuatorData records for topic: " + topic);
            
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to retrieve ActuatorData from Redis.", e);
        }
        
        return dataList.toArray(new ActuatorData[0]);
    }
    
    @Override
    public SensorData[] getSensorData(String topic, Date startDate, Date endDate) {
        if (!this.isConnected) {
            _Logger.warning("Cannot retrieve data - Redis client not connected.");
            return new SensorData[0];
        }
        
        List<SensorData> dataList = new ArrayList<>();
        
        try {
            String pattern = generateTopicKey(topic) + ":sensor:*";
            Set<String> keys = this.jedisClient.keys(pattern);
            
            for (String key : keys) {
                String jsonData = this.jedisClient.get(key);
                if (jsonData != null && !jsonData.isEmpty()) {
                    SensorData data = this.dataUtil.jsonToSensorData(jsonData);
                    
                    if (data != null) {
                        // Filter by date range if provided
                        if (isWithinDateRange(data.getTimeStamp(), startDate, endDate)) {
                            dataList.add(data);
                        }
                    }
                }
            }
            
            _Logger.info("Retrieved " + dataList.size() + " SensorData records for topic: " + topic);
            
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to retrieve SensorData from Redis.", e);
        }
        
        return dataList.toArray(new SensorData[0]);
    }
    
    @Override
    public boolean storeData(String topic, int qos, ActuatorData... data) {
        if (!this.isConnected) {
            _Logger.warning("Cannot store data - Redis client not connected.");
            return false;
        }
        
        if (data == null || data.length == 0) {
            _Logger.warning("No ActuatorData provided to store.");
            return false;
        }
        
        try {
            for (ActuatorData actuatorData : data) {
                String key = generateDataKey(topic, "actuator", actuatorData.getTimeStamp());
                String jsonData = this.dataUtil.actuatorDataToJson(actuatorData);
                
                this.jedisClient.set(key, jsonData);
            }
            
            _Logger.info("Stored " + data.length + " ActuatorData records for topic: " + topic);
            return true;
            
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to store ActuatorData to Redis.", e);
            return false;
        }
    }
    
    @Override
    public boolean storeData(String topic, int qos, SensorData... data) {
        if (!this.isConnected) {
            _Logger.warning("Cannot store data - Redis client not connected.");
            return false;
        }
        
        if (data == null || data.length == 0) {
            _Logger.warning("No SensorData provided to store.");
            return false;
        }
        
        try {
            for (SensorData sensorData : data) {
                String key = generateDataKey(topic, "sensor", sensorData.getTimeStamp());
                String jsonData = this.dataUtil.sensorDataToJson(sensorData);
                
                this.jedisClient.set(key, jsonData);
            }
            
            _Logger.info("Stored " + data.length + " SensorData records for topic: " + topic);
            return true;
            
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to store SensorData to Redis.", e);
            return false;
        }
    }
    
    @Override
    public boolean storeData(String topic, int qos, SystemPerformanceData... data) {
        if (!this.isConnected) {
            _Logger.warning("Cannot store data - Redis client not connected.");
            return false;
        }
        
        if (data == null || data.length == 0) {
            _Logger.warning("No SystemPerformanceData provided to store.");
            return false;
        }
        
        try {
            for (SystemPerformanceData perfData : data) {
                String key = generateDataKey(topic, "sysperf", perfData.getTimeStamp());
                String jsonData = this.dataUtil.systemPerformanceDataToJson(perfData);
                
                this.jedisClient.set(key, jsonData);
            }
            
            _Logger.info("Stored " + data.length + " SystemPerformanceData records for topic: " + topic);
            return true;
            
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to store SystemPerformanceData to Redis.", e);
            return false;
        }
    }
    
    @Override
    public void registerDataStorageListener(
            Class dataType, 
            IPersistenceListener listener, 
            String... topics) {
        // Not implemented for Redis persistence adapter
        _Logger.warning("registerDataStorageListener not implemented for Redis adapter.");
    }
    
    // Private helper methods
    
    /**
     * Generates a Redis key prefix from a topic string.
     * Replaces forward slashes with colons for Redis key hierarchy.
     */
    private String generateTopicKey(String topic) {
        if (topic == null || topic.isEmpty()) {
            return "gda:data";
        }
        return "gda:data:" + topic.replace("/", ":");
    }
    
    /**
     * Generates a unique Redis key for storing data.
     * Format: gda:data:{topic}:{dataType}:{timestamp}
     */
    private String generateDataKey(String topic, String dataType, long timestamp) {
        return generateTopicKey(topic) + ":" + dataType + ":" + timestamp;
    }
    
    /**
     * Checks if a timestamp falls within a date range.
     * Null dates are treated as unbounded (no filter applied).
     */
    private boolean isWithinDateRange(long timestamp, Date startDate, Date endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        
        if (startDate != null && timestamp < startDate.getTime()) {
            return false;
        }
        
        if (endDate != null && timestamp > endDate.getTime()) {
            return false;
        }
        
        return true;
    }
}
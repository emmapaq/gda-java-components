package programmingtheiot.gda.connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Redis-based persistence adapter for storing and retrieving IoT data.
 * 
 * Provides persistence operations for ActuatorData, SensorData, and 
 * SystemPerformanceData using Redis as the backing store.
 */
public class RedisPersistenceAdapter implements IPersistenceClient {
    
    private static final Logger _Logger = 
        Logger.getLogger(RedisPersistenceAdapter.class.getName());
    
    // Redis client
    private Jedis jedis = null;
    
    // Configuration
    private String host = ConfigConst.DEFAULT_HOST;
    private int port = 6379; // Default Redis port
    
    // Data utilities
    private DataUtil dataUtil = DataUtil.getInstance();
    
    // Listener management
    @SuppressWarnings("rawtypes")
    private Map<Class, List<IPersistenceListener>> listenerMap = new HashMap<>();
    private Map<IPersistenceListener, List<String>> topicMap = new HashMap<>();
    
    /**
     * Constructor.
     * Initializes the Redis client with configuration from PiotConfig.props
     */
    public RedisPersistenceAdapter() {
        super();
        
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        // Load host and port from configuration
        // Using "gateway.GatewayDeviceApp" as the section name
        this.host = configUtil.getProperty(
            "gateway.GatewayDeviceApp", 
            ConfigConst.HOST_KEY, 
            ConfigConst.DEFAULT_HOST);
        
        this.port = configUtil.getInteger(
            "gateway.GatewayDeviceApp", 
            ConfigConst.PORT_KEY, 
            6379);
        
        _Logger.info("Redis Persistence Adapter initialized with host: " + 
            this.host + ", port: " + this.port);
        
        // Create Jedis instance
        try {
            this.jedis = new Jedis(this.host, this.port);
            _Logger.info("Jedis client created successfully.");
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to create Jedis client.", e);
        }
    }
    
    // ========================================================================
    // IPersistenceClient implementation
    // ========================================================================
    
    @Override
    public boolean connectClient() {
        try {
            if (this.jedis != null && this.jedis.isConnected()) {
                _Logger.warning("Redis client is already connected.");
                return true;
            }
            
            if (this.jedis == null) {
                this.jedis = new Jedis(this.host, this.port);
            }
            
            // Test connection with PING
            String response = this.jedis.ping();
            
            if ("PONG".equalsIgnoreCase(response)) {
                _Logger.info("Successfully connected to Redis at " + 
                    this.host + ":" + this.port);
                return true;
            } else {
                _Logger.warning("Redis connection test failed. Response: " + response);
                return false;
            }
        } catch (JedisException e) {
            _Logger.log(Level.SEVERE, "Failed to connect to Redis.", e);
            return false;
        }
    }
    
    @Override
    public boolean disconnectClient() {
        try {
            if (this.jedis == null || !this.jedis.isConnected()) {
                _Logger.warning("Redis client is already disconnected.");
                return true;
            }
            
            this.jedis.close();
            _Logger.info("Successfully disconnected from Redis.");
            return true;
        } catch (JedisException e) {
            _Logger.log(Level.SEVERE, "Failed to disconnect from Redis.", e);
            return false;
        }
    }
    
    @Override
    public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate) {
        if (topic == null || topic.isEmpty()) {
            _Logger.warning("Topic is null or empty. Cannot retrieve ActuatorData.");
            return new ActuatorData[0];
        }
        
        try {
            if (this.jedis == null || !this.jedis.isConnected()) {
                _Logger.warning("Redis client is not connected. Cannot retrieve data.");
                return new ActuatorData[0];
            }
            
            List<ActuatorData> dataList = new ArrayList<>();
            
            // Get all keys matching the topic pattern
            String pattern = topic + ":*";
            Set<String> keys = this.jedis.keys(pattern);
            
            for (String key : keys) {
                String jsonData = this.jedis.get(key);
                if (jsonData != null && !jsonData.isEmpty()) {
                    ActuatorData data = this.dataUtil.jsonToActuatorData(jsonData);
                    
                    // Filter by date range if provided
                    if (data != null && isWithinDateRange(data.getTimeStamp(), startDate, endDate)) {
                        dataList.add(data);
                    }
                }
            }
            
            _Logger.info("Retrieved " + dataList.size() + " ActuatorData entries for topic: " + topic);
            return dataList.toArray(new ActuatorData[0]);
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to retrieve ActuatorData for topic: " + topic, e);
            return new ActuatorData[0];
        }
    }
    
    @Override
    public SensorData[] getSensorData(String topic, Date startDate, Date endDate) {
        if (topic == null || topic.isEmpty()) {
            _Logger.warning("Topic is null or empty. Cannot retrieve SensorData.");
            return new SensorData[0];
        }
        
        try {
            if (this.jedis == null || !this.jedis.isConnected()) {
                _Logger.warning("Redis client is not connected. Cannot retrieve data.");
                return new SensorData[0];
            }
            
            List<SensorData> dataList = new ArrayList<>();
            
            // Get all keys matching the topic pattern
            String pattern = topic + ":*";
            Set<String> keys = this.jedis.keys(pattern);
            
            for (String key : keys) {
                String jsonData = this.jedis.get(key);
                if (jsonData != null && !jsonData.isEmpty()) {
                    SensorData data = this.dataUtil.jsonToSensorData(jsonData);
                    
                    // Filter by date range if provided
                    if (data != null && isWithinDateRange(data.getTimeStamp(), startDate, endDate)) {
                        dataList.add(data);
                    }
                }
            }
            
            _Logger.info("Retrieved " + dataList.size() + " SensorData entries for topic: " + topic);
            return dataList.toArray(new SensorData[0]);
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to retrieve SensorData for topic: " + topic, e);
            return new SensorData[0];
        }
    }
    
    @Override
    public boolean storeData(String topic, int qos, ActuatorData... data) {
        if (topic == null || topic.isEmpty()) {
            _Logger.warning("Topic is null or empty. Cannot store ActuatorData.");
            return false;
        }
        
        if (data == null || data.length == 0) {
            _Logger.warning("No ActuatorData provided to store.");
            return false;
        }
        
        try {
            if (this.jedis == null || !this.jedis.isConnected()) {
                _Logger.warning("Redis client is not connected. Cannot store data.");
                return false;
            }
            
            int successCount = 0;
            
            for (ActuatorData actuatorData : data) {
                if (actuatorData != null) {
                    String jsonData = this.dataUtil.actuatorDataToJson(actuatorData);
                    String key = topic + ":" + System.currentTimeMillis() + ":" + actuatorData.getName();
                    
                    String result = this.jedis.set(key, jsonData);
                    
                    if ("OK".equalsIgnoreCase(result)) {
                        successCount++;
                        // Notify listeners after successful storage
                        notifyListeners(ActuatorData.class, topic, actuatorData);
                    }
                }
            }
            
            _Logger.info("Stored " + successCount + " of " + data.length + 
                " ActuatorData entries for topic: " + topic);
            
            return successCount == data.length;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to store ActuatorData for topic: " + topic, e);
            return false;
        }
    }
    
    @Override
    public boolean storeData(String topic, int qos, SensorData... data) {
        if (topic == null || topic.isEmpty()) {
            _Logger.warning("Topic is null or empty. Cannot store SensorData.");
            return false;
        }
        
        if (data == null || data.length == 0) {
            _Logger.warning("No SensorData provided to store.");
            return false;
        }
        
        try {
            if (this.jedis == null || !this.jedis.isConnected()) {
                _Logger.warning("Redis client is not connected. Cannot store data.");
                return false;
            }
            
            int successCount = 0;
            
            for (SensorData sensorData : data) {
                if (sensorData != null) {
                    String jsonData = this.dataUtil.sensorDataToJson(sensorData);
                    String key = topic + ":" + System.currentTimeMillis() + ":" + sensorData.getName();
                    
                    String result = this.jedis.set(key, jsonData);
                    
                    if ("OK".equalsIgnoreCase(result)) {
                        successCount++;
                        // Notify listeners after successful storage
                        notifyListeners(SensorData.class, topic, sensorData);
                    }
                }
            }
            
            _Logger.info("Stored " + successCount + " of " + data.length + 
                " SensorData entries for topic: " + topic);
            
            return successCount == data.length;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to store SensorData for topic: " + topic, e);
            return false;
        }
    }
    
    @Override
    public boolean storeData(String topic, int qos, SystemPerformanceData... data) {
        if (topic == null || topic.isEmpty()) {
            _Logger.warning("Topic is null or empty. Cannot store SystemPerformanceData.");
            return false;
        }
        
        if (data == null || data.length == 0) {
            _Logger.warning("No SystemPerformanceData provided to store.");
            return false;
        }
        
        try {
            if (this.jedis == null || !this.jedis.isConnected()) {
                _Logger.warning("Redis client is not connected. Cannot store data.");
                return false;
            }
            
            int successCount = 0;
            
            for (SystemPerformanceData sysPerfData : data) {
                if (sysPerfData != null) {
                    String jsonData = this.dataUtil.systemPerformanceDataToJson(sysPerfData);
                    String key = topic + ":" + System.currentTimeMillis() + ":" + sysPerfData.getName();
                    
                    String result = this.jedis.set(key, jsonData);
                    
                    if ("OK".equalsIgnoreCase(result)) {
                        successCount++;
                    }
                }
            }
            
            _Logger.info("Stored " + successCount + " of " + data.length + 
                " SystemPerformanceData entries for topic: " + topic);
            
            return successCount == data.length;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to store SystemPerformanceData for topic: " + topic, e);
            return false;
        }
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public void registerDataStorageListener(
        Class dataType, 
        IPersistenceListener listener, 
        String... topics) {
        
        if (listener == null) {
            _Logger.warning("Listener is null. Cannot register.");
            return;
        }
        
        if (dataType == null) {
            _Logger.warning("DataType is null. Cannot register listener.");
            return;
        }
        
        try {
            // Add listener to the map by data type
            if (!this.listenerMap.containsKey(dataType)) {
                this.listenerMap.put(dataType, new ArrayList<>());
            }
            
            List<IPersistenceListener> listeners = this.listenerMap.get(dataType);
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
            
            // Store topics for this listener
            if (topics != null && topics.length > 0) {
                List<String> topicList = new ArrayList<>();
                for (String topic : topics) {
                    if (topic != null && !topic.isEmpty()) {
                        topicList.add(topic);
                    }
                }
                this.topicMap.put(listener, topicList);
            }
            
            _Logger.info("Successfully registered listener for data type: " + 
                dataType.getSimpleName() + 
                (topics != null && topics.length > 0 ? 
                    " with " + topics.length + " topic(s)" : ""));
            
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to register listener for data type: " + 
                dataType.getSimpleName(), e);
        }
    }
    
    /**
     * Unregisters a persistence listener.
     * 
     * @param listener The listener to unregister
     */
    public void unregisterDataStorageListener(IPersistenceListener listener) {
        if (listener == null) {
            _Logger.warning("Listener is null. Cannot unregister.");
            return;
        }
        
        boolean removed = false;
        
        // Remove from all data type lists
        for (List<IPersistenceListener> listeners : this.listenerMap.values()) {
            if (listeners.remove(listener)) {
                removed = true;
            }
        }
        
        // Remove topic mappings
        this.topicMap.remove(listener);
        
        if (removed) {
            _Logger.info("Successfully unregistered listener.");
        } else {
            _Logger.warning("Listener was not registered.");
        }
    }
    
    /**
     * Notifies registered listeners when data is stored.
     * 
     * @param dataType The class type of the data
     * @param topic The topic associated with the data
     * @param data The data that was stored
     */
    @SuppressWarnings("rawtypes")
    private void notifyListeners(Class dataType, String topic, Object data) {
        if (!this.listenerMap.containsKey(dataType)) {
            return;
        }
        
        List<IPersistenceListener> listeners = this.listenerMap.get(dataType);
        
        for (IPersistenceListener listener : listeners) {
            // Check if listener is registered for this topic
            List<String> registeredTopics = this.topicMap.get(listener);
            
            if (registeredTopics == null || registeredTopics.isEmpty() || 
                registeredTopics.contains(topic)) {
                
                try {
                    // Call the listener's callback method if it exists
                    // Since IPersistenceListener may not have onDataStored method,
                    // we just log the notification
                    _Logger.fine("Notifying listener of data storage for type: " + 
                        dataType.getSimpleName() + " on topic: " + topic);
                } catch (Exception e) {
                    _Logger.log(Level.WARNING, 
                        "Listener threw exception during notification", e);
                }
            }
        }
    }
    
    // ========================================================================
    // Private helper methods
    // ========================================================================
    
    /**
     * Checks if a timestamp falls within the specified date range.
     * 
     * @param timestamp The timestamp to check (in milliseconds since epoch)
     * @param startDate The start date (inclusive), or null for no lower bound
     * @param endDate The end date (inclusive), or null for no upper bound
     * @return true if the timestamp is within range, false otherwise
     */
    private boolean isWithinDateRange(long timestamp, Date startDate, Date endDate) {
        if (startDate != null && timestamp < startDate.getTime()) {
            return false;
        }
        
        if (endDate != null && timestamp > endDate.getTime()) {
            return false;
        }
        
        return true;
    }
}
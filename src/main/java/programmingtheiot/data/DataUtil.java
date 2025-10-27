package programmingtheiot.data;

import java.util.logging.Logger;
import com.google.gson.Gson;

/**
 * Utility class for converting data objects to/from JSON format.
 * Uses Gson library for JSON serialization and deserialization.
 */
public class DataUtil {
    
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(DataUtil.class.getName());
    
    private static final DataUtil _Instance = new DataUtil();
    
    public static final DataUtil getInstance() {
        return _Instance;
    }
    
    // private var's
    
    private Gson gson = null;
    
    // constructors
    
    private DataUtil() {
        super();
        this.gson = new Gson();
    }
    
    // public methods
    
    // ActuatorData conversions
    
    public String actuatorDataToJson(ActuatorData data) {
        String jsonData = null;
        
        if (data != null) {
            jsonData = this.gson.toJson(data);
        }
        
        return jsonData;
    }
    
    public ActuatorData jsonToActuatorData(String jsonData) {
        ActuatorData data = null;
        
        if (jsonData != null && jsonData.trim().length() > 0) {
            data = this.gson.fromJson(jsonData, ActuatorData.class);
        }
        
        return data;
    }
    
    // SensorData conversions
    
    public String sensorDataToJson(SensorData data) {
        String jsonData = null;
        
        if (data != null) {
            jsonData = this.gson.toJson(data);
        }
        
        return jsonData;
    }
    
    public SensorData jsonToSensorData(String jsonData) {
        SensorData data = null;
        
        if (jsonData != null && jsonData.trim().length() > 0) {
            data = this.gson.fromJson(jsonData, SensorData.class);
        }
        
        return data;
    }
    
    // SystemPerformanceData conversions
    
    public String systemPerformanceDataToJson(SystemPerformanceData data) {
        String jsonData = null;
        
        if (data != null) {
            jsonData = this.gson.toJson(data);
        }
        
        return jsonData;
    }
    
    public SystemPerformanceData jsonToSystemPerformanceData(String jsonData) {
        SystemPerformanceData data = null;
        
        if (jsonData != null && jsonData.trim().length() > 0) {
            data = this.gson.fromJson(jsonData, SystemPerformanceData.class);
        }
        
        return data;
    }
    
    // SystemStateData conversions
    
    public String systemStateDataToJson(SystemStateData data) {
        String jsonData = null;
        
        if (data != null) {
            jsonData = this.gson.toJson(data);
        }
        
        return jsonData;
    }
    
    public SystemStateData jsonToSystemStateData(String jsonData) {
        SystemStateData data = null;
        
        if (jsonData != null && jsonData.trim().length() > 0) {
            data = this.gson.fromJson(jsonData, SystemStateData.class);
        }
        
        return data;
    }
}
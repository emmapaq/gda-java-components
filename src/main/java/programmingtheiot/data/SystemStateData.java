package programmingtheiot.data;

import java.util.ArrayList;
import java.util.List;
import programmingtheiot.common.ConfigConst;

/**
 * Represents the state of the system including multiple sensor readings.
 */
public class SystemStateData extends BaseIotData {
    private static final long serialVersionUID = 1L;

    private int command = ConfigConst.DEFAULT_COMMAND;
    private int stateData = ConfigConst.DEFAULT_TYPE;
    private List<SensorData> sensorDataList = null;
    private List<SystemPerformanceData> sysPerfDataList = null;

    /**
     * Default constructor.
     */
    public SystemStateData() {
        super();
        this.sensorDataList = new ArrayList<>();
    }

    /**
     * Constructor with typeID.
     * 
     * @param typeID The type identifier
     */
    public SystemStateData(int typeID) {
        super(ConfigConst.SYS_STATE_DATA, typeID);
        this.sensorDataList = new ArrayList<>();
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
        updateTimeStamp();
    }

    public int getStateData() {
        return stateData;
    }

    public void setStateData(int stateData) {
        this.stateData = stateData;
        updateTimeStamp();
    }

    /**
     * Returns the list of sensor data.
     * 
     * @return List of SensorData objects
     */
    public List<SensorData> getSensorDataList() {
        return this.sensorDataList;
    }

    /**
     * Adds a sensor data entry to the list.
     * 
     * @param data The SensorData to add
     */
    public void addSensorData(SensorData data) {
        if (data != null) {
            if (this.sensorDataList == null) {
                this.sensorDataList = new ArrayList<>();
            }
            this.sensorDataList.add(data);
            updateTimeStamp();
        }
    }

    /**
     * Sets the entire sensor data list.
     * 
     * @param sensorDataList The list of SensorData objects
     */
    public void setSensorDataList(List<SensorData> sensorDataList) {
        if (sensorDataList != null) {
            this.sensorDataList = sensorDataList;
            updateTimeStamp();
        }
    }

    // ========================================
    // PROTECTED METHODS
    // ========================================

    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof SystemStateData) {
            SystemStateData ssd = (SystemStateData) data;
            this.command = ssd.getCommand();
            this.stateData = ssd.getStateData();
            
            // Deep copy sensor data list
            if (ssd.getSensorDataList() != null) {
                this.sensorDataList = new ArrayList<>(ssd.getSensorDataList());
            }
        }
    }

    // ========================================
    // OVERRIDDEN METHODS
    // ========================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Command: ").append(this.command).append("\n");
        sb.append("State Data: ").append(this.stateData).append("\n");
        sb.append("Sensor Data Count: ");
        sb.append(this.sensorDataList != null ? this.sensorDataList.size() : 0);
        sb.append("\n");
        
        if (this.sensorDataList != null && !this.sensorDataList.isEmpty()) {
            sb.append("Sensor Data:\n");
            for (SensorData sd : this.sensorDataList) {
                sb.append("  - ").append(sd.getName()).append(": ");
                sb.append(sd.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private List<SystemPerformanceData> perfDataList = new ArrayList<>();

    public void addSystemPerformanceData(SystemPerformanceData data) {
        if (data != null) {
            perfDataList.add(data);
        }
    }

    public List<SystemPerformanceData> getSystemPerformanceDataList() {
        return perfDataList;
    }

}
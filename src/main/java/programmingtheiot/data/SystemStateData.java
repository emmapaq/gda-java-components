package programmingtheiot.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import programmingtheiot.common.ConfigConst;

/**
 * Represents the state of the system including multiple sensor readings
 * and system performance data.
 */
public class SystemStateData extends BaseIotData implements Serializable {
    
    // static
    
    private static final long serialVersionUID = 1L;
    
    // private var's
    
    private int command = ConfigConst.DEFAULT_COMMAND;
    private int stateData = ConfigConst.DEFAULT_TYPE;
    private List<SensorData> sensorDataList = null;
    private List<SystemPerformanceData> sysPerfDataList = null;
    
    // constructors
    
    /**
     * Default constructor.
     */
    public SystemStateData() {
        super();
        super.setName(ConfigConst.SYS_STATE_DATA);
        this.sensorDataList = new ArrayList<>();
        this.sysPerfDataList = new ArrayList<>();
    }
    
    /**
     * Constructor with typeID.
     * 
     * @param typeID The type identifier
     */
    public SystemStateData(int typeID) {
        super(ConfigConst.SYS_STATE_DATA, typeID);
        this.sensorDataList = new ArrayList<>();
        this.sysPerfDataList = new ArrayList<>();
    }
    
    // public methods
    
    public int getCommand() {
        return this.command;
    }
    
    public void setCommand(int command) {
        updateTimeStamp();
        this.command = command;
    }
    
    public int getStateData() {
        return this.stateData;
    }
    
    public void setStateData(int stateData) {
        updateTimeStamp();
        this.stateData = stateData;
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
    
    /**
     * Returns the list of system performance data.
     * 
     * @return List of SystemPerformanceData objects
     */
    public List<SystemPerformanceData> getSystemPerformanceDataList() {
        return this.sysPerfDataList;
    }
    
    /**
     * Adds a system performance data entry to the list.
     * 
     * @param data The SystemPerformanceData to add
     */
    public void addSystemPerformanceData(SystemPerformanceData data) {
        if (data != null) {
            if (this.sysPerfDataList == null) {
                this.sysPerfDataList = new ArrayList<>();
            }
            this.sysPerfDataList.add(data);
            updateTimeStamp();
        }
    }
    
    /**
     * Sets the entire system performance data list.
     * 
     * @param sysPerfDataList The list of SystemPerformanceData objects
     */
    public void setSystemPerformanceDataList(List<SystemPerformanceData> sysPerfDataList) {
        if (sysPerfDataList != null) {
            this.sysPerfDataList = sysPerfDataList;
            updateTimeStamp();
        }
    }
    
    // protected methods
    
    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof SystemStateData) {
            SystemStateData ssd = (SystemStateData) data;
            this.setCommand(ssd.getCommand());
            this.setStateData(ssd.getStateData());
            
            // Deep copy sensor data list
            if (ssd.getSensorDataList() != null) {
                this.sensorDataList = new ArrayList<>(ssd.getSensorDataList());
            }
            
            // Deep copy system performance data list
            if (ssd.getSystemPerformanceDataList() != null) {
                this.sysPerfDataList = new ArrayList<>(ssd.getSystemPerformanceDataList());
            }
        }
    }
    
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
        
        sb.append("System Performance Data Count: ");
        sb.append(this.sysPerfDataList != null ? this.sysPerfDataList.size() : 0);
        sb.append("\n");
        
        if (this.sysPerfDataList != null && !this.sysPerfDataList.isEmpty()) {
            sb.append("System Performance Data:\n");
            for (SystemPerformanceData spd : this.sysPerfDataList) {
                sb.append("  - CPU: ").append(spd.getCpuUtilization());
                sb.append(", Mem: ").append(spd.getMemoryUtilization());
                sb.append(", Disk: ").append(spd.getDiskUtilization()).append("\n");
            }
        }
        
        return sb.toString();
    }
}
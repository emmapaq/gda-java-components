package programmingtheiot.data;

import programmingtheiot.common.ConfigConst;

/**
 * Represents system performance metrics including CPU, memory, and disk usage.
 */
public class SystemPerformanceData extends BaseIotData {
    private static final long serialVersionUID = 1L;

    private float cpuUtilization = ConfigConst.DEFAULT_VAL;
    private float memoryUtilization = ConfigConst.DEFAULT_VAL;
    private float diskUtilization = ConfigConst.DEFAULT_VAL;

    /**
     * Default constructor.
     */
    public SystemPerformanceData() {
        super(ConfigConst.SYS_PERF_DATA, ConfigConst.DEFAULT_TYPE);
    }

    /**
     * Constructor with typeID.
     * 
     * @param typeID The type identifier
     */
    public SystemPerformanceData(int typeID) {
        super(ConfigConst.SYS_PERF_DATA, typeID);
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    /**
     * Gets the CPU utilization percentage.
     * 
     * @return CPU utilization (0.0 to 100.0)
     */
    public float getCpuUtilization() {
        return cpuUtilization;
    }

    /**
     * Sets the CPU utilization percentage.
     * 
     * @param cpuUtilization CPU utilization (0.0 to 100.0)
     */
    public void setCpuUtilization(float cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
        updateTimeStamp();
    }

    /**
     * Gets the memory utilization percentage.
     * 
     * @return Memory utilization (0.0 to 100.0)
     */
    public float getMemoryUtilization() {
        return memoryUtilization;
    }

    /**
     * Sets the memory utilization percentage.
     * 
     * @param memoryUtilization Memory utilization (0.0 to 100.0)
     */
    public void setMemoryUtilization(float memoryUtilization) {
        this.memoryUtilization = memoryUtilization;
        updateTimeStamp();
    }

    /**
     * Gets the disk utilization percentage.
     * 
     * @return Disk utilization (0.0 to 100.0)
     */
    public float getDiskUtilization() {
        return diskUtilization;
    }

    /**
     * Sets the disk utilization percentage.
     * 
     * @param diskUtilization Disk utilization (0.0 to 100.0)
     */
    public void setDiskUtilization(float diskUtilization) {
        this.diskUtilization = diskUtilization;
        updateTimeStamp();
    }

    // ========================================
    // PROTECTED METHODS
    // ========================================

    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof SystemPerformanceData) {
            SystemPerformanceData spd = (SystemPerformanceData) data;
            this.cpuUtilization = spd.getCpuUtilization();
            this.memoryUtilization = spd.getMemoryUtilization();
            this.diskUtilization = spd.getDiskUtilization();
        }
    }

    // ========================================
    // OVERRIDDEN METHODS
    // ========================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("CPU Utilization: ").append(this.cpuUtilization).append("%\n");
        sb.append("Memory Utilization: ").append(this.memoryUtilization).append("%\n");
        sb.append("Disk Utilization: ").append(this.diskUtilization).append("%\n");
        return sb.toString();
    }
}
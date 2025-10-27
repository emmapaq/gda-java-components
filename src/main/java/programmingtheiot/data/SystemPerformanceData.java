package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;

/**
 * Data container for system performance metrics.
 */
public class SystemPerformanceData extends BaseIotData implements Serializable {
    
    // static
    
    private static final long serialVersionUID = 1L;
    
    // private var's
    
    private float cpuUtil  = ConfigConst.DEFAULT_VAL;
    private float diskUtil = ConfigConst.DEFAULT_VAL;
    private float memUtil  = ConfigConst.DEFAULT_VAL;
    
    // constructors
    
    /**
     * Default constructor.
     */
    public SystemPerformanceData() {
        super();
        super.setName(ConfigConst.SYS_PERF_DATA);
    }
    
    // public methods
    
    public float getCpuUtilization() {
        return this.cpuUtil;
    }
    
    public float getDiskUtilization() {
        return this.diskUtil;
    }
    
    public float getMemoryUtilization() {
        return this.memUtil;
    }
    
    public void setCpuUtilization(float val) {
        updateTimeStamp();
        this.cpuUtil = val;
    }
    
    public void setDiskUtilization(float val) {
        updateTimeStamp();
        this.diskUtil = val;
    }
    
    public void setMemoryUtilization(float val) {
        updateTimeStamp();
        this.memUtil = val;
    }
    
    // protected methods
    
    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof SystemPerformanceData) {
            SystemPerformanceData spdData = (SystemPerformanceData) data;
            this.setCpuUtilization(spdData.getCpuUtilization());
            this.setDiskUtilization(spdData.getDiskUtilization());
            this.setMemoryUtilization(spdData.getMemoryUtilization());
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("CPU Utilization: ").append(this.cpuUtil).append("\n");
        sb.append("Disk Utilization: ").append(this.diskUtil).append("\n");
        sb.append("Memory Utilization: ").append(this.memUtil).append("\n");
        return sb.toString();
    }
}
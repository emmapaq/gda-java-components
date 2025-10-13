package programmingtheiot.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemStateData extends BaseIotData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<SensorData> sensorDataList = new ArrayList<>();
    private List<SystemPerformanceData> sysPerfDataList = new ArrayList<>();

    public SystemStateData() {
        super();
        // Optional name, type can be set here
    }

    public List<SensorData> getSensorDataList() {
        return this.sensorDataList;
    }

    public void addSensorData(SensorData data) {
        if (data != null) {
            sensorDataList.add(data);
            updateTimeStamp();
        }
    }

    public List<SystemPerformanceData> getSysPerfDataList() {
        return this.sysPerfDataList;
    }

    public void addSysPerfData(SystemPerformanceData data) {
        if (data != null) {
            sysPerfDataList.add(data);
            updateTimeStamp();
        }
    }
}

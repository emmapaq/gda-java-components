package programmingtheiot.data;

import programmingtheiot.common.ConfigConst;

/**
 * Represents sensor data including a measured value.
 */
public class SensorData extends BaseIotData {
    private static final long serialVersionUID = 1L;

    private float value = ConfigConst.DEFAULT_VAL;

    /**
     * Default constructor.
     */
    public SensorData() {
        super();
        this.setName(ConfigConst.DEFAULT_SENSOR_NAME);
        this.setTypeID(ConfigConst.DEFAULT_TYPE);
    }

    /**
     * Constructor with typeID.
     * 
     * @param typeID The sensor type identifier
     */
    public SensorData(int typeID) {
        super(ConfigConst.DEFAULT_SENSOR_NAME, typeID);
    }

    /**
     * Constructor with name and typeID.
     * 
     * @param name The sensor name
     * @param typeID The sensor type identifier
     */
    public SensorData(String name, int typeID) {
        super(name, typeID);
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        updateTimeStamp();
    }

    // ========================================
    // PROTECTED METHODS
    // ========================================

    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof SensorData) {
            SensorData sd = (SensorData) data;
            this.value = sd.getValue();
        }
    }

    // ========================================
    // OVERRIDDEN METHODS
    // ========================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Value: ").append(this.value).append("\n");
        return sb.toString();
    }
}
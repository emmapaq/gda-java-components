package programmingtheiot.data;
import java.io.Serializable;
import programmingtheiot.common.ConfigConst;
/**
 * Represents sensor data including a value reading.
 */
public class SensorData extends BaseIotData implements Serializable {
    
    // static
    
    private static final long serialVersionUID = 1L;
    
    // private var's
    
    private float value = ConfigConst.DEFAULT_VAL;
    
    // constructors
    
    /**
     * Default constructor.
     * Name will be initialized to NOT_SET by BaseIotData.
     */
    public SensorData() {
        super();
    }
    
    /**
     * Constructor with specific type ID.
     * 
     * @param typeID The sensor type ID
     */
    public SensorData(int typeID) {
        super();
        this.setTypeID(typeID);
    }
    
    // public methods
    
    /**
     * Returns the sensor value.
     * 
     * @return The current value.
     */
    public float getValue() {
        return this.value;
    }
    
    /**
     * Sets the sensor value and updates the timestamp.
     * 
     * @param val The new value.
     */
    public void setValue(float val) {
        updateTimeStamp();
        this.value = val;
    }
    
    // protected methods
    
    /**
     * Updates this instance with data from another SensorData object.
     * 
     * @param data The incoming data to merge.
     */
    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof SensorData) {
            SensorData sData = (SensorData) data;
            this.setValue(sData.getValue());
        }
    }
    
    /**
     * Returns a string representation of the sensor data.
     */
    @Override
    public String toString() {
        return super.toString() + "Value: " + this.value + "\n";
    }
}
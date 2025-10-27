package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;


public abstract class BaseIotData implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String name = ConfigConst.NOT_SET;
    protected int typeID = ConfigConst.DEFAULT_TYPE;
    protected String locationID;
    protected long timeStamp = System.currentTimeMillis();
    protected int statusCode = ConfigConst.DEFAULT_STATUS;
    protected boolean hasError = false;

    /**
     * Default constructor.
     * Initializes with default values and loads location ID from configuration.
     */
    public BaseIotData() {
        // Load location ID from configuration
        this.locationID = ConfigUtil.getInstance().getProperty(
            ConfigConst.GATEWAY_DEVICE, 
            ConfigConst.DEVICE_LOCATION_ID_KEY, 
            ConfigConst.NOT_SET);
        this.timeStamp = System.currentTimeMillis();
    }

    /**
     * Constructor with name and type ID.
     * 
     * @param name The name of the data object
     * @param typeID The type identifier
     */
    public BaseIotData(String name, int typeID) {
        this(); // Call default constructor first to initialize location ID
        if (name != null) {
            this.name = name;
        }
        this.typeID = typeID;
    }

    /**
     * Gets the name of this data object.
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this data object.
     * 
     * @param name The name to set
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
            updateTimeStamp();
        }
    }

    /**
     * Gets the type ID of this data object.
     * 
     * @return The type ID
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * Sets the type ID of this data object.
     * 
     * @param typeID The type ID to set
     */
    public void setTypeID(int typeID) {
        this.typeID = typeID;
        updateTimeStamp();
    }

    /**
     * Gets the location ID of this data object.
     * 
     * @return The location ID
     */
    public String getLocationID() {
        return locationID;
    }

    /**
     * Sets the location ID of this data object.
     * 
     * @param locationID The location ID to set
     */
    public void setLocationID(String locationID) {
        if (locationID != null) {
            this.locationID = locationID;
            updateTimeStamp();
        }
    }

    /**
     * Gets the timestamp of this data object.
     * 
     * @return The timestamp
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Gets the timestamp in milliseconds.
     * 
     * @return The timestamp in milliseconds
     */
    public long getTimeStampMillis() {
        return timeStamp;
    }

    /**
     * Updates the timestamp to the current system time.
     */
    public void updateTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    /**
     * Gets the status code of this data object.
     * 
     * @return The status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the status code of this data object.
     * 
     * @param statusCode The status code to set
     */
    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
        // Automatically set hasError for negative status codes
        this.hasError = (statusCode < 0);
        updateTimeStamp();
    }

    
    /**
     * Checks if this data object has an error.
     * 
     * @return true if has error, false otherwise
     */
    public boolean hasError() {
        return hasError;
    }

    /**
     * Sets the error flag for this data object.
     * 
     * @param hasError The error flag to set
     */
    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        updateTimeStamp();
    }

    /**
     * Sets status data including status code and error flag.
     * 
     * @param statusCode The status code to set
     * @param hasError The error flag to set
     */
    public void setStatusData(int statusCode, boolean hasError)
    {
        this.statusCode = statusCode;
        this.hasError = hasError;
        updateTimeStamp();
    }

    /**
     * Updates this data object with data from another BaseIotData object.
     * 
     * @param data The source data object
     */
    public void updateData(BaseIotData data) {
        if (data != null) {
            this.name = data.getName();
            this.typeID = data.getTypeID();
            this.locationID = data.getLocationID();
            this.statusCode = data.getStatusCode();
            this.timeStamp = data.getTimeStamp();
            this.hasError = data.hasError();
            handleUpdateData(data);
        }
    }

    /**
     * Abstract method to handle update of subclass-specific data.
     * Must be implemented by subclasses.
     * 
     * @param data The source data object
     */
    protected abstract void handleUpdateData(BaseIotData data);

    /**
     * Returns a string representation of this data object.
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "Name: " + name + "\n" +
               "Type ID: " + typeID + "\n" +
               "Location ID: " + locationID + "\n" +
               "Time Stamp: " + timeStamp + "\n" +
               "Status Code: " + statusCode + "\n" +
               "Has Error: " + hasError + "\n";
    }
}
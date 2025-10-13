package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;

/**
 * Base class for all IoT data objects in the system.
 * Provides common properties and behaviors for sensors, actuators, and system data.
 */
public abstract class BaseIotData implements Serializable {
    private static final long serialVersionUID = 1L;

    // Instance variables
    protected String name = ConfigConst.NOT_SET;
    protected int typeID = ConfigConst.DEFAULT_TYPE_ID;
    protected String locationID = ConfigConst.NOT_SET;
    protected long timeStamp = System.currentTimeMillis();
    protected int statusCode = ConfigConst.DEFAULT_STATUS;
    protected boolean hasError = false;

    /**
     * Default constructor.
     */
    public BaseIotData() {
        super();
    }

    /**
     * Constructor with name and typeID.
     * 
     * @param name The name of this data instance
     * @param typeID The type identifier
     */
    public BaseIotData(String name, int typeID) {
        this.name = name;
        this.typeID = typeID;
        updateTimeStamp();
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateTimeStamp();
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
        updateTimeStamp();
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
        updateTimeStamp();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the timestamp in milliseconds.
     * This is an alias for getTimeStamp() to support different naming conventions.
     * 
     * @return The timestamp in milliseconds
     */
    public long getTimeStampMillis() {
        return timeStamp;
    }

    public void updateTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        updateTimeStamp();
    }

    public boolean hasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        updateTimeStamp();
    }

    // ========================================
    // PUBLIC METHODS
    // ========================================

    /**
     * Updates this data instance with values from another BaseIotData instance.
     * Calls handleUpdateData() for subclass-specific update logic.
     * 
     * @param data The source data to copy from
     */
    public void updateData(BaseIotData data) {
        if (data != null) {
            this.name = data.getName();
            this.typeID = data.getTypeID();
            this.locationID = data.getLocationID();
            this.statusCode = data.getStatusCode();
            this.timeStamp = data.getTimeStamp();
            this.hasError = data.hasError();
            
            // Call subclass-specific update logic
            handleUpdateData(data);
        }
    }

    // ========================================
    // PROTECTED ABSTRACT METHODS
    // ========================================

    /**
     * Template method for subclasses to implement specific update logic.
     * This is called by updateData() after common fields are copied.
     * 
     * @param data The source data to copy subclass-specific fields from
     */
    protected abstract void handleUpdateData(BaseIotData data);

    // ========================================
    // OVERRIDDEN METHODS
    // ========================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(this.name).append("\n");
        sb.append("Type ID: ").append(this.typeID).append("\n");
        sb.append("Location ID: ").append(this.locationID).append("\n");
        sb.append("Time Stamp: ").append(this.timeStamp).append("\n");
        sb.append("Status Code: ").append(this.statusCode).append("\n");
        sb.append("Has Error: ").append(this.hasError).append("\n");
        return sb.toString();
    }
}
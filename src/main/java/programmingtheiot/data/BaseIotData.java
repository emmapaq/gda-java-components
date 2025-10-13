package programmingtheiot.data;

import java.io.Serializable;

/**
 * Base class for all IoT data containers.
 * Provides common fields such as name, typeID, timestamp, and statusCode.
 * 
 * This class serves as the foundation for SensorData, ActuatorData, and
 * SystemPerformanceData, providing core attributes and helper methods
 * to update and synchronize IoT-related data.
 * 
 * @author 
 */
public class BaseIotData implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Default values
	protected String name = "Not Set";
	protected int typeID = 0;
	protected long timeStamp = System.currentTimeMillis();
	protected int statusCode = 0;

	// Constructors
	public BaseIotData()
	{
		super();
	}

	/**
	 * Full constructor that initializes name and typeID.
	 * Automatically updates timestamp.
	 * 
	 * @param name The name of the IoT data source
	 * @param typeID The data type identifier
	 */
	public BaseIotData(String name, int typeID)
	{
		super();
		this.name = name;
		this.typeID = typeID;
		this.updateTimeStamp();
	}

	// ======== Getters and Setters ========

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
		this.updateTimeStamp();
	}

	public int getTypeID()
	{
		return this.typeID;
	}

	public void setTypeID(int typeID)
	{
		this.typeID = typeID;
		this.updateTimeStamp();
	}

	public long getTimeStamp()
	{
		return this.timeStamp;
	}

	/**
	 * Returns the timestamp in milliseconds (alias for getTimeStamp()).
	 * @return timeStamp value
	 */
	public long getTimeStampMillis()
	{
		return this.timeStamp;
	}

	public void updateTimeStamp()
	{
		this.timeStamp = System.currentTimeMillis();
	}

	public int getStatusCode()
	{
		return this.statusCode;
	}

	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
		this.updateTimeStamp();
	}

	// ======== Data Handling Methods ========

	/**
	 * Internal helper to copy data fields from another BaseIotData instance.
	 * Subclasses should use updateData() instead of this directly.
	 * 
	 * @param data The BaseIotData instance to copy from
	 */
	protected void handleUpdateData(BaseIotData data)
	{
		if (data != null)
		{
			this.name = data.getName();
			this.typeID = data.getTypeID();
			this.statusCode = data.getStatusCode();
			this.timeStamp = data.getTimeStampMillis();
		}
	}

	/**
	 * Public method for subclasses to update their state from another IoT data object.
	 * 
	 * @param data The data object to copy values from
	 */
	public void updateData(BaseIotData data)
	{
		handleUpdateData(data);
	}

	@Override
	public String toString()
	{
		return "BaseIotData [name=" + name + 
				", typeID=" + typeID + 
				", timeStamp=" + timeStamp + 
				", statusCode=" + statusCode + "]";
	}
}

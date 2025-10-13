package programmingtheiot.data;

import programmingtheiot.common.ConfigConst;

/**
 * Represents actuator data including command and value.
 */
public class ActuatorData extends BaseIotData {
    private static final long serialVersionUID = 1L;

    private int command = ConfigConst.DEFAULT_COMMAND;
    private float value = ConfigConst.DEFAULT_VAL;
    private boolean isResponse = false;

    /**
     * Default constructor.
     */
    public ActuatorData() {
        super();
        this.setName(ConfigConst.DEFAULT_ACTUATOR_NAME);
        this.setTypeID(ConfigConst.DEFAULT_TYPE);
    }

    /**
     * Constructor with typeID.
     * 
     * @param typeID The actuator type identifier
     */
    public ActuatorData(int typeID) {
        super(ConfigConst.DEFAULT_ACTUATOR_NAME, typeID);
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        updateTimeStamp();
    }

    public boolean isResponse() {
        return isResponse;
    }

    public void setAsResponse(boolean isResponse) {
        this.isResponse = isResponse;
        updateTimeStamp();
    }

    // ========================================
    // PROTECTED METHODS
    // ========================================

    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof ActuatorData) {
            ActuatorData ad = (ActuatorData) data;
            this.command = ad.getCommand();
            this.value = ad.getValue();
            this.isResponse = ad.isResponse();
        }
    }

    // ========================================
    // OVERRIDDEN METHODS
    // ========================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Command: ").append(this.command).append("\n");
        sb.append("Value: ").append(this.value).append("\n");
        sb.append("Is Response: ").append(this.isResponse).append("\n");
        return sb.toString();
    }
}
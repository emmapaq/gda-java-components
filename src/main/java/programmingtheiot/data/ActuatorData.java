package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;

/**
 * Data container for actuator commands and responses.
 */
public class ActuatorData extends BaseIotData implements Serializable {
    
    // static
    
    private static final long serialVersionUID = 1L;
    
    // private var's
    
    private int     command    = ConfigConst.DEFAULT_COMMAND;
    private float   value      = ConfigConst.DEFAULT_VAL;
    private boolean isResponse = false;
    private String  stateData  = "";
    
    // constructors
    
    /**
     * Default constructor.
     */
    public ActuatorData() {
        super();
    }
    
    // public methods
    
    public int getCommand() {
        return this.command;
    }
    
    public String getStateData() {
        return this.stateData;
    }
    
    public float getValue() {
        return this.value;
    }
    
    public boolean isResponseFlagEnabled() {
        return this.isResponse;
    }
    
    public void setAsResponse() {
        updateTimeStamp();
        this.isResponse = true;
    }
    
    public void setCommand(int command) {
        updateTimeStamp();
        this.command = command;
    }
    
    public void setStateData(String stateData) {
        updateTimeStamp();
        
        if (stateData != null) {
            this.stateData = stateData;
        }
    }
    
    public void setValue(float val) {
        updateTimeStamp();
        this.value = val;
    }
    
    // protected methods
    
    @Override
    protected void handleUpdateData(BaseIotData data) {
        if (data instanceof ActuatorData) {
            ActuatorData aData = (ActuatorData) data;
            this.setCommand(aData.getCommand());
            this.setValue(aData.getValue());
            this.setStateData(aData.getStateData());
            
            if (aData.isResponseFlagEnabled()) {
                this.isResponse = true;
            }
        }
    }
}
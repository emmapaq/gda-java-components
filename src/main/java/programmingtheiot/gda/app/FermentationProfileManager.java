package programmingtheiot.gda.app;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.data.ActuatorData;

import java.util.logging.Logger;

/**
 * Manages fermentation profiles and generates appropriate actuation commands.
 */
public class FermentationProfileManager {
    
    private static final Logger _Logger =
        Logger.getLogger(FermentationProfileManager.class.getName());
    
    private String currentProfile = ConfigConst.FERMENTATION_PROFILE_ALE;
    
    // Profile thresholds
    private float currentTempMin;
    private float currentTempMax;
    private float currentHumidityMin;
    private float currentHumidityMax;
    
    public FermentationProfileManager() {
        // Initialize with ALE profile by default
        setProfile(ConfigConst.FERMENTATION_PROFILE_ALE);
    }
    
    /**
     * Set the current fermentation profile.
     * 
     * @param profileName Profile name (ALE, LAGER, CONDITIONING, COLD_CRASH)
     * @return True if profile was set successfully
     */
    public boolean setProfile(String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            return false;
        }
        
        switch (profileName.toUpperCase()) {
            case ConfigConst.FERMENTATION_PROFILE_ALE:
                this.currentTempMin = ConfigConst.ALE_TEMP_MIN;
                this.currentTempMax = ConfigConst.ALE_TEMP_MAX;
                this.currentHumidityMin = ConfigConst.ALE_HUMIDITY_MIN;
                this.currentHumidityMax = ConfigConst.ALE_HUMIDITY_MAX;
                break;
                
            case ConfigConst.FERMENTATION_PROFILE_LAGER:
                this.currentTempMin = ConfigConst.LAGER_TEMP_MIN;
                this.currentTempMax = ConfigConst.LAGER_TEMP_MAX;
                this.currentHumidityMin = ConfigConst.LAGER_HUMIDITY_MIN;
                this.currentHumidityMax = ConfigConst.LAGER_HUMIDITY_MAX;
                break;
                
            case ConfigConst.FERMENTATION_PROFILE_CONDITIONING:
                this.currentTempMin = ConfigConst.CONDITIONING_TEMP_MIN;
                this.currentTempMax = ConfigConst.CONDITIONING_TEMP_MAX;
                this.currentHumidityMin = ConfigConst.CONDITIONING_HUMIDITY_MIN;
                this.currentHumidityMax = ConfigConst.CONDITIONING_HUMIDITY_MAX;
                break;
                
            case ConfigConst.FERMENTATION_PROFILE_COLD_CRASH:
                this.currentTempMin = ConfigConst.COLD_CRASH_TEMP_MIN;
                this.currentTempMax = ConfigConst.COLD_CRASH_TEMP_MAX;
                this.currentHumidityMin = ConfigConst.COLD_CRASH_HUMIDITY_MIN;
                this.currentHumidityMax = ConfigConst.COLD_CRASH_HUMIDITY_MAX;
                break;
                
            default:
                _Logger.warning("Unknown fermentation profile: " + profileName);
                return false;
        }
        
        this.currentProfile = profileName.toUpperCase();
        _Logger.info("Fermentation profile set to: " + this.currentProfile);
        
        return true;
    }
    
    /**
     * Generate an ActuatorData command to change the CDA's fermentation profile.
     * 
     * @param profileName The profile to switch to
     * @return ActuatorData containing the profile change command
     */
    public ActuatorData generateProfileChangeCommand(String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            _Logger.warning("Cannot generate profile change command: profile name is null or empty");
            return null;
        }
        
        ActuatorData actuatorData = new ActuatorData();
        actuatorData.setTypeID(ConfigConst.FERMENTATION_PROFILE_ACTUATOR_TYPE);
        actuatorData.setName("Fermentation Profile Change");
        actuatorData.setValue(0);  // Not used for profile changes
        actuatorData.setCommand(0);  // Not used for profile changes
        actuatorData.setStateData(profileName.toUpperCase());  // Profile name stored here
        
        _Logger.info("Generated profile change command: " + profileName);
        
        return actuatorData;
    }
    
    /**
     * Get current profile name.
     */
    public String getCurrentProfile() {
        return this.currentProfile;
    }
    
    /**
     * Get current temperature min for active profile.
     */
    public float getCurrentTempMin() {
        return this.currentTempMin;
    }
    
    /**
     * Get current temperature max for active profile.
     */
    public float getCurrentTempMax() {
        return this.currentTempMax;
    }
    
    /**
     * Get current humidity min for active profile.
     */
    public float getCurrentHumidityMin() {
        return this.currentHumidityMin;
    }
    
    /**
     * Get current humidity max for active profile.
     */
    public float getCurrentHumidityMax() {
        return this.currentHumidityMax;
    }
}
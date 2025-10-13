package programmingtheiot.common;

/**
 * Enumeration of resource names used in the IoT system.
 * Provides mappings for different device types and message categories.
 */
public enum ResourceNameEnum {
    
    // ========================================
    // CONSTRAINED DEVICE RESOURCES
    // ========================================
    
    CDA_UPDATE_NOTIFICATIONS_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.UPDATE_NOTIFICATIONS_MSG
    ),
    
    CDA_MEDIA_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.MEDIA_MSG
    ),
    
    CDA_SENSOR_MSG_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.SENSOR_MSG
    ),
    
    CDA_ACTUATOR_CMD_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.ACTUATOR_CMD
    ),
    
    CDA_ACTUATOR_RESPONSE_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.ACTUATOR_RESPONSE
    ),
    
    CDA_MGMT_STATUS_MSG_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.MGMT_STATUS_MSG
    ),
    
    CDA_MGMT_STATUS_CMD_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.MGMT_STATUS_CMD
    ),
    
    CDA_RESOURCE_REGISTRATION_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.RESOURCE_REGISTRATION_REQUEST
    ),
    
    CDA_SYSTEM_PERF_MSG_RESOURCE(
        ConfigConst.CONSTRAINED_DEVICE,
        ConfigConst.SYSTEM_PERF_MSG
    ),
    
    // ========================================
    // GATEWAY DEVICE RESOURCES
    // ========================================
    
    GDA_UPDATE_NOTIFICATIONS_RESOURCE(
        ConfigConst.GATEWAY_DEVICE,
        ConfigConst.UPDATE_NOTIFICATIONS_MSG
    ),
    
    GDA_MEDIA_RESOURCE(
        ConfigConst.GATEWAY_DEVICE,
        ConfigConst.MEDIA_MSG
    ),
    
    GDA_MGMT_STATUS_MSG_RESOURCE(
        ConfigConst.GATEWAY_DEVICE,
        ConfigConst.MGMT_STATUS_MSG
    ),
    
    GDA_MGMT_STATUS_CMD_RESOURCE(
        ConfigConst.GATEWAY_DEVICE,
        ConfigConst.MGMT_STATUS_CMD
    ),
    
    GDA_RESOURCE_REGISTRATION_RESOURCE(
        ConfigConst.GATEWAY_DEVICE,
        ConfigConst.RESOURCE_REGISTRATION_REQUEST
    ),
    
    GDA_SYSTEM_PERF_MSG_RESOURCE(
        ConfigConst.GATEWAY_DEVICE,
        ConfigConst.SYSTEM_PERF_MSG
    );
    
    // ========================================
    // INSTANCE VARIABLES
    // ========================================
    
    private String deviceName;
    private String resourceType;
    private String resourceName;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Private constructor for enum constants.
     * 
     * @param deviceName The device name (e.g., GatewayDevice, ConstrainedDevice)
     * @param resourceType The resource type (e.g., SensorMsg, ActuatorCmd)
     */
    private ResourceNameEnum(String deviceName, String resourceType) {
        this.deviceName = deviceName;
        this.resourceType = resourceType;
        this.resourceName = createResourceName(deviceName, resourceType);
    }
    
    // ========================================
    // PUBLIC METHODS
    // ========================================
    
    /**
     * Gets the device name.
     * 
     * @return The device name
     */
    public String getDeviceName() {
        return this.deviceName;
    }
    
    /**
     * Gets the resource type.
     * 
     * @return The resource type
     */
    public String getResourceType() {
        return this.resourceType;
    }
    
    /**
     * Gets the full resource name.
     * 
     * @return The complete resource name (e.g., "PIOT/GatewayDevice/MgmtStatusMsg")
     */
    public String getResourceName() {
        return this.resourceName;
    }
    
    /**
     * Creates a resource name from device and resource type.
     * 
     * @param deviceName The device name
     * @param resourceType The resource type
     * @return The formatted resource name
     */
    private String createResourceName(String deviceName, String resourceType) {
        return ConfigConst.PRODUCT_NAME + "/" + deviceName + "/" + resourceType;
    }
    
    /**
     * Finds a ResourceNameEnum by its resource name string.
     * 
     * @param resourceName The resource name to search for
     * @return The matching ResourceNameEnum, or null if not found
     */
    public static ResourceNameEnum getEnumFromValue(String resourceName) {
        if (resourceName != null && !resourceName.isEmpty()) {
            for (ResourceNameEnum rne : ResourceNameEnum.values()) {
                if (rne.getResourceName().equals(resourceName)) {
                    return rne;
                }
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return this.resourceName;
    }
}
package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

/**
 * Factory class for creating ICloudClient instances.
 * Implements Singleton pattern.
 */
public class CloudClientFactory
{
    private static final Logger _Logger =
        Logger.getLogger(CloudClientFactory.class.getName());
    
    private static final CloudClientFactory _Instance = new CloudClientFactory();
    
    private ConfigUtil configUtil = ConfigUtil.getInstance();
    
    /**
     * Private constructor for Singleton pattern.
     */
    private CloudClientFactory()
    {
        super();
    }
    
    /**
     * Returns the singleton instance.
     * 
     * @return CloudClientFactory The singleton instance
     */
    public static final CloudClientFactory getInstance()
    {
        return _Instance;
    }
    
    /**
     * Creates an ICloudClient instance based on configuration.
     * Reads cloudServiceName from [Cloud.GatewayService] section.
     * 
     * @return ICloudClient The cloud client instance, or null if creation fails
     */
    public ICloudClient createCloudClient()
    {
        ICloudClient cloudClient = null;
        
        if (configUtil.hasProperty(ConfigConst.CLOUD_GATEWAY_SERVICE, "cloudServiceName")) {
            String cloudSvcName = configUtil.getProperty(
                ConfigConst.CLOUD_GATEWAY_SERVICE, 
                "cloudServiceName");
            
            if (cloudSvcName != null && cloudSvcName.trim().length() > 0) {
                _Logger.info("Creating cloud client for service: " + cloudSvcName);
                
                // Create CloudClientConnector with provider-specific config section
                String configSection = ConfigConst.CLOUD_GATEWAY_SERVICE + "." + cloudSvcName;
                cloudClient = new CloudClientConnector(configSection);
            }
        }
        
        if (cloudClient == null) {
            _Logger.info("No cloud service name specified. Using default configuration.");
            cloudClient = new CloudClientConnector();
        }
        
        return cloudClient;
    }
    
    /**
     * Creates an ICloudClient for a specific cloud service.
     * 
     * @param cloudServiceName The cloud service name (e.g., "AWS", "Ubidots")
     * @return ICloudClient The cloud client instance
     */
    public ICloudClient createCloudClient(String cloudServiceName)
    {
        if (cloudServiceName == null || cloudServiceName.trim().length() == 0) {
            return createCloudClient();
        }
        
        _Logger.info("Creating cloud client for: " + cloudServiceName);
        
        String configSection = ConfigConst.CLOUD_GATEWAY_SERVICE + "." + cloudServiceName;
        return new CloudClientConnector(configSection);
    }
}
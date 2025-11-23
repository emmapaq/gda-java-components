package programmingtheiot.gda.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * CoAP Client Connector for GET, PUT, POST, DELETE, and OBSERVE operations
 * 
 * This class provides CoAP client functionality using Eclipse Californium.
 * It supports both CON (confirmed) and NON (non-confirmed) messaging.
 * 
 * @author Emma
 */
public class CoapClientConnector {
    private static final Logger _Logger = 
        Logger.getLogger(CoapClientConnector.class.getName());
    
    // Static initializer to register Californium configuration modules
    static {
        try {
            org.eclipse.californium.core.config.CoapConfig.register();
            org.eclipse.californium.elements.config.UdpConfig.register();
        } catch (Exception e) {
            _Logger.warning("Failed to register Californium configuration: " + e.getMessage());
        }
    }
    
    private String protocol;
    private String host;
    private int port;
    private String serverAddr;
    private CoapClient clientConn;
    private IDataMessageListener dataMsgListener;
    private Map<String, CoapObserveRelation> observeRelations;
    
    /**
     * Constructor - initializes CoAP client with configuration from PiotConfig.props
     */
    public CoapClientConnector() {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        this.host = configUtil.getProperty(
            ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
        
        if (configUtil.getBoolean(
            ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
            this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
            this.port = configUtil.getInteger(
                ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
        } else {
            this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
            this.port = configUtil.getInteger(
                ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
        }
        
        this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;
        
        // Initialize the CoapClient
        this.clientConn = new CoapClient();
        
        // Initialize observe relations map
        this.observeRelations = new HashMap<>();
        
        _Logger.info("CoAP Client will use server address: " + this.serverAddr);
    }
    
    /**
     * Sends a GET request to the specified resource
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @return boolean True if request was sent successfully
     */
    public boolean sendGetRequest(ResourceNameEnum resource, boolean enableCON) {
        if (resource == null) {
            _Logger.warning("Resource is null. Cannot send GET request.");
            return false;
        }
        
        return sendGetRequest(resource, enableCON, 0);
    }
    
    /**
     * Sends a GET request to the specified resource with timeout
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendGetRequest(ResourceNameEnum resource, boolean enableCON, int timeout) {
        if (resource == null) {
            _Logger.warning("Resource is null. Cannot send GET request.");
            return false;
        }
        
        CoapResponse response = null;
        String targetUri = this.serverAddr + "/" + resource.getResourceName();
        
        try {
            if (enableCON) {
                this.clientConn.useCONs();
                _Logger.info("Using CON messaging for GET request");
            } else {
                this.clientConn.useNONs();
                _Logger.info("Using NON messaging for GET request");
            }
            
            this.clientConn.setURI(targetUri);
            _Logger.info("Sending GET request to: " + targetUri);
            
            if (timeout > 0) {
                this.clientConn.setTimeout((long) timeout * 1000);
                _Logger.info("Timeout set to: " + timeout + " seconds");
            }
            
            response = this.clientConn.get();
            
            if (response != null) {
                _Logger.info("Handling GET. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
                    response.getCode() + " - " + response.getResponseText());
                
                if (this.dataMsgListener != null) {
                    // TODO: Parse the response payload and convert to appropriate message type
                }
                
                return true;
            } else {
                _Logger.warning("Handling GET. No response received from: " + targetUri);
                _Logger.warning("Possible causes: Server not running, resource not found, network timeout");
            }
        } catch (Exception e) {
            _Logger.severe("Exception during GET request to " + targetUri + ": " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Sends a GET request to the specified resource with optional path and timeout
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param path Optional path to append to resource (can be null)
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendGetRequest(ResourceNameEnum resource, String path, boolean enableCON, int timeout) {
        // For now, ignore the path parameter and use the basic implementation
        return sendGetRequest(resource, enableCON, timeout);
    }
    
    /**
     * Sends an asynchronous GET request (currently implemented as synchronous)
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendGetRequestAsync(ResourceNameEnum resource, boolean enableCON, int timeout) {
        // For now, just call the synchronous version
        return sendGetRequest(resource, enableCON, timeout);
    }
    
    /**
     * Sends a PUT request to the specified resource
     * 
     * PERFORMANCE TESTING: Logging has been disabled in this method for performance testing.
     * Uncomment logging statements after performance tests are complete.
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPutRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout) {
        if (resource == null) {
            _Logger.warning("Resource is null. Cannot send PUT request.");
            return false;
        }
        
        if (payload == null || payload.isEmpty()) {
            _Logger.warning("Payload is null or empty. Cannot send PUT request.");
            return false;
        }
        
        CoapResponse response = null;
        String targetUri = this.serverAddr + "/" + resource.getResourceName();
        
        try {
            if (enableCON) {
                this.clientConn.useCONs();
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Using CON messaging for PUT request");
            } else {
                this.clientConn.useNONs();
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Using NON messaging for PUT request");
            }
            
            this.clientConn.setURI(targetUri);
            // PERFORMANCE TESTING: Comment out during performance tests
            // _Logger.info("Sending PUT request to: " + targetUri);
            // _Logger.info("Payload: " + payload);
            
            if (timeout > 0) {
                this.clientConn.setTimeout((long) timeout * 1000);
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Timeout set to: " + timeout + " seconds");
            }
            
            response = this.clientConn.put(payload, MediaTypeRegistry.APPLICATION_JSON);
            
            if (response != null) {
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Handling PUT. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
                //     response.getCode() + " - " + response.getResponseText());
                
                if (this.dataMsgListener != null) {
                    // TODO: Parse the response payload and convert to appropriate message type
                }
                
                return true;
            } else {
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.warning("Handling PUT. No response received from: " + targetUri);
            }
        } catch (Exception e) {
            _Logger.severe("Exception during PUT request to " + targetUri + ": " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Sends a PUT request to the specified resource with optional path
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param path Optional path to append to resource (can be null)
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPutRequest(ResourceNameEnum resource, String path, boolean enableCON, String payload, int timeout) {
        // For now, ignore the path parameter and use the basic implementation
        return sendPutRequest(resource, enableCON, payload, timeout);
    }
    
    /**
     * Sends a PUT request to the specified resource without timeout parameter
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPutRequest(ResourceNameEnum resource, boolean enableCON, String payload) {
        return sendPutRequest(resource, enableCON, payload, 0);
    }
    
    /**
     * Sends an asynchronous PUT request (currently implemented as synchronous)
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPutRequestAsync(ResourceNameEnum resource, boolean enableCON, String payload, int timeout) {
        // For now, just call the synchronous version
        return sendPutRequest(resource, enableCON, payload, timeout);
    }
    
    /**
     * Sends a POST request to the specified resource
     * 
     * PERFORMANCE TESTING: Logging has been disabled in this method for performance testing.
     * Uncomment logging statements after performance tests are complete.
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPostRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout) {
        if (resource == null) {
            _Logger.warning("Resource is null. Cannot send POST request.");
            return false;
        }
        
        if (payload == null || payload.isEmpty()) {
            _Logger.warning("Payload is null or empty. Cannot send POST request.");
            return false;
        }
        
        CoapResponse response = null;
        String targetUri = this.serverAddr + "/" + resource.getResourceName();
        
        try {
            if (enableCON) {
                this.clientConn.useCONs();
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Using CON messaging for POST request");
            } else {
                this.clientConn.useNONs();
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Using NON messaging for POST request");
            }
            
            this.clientConn.setURI(targetUri);
            // PERFORMANCE TESTING: Comment out during performance tests
            // _Logger.info("Sending POST request to: " + targetUri);
            // _Logger.info("Payload: " + payload);
            
            if (timeout > 0) {
                this.clientConn.setTimeout((long) timeout * 1000);
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Timeout set to: " + timeout + " seconds");
            }
            
            response = this.clientConn.post(payload, MediaTypeRegistry.APPLICATION_JSON);
            
            if (response != null) {
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.info("Handling POST. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
                //     response.getCode() + " - " + response.getResponseText());
                
                if (this.dataMsgListener != null) {
                    // TODO: Parse the response payload and convert to appropriate message type
                }
                
                return true;
            } else {
                // PERFORMANCE TESTING: Comment out during performance tests
                // _Logger.warning("Handling POST. No response received from: " + targetUri);
            }
        } catch (Exception e) {
            _Logger.severe("Exception during POST request to " + targetUri + ": " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Sends a POST request to the specified resource with optional path
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param path Optional path to append to resource (can be null)
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPostRequest(ResourceNameEnum resource, String path, boolean enableCON, String payload, int timeout) {
        // For now, ignore the path parameter and use the basic implementation
        return sendPostRequest(resource, enableCON, payload, timeout);
    }
    
    /**
     * Sends an asynchronous POST request (currently implemented as synchronous)
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param payload The payload to send (typically JSON string)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendPostRequestAsync(ResourceNameEnum resource, boolean enableCON, String payload, int timeout) {
        // For now, just call the synchronous version
        return sendPostRequest(resource, enableCON, payload, timeout);
    }
    
    /**
     * Sends a DELETE request to the specified resource
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendDeleteRequest(ResourceNameEnum resource, boolean enableCON, int timeout) {
        if (resource == null) {
            _Logger.warning("Resource is null. Cannot send DELETE request.");
            return false;
        }
        
        CoapResponse response = null;
        String targetUri = this.serverAddr + "/" + resource.getResourceName();
        
        try {
            if (enableCON) {
                this.clientConn.useCONs();
                _Logger.info("Using CON messaging for DELETE request");
            } else {
                this.clientConn.useNONs();
                _Logger.info("Using NON messaging for DELETE request");
            }
            
            this.clientConn.setURI(targetUri);
            _Logger.info("Sending DELETE request to: " + targetUri);
            
            if (timeout > 0) {
                this.clientConn.setTimeout((long) timeout * 1000);
                _Logger.info("Timeout set to: " + timeout + " seconds");
            }
            
            response = this.clientConn.delete();
            
            if (response != null) {
                _Logger.info("Handling DELETE. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
                    response.getCode() + " - " + response.getResponseText());
                
                if (this.dataMsgListener != null) {
                    // TODO: Implement callback to data message listener if needed
                }
                
                return true;
            } else {
                _Logger.warning("Handling DELETE. No response received from: " + targetUri);
            }
        } catch (Exception e) {
            _Logger.severe("Exception during DELETE request to " + targetUri + ": " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Sends a DELETE request to the specified resource with optional path
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param path Optional path to append to resource (can be null)
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendDeleteRequest(ResourceNameEnum resource, String path, boolean enableCON, int timeout) {
        // For now, ignore the path parameter and use the basic implementation
        return sendDeleteRequest(resource, enableCON, timeout);
    }
    
    /**
     * Sends an asynchronous DELETE request (currently implemented as synchronous)
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param enableCON If true, uses confirmable (CON) messaging; otherwise uses non-confirmable (NON)
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if request was sent successfully
     */
    public boolean sendDeleteRequestAsync(ResourceNameEnum resource, boolean enableCON, int timeout) {
        // For now, just call the synchronous version
        return sendDeleteRequest(resource, enableCON, timeout);
    }
    
    /**
     * Sends a discovery request to find available resources
     * 
     * @param timeout Timeout in seconds (0 for default)
     * @return boolean True if discovery request completed successfully
     */
    public boolean sendDiscoveryRequest(int timeout) {
        _Logger.info("Discovering resources at: " + this.serverAddr);
        
        try {
            this.clientConn.setURI(this.serverAddr + "/.well-known/core");
            
            if (timeout > 0) {
                this.clientConn.setTimeout((long) timeout * 1000);
            }
            
            CoapResponse response = this.clientConn.get();
            
            if (response != null) {
                _Logger.info("Discovery response: " + response.getResponseText());
                return true;
            } else {
                _Logger.warning("Discovery call completed. Result: null");
            }
        } catch (Exception e) {
            _Logger.warning("Exception during discovery: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Create a URI path from resource and optional name
     * 
     * @param resource The resource enumeration
     * @param name Optional resource name/identifier
     * @return String The complete URI path
     */
    private String createUriPath(ResourceNameEnum resource, String name)
    {
        StringBuilder uriPath = new StringBuilder(this.serverAddr);
        uriPath.append("/").append(resource.getResourceName());
        
        if (name != null && !name.isEmpty()) {
            uriPath.append("/").append(name);
        }
        
        return uriPath.toString();
    }
    
    /**
     * Start observing a resource for updates
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param name Optional sub-resource name (can be null)
     * @param ttl Time-to-live in seconds (0 for default)
     * @return boolean True if observation started successfully
     */
    public boolean startObserver(ResourceNameEnum resource, String name, int ttl)
    {
        String uriPath = createUriPath(resource, name);
        
        _Logger.info("Observing resource [START]: " + uriPath);
        
        try {
            this.clientConn.setURI(uriPath);
            
            // Determine which handler to use based on resource type
            CoapHandler handler = null;
            String resourceName = resource.getResourceName();
            
            if (resourceName.contains("SensorMsg") || resourceName.contains("Sensor")) {
                _Logger.info("Creating SensorData observer handler");
                SensorDataObserverHandler sensorHandler = new SensorDataObserverHandler();
                sensorHandler.setDataMessageListener(this.dataMsgListener);
                handler = sensorHandler;
            } 
            else if (resourceName.contains("SystemPerfMsg") || resourceName.contains("SystemPerf")) {
                _Logger.info("Creating SystemPerformanceData observer handler");
                SystemPerformanceDataObserverHandler sysPerfHandler = 
                    new SystemPerformanceDataObserverHandler();
                sysPerfHandler.setDataMessageListener(this.dataMsgListener);
                handler = sysPerfHandler;
            }
            else {
                _Logger.warning("Unknown resource type for observation: " + resourceName);
                return false;
            }
            
            // Start the observation
            CoapObserveRelation cor = this.clientConn.observe(handler);
            
            // Store the observe relation for later cancellation
            if (cor != null) {
                this.observeRelations.put(uriPath, cor);
                _Logger.info("Observation started successfully for: " + uriPath);
                return !cor.isCanceled();
            } else {
                _Logger.warning("Failed to create observe relation for: " + uriPath);
                return false;
            }
            
        } catch (Exception e) {
            _Logger.severe("Exception during startObserver: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Stop observing a resource
     * 
     * @param resource The ResourceNameEnum representing the target resource
     * @param name Optional sub-resource name (can be null)
     * @return boolean True if observation stopped successfully
     */
    public boolean stopObserver(ResourceNameEnum resource, String name)
    {
        String uriPath = createUriPath(resource, name);
        
        _Logger.info("Observing resource [STOP]: " + uriPath);
        
        CoapObserveRelation cor = this.observeRelations.get(uriPath);
        
        if (cor != null) {
            cor.proactiveCancel();
            this.observeRelations.remove(uriPath);
            _Logger.info("Observation stopped for: " + uriPath);
            return true;
        } else {
            _Logger.warning("No active observation found for: " + uriPath);
            return false;
        }
    }
    
    /**
     * Stop all active observations
     * 
     * @return boolean True if all observations stopped successfully
     */
    public boolean stopAllObservers()
    {
        _Logger.info("Stopping all active observations...");
        
        for (Map.Entry<String, CoapObserveRelation> entry : this.observeRelations.entrySet()) {
            String uri = entry.getKey();
            CoapObserveRelation cor = entry.getValue();
            
            if (cor != null) {
                cor.proactiveCancel();
                _Logger.info("Stopped observation for: " + uri);
            }
        }
        
        this.observeRelations.clear();
        _Logger.info("All observations stopped");
        
        return true;
    }
    
    /**
     * Sets the data message listener for handling responses
     * 
     * @param listener The IDataMessageListener implementation
     */
    public void setDataMessageListener(IDataMessageListener listener) {
        this.dataMsgListener = listener;
    }
    
    /**
     * Starts the client connector
     * 
     * @return boolean True if started successfully
     */
    public boolean startClient() {
        _Logger.info("CoAP client started.");
        return true;
    }
    
    /**
     * Stops the client connector and cleans up resources
     * 
     * @return boolean True if stopped successfully
     */
    public boolean stopClient() {
        _Logger.info("CoAP client stopping...");
        
        // Stop all active observations before shutting down
        stopAllObservers();
        
        if (this.clientConn != null) {
            this.clientConn.shutdown();
        }
        
        _Logger.info("CoAP client stopped.");
        
        return true;
    }
}
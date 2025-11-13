/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.UdpConfig;
import org.eclipse.californium.core.config.CoapConfig;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * CoAP Server Gateway for the Gateway Device Application (GDA).
 * 
 * This class provides CoAP server functionality using the Eclipse Californium library,
 * hosting resources that can be accessed by external clients and the cloud.
 */
public class CoapServerGateway
{
	// Static Logger
	private static final Logger _Logger = 
		Logger.getLogger(CoapServerGateway.class.getName());
	
	// Static initializer for Californium configuration (required for Californium > 3.8.0)
	static {
		CoapConfig.register();
		UdpConfig.register();
	}
	
	// Class-scoped variables
	private CoapServer coapServer = null;
	private IDataMessageListener dataMsgListener = null;
	
	
	// Constructors
	
	/**
	 * Constructor with data message listener.
	 * 
	 * Creates a CoAP server instance and initializes with default configuration.
	 * 
	 * @param dataMsgListener The IDataMessageListener implementation (typically DeviceDataManager)
	 */
	public CoapServerGateway(IDataMessageListener dataMsgListener)
	{
		super();
		
		this.dataMsgListener = dataMsgListener;
		
		// Initialize the server with default resources
		initServer();
		
		_Logger.info("CoAP server gateway created.");
	}
	
	/**
	 * Constructor with data message listener and custom resources.
	 * 
	 * Creates a CoAP server instance and initializes with specified resources.
	 * 
	 * @param dataMsgListener The IDataMessageListener implementation
	 * @param resources Variable length list of resources to register
	 */
	public CoapServerGateway(IDataMessageListener dataMsgListener, ResourceNameEnum ...resources)
	{
		super();
		
		this.dataMsgListener = dataMsgListener;
		
		// Initialize the server with specified resources
		initServer(resources);
		
		_Logger.info("CoAP server gateway created with custom resources.");
	}
	
	
	// Public Methods
	
	/**
	 * Set or update the data message listener.
	 * 
	 * NOTE: This is optional since the listener should be passed in the constructor.
	 * This is provided as a convenience method.
	 * 
	 * @param listener The IDataMessageListener implementation
	 */
	public void setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
			_Logger.info("Data message listener updated.");
		}
	}
	
	/**
	 * Add a resource to the CoAP server.
	 * 
	 * This allows external registration of resource handlers after server initialization.
	 * Resources are organized in a tree structure (e.g., /PIOT/GatewayDevice/SystemPerfMsg).
	 * 
	 * @param resourceType The resource type enumeration
	 * @param endName Optional endpoint name (final segment of path)
	 * @param resource The CoapResource instance to register
	 */
	public void addResource(ResourceNameEnum resourceType, String endName, Resource resource)
	{
		if (this.coapServer != null && resource != null) {
			// TODO: Implement resource registration in PIOT-GDA-08-002
			_Logger.info("Adding resource: " + resourceType.getResourceName());
		} else {
			_Logger.warning("CoAP server not initialized or resource is null.");
		}
	}
	
	/**
	 * Start the CoAP server.
	 * 
	 * Starts the server and adds message tracing interceptors for debugging.
	 * Subsequent calls to start an already running server will have no effect.
	 * 
	 * @return boolean True if server started successfully, false otherwise
	 */
	public boolean startServer()
	{
		try {
			if (this.coapServer != null) {
				_Logger.info("Starting CoAP server...");
				
				this.coapServer.start();
				
				// Add message tracer interceptor for debugging/logging
				for (Endpoint ep : this.coapServer.getEndpoints()) {
					ep.addInterceptor(new MessageTracer());
				}
				
				_Logger.info("\n\n***** CoAP server started. *****\n");
				
				return true;
			} else {
				_Logger.warning("CoAP server START failed. Not yet initialized.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to start CoAP server.", e);
		}
		
		return false;
	}
	
	/**
	 * Stop the CoAP server.
	 * 
	 * Stops the server and releases resources.
	 * Subsequent calls to stop an already stopped server will have no effect.
	 * 
	 * @return boolean True if server stopped successfully, false otherwise
	 */
	public boolean stopServer()
	{
		try {
			if (this.coapServer != null) {
				_Logger.info("Stopping CoAP server...");
				
				this.coapServer.stop();
				
				_Logger.info("CoAP server stopped successfully.");
				
				return true;
			} else {
				_Logger.warning("CoAP server STOP failed. Not yet initialized.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to stop CoAP server.", e);
		}
		
		return false;
	}
	
	
	// Private Methods
	
	/**
	 * Initialize the CoAP server with default or specified resources.
	 * 
	 * Creates the CoapServer instance. Resource handlers will be added
	 * in PIOT-GDA-08-002.
	 * 
	 * @param resources Optional variable length list of resources to register
	 */
	private void initServer(ResourceNameEnum ...resources)
	{
	    try {
	        _Logger.info("Initializing CoAP server...");
	        
	        // Create the CoAP server instance
	        this.coapServer = new CoapServer();
	        
	        _Logger.info("Creating CoAP server resources...");
	        
	        // Create resource tree structure: PIOT/GatewayDevice/
	        CoapResource piotResource = new CoapResource("PIOT");
	        CoapResource gdResource = new CoapResource("GatewayDevice");
	        
	        // Create resource handlers - use only the final segment name
	        GenericCoapResourceHandler mgmtStatusResource = 
	            new GenericCoapResourceHandler("MgmtStatusMsg", this.dataMsgListener);  // Changed!
	        
	        GenericCoapResourceHandler mgmtStatusCmdResource = 
	            new GenericCoapResourceHandler("MgmtStatusCmd", this.dataMsgListener);  // Changed!
	        
	        // Build resource tree
	        piotResource.add(gdResource);
	        gdResource.add(mgmtStatusResource);
	        gdResource.add(mgmtStatusCmdResource);
	        
	        // Add root to server
	        this.coapServer.add(piotResource);
	        
	        _Logger.info("CoAP server resources created successfully:");
	        _Logger.info("  - /PIOT/GatewayDevice/MgmtStatusMsg");
	        _Logger.info("  - /PIOT/GatewayDevice/MgmtStatusCmd");
	        
	    } catch (Exception e) {
	        _Logger.log(Level.SEVERE, "Failed to initialize CoAP server.", e);
	        e.printStackTrace();
	    }
	}
}
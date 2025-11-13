package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IDataMessageListener;

/**
 * Generic CoAP Resource Handler
 * 
 * Handles GET, PUT, POST, and DELETE requests for CoAP resources.
 */
public class GenericCoapResourceHandler extends CoapResource
{
    private static final Logger _Logger = 
        Logger.getLogger(GenericCoapResourceHandler.class.getName());
    
    private IDataMessageListener dataMsgListener;
    
    /**
     * Constructor
     * 
     * @param resourceName The name of this resource (final segment of URI path)
     * @param dataMsgListener Optional data message listener for handling incoming data
     */
    public GenericCoapResourceHandler(String resourceName, IDataMessageListener dataMsgListener)
    {
        super(resourceName);
        this.dataMsgListener = dataMsgListener;
        
        // Make this resource observable for future OBSERVE support
        setObservable(true);
    }
    
    /**
     * Handle GET requests
     * Returns current resource state as JSON
     */
    @Override
    public void handleGET(CoapExchange exchange)
    {
        _Logger.info("Handling GET request for: " + this.getName());
        
        // Return a simple JSON response
        String response = "{\"status\":\"active\",\"resource\":\"" + this.getName() + 
                         "\",\"timestamp\":" + System.currentTimeMillis() + "}";
        
        exchange.respond(ResponseCode.CONTENT, response);
    }
    
    /**
     * Handle PUT requests
     * Updates existing resource with provided data
     */
    @Override
    public void handlePUT(CoapExchange exchange)
    {
        _Logger.info("Handling PUT request for: " + this.getName());
        
        String payload = exchange.getRequestText();
        _Logger.info("PUT Payload: " + payload);
        
        // TODO: Parse payload and update resource state
        // TODO: Call dataMsgListener if needed
        
        // Respond with success
        exchange.respond(ResponseCode.CHANGED, "Resource updated successfully");
    }
    
    /**
     * Handle POST requests
     * Creates new resource or triggers action with provided data
     */
    @Override
    public void handlePOST(CoapExchange exchange)
    {
        _Logger.info("Handling POST request for: " + this.getName());
        
        String payload = exchange.getRequestText();
        _Logger.info("POST Payload: " + payload);
        
        // TODO: Parse payload and create/process resource
        // TODO: Call dataMsgListener if needed
        
        // Respond with success
        exchange.respond(ResponseCode.CREATED, "Resource created successfully");
    }
    
    /**
     * Handle DELETE requests
     * Deletes or resets the resource
     */
    @Override
    public void handleDELETE(CoapExchange exchange)
    {
        _Logger.info("Handling DELETE request for: " + this.getName());
        
        // TODO: Delete or reset resource state
        // TODO: Call dataMsgListener if needed
        
        // Respond with success
        exchange.respond(ResponseCode.DELETED, "Resource deleted successfully");
    }
}
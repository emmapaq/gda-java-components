package programmingtheiot.gda.connection;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
//import programmingtheiot.common.IConnectionListener;
import programmingtheiot.gda.connection.IConnectionListener;

/**
 * Interface for Publish/Subscribe client implementations (MQTT, CoAP, etc.).
 */
public interface IPubSubClient
{
    /**
     * Connects the client to the broker/server.
     * 
     * @return boolean True if connection successful, false otherwise
     */
    public boolean connectClient();
    
    /**
     * Disconnects the client from the broker/server.
     * 
     * @return boolean True if disconnection successful, false otherwise
     */
    public boolean disconnectClient();
    
    /**
     * Publishes a message to the specified resource.
     * 
     * @param resource The resource to publish to
     * @param msg The message to publish
     * @param qos The quality of service level
     * @return boolean True if publish successful, false otherwise
     */
    public boolean publishMessage(ResourceNameEnum resource, String msg, int qos);
    
    /**
     * Subscribes to messages from the specified resource.
     * 
     * @param resource The resource to subscribe to
     * @param qos The quality of service level
     * @return boolean True if subscription successful, false otherwise
     */
    public boolean subscribeToTopic(ResourceNameEnum resource, int qos);
    
    /**
     * Unsubscribes from messages from the specified resource.
     * 
     * @param resource The resource to unsubscribe from
     * @return boolean True if unsubscription successful, false otherwise
     */
    public boolean unsubscribeFromTopic(ResourceNameEnum resource);
    
    /**
     * Sets the data message listener for incoming messages.
     * 
     * @param listener The data message listener
     * @return boolean True if listener set successfully, false otherwise
     */
    public boolean setDataMessageListener(IDataMessageListener listener);
    
    /**
     * Sets the connection listener for connection events.
     * NOTE: This method is optional and may not be implemented by all clients.
     * 
     * @param listener The connection listener
     * @return boolean True if listener set successfully, false otherwise
     */
    public default boolean setConnectionListener(IConnectionListener listener)
    {
        // Default implementation does nothing
        return false;
    }
}
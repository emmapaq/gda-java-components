/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

/**
 * Interface for connection event listeners.
 * 
 * This interface defines callback methods for handling connection-related
 * events such as successful connections, disconnections, and connection failures.
 * 
 */
public interface IConnectionListener
{
	/**
	 * Called when a connection to a remote service is successfully established.
	 * 
	 * @param isConnected True if connected, false if disconnected
	 */
	public void onConnect(boolean isConnected);
	
	/**
	 * Called when a disconnection from a remote service occurs.
	 * 
	 * @param cause The cause of the disconnection (may be null for normal disconnect)
	 */
	public void onDisconnect(Throwable cause);
	
	/**
	 * Called when a connection attempt fails.
	 * 
	 * @param cause The cause of the connection failure
	 */
	public void onConnectionFailure(Throwable cause);
	
}
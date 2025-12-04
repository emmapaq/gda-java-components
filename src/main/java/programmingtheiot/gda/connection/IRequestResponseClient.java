package programmingtheiot.gda.connection;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Interface for request/response style clients used by the GDA module.
 */
public interface IRequestResponseClient
{
    // discovery
    boolean sendDiscoveryRequest(int timeout);

    // standard request methods with explicit path
    boolean sendDeleteRequest(ResourceNameEnum resource, String path, boolean enableCON, int timeout);
    boolean sendGetRequest(ResourceNameEnum resource, String path, boolean enableCON, int timeout);
    boolean sendPostRequest(ResourceNameEnum resource, String path, boolean enableCON, String payload, int timeout);
    boolean sendPutRequest(ResourceNameEnum resource, String path, boolean enableCON, String payload, int timeout);

    // convenience overloads (implementations may offer these)
    boolean sendDeleteRequest(ResourceNameEnum resource, boolean enableCON, int timeout);
    boolean sendGetRequest(ResourceNameEnum resource, boolean enableCON, int timeout);
    boolean sendPostRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout);
    boolean sendPutRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout);

    // observer controls
    boolean startObserver(ResourceNameEnum resource, String path, int ttl);
    boolean stopObserver(ResourceNameEnum resource, String path, int timeout);

    // message listener
    boolean setDataMessageListener(IDataMessageListener listener);

    // endpoint helpers
    void setEndpointPath(ResourceNameEnum resource);
    void clearEndpointPath();
}

package programmingtheiot.gda.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
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
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * CoAP Client Connector for Lab Module 09
 * Implements GET, PUT, POST, DELETE, and OBSERVE operations
 */
public class CoapClientConnector implements IRequestResponseClient
{
    private static final Logger _Logger = 
        Logger.getLogger(CoapClientConnector.class.getName());
    
    static {
        try {
            org.eclipse.californium.core.config.CoapConfig.register();
            org.eclipse.californium.elements.config.UdpConfig.register();
        } catch (Exception e) {
            _Logger.warning("Failed to register Californium config: " + e.getMessage());
        }
    }
    
    private String protocol;
    private String host;
    private int port;
    private String serverAddr;
    private CoapClient clientConn;
    private IDataMessageListener dataMsgListener;
    private Map<String, CoapObserveRelation> observeRelations;
    
    public CoapClientConnector()
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        this.host = configUtil.getProperty(
            ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
        
        if (configUtil.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
            this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
            this.port = configUtil.getInteger(
                ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, 
                ConfigConst.DEFAULT_COAP_SECURE_PORT);
        } else {
            this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
            this.port = configUtil.getInteger(
                ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, 
                ConfigConst.DEFAULT_COAP_PORT);
        }
        
        this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;
        this.clientConn = new CoapClient();
        this.observeRelations = new HashMap<>();
        
        _Logger.info("CoAP Client server address: " + this.serverAddr);
    }
    
    // IRequestResponseClient implementation
    
    @Override
    public boolean sendDiscoveryRequest(int timeout)
    {
        try {
            this.clientConn.setURI(this.serverAddr + "/.well-known/core");
            if (timeout > 0) this.clientConn.setTimeout((long) timeout * 1000);
            
            CoapResponse response = this.clientConn.get();
            if (response != null) {
                _Logger.info("Discovery: " + response.getResponseText());
                return true;
            }
        } catch (Exception e) {
            _Logger.warning("Discovery failed: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean sendDeleteRequest(ResourceNameEnum resource, String path, boolean enableCON, int timeout)
    {
        String uri = buildUri(resource, path);
        try {
            if (enableCON) this.clientConn.useCONs(); else this.clientConn.useNONs();
            this.clientConn.setURI(uri);
            if (timeout > 0) this.clientConn.setTimeout((long) timeout * 1000);
            
            CoapResponse response = this.clientConn.delete();
            return (response != null && response.isSuccess());
        } catch (Exception e) {
            _Logger.warning("DELETE failed: " + e.getMessage());
        }
        return false;
    }
    
    public boolean sendDeleteRequest(ResourceNameEnum resource, boolean enableCON, int timeout)
    {
        return sendDeleteRequest(resource, null, enableCON, timeout);
    }
    
    @Override
    public boolean sendGetRequest(ResourceNameEnum resource, String path, boolean enableCON, int timeout)
    {
        String uri = buildUri(resource, path);
        try {
            if (enableCON) this.clientConn.useCONs(); else this.clientConn.useNONs();
            this.clientConn.setURI(uri);
            if (timeout > 0) this.clientConn.setTimeout((long) timeout * 1000);
            
            CoapResponse response = this.clientConn.get();
            return (response != null && response.isSuccess());
        } catch (Exception e) {
            _Logger.warning("GET failed: " + e.getMessage());
        }
        return false;
    }
    
    public boolean sendGetRequest(ResourceNameEnum resource, boolean enableCON, int timeout)
    {
        return sendGetRequest(resource, null, enableCON, timeout);
    }
    
    @Override
    public boolean sendPostRequest(ResourceNameEnum resource, String path, boolean enableCON, String payload, int timeout)
    {
        if (payload == null) return false;
        
        String uri = buildUri(resource, path);
        try {
            if (enableCON) this.clientConn.useCONs(); else this.clientConn.useNONs();
            this.clientConn.setURI(uri);
            if (timeout > 0) this.clientConn.setTimeout((long) timeout * 1000);
            
            CoapResponse response = this.clientConn.post(payload, MediaTypeRegistry.APPLICATION_JSON);
            return (response != null && response.isSuccess());
        } catch (Exception e) {
            _Logger.warning("POST failed: " + e.getMessage());
        }
        return false;
    }
    
    public boolean sendPostRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout)
    {
        return sendPostRequest(resource, null, enableCON, payload, timeout);
    }
    
    @Override
    public boolean sendPutRequest(ResourceNameEnum resource, String path, boolean enableCON, String payload, int timeout)
    {
        if (payload == null) return false;
        
        String uri = buildUri(resource, path);
        try {
            if (enableCON) this.clientConn.useCONs(); else this.clientConn.useNONs();
            this.clientConn.setURI(uri);
            if (timeout > 0) this.clientConn.setTimeout((long) timeout * 1000);
            
            CoapResponse response = this.clientConn.put(payload, MediaTypeRegistry.APPLICATION_JSON);
            return (response != null && response.isSuccess());
        } catch (Exception e) {
            _Logger.warning("PUT failed: " + e.getMessage());
        }
        return false;
    }
    
    public boolean sendPutRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout)
    {
        return sendPutRequest(resource, null, enableCON, payload, timeout);
    }
    
    public boolean sendPutRequestAsync(ResourceNameEnum resource, boolean enableCON, String payload, int timeout)
    {
        return sendPutRequest(resource, enableCON, payload, timeout);
    }
    
    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        this.dataMsgListener = listener;
        return true;
    }
    
    @Override
    public boolean startObserver(ResourceNameEnum resource, String path, int ttl)
    {
        String uri = buildUri(resource, path);
        try {
            this.clientConn.setURI(uri);
            
            CoapHandler handler = new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {
                    if (dataMsgListener != null) {
                        dataMsgListener.handleIncomingMessage(resource, response.getResponseText());
                    }
                }
                
                @Override
                public void onError() {
                    _Logger.warning("Observer error for: " + uri);
                }
            };
            
            CoapObserveRelation cor = this.clientConn.observe(handler);
            if (cor != null) {
                this.observeRelations.put(uri, cor);
                return !cor.isCanceled();
            }
        } catch (Exception e) {
            _Logger.warning("Observer failed: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean stopObserver(ResourceNameEnum resource, String path, int timeout)
    {
        String uri = buildUri(resource, path);
        CoapObserveRelation cor = this.observeRelations.get(uri);
        
        if (cor != null) {
            cor.proactiveCancel();
            this.observeRelations.remove(uri);
            return true;
        }
        return false;
    }
    
    @Override
    public void setEndpointPath(ResourceNameEnum resource)
    {
        if (resource != null) {
            String uri = this.serverAddr + "/" + resource.getResourceName();
            this.clientConn.setURI(uri);
            _Logger.info("Endpoint path set to: " + uri);
        }
    }
    
    @Override
    public void clearEndpointPath()
    {
        this.clientConn.setURI(this.serverAddr);
        _Logger.info("Endpoint path cleared");
    }
    
    // Additional public methods for test compatibility
    
    public boolean startClient()
    {
        _Logger.info("CoAP client started");
        return true;
    }
    
    public boolean stopClient()
    {
        for (CoapObserveRelation cor : this.observeRelations.values()) {
            if (cor != null) cor.proactiveCancel();
        }
        this.observeRelations.clear();
        if (this.clientConn != null) this.clientConn.shutdown();
        _Logger.info("CoAP client stopped");
        return true;
    }
    
    // Private helper methods
    
    private String buildUri(ResourceNameEnum resource, String path)
    {
        StringBuilder uri = new StringBuilder(this.serverAddr);
        
        if (resource != null) {
            uri.append("/").append(resource.getResourceName());
        }
        
        if (path != null && !path.isEmpty()) {
            if (!path.startsWith("/")) uri.append("/");
            uri.append(path);
        }
        
        return uri.toString();
    }
}
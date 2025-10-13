package programmingtheiot.gda.connection;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;

public class CoapServerGateway {
    private static final Logger _Logger = Logger.getLogger(CoapServerGateway.class.getName());
    
    private CoapServer coapServer;
    private IDataMessageListener dataMsgListener;
    private Map<String, Resource> resourceMap = new HashMap<>();
    
    public CoapServerGateway(IDataMessageListener dataMsgListener) {
        this.dataMsgListener = dataMsgListener;
        initServer(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
    }
    
    public void addResource(ResourceNameEnum resource) {
        if (resource != null && !resourceMap.containsKey(resource.getResourceName())) {
            Resource res = createResourceChain(resource);
            if (res != null) {
                coapServer.add(res);
                resourceMap.put(resource.getResourceName(), res);
                _Logger.info("Added CoAP resource: " + resource.getResourceName());
            }
        }
    }
    
    public boolean hasResource(String name) {
        return resourceMap.containsKey(name);
    }
    
    public void setDataMessageListener(IDataMessageListener listener) {
        this.dataMsgListener = listener;
    }
    
    public boolean startServer() {
        if (coapServer != null) {
            coapServer.start();
            _Logger.info("CoAP server started.");
            return true;
        }
        return false;
    }
    
    public boolean stopServer() {
        if (coapServer != null) {
            coapServer.stop();
            _Logger.info("CoAP server stopped.");
            return true;
        }
        return false;
    }
    
    private Resource createResourceChain(ResourceNameEnum resource) {
        // FIXED: Create handler with ResourceNameEnum, then set listener separately
        GenericCoapResourceHandler handler = new GenericCoapResourceHandler(resource);
        handler.setDataMessageListener(this.dataMsgListener);
        return handler;
    }
    
    private void initServer(ResourceNameEnum... resources) {
        coapServer = new CoapServer();
        for (ResourceNameEnum res : resources) {
            addResource(res);
        }
        _Logger.info("CoAP server initialized with resources.");
    }
}
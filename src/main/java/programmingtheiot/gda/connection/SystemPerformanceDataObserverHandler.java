package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SystemPerformanceData;

/**
 * CoAP Observer Handler for SystemPerformanceData updates.
 * 
 * This handler processes incoming OBSERVE notifications containing SystemPerformanceData payloads.
 */
public class SystemPerformanceDataObserverHandler implements CoapHandler
{
    private static final Logger _Logger =
        Logger.getLogger(SystemPerformanceDataObserverHandler.class.getName());
    
    private IDataMessageListener dataMsgListener;
    
    /**
     * Constructor
     */
    public SystemPerformanceDataObserverHandler()
    {
        super();
    }
    
    /**
     * Set the data message listener for handling incoming system performance data
     * 
     * @param listener The IDataMessageListener instance
     */
    public void setDataMessageListener(IDataMessageListener listener)
    {
        this.dataMsgListener = listener;
    }
    
    /**
     * Handle CoAP errors
     */
    @Override
    public void onError()
    {
        _Logger.warning("Handling CoAP error for SystemPerformanceData observation...");
    }
    
    /**
     * Handle incoming OBSERVE notifications with SystemPerformanceData payload
     * 
     * @param response The CoAP response containing SystemPerformanceData in JSON format
     */
    @Override
    public void onLoad(CoapResponse response)
    {
        if (response != null) {
            _Logger.info("Received CoAP OBSERVE notification - SystemPerformanceData:");
            _Logger.info("  Response Code: " + response.getCode());
            
            String payload = response.getResponseText();
            _Logger.info("  Payload (JSON): " + payload);
            
            // Parse the JSON payload to SystemPerformanceData
            try {
                SystemPerformanceData sysPerfData = 
                    DataUtil.getInstance().jsonToSystemPerformanceData(payload);
                
                if (sysPerfData != null) {
                    _Logger.info("  Parsed SystemPerformanceData: CPU=" + 
                               sysPerfData.getCpuUtilization() + "%, Memory=" +
                               sysPerfData.getMemoryUtilization() + "%");
                    
                    // Forward to data message listener if available
                    if (this.dataMsgListener != null) {
                        // CORRECTED LINE: Use ResourceNameEnum instead of String
                        this.dataMsgListener.handleSystemPerformanceMessage(
                            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
                    }
                } else {
                    _Logger.warning("  Failed to parse SystemPerformanceData from payload");
                }
            } catch (Exception e) {
                _Logger.warning("  Exception parsing SystemPerformanceData: " + e.getMessage());
            }
        } else {
            _Logger.warning("Received null CoAP response for SystemPerformanceData observation");
        }
    }
}
package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;

/**
 * CoAP Observer Handler for SensorData updates.
 * 
 * This handler processes incoming OBSERVE notifications containing SensorData payloads.
 */
public class SensorDataObserverHandler implements CoapHandler
{
    private static final Logger _Logger =
        Logger.getLogger(SensorDataObserverHandler.class.getName());
    
    private IDataMessageListener dataMsgListener;
    
    /**
     * Constructor
     */
    public SensorDataObserverHandler()
    {
        super();
    }
    
    /**
     * Set the data message listener for handling incoming sensor data
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
        _Logger.warning("Handling CoAP error for SensorData observation...");
    }
    
    /**
     * Handle incoming OBSERVE notifications with SensorData payload
     * 
     * @param response The CoAP response containing SensorData in JSON format
     */
    @Override
    public void onLoad(CoapResponse response)
    {
        if (response != null) {
            _Logger.info("Received CoAP OBSERVE notification - SensorData:");
            _Logger.info("  Response Code: " + response.getCode());
            
            String payload = response.getResponseText();
            _Logger.info("  Payload (JSON): " + payload);
            
            // Parse the JSON payload to SensorData
            try {
                SensorData sensorData = DataUtil.getInstance().jsonToSensorData(payload);
                
                if (sensorData != null) {
                    _Logger.info("  Parsed SensorData: " + sensorData.getName() + 
                               " - Value: " + sensorData.getValue());
                    
                    // Forward to data message listener if available
                    if (this.dataMsgListener != null) {
                        // Use the appropriate ResourceNameEnum based on sensor type
                        this.dataMsgListener.handleSensorMessage(
                            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
                    }
                } else {
                    _Logger.warning("  Failed to parse SensorData from payload");
                }
            } catch (Exception e) {
                _Logger.warning("  Exception parsing SensorData: " + e.getMessage());
            }
        } else {
            _Logger.warning("Received null CoAP response for SensorData observation");
        }
    }
}
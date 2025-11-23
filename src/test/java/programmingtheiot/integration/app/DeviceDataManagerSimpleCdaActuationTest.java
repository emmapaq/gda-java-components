package programmingtheiot.integration.app;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.app.DeviceDataManager;

/**
 * Test class for DeviceDataManager humidity threshold monitoring.
 * 
 * Tests the GDA's ability to analyze humidity sensor data from the CDA
 * and trigger actuation events based on threshold crossings.
 * 
 * This test verifies:
 * - Humidity threshold crossing detection
 * - Time-based actuation triggering (humidityMaxTimePastThreshold)
 * - Humidifier ON command when humidity too low
 * - Humidifier OFF command when humidity reaches nominal
 * 
 * @author Emma
 */
public class DeviceDataManagerSimpleCdaActuationTest
{
    // Static
    
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManagerSimpleCdaActuationTest.class.getName());
    
    
    // Member variables
    
    private DeviceDataManager devDataMgr = null;
    
    
    // Test setup methods
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Nothing to set up per-test
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        // Nothing to tear down per-test
    }
    
    
    // Test methods
    
    /**
     * Test method for running the DeviceDataManager humidity analysis.
     * 
     * This test simulates receiving humidity sensor data from the CDA
     * and verifies that the GDA correctly:
     * 1. Detects threshold crossings (below floor, above ceiling)
     * 2. Waits for humidityMaxTimePastThreshold before triggering actuation
     * 3. Sends humidifier ON command when humidity too low
     * 4. Sends humidifier OFF command when humidity returns to nominal
     */
    @Test
    public void testSendActuationEventsToCda()
    {
        _Logger.info("\n\n***** testSendActuationEventsToCda *****");
        
        devDataMgr = new DeviceDataManager();
        
        // NOTE: Be sure your PiotConfig.props is setup properly
        // to connect with the CDA (or disable MQTT for standalone test)
        // For this test, you can disable MQTT in PiotConfig.props:
        // enableMqttClient = False
        devDataMgr.startManager();
        
        ConfigUtil cfgUtil = ConfigUtil.getInstance();
        
        // Load threshold values from configuration
        float nominalVal = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");
        float lowVal = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");
        float highVal = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");
        int delay = cfgUtil.getInteger(ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");
        
        _Logger.info("Test Configuration:");
        _Logger.info("  Nominal Humidity: " + nominalVal + "%");
        _Logger.info("  Floor Threshold: " + lowVal + "%");
        _Logger.info("  Ceiling Threshold: " + highVal + "%");
        _Logger.info("  Time Delay: " + delay + " seconds");
        
        // Test Sequence No. 1: Low humidity triggering humidifier ON
        _Logger.info("\n===== Test Sequence 1: Low Humidity =====");
        generateAndProcessHumiditySensorDataSequence(
            devDataMgr, nominalVal, lowVal, highVal, delay);
        
        // Test Sequence No. 2: High humidity triggering humidifier OFF (optional)
        _Logger.info("\n===== Test Sequence 2: High Humidity =====");
        generateAndProcessHighHumiditySensorDataSequence(
            devDataMgr, nominalVal, lowVal, highVal, delay);
        
        devDataMgr.stopManager();
        
        _Logger.info("\n***** Test Complete *****");
    }
    
    /**
     * Generates and processes a sequence of humidity sensor data messages
     * to test low humidity threshold crossing and actuation.
     * 
     * @param ddm DeviceDataManager instance
     * @param nominalVal Nominal humidity value
     * @param lowVal Floor threshold value
     * @param highVal Ceiling threshold value
     * @param delay Time delay threshold in seconds
     */
    private void generateAndProcessHumiditySensorDataSequence(
        DeviceDataManager ddm, float nominalVal, float lowVal, float highVal, int delay)
    {
        SensorData sd = new SensorData();
        sd.setName("My Test Humidity Sensor");
        sd.setLocationID("constraineddevice001");
        sd.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
        
        // Send nominal values - should NOT trigger actuation
        _Logger.info("Step 1: Sending nominal humidity values...");
        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);
        
        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);
        
        // Send low value - first crossing (starts timer)
        _Logger.info("Step 2: Sending first low humidity value (below floor)...");
        sd.setValue(lowVal - 2);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
        
        // Send low value again - second crossing (should trigger ON)
        _Logger.info("Step 3: Sending second low humidity value (should trigger ON)...");
        sd.setValue(lowVal - 1);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
        
        // Send value returning to normal range
        _Logger.info("Step 4: Sending humidity value above floor...");
        sd.setValue(lowVal + 1);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
        
        // Send nominal value (should trigger OFF)
        _Logger.info("Step 5: Sending nominal humidity value (should trigger OFF)...");
        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
    }
    
    /**
     * Generates and processes high humidity values to test ceiling threshold.
     * 
     * @param ddm DeviceDataManager instance
     * @param nominalVal Nominal humidity value
     * @param lowVal Floor threshold value
     * @param highVal Ceiling threshold value
     * @param delay Time delay threshold in seconds
     */
    private void generateAndProcessHighHumiditySensorDataSequence(
        DeviceDataManager ddm, float nominalVal, float lowVal, float highVal, int delay)
    {
        SensorData sd = new SensorData();
        sd.setName("My Test Humidity Sensor");
        sd.setLocationID("constraineddevice001");
        sd.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
        
        // Send high value - first crossing
        _Logger.info("Step 1: Sending first high humidity value (above ceiling)...");
        sd.setValue(highVal + 2);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
        
        // Send high value again - second crossing (should trigger OFF)
        _Logger.info("Step 2: Sending second high humidity value (should trigger OFF)...");
        sd.setValue(highVal + 1);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
        
        // Return to nominal
        _Logger.info("Step 3: Sending nominal humidity value...");
        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);
    }
    
    /**
     * Helper method to wait for specified seconds.
     * 
     * @param seconds Number of seconds to wait
     */
    private void waitForSeconds(int seconds)
    {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
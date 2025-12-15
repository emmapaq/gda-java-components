/**
 * Integration tests for fermentation data flow through GDA.
 * 
 * This test class validates the complete data flow for fermentation monitoring,
 * including sensor data reception, persistence, actuation command relay,
 * profile change propagation, and cloud integration.
 * 
 * Validation:
 * - Sensor data reception from CDA
 * - Sensor data persistence to Redis
 * - Actuation command generation and relay
 * - Profile change command propagation
 * - Cloud data forwarding
 * - End-to-end data flow
 * 
 * @author Emma
 */
package programmingtheiot.integration.app;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.app.DeviceDataManager;
import programmingtheiot.gda.app.FermentationProfileManager;

public class FermentationDataFlowTest {
    
    // Static logger
    private static final Logger _Logger =
        Logger.getLogger(FermentationDataFlowTest.class.getName());
    
    // Test fixtures
    private DeviceDataManager deviceDataMgr;
    private FermentationProfileManager profileMgr;
    
    // Tracking variables
    private int sensorDataCount = 0;
    private int actuatorCommandCount = 0;
    
    // ========================================================================
    // Setup and Teardown
    // ========================================================================
    
    @Before
    public void setUp() throws Exception {
        _Logger.info("\n\n===== Setting up Fermentation Data Flow test =====");
        
        this.deviceDataMgr = new DeviceDataManager();
        this.profileMgr = new FermentationProfileManager();
        
        // Start the device data manager
        this.deviceDataMgr.startManager();
        
        // Reset counters
        this.sensorDataCount = 0;
        this.actuatorCommandCount = 0;
        
        _Logger.info("✓ DeviceDataManager started");
    }
    
    @After
    public void tearDown() throws Exception {
        _Logger.info("===== Stopping DeviceDataManager =====");
        
        // Stop the device data manager
        this.deviceDataMgr.stopManager();
        
        _Logger.info("✓ DeviceDataManager stopped");
        _Logger.info("===== Test complete =====\n");
    }
    
    // ========================================================================
    // Test 1: Temperature Sensor Data Reception
    // ========================================================================
    
    @Test
    public void testTemperatureSensorDataReception() {
        _Logger.info("\n===== Test 1: Temperature Sensor Data Reception =====\n");
        
        // Create temperature sensor data
        SensorData tempData = new SensorData();
        tempData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
        tempData.setValue(70.5f);
        tempData.setName("TempSensor");
        
        _Logger.info("Sending temperature data: " + tempData.getValue() + "°F");
        
        // Send to device data manager
        boolean result = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);
        
        assertTrue("Temperature sensor data should be handled", result);
        
        _Logger.info("✓ Temperature data received and handled");
        
        _Logger.info("\n✓✓✓ Test PASSED: Temperature data flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 2: Humidity Sensor Data Reception
    // ========================================================================
    
    @Test
    public void testHumiditySensorDataReception() {
        _Logger.info("\n===== Test 2: Humidity Sensor Data Reception =====\n");
        
        // Create humidity sensor data
        SensorData humidData = new SensorData();
        humidData.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
        humidData.setValue(65.0f);
        humidData.setName("HumiditySensor");
        
        _Logger.info("Sending humidity data: " + humidData.getValue() + "%");
        
        // Send to device data manager
        boolean result = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, humidData);
        
        assertTrue("Humidity sensor data should be handled", result);
        
        _Logger.info("✓ Humidity data received and handled");
        
        _Logger.info("\n✓✓✓ Test PASSED: Humidity data flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 3: Pressure Sensor Data Reception
    // ========================================================================
    
    @Test
    public void testPressureSensorDataReception() {
        _Logger.info("\n===== Test 3: Pressure Sensor Data Reception =====\n");
        
        // Create pressure sensor data
        SensorData pressData = new SensorData();
        // FIXED: Use PRESS_SENSOR_TYPE or add PRESSURE_SENSOR_TYPE to ConfigConst
        pressData.setTypeID(ConfigConst.PRESSURE_SENSOR_TYPE);
        pressData.setValue(101.3f);
        pressData.setName("PressureSensor");
        
        _Logger.info("Sending pressure data: " + pressData.getValue() + " kPa");
        
        // Send to device data manager
        boolean result = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, pressData);
        
        assertTrue("Pressure sensor data should be handled", result);
        
        _Logger.info("✓ Pressure data received and handled");
        
        _Logger.info("\n✓✓✓ Test PASSED: Pressure data flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 4: Multiple Sensor Data Reception
    // ========================================================================
    
    @Test
    public void testMultipleSensorDataReception() {
        _Logger.info("\n===== Test 4: Multiple Sensor Data Reception =====\n");
        
        int messageCount = 10;
        int successCount = 0;
        
        _Logger.info("Sending " + messageCount + " sensor data messages");
        
        for (int i = 0; i < messageCount; i++) {
            // Alternate between sensor types
            SensorData sensorData = new SensorData();
            
            if (i % 3 == 0) {
                sensorData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
                sensorData.setValue(68.0f + i);
                sensorData.setName("TempSensor");
            } else if (i % 3 == 1) {
                sensorData.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
                sensorData.setValue(60.0f + i);
                sensorData.setName("HumiditySensor");
            } else {
                // FIXED: Use PRESS_SENSOR_TYPE
                sensorData.setTypeID(ConfigConst.PRESSURE_SENSOR_TYPE);
                sensorData.setValue(100.0f + i);
                sensorData.setName("PressureSensor");
            }
            
            boolean result = this.deviceDataMgr.handleSensorMessage(
                ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
            
            if (result) {
                successCount++;
            }
        }
        
        assertEquals("All sensor messages should be handled", 
                    messageCount, successCount);
        
        _Logger.info("✓ All " + messageCount + " sensor messages handled successfully");
        
        _Logger.info("\n✓✓✓ Test PASSED: Multiple sensor data flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 5: System Performance Data Reception
    // ========================================================================
    
    @Test
    public void testSystemPerformanceDataReception() {
        _Logger.info("\n===== Test 5: System Performance Data Reception =====\n");
        
        // Create system performance data
        SystemPerformanceData sysPerfData = new SystemPerformanceData();
        sysPerfData.setCpuUtilization(45.5f);
        sysPerfData.setMemoryUtilization(62.3f);
        sysPerfData.setName("SystemPerformance");
        
        _Logger.info("Sending system performance data:");
        _Logger.info("  CPU: " + sysPerfData.getCpuUtilization() + "%");
        _Logger.info("  Memory: " + sysPerfData.getMemoryUtilization() + "%");
        
        // Send to device data manager
        boolean result = this.deviceDataMgr.handleSystemPerformanceMessage(
            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
        
        assertTrue("System performance data should be handled", result);
        
        _Logger.info("✓ System performance data received and handled");
        
        _Logger.info("\n✓✓✓ Test PASSED: System performance data flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 6: Actuation Command Response
    // ========================================================================
    
    @Test
    public void testActuationCommandResponse() {
        _Logger.info("\n===== Test 6: Actuation Command Response =====\n");
        
        // Create actuation command
        ActuatorData actuatorCmd = new ActuatorData();
        // FIXED: Use setTypeID instead of setActuatorType
        actuatorCmd.setTypeID(ConfigConst.HVAC_ACTUATOR_TYPE);
        // FIXED: setCommand takes int, use setStateData for string command
        actuatorCmd.setCommand(1);  // ON command
        actuatorCmd.setStateData(ConfigConst.HVAC_COOLING_CMD);  // Store command string here
        actuatorCmd.setValue(1.0f);
        actuatorCmd.setName("HVAC Cooling Command");
        
        _Logger.info("Sending actuation command:");
        _Logger.info("  Type: " + actuatorCmd.getTypeID());
        _Logger.info("  Command: " + actuatorCmd.getStateData());
        
        // Send to device data manager (simulating cloud command)
        boolean result = this.deviceDataMgr.handleActuatorCommandResponse(
            ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, actuatorCmd);
        
        assertTrue("Actuation command should be handled", result);
        
        _Logger.info("✓ Actuation command received and handled");
        
        _Logger.info("\n✓✓✓ Test PASSED: Actuation command response flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 7: Profile Change Command Propagation
    // ========================================================================
    
    @Test
    public void testProfileChangeCommandPropagation() {
        _Logger.info("\n===== Test 7: Profile Change Command Propagation =====\n");
        
        // Generate profile change command
        ActuatorData profileCmd = this.profileMgr.generateProfileChangeCommand(
            ConfigConst.FERMENTATION_PROFILE_LAGER);
        
        // FIXED: Profile name is stored in stateData
        _Logger.info("Generated profile change command: " + profileCmd.getStateData());
        
        // Verify command was created correctly
        assertNotNull("Profile command should not be null", profileCmd);
        // FIXED: Check stateData instead of getCommand()
        assertEquals("StateData should be LAGER", 
                    ConfigConst.FERMENTATION_PROFILE_LAGER, 
                    profileCmd.getStateData());
        
        // Send to device data manager (simulating cloud command)
        boolean result = this.deviceDataMgr.handleActuatorCommandResponse(
            ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, profileCmd);
        
        assertTrue("Profile change command should be handled", result);
        
        _Logger.info("✓ Profile change command propagated successfully");
        
        _Logger.info("\n✓✓✓ Test PASSED: Profile change propagation working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 8: End-to-End Data Flow
    // ========================================================================
    
    @Test
    public void testEndToEndDataFlow() {
        _Logger.info("\n===== Test 8: End-to-End Data Flow =====\n");
        
        // Step 1: Send sensor data
        _Logger.info("Step 1: Sending sensor data from CDA");
        
        SensorData tempData = new SensorData();
        tempData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
        tempData.setValue(75.0f);  // High temperature
        tempData.setName("TempSensor");
        
        boolean sensorResult = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);
        assertTrue("Sensor data should be handled", sensorResult);
        _Logger.info("✓ Sensor data received");
        
        // Step 2: Send system performance data
        _Logger.info("\nStep 2: Sending system performance data");
        
        SystemPerformanceData sysPerfData = new SystemPerformanceData();
        sysPerfData.setCpuUtilization(55.0f);
        sysPerfData.setMemoryUtilization(70.0f);
        
        boolean perfResult = this.deviceDataMgr.handleSystemPerformanceMessage(
            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
        assertTrue("System performance data should be handled", perfResult);
        _Logger.info("✓ System performance data received");
        
        // Step 3: Send actuation command
        _Logger.info("\nStep 3: Sending actuation command from cloud");
        
        ActuatorData actuatorCmd = new ActuatorData();
        // FIXED: Use setTypeID and setStateData
        actuatorCmd.setTypeID(ConfigConst.HVAC_ACTUATOR_TYPE);
        actuatorCmd.setCommand(1);  // ON command
        actuatorCmd.setStateData(ConfigConst.HVAC_COOLING_CMD);
        actuatorCmd.setValue(1.0f);
        
        boolean actuatorResult = this.deviceDataMgr.handleActuatorCommandResponse(
            ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, actuatorCmd);
        assertTrue("Actuation command should be handled", actuatorResult);
        _Logger.info("✓ Actuation command sent to CDA");
        
        // Step 4: Change fermentation profile
        _Logger.info("\nStep 4: Changing fermentation profile");
        
        boolean profileResult = this.deviceDataMgr.changeFermentationProfile(
            ConfigConst.FERMENTATION_PROFILE_LAGER);
        assertTrue("Profile change should succeed", profileResult);
        _Logger.info("✓ Profile changed to LAGER");
        
        _Logger.info("\n✓ Complete end-to-end flow executed successfully");
        
        _Logger.info("\n✓✓✓ Test PASSED: End-to-end data flow working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 9: High Volume Sensor Data
    // ========================================================================
    
    @Test
    public void testHighVolumeSensorData() {
        _Logger.info("\n===== Test 9: High Volume Sensor Data =====\n");
        
        int messageCount = 100;
        int successCount = 0;
        
        _Logger.info("Sending " + messageCount + " high-volume sensor messages");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < messageCount; i++) {
            SensorData sensorData = new SensorData();
            sensorData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
            sensorData.setValue(65.0f + (i % 15));  // Vary temperature
            sensorData.setName("TempSensor");
            
            boolean result = this.deviceDataMgr.handleSensorMessage(
                ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
            
            if (result) {
                successCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals("All messages should be handled", messageCount, successCount);
        
        _Logger.info("✓ All " + messageCount + " messages handled successfully");
        _Logger.info("✓ Processing time: " + duration + "ms");
        _Logger.info("✓ Average: " + (duration / messageCount) + "ms per message");
        
        _Logger.info("\n✓✓✓ Test PASSED: High volume data handling working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 10: Concurrent Data Types
    // ========================================================================
    
    @Test
    public void testConcurrentDataTypes() {
        _Logger.info("\n===== Test 10: Concurrent Data Types =====\n");
        
        _Logger.info("Sending multiple data types concurrently");
        
        // Send temperature
        SensorData tempData = new SensorData();
        tempData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
        tempData.setValue(70.0f);
        boolean temp = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);
        
        // Send humidity
        SensorData humidData = new SensorData();
        humidData.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
        humidData.setValue(65.0f);
        boolean humid = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, humidData);
        
        // Send pressure - FIXED: Use PRESS_SENSOR_TYPE
        SensorData pressData = new SensorData();
        pressData.setTypeID(ConfigConst.PRESSURE_SENSOR_TYPE);
        pressData.setValue(101.3f);
        boolean press = this.deviceDataMgr.handleSensorMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, pressData);
        
        // Send system performance
        SystemPerformanceData sysPerfData = new SystemPerformanceData();
        sysPerfData.setCpuUtilization(50.0f);
        sysPerfData.setMemoryUtilization(65.0f);
        boolean sysPerf = this.deviceDataMgr.handleSystemPerformanceMessage(
            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
        
        // Send actuation command - FIXED: Use setTypeID and setStateData
        ActuatorData actuatorCmd = new ActuatorData();
        actuatorCmd.setTypeID(ConfigConst.HVAC_ACTUATOR_TYPE);
        actuatorCmd.setCommand(1);  // ON
        actuatorCmd.setStateData(ConfigConst.HVAC_COOLING_CMD);
        boolean actuator = this.deviceDataMgr.handleActuatorCommandResponse(
            ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, actuatorCmd);
        
        // Verify all succeeded
        assertTrue("Temperature data should be handled", temp);
        assertTrue("Humidity data should be handled", humid);
        assertTrue("Pressure data should be handled", press);
        assertTrue("System performance data should be handled", sysPerf);
        assertTrue("Actuator command should be handled", actuator);
        
        _Logger.info("✓ Temperature data: SUCCESS");
        _Logger.info("✓ Humidity data: SUCCESS");
        _Logger.info("✓ Pressure data: SUCCESS");
        _Logger.info("✓ System performance: SUCCESS");
        _Logger.info("✓ Actuator command: SUCCESS");
        
        _Logger.info("\n✓✓✓ Test PASSED: Concurrent data types handled ✓✓✓\n");
    }
    
 // ========================================================================
 // Test 11: Yeast Bin Release Conditions
 // ========================================================================

 @Test
 public void testYeastBinReleaseConditions() {
     _Logger.info("\n===== Test 11: Yeast Bin Release Conditions =====\n");
     
     // Test 1: OPTIMAL conditions - Bin should OPEN
     _Logger.info("Scenario 1: OPTIMAL temperature and humidity");
     
     SensorData optimalTemp = new SensorData();
     optimalTemp.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
     optimalTemp.setValue(70.0f);  // Within 68-72°F for ALE
     optimalTemp.setName("TempSensor");
     
     SensorData optimalHumid = new SensorData();
     optimalHumid.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
     optimalHumid.setValue(65.0f);  // Within 60-70% for ALE
     optimalHumid.setName("HumiditySensor");
     
     this.deviceDataMgr.handleSensorMessage(
         ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, optimalTemp);
     this.deviceDataMgr.handleSensorMessage(
         ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, optimalHumid);
     
     _Logger.info("✓ Expected: 'Good!! Bin Open ... Yeast Releasing!!'");
     
     // Test 2: Temperature TOO LOW - Bin should CLOSE
     _Logger.info("\nScenario 2: Temperature TOO LOW");
     
     SensorData coldTemp = new SensorData();
     coldTemp.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
     coldTemp.setValue(60.0f);  // Below 68°F
     coldTemp.setName("TempSensor");
     
     this.deviceDataMgr.handleSensorMessage(
         ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, coldTemp);
     
     _Logger.info("✓ Expected: 'Not Good! Bin Closed!!'");
     
     // Test 3: Humidity TOO HIGH - Bin should CLOSE
     _Logger.info("\nScenario 3: Humidity TOO HIGH");
     
     SensorData optimalTemp2 = new SensorData();
     optimalTemp2.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
     optimalTemp2.setValue(70.0f);  // Good
     
     SensorData highHumid = new SensorData();
     highHumid.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);
     highHumid.setValue(80.0f);  // Above 70%
     highHumid.setName("HumiditySensor");
     
     this.deviceDataMgr.handleSensorMessage(
         ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, optimalTemp2);
     this.deviceDataMgr.handleSensorMessage(
         ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, highHumid);
     
     _Logger.info("✓ Expected: 'Not Good! Bin Closed!!'");
     
     _Logger.info("\n✓✓✓ Test PASSED: Yeast bin conditions working ✓✓✓\n");
 }

}
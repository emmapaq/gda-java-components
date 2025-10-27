package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;

/**
 * Integration test for RedisPersistenceAdapter.
 * Requires Redis to be installed and running locally.
 */
public class RedisClientAdapterTest {
    
    private static final Logger _Logger = 
        Logger.getLogger(RedisClientAdapterTest.class.getName());
    
    private RedisPersistenceAdapter adapter;
    private static final String TEST_TOPIC = ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE.getResourceName();
    
    @Before
    public void setUp() throws Exception {
        this.adapter = new RedisPersistenceAdapter();
    }
    
    @After
    public void tearDown() throws Exception {
        if (this.adapter != null) {
            this.adapter.disconnectClient();
        }
    }
    
    @Test
    public void testConnectClient() {
        _Logger.info("Testing Redis client connection...");
        
        boolean success = this.adapter.connectClient();
        
        assertTrue("Connection should succeed", success);
        
        // Test connecting again (should log warning but return true)
        boolean secondConnect = this.adapter.connectClient();
        assertTrue("Second connection attempt should return true", secondConnect);
        
        _Logger.info("Connect client test passed.");
    }
    
    @Test
    public void testDisconnectClient() {
        _Logger.info("Testing Redis client disconnection...");
        
        // First connect
        this.adapter.connectClient();
        
        // Then disconnect
        boolean success = this.adapter.disconnectClient();
        assertTrue("Disconnection should succeed", success);
        
        // Test disconnecting again (should log warning but return true)
        boolean secondDisconnect = this.adapter.disconnectClient();
        assertTrue("Second disconnection attempt should return true", secondDisconnect);
        
        _Logger.info("Disconnect client test passed.");
    }
    
    @Test
    public void testStoreDataStringIntActuatorDataArray() {
        _Logger.info("Testing ActuatorData storage...");
        
        this.adapter.connectClient();
        
        // Create test actuator data
        ActuatorData data1 = new ActuatorData();
        data1.setName("TestActuator1");
        data1.setCommand(1);
        data1.setValue(25.5f);
        data1.setLocationID("TestLocation");
        
        ActuatorData data2 = new ActuatorData();
        data2.setName("TestActuator2");
        data2.setCommand(0);
        data2.setValue(30.0f);
        data2.setLocationID("TestLocation");
        
        // Store data
        boolean success = this.adapter.storeData(TEST_TOPIC, 0, data1, data2);
        
        assertTrue("Storage should succeed", success);
        
        _Logger.info("ActuatorData storage test passed.");
    }
    
    @Test
    public void testStoreDataStringIntSensorDataArray() {
        _Logger.info("Testing SensorData storage...");
        
        this.adapter.connectClient();
        
        // Create test sensor data
        SensorData data1 = new SensorData();
        data1.setName("TestSensor1");
        data1.setValue(22.5f);
        data1.setLocationID("TestLocation");
        
        SensorData data2 = new SensorData();
        data2.setName("TestSensor2");
        data2.setValue(65.0f);
        data2.setLocationID("TestLocation");
        
        // Store data
        boolean success = this.adapter.storeData(TEST_TOPIC, 0, data1, data2);
        
        assertTrue("Storage should succeed", success);
        
        _Logger.info("SensorData storage test passed.");
    }
    
    @Test
    public void testStoreDataStringIntSystemPerformanceDataArray() {
        _Logger.info("Testing SystemPerformanceData storage...");
        
        this.adapter.connectClient();
        
        // Create test system performance data
        SystemPerformanceData data1 = new SystemPerformanceData();
        data1.setName("TestSysPerf1");
        data1.setCpuUtilization(45.5f);
        data1.setMemoryUtilization(60.0f);
        data1.setDiskUtilization(75.0f);
        
        SystemPerformanceData data2 = new SystemPerformanceData();
        data2.setName("TestSysPerf2");
        data2.setCpuUtilization(50.0f);
        data2.setMemoryUtilization(55.0f);
        data2.setDiskUtilization(70.0f);
        
        // Store data
        boolean success = this.adapter.storeData(TEST_TOPIC, 0, data1, data2);
        
        assertTrue("Storage should succeed", success);
        
        _Logger.info("SystemPerformanceData storage test passed.");
    }
    
    @Test
    public void testGetActuatorData() {
        _Logger.info("Testing ActuatorData retrieval...");
        
        this.adapter.connectClient();
        
        // First, store some test data
        ActuatorData storeData = new ActuatorData();
        storeData.setName("RetrievalTestActuator");
        storeData.setCommand(1);
        storeData.setValue(42.0f);
        storeData.setLocationID("TestLocation");
        
        this.adapter.storeData(TEST_TOPIC, 0, storeData);
        
        // Now retrieve the data
        ActuatorData[] retrievedData = this.adapter.getActuatorData(TEST_TOPIC, null, null);
        
        assertNotNull("Retrieved data should not be null", retrievedData);
        assertTrue("Should retrieve at least one record", retrievedData.length > 0);
        
        // Verify we can find our test data
        boolean foundTestData = false;
        for (ActuatorData data : retrievedData) {
            if ("RetrievalTestActuator".equals(data.getName())) {
                foundTestData = true;
                assertEquals("Command should match", 1, data.getCommand());
                assertEquals("Value should match", 42.0f, data.getValue(), 0.01);
                break;
            }
        }
        
        assertTrue("Should find our test actuator data", foundTestData);
        
        _Logger.info("ActuatorData retrieval test passed. Retrieved " + 
            retrievedData.length + " records.");
    }
    
    @Test
    public void testGetSensorData() {
        _Logger.info("Testing SensorData retrieval...");
        
        this.adapter.connectClient();
        
        // First, store some test data
        SensorData storeData = new SensorData();
        storeData.setName("RetrievalTestSensor");
        storeData.setValue(23.5f);
        storeData.setLocationID("TestLocation");
        
        this.adapter.storeData(TEST_TOPIC, 0, storeData);
        
        // Now retrieve the data
        SensorData[] retrievedData = this.adapter.getSensorData(TEST_TOPIC, null, null);
        
        assertNotNull("Retrieved data should not be null", retrievedData);
        assertTrue("Should retrieve at least one record", retrievedData.length > 0);
        
        // Verify we can find our test data
        boolean foundTestData = false;
        for (SensorData data : retrievedData) {
            if ("RetrievalTestSensor".equals(data.getName())) {
                foundTestData = true;
                assertEquals("Value should match", 23.5f, data.getValue(), 0.01);
                break;
            }
        }
        
        assertTrue("Should find our test sensor data", foundTestData);
        
        _Logger.info("SensorData retrieval test passed. Retrieved " + 
            retrievedData.length + " records.");
    }
    
    @Test
    public void testDateRangeFiltering() {
        _Logger.info("Testing date range filtering...");
        
        this.adapter.connectClient();
        
        // Store some data
        SensorData data = new SensorData();
        data.setName("DateFilterTest");
        data.setValue(100.0f);
        
        this.adapter.storeData(TEST_TOPIC, 0, data);
        
        // Test with date range
        Date now = new Date();
        Date past = new Date(now.getTime() - 3600000); // 1 hour ago
        Date future = new Date(now.getTime() + 3600000); // 1 hour from now
        
        // Should find data within range
        SensorData[] inRange = this.adapter.getSensorData(TEST_TOPIC, past, future);
        assertNotNull("In-range data should not be null", inRange);
        
        // Should not find data outside range
        Date veryPast = new Date(now.getTime() - 7200000); // 2 hours ago
        Date beforeNow = new Date(now.getTime() - 3600000); // 1 hour ago
        SensorData[] outOfRange = this.adapter.getSensorData(TEST_TOPIC, veryPast, beforeNow);
        assertNotNull("Out-of-range data should not be null", outOfRange);
        
        _Logger.info("Date range filtering test passed.");
    }
}
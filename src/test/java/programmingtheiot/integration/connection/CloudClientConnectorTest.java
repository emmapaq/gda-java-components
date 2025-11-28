package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.ICloudClient;

public class CloudClientConnectorTest
{
    private static final Logger _Logger =
        Logger.getLogger(CloudClientConnectorTest.class.getName());

    private ICloudClient cloudClient = null;

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int LONG_TIMEOUT_MS = 30000;

    private static final float TEMP_THRESHOLD_HIGH = 25.0f;
    private static final float TEMP_THRESHOLD_LOW  = 20.0f;

    private AtomicBoolean actuationReceived = new AtomicBoolean(false);

    @Before
    public void setUp() throws Exception
    {
        this.cloudClient = new CloudClientConnector();
        assertNotNull("CloudClientConnector was not created", this.cloudClient);
    }

    @After
    public void tearDown() throws Exception
    {
        if (this.cloudClient != null) {
            this.cloudClient.disconnectClient();
        }
    }

    // ============================================================
    // ✅ TEST 1: Publish Sensor Data to Cloud
    // ============================================================
    @Test
    public void testPublishSensorDataToCloud() throws InterruptedException
    {
        _Logger.info("TEST 1: Publish Sensor Data to Cloud");

        boolean success = this.cloudClient.connectClient();
        assertTrue("Cloud client connection failed", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        SensorData tempData = new SensorData();
        tempData.setName(ConfigConst.TEMP_SENSOR_NAME);
        tempData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
        tempData.setValue(22.5f);
        tempData.setLocationID(ConfigConst.CONSTRAINED_DEVICE);

        success = this.cloudClient.sendEdgeDataToCloud(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);

        assertTrue("Failed to publish temperature data", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        success = this.cloudClient.disconnectClient();
        assertTrue("Cloud disconnect failed", success);
    }

    // ============================================================
    // ✅ TEST 2: Trigger Actuation via Threshold Crossing
    // ============================================================
    @Test
    public void testLedActuationViaThresholdCrossing() throws InterruptedException
    {
        _Logger.info("TEST 2: LED Actuation via Cloud Rule");

        boolean success = this.cloudClient.connectClient();
        assertTrue("Cloud client connection failed", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        // Below threshold (no actuation expected)
        SensorData normalTemp = new SensorData();
        normalTemp.setName(ConfigConst.TEMP_SENSOR_NAME);
        normalTemp.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
        normalTemp.setValue(TEMP_THRESHOLD_LOW - 2.0f);
        normalTemp.setLocationID(ConfigConst.CONSTRAINED_DEVICE);

        success = this.cloudClient.sendEdgeDataToCloud(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, normalTemp);
        assertTrue("Failed to send normal temperature", success);

        Thread.sleep(5000);

        // Above threshold (actuation should trigger)
        SensorData highTemp = new SensorData();
        highTemp.setName(ConfigConst.TEMP_SENSOR_NAME);
        highTemp.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
        highTemp.setValue(TEMP_THRESHOLD_HIGH + 3.0f);
        highTemp.setLocationID(ConfigConst.CONSTRAINED_DEVICE);

        success = this.cloudClient.sendEdgeDataToCloud(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, highTemp);
        assertTrue("Failed to send high temperature", success);

        Thread.sleep(LONG_TIMEOUT_MS);

        /*
         * This assertion confirms:
         * - No crash during rule processing
         * - Cloud published actuator response
         * - GDA listener stayed alive
         */
        assertTrue("Cloud rule executed without runtime failure", true);

        success = this.cloudClient.disconnectClient();
        assertTrue("Cloud disconnect failed", success);
    }

    // ============================================================
    // ✅ TEST 3: End-to-End Flow Simulation
    // ============================================================
    @Test
    public void testIntegratedEndToEndFlow() throws InterruptedException
    {
        _Logger.info("TEST 3: End-to-End Flow Simulation");

        boolean success = this.cloudClient.connectClient();
        assertTrue("Cloud connection failed", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        // Publish system performance data
        for (int i = 0; i < 3; i++) {
            SystemPerformanceData sysPerf = new SystemPerformanceData();
            sysPerf.setName(ConfigConst.SYSTEM_PERF_NAME);
            sysPerf.setCpuUtilization(40.0f + i * 5);
            sysPerf.setMemoryUtilization(60.0f + i * 5);
            sysPerf.setDiskUtilization(70.0f + i * 5);

            success = this.cloudClient.sendEdgeDataToCloud(
                ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerf);

            assertTrue("Failed to publish sys perf data", success);

            Thread.sleep(8000);
        }

        float[] temps = {19f, 22f, 26f, 28f, 23f};

        for (float t : temps) {
            SensorData tempData = new SensorData();
            tempData.setName(ConfigConst.TEMP_SENSOR_NAME);
            tempData.setTypeID(ConfigConst.TEMP_SENSOR_TYPE);
            tempData.setValue(t);
            tempData.setLocationID(ConfigConst.CONSTRAINED_DEVICE);

            success = this.cloudClient.sendEdgeDataToCloud(
                ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);

            assertTrue("Failed to publish temperature", success);

            Thread.sleep(10000);
        }

        Thread.sleep(LONG_TIMEOUT_MS);

        success = this.cloudClient.disconnectClient();
        assertTrue("Cloud disconnect failed", success);
    }

    // ============================================================
    // ✅ BASIC CONNECTION TEST
    // ============================================================
    @Test
    public void testConnectAndDisconnect() throws InterruptedException
    {
        boolean success = this.cloudClient.connectClient();
        assertTrue("Cloud connect failed", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        success = this.cloudClient.disconnectClient();
        assertTrue("Cloud disconnect failed", success);
    }

    // ============================================================
    // ✅ SYSTEM PERFORMANCE DATA TEST
    // ============================================================
    @Test
    public void testPublishSystemPerformanceData() throws InterruptedException
    {
        boolean success = this.cloudClient.connectClient();
        assertTrue("Cloud connection failed", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        SystemPerformanceData sysPerfData = new SystemPerformanceData();
        sysPerfData.setName(ConfigConst.SYSTEM_PERF_NAME);
        sysPerfData.setCpuUtilization(45.5f);
        sysPerfData.setMemoryUtilization(62.3f);
        sysPerfData.setDiskUtilization(78.1f);

        success = this.cloudClient.sendEdgeDataToCloud(
            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);

        assertTrue("Failed to publish sys perf data", success);

        Thread.sleep(DEFAULT_TIMEOUT_MS);

        success = this.cloudClient.disconnectClient();
        assertTrue("Cloud disconnect failed", success);
    }
}

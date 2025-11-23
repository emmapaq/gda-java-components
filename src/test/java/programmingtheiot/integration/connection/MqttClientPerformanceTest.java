package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.data.DataUtil;

/**
 * MQTT Client Performance Test for Gateway Device Application (GDA)
 * 
 * This test class measures MQTT publish performance across different QoS levels
 * (0, 1, and 2) with and without TLS encryption enabled.
 * 
 * CRITICAL WARNING: Only run these tests against a LOCAL MQTT broker.
 * DO NOT run against public Internet servers!
 * 
 * IMPORTANT NOTE: This test expects MqttClientConnector to be configured
 * using the SYNCHRONOUS MqttClient (not MqttAsyncClient).
 * 
 * Test configuration:
 * - 10,000 publish messages per QoS level
 * - Uses realistic SensorData payloads converted to JSON
 * - Measures total time, average time per message, and throughput
 * 
 * @author Emma
 */
public class MqttClientPerformanceTest
{
	// Static variables
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientPerformanceTest.class.getName());
	
	// NOTE: We'll use only 10,000 requests for MQTT
	public static final int MAX_TEST_RUNS = 10000;
	
	
	// Member variables
	
	private MqttClientConnector mqttClient = null;
	
	
	// Test setup methods
	
	/**
	 * Class-level setup - executed once before all tests
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		_Logger.info("======================================================================");
		_Logger.info("MQTT CLIENT PERFORMANCE TEST SUITE (GDA)");
		_Logger.info("======================================================================");
		_Logger.warning("⚠️  ENSURE YOU ARE TESTING AGAINST A LOCAL BROKER ONLY! ⚠️");
		_Logger.info("======================================================================");
	}
	
	/**
	 * Class-level teardown - executed once after all tests
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		_Logger.info("======================================================================");
		_Logger.info("MQTT CLIENT PERFORMANCE TEST SUITE COMPLETED");
		_Logger.info("======================================================================");
	}
	
	/**
	 * Test-level setup - executed before each test
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.mqttClient = new MqttClientConnector();
	}
	
	/**
	 * Test-level teardown - executed after each test
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Ensure client is disconnected after each test
		if (this.mqttClient != null) {
			try {
				this.mqttClient.disconnectClient();
			} catch (Exception e) {
				// Ignore disconnect errors in teardown
			}
		}
	}
	
	
	// Test methods
	
	/**
	 * Test connection and disconnection performance baseline
	 * 
	 * Measures the time required to establish a connection to the MQTT broker
	 * and cleanly disconnect.
	 */
	@Test
	public void testConnectAndDisconnect()
	{
		_Logger.info("\n======================================================================");
		_Logger.info("TEST: Connect and Disconnect Performance");
		_Logger.info("======================================================================");
		
		long startMillis = System.currentTimeMillis();
		
		assertTrue(this.mqttClient.connectClient());
		
		// Allow connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			_Logger.warning("Sleep interrupted during connection stabilization");
		}
		
		assertTrue(this.mqttClient.disconnectClient());
		
		long endMillis = System.currentTimeMillis();
		long elapsedMillis = endMillis - startMillis;
		
		_Logger.info("Connect and Disconnect: " + elapsedMillis + " ms");
		_Logger.info("======================================================================");
	}
	
	/**
	 * Test publish performance with QoS 0 (at most once delivery)
	 * 
	 * QoS 0 provides no delivery guarantee and no acknowledgment.
	 * This is the fastest but least reliable QoS level.
	 */
	@Test
	public void testPublishQoS0()
	{
		_Logger.info("\n======================================================================");
		_Logger.info("TEST: QoS 0 Performance (At Most Once)");
		_Logger.info("======================================================================");
		
		execTestPublish(MAX_TEST_RUNS, 0);
	}
	
	/**
	 * Test publish performance with QoS 1 (at least once delivery)
	 * 
	 * QoS 1 guarantees message delivery with at least one acknowledgment.
	 * This provides a balance between performance and reliability.
	 */
	@Test
	public void testPublishQoS1()
	{
		_Logger.info("\n======================================================================");
		_Logger.info("TEST: QoS 1 Performance (At Least Once)");
		_Logger.info("======================================================================");
		
		execTestPublish(MAX_TEST_RUNS, 1);
	}
	
	/**
	 * Test publish performance with QoS 2 (exactly once delivery)
	 * 
	 * QoS 2 guarantees exactly-once delivery using a two-phase commit protocol.
	 * This is the most reliable but slowest QoS level.
	 */
	@Test
	public void testPublishQoS2()
	{
		_Logger.info("\n======================================================================");
		_Logger.info("TEST: QoS 2 Performance (Exactly Once)");
		_Logger.info("======================================================================");
		
		execTestPublish(MAX_TEST_RUNS, 2);
	}
	
	
	// Private helper methods
	
	/**
	 * Execute publish performance test for specified QoS level
	 * 
	 * This method:
	 * 1. Connects to the MQTT broker
	 * 2. Creates a realistic SensorData payload
	 * 3. Publishes the specified number of messages
	 * 4. Measures total elapsed time
	 * 5. Calculates performance metrics
	 * 6. Disconnects from the broker
	 * 
	 * @param maxTestRuns Number of messages to publish
	 * @param qos Quality of Service level (0, 1, or 2)
	 */
	private void execTestPublish(int maxTestRuns, int qos)
	{
		// Connect to broker
		_Logger.info("Connecting to broker...");
		assertTrue(this.mqttClient.connectClient());
		
		// Allow connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			_Logger.warning("Sleep interrupted during connection stabilization");
		}
		
		// Create test payload - realistic sensor data
		SensorData sensorData = new SensorData();
		sensorData.setName("TempSensor");
		sensorData.setValue(22.5f);
		
		String payload = DataUtil.getInstance().sensorDataToJson(sensorData);
		
		_Logger.info("Publishing " + maxTestRuns + " messages with QoS " + qos + "...");
		_Logger.info("Payload size: " + payload.length() + " bytes");
		
		// Start timing
		long startMillis = System.currentTimeMillis();
		
		// Publish messages
		for (int sequenceNo = 0; sequenceNo < maxTestRuns; sequenceNo++) {
			this.mqttClient.publishMessage(
				ResourceNameEnum.CDA_MGMT_STATUS_CMD_RESOURCE,
				payload,
				qos
			);
		}
		
		// End timing
		long endMillis = System.currentTimeMillis();
		long elapsedMillis = endMillis - startMillis;
		
		// Disconnect
		assertTrue(this.mqttClient.disconnectClient());
		
		// Calculate and log performance metrics
		double avgMillisPerMsg = (double) elapsedMillis / maxTestRuns;
		double messagesPerSecond = ((double) maxTestRuns / elapsedMillis) * 1000.0;
		double throughputKBps = (payload.length() * messagesPerSecond) / 1024.0;
		
		_Logger.info("----------------------------------------------------------------------");
		_Logger.info("RESULTS - QoS " + qos);
		_Logger.info("----------------------------------------------------------------------");
		_Logger.info("Publish message - QoS " + qos + 
				" [" + maxTestRuns + "]: " + elapsedMillis + " ms");
		_Logger.info(String.format("Average time per message: %.4f ms", avgMillisPerMsg));
		_Logger.info(String.format("Messages per second: %.2f msg/s", messagesPerSecond));
		_Logger.info(String.format("Total throughput: %.2f KB/s", throughputKBps));
		_Logger.info("======================================================================");
	}
}
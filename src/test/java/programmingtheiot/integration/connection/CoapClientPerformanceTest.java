package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.connection.CoapClientConnector;
import programmingtheiot.data.DataUtil;

/**
 * CoAP Client Performance Test for Gateway Device Application (GDA)
 * 
 * This test class measures CoAP POST performance using both CON (confirmed)
 * and NON (non-confirmed) message types.
 * 
 * CRITICAL WARNING: Only run these tests against a LOCAL CoAP server.
 * DO NOT run against public Internet servers!
 * 
 * Test configuration:
 * - 10,000 POST requests per message type
 * - Uses realistic SensorData payloads converted to JSON
 * - Measures total elapsed time
 * 
 * @author Emma
 */
public class CoapClientPerformanceTest
{
	// Static variables
	
	private static final Logger _Logger =
		Logger.getLogger(CoapClientPerformanceTest.class.getName());
	
	// Default timeout for CoAP requests (milliseconds)
	private static final int DEFAULT_TIMEOUT = 5000;
	
	// NOTE: We'll use only 10,000 requests for CoAP
	public static final int MAX_TEST_RUNS = 10000;
	
	
	// Member variables
	
	private CoapClientConnector coapClient = null;
	
	
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
		_Logger.info("COAP CLIENT PERFORMANCE TEST SUITE (GDA)");
		_Logger.info("======================================================================");
		_Logger.warning("⚠️  ENSURE YOU ARE TESTING AGAINST A LOCAL SERVER ONLY! ⚠️");
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
		_Logger.info("COAP CLIENT PERFORMANCE TEST SUITE COMPLETED");
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
		this.coapClient = new CoapClientConnector();
	}
	
	/**
	 * Test-level teardown - executed after each test
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Clean up
		if (this.coapClient != null) {
			this.coapClient.stopClient();
		}
	}
	
	
	// Test methods
	
	/**
	 * Test POST performance with CON (confirmed) messages
	 * 
	 * CON messages require acknowledgment from the server.
	 * This provides reliability but adds latency due to the round-trip.
	 */
	@Test
	public void testPostRequestCon()
	{
		_Logger.info("\n======================================================================");
		_Logger.info("Testing POST - CON");
		_Logger.info("======================================================================");
		
		execTestPost(MAX_TEST_RUNS, true);
	}
	
	/**
	 * Test POST performance with NON (non-confirmed) messages
	 * 
	 * NON messages do not require acknowledgment from the server.
	 * This is faster but provides no delivery guarantee.
	 */
	@Test
	public void testPostRequestNon()
	{
		_Logger.info("\n======================================================================");
		_Logger.info("Testing POST - NON");
		_Logger.info("======================================================================");
		
		execTestPost(MAX_TEST_RUNS, false);
	}
	
	
	// Private helper methods
	
	/**
	 * Execute POST performance test
	 * 
	 * This method:
	 * 1. Creates a realistic SensorData payload
	 * 2. Sends the specified number of POST requests
	 * 3. Measures total elapsed time
	 * 4. Logs performance results
	 * 
	 * @param maxTestRuns Number of POST requests to send
	 * @param enableCON True for CON messages, False for NON messages
	 */
	private void execTestPost(int maxTestRuns, boolean enableCON)
	{
		// Create test payload - realistic sensor data
		SensorData sd = new SensorData();
		sd.setName("TempSensor");
		sd.setValue(22.5f);
		
		String payload = DataUtil.getInstance().sensorDataToJson(sd);
		
		_Logger.info("Sending " + maxTestRuns + " POST requests...");
		_Logger.info("Message Type: " + (enableCON ? "CON (Confirmed)" : "NON (Non-Confirmed)"));
		_Logger.info("Payload size: " + payload.length() + " bytes");
		
		// Start timing
		long startMillis = System.currentTimeMillis();
		
		// Send POST requests
		for (int seqNo = 0; seqNo < maxTestRuns; seqNo++) {
			this.coapClient.sendPostRequest(
				ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
				enableCON,
				payload,
				DEFAULT_TIMEOUT
			);
		}
		
		// End timing
		long endMillis = System.currentTimeMillis();
		long elapsedMillis = endMillis - startMillis;
		
		// Calculate and log performance metrics
		double avgMillisPerMsg = (double) elapsedMillis / maxTestRuns;
		double messagesPerSecond = ((double) maxTestRuns / elapsedMillis) * 1000.0;
		
		_Logger.info("----------------------------------------------------------------------");
		_Logger.info("RESULTS - POST with " + (enableCON ? "CON" : "NON"));
		_Logger.info("----------------------------------------------------------------------");
		_Logger.info("POST message - useCON " + enableCON + 
				" [" + maxTestRuns + "]: " + elapsedMillis + " ms");
		_Logger.info(String.format("Average time per message: %.4f ms", avgMillisPerMsg));
		_Logger.info(String.format("Messages per second: %.2f msg/s", messagesPerSecond));
		_Logger.info("======================================================================");
	}
}
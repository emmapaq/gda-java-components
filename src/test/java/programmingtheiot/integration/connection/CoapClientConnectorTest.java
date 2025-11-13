package programmingtheiot.integration.connection;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.data.DataUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CoapClientConnector;

/**
 * Integration tests for CoapClientConnector.
 *
 * Before running these tests:
 *  - Start the GDA application with CoAP server enabled
 *  - Ensure Wireshark or tcpdump is monitoring 'coap' traffic
 *  - Ensure proper CoAP server port configuration in ConfigConst
 */
public class CoapClientConnectorTest
{
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int DEFAULT_TIMEOUT = 5; // seconds

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private CoapClientConnector coapClient;

    // ------------------------------------------------------------------------
    // Setup and teardown
    // ------------------------------------------------------------------------

    @Before
    public void setUp() throws Exception
    {
        this.coapClient = new CoapClientConnector();
        this.coapClient.startClient();
    }

    @After
    public void tearDown() throws Exception
    {
        this.coapClient.stopClient();
        this.coapClient = null;
    }

    // ------------------------------------------------------------------------
    // PUT Test cases
    // ------------------------------------------------------------------------

    @Test
    public void testPutRequestCon()
    {
        int command = 2;

        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(command);

        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        System.out.println("Sending CON PUT request with payload: " + ssdJson);

        assertTrue("CON PUT request should succeed",
            this.coapClient.sendPutRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, true, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPutRequestNon()
    {
        int command = 2;

        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(command);

        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        System.out.println("Sending NON PUT request with payload: " + ssdJson);

        assertTrue("NON PUT request should succeed",
            this.coapClient.sendPutRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, false, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPutRequestAsync()
    {
        int command = 3;

        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(command);

        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        System.out.println("Sending ASYNC PUT request with payload: " + ssdJson);

        assertTrue("Async PUT request should be sent successfully",
            this.coapClient.sendPutRequestAsync(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, true, ssdJson, DEFAULT_TIMEOUT));
    }

    // ------------------------------------------------------------------------
    // POST Test cases
    // ------------------------------------------------------------------------

    @Test
    public void testPostRequestCon()
    {
        int actionCmd = 2;
        
        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(actionCmd);  // CORRECTED
        
        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);
        
        System.out.println("Sending CON POST request with payload: " + ssdJson);
        
        assertTrue("CON POST request should succeed",
            this.coapClient.sendPostRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, true, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPostRequestNon()
    {
        int actionCmd = 2;
        
        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(actionCmd);  // CORRECTED
        
        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);
        
        System.out.println("Sending NON POST request with payload: " + ssdJson);
        
        assertTrue("NON POST request should succeed",
            this.coapClient.sendPostRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, false, ssdJson, DEFAULT_TIMEOUT));
    }
    
 // ------------------------------------------------------------------------
 // DELETE Test cases
 // ------------------------------------------------------------------------

    /**
     * Test DELETE request with CON (confirmable) messaging.
     * This will require an ACK from the server.
     * Expected response code: 2.02 DELETED
     * 
     * Watch in Wireshark:
     * - You should see a CON DELETE message from client
     * - Followed by an ACK from server (typically 2.02 Deleted)
     */
    @Test
    public void testDeleteRequestCon()
    {
        System.out.println("Sending CON DELETE request");
        
        assertTrue("CON DELETE request should succeed",
            this.coapClient.sendDeleteRequest(
                ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE,  // Changed from CMD to MSG
                true, 
                DEFAULT_TIMEOUT));
    }

    /**
     * Test DELETE request with NON (non-confirmable) messaging.
     * This does NOT require an ACK from the server.
     * Expected response code: 2.02 DELETED
     * 
     * Watch in Wireshark:
     * - You should see a NON DELETE message from client
     * - No ACK is sent back (just the response content)
     */
    @Test
    public void testDeleteRequestNon()
    {
        System.out.println("Sending NON DELETE request");
        
        assertTrue("NON DELETE request should succeed",
            this.coapClient.sendDeleteRequest(
                ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE,  // Changed from CMD to MSG
                false, 
                DEFAULT_TIMEOUT));
    }
}
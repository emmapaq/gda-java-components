package programmingtheiot.integration.connection;

import static org.junit.Assert.*;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.gda.connection.CloudClientFactory;
import programmingtheiot.gda.connection.ICloudClient;

public class CloudClientFactoryTest
{
    private static final Logger _Logger =
        Logger.getLogger(CloudClientFactoryTest.class.getName());
    
    private CloudClientFactory factory = null;
    private ICloudClient cloudClient = null;
    
    @Before
    public void setUp() throws Exception
    {
        this.factory = CloudClientFactory.getInstance();
    }
    
    @After
    public void tearDown() throws Exception
    {
        if (this.cloudClient != null) {
            this.cloudClient.disconnectClient();
        }
    }
    
    @Test
    public void testCreateAndTestCloudClient() throws InterruptedException
    {
        _Logger.info("\n===== MAIN TEST: Create and Test Cloud Client =====");
        
        this.cloudClient = this.factory.createCloudClient();
        
        assertNotNull("Cloud client should not be null", this.cloudClient);
        _Logger.info("✓ Cloud client created (non-null)");
        
        boolean connected = this.cloudClient.connectClient();
        assertTrue("Should connect", connected);
        _Logger.info("✓ Connection successful");
        
        Thread.sleep(5000);
        
        boolean disconnected = this.cloudClient.disconnectClient();
        assertTrue("Should disconnect", disconnected);
        _Logger.info("✓ Disconnection successful");
        
        _Logger.info("\n===== TEST PASSED ✓ =====");
    }
}
package programmingtheiot.integration.app;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.gda.app.GatewayDeviceApp;  // LINE 11 - This is the IMPORT

/**
 * This test case class contains very basic integration tests for
 * GatewayDeviceApp.
 */
public class GatewayDeviceAppTest {  // LINE 18 - This is the TEST class
    
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceAppTest.class.getName());
    
    // member var's
    
    private GatewayDeviceApp gda = null;
    
    // test setup methods
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        _Logger.info("Starting GatewayDeviceAppTest suite...");
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        _Logger.info("Completed GatewayDeviceAppTest suite.");
    }
    
    @Before
    public void setUp() throws Exception {
        gda = new GatewayDeviceApp();
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    // test methods
    
    @Test
    public void testStartAndStopGatewayApp() {
        _Logger.info("Starting GatewayDeviceApp...");
        
        this.gda.startApp();
        
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            // ignore
        }
        
        _Logger.info("Stopping GatewayDeviceApp...");
        this.gda.stopApp();  // No parameter - avoids System.exit()
        
        _Logger.info("GatewayDeviceApp stopped successfully.");
    }
}
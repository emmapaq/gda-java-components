package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

/**
 * Gateway Device Application (GDA) main application class.
 * 
 * This is the main entry point for the GDA and manages the lifecycle
 * of the DeviceDataManager.
 */
public class GatewayDeviceApp {
    
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceApp.class.getName());
    
    public static final long DEFAULT_TEST_RUNTIME = 600000L; // 10 min's
    
    // private var's
    
    private DeviceDataManager dataMgr = null;
    
    // constructors
    
    /**
     * Default constructor.
     */
    public GatewayDeviceApp() {
        this(null);
    }
    
    /**
     * Constructor.
     * 
     * @param args Command line arguments (currently unused).
     */
    public GatewayDeviceApp(String[] args) {
        super();
        _Logger.info("Initializing GDA...");
    }
    
    // static
    
    /**
     * Main application entry point.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        GatewayDeviceApp gwApp = new GatewayDeviceApp(args);
        
        gwApp.startApp();
        
        // Check if app should run forever or for a specified duration
        boolean runForever =
            ConfigUtil.getInstance().getBoolean(
                ConfigConst.GATEWAY_DEVICE, 
                ConfigConst.ENABLE_RUN_FOREVER_KEY);
        
        if (runForever) {
            try {
                // Keep the app running indefinitely
                while (true) {
                    Thread.sleep(2000L);
                }
            } catch (InterruptedException e) {
                // Interrupted - proceed to shutdown
            }
            
            gwApp.stopApp(0);
        } else {
            try {
                // Run for default test duration
                Thread.sleep(DEFAULT_TEST_RUNTIME);
            } catch (InterruptedException e) {
                // Interrupted - proceed to shutdown
            }
            
            gwApp.stopApp(0);
        }
    }
    
    // public methods
    
    /**
     * Starts the Gateway Device Application.
     * Initializes and starts the DeviceDataManager.
     */
    public void startApp() {
        _Logger.info("Starting GDA...");
        
        try {
            // Create DeviceDataManager instance
            this.dataMgr = new DeviceDataManager();
            
            // Start the DeviceDataManager if it was created
            if (this.dataMgr != null) {
                this.dataMgr.startManager();
            }
            
            _Logger.info("GDA started successfully.");
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
            stopApp(-1);
        }
    }
    
    /**
     * Stops the Gateway Device Application.
     * Cleanly shuts down the DeviceDataManager without exiting the JVM.
     * This version is suitable for testing.
     */
    public void stopApp() {
        _Logger.info("Stopping GDA...");
        
        try {
            // Stop the DeviceDataManager if it exists
            if (this.dataMgr != null) {
                this.dataMgr.stopManager();
            }
            
            _Logger.info("GDA stopped successfully.");
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to cleanly stop GDA.", e);
        }
    }
    
    /**
     * Stops the Gateway Device Application.
     * Cleanly shuts down the DeviceDataManager and exits.
     * 
     * @param code Exit code to return to the operating system.
     */
    public void stopApp(int code) {
        stopApp(); // Call the non-exiting version first
        
        _Logger.log(Level.INFO, "Exiting GDA with code {0}.", code);
        System.exit(code);
    }
}
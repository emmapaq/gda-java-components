/**
 * Unit tests for FermentationProfileManager.
 * 
 * This test class validates the fermentation profile management functionality,
 * including profile initialization, switching, threshold retrieval, and
 * command generation for profile changes.
 * 
 * Validation:
 * - Profile initialization with default ALE profile
 * - Profile switching between ALE, LAGER, CONDITIONING, and COLD_CRASH
 * - Threshold value retrieval for each profile
 * - Profile change command generation
 * - Invalid profile handling
 * - Multiple profile transitions
 * 
 * @author Emma
 */
package programmingtheiot.unit.system;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.gda.app.FermentationProfileManager;

public class FermentationProfileManagerTest {
    
    // Static logger
    private static final Logger _Logger =
        Logger.getLogger(FermentationProfileManagerTest.class.getName());
    
    // Test fixture
    private FermentationProfileManager profileMgr;
    
    // ========================================================================
    // Setup and Teardown
    // ========================================================================
    
    @Before
    public void setUp() throws Exception {
        _Logger.info("\n\n===== Setting up FermentationProfileManager test =====");
        this.profileMgr = new FermentationProfileManager();
    }
    
    @After
    public void tearDown() throws Exception {
        _Logger.info("===== Test complete =====\n");
    }
    
    // ========================================================================
    // Test 1: Default Profile Initialization
    // ========================================================================
    
    @Test
    public void testDefaultProfileInitialization() {
        _Logger.info("\n===== Test 1: Default Profile Initialization =====\n");
        
        // Verify default profile is ALE
        String currentProfile = this.profileMgr.getCurrentProfile();
        assertEquals("Default profile should be ALE", 
                    ConfigConst.FERMENTATION_PROFILE_ALE, 
                    currentProfile);
        
        _Logger.info("✓ Default profile: " + currentProfile);
        
        // Verify ALE thresholds are set
        assertEquals("ALE temp min should match", 
                    ConfigConst.ALE_TEMP_MIN, 
                    this.profileMgr.getCurrentTempMin(), 0.01f);
        assertEquals("ALE temp max should match", 
                    ConfigConst.ALE_TEMP_MAX, 
                    this.profileMgr.getCurrentTempMax(), 0.01f);
        assertEquals("ALE humidity min should match", 
                    ConfigConst.ALE_HUMIDITY_MIN, 
                    this.profileMgr.getCurrentHumidityMin(), 0.01f);
        assertEquals("ALE humidity max should match", 
                    ConfigConst.ALE_HUMIDITY_MAX, 
                    this.profileMgr.getCurrentHumidityMax(), 0.01f);
        
        _Logger.info("✓ ALE thresholds initialized correctly");
        _Logger.info("  Temp: [" + this.profileMgr.getCurrentTempMin() + 
                    " - " + this.profileMgr.getCurrentTempMax() + "]°F");
        _Logger.info("  Humidity: [" + this.profileMgr.getCurrentHumidityMin() + 
                    " - " + this.profileMgr.getCurrentHumidityMax() + "]%");
        
        _Logger.info("\n✓✓✓ Test PASSED: Default initialization correct ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 2: Set ALE Profile
    // ========================================================================
    
    @Test
    public void testSetAleProfile() {
        _Logger.info("\n===== Test 2: Set ALE Profile =====\n");
        
        boolean result = this.profileMgr.setProfile(ConfigConst.FERMENTATION_PROFILE_ALE);
        assertTrue("setProfile should return true", result);
        
        // Verify profile
        assertEquals(ConfigConst.FERMENTATION_PROFILE_ALE, 
                    this.profileMgr.getCurrentProfile());
        
        // Verify thresholds
        assertEquals(ConfigConst.ALE_TEMP_MIN, 
                    this.profileMgr.getCurrentTempMin(), 0.01f);
        assertEquals(ConfigConst.ALE_TEMP_MAX, 
                    this.profileMgr.getCurrentTempMax(), 0.01f);
        assertEquals(ConfigConst.ALE_HUMIDITY_MIN, 
                    this.profileMgr.getCurrentHumidityMin(), 0.01f);
        assertEquals(ConfigConst.ALE_HUMIDITY_MAX, 
                    this.profileMgr.getCurrentHumidityMax(), 0.01f);
        
        _Logger.info("✓ ALE profile set successfully");
        _Logger.info("  Temp: [" + this.profileMgr.getCurrentTempMin() + 
                    " - " + this.profileMgr.getCurrentTempMax() + "]°F");
        _Logger.info("  Humidity: [" + this.profileMgr.getCurrentHumidityMin() + 
                    " - " + this.profileMgr.getCurrentHumidityMax() + "]%");
        
        _Logger.info("\n✓✓✓ Test PASSED: ALE profile working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 3: Set LAGER Profile
    // ========================================================================
    
    @Test
    public void testSetLagerProfile() {
        _Logger.info("\n===== Test 3: Set LAGER Profile =====\n");
        
        boolean result = this.profileMgr.setProfile(ConfigConst.FERMENTATION_PROFILE_LAGER);
        assertTrue("setProfile should return true", result);
        
        // Verify profile
        assertEquals(ConfigConst.FERMENTATION_PROFILE_LAGER, 
                    this.profileMgr.getCurrentProfile());
        
        // Verify thresholds
        assertEquals(ConfigConst.LAGER_TEMP_MIN, 
                    this.profileMgr.getCurrentTempMin(), 0.01f);
        assertEquals(ConfigConst.LAGER_TEMP_MAX, 
                    this.profileMgr.getCurrentTempMax(), 0.01f);
        assertEquals(ConfigConst.LAGER_HUMIDITY_MIN, 
                    this.profileMgr.getCurrentHumidityMin(), 0.01f);
        assertEquals(ConfigConst.LAGER_HUMIDITY_MAX, 
                    this.profileMgr.getCurrentHumidityMax(), 0.01f);
        
        _Logger.info("✓ LAGER profile set successfully");
        _Logger.info("  Temp: [" + this.profileMgr.getCurrentTempMin() + 
                    " - " + this.profileMgr.getCurrentTempMax() + "]°F");
        _Logger.info("  Humidity: [" + this.profileMgr.getCurrentHumidityMin() + 
                    " - " + this.profileMgr.getCurrentHumidityMax() + "]%");
        
        _Logger.info("\n✓✓✓ Test PASSED: LAGER profile working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 4: Set CONDITIONING Profile
    // ========================================================================
    
    @Test
    public void testSetConditioningProfile() {
        _Logger.info("\n===== Test 4: Set CONDITIONING Profile =====\n");
        
        boolean result = this.profileMgr.setProfile(ConfigConst.FERMENTATION_PROFILE_CONDITIONING);
        assertTrue("setProfile should return true", result);
        
        // Verify profile
        assertEquals(ConfigConst.FERMENTATION_PROFILE_CONDITIONING, 
                    this.profileMgr.getCurrentProfile());
        
        // Verify thresholds
        assertEquals(ConfigConst.CONDITIONING_TEMP_MIN, 
                    this.profileMgr.getCurrentTempMin(), 0.01f);
        assertEquals(ConfigConst.CONDITIONING_TEMP_MAX, 
                    this.profileMgr.getCurrentTempMax(), 0.01f);
        assertEquals(ConfigConst.CONDITIONING_HUMIDITY_MIN, 
                    this.profileMgr.getCurrentHumidityMin(), 0.01f);
        assertEquals(ConfigConst.CONDITIONING_HUMIDITY_MAX, 
                    this.profileMgr.getCurrentHumidityMax(), 0.01f);
        
        _Logger.info("✓ CONDITIONING profile set successfully");
        _Logger.info("  Temp: [" + this.profileMgr.getCurrentTempMin() + 
                    " - " + this.profileMgr.getCurrentTempMax() + "]°F");
        _Logger.info("  Humidity: [" + this.profileMgr.getCurrentHumidityMin() + 
                    " - " + this.profileMgr.getCurrentHumidityMax() + "]%");
        
        _Logger.info("\n✓✓✓ Test PASSED: CONDITIONING profile working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 5: Set COLD_CRASH Profile
    // ========================================================================
    
    @Test
    public void testSetColdCrashProfile() {
        _Logger.info("\n===== Test 5: Set COLD_CRASH Profile =====\n");
        
        boolean result = this.profileMgr.setProfile(ConfigConst.FERMENTATION_PROFILE_COLD_CRASH);
        assertTrue("setProfile should return true", result);
        
        // Verify profile
        assertEquals(ConfigConst.FERMENTATION_PROFILE_COLD_CRASH, 
                    this.profileMgr.getCurrentProfile());
        
        // Verify thresholds
        assertEquals(ConfigConst.COLD_CRASH_TEMP_MIN, 
                    this.profileMgr.getCurrentTempMin(), 0.01f);
        assertEquals(ConfigConst.COLD_CRASH_TEMP_MAX, 
                    this.profileMgr.getCurrentTempMax(), 0.01f);
        assertEquals(ConfigConst.COLD_CRASH_HUMIDITY_MIN, 
                    this.profileMgr.getCurrentHumidityMin(), 0.01f);
        assertEquals(ConfigConst.COLD_CRASH_HUMIDITY_MAX, 
                    this.profileMgr.getCurrentHumidityMax(), 0.01f);
        
        _Logger.info("✓ COLD_CRASH profile set successfully");
        _Logger.info("  Temp: [" + this.profileMgr.getCurrentTempMin() + 
                    " - " + this.profileMgr.getCurrentTempMax() + "]°F");
        _Logger.info("  Humidity: [" + this.profileMgr.getCurrentHumidityMin() + 
                    " - " + this.profileMgr.getCurrentHumidityMax() + "]%");
        
        _Logger.info("\n✓✓✓ Test PASSED: COLD_CRASH profile working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 6: Invalid Profile Name
    // ========================================================================
    
    @Test
    public void testInvalidProfileName() {
        _Logger.info("\n===== Test 6: Invalid Profile Name =====\n");
        
        // Store current profile
        String originalProfile = this.profileMgr.getCurrentProfile();
        float originalTempMin = this.profileMgr.getCurrentTempMin();
        
        // Try to set invalid profile
        boolean result = this.profileMgr.setProfile("INVALID_PROFILE");
        assertFalse("setProfile should return false for invalid profile", result);
        
        // Verify profile unchanged
        assertEquals("Profile should remain unchanged", 
                    originalProfile, 
                    this.profileMgr.getCurrentProfile());
        assertEquals("Thresholds should remain unchanged", 
                    originalTempMin, 
                    this.profileMgr.getCurrentTempMin(), 0.01f);
        
        _Logger.info("✓ Invalid profile rejected correctly");
        _Logger.info("✓ Original profile maintained: " + originalProfile);
        
        _Logger.info("\n✓✓✓ Test PASSED: Invalid profile handled ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 7: Null Profile Name
    // ========================================================================
    
    @Test
    public void testNullProfileName() {
        _Logger.info("\n===== Test 7: Null Profile Name =====\n");
        
        // Store current profile
        String originalProfile = this.profileMgr.getCurrentProfile();
        
        // Try to set null profile
        boolean result = this.profileMgr.setProfile(null);
        assertFalse("setProfile should return false for null profile", result);
        
        // Verify profile unchanged
        assertEquals("Profile should remain unchanged", 
                    originalProfile, 
                    this.profileMgr.getCurrentProfile());
        
        _Logger.info("✓ Null profile rejected correctly");
        _Logger.info("✓ Original profile maintained: " + originalProfile);
        
        _Logger.info("\n✓✓✓ Test PASSED: Null profile handled ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 8: Empty Profile Name
    // ========================================================================
    
    @Test
    public void testEmptyProfileName() {
        _Logger.info("\n===== Test 8: Empty Profile Name =====\n");
        
        // Store current profile
        String originalProfile = this.profileMgr.getCurrentProfile();
        
        // Try to set empty profile
        boolean result = this.profileMgr.setProfile("");
        assertFalse("setProfile should return false for empty profile", result);
        
        // Verify profile unchanged
        assertEquals("Profile should remain unchanged", 
                    originalProfile, 
                    this.profileMgr.getCurrentProfile());
        
        _Logger.info("✓ Empty profile rejected correctly");
        _Logger.info("✓ Original profile maintained: " + originalProfile);
        
        _Logger.info("\n✓✓✓ Test PASSED: Empty profile handled ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 9: Profile Change Command Generation
    // ========================================================================
    
    @Test
    public void testProfileChangeCommandGeneration() {
        _Logger.info("\n===== Test 9: Profile Change Command Generation =====\n");
        
        // Generate profile change command for LAGER
        ActuatorData profileCmd = this.profileMgr.generateProfileChangeCommand(
            ConfigConst.FERMENTATION_PROFILE_LAGER);
        
        assertNotNull("Profile command should not be null", profileCmd);
        
        // FIXED: Use getTypeID() instead of getActuatorType()
        assertEquals("Actuator type should be profile type", 
                    ConfigConst.FERMENTATION_PROFILE_ACTUATOR_TYPE, 
                    profileCmd.getTypeID());
        
        // FIXED: Use getStateData() instead of getCommand()
        assertEquals("State data should be LAGER", 
                    ConfigConst.FERMENTATION_PROFILE_LAGER, 
                    profileCmd.getStateData());
        
        assertNotNull("Name should be set", profileCmd.getName());
        
        _Logger.info("✓ Profile change command generated successfully");
        _Logger.info("  Actuator Type: " + profileCmd.getTypeID());
        _Logger.info("  State Data: " + profileCmd.getStateData());
        _Logger.info("  Name: " + profileCmd.getName());
        
        _Logger.info("\n✓✓✓ Test PASSED: Command generation working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 10: Multiple Profile Transitions
    // ========================================================================
    
    @Test
    public void testMultipleProfileTransitions() {
        _Logger.info("\n===== Test 10: Multiple Profile Transitions =====\n");
        
        String[] profiles = {
            ConfigConst.FERMENTATION_PROFILE_ALE,
            ConfigConst.FERMENTATION_PROFILE_LAGER,
            ConfigConst.FERMENTATION_PROFILE_CONDITIONING,
            ConfigConst.FERMENTATION_PROFILE_COLD_CRASH,
            ConfigConst.FERMENTATION_PROFILE_ALE  // Back to ALE
        };
        
        for (String profile : profiles) {
            _Logger.info("\nTransitioning to: " + profile);
            
            boolean result = this.profileMgr.setProfile(profile);
            assertTrue("Profile transition should succeed", result);
            
            assertEquals("Current profile should match", 
                        profile, 
                        this.profileMgr.getCurrentProfile());
            
            _Logger.info("✓ Profile: " + this.profileMgr.getCurrentProfile());
            _Logger.info("  Temp: [" + this.profileMgr.getCurrentTempMin() + 
                        " - " + this.profileMgr.getCurrentTempMax() + "]°F");
            _Logger.info("  Humidity: [" + this.profileMgr.getCurrentHumidityMin() + 
                        " - " + this.profileMgr.getCurrentHumidityMax() + "]%");
        }
        
        _Logger.info("\n✓ All " + profiles.length + " profile transitions successful");
        
        _Logger.info("\n✓✓✓ Test PASSED: Multiple transitions working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 11: Case Insensitive Profile Names
    // ========================================================================
    
    @Test
    public void testCaseInsensitiveProfileNames() {
        _Logger.info("\n===== Test 11: Case Insensitive Profile Names =====\n");
        
        // Test lowercase
        boolean result1 = this.profileMgr.setProfile("lager");
        assertTrue("Lowercase profile name should work", result1);
        assertEquals(ConfigConst.FERMENTATION_PROFILE_LAGER, 
                    this.profileMgr.getCurrentProfile());
        _Logger.info("✓ Lowercase 'lager' accepted");
        
        // Test mixed case
        boolean result2 = this.profileMgr.setProfile("CoNdItIoNiNg");
        assertTrue("Mixed case profile name should work", result2);
        assertEquals(ConfigConst.FERMENTATION_PROFILE_CONDITIONING, 
                    this.profileMgr.getCurrentProfile());
        _Logger.info("✓ Mixed case 'CoNdItIoNiNg' accepted");
        
        // Test uppercase (should already work)
        boolean result3 = this.profileMgr.setProfile("COLD_CRASH");
        assertTrue("Uppercase profile name should work", result3);
        assertEquals(ConfigConst.FERMENTATION_PROFILE_COLD_CRASH, 
                    this.profileMgr.getCurrentProfile());
        _Logger.info("✓ Uppercase 'COLD_CRASH' accepted");
        
        _Logger.info("\n✓✓✓ Test PASSED: Case insensitive handling working ✓✓✓\n");
    }
    
    // ========================================================================
    // Test 12: Threshold Value Ranges
    // ========================================================================
    
    @Test
    public void testThresholdValueRanges() {
        _Logger.info("\n===== Test 12: Threshold Value Ranges =====\n");
        
        // Verify all profiles have valid threshold ranges
        String[] profiles = {
            ConfigConst.FERMENTATION_PROFILE_ALE,
            ConfigConst.FERMENTATION_PROFILE_LAGER,
            ConfigConst.FERMENTATION_PROFILE_CONDITIONING,
            ConfigConst.FERMENTATION_PROFILE_COLD_CRASH
        };
        
        for (String profile : profiles) {
            this.profileMgr.setProfile(profile);
            
            float tempMin = this.profileMgr.getCurrentTempMin();
            float tempMax = this.profileMgr.getCurrentTempMax();
            float humidMin = this.profileMgr.getCurrentHumidityMin();
            float humidMax = this.profileMgr.getCurrentHumidityMax();
            
            // Verify min < max for both temperature and humidity
            assertTrue("Temp min should be less than temp max for " + profile, 
                      tempMin < tempMax);
            assertTrue("Humidity min should be less than humidity max for " + profile, 
                      humidMin < humidMax);
            
            // Verify reasonable ranges
            assertTrue("Temp min should be > 32°F (freezing) for " + profile, 
                      tempMin > 32.0f);
            assertTrue("Temp max should be < 100°F for " + profile, 
                      tempMax < 100.0f);
            assertTrue("Humidity min should be > 0% for " + profile, 
                      humidMin > 0.0f);
            assertTrue("Humidity max should be < 100% for " + profile, 
                      humidMax < 100.0f);
            
            _Logger.info("✓ " + profile + " ranges valid:");
            _Logger.info("  Temp: [" + tempMin + " - " + tempMax + "]°F");
            _Logger.info("  Humidity: [" + humidMin + " - " + humidMax + "]%");
        }
        
        _Logger.info("\n✓✓✓ Test PASSED: All threshold ranges valid ✓✓✓\n");
    }
}
package de.danberg.wachwerk;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UpdateManagerTest {
    @Test public void comparesWorkflowReleaseVersionsNumerically() {
        assertEquals(1, UpdateManager.compareVersions("1.15.120.1", "1.15.99.2"));
        assertEquals(0, UpdateManager.compareVersions("v1.15.7", "1.15.7"));
        assertEquals(-1, UpdateManager.compareVersions("1.14.9", "1.15.0"));
    }
}

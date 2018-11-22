package com.github.sbaudoin.sonar.plugins.shellcheck.settings;

import junit.framework.TestCase;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;

public class ShellCheckSettingsTest extends TestCase {
    public void testGetProperties() {
        List<PropertyDefinition> defs = ShellCheckSettings.getProperties();

        assertEquals(2, defs.size());
        assertEquals(ShellCheckSettings.FILE_SUFFIXES_KEY, defs.get(0).key());
        assertEquals(ShellCheckSettings.FILE_SUFFIXES_DEFAULT_VALUE, defs.get(0).defaultValue());
        assertEquals(ShellCheckSettings.SHELLCHECK_PATH_KEY, defs.get(1).key());
        assertEquals(ShellCheckSettings.SHELLCHECK_PATH_DEFAULT_VALUE, defs.get(1).defaultValue());
    }
}

package com.github.sbaudoin.sonar.plugins.shellcheck.languages;

import com.github.sbaudoin.sonar.plugins.shellcheck.checks.CheckRepository;
import junit.framework.TestCase;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import java.io.File;

public class ShellQualityProfileTest extends TestCase {
    public void testDefine() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        ShellQualityProfile qp = new ShellQualityProfile();
        qp.define(context);
        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("shell", "ShellCheck");
        assertNotNull(profile);
        assertTrue(profile.isDefault());
        assertEquals(1, profile.rules().size());
        assertEquals("rule1", profile.rules().get(0).ruleKey());
    }

    public void testProfileNotSetAsDefault() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        // Register a default profile first
        DummyProfile qp1 = new DummyProfile();
        qp1.define(context);

        ShellQualityProfile qp2 = new ShellQualityProfile();
        qp2.define(context);

        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("shell", "ShellCheck");
        assertNotNull(profile);
        assertFalse(profile.isDefault());
    }


    private class DummyProfile implements BuiltInQualityProfilesDefinition {
        @Override
        public void define(Context context) {
            NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Sonar Way", ShellLanguage.KEY);
            profile.setDefault(true);
            profile.done();
        }
    }
}

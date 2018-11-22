package com.github.sbaudoin.sonar.plugins.shellcheck;

import junit.framework.TestCase;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

public class ShellCheckPluginTest extends TestCase {
    public void testExtensionCounts() {
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }
}

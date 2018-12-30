package com.github.sbaudoin.sonar.plugins.shellcheck;

import junit.framework.TestCase;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

public class ShellCheckPluginTest extends TestCase {
    public void testExtensionCounts1() {
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    public void testExtensionCounts2() {
        System.setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_PROPERTY, "false");
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(5, context.getExtensions().size());
    }

    public void testExtensionCounts3() {
        System.setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_PROPERTY, "True");
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    public void testExtensionCounts4() {
        System.setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_PROPERTY, "something");
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(5, context.getExtensions().size());
    }
}

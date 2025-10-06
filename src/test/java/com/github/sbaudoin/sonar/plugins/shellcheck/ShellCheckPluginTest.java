/*
 * Copyright (c) 2018-2025, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.shellcheck;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.PluginContextImpl;

import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;

public class ShellCheckPluginTest {
    @Rule
    public final EnvironmentVariablesRule environmentVariables = new EnvironmentVariablesRule();


    @Test
    public void testExtensionCounts1() {
        Plugin.Context context = getContext(null);
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts2() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "false");
        Plugin.Context context = getContext(null);
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts3() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "True");
        Plugin.Context context = getContext(null);
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts4() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "something");
        Plugin.Context context = getContext(null);
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts5() {
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "false");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts6() {
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "True");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts7() {
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "something");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts10() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "True");
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "false");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts11() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "True");
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "true");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts12() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "false");
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "false");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts13() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "false");
        MapSettings settings = new MapSettings().setProperty(ShellCheckPlugin.ADD_SHELL_LANGUAGE_CONF_PROP, "true");
        Plugin.Context context = getContext(settings);
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }


    private Plugin.Context getContext(MapSettings settings) {
        SonarRuntime runtime = mock(SonarRuntime.class);
        PluginContextImpl.Builder contextBuilder = new PluginContextImpl.Builder();
        contextBuilder.setSonarRuntime(runtime);
        if (settings != null) {
            contextBuilder.setBootConfiguration(settings.asConfig());
        }
        return contextBuilder.build();
    }
}

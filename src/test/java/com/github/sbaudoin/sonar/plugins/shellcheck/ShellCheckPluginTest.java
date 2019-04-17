/**
 * Copyright (c) 2018-2019, Sylvain Baudoin
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.junit.Assert.assertEquals;

public class ShellCheckPluginTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();


    @Test
    public void testExtensionCounts1() {
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts2() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "false");
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts3() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "True");
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(7, context.getExtensions().size());
    }

    @Test
    public void testExtensionCounts4() {
        environmentVariables.set(ShellCheckPlugin.ADD_SHELL_LANGUAGE_ENV_VAR, "something");
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new ShellCheckPlugin().define(context);
        assertEquals(6, context.getExtensions().size());
    }
}

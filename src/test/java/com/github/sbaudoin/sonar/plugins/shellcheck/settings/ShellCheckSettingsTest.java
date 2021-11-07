/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.shellcheck.settings;

import junit.framework.TestCase;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;

public class ShellCheckSettingsTest extends TestCase {
    public void testGetProperties() {
        List<PropertyDefinition> defs = ShellCheckSettings.getProperties();

        assertEquals(3, defs.size());
        assertEquals(ShellCheckSettings.FILE_SUFFIXES_KEY, defs.get(0).key());
        assertEquals(ShellCheckSettings.FILE_SUFFIXES_DEFAULT_VALUE, defs.get(0).defaultValue());
        assertEquals(ShellCheckSettings.SHELLCHECK_PATH_KEY, defs.get(1).key());
        assertEquals(ShellCheckSettings.SHELLCHECK_PATH_DEFAULT_VALUE, defs.get(1).defaultValue());
        assertEquals(ShellCheckSettings.SKIP_KEY, defs.get(2).key());
        assertEquals(ShellCheckSettings.SKIP_DEFAULT_VALUE, defs.get(2).defaultValue());
    }
}

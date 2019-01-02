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
package com.github.sbaudoin.sonar.plugins.shellcheck.languages;

import com.github.sbaudoin.sonar.plugins.shellcheck.settings.ShellCheckSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.resources.AbstractLanguage;

public class ShellLanguageTest {
    private MapSettings settings;
    private ShellLanguage shell;


    @Before
    public void setUp() {
        settings = new MapSettings();
        shell = new ShellLanguage(settings.asConfig());
    }

    @Test
    public void defaultSuffixes() {
        settings.setProperty(ShellCheckSettings.FILE_SUFFIXES_KEY, "");
        Assert.assertArrayEquals(new String[] { ".sh", ".ksh" }, shell.getFileSuffixes());
    }

    @Test
    public void customSuffixes() {
        settings.setProperty(ShellCheckSettings.FILE_SUFFIXES_KEY, ".myShell, ");
        Assert.assertArrayEquals(new String[] { ".myShell" }, shell.getFileSuffixes());
    }

    @Test
    public void testEquals() {
        Assert.assertFalse(shell.equals("foo"));
        Assert.assertTrue(shell.equals(shell));
        Assert.assertTrue(shell.equals(new ShellLanguage(settings.asConfig())));
        Assert.assertTrue(shell.equals(new FakeLanguage()));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals("shell".hashCode(), shell.hashCode());
    }


    private class FakeLanguage extends AbstractLanguage {
        public FakeLanguage() {
            super("shell");
        }

        @Override
        public String[] getFileSuffixes() {
            return new String[0];
        }
    }
}

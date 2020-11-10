/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

import org.junit.Assert;
import org.junit.Test;

public class HeredocSharedImplTest {
    @Test
    public void testStartOffset() throws Exception {
        Assert.assertEquals(0, HeredocSharedImpl.startMarkerTextOffset("$", false));
        Assert.assertEquals(0, HeredocSharedImpl.startMarkerTextOffset("$ABC", false));

        Assert.assertEquals(1, HeredocSharedImpl.startMarkerTextOffset("\"ABC\"", false));
        Assert.assertEquals(2, HeredocSharedImpl.startMarkerTextOffset("$\"ABC\"", false));
    }

    @Test
    public void testStartOffsetTabs() throws Exception {
        Assert.assertEquals(0, HeredocSharedImpl.startMarkerTextOffset("$", true));
        Assert.assertEquals(2, HeredocSharedImpl.startMarkerTextOffset("\t\t$", true));
        Assert.assertEquals(3, HeredocSharedImpl.startMarkerTextOffset("\t\t\t$ABC", true));

        Assert.assertEquals(0, HeredocSharedImpl.startMarkerTextOffset("\t\"ABC\"", false));
        Assert.assertEquals(0, HeredocSharedImpl.startMarkerTextOffset("\t$\"ABC\"", false));
    }

    @Test
    public void testWrapMarker() throws Exception {
        Assert.assertEquals("EOF_NEW", HeredocSharedImpl.wrapMarker("EOF_NEW", "EOF"));
        Assert.assertEquals("\"EOF_NEW\"", HeredocSharedImpl.wrapMarker("EOF_NEW", "\"EOF\""));
        Assert.assertEquals("\'EOF_NEW\'", HeredocSharedImpl.wrapMarker("EOF_NEW", "\'EOF\'"));
        Assert.assertEquals("\\EOF_NEW", HeredocSharedImpl.wrapMarker("EOF_NEW", "\\EOF"));

        Assert.assertEquals("$\"EOF_NEW\"", HeredocSharedImpl.wrapMarker("EOF_NEW", "$\"EOF\""));
        Assert.assertEquals("$\'EOF_NEW\'", HeredocSharedImpl.wrapMarker("EOF_NEW", "$\'EOF\'"));
    }

    @Test
    public void testIssue331() throws Exception {
        Assert.assertEquals(0, HeredocSharedImpl.startMarkerTextOffset("\t\t", false));

        Assert.assertEquals(1, HeredocSharedImpl.startMarkerTextOffset("\t\t", true));
    }
}

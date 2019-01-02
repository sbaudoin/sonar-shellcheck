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
package com.github.sbaudoin.sonar.plugins.shellcheck.measures;

import com.github.sbaudoin.sonar.plugins.shellcheck.Utils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class LineCounterTest {
    private FileLinesContextFactory fileLinesContextFactory;
    private FileLinesContext fileLinesContext;

    @Rule
    public LogTester logTester = new LogTester();

    @Before
    public void init() {
        fileLinesContextFactory = mock(FileLinesContextFactory.class);
        fileLinesContext = mock(FileLinesContext.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    }

    @Test
    public void testNormal() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        String filePath = "test4.sh";
        LineCounter.analyse(context, fileLinesContextFactory, Utils.getInputFile(filePath));
        assertEquals(new Integer(13), context.measure(getComponentKey(filePath), CoreMetrics.NCLOC).value());
        assertEquals(new Integer(2), context.measure(getComponentKey(filePath), CoreMetrics.COMMENT_LINES).value());
    }

    @Test
    public void testIOException() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        InputFile inputFile = Utils.getInputFile("test1.sh");
        InputFile spy = spy(inputFile);
        when(spy.contents()).thenThrow(new IOException("Cannot read file"));

        LineCounter.analyse(context, fileLinesContextFactory, spy);
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertEquals("Unable to count lines for file " + inputFile.filename() + ", ignoring measures", logTester.logs(LoggerLevel.WARN).get(0));
    }

    private String getComponentKey(String filePath) {
        return Utils.MODULE_KEY + ":src/test/resources/" + filePath;
    }
}

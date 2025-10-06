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
package com.github.sbaudoin.sonar.plugins.shellcheck.measures;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.testfixtures.log.LogTester;

import com.github.sbaudoin.sonar.plugins.shellcheck.Utils;

public class LineCounterTest {
    private FileLinesContextFactory fileLinesContextFactory;
    private FileLinesContext fileLinesContext;

    @Rule
    public LogTester logTester = new LogTester();

    @Before
    public void init() {
        fileLinesContextFactory = mock(FileLinesContextFactory.class);
        fileLinesContext = new MyFileLinesContext();
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    }

    @Test
    public void testNormal() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        String filePath = "test4.sh";
        LineCounter.analyse(context, fileLinesContextFactory, Utils.getInputFile(filePath));
        // Check the number 4 line in script containing code. be careful +1 is smowhere
        assertEquals(Integer.valueOf(1), ((MyFileLinesContext)fileLinesContext).getIntValue(CoreMetrics.NCLOC_DATA_KEY, 5));
        // Check the number 4 line in script containing comment. be careful +1 is smowhere
        assertEquals(Integer.valueOf(0), ((MyFileLinesContext)fileLinesContext).getIntValue(CoreMetrics.NCLOC_DATA_KEY, 1));
        assertEquals(Integer.valueOf(13), context.measure(getComponentKey(filePath), CoreMetrics.NCLOC).value());
        assertEquals(Integer.valueOf(2), context.measure(getComponentKey(filePath), CoreMetrics.COMMENT_LINES).value());
    }

    @Test
    public void testIOException() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        InputFile inputFile = Utils.getInputFile("test1.sh");
        InputFile spy = Mockito.spy(inputFile);
        when(spy.contents()).thenThrow(new IOException("Cannot read file"));

        LineCounter.analyse(context, fileLinesContextFactory, spy);
        assertEquals(1, logTester.logs(Level.WARN).size());
        assertEquals("Unable to count lines for file " + inputFile.filename() + ", ignoring measures", logTester.logs(Level.WARN).get(0));
    }

    private String getComponentKey(String filePath) {
        return Utils.MODULE_KEY + ":src/test/resources/" + filePath;
    }


    private class MyFileLinesContext implements FileLinesContext {
        Map<String, Map<Integer, Integer>> intValues = new HashMap<>();


        @Override
        public void setIntValue(String metricKey, int line, int value) {
            if (intValues.containsKey(metricKey)) {
                intValues.get(metricKey).put(line, value);
            } else {
                Map<Integer, Integer> values = new HashMap<>();
                values.put(line, value);
                intValues.put(metricKey, values);
            }
        }

        public Integer getIntValue(String metricKey, int line) {
            if (intValues.containsKey(metricKey)) {
                return intValues.get(metricKey).getOrDefault(line, -1);
            } else {
                return -1;
            }
        }

        @Override
        public void setStringValue(String metricKey, int line, String value) {

        }

        @Override
        public void save() {

        }
    }
}

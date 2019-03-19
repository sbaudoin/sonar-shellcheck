package com.github.sbaudoin.sonar.plugins.shellcheck.rules;

import com.github.sbaudoin.sonar.plugins.shellcheck.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class ShellCheckSensorNoRuleTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public LogTester logTester = new LogTester();


    @Test
    public void testNoActiveRule() throws IOException {
        SensorContextTester context = Utils.getSensorContext();

        DefaultFileSystem fs = Utils.getFileSystem();
        fs.setWorkDir(temporaryFolder.newFolder("temp").toPath());
        context.setFileSystem(fs);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        InputFile script1 = Utils.getInputFile("test1.sh");
        InputFile script2 = Utils.getInputFile("test2.sh");
        InputFile script3 = Utils.getInputFile("test3.sh");
        context.fileSystem().add(script1).add(script2).add(script3);

        ShellCheckSensor sensor = new ShellCheckSensor(fs, fileLinesContextFactory);

        sensor.execute(context);
        assertEquals(1, logTester.logs(LoggerLevel.INFO).size());
        assertEquals("No active rules found for this plugin, skipping.", logTester.logs(LoggerLevel.INFO).get(0));
        assertEquals(0, context.allIssues().size());
    }
}

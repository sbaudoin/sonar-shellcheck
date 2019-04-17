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
package com.github.sbaudoin.sonar.plugins.shellcheck.rules;

import com.github.sbaudoin.sonar.plugins.shellcheck.Utils;
import com.github.sbaudoin.sonar.plugins.shellcheck.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import com.github.sbaudoin.sonar.plugins.shellcheck.settings.ShellCheckSettings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import static com.github.sbaudoin.sonar.plugins.shellcheck.Utils.issueExists;
import static com.github.sbaudoin.sonar.plugins.shellcheck.Utils.setShellRights;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.doThrow;

public class ShellCheckSensorTest {
    private static final String RULE_ID1 = "SC2037";
    private static final String RULE_ID2 = "SC2086";
    private final RuleKey ruleKey1 = RuleKey.of(CheckRepository.REPOSITORY_KEY, RULE_ID1);
    private final RuleKey ruleKey2 = RuleKey.of(CheckRepository.REPOSITORY_KEY, RULE_ID2);
    private SensorContextTester context;
    private ShellCheckSensor sensor;


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public LogTester logTester = new LogTester();

    @Before
    public void init() throws Exception {
        context = Utils.getSensorContext();

        DefaultFileSystem fs = Utils.getFileSystem();
        fs.setWorkDir(temporaryFolder.newFolder("temp").toPath());
        context.setFileSystem(fs);

        ActiveRules activeRules = new ActiveRulesBuilder()
                .create(ruleKey1)
                .activate()
                .create(ruleKey2)
                .activate()
                .build();
        context.setActiveRules(activeRules);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        sensor = new ShellCheckSensor(fs, fileLinesContextFactory);
    }

    @Test
    public void testDescribe() {
        DummySensorDescriptor descriptor = new DummySensorDescriptor();
        sensor.describe(descriptor);

        assertEquals("ShellCheck Sensor", descriptor.sensorName);
        assertEquals(ShellLanguage.KEY, descriptor.languageKey);
    }

    @Test
    public void testWithShellCheckEmptyOutput() throws IOException {
        InputFile script1 = Utils.getInputFile("test1.sh");
        InputFile script2 = Utils.getInputFile("test2.sh");
        InputFile script3 = Utils.getInputFile("test3.sh");
        context.fileSystem().add(script1).add(script2).add(script3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src\\test\\resources\\scripts\\shellcheck1.cmd");
        } else {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src/test/resources/scripts/shellcheck1.sh");
            setShellRights("src/test/resources/scripts/shellcheck1.sh");
        }

        sensor.execute(context);
        assertEquals(0, context.allIssues().size());
    }

    @Test
    public void testExecuteWithShellCheckErrorOutput() throws IOException {
        InputFile script1 = Utils.getInputFile("test1.sh");
        InputFile script2 = Utils.getInputFile("test2.sh");
        InputFile script3 = Utils.getInputFile("test3.sh");
        context.fileSystem().add(script1).add(script2).add(script3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src\\test\\resources\\scripts\\shellcheck2.cmd");
        } else {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src/test/resources/scripts/shellcheck2.sh");
            setShellRights("src/test/resources/scripts/shellcheck2.sh");
        }

        sensor.execute(context);

        assertEquals(3, logTester.logs(LoggerLevel.WARN).size());
        logTester.logs(LoggerLevel.WARN).stream().forEach(log -> assertTrue(log.startsWith("Errors happened during analysis:")));
    }

    @Test
    public void testExecuteWithShellCheckLongStdOutput() throws IOException {
        InputFile script1 = Utils.getInputFile("test1.sh");
        InputFile script2 = Utils.getInputFile("test2.sh");
        InputFile script3 = Utils.getInputFile("test3.sh");
        context.fileSystem().add(script1).add(script2).add(script3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src\\test\\resources\\scripts\\shellcheck3.cmd");
        } else {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src/test/resources/scripts/shellcheck3.sh");
            setShellRights("src/test/resources/scripts/shellcheck3.sh");
        }

        try {
            sensor.execute(context);
            fail("Invalid command output accepted");
        } catch (RuntimeException e) {
            assertEquals("Cannot scan shellcheck output: 2 lines returned by shellcheck whereas only one is expected", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithIOException() throws IOException, InterruptedException {
        InputFile script1 = Utils.getInputFile("test1.sh");
        InputFile script2 = Utils.getInputFile("test2.sh");
        InputFile script3 = Utils.getInputFile("test3.sh");
        context.fileSystem().add(script1).add(script2).add(script3);

        ShellCheckSensor theSensor = spy(sensor);
        doThrow(new IOException("Boom!")).when(theSensor).executeCommand(any(), any(), any());

        theSensor.execute(context);
        Collection<Issue> issues = context.allIssues();
        assertEquals(0, issues.size());
    }

    @Test
    public void testExecuteWithShellCheckLongOutput() throws IOException {
        InputFile script1 = Utils.getInputFile("test1.sh");
        context.fileSystem().add(script1);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src\\test\\resources\\scripts\\shellcheck4.cmd");
        } else {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src/test/resources/scripts/shellcheck4.sh");
            setShellRights("src/test/resources/scripts/shellcheck4.sh");
        }

        sensor.execute(context);

        Collection<Issue> issues = context.allIssues();
        assertEquals(3, issues.size());
        assertTrue(issueExists(issues, ruleKey1, script1, 3, "To assign the output of a command, use var=\\$\\(cmd\\) \\."));
        assertTrue(issueExists(issues, ruleKey2, script1, 5, "Double quote to prevent globbing and word splitting."));
        assertTrue(issueExists(issues, ruleKey2, script1, 6, "Double quote to prevent globbing and word splitting."));
    }

    @Test
    public void testGetShellCheckPath() {
        assertEquals("shellcheck", sensor.getShellCheckPath(context));
        context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "/usr/bin/shellcheck");
        assertEquals("/usr/bin/shellcheck", sensor.getShellCheckPath(context));
    }

    @Test
    public void testExecuteCommand() {
        ArrayList<String> stdOut = new ArrayList();
        ArrayList<String> stdErr = new ArrayList();

        try {
            sensor.executeCommand(Arrays.asList("invalid-command", "bar"), stdOut, stdErr);
            fail("Invalid/unknown command executed");
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
        }

        try {
            stdOut.clear();
            stdErr.clear();
            ArrayList<String> command = new ArrayList<>();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                command.add("src\\test\\resources\\scripts\\echo.cmd");
            } else {
                command.add("src/test/resources/scripts/echo.sh");
                setShellRights("src/test/resources/scripts/echo.sh");
            }
            command.add("foo");
            assertEquals(0, sensor.executeCommand(command, stdOut, stdErr));
            assertEquals(1, stdOut.size());
            assertEquals("foo", stdOut.get(0));
            assertEquals(0, stdErr.size());

            stdOut.clear();
            stdErr.clear();
            assertEquals(0, sensor.executeCommand(Arrays.asList("java", "-version"), stdOut, stdErr));
            assertEquals(0, stdOut.size());
            assertNotEquals(0, stdErr.size());
        } catch (Exception e) {
            fail("Command not executed: " + e.getMessage());
        }
    }

    @Test
    public void testSaveIssues() throws IOException {
        InputFile shellScript = Utils.getInputFile("test1.sh");
        String json = "[" +
                "{\"file\":\"" + shellScript.key() + "\",\"line\":3,\"column\":1,\"level\":\"warning\",\"code\":2037,\"message\":\"To assign the output of a command, use var=$(cmd) .\"}," +
                "{\"file\":\"" + shellScript.key() + "\",\"line\":5,\"column\":6,\"level\":\"info\",\"code\":2086,\"message\":\"Double quote to prevent globbing and word splitting.\"}," +
                "{\"file\":\"" + shellScript.key() + "\",\"line\":6,\"column\":6,\"level\":\"info\",\"code\":2086,\"message\":\"Double quote to prevent globbing and word splitting.\"}]";

        sensor.saveIssues(shellScript, json, context);

        Collection<Issue> issues = context.allIssues();
        assertEquals(3, issues.size());
        assertTrue(issueExists(issues, ruleKey1, shellScript, 3, "To assign the output of a command, use var=\\$\\(cmd\\) \\."));
        assertTrue(issueExists(issues, ruleKey2, shellScript, 5, "Double quote to prevent globbing and word splitting."));
        assertTrue(issueExists(issues, ruleKey2, shellScript, 6, "Double quote to prevent globbing and word splitting."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveIssuesInvalidOutput() throws IOException {
        InputFile shellScript = Utils.getInputFile("test1.sh");
        String json = "this is an invalid JSON";

        sensor.saveIssues(shellScript, json, context);
    }

    @Test
    public void testSaveIssue() throws IOException {
        InputFile shellScript = Utils.getInputFile("test1.sh");

        // Try to save issue for an unknown rule
        logTester.clear();
        sensor.saveIssue(context, shellScript, 2, "foo", "An error here");
        assertTrue(logTester.logs(LoggerLevel.DEBUG).contains("Rule foo ignored, not found in repository"));

        // Save issue for a known rule
        logTester.clear();
        sensor.saveIssue(context, shellScript, 2, RULE_ID1, "An error here");
        assertEquals(1, context.allIssues().size());
        Issue issue = (Issue)context.allIssues().toArray()[0];
        assertEquals(ruleKey1, issue.ruleKey());
        IssueLocation location = issue.primaryLocation();
        assertEquals(shellScript.key(), location.inputComponent().key());
        assertEquals(2, location.textRange().start().line());
        assertEquals("An error here", location.message());
    }

    @Test
    public void testGetRuleKey() {
        assertNull(sensor.getRuleKey(context, "foo"));
        assertEquals(RuleKey.of(CheckRepository.REPOSITORY_KEY, RULE_ID1), sensor.getRuleKey(context, RULE_ID1));
    }

    @Test
    public void testSkip() throws IOException {
        context.settings().appendProperty(ShellCheckSettings.SKIP_KEY, "true");

        InputFile script1 = Utils.getInputFile("test1.sh");
        context.fileSystem().add(script1);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src\\test\\resources\\scripts\\shellcheck4.cmd");
        } else {
            context.settings().appendProperty(ShellCheckSettings.SHELLCHECK_PATH_KEY, "src/test/resources/scripts/shellcheck4.sh");
            setShellRights("src/test/resources/scripts/shellcheck4.sh");
        }

        sensor.execute(context);

        Collection<Issue> issues = context.allIssues();
        assertEquals(0, issues.size());
        assertEquals(1, logTester.logs(LoggerLevel.INFO).size());
        assertEquals("Plugin disabled by configuration for this project, skipping.", logTester.logs(LoggerLevel.INFO).get(0));
    }


    private class DummySensorDescriptor implements SensorDescriptor {
        private String sensorName;
        private String languageKey;

        @Override
        public SensorDescriptor name(String sensorName) {
            this.sensorName = sensorName;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguage(String languageKey) {
            this.languageKey = languageKey;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguages(String... languageKeys) {
            return this;
        }

        @Override
        public SensorDescriptor onlyOnFileType(InputFile.Type type) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepository(String... repositoryKey) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepositories(String... repositoryKeys) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperty(String... propertyKey) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperties(String... propertyKeys) {
            return this;
        }

        @Override
        public SensorDescriptor global() {
            return this;
        }

        @Override
        public SensorDescriptor onlyWhenConfiguration(Predicate<Configuration> predicate) {
            return this;
        }
    }
}

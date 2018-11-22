package com.github.sbaudoin.sonar.plugins.shellcheck.rules;

import com.github.sbaudoin.sonar.plugins.shellcheck.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import com.github.sbaudoin.sonar.plugins.shellcheck.settings.ShellCheckSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.*;
import java.util.*;

public class ShellCheckSensor implements Sensor {
    private static final Logger LOGGER = Loggers.get(ShellCheckSensor.class);

    /**
     * The underlying file system that will give access to the files to be analyzed
     */
    private FileSystem fileSystem;

    /**
     * File predicate to filter and select the files to be analyzed with this sensor
     */
    private FilePredicate mainFilesPredicate;


    public ShellCheckSensor(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.mainFilesPredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(ShellLanguage.KEY));
    }


    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(ShellLanguage.KEY);
        descriptor.name("ShellCheck Sensor");
    }

    /**
     * Executes {@code shellcheck} and saves the issues detected with this tool
     *
     * @param context the execution sensor context (taken from the method {@link #execute(SensorContext)} of the child class)
     */
    @Override
    public void execute(SensorContext context) {
        LOGGER.debug("ShellCheck sensor executed with context: " + context);

        for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
            LOGGER.debug("Analyzing file: " + inputFile.filename());

            // Build shellcheck command
            List<String> command = new ArrayList<>();
            command.addAll(Arrays.asList(getShellCheckPath(context), "-x", "-f", "json"));
            command.add(new File(inputFile.uri()).getAbsolutePath());

            // Execute shallcheck and get a parsable output
            List<String> output = new ArrayList<>();
            List<String> error = new ArrayList<>();
            try {
                executeCommand(command, output, error);
            } catch (InterruptedException| IOException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (!error.isEmpty()) {
                LOGGER.warn("Errors happened during analysis:{}{}",
                        System.getProperty("line.separator"),
                        String.join(System.getProperty("line.separator"), error)
                );
            }

            // Only one output line expected
            LOGGER.debug("Output from shellcheck:");
            output.forEach(LOGGER::debug);
            if (output.size() == 1) {
                // Save all found issues
                saveIssues(inputFile, output.get(0), context);
            } else if (output.size() > 1) {
                throw new RuntimeException("Cannot parse shellcheck output: " + output.size() + " lines returned by shellcheck whereas only one is expected");
            }
        }
    }

    /**
     * Returns the plugin configuration parameter (settings) that defines the path to the command {@code shellcheck}
     *
     * @param context the execution sensor context (taken from the method {@link #execute(SensorContext)} of the child class)
     * @return the path to the command {@code shellcheck} or {@literal shellcheck} if the plugin setting is not set
     * @see ShellCheckSettings#SHELLCHECK_PATH_KEY
     */
    protected String getShellCheckPath(SensorContext context) {
        Optional<String> path = context.config().get(ShellCheckSettings.SHELLCHECK_PATH_KEY);
        return (path.isPresent())?path.get():"shellcheck";
    }

    /**
     * Executes a system command and writes the standard and error outputs to the passed
     * <code>StringBuilder</code> if not <code>null</code>
     *
     * @param command the command to be executed
     * @param stdOut where the standard output is written to line by line
     * @param errOut where the error output is written to
     * @return the command exit code
     * @throws IOException if an error occurred executing the command. See {@link ProcessBuilder#start()} and {@link Process#waitFor()}
     * @throws InterruptedException if an error occurred executing the command. See {@link ProcessBuilder#start()}
     *                                and {@link Process#waitFor()}
     * @see ProcessBuilder#start()
     * @see Process#waitFor()
     */
    protected int executeCommand(List<String> command, List<String> stdOut, List<String> errOut) throws InterruptedException, IOException {
        assert stdOut != null;
        assert errOut != null;

        LOGGER.debug("Executing command: {}", command);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            // Read standard output
            LineInputReader stdOutputReader = new LineInputReader(p.getInputStream());
            stdOutputReader.start();
            // Wait for thread to be ready
            while (!stdOutputReader.isReady()) {
                Thread.sleep(100);
            }
            // Get error output
            LineInputReader errOutputReader = new LineInputReader(p.getErrorStream());
            errOutputReader.start();
            // Wait for thread to be ready
            while (!errOutputReader.isReady()) {
                Thread.sleep(100);
            }

            int status = p.waitFor();

            // Create standard output lines
            stdOut.addAll(stdOutputReader.getOutput());

            // Write error output if any
            errOut.addAll(errOutputReader.getOutput());

            return status;
        } catch (InterruptedException|IOException e) {
            LOGGER.error("Error executing command", e);
            throw e;
        }
    }

    /**
     * Saves in SonarQube all the issues found in the passed JSON output. If the output is not JSON compliant, an {@code IllegaArgumentException}
     * is raised.
     *
     * @param inputFile the file that has the issues to be saved
     * @param output JSON output received from shellcheck
     * @param context the execution sensor context (taken from the method {@link #execute(SensorContext)} of the child class)
     */
    protected void saveIssues(InputFile inputFile, String output, SensorContext context) {
        try {
            JSONArray issues = new JSONArray(output);
            for (int i = 0; i < issues.length(); i++) {
                JSONObject issue = issues.getJSONObject(i);
                saveIssue(context, inputFile, issue.getInt("line"), "SC" + issue.getInt("code"), issue.getString("message"));
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Passed output cannot be parsed as JSON", e);
        }
    }

    /**
     * Saves the found issues in SonarQube
     *
     * @param context the context
     * @param inputFile the file where the issue was found
     * @param line the line where the issue was found
     * @param ruleId the Id of the rule that raised the issue
     * @param message a message describing the issue
     */
    protected void saveIssue(SensorContext context, InputFile inputFile, int line, String ruleId, String message) {
        RuleKey ruleKey = getRuleKey(context, ruleId);

        if (ruleKey == null) {
            LOGGER.debug("Rule " + ruleId + " ignored, not found in repository");
            return;
        }

        NewIssue newIssue = context.newIssue().forRule(ruleKey);
        NewIssueLocation location = newIssue.newLocation()
                .on(inputFile)
                .message(message)
                .at(inputFile.selectLine(line));
        newIssue.at(location).save();
        LOGGER.debug("Issue {} saved for {}", ruleId, inputFile.filename());
    }

    /**
     * Returns the {@code RuleKey} identified as the passed {@code ruleId} or {@code null} if no corresponding active
     * rule has been found
     *
     * @param context the sensor context (that contains the active rules)
     * @param ruleId the rule Id (corresponding to the searched {@code RuleKey})
     * @return the {@code RuleKey} or {@code null} if no active rule has been found
     */
    protected RuleKey getRuleKey(SensorContext context, String ruleId) {
        RuleKey key = CheckRepository.getRuleKey(ruleId);
        return (context.activeRules().find(key) != null)?key:null;
    }


    /**
     * Reader class for {@code shellcheck} output
     */
    private class LineInputReader extends Thread {
        private List<String> output = new ArrayList<>();
        private BufferedReader input;
        private boolean ready = false;


        public LineInputReader(InputStream input) {
            this.input = new BufferedReader(new InputStreamReader(input));
        }

        @Override
        public void run() {
            try {
                String line;
                ready = true;
                while ((line = input.readLine()) != null) {
                    output.add(line);
                    LOGGER.trace("Read from input: {}", line);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot read input stream", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error("Unknown error", e);
                }
            }
        }

        public List<String> getOutput() {
            return output;
        }

        public boolean isReady() {
            return ready;
        }
    }
}

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
package com.github.sbaudoin.sonar.plugins.shellcheck.measures;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.io.Serializable;

/**
 * Class used to count the code and comment lines of a Shell script and save these "facts"
 * into SonarQube as measures
 */
public class LineCounter {
    private static final Logger LOGGER = Loggers.get(LineCounter.class);

    /**
     * Hide constructor
     */
    private LineCounter() {
    }


    /**
     * Analyzes a file and saves its line measures. This method is called by dependency injection by the Sonar runner.
     *
     * @param context the {@code SensorContext}
     * @param fileLinesContextFactory {@code FileLinesContextFactory} used to save line measures
     * @param inputFile the Shell script to be analyzed
     */
    public static void analyse(SensorContext context, FileLinesContextFactory fileLinesContextFactory, InputFile inputFile) {
        LOGGER.debug("Count lines in {}", inputFile.filename());

        try {
            saveMeasures(
                    inputFile,
                    new LineCountParser(inputFile.contents()).getLineCountData(),
                    fileLinesContextFactory.createFor(inputFile), context);
        } catch (IOException e) {
            LOGGER.warn("Unable to count lines for file " + inputFile.filename() + ", ignoring measures", e);
        }
    }

    /**
     * Saves the measures of the passed Shell script: lines of code and comments
     *
     * @param script the Shell script being analyzed
     * @param data the {@code LineCountData} describing this Shell script
     * @param fileLinesContext {@code FileLinesContext} used to save the code lines and comments of the Shell script
     * @param context the {@code SensorContext}
     */
    private static void saveMeasures(InputFile script, LineCountData data, FileLinesContext fileLinesContext, SensorContext context) {
        for (int line = 1; line <= data.linesNumber(); line++) {
            fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, data.linesOfCodeLines().contains(line) ? 1 : 0);
            if (Version.create(7, 3).isGreaterThanOrEqual(context.getSonarQubeVersion())) {
                fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, data.effectiveCommentLines().contains(line) ? 1 : 0);
            }
        }
        fileLinesContext.save();

        saveMeasure(context, script, CoreMetrics.COMMENT_LINES, data.effectiveCommentLines().size());
        saveMeasure(context, script, CoreMetrics.NCLOC, data.linesOfCodeLines().size());
    }

    /**
     * Saves the passed measure in SonarQube's database
     *
     * @param context the {@code SensorContext}, used to save the measure
     * @param inputFile the file being analyzed
     * @param metric a metric to be saved
     * @param value the metric's value
     */
    private static <T extends Serializable> void saveMeasure(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
        context.<T>newMeasure()
                .withValue(value)
                .forMetric(metric)
                .on(inputFile)
                .save();
    }
}

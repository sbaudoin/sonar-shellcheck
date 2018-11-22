package com.github.sbaudoin.sonar.plugins.shellcheck;

import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static final String MODULE_KEY = "moduleKey";

    public static final Path BASE_DIR = Paths.get("src", "test", "resources");


    private Utils() {
    }


    public static InputFile getInputFile(String relativePath) throws IOException {
        return TestInputFileBuilder.create(MODULE_KEY, BASE_DIR.resolve(relativePath).toString())
                .setModuleBaseDir(Paths.get("."))
                .setContents(new String(Files.readAllBytes(BASE_DIR.resolve(relativePath))))
                .setLanguage(ShellLanguage.KEY)
                .setCharset(StandardCharsets.UTF_8)
                .build();
    }

    public static SensorContextTester getSensorContext() {
        return SensorContextTester.create(BASE_DIR);
    }

    public static DefaultFileSystem getFileSystem() {
        return new DefaultFileSystem(BASE_DIR);
    }

    public static boolean issueExists(Collection<Issue> issues, RuleKey ruleKey, InputFile file, int line, String regex) {
        // Brut force...
        for (Issue issue : issues) {
            IssueLocation location = issue.primaryLocation();
            if (issue.ruleKey().equals(ruleKey) &&
                    file.key().equals(location.inputComponent().key()) &&
                    line == location.textRange().start().line() &&
                    location.message().matches(regex)
            ) {
                return true;
            }
        }
        return false;
    }

    public static void setShellRights(String path) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(Paths.get(path), perms);
    }
}

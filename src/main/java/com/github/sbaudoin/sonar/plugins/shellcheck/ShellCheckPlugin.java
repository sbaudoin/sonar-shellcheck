package com.github.sbaudoin.sonar.plugins.shellcheck;

import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellQualityProfile;
import com.github.sbaudoin.sonar.plugins.shellcheck.rules.ShellCheckRulesDefinition;
import com.github.sbaudoin.sonar.plugins.shellcheck.rules.ShellCheckSensor;
import com.github.sbaudoin.sonar.plugins.shellcheck.settings.ShellCheckSettings;
import org.sonar.api.Plugin;

public class ShellCheckPlugin implements Plugin {
    @Override
    public void define(Context context) {
        context.addExtension(ShellLanguage.class);
        context.addExtension(ShellQualityProfile.class);

        // Add plugin settings (file extensions, etc.)
        context.addExtensions(ShellCheckSettings.getProperties());

        // Add rules
        context.addExtensions(ShellCheckRulesDefinition.class, ShellCheckSensor.class);
    }
}

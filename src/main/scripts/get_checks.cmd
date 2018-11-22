SET PWD=%cd%
MKDIR %~dp0\..\resources\org\sonar\l10n\shellcheck\rules\shellcheck
DEL /S /Q %~dp0\..\resources\org\sonar\l10n\shellcheck\rules\shellcheck
docker run --rm -v %PWD%\..:/mnt python:3.7-stretch sh /mnt/scripts/build_checks.sh /mnt/resources/org/sonar/l10n/shellcheck/rules/shellcheck

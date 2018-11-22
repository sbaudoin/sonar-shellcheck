#!/bin/bash -e

pip install mistune PyYAML

cd /tmp
git clone https://github.com/koalaman/shellcheck.wiki.git
git clone https://github.com/koalaman/shellcheck.git

CODE_YAML=/tmp/shellcheck_codes.yml
TMP_CODE=$CODE_YAML.tmp
# Get error levels
cd shellcheck
#sed 'H;1h;$!d;g;s/\n  */ /g' src/ShellCheck/Parser.hs | grep -o ' [^ ]\+ [0-9]\{4\} ' | sed 's/C / /' | awk '{ print "SC"$2","$1 }' | tr 'EWI' 'ewi' | sed 's/ppt/error/' > $TMP_CSV
for FILE in src/ShellCheck/Parser.hs src/ShellCheck/Checks/Commands.hs src/ShellCheck/Checks/ShellSupport.hs src/ShellCheck/Analytics.hs
do
    echo "Processing $FILE..."
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' style [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": style" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' info [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": info" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' InfoC [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": info" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' warn [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": warning" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' warning [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": warning" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' WarningC [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": warning" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' err [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": error" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' ErrorC [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": error" }' >> $TMP_CODE
    sed 'H;1h;$!d;g;s/\n  */ /g' $FILE | grep -o ' ppt [^0-9]*[0-9]\{4\}' | sed 's/.*\([0-9]\{4\}\)$/\1/' | awk '{ print "SC"$1": error" }' >> $TMP_CODE
done
cat $TMP_CODE | sed 's/C / /' | tr 'EWI' 'ewi' | sed 's/ppt/error/' | sort -u > $CODE_YAML

cd $1
echo "Generating description files..."
python /mnt/scripts/build_checks.py /tmp/shellcheck.wiki $CODE_YAML

# Now checking result
echo "Checking rules..."

# Checking rules with no desc files
cat $CODE_YAML | cut -d ':' -f 1 | while read CODE
do
    if [ ! -f $CODE.html ] || [ ! -f $CODE.json ]
    then
        echo "$CODE is missing desc files"
    fi
done

# Checking desc files with no rules
for FILE in `ls *.html`
do
    CODE=`echo $FILE | sed 's/.html//'`
    # Attention aux règles dans build_checks.yml
    if ! grep -q $CODE $CODE_YAML && ! grep -q "$CODE:" /mnt/scripts/build_checks.yml
    then
        echo "Rule $CODE described but does not exist"
    fi
done

echo "Done."

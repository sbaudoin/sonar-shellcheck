#!/bin/bash

# Comment
FOO=ls /tmp

echo "foo bar"
echo 'spam, bacon and egg'
eval `$FOO`

echo << EOF
Foo
# Bar
EOF

if [ $0 -ne 0 ]
then
    echo 'Error' >&2
    exit 1
fi

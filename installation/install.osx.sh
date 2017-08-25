#!/bin/bash

# Run this file from your mac to install the nessisary software

if [ -z $1 ]
then
    echo "Usage: $0 <roborio-hostname>"
    exit -1;
fi;

ssh admin@$1 "rm -r ~/*"

FILES_TO_COPY="\
./RIOdroid.roborio.sh \
./RIOdroid.tar.gz"

scp $FILES_TO_COPY admin@$1:~/

ssh admin@$1 "./RIOdroid.roborio.sh"

echo "*** adb installed. Please reboot roborio before connecting phone. ***"

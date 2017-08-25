#!/bin/bash

# Run this file from your mac to install the necessary software for mjpg streamer
# Make sure to install the helpful scrips by the robotpy guys.
#
# pip3 install robotpy-installer

# Only needed if opkg package needs to be pulled from upstream.
#robotpy-installer download-opkg mjpg-streamer v4l-utils

robotpy-installer install-opkg mjpg-streamer v4l-utils

scp mjpg-streamer admin@roborio-254-frc.local:/etc/default/mjpg-streamer
scp mjpg-streamer-initd admin@roborio-254-frc.local:/etc/init.d/mjpg-streamer

ssh admin@roborio-254-frc.local 'v4l2-ctl -c focus_auto=0'

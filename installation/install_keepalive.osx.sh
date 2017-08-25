#!/bin/bash

# Run this file from your mac to install the keepalive script

scp keep_streamer_alive_bin admin@roborio-254-frc.local:/usr/local/bin/keep_streamer_alive
scp keep-streamer-alive-initd admin@roborio-254-frc.local:/etc/init.d/keep-streamer-alive
scp mjpg-streamer-initd admin@roborio-254-frc.local:/etc/init.d/mjpg-streamer

ssh admin@roborio-254-frc.local chmod +x \
    /usr/local/bin/keep_streamer_alive \
    /etc/init.d/keep-streamer-alive \
    /etc/init.d/mjpg-streamer

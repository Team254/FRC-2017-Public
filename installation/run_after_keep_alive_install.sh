#!/bin/sh

/sbin/ldconfig

/usr/sbin/update-rc.d -s keep_streamer_alive defaults 89

#!/bin/bash

scp admin@roborio-254-frc.local:/home/lvuser/SHOOTER-LOGS.csv . && python plot_data.py


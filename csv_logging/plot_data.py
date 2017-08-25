#!/usr/bin/env python
#
# You can install numpy and matplotlib via pip
#

import numpy as np

import matplotlib as mpl
import matplotlib.pyplot as plt
import matplotlib.cbook as cbook

data = np.genfromtxt('SHOOTER-LOGS.csv', delimiter=',', names=True)

fig = plt.figure()

outputPlot = fig.add_subplot(1, 1, 1)
outputPlot.plot(data['time'], data['flywheel_rpm'], label='flywheel_rpm')

plt.show()

import sys
import time
import logging
import json

sys.path.append("monotonic-1.3-py2.7.egg")
sys.path.append("pynetworktables-2017.0.8-py2.7.egg")
from networktables import NetworkTable

logging.basicConfig(level=logging.DEBUG)
NetworkTable.initialize()

table = NetworkTable.getTable("SmartDashboard")

def valueChanged(table, key, value, isNew):
    print("Value Changed", table.path, key, value)
table.addTableListener(valueChanged)

i = 0
while True:
    table.putNumber("server_count", i)
    table.putBoolean("have_ball", i % 2 == 0)
    if i % 5 == 0:
        table.putString(
            "auto_options",
            json.dumps([
                "some auto option",
                "another auto option",
                "crappy option",
                "4th option"]))
    table.putNumber("Air Pressure psi", i % 120)
    table.putString("color_box_color", "ff0000" if i % 2 == 1 else "ffff00")
    table.putBoolean("camera_connected", i % 2 == 0)
    i += 1
    time.sleep(1)

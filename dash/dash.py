import signal
import sys
import time
import json
import recorder
import re
import urllib
import time
import traceback

sys.path.append("monotonic-1.3-py2.7.egg")
sys.path.append("pynetworktables-2017.0.8-py2.7.egg")
from networktables import NetworkTable

sys.path.append("SimpleWebSocketServer-0.1.0-py2.7.egg")
from SimpleWebSocketServer import SimpleWebSocketServer, WebSocket

if len(sys.argv) != 2:
    print("Error: specify a Network Table IP to connect to!")
    exit(-1)

ip = sys.argv[1]
print("Connecting to ip: %s" % ip)

ensureDb = recorder.RecorderDb(True)

NetworkTable.setClientMode()
NetworkTable.setIPAddress(ip)
NetworkTable.initialize()

table = NetworkTable.getTable("SmartDashboard")

def tableConnectionListener(isConnected, connectionInfo):
    if isConnected:
        print("Connected to", connectionInfo.remote_ip)
    else:
        print("disconnectd", connectionInfo.remote_ip)

table.addConnectionListener(tableConnectionListener)

activeBridges = set()
activeCharts = set()
clientInitMessages = {}
BRIDGE_TYPE = 'bridge'
CHART_TYPE = 'chart'
class DashboardWebSocket(WebSocket):
    def handleConnected(self):
        try:
            print("Connected", self.address, self.request.path)
            if self.request.path == '/':
                self.socketType = BRIDGE_TYPE
                activeBridges.add(self)
                for _, (tableName, key, value) in clientInitMessages.iteritems():
                    self.sendBridgeValue(tableName, key, value)
            elif self.request.path.startswith('/chart/'):
                self.socketType = CHART_TYPE
                m = re.match(
                    r"^/chart/(.+)/(.+)/(.+)/$", self.request.path)
                if not m:
                    print("Invalid chart uri: ", self.request.path)
                    return
                self.tableName = urllib.unquote(m.group(1))
                self.keyName = urllib.unquote(m.group(2))
                minutesHistory = float(urllib.unquote(m.group(3)))
                backfillStartTimestampMs = \
                    (time.time() - minutesHistory * 60) * 1000
                activeCharts.add(self)
                # get all the history the client wants
                self.prevSequenceId = \
                    recorder.RecorderDb().getStartSequenceIdForTime(
                        backfillStartTimestampMs,
                        self.tableName,
                        self.keyName)
                self.drainChartHistory()
                print("Opened Chart, table: %s key: %s" % (self.tableName, self.keyName))
        except:
            traceback.print_exc()

    def handleMessage(self):
        print("Got Message:", self.data)
        if self.socketType is not BRIDGE_TYPE:
            print "ignoring message"
            return
        try:
            jsonPayload = json.loads(self.data)
            if jsonPayload["table"] != table.path:
                print("Unknown table")
                return
            if jsonPayload["type"] == "string":
                table.putString(jsonPayload["key"], jsonPayload["value"])
                self.echoOwnValue(jsonPayload)
            elif jsonPayload["type"] == "bool":
                table.putBoolean(jsonPayload["key"], jsonPayload["value"])
                self.echoOwnValue(jsonPayload)
        except:
            traceback.print_exc()

    def echoOwnValue(self, jsonPayload):
        table = jsonPayload["table"]
        key = jsonPayload["key"]
        value = jsonPayload["value"]
        self.sendBridgeValue(table, key, value)
        clientInitMessages[(table, key)] = (table, key, value)

    def handleClose(self):
        if self.socketType is BRIDGE_TYPE:
            activeBridges.remove(self)
            print("Bridge Closed", self.address)
        elif self.socketType is CHART_TYPE:
            activeCharts.remove(self)
            print("Chart closed", self.address)


    def sendBridgeValue(self, tableName, key, value):
        if self.socketType is not BRIDGE_TYPE:
            print "ignoring sendBridgeValue"
            return
        jsonPayload = {}
        jsonPayload["table"] = tableName
        jsonPayload["key"] = key
        jsonPayload["value"] = value
        jsonString = u"" + json.dumps(jsonPayload)
        self.sendMessage(jsonString)

    def drainChartHistory(self):
        if self.socketType is not CHART_TYPE:
            print "ignoring drainChartHistory"
            return

        for logPoint in recorder.RecorderDb().genNumberLogPoints(
                self.prevSequenceId, self.tableName, self.keyName):
            self.prevSequenceId = logPoint.sequenceId
            jsonPayload = {}
            jsonPayload["wall_time_ms"] = logPoint.wallTimeMs
            jsonPayload["value"] = logPoint.value
            jsonPayload["sequence_id"] = logPoint.sequenceId
            jsonString = u"" + json.dumps(jsonPayload)
            self.sendMessage(jsonString)

def valueChanged(table, key, value, isNew):
    try:
        clientInitMessages[(table.path, key)] = (table.path, key, value)
        for bridge in activeBridges:
            bridge.sendBridgeValue(table.path, key, value)
        # db = recorder.RecorderDb()
        # db.addLogPoint(table.path, key, value)
        # for chart in activeCharts:
        #    if chart.tableName == table.path and chart.keyName == key:
        #        chart.drainChartHistory()
    except:
        traceback.print_exc()

table.writerDb = None
table.addTableListener(valueChanged)

server = SimpleWebSocketServer("", 8000, DashboardWebSocket)

def close_sig_handler(signal, frame):
    server.close()
    sys.exit()

signal.signal(signal.SIGINT, close_sig_handler)
server.serveforever()

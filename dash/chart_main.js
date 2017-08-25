var TABLE;
var KEY;
var HISTORY_MINUTES;
var PAGE_START_WALL_TIME_MS = Date.now();

var webSocket = null;
var chart;
var dataPoints = [];

$(document).ready(function() {
  TABLE = url("?table");
  KEY = url("?key");
  HISTORY_MINUTES = url("?history_minutes") || "0";

  chart = new CanvasJS.Chart(
    "chartHolder",
    {
      title: { text: TABLE + "/" + KEY },
      data: [{
        type: "line",
        dataPoints: dataPoints
      }],
      zoomEnabled: true,
      panEnabled: true,
      axisX: {
        title: "Milliseconds since opening page",
      },
    });
  chart.render()
  setInterval(function () { chart.render(); }, 30);

  kickWebSocket();
  setInterval(kickWebSocket, 1000);
});

function kickWebSocket() {
  if (webSocket != null) {
    // already working
    return;
  }
  webSocket = new WebSocket(
    "ws://localhost:8000/chart/"
      + encodeURIComponent(TABLE) + "/"
      + encodeURIComponent(KEY) + "/"
      + encodeURIComponent(HISTORY_MINUTES) + "/");
  dataPoints.splice(0);
  webSocket.onmessage = function(evt) {
    console.log(evt.data);
    handlePayloadFromWebSocket(evt.data);
  };
  webSocket.onclose = function() {
    webSocket = null;
  };
}

function handlePayloadFromWebSocket(payloadString) {
  var payloadJson = JSON.parse(payloadString);
  var wallTimeMs = parseInt(payloadJson["wall_time_ms"]);
  var newXValue = wallTimeMs - PAGE_START_WALL_TIME_MS;
  // chart.render() internally has a reference to dataPoints
  dataPoints.push({
    x: newXValue,
    y: parseFloat(payloadJson["value"])
  });
  var earliestAllowedXValue = newXValue - (HISTORY_MINUTES * 60 * 1000);
  while (dataPoints.length > 0 && dataPoints[0].x < earliestAllowedXValue) {
    dataPoints.shift();
  }
}


var imageNr = 0; // Serial number of current image
var webcam;

function startStream() {
  webcam = document.getElementById("webcam");

  setInterval(renewStream, 2000);
}

function renewStream() {
  webcam.src = "http://roborio-254-frc.local:5801/?action=stream&n=" + (++imageNr);
}

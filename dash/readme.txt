You must have python 2.7 installed (3 might work, it's untested though)

* To run the dashboard:

** OSX/Linux:
*** To run the dashboard while you have the robot:
- start the dashboard server:
$ python dash.py 10.2.54.2

- view the client by going opening `dashboard.html` in a browser

*** If you don't have the robot, you can run a fake smartdashboard server:
- start the fake robot server
$ python test_server.py

- start the dashboard server
$ python dash.py localhost

- view the client by going opening `dashboard.html` in a browser

This spews some basic data at the client.

** Windows:
Run `run_dashboard.bat`, this assumes the robot is at `10.2.54.2`. This will start the server and launch a browser with the client.

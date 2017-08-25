# CheezDroid
Android-based Vision System

## To install OpenCV
* Download http://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.1.0/OpenCV-3.1.0-android-sdk.zip/download
* Unzip OpenCV-3.1.0-android-sdk.zip (OpenCV-android-sdk)
* Create directory app/src/main/jniLibs
* Copy OpenCV-android-sdk/sdk/native/libs/* to app/src/main/jniLibs/

## To provision a device for robot use
* Enable device admin
1. Settings App > Security > Device Administrators > Click box 'on' for CheezDroid

* Enable device owner
1. adb shell
2. dpm set-device-owner com.team254.cheezdroid/.ChezyDeviceAdminReceiver

## How to Install ADB on the RoboRIO

Download and run the [install.osx.sh](../installation/install.osx.sh) script in the installation folder. Note that this script has only been tested on Mac OS X; it hasn't been tested on Windows or Linux.

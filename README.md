# TwipWire
TwipWire is an application that allows the user to monitor a particular
Twitter search query in realtime. 

This GitHub project contains all the source code needed to build and
run the application. All that is required to do this is the following:
### Preparation

* Register for a developer account (it's free) at [devs.golgi.io](https://devs.golgi.io)
* Generate a new Application Key on the developer portal
* Download and install the SDK from the developer portal.
* Clone this repository
* Create a file called ```Golgi.DevKey``` containing a single line (the Developer Key you were assigned)
* Create another file called ```Golgi.AppKey``` containing a single line (the Application Key assigned to your new application).

### Build the Server
In the Server directory:

* Edit the ```Makefile``` changing ```PKG_DIR``` ```JDK_BIN``` and ```JRE_BIN``` if needed.

* Run getjars.sh to pull all the needed third-party libraries

* Type ```make run``` to build and launch the Server for QuakeWatch


### Build the Android Application

In Android Studio, open the Project in the 
```Android/Studio/TwipWire``` directory. Modify ```build.gradle``` so that it points to wherever your latest Golgi SDK is installed. 

Build the Application and install it on one
or more Android devices.

### Build the iOS Application

In Xcode, open ```iOS/TwipWire/TwipWire.xcodeproj``` then build and install
the application on one or more iOS devices.


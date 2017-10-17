# Chlorine-Client
COMP30022 - Team Chlorine

# Synopsis
Android application (for devices supporting Android 4.0 or later) to assist users in finding nearby friends or other potential users quickly and efficiently using a graphical interface, chat functionality, map and augmented reality views. 

# Motivation
Currently the best alternative is for the user to directly contact the friend and have them attempt to provide a description of where they are. This often leads to confusion, particularly in unfamiliar locations. Extending the problem to multiple people trying to find each other further complicates the situation, and can easily result in frustration.

# Prerequisites
You will first need to download and run the server which can be found in the `Chlorine-Server` repository (please refer to the instructions in that repository). <br/>
For running on Android Studio: Create an emulator to run the app <br/>
For running on Android phone: Please ensure you have Android 4.0 (API level 15) or later <br/>

# Dependecies
OkHTTP - HTTP client that handles requests/responses <br/>
Gson - used to convert Java objects to their JSON representation <br/><br/>
Please note all dependencies are handled by Gradle and the Android Manifest file which is contained within the repository which you have downloaded, so you do not have to worry about manually handling dependencies (that is assuming you are using Gradle).

# Installing and Running
Download this project. <br/>
Run server (see `Chlorine-Server` repository and Connecting to Server section at the bottom of this document). <br/>
Run project on Android Studio using an emulator or device. <br/>

# Testing
Tests can be found in: `Chlorine-Client/app/src/test/java/net/noconroy/itproject/application` <br/>
Run in Android studio.

### Code layout
Please note that some files less strictly relavant to development (like automatically generated android build files) are emitted from the diagram below. <br/>
```
root/app/src
 |
 |
 +-main (stores the actual code for running the app)
 |  |
 |  |
 |  +-AndroidManifest.xml (contains permissions and information about activities)
 |  |
 |  +-java/net/noconroy/itproject/application (the logic for running the app)
 |  |  |
 |  |  |
 |  |  +- AR (contains logic for the AR view)
 |  |  |  |
 |  |  |  +-CameraFragment.java
 |  |  |  +-CameraPreview.java
 |  |  |  +-CameraSurfaceView.java
 |  |  |  +-CardinalDrawing.java
 |  |  |  +-CompassActivity.java
 |  |  |  +-CompassFriend.java
 |  |  |  +-CompassManager.java
 |  |  |  +-CompassRender.java
 |  |  |  +-CompassSurfacView.java
 |  |  |  +-FriendDrawing.java
 |  |  |  +-LocationCompare.java
 |  |  |  +-LocationService.java
 |  |  |  +-LocationServiceProvider.java
 |  |  |  
 |  |  |
 |  |  +- callbacks (contains logic for callbacks involved with the server" 
 |  |  |  |
 |  |  |  +-AuthenticationCallback.java
 |  |  |  +-EmptyCallback.java
 |  |  |  +-NetworkCallback.java
 |  |  |
 |  |  |
 |  |  +- Chat (contains logic for chat)
 |  |  |  |
 |  |  |  +-ChatActivity.java
 |  |  |  +-ChatAdapter.java
 |  |  |  +-ChatHelper.java
 |  |  |
 |  |  |
 |  |  +- models (classes that allows logical storing of data)
 |  |  |  |
 |  |  |  +-Friends.java
 |  |  |  +-IncomingFriendRequests.java
 |  |  |  +-Message.java
 |  |  |  +-Orientation.java
 |  |  |  +-OutgoingFriendRequests.java
 |  |  |  +-Profile.java
 |  |  |
 |  |  |
 |  |  +-AddFriendActivity.java (activity that allows adding a friend)
 |  |  +-AppLifecycleHandler.java
 |  |  +-DataStorage.java
 |  |  +-DeviceLocationActivity.java
 |  |  +-FriendRequestAdapter.java
 |  |  +-FriendRequestActivity.java
 |  |  +-FriendsActivity.java
 |  |  +-FriendsListAdapter.java
 |  |  +-HomeActivity.java
 |  |  +-MainActivity.java
 |  |  +-MainApplication.java
 |  |  +-NetworkHelper.java
 |  |  +-Notifications.java
 |  |  +-RegisterActivity.java
 |  |  
 |  |
 |  +-java/net/noconroy/itproject/application(test) (Unit Tests)  
 |  |  |
 |  |  +-CameraFragmentTest.java
 |  |  +-ChatHelperUnitTest.javano
 |  |  +-CompassViewUnitTest.java
 |  |  +-FriendFunctionalityUnitTest.javano
 |  |  +-LocationCompareUnitTest.java
 |  |  +-NetworkHelperUnitTest.java
 |  |
 |  |
 |  +-res (contains files/folders relavant to layout/presentation)
 |     |
 |     +-drawable (contains images)
 |     +-layout (determines the visual presentation of a given activity)
 |     +-values (contains values for string and colours)

```

# Connecting to Server
If you're connecting to the default remote server, connecting is very easy and straightforward.
Connecting your app to your own server is dependent on if your server is setup either remotely or locally, and if you're using an emulator or actual device.
###### Default Remote server - EASY OPTION
Just open the app and use it as usual, you don't have to change any settings or anything like that.
###### Your Own Remote server 
Just simply change the SERVER_HOST constant in the NetworkHelper class to the ip address of the server. It does not matter if you are using an emulator or an actual device.
###### Your Own Local server 
**If you are using an emulator:** Change the SERVER_HOST constant in the NetworkHelper class to 10.0.2.2 if you are using the defualt Android Studio emulator, or 10.0.3.2 if you are using a genymotion emulator. <br/><br/>
**If you are using a device that is plugged into the machine running localhost:** This still can work, but what you need to do depends on if you're device/machine is connected to wifi/mobile network, what OS your machine is running, and basically what you need to do is quite dependent on your specific setup. So for this reason, this method is not recommended unless you're an advanced user, but if you still wish to do this, it is still quite achievable and there are resources out there on the internet on how this can be done.













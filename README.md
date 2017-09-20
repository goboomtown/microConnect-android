# Boomtown microConnect for Android (v.1.2.0)

#### Minmum Requirements
- Android 4.4 (API Level 19)
- Android Studio, v2.3.0 or higher recommended
- Gradle 3 or higher recommended

## Overview
**microConnect-android** contains Android libraries for [Connect][connectLink] partners:
1. `BTConnectHelp`, Allows partners to integrate issue creation, notification, and chat in a single `BTConnectHelpButton` button.
1. `BTConnectPresenceService`, Allows partners to integrate with onsite intelligence agents.
## Getting Started

1. Clone this repository and include as a library project in your Android project.
1. In Android Studio, open File -> New -> Import Module
1. Choose the "Source Directory" by browsing to the file path/location of this clone'd repo.
1. The module name "microConnect-android" should be auto-filled once you choose this library's file path.
1. Click Finish.
- or -
1. Download the AAR file from [https://github.com/goboomtown/microConnect-android/blob/master/dist/microConnect-android-1.1.0/microConnect-android-release-1.1.0.aar]
1. From within Android Studio, open File -> New -> New Module
1. Choose Import .JAR/.AAR Package, click Next
1. Enter the filename with full path - use "..." to browse to the file location where you downloaded microConnect-android-release-1.1.0.aar
1. The "Subproject name" should be auto-filled to "microConnect-android-release-1.0.0" once you choose this AAR file path.
1. Click Finish

## API Key Generation
1. Log onto the Admin Portal (https://admin.goboomtown.com)
1. Click "Providers" in the left menu
1. Find your provider in the list
1. Double-click your provider to show the "Edit Provider" window
1. Click "API Settings,"" near the button of the configuration panel
1. Select Sandbox or Live, depending on the state of development
1. Click "Re-Generate"
1. Copy the access token and private-key, as provided in the pop-up dialog

## Obtaining Member Information
For chat to work, **microConnect-android** requires you specify the member and user information of the person using your app. This information can be obtained as follows:

1. Log onto the Admin Portal (https://admin.goboomtown.com)
1. Click "Providers" in the left menu
1. Find and double-click your provider to show the "Edit Provider" window
1. Click "Members" along the top of the "Edit Provider" window
1. Find and double-click the appropriate member from the list to show the "Edit Member" window
1. The `Id` field of the Member Info section contains the value to use for BTConnectHelpButton `membersId`
1. Click "Locations" along the top of the "Edit Member" window
1. Find and double-click the appropriate location for the user of your app to show the "Edit Member Location" window
1. The `Id` field of the Location Information section contains the value to use for BTConnectHelpButton `membersLocationId`
1. Click "Discard & Close" in the lower right to return to the "Edit Member" window
1. Click "Users" along the top of the "Edit Member" window
1. Find and double-click the user of your app to show the "Edit Member User" window
1. The `Id` field of the Member User Info section contains the value to use for BTConnectHelpButton `membersUsersId`

## BTConnectHelp

### Appearance

A `BTConnectHelpButton` can be added to your app using an XML layout file or programmatically, as shown in this screenshot from one of the included example apps.

![screenshot example initial view]<img src="https://raw.githubusercontent.com/goboomtown/microConnect-android/master/dist/images/Sample%20Initial%20View.png" alt="a screenshot of initial view provided by this component" width="450" />

Tapping the `BTConnectHelpButton` will take your user to the Help view.

![screenshot help view]<img src="https://raw.githubusercontent.com/goboomtown/microConnect-android/master/dist/images/Connect%20Help%20View.png" alt="a screenshot of the help view provided by this component" width="450" />

From the Help view, the user may tap the buttons for chat, web, e-mail, or phone support. If the user taps "Chat With Us," an issue will be created for him/her, and he/she will be taken to a chat room associated with that issue.

![screenshot chat view]<img src="https://raw.githubusercontent.com/goboomtown/microConnect-android/master/dist/images/Connect%20Chat%20View.png" alt="a screenshot of the chat view provided by this component" width="450" />

### Usage

_Note:_ An example Android application that uses this library may be found in the `Example/BoomtownSample` folder of this repository.

#### Sample XML Layout

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:tools="http://schemas.android.com/tools"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       android:paddingBottom="@dimen/activity_vertical_margin"
       android:paddingLeft="@dimen/activity_horizontal_margin"
       android:paddingRight="@dimen/activity_horizontal_margin"
       android:paddingTop="@dimen/activity_vertical_margin"
       tools:context="com.goboomtown.boomtowntest.MainActivity">
   
       <com.goboomtown.btconnecthelp.view.BTConnectHelpButton
           android:id="@+id/helpButton"
           android:layout_width="300dp"
           android:layout_height="300dp"
           android:layout_centerVertical="true"
           android:layout_centerHorizontal="true"
           android:background="@android:color/transparent"
           android:padding="20dp"
           app:exampleColor="#33b5e5"
           app:exampleDimension="24sp"
           app:exampleString="" />
   
       <FrameLayout
           xmlns:android="http://schemas.android.com/apk/res/android"
           xmlns:app="http://schemas.android.com/apk/res-auto"
           android:id="@+id/fragment_container"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:visibility="gone" />
   
   </RelativeLayout>

```

#### Sample Java code

```
mHelpButton         = (BTConnectHelpButton) findViewById(R.id.helpButton);
mFragmentContainer  = (FrameLayout)         findViewById(R.id.fragment_container);

mHelpButton.setListener(this);

mHelpButton.memberID 			= "WA3QMJ";
mHelpButton.memberUserID 		= "WA3QMJ-5XK"; //@"WA3QMJ-2QE";
mHelpButton.memberLocationID 	= "WA3QMJ-FYH"; //@"WA3QMJ-JVE";

mHelpButton.supportWebsiteURL 	= Uri.parse("http://example.com");
mHelpButton.supportEmailAddress  = "support@example.com";
mHelpButton.supportPhoneNumber 	= "1-888-555-2368";

mHelpButton.setCredentials("31211E2CC0A30F98ABBD","0a46f159dc5a846d3fa7cf7024adb2248a8bc8ed");

```

### Connect! Intelligent Agent (mDNS) Broadcasts Using BTConnectHelpButton
The BTConnectHelpButton provides a convenient way to connect to Boomtown onsite intelligence agents.  This provides a mechanism for broadcasting mDNS data.  This can be done with a call to BTConnectHelpButton#advertiseServiceWithPublicData(Map, Map) method.

```
Map<String, String> myPubData = new HashMap<String, String>();
myPubData.put("public", "fooData");
Map<String, String> myPrivData = new HashMap<String, String>();
myPrivData.put("private", "someEncryptedData");

mHelpButton.advertiseServiceWithPublicData(myPubData, myPrivData);
```

The lifecycle of advertiseServiceWithPublicData() will be managed by the BTConnectHelpButton class.  To 
manage the lifecycle yourself you can invoke BTConnectHelpButton#stopAdvertiseServiceWithPublicData().

Two methods in BTConnectHelpButtonListener provide for insight into the broadcast of mDNS data:

1. BTConnectHelpButtonListener#helpButtonDidAdvertiseService()
1. BTConnectHelpButtonListener#helpButtonDidFailToAdvertiseService()

## BTConnectPresenceService

This library provides a service class for connecting to Boomtown onsite intelligence agents.  There are no visible parts of this library, as it all works in the background, but you can build visual elements that interact with the service.
This is another way to use the mDNS broadcast functionality in this library is through the exposed BTConnectPresenceService.


### Usage

BTConnectPresenceService is a (singleton) class with public methods for starting and stopping its service.  It provides a DNS-SD broadcast service.  You will need a Boomtown API Key (described above) to use this library component.

The BTConnectPresenceService service can broadcast custom data as desired by the user by using the BTConnectPresenceService#addCustomPayloadData(String,String) methosd.  This custom payload data is encrypted by default, using a SHA256HMAC-encryption algorithm with the API secret as the salt.  Custom payload data can also be sent unencrypted with the BTConnectPresenceService#addCustomPayloadData(String, String, false) method.

One important aspect of using BTConnectPresenceService is to manage its lifecycle as you manage your application's lifecycle (onCreate/onResume/onPause/onStop/etc).  Examples are provided in this library.

_Note:_ An example Android application that uses the BTConnectPresenceService may be found in the `Example/BoomtownSample` folder of this repository.

#### Sample Java code

```
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
        mBTPresenceSvc = com.goboomtown.btconnecthelp.service.BTConnectPresenceService.getInstance(this,
                new BTConnectPresenceService.ServiceAdvertisementListener() {
            public void didAdvertiseService() {
                Log.i(TAG, "service advertised successfully");
            }

            public void didFailToAdvertiseService() {
                Log.i(TAG, "error when advertising service");
            }
        });
}

@Override
protected void onResume() {
	super.onResume();
	mBTPresenceSvc.addCustomPayloadData("test", "public-data", false);
	mBTPresenceSvc.addCustomPayloadData("private", "private-encrypted-data");
	mBTPresenceSvc.start();
}

@Override
protected void onPause() {
	super.onPause();
	mBTPresenceSvc.tearDown();
	Log.v(TAG, "onPause complete");
}

```

## Building Library from Sources

The steps to build this library from source are:

1. Verify a working Gradle v3+ installation.
1. git clone \[this-project]
1. cd /\[this-project]
1. Issue the following command

```
gradle build -x javadoc -x lint
```

## Acknowledgements

**microConnect-android** uses Smack (http://www.igniterealtime.org/projects/smack/), and we are grateful for the contributions of the open source community.

[connectLink]:http://www.goboomtown.com/connect/
[imgLinkChatView]:https://raw.githubusercontent.com/goboomtown/microConnect-android/master/dist/images/Connect%20Chat%20View.png
[imgLinkHelpView]:https://raw.githubusercontent.com/goboomtown/microConnect-android/master/dist/images/Connect%20Help%20View.png
[imgLinkInitialView]:https://raw.githubusercontent.com/goboomtown/microConnect-android/master/dist/images/Sample%20Initial%20View.png

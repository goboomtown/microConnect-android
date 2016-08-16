# Boomtown microConnect for Android (v.1.0)

#### Minmum Requirements
- Android Studio
- Android 4.4 (API Level 19)

## Overview
**microConnect-android** contains `BTConnectHelp`, an Android library for [Connect][connectLink] partners. It allows partners to integrate issue creation, notification, and chat in a single `BTConnectHelpButton` button.

## Getting Started

1. Clone this repository
2. Drag `BTConnectHelp.framework` from the SDK folder into the `Embedded Binaries` section in `Targets->General`
3. In the "Choose options for adding these files" dialog, ensure "Copy items if needed" is checked and "Create Groups" is selected next to "Added folders"

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


## Appearance

A `BTConnectHelpButton` can be added to your app using an XML layout file or programmatically, as shown in this screenshot from one of the included example apps.

![screenshot example initial view][imgLinkInitialView]

Tapping the `BTConnectHelpButton` will take your user to the Help view.

![screenshot help view][imgLinkHelpView]

From the Help view, the user may tap the buttons for chat, web, e-mail, or phone support. If the user taps "Chat With Us," an issue will be created for him, and he will be taken to a chat room associated with that issue.

![screenshot chat view][imgLinkChatView]

## Usage

_Note:_ An example may be found in the `Example` folder of this repository

### Sample XML Layout

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

### Sample Java code
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

## Acknowledgements

**microConnect-android** uses Smack (http://www.igniterealtime.org/projects/smack/), and we are grateful for the contributions of the open source community.


[connectLink]:http://www.goboomtown.com/connect/
[imgLinkChatView]:https://raw.githubusercontent.com/goboomtown/microConnect-android/master/Examples/Images/Connect%20Chat%20View.png
[imgLinkHelpView]:https://raw.githubusercontent.com/goboomtown/microConnect-android/master/Examples/Images/Connect%20Help%20View.png
[imgLinkInitialView]:https://raw.githubusercontent.com/goboomtown/microConnect-android/master/Examples/Images/Sample%20Initial%20View.png

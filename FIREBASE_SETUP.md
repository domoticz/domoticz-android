# Firebase Cloud Messaging (FCM) Configuration Guide

## Overview

This guide explains how to configure your own Firebase Cloud Messaging (FCM) project for push notifications in the Domoticz Android app. This is necessary when running your own Domoticz server on a Raspberry Pi or other local network device.

## Prerequisites
- A Google account
- Your own Domoticz server running on your network
- The Domoticz Android app installed

## Step-by-Step Setup

### 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select an existing project
3. Enter a project name (e.g., "My Domoticz Notifications")
4. (Optional) Disable Google Analytics if you don't need it
5. Click "Create Project"

### 2. Add Android App to Firebase Project
1. In your Firebase project, click the Android icon to add an Android app
2. Enter the next package names:
   `nl.hnogames.domoticz`
4. Click "Register app"

### 3. Get Firebase Configuration
You have two options to configure the app:

#### Option A: Download google-services.json (Recommended)

1. In the Firebase Console, download the `google-services.json` file
2. Transfer the file to your Android device
3. In the Domoticz app:
    - Go to **Settings** → **Notifications** → **Firebase Settings**
    - Tap "Upload google-services.json"
    - Select the downloaded file
    - The app will automatically extract all required information

#### Option B: Manual Configuration

If you prefer to enter the values manually:

1. In the Firebase Console, go to **Project Settings** (gear icon)
2. Under "Your apps", find your Android app
3. Note down the following values:
    - **Project ID**: Found in Project Settings → General
    - **App ID (mobilesdk_app_id)**: Found under your app in Project Settings
    - **API Key**: Found in Project Settings → General → Web API Key
    - **Sender ID (Project Number)**: Found in Project Settings → General → Project Number

4. In the Domoticz app:
    - Go to **Settings** → **Notifications** → **Firebase Settings**
    - Enter each value in the corresponding field
    - Tap "Save Configuration"

### 4. Enable Cloud Messaging API

1. In the Firebase Console, go to **Project Settings** → **Cloud Messaging**
2. At the "Firebase Cloud Messaging API (V1) / Enabled" section, click on the link "Manage Service Accounts"
3. Click on the service account email (the one has firebase-adminsdk in the name)
4. Naviate to 'Keys'
5. Choose Add Key / Create new key
6. Choose 'JSON' as key type
7. You've now downloaded json file that we need for our Domoticz server config.

### 5. Configure Your Domoticz Server

1. Log into your Domoticz web interface
2. Go to **Setup** → **Settings** → **Notifications**
3. Under **Google Cloud Messaging**:
    - Enable GCM/FCM
    - Upload/Choose the json file from step 4
4. Save the settings

### 6. Test the Configuration

1. In the Domoticz app:
    - Go to **Settings** → **Notifications** → **Firebase Settings**
    - Tap "Test Configuration"
    - You should see a success message with your FCM token
2. If successful, restart the app when prompted
3. Go to your Domoticz server (web ui) and under settings/notifications choose 'Test' under Google Cloud Messaging

## Additional Resources

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Domoticz Documentation](https://www.domoticz.com/wiki/)
- [Firebase Console](https://console.firebase.google.com/)

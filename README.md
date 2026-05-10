# Shuffle Cafe

Shuffle Cafe is an Android application intended to be opened, built, and run using Android Studio.

## Prerequisites

Before running the project, install:

- Android Studio
- Android SDK, usually installed through Android Studio
- Git, if you plan to clone the repository
- A physical Android device or an Android Emulator

Android Studio is Google’s official IDE for Android development. The official installation guide is available from Android Developers. :contentReference[oaicite:0]{index=0}

## Installing Android Studio

1. Go to the official Android Studio download page.
2. Download the installer for your operating system:
   - Windows
   - macOS
   - Linux
   - ChromeOS
3. Run the installer.
4. Open Android Studio after installation.
5. Follow the setup wizard.
6. Accept the recommended SDK, emulator, and build tool options.
7. Let Android Studio finish downloading the required components.

To check for updates later, use:

- **Windows/Linux:** `Help > Check for Update`
- **macOS:** `Android Studio > Check for Updates`

## Opening the Shuffle Cafe Project

1. Open Android Studio.
2. Select **Open**.
3. Choose the root folder of the `Shuffle Cafe` project.
4. Wait for Android Studio to sync the project with Gradle.
5. If prompted, install any missing SDK packages or Gradle components.

The first sync may take several minutes because Android Studio may need to download dependencies.

## Setting Up an Emulator

To run the app without a physical Android phone, create an Android Virtual Device, also called an AVD. An AVD defines the Android device profile that the emulator will simulate. :contentReference[oaicite:1]{index=1}

1. In Android Studio, open **Device Manager**.
2. Click **Create Device**.
3. Choose a device profile, such as **Pixel**.
4. Select a recommended Android system image.
5. Download the system image if needed.
6. Click **Finish**.
7. Start the emulator from Device Manager.

The Android Emulator lets you test the app on a simulated Android device without needing a physical phone. :contentReference[oaicite:2]{index=2}

## Running the App

1. Open the project in Android Studio.
2. Wait for Gradle sync to finish.
3. In the top toolbar, select the app run configuration.
4. Select a target device:
   - An Android Emulator, or
   - A connected Android device
5. Click the green **Run** button.

Android Studio will build the app, install it on the selected device, and launch it. :contentReference[oaicite:3]{index=3}

## Running on a Physical Android Device

1. Enable **Developer Options** on your Android device.
2. Enable **USB Debugging**.
3. Connect the device to your computer with a USB cable.
4. Accept the debugging permission prompt on the device.
5. Select the device from the Android Studio device menu.
6. Click **Run**.

Google recommends testing Android apps on a real device before releasing them. :contentReference[oaicite:4]{index=4}

## Troubleshooting

### Gradle sync failed

Try the following:

- Make sure you are connected to the internet.
- Click **Retry** in the Gradle sync message.
- Install any missing SDK packages Android Studio recommends.
- Restart Android Studio.

### No device appears

Try one of these options:

- Start an emulator from **Device Manager**.
- Reconnect your physical Android device.
- Make sure USB Debugging is enabled.
- Try a different USB cable or USB port.

### App will not build

Try:

- `Build > Clean Project`
- `Build > Rebuild Project`
- Checking the **Build** output panel for error messages
- Making sure the correct Android SDK is installed

## Useful Links

- Android Studio install guide: https://developer.android.com/studio/install
- Run apps in Android Studio: https://developer.android.com/studio/run
- Android Emulator guide: https://developer.android.com/studio/run/emulator
- Create and manage virtual devices: https://developer.android.com/studio/run/managing-avds

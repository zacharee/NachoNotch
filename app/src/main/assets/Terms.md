# Nacho Notch

Nacho Notch is a simple app to "hide" the notch on your device.

**Please read below for usage instructions and the Terms of Use.**

# Terms

To use this app, you agree that you have read everything in this document and that you understand it. If you have any questions, please contact me.

If you neglect to read this document, and you contact me with a question already answered here, you likely will not receive an answer.

Be aware that these terms may be updated. Please make sure you review them frequently. They will be linked in the Play Store description and in the app.

# Usage

Install the app and add the included Quick Settings Tile to your QS shade. Use it to toggle Nacho Notch's functionality.
Instructional video: https://www.youtube.com/watch?v=HhH5wK1NokY

Nacho Notch has a few customization options. These are accessed by holding down on the QS Tile.
 - Enable Rounded Corners on Top
   - Add two overlays under the status bar to emulate rounded screen corners
 - Enable Rounded Corners on Bottom
   - Add two overlays above the navigation bar to emulate rounded screen corners
 - Black-Out Navigation Bar
   - Add a black overlay behind the navigation bar
 - Adjust Dimensions
   - Manually adjust the status bar overlay height
   - Manually adjust the navigation bar overlay height
   - Manually adjust the height and width of the rounded corners

These options can be useful for devices which have displays with rounded corners. 

They can also be useful for devices whose navigation bar buttons turn white-on-white in certain apps.

# Permissions

Nacho Notch uses the following permissions:
 - SYSTEM_ALERT_WINDOW
   - Required to show the overlays
   - Should be granted automatically if installed from the Play Store
 - RECEIVE_BOOT_COMPLETED
   - Required to retain enabled state on reboot
   - Automatically granted on install

# Behavior and Limitations

Nacho Notch will remain enabled for as long as you have the toggle enabled. It will retain its state across reboots.
Nacho Notch "hides" the notch by adding a black overlay underneath the status bar. Nothing more, nothing less.

However, there are some limitations:
 - Nacho Notch does *not* function in landscape, as doing so would cut content off.
 - Nacho Notch does *not* push the display content below the notch area. While this is possible, it is not in the scope of this project and introduces overall usability issues.
 - Nacho Notch does *not* function on the lockscreen. This is a limitation in Android.

Please understand that, while these limitations can be worked around, those workarounds could cause many other issues.

Nacho Notch also forces the status bar icons to be white. On some devices, this process also forces the navigation bar buttons to be white, occasionally resulting in an issue where the buttons are white on a white background. See the *Usage* section for options.
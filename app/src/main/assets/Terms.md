# Nacho Notch

Nacho Notch is a simple app to "hide" the notch on your device.

**Please read below for usage instructions and the Terms of Use.**

# Terms

To use this app, you agree that you have read everything in this document and that you understand it. If you have any questions, please contact me.

If you neglect to read this document, and you contact me with a question already answered here, you likely will not receive an answer.

Be aware that these terms may be updated. Please make sure you review them frequently. They will be linked in the Play Store description and in the app.

# Usage

Install the app and add the included Quick Settings Tile to your QS shade. Use it to toggle Nacho Notch's functionality.
[Instructional Video](https://www.youtube.com/watch?v=HhH5wK1NokY)

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
 - Nacho Notch does *not* function in fullscreen, as doing so would cut content off.
 - Nacho Notch does *not* push the display content below the notch area.
 - Nacho Notch does *not* function on the lockscreen. This is a limitation in Android.

Please understand that, while some of these limitations can be worked around, those workarounds could cause many other issues.

Nacho Notch also forces the status bar icons to be white. On some devices, this process also forces the navigation bar buttons to be white, occasionally resulting in an issue where the buttons are white on a white background. See the *Usage* section for options.

# FAQ

## Will Nacho Notch work in landscape mode?
No, and that's by design. Nacho Notch is simply a black overlay behind the status bar. Showing it in landscape, when the status bar is no longer around the notch, would cause content to be cut off.

## Will Nacho Notch work in fullscreen?
No, and that's by design. Nacho Notch is simply a black overlay behind the status bar. Showing it in fullscreen, when the status bar is hidden, would cause content to be cut off.

## Can Nacho Notch push the status bar below the notch area?
No, this isn't possible to do.

## Can Nacho Notch work on the lock screen?
No, this isn't possible to do.

## Can Nacho Notch be set to function in only certain apps?
While it is possible to track which app is currently showing, this is out of scope for Nacho Notch, and would cause significant battery usage.

## Why does Nacho Notch disappear in some apps?
Sensitive system apps, like some Settings pages, prevent any normal screen overlays from showing. This can't be worked around.

## With Nacho Notch active, why isn't it possible to grant permissions or install APKs?
Android has a security feature that prevents certain sensitive options being changed while overlays are displayed on screen. Nacho Notch and any other overlays need to be disabled to change these settings.

## Does Nacho Notch have any settings?
Yes. Access them by long-pressing the quick settings tile.
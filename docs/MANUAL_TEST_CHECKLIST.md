# Manual Testing Checklist - Navigation3 Migration

**Version:** Phase 3 - Navigation3 + EdgeToEdge + Single Activity Architecture
**Date:** 2025-12-22
**Branch:** feature/single-activity-navigation3-design-system

## Overview

This checklist covers manual testing for Phase 3 changes:
- Navigation3 with type-safe NavKeys
- Single Activity Architecture with 4 bottom tabs
- Toss-style tab transition animations (crossfade + scale 0.98 → 1.0)
- QrScanActivity migrated to Composable
- EdgeToEdge implementation with proper WindowInsets handling

## Testing Environment

- **Device/Emulator:** _____________
- **Android Version:** _____________
- **Screen Size:** _____________
- **Build Variant:** Debug / Release
- **Date Tested:** _____________
- **Tester:** _____________

---

## 1. Tab Navigation

### 1.1 Basic Tab Switching
- [ ] **Home tab displays correctly on app launch**
  - Latest lotto result is visible
  - UI elements render properly
  - No layout issues or overlaps

- [ ] **My Lotto tab displays correctly**
  - Saved tickets list is visible (or empty state if no tickets)
  - All UI elements render properly
  - No layout issues

- [ ] **History tab shows "Coming Soon" placeholder**
  - Placeholder text is centered and visible
  - No crashes or errors

- [ ] **Stores tab shows "Coming Soon" placeholder**
  - Placeholder text is centered and visible
  - No crashes or errors

- [ ] **Tab switching works smoothly**
  - Tap each tab in sequence: Home → My Lotto → History → Stores → Home
  - Each tab loads without delay
  - No crashes during tab switches

### 1.2 Tab Icons and Labels
- [ ] **Tab icons update correctly on selection**
  - Selected tab shows filled icon
  - Unselected tabs show outlined icons
  - Icon changes are immediate (no lag)

- [ ] **Tab labels are readable**
  - All 4 tab labels (Home, My Lotto, History, Stores) are visible
  - Text is not truncated
  - Labels match the correct tabs

- [ ] **Selected tab is visually distinct**
  - Selected tab has different color/style than unselected
  - Easy to identify which tab is currently active

### 1.3 Tab State Preservation
- [ ] **Home tab state is preserved when switching tabs**
  - Navigate to a specific lotto result on Home
  - Switch to My Lotto, then back to Home
  - Verify Home still shows the same result (state preserved)

- [ ] **My Lotto tab state is preserved**
  - Scroll to a specific position in My Lotto list
  - Switch to another tab, then back to My Lotto
  - Verify scroll position is preserved

- [ ] **Tab state survives screen rotation**
  - Navigate to a specific tab (e.g., My Lotto)
  - Rotate device (portrait ↔ landscape)
  - Verify same tab is still selected after rotation

### 1.4 Back Button Behavior
- [ ] **Back button on Home tab root exits app**
  - Navigate to Home tab
  - Press device back button
  - App should exit (or show exit confirmation)

- [ ] **Back button on other tabs returns to Home**
  - Navigate to My Lotto/History/Stores tab
  - Press device back button
  - Should return to Home tab (not exit app)

- [ ] **Back button navigates within tab if deep stack exists**
  - If applicable: Navigate deep within a tab
  - Press back button
  - Should navigate back within that tab's stack

### 1.5 Rapid Tab Switching (Stress Test)
- [ ] **No crashes during rapid tab switching**
  - Quickly tap between tabs 10+ times
  - No crashes, ANRs, or freezes

- [ ] **No animation glitches during rapid switching**
  - Animations complete smoothly
  - No visual artifacts or flickering

- [ ] **Memory usage remains stable**
  - Monitor memory in Android Studio Profiler (if available)
  - No significant memory leaks during rapid switching

---

## 2. Tab Transition Animations

### 2.1 Crossfade Animation
- [ ] **Crossfade effect is visible during tab switch**
  - Old tab content fades out
  - New tab content fades in
  - Transition is smooth and pleasing

- [ ] **Crossfade duration feels appropriate**
  - Not too fast (jarring)
  - Not too slow (sluggish)
  - Smooth and responsive feel

### 2.2 Scale Animation (Toss-style)
- [ ] **Subtle scale animation is visible (0.98 → 1.0)**
  - New tab content slightly scales up during transition
  - Effect is subtle and not jarring
  - Enhances the "premium" feel of the app

- [ ] **Scale animation syncs with crossfade**
  - Both animations happen simultaneously
  - No timing mismatch
  - Feels cohesive

### 2.3 Animation Performance
- [ ] **Animations run at 60fps (smooth)**
  - No stuttering or frame drops
  - Smooth on the test device
  - Enable "Profile GPU Rendering" in Developer Options to verify

- [ ] **No animation lag on low-end devices** (if testing on low-end device)
  - Animations still smooth on budget devices
  - Or gracefully degrade if necessary

---

## 3. QR Scan Flow

### 3.1 QR Scan Trigger
- [ ] **QR scan can be triggered from Home screen**
  - FAB or button to open QR scan is visible on Home
  - Tapping it opens QR scan screen

- [ ] **QR scan screen opens correctly**
  - Camera preview appears
  - No blank screen or crash

### 3.2 Camera Permissions
- [ ] **Camera permission is requested on first QR scan**
  - Permission dialog appears
  - Clear explanation for why camera is needed (if provided)

- [ ] **QR scan works after granting permission**
  - Camera preview starts after permission granted
  - Scanning functionality is enabled

- [ ] **Graceful handling if permission denied**
  - Informative message shown to user
  - Option to open app settings to grant permission
  - No crash

- [ ] **Permission state is remembered**
  - After granting permission once, subsequent scans don't re-request
  - After denying, app handles gracefully on retry

### 3.3 QR Scanning Functionality
- [ ] **Camera preview displays correctly**
  - Live camera feed is visible
  - Preview is not distorted or stretched
  - Preview fills screen appropriately

- [ ] **QR code scanning works**
  - Point camera at a lotto QR code
  - QR code is detected and scanned
  - Scanning happens automatically (no manual trigger needed)

- [ ] **Scanned data is processed correctly**
  - After successful scan, data is parsed
  - Ticket information is extracted
  - No errors in parsing

- [ ] **Scanned data is passed back to Home screen**
  - QR scan screen closes after successful scan
  - Home screen receives the scanned ticket data
  - Ticket is displayed or saved appropriately

### 3.4 QR Scan Screen Behavior
- [ ] **Screen stays on during scanning (FLAG_KEEP_SCREEN_ON)**
  - Device screen doesn't dim or sleep during scan
  - Easier to scan QR codes without interruption

- [ ] **Back button exits QR scan correctly**
  - Press back button while in QR scan
  - Returns to Home screen
  - Camera is released properly (no resource leak)

- [ ] **QR scan is full screen (no bottom nav visible)**
  - Bottom navigation bar is hidden during QR scan
  - Provides unobstructed camera view
  - Full screen camera preview

### 3.5 Error Handling
- [ ] **Invalid QR code is handled gracefully**
  - Scan a non-lotto QR code
  - Appropriate error message shown
  - User can retry scanning

- [ ] **Camera initialization failure is handled**
  - If camera fails to start (e.g., in emulator without camera)
  - Informative error message shown
  - No crash

---

## 4. EdgeToEdge Implementation

### 4.1 System Bars (Status Bar & Navigation Bar)
- [ ] **Status bar is transparent/translucent**
  - App content extends behind status bar
  - Status bar icons are visible and contrasting

- [ ] **Navigation bar is transparent/translucent**
  - App content extends behind navigation bar
  - Navigation bar icons are visible

- [ ] **System bar icon colors adapt to theme**
  - Light theme: Dark icons on status/nav bars
  - Dark theme: Light icons on status/nav bars
  - Icons remain legible

### 4.2 Safe Areas and Content Padding
- [ ] **Content doesn't overlap with status bar**
  - Top content (e.g., toolbar) has proper padding
  - Content is not hidden behind status bar

- [ ] **Bottom navigation doesn't overlap with system nav bar**
  - Bottom tab bar is fully visible
  - Tab labels and icons are not cut off
  - Proper inset padding applied

- [ ] **Interactive elements are within safe areas**
  - Buttons, tabs, and other clickable elements are accessible
  - No elements are obstructed by system bars

### 4.3 EdgeToEdge on Different Android Versions
- [ ] **EdgeToEdge works properly on Android 15+** (if testing on Android 15+)
  - Window insets are handled correctly
  - No layout issues specific to Android 15

- [ ] **Graceful fallback on older Android versions** (Android 6.0 - 14)
  - App still functions correctly
  - May not have full edge-to-edge, but no crashes
  - Safe areas are respected

### 4.4 QR Scan EdgeToEdge Behavior
- [ ] **QR scan is truly full screen**
  - Camera preview extends to all edges
  - No bottom nav or other UI elements visible
  - Immersive full-screen experience

- [ ] **System bars behavior during QR scan**
  - System bars may be hidden or translucent
  - Camera preview is unobstructed

---

## 5. Theme Compatibility

### 5.1 Light Theme
- [ ] **App displays correctly in Light theme**
  - All screens render properly
  - Text is readable
  - Colors are correct

- [ ] **Tab navigation works in Light theme**
  - Tab icons and labels are visible
  - Selected/unselected states are clear

- [ ] **QR scan works in Light theme**
  - Camera preview is visible
  - UI elements are legible

### 5.2 Dark Theme
- [ ] **App displays correctly in Dark theme**
  - All screens render properly
  - Text is readable on dark background
  - Colors are correct

- [ ] **Tab navigation works in Dark theme**
  - Tab icons and labels are visible
  - Selected/unselected states are clear

- [ ] **QR scan works in Dark theme**
  - Camera preview is visible
  - UI elements are legible

### 5.3 Theme Switching
- [ ] **Theme can be switched without restart** (if app supports runtime theme change)
  - Change theme in system settings
  - App updates theme immediately or on resume
  - No crashes

- [ ] **Theme persists across app restarts**
  - App respects system theme setting
  - Theme is consistent on launch

---

## 6. Android Version Compatibility

### 6.1 Minimum SDK (Android 6.0, API 23)
- [ ] **App installs on Android 6.0+**
  - APK can be installed on API 23+ devices
  - No installation errors

- [ ] **Core functionality works on Android 6.0**
  - Tab navigation works
  - QR scan works (if device has camera)
  - No crashes

### 6.2 Target SDK (Android 14/15, API 34/35)
- [ ] **App works correctly on latest Android version**
  - All features function as expected
  - EdgeToEdge works properly
  - No deprecated API warnings affect functionality

### 6.3 Different Screen Sizes
- [ ] **App works on small screens (phone, 5" or less)**
  - UI elements are not cut off
  - Text is readable
  - Tab labels fit

- [ ] **App works on large screens (tablet, 10"+)**
  - Layout adapts appropriately
  - No excessive whitespace or stretched UI
  - Responsive design

- [ ] **App works in landscape orientation**
  - Layout adapts to landscape
  - Tab navigation is still usable
  - QR scan works in landscape

---

## 7. Performance and Stability

### 7.1 App Launch
- [ ] **App launches quickly**
  - Cold start time is acceptable (< 3 seconds)
  - No prolonged splash screen or black screen

- [ ] **No crashes on launch**
  - App opens reliably
  - No ANRs (Application Not Responding)

### 7.2 Memory Usage
- [ ] **Memory usage is reasonable**
  - Check in Android Studio Profiler (if available)
  - No significant memory leaks during normal usage

- [ ] **No memory leaks during tab switching**
  - Memory usage stabilizes after several tab switches
  - Old fragments/composables are properly released

### 7.3 Battery and Resource Usage
- [ ] **QR scan doesn't drain battery excessively**
  - Camera is released when QR scan closes
  - No background camera usage

- [ ] **App doesn't keep CPU/GPU busy when idle**
  - Animations stop when not switching tabs
  - No continuous background processing

---

## 8. Regression Testing (Existing Features)

### 8.1 Home Screen - Lotto Results
- [ ] **Latest lotto result displays correctly**
  - Numbers, bonus, prize info are visible
  - UI matches previous design (or improved)

- [ ] **Lotto result refresh works**
  - Pull-to-refresh or refresh button works
  - New data is fetched and displayed

### 8.2 My Lotto Screen - Saved Tickets
- [ ] **Saved tickets list displays correctly**
  - All saved tickets are visible
  - Ticket details are correct

- [ ] **Ticket CRUD operations work**
  - Add ticket (via QR scan or manual entry)
  - Edit ticket (if applicable)
  - Delete ticket
  - All operations work as before

- [ ] **Winning statistics widget displays correctly**
  - Statistics are calculated correctly
  - Widget is positioned correctly (not overlapping)

### 8.3 QR Code Parsing
- [ ] **QR code parsing works for various formats**
  - Test with multiple lotto QR codes
  - All formats are parsed correctly
  - No parsing errors

---

## 9. Edge Cases and Error Scenarios

### 9.1 Network Errors
- [ ] **App handles network errors gracefully**
  - Disable network, try to refresh lotto results
  - Appropriate error message shown
  - No crashes

### 9.2 Configuration Changes
- [ ] **Screen rotation preserves state**
  - Rotate device multiple times
  - Current tab and data are preserved
  - No crashes or data loss

- [ ] **Locale change doesn't break app** (if app supports multiple languages)
  - Change device language
  - App still functions correctly

### 9.3 Low Memory Scenarios
- [ ] **App handles low memory gracefully**
  - Simulate low memory (if possible)
  - App may reload, but doesn't crash
  - Critical data is preserved

### 9.4 Process Death
- [ ] **App restores state after process death**
  - Open app, navigate to a specific tab/state
  - Force app to background, kill process (Settings → Force Stop)
  - Reopen app
  - Verify state is restored (tab, scroll position, etc.)

---

## 10. Accessibility

### 10.1 TalkBack / Screen Reader
- [ ] **Tab labels are announced correctly**
  - Enable TalkBack
  - Navigate between tabs
  - Each tab is announced clearly

- [ ] **All interactive elements are accessible**
  - Buttons, tabs, and other elements can be focused
  - Proper content descriptions provided

### 10.2 Font Scaling
- [ ] **App works with large font sizes**
  - Increase system font size to maximum
  - Text is readable and not cut off
  - Layout doesn't break

### 10.3 Color Contrast
- [ ] **Text has sufficient color contrast**
  - Text is readable on backgrounds
  - Meets WCAG accessibility guidelines (if applicable)

---

## Test Summary

### Overall Results
- **Total Tests:** ____
- **Passed:** ____
- **Failed:** ____
- **Blocked:** ____

### Critical Issues Found
1. _____________________________________________________________
2. _____________________________________________________________
3. _____________________________________________________________

### Non-Critical Issues Found
1. _____________________________________________________________
2. _____________________________________________________________
3. _____________________________________________________________

### Recommendations
- _____________________________________________________________
- _____________________________________________________________
- _____________________________________________________________

### Sign-off

**Tester Signature:** ____________________
**Date:** ____________________
**Approved for Release:** Yes / No / Conditional

---

## Notes

- Use an actual device for most accurate testing (emulator may not show some EdgeToEdge behaviors correctly)
- Test on multiple devices with different screen sizes and Android versions if possible
- For animation testing, enable "Show layout bounds" and "Profile GPU rendering" in Developer Options for detailed analysis
- Document any crashes with stack traces from Logcat
- Take screenshots of visual issues for bug reports


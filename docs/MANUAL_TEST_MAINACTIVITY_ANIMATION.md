# MainActivity Animation Improvements - Manual Test Checklist

## Overview
This checklist validates the UI animation improvements made to MainActivity.kt for QR screen transitions.

**Changes Tested:**
- Scaffold → Box layout migration
- NavigationBar overlay with AnimatedVisibility
- Dynamic padding animation with `animateDpAsState`
- StatusBar height awareness

**Test Date:** _____________
**Tester:** _____________
**Device/Emulator:** _____________
**Android Version:** _____________

---

## Pre-Test Setup

- [ ] Clean build: `./gradlew clean build`
- [ ] Install fresh APK on test device
- [ ] Enable Developer Options → Show layout bounds (optional, for debugging)

---

## Test Scenarios

### 1. Home Screen → QR Screen Transition

#### 1.1 Basic Navigation
- [ ] **Test:** Tap FAB or button to navigate to QR screen
- [ ] **Expected:**
  - Screen transitions smoothly without jumping
  - StatusBar area transitions from colored to transparent
  - NavigationBar slides down and fades out simultaneously
  - Content area expands smoothly to full screen
  - Camera preview appears without layout shift

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 1.2 Animation Timing
- [ ] **Test:** Observe animation speed and coordination
- [ ] **Expected:**
  - NavigationBar exit animation: ~150ms (should feel snappy)
  - Top padding animation: ~250ms (smooth transition)
  - Bottom padding animation: ~200ms with 100ms delay (synchronized with NavBar exit)
  - No visible "gap" or "jump" during transition

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 1.3 Edge-to-Edge Behavior
- [ ] **Test:** Check edge-to-edge rendering during transition
- [ ] **Expected:**
  - StatusBar becomes transparent (camera shows behind it)
  - NavigationBar area becomes part of content
  - No white/black bars visible during animation
  - Gesture navigation bar (Android 10+) remains visible

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

---

### 2. QR Screen → Home Screen Transition (Back Navigation)

#### 2.1 Back Button/Gesture Navigation
- [ ] **Test:** Press back button or use back gesture from QR screen
- [ ] **Expected:**
  - Screen transitions smoothly back to Home
  - NavigationBar slides up and fades in simultaneously
  - StatusBar background returns smoothly
  - Content area shrinks smoothly with no jump
  - No visible flicker of NavigationBar background

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 2.2 NavigationBar Appearance Timing
- [ ] **Test:** Focus on NavigationBar during exit animation
- [ ] **Expected:**
  - NavigationBar slides in with 100ms delay (should feel intentional, not laggy)
  - Background and content appear together (no separation)
  - Final position is exactly at screen bottom, no gap

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

---

### 3. Tab Switching (Home ↔ My Lotto ↔ History ↔ Stores)

#### 3.1 Tab Navigation
- [ ] **Test:** Switch between all bottom navigation tabs
- [ ] **Expected:**
  - NavigationBar remains visible and stable
  - No animation jank during tab switches
  - Selected tab indicator updates immediately
  - Content swaps correctly with no layout shift

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

---

### 4. Configuration Changes

#### 4.1 Screen Rotation
- [ ] **Test:** Rotate device on Home screen
- [ ] **Expected:** Layout adjusts correctly, NavigationBar remains visible
- [ ] **Test:** Rotate device on QR screen
- [ ] **Expected:** Full-screen layout maintained, no NavigationBar visible
- [ ] **Test:** Rotate device DURING transition animation
- [ ] **Expected:** Animation completes gracefully, final state is correct

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 4.2 Multi-Window / Split Screen
- [ ] **Test:** Enter multi-window mode from Home screen
- [ ] **Expected:** NavigationBar adapts to smaller window size
- [ ] **Test:** Navigate to QR screen in multi-window mode
- [ ] **Expected:** Full-screen behavior works within window bounds

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

---

### 5. Edge Cases

#### 5.1 Rapid Navigation
- [ ] **Test:** Quickly tap to QR screen → immediately tap back (before animation finishes)
- [ ] **Expected:**
  - Animation reverses smoothly, no crash
  - Final state is correct (back on Home screen)
  - No visual artifacts

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 5.2 App Background/Foreground
- [ ] **Test:** Navigate to QR screen → press Home button → reopen app
- [ ] **Expected:** QR screen state preserved, full-screen layout maintained
- [ ] **Test:** On Home screen → background → foreground
- [ ] **Expected:** NavigationBar visible, layout correct

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 5.3 Low-End Device Performance
- [ ] **Test:** Test on low-end device (if available) or enable "Animator duration scale 10x" in Developer Options
- [ ] **Expected:**
  - Animations still synchronize correctly (even if slower)
  - No frame drops that cause visual glitches
  - Padding updates stay coordinated with NavigationBar visibility

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

---

### 6. Accessibility

#### 6.1 TalkBack Navigation
- [ ] **Test:** Enable TalkBack, navigate from Home → QR → Home
- [ ] **Expected:**
  - TalkBack announces screen changes correctly
  - Focus moves logically during transitions
  - NavigationBar items remain accessible when visible

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

#### 6.2 Large Font Sizes
- [ ] **Test:** Enable largest font size, navigate between screens
- [ ] **Expected:**
  - NavigationBar height adjusts correctly (measured dynamically)
  - Bottom padding accounts for larger NavigationBar
  - No content clipping

**Result:** ✅ Pass / ❌ Fail / ⚠️ Issues
**Notes:**
```



```

---

### 7. Visual Regression (Compare with Previous Version)

#### 7.1 Known Issues Fixed
- [ ] **Issue 1 (FIXED):** Screen top jumping when entering QR screen
  - **Test:** Navigate Home → QR, observe top of screen
  - **Expected:** Smooth expansion, no jump
  - **Result:** ✅ Fixed / ❌ Still occurs

- [ ] **Issue 2 (FIXED):** NavigationBar background appearing without animation when exiting QR screen
  - **Test:** Navigate QR → Home, observe NavigationBar
  - **Expected:** Background slides in with content (not before)
  - **Result:** ✅ Fixed / ❌ Still occurs

**Notes:**
```



```

---

## Device Coverage Matrix

Test on at least 3 different configurations:

| Device/Emulator | Android Version | Screen Size | Gesture Nav | Result |
|----------------|-----------------|-------------|-------------|---------|
| _______________ | _______________ | ___________ | ☐ Yes ☐ No | ☐ Pass ☐ Fail |
| _______________ | _______________ | ___________ | ☐ Yes ☐ No | ☐ Pass ☐ Fail |
| _______________ | _______________ | ___________ | ☐ Yes ☐ No | ☐ Pass ☐ Fail |

**Recommended Coverage:**
- One phone (normal size)
- One tablet (large screen)
- One Android 10+ with gesture navigation
- One Android 9 with button navigation

---

## Overall Test Summary

**Total Tests:** ___ / 20
**Passed:** ___
**Failed:** ___
**Issues Found:** ___

**Critical Issues:**
```



```

**Minor Issues:**
```



```

**Recommendations:**
```



```

---

## Sign-Off

- [ ] All critical scenarios pass
- [ ] Known issues are documented
- [ ] Performance is acceptable on target devices
- [ ] Ready for release

**Tester Signature:** _____________
**Date:** _____________

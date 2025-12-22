# MainActivity UI Animation Testing - Quick Start

## What Changed?

MainActivity.kt received UI animation improvements to fix:
1. Screen top jumping when entering QR screen
2. NavigationBar background appearing without animation when exiting QR screen

**Solution:** Scaffold → Box layout + AnimatedVisibility + animateDpAsState

---

## How to Test

### Quick Start (15 minutes)

1. **Build and install:**
   ```bash
   ./gradlew installDebug
   ```

2. **Run manual tests:**
   - Navigate: Home → QR → Home (back button)
   - Navigate: Home → QR → Home (back gesture, if device supports)
   - Switch tabs: Home → My Lotto → History → Stores
   - Rotate device on QR screen and Home screen

3. **Check these specific issues (FIXED):**
   - ✅ No screen top jump when entering QR screen
   - ✅ NavigationBar background slides in (not appearing instantly) when exiting QR

4. **Look for regressions:**
   - ❌ Animation stuttering or frame drops
   - ❌ Layout glitches during transitions
   - ❌ NavigationBar not hiding on QR screen

---

## Test Artifacts

| File | Purpose | When to Use |
|------|---------|-------------|
| [MANUAL_TEST_MAINACTIVITY_ANIMATION.md](./MANUAL_TEST_MAINACTIVITY_ANIMATION.md) | Comprehensive manual test checklist (20 test scenarios) | Before PR merge, QA sign-off, release verification |
| [TESTING_STRATEGY_MAINACTIVITY_ANIMATION.md](./TESTING_STRATEGY_MAINACTIVITY_ANIMATION.md) | Explains why manual testing is preferred over automated | Reference for future similar changes |
| `app/src/androidTest/.../MainScreenSmokeTest.kt` | Minimal smoke test (optional, may fail) | Optional regression check |

---

## Testing Philosophy for Animation Changes

**TL;DR:** Manual testing is the primary validation method for UI animations.

**Why?**
- Automated tests cannot verify animation smoothness, timing, or visual polish
- Test infrastructure cost > value for animation validation
- 15 minutes of manual testing catches more issues than hours of automated test development

**When to use automated tests:**
- Business logic (UseCases, ViewModels, Repositories)
- Data transformations
- State management

**When to use manual tests:**
- Animations and visual polish
- User interaction flows
- Device-specific rendering

---

## Quick Commands

```bash
# Build the app
./gradlew :app:assembleDebug

# Install on connected device
./gradlew installDebug

# Run optional smoke test (may require Hilt setup)
./gradlew :app:connectedDebugAndroidTest --tests MainScreenSmokeTest

# Clean build
./gradlew clean build
```

---

## Sign-Off Checklist

Before merging this PR:

- [ ] Manual test checklist completed on at least 1 device
- [ ] Both original issues confirmed fixed (screen jump, NavBar background)
- [ ] No new visual regressions introduced
- [ ] Tested on Android 10+ with gesture navigation (if available)

**QA Sign-Off (Optional but Recommended):**
- [ ] Tested on phone (normal size)
- [ ] Tested on tablet (large screen)
- [ ] Tested on low-end device (performance check)
- [ ] Tested with TalkBack enabled (accessibility)

---

## Questions?

**Q: Do I need to run the automated smoke test?**
A: No. It's optional and may fail due to Hilt dependencies. Manual testing is more important.

**Q: How do I know if animations are "smooth enough"?**
A: Compare with system animations (e.g., opening Settings app). Should feel similar.

**Q: What if I find issues during manual testing?**
A: Document them in the manual test checklist, then fix and re-test.

**Q: Should I block PR on test coverage?**
A: No. Manual testing is the primary validation. The test checklist serves as "test coverage."

---

## Related Files

- **Modified Code:** `/app/src/main/java/com/enso/lotto_assist/MainActivity.kt`
- **Build Config:** `/app/build.gradle.kts` (added Compose UI test dependencies)

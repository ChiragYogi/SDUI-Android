package com.chiraggoswami.sduidemo.macrobenchmark

import android.content.Intent

// This module talks to the target app only via Intent/UiAutomator, never its code — every
// literal here (package, activity, extras, testTag-as-resource-id) mirrors a constant in
// :app and must be kept in sync by hand if the app-side name ever changes.
const val TARGET_PACKAGE = "com.chiraggoswami.sduidemo"
private const val MAIN_ACTIVITY = "$TARGET_PACKAGE.MainActivity"

/** Mirrors MainActivity.EXTRA_SCREEN_VARIANT / VARIANT_STATIC. */
const val EXTRA_SCREEN_VARIANT = "screen_variant"
const val VARIANT_STATIC = "static"

/** Mirrors SduiScreen's HOME_SCROLL_ROOT_TAG, exposed via testTagsAsResourceId. */
const val HOME_SCROLL_ROOT_TAG = "home_scroll_root"

fun launchIntent(variant: String? = null): Intent = Intent(Intent.ACTION_MAIN).apply {
    setClassName(TARGET_PACKAGE, MAIN_ACTIVITY)
    addCategory(Intent.CATEGORY_LAUNCHER)
    variant?.let { putExtra(EXTRA_SCREEN_VARIANT, it) }
}

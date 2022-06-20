/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION") // TODO: replace deprecated android.preference with androidx

package com.example.android.apis.app

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.android.apis.R
import kotlin.math.abs

/**
 * This activity provides a comprehensive UI for exploring and operating the DevicePolicyManager
 * api.  It consists of two primary modules:
 *
 *  1.  A device policy controller, implemented here as a series of preference fragments.  Each
 *  one contains code to monitor and control a particular subset of device policies.
 *
 *  2.  A DeviceAdminReceiver, to receive updates from the DevicePolicyManager when certain aspects
 *  of the device security status have changed.
 */
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(api = Build.VERSION_CODES.N)
class DeviceAdminSample : PreferenceActivity() {
    // Interaction with the DevicePolicyManager
    /**
     * Handle to the DEVICE_POLICY_SERVICE system level service.
     */
    var mDPM: DevicePolicyManager? = null

    /**
     * `ComponentName` of the class of `DeviceAdminSampleReceiver`
     */
    var mDeviceAdminSample: ComponentName? = null

    /**
     * Called when the PreferenceActivity is starting. First we call our super's implementation of
     * `onCreate`. Then we initialize our field `DevicePolicyManager mDPM` with a handle
     * to the DEVICE_POLICY_SERVICE system level service (the public interface for managing policies
     * enforced on a device). Finally we initialize our field `ComponentName mDeviceAdminSample`
     * with an instance for the class of `DeviceAdminSampleReceiver` (we will later use this
     * when we need to supply the name of the admin component to `DevicePolicyManager` methods).
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prepare to work with the DPM
        mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mDeviceAdminSample = ComponentName(this, DeviceAdminSampleReceiver::class.java)
    }

    /**
     * Called when the activity needs its list of headers built. By implementing this and adding at
     * least one item to the list, you will cause the activity to run in its modern fragment mode.
     * Note that this function may not always be called; for example, if the activity has been asked
     * to display a particular fragment without the header list, there is no need to build the headers.
     *
     *
     * Typical implementations will use [.loadHeadersFromResource] to fill in the list from a
     * resource as we do, loading them from the file R.xml.device_admin_headers into our parameter
     * `List<Header> target`.
     *
     * @param target The list in which to place the headers.
     * We override this method to provide PreferenceActivity with the top-level preference headers.
     */
    @Deprecated("Deprecated in Java")
    override fun onBuildHeaders(target: List<Header>) {
        loadHeadersFromResource(R.xml.device_admin_headers, target)
    }

    /**
     * Helper to determine if we are an active admin, returns true if `ComponentName mDeviceAdminSample`
     * is currently active (enabled) in the system. We just return the value returned by the
     * `isAdminActive` method of our field `DevicePolicyManager mDPM` for our field
     * `ComponentName mDeviceAdminSample`.
     *
     * @return `true` if `ComponentName mDeviceAdminSample` is currently enabled in the
     * system, `false` otherwise.
     */
    private val isActiveAdmin: Boolean
        get() = mDPM!!.isAdminActive((mDeviceAdminSample)!!)

    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. We return true if our parameter `String fragmentName`
     * matches one of five the class names: `GeneralFragment`, `QualityFragment`,
     * `ExpirationFragment`, `LockWipeFragment`, or `EncryptionFragment`.
     *
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
    @Deprecated("Deprecated in Java")
    override fun isValidFragment(fragmentName: String): Boolean {
        return ((GeneralFragment::class.java.name == fragmentName) || (QualityFragment::class.java.name == fragmentName) || (ExpirationFragment::class.java.name == fragmentName) || (LockWipeFragment::class.java.name == fragmentName) || (EncryptionFragment::class.java.name == fragmentName))
    }

    /**
     * Common fragment code for DevicePolicyManager access. Provides two shared elements:
     *
     *  *
     * 1. Provides instance variables to access activity/context, DevicePolicyManager, etc.
     *
     *  *
     * 2. Provides support for the "set password" button(s) shared by multiple fragments.
     *
     *
     */
    open class AdminSampleFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
        // Useful instance variables
        /**
         * Cached reference to the Activity this fragment is currently associated with, as returned
         * by `getActivity`. Used as the context for retrieving resources, and accessing fields
         * and methods of our parent Activity.
         */
        protected var mActivity: DeviceAdminSample? = null

        /**
         * Cached reference to the `DevicePolicyManager mDPM` field of `mActivity`
         * (saves typing?).
         */
        protected var mDPM: DevicePolicyManager? = null

        /**
         * Cached reference to the `ComponentName mDeviceAdminSample` field of `mActivity`
         * (saves typing?).
         */
        protected var mDeviceAdminSample: ComponentName? = null

        /**
         * Cached value of a call to the `isActiveAdmin` method of `mActivity`, saves
         * repeated calls to the `DevicePolicyManager.isAdminActive` method.
         */
        protected var mAdminActive = false
        // Optional shared UI
        /**
         * Reference to the KEY_SET_PASSWORD ("key_set_password") `PreferenceScreen` in our UI
         * (if it exists).
         */
        private var mSetPassword: PreferenceScreen? = null

        /**
         * Reference to the KEY_RESET_PASSWORD ("key_reset_password") `EditTextPreference` in
         * our UI (if it exists).
         */
        private var mResetPassword: EditTextPreference? = null

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call our super's implementation of `onActivityCreated`. We
         * initialize our field `DeviceAdminSample mActivity` with the Activity this fragment
         * is currently associated with (as returned by the `getActivity` method), and then use
         * it to set our field `DevicePolicyManager mDPM` to the value of the `mDPM` field
         * of `mActivity`, our field `ComponentName mDeviceAdminSample` to the value of
         * field `mDeviceAdminSample` of `mActivity`, and our field `boolean mAdminActive`
         * to the value returned by the `isActiveAdmin` method of `mActivity`. We initialize
         * our field `EditTextPreference mResetPassword` by finding the [Preference] with
         * the key KEY_RESET_PASSWORD ("key_reset_password"), and our field `EditTextPreference mResetPassword`
         * by finding the [Preference] with the key KEY_SET_PASSWORD ("key_set_password"). If
         * `mResetPassword` is not null we set its `OnPreferenceChangeListener` to this
         * (the callback to be invoked when the Preference is changed by the user will be our override
         * of `onPreferenceChange`). If `mSetPassword` is not null we set its
         * `OnPreferenceClickListener` to this (the callback to be invoked when this Preference
         * is clicked, our `onPreferenceClick` override will be called).
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
         */
        @Deprecated("Deprecated in Java")
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            // Retrieve the useful instance variables
            mActivity = activity as DeviceAdminSample
            mDPM = mActivity!!.mDPM
            mDeviceAdminSample = mActivity!!.mDeviceAdminSample
            mAdminActive = mActivity!!.isActiveAdmin

            // Configure the shared UI elements (if they exist)
            mResetPassword = findPreference(KEY_RESET_PASSWORD) as EditTextPreference
            mSetPassword = findPreference(KEY_SET_PASSWORD) as PreferenceScreen
            if (mResetPassword != null) {
                mResetPassword!!.onPreferenceChangeListener = this
            }
            if (mSetPassword != null) {
                mSetPassword!!.onPreferenceClickListener = this
            }
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of `onResume`. We set our `boolean mAdminActive` to the
         * value returned by the `isActiveAdmin` method of our parent activity `mActivity`
         * (returns true if we are currently an active admin). We then call our method `reloadSummaries`
         * which will display text describing the sufficiency of our password if it is appropriate to
         * do so. If our field `EditTextPreference mResetPassword` is not null we call its
         * `setEnabled` method with `mAdminActive`, disabling the preference if we are
         * not an active admin.
         */
        @Deprecated("Deprecated in Java")
        override fun onResume() {
            super.onResume()
            mAdminActive = mActivity!!.isActiveAdmin
            reloadSummaries()
            // Resetting the password via API is available only to active admins
            if (mResetPassword != null) {
                mResetPassword!!.isEnabled = mAdminActive
            }
        }

        /**
         * Called automatically at every onResume. Should also call explicitly any time a  policy
         * changes that may affect other policy values. If our field `PreferenceScreen mSetPassword`
         * is null we do nothing, if it is not branch on the value of `mAdminActive`:
         *
         *  *
         * true (we are an active admin): we initialize `boolean sufficient` with the
         * value returned by the `isActivePasswordSufficient` method of `mDPM`
         * (Returns true if the password meets the current requirements, else false). If
         * `sufficient` is true we set the summary of `mSetPassword` to the string
         * with resource ID R.string.password_sufficient ("Current password meets policy
         * requirements"), if false we set its summary to the string with resource ID
         * R.string.password_insufficient ("Current password does not meet policy requirements").
         *
         *  *
         * false (we are not an active admin): we set the summary of `mSetPassword` to null.
         *
         *
         */
        protected open fun reloadSummaries() {
            if (mSetPassword != null) {
                if (mAdminActive) {
                    // Show password-sufficient status under Set Password button
                    val sufficient = mDPM!!.isActivePasswordSufficient
                    mSetPassword!!.setSummary(if (sufficient) R.string.password_sufficient else R.string.password_insufficient)
                } else {
                    val dummy: CharSequence? = null
                    mSetPassword!!.summary = dummy
                }
            }
        }

        /**
         * Delays a call to `reloadSummaries` until after preference changes have been applied
         * upon return from the various `onPreferenceChange` overrides.
         */
        protected fun postReloadSummaries() {
            view!!.post { reloadSummaries() }
        }

        /**
         * Called when a Preference has been clicked. This is used only by `mSetPassword`, so
         * if `mSetPassword` is null or not equal to our parameter `preference` we return
         * false having done nothing. If it is `mSetPassword` we initialize `Intent intent`
         * with an instance for the action ACTION_SET_NEW_PASSWORD (have the user enter a new password
         * that meets the current requirements), launch the activity of `intent` and return true
         * consuming the click.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference): Boolean {
            if (mSetPassword != null && preference === mSetPassword) {
                val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                startActivity(intent)
                return true
            }
            return false
        }

        /**
         * Called when a Preference has been changed by the user. This is called before the state of
         * the Preference is about to be updated and before the state is persisted. This is used only
         * by `mResetPassword`, so if `mResetPassword` is null or not equal to our parameter
         * `preference` we return false having done nothing. If it is `mResetPassword` we
         * call our method `doResetPassword` with the string cast of our parameter `newValue`
         * to force a new password for device unlock (no longer works for Android O and above), and
         * return true to update the state of the Preference with the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (mResetPassword != null && preference === mResetPassword) {
                doResetPassword(newValue as String)
                return true
            }
            return false
        }

        /**
         * Force a new password for device unlock. This is dangerous, so we prevent automated tests
         * from doing it, and we remind the user after we do it. If our method `alertIfMonkey`
         * returns true indicating we are being run by an automated test, we return true having done
         * nothing (`alertIfMonkey` will have displayed an alert dialog with the string with
         * resource id R.string.monkey_reset_password: "You can't reset my password, you are a monkey!")
         * If we are being used by a real user we call the `resetPassword` method of our field
         * `DevicePolicyManager mDPM` with our parameter `newPassword` as the new password
         * for the user, and RESET_PASSWORD_REQUIRE_ENTRY (don't allow other admins to change the
         * password again until the user has entered it) as the flags. We then initialize our variable
         * `AlertDialog.Builder builder` with a new instance, initialize `String message`
         * with the string resulting from formatting `newPassword` with the format string with
         * resource id R.string.reset_password_warning ("You have just reset your screen lock password
         * to "%1$s"). We then set the message of `builder` to `message`, set the text of
         * its positive button to the string with resource id R.string.reset_password_ok ("Don't forget
         * it") with null for the `OnClickListener`. Finally we create an [AlertDialog]
         * with the arguments supplied to `builder` and immediately display the dialog.
         *
         * @param newPassword new password.
         */
        private fun doResetPassword(newPassword: String) {
            if (alertIfMonkey(mActivity, R.string.monkey_reset_password)) {
                return
            }
            mDPM!!.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY)
            val builder = AlertDialog.Builder(mActivity)
            val message = mActivity!!.getString(R.string.reset_password_warning, newPassword)
            builder.setMessage(message)
            builder.setPositiveButton(R.string.reset_password_ok, null)
            builder.show()
        }

        /**
         * Simple helper for summaries showing local & global (aggregate) policy settings. We just
         * return a string displaying the string value of our parameters formatted using the string
         * with resource id R.string.status_local_global ("Local=%1$s / Global=%2$s").
         *
         * @param local policy setting for local
         * @param global policy setting for global
         * @return string displaying the string value of our parameters formatted using the string
         * with resource id R.string.status_local_global ("Local=%1$s / Global=%2$s")
         */
        protected fun localGlobalSummary(local: Any?, global: Any?): String {
            return getString(R.string.status_local_global, local, global)
        }
    }

    /**
     * PreferenceFragment for "general" preferences.
     */
    class GeneralFragment : AdminSampleFragment(), Preference.OnPreferenceChangeListener {
        // UI elements
        /**
         * `CheckBoxPreference` with the key KEY_ENABLE_ADMIN ("key_enable_admin") in the
         * xml/device_admin_general.xml PreferenceScreen
         */
        private var mEnableCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_CAMERA ("key_disable_camera") in the
         * xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableCameraCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_KEYGUARD_WIDGETS ("key_disable_keyguard_widgets")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardWidgetsCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_KEYGUARD_SECURE_CAMERA
         * ("key_disable_keyguard_secure_camera") in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardSecureCameraCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_NOTIFICATIONS ("key_disable_notifications")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardNotificationCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_TRUST_AGENTS ("key_disable_trust_agents")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardTrustAgentCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_UNREDACTED ("key_disable_unredacted")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardUnredactedCheckbox: CheckBoxPreference? = null

        /**
         * `EditTextPreference` with the key KEY_TRUST_AGENT_COMPONENT ("key_trust_agent_component")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mTrustAgentComponent: EditTextPreference? = null

        /**
         * `EditTextPreference` with the key KEY_TRUST_AGENT_FEATURES ("key_trust_agent_features")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mTrustAgentFeatures: EditTextPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_FINGERPRINT ("key_disable_fingerprint")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardFingerprintCheckbox: CheckBoxPreference? = null

        /**
         * `CheckBoxPreference` with the key KEY_DISABLE_REMOTE_INPUT ("key_disable_remote_input")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private var mDisableKeyguardRemoteInputCheckbox: CheckBoxPreference? = null

        /**
         * Called to do initial creation of a `PreferenceFragment`. First we call our super's
         * implementation of `onCreate`, then we call the `addPreferencesFromResource`
         * method to inflate our XML resource file R.xml.device_admin_general and add its preference
         * hierarchy to the current preference hierarchy. We then initialize the fields we use to
         * access the various [Preference] widgets in our UI by finding them using the android:key
         * strings they are identified by in the xml/device_admin_general.xml file. After doing so we
         * set their `OnPreferenceChangeListener` to this.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.device_admin_general)
            mEnableCheckbox = findPreference(KEY_ENABLE_ADMIN) as CheckBoxPreference
            mEnableCheckbox!!.onPreferenceChangeListener = this
            mDisableCameraCheckbox = findPreference(KEY_DISABLE_CAMERA) as CheckBoxPreference
            mDisableCameraCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardWidgetsCheckbox =
                findPreference(KEY_DISABLE_KEYGUARD_WIDGETS) as CheckBoxPreference
            mDisableKeyguardWidgetsCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardSecureCameraCheckbox =
                findPreference(KEY_DISABLE_KEYGUARD_SECURE_CAMERA) as CheckBoxPreference
            mDisableKeyguardSecureCameraCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardNotificationCheckbox =
                findPreference(KEY_DISABLE_NOTIFICATIONS) as CheckBoxPreference
            mDisableKeyguardNotificationCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardUnredactedCheckbox =
                findPreference(KEY_DISABLE_UNREDACTED) as CheckBoxPreference
            mDisableKeyguardUnredactedCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardFingerprintCheckbox =
                findPreference(KEY_DISABLE_FINGERPRINT) as CheckBoxPreference
            mDisableKeyguardFingerprintCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardRemoteInputCheckbox =
                findPreference(KEY_DISABLE_REMOTE_INPUT) as CheckBoxPreference
            mDisableKeyguardRemoteInputCheckbox!!.onPreferenceChangeListener = this
            mDisableKeyguardTrustAgentCheckbox =
                findPreference(KEY_DISABLE_TRUST_AGENTS) as CheckBoxPreference
            mDisableKeyguardTrustAgentCheckbox!!.onPreferenceChangeListener = this
            mTrustAgentComponent = findPreference(KEY_TRUST_AGENT_COMPONENT) as EditTextPreference
            mTrustAgentComponent!!.onPreferenceChangeListener = this
            mTrustAgentFeatures = findPreference(KEY_TRUST_AGENT_FEATURES) as EditTextPreference
            mTrustAgentFeatures!!.onPreferenceChangeListener = this
        }

        /**
         * Called when the fragment is visible to the user and actively running, this is where we
         * reload our UI with the current values of the Preferences. First we call our super's
         * implementation of `onResume`. We then enable or disable the `mEnableCheckbox`
         * `CheckBoxPreference` depending on whether our `mAdminActive` is true of false.
         * We call our `enableDeviceCapabilitiesArea` method with `mAdminActive` to do the
         * same with all the other widgets in our UI. If `mAdminActive` is true we call the
         * `setCameraDisabled` method of `DevicePolicyManager mDPM` to disable all cameras
         * on the device for this user if our `CheckBoxPreference mDisableCameraCheckbox` is
         * checked. We call the `setKeyguardDisabledFeatures` method of `mDPM` to disable
         * keyguard customizations, such as widgets according to the feature list created by our method
         * `createKeyguardDisabledFlag` (it reads the state of the various keyguard widgets in
         * order to build a flag bitmask enabling or disabling the feature the widget controls depending
         * on the checked/unchecked state of the widget). Finally we call our `reloadSummaries`
         * method to set the summary text of our widgets appropriately for their current state.
         */
        @Deprecated("Deprecated in Java")
        override fun onResume() {
            super.onResume()
            mEnableCheckbox!!.isChecked = mAdminActive
            enableDeviceCapabilitiesArea(mAdminActive)
            if (mAdminActive) {
                mDPM!!.setCameraDisabled((mDeviceAdminSample)!!, mDisableCameraCheckbox!!.isChecked)
                mDPM!!.setKeyguardDisabledFeatures(
                    (mDeviceAdminSample)!!,
                    createKeyguardDisabledFlag()
                )
                reloadSummaries()
            }
        }

        /**
         * Creates a bitmask of keyguard features that are disabled by the checked state of the feature's
         * `CheckBoxPreference`. The seven keyguard `CheckBoxPreference` in our UI and the
         * feature bitmask they disable are:
         *
         *  *
         * `mDisableKeyguardWidgetsCheckbox` ("Disable keyguard widgets"): KEYGUARD_DISABLE_WIDGETS_ALL
         * Disable all keyguard widgets, has no effect starting from LOLLIPOP since keyguard
         * widget is only supported on Android versions lower than 5.0.
         *
         *  *
         * `mDisableKeyguardSecureCameraCheckbox` ("Disable keyguard secure camera"):
         * KEYGUARD_DISABLE_SECURE_CAMERA Disable the camera on secure keyguard screens (e.g.
         * PIN/Pattern/Password)
         *
         *  *
         * `mDisableKeyguardNotificationCheckbox` ("Disable keyguard notifications"):
         * KEYGUARD_DISABLE_SECURE_NOTIFICATIONS Disable showing all notifications on secure
         * keyguard screens (e.g. PIN/Pattern/Password)
         *
         *  *
         * `mDisableKeyguardUnredactedCheckbox` ("Disable keyguard unredacted notifications")
         * KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS Only allow redacted notifications on secure
         * keyguard screens (e.g. PIN/Pattern/Password)
         *
         *  *
         * `mDisableKeyguardTrustAgentCheckbox` ("Disable keyguard Trust Agents")
         * KEYGUARD_DISABLE_TRUST_AGENTS Disable trust agents on secure keyguard screens
         * (e.g. PIN/Pattern/Password). By setting this flag alone, all trust agents are
         * disabled. If the admin then wants to whitelist specific features of some trust
         * agent, `setTrustAgentConfiguration` can be used in conjunction to set
         * trust-agent-specific configurations.
         *
         *  *
         * `mDisableKeyguardFingerprintCheckbox` ("Disable keyguard Fingerprint")
         * KEYGUARD_DISABLE_FINGERPRINT Disable fingerprint authentication on keyguard secure
         * screens (e.g. PIN/Pattern/Password).
         *
         *  *
         * `mDisableKeyguardRemoteInputCheckbox` ("Disable keyguard Remote Input")
         * KEYGUARD_DISABLE_REMOTE_INPUT Disable text entry into notifications on secure
         * keyguard screens (e.g. PIN/Pattern/Password).
         *
         *
         * Having created `flags` by or'ing the appropriate bitmasks into it we return `flags`
         * to the caller.
         *
         * @return bitmask of keyguard features representing the state of our keyguard widgets that
         * is appropriate for the `setKeyguardDisabledFeatures` method of `DevicePolicyManager`
         */
        fun createKeyguardDisabledFlag(): Int {
            var flags = DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE
            flags =
                flags or if (mDisableKeyguardWidgetsCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL else 0
            flags =
                flags or if (mDisableKeyguardSecureCameraCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA else 0
            flags =
                flags or if (mDisableKeyguardNotificationCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS else 0
            flags =
                flags or if (mDisableKeyguardUnredactedCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS else 0
            flags =
                flags or if (mDisableKeyguardTrustAgentCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS else 0
            flags =
                flags or if (mDisableKeyguardFingerprintCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT else 0
            flags =
                flags or if (mDisableKeyguardRemoteInputCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT else 0
            return flags
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * `onPreferenceChange` returns true, we return true having done nothing. Otherwise we
         * branch on the value of our parameter `Preference preference`:
         *
         *  *
         * `mEnableCheckbox` "Enable admin" `CheckBoxPreference` in the PreferenceScreen
         * xml/device_admin_general.xml -- We initialize `boolean value` by casting our parameter
         * `Object newValue` to boolean, and if `value` is not equal to our field
         * `mAdminActive` we branch on the value of `value`
         *
         *  *
         * true: we initialize `Intent intent` with an instance whose action is
         * ACTION_ADD_DEVICE_ADMIN (ask the user to add a new device administrator to
         * the system), add `mDeviceAdminSample` as an extra under the key EXTRA_DEVICE_ADMIN
         * (ComponentName of the administrator component), add the string with resource
         * id R.string.add_admin_extra_app_text ("Additional text explaining why this needs
         * to be added") as an extra under the key EXTRA_ADD_EXPLANATION (optional CharSequence
         * providing additional explanation for why the admin is being added) then start the
         * intent's activity running for a result. Finally we return false so that the
         * Preference will not be updated until we're really active.
         *
         *  *
         * false: we call the `removeActiveAdmin` method of `DevicePolicyManager mDPM`
         * to remove the administration component `ComponentName mDeviceAdminSample` then
         * call our method `enableDeviceCapabilitiesArea` to update the device capabilities
         * area of our UI by disabling the widgets.
         *
         *
         *
         *  *
         * `mDisableCameraCheckbox` "Disable all device cameras" `CheckBoxPreference` in the
         * PreferenceScreen xml/device_admin_general.xml -- We initialize `boolean value` by casting
         * our parameter `Object newValue` to boolean, then call the `setCameraDisabled`
         * method of `DevicePolicyManager mDPM` to disable or enable all cameras on the device for
         * this user depending on the value of `value`. Finally we call our `postReloadSummaries`
         * method to delay a call to `reloadSummaries` until after preference changes have been applied.
         *
         *  *
         * If `Preference preference` is one of:
         *
         *  *
         * `mDisableKeyguardWidgetsCheckbox` "Disable keyguard widgets" in the
         * PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mDisableKeyguardSecureCameraCheckbox` "Disable keyguard secure camera"
         * in the PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mDisableKeyguardNotificationCheckbox` "Disable keyguard notifications"
         * in the PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mDisableKeyguardUnredactedCheckbox` "Disable keyguard unredacted notifications"
         * in the PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mDisableKeyguardTrustAgentCheckbox` "Disable keyguard Trust Agents"
         * in the PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mDisableKeyguardFingerprintCheckbox` "Disable keyguard Fingerprint"
         * in the PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mDisableKeyguardRemoteInputCheckbox` "Disable keyguard Remote Input"
         * in the PreferenceScreen xml/device_admin_general.xml
         *
         *  *
         * `mTrustAgentComponent` "Enabled Component Name" in the PreferenceScreen
         * xml/device_admin_general.xml
         *
         *  *
         * `mTrustAgentFeatures` "Enabled Features (comma-separated)" in the
         * PreferenceScreen xml/device_admin_general.xml
         *
         * We call our method `postUpdateDpmDisableFeatures` to have the `DevicePolicyManager`
         * enable, disable or set all the features controlled by these `Preference` widgets. Then
         * we call our `postReloadSummaries` method to delay a call to `reloadSummaries`
         * until after the preference change has been applied.
         *
         *
         *
         * Finally we return true to the caller to have the state of the Preference updated with the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (super.onPreferenceChange(preference, newValue)) {
                return true
            }
            if (preference === mEnableCheckbox) {
                val value = newValue as Boolean
                if (value != mAdminActive) {
                    if (value) {
                        // Launch the activity to have the user enable our admin.
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample)
                        intent.putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            mActivity!!.getString(R.string.add_admin_extra_app_text)
                        )
                        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
                        // return false - don't update checkbox until we're really active
                        return false
                    } else {
                        mDPM!!.removeActiveAdmin((mDeviceAdminSample)!!)
                        enableDeviceCapabilitiesArea(false)
                        mAdminActive = false
                    }
                }
            } else if (preference === mDisableCameraCheckbox) {
                val value = newValue as Boolean
                mDPM!!.setCameraDisabled((mDeviceAdminSample)!!, value)
                // Delay update because the change is only applied after exiting this method.
                postReloadSummaries()
            } else if ((preference === mDisableKeyguardWidgetsCheckbox
                    ) || (preference === mDisableKeyguardSecureCameraCheckbox
                    ) || (preference === mDisableKeyguardNotificationCheckbox
                    ) || (preference === mDisableKeyguardUnredactedCheckbox
                    ) || (preference === mDisableKeyguardTrustAgentCheckbox
                    ) || (preference === mDisableKeyguardFingerprintCheckbox
                    ) || (preference === mDisableKeyguardRemoteInputCheckbox
                    ) || (preference === mTrustAgentComponent
                    ) || (preference === mTrustAgentFeatures)
            ) {
                postUpdateDpmDisableFeatures()
                postReloadSummaries()
            }
            return true
        }

        /**
         * Posts a runnable to the queue of the UI thread which propagates the values set in our
         * `PreferenceScreen` xml/device_admin_general.xml to `DevicePolicyManager mDPM`,
         * enabling and disabling the keyguard features controlled by the various `CheckBoxPreference`
         * widgets and transferring the text in the `EditTextPreference` widgets to their appropriate
         * destinations.
         */
        private fun postUpdateDpmDisableFeatures() {
            view!!.post {
                mDPM!!.setKeyguardDisabledFeatures(
                    (mDeviceAdminSample)!!,
                    createKeyguardDisabledFlag()
                )
                val component = mTrustAgentComponent!!.text
                if (component != null) {
                    val agent = ComponentName.unflattenFromString(component)
                    if (agent != null) {
                        val featureString = mTrustAgentFeatures!!.text
                        if (featureString != null) {
                            val bundle = PersistableBundle()
                            bundle.putStringArray(
                                "features",
                                featureString.split(",").toTypedArray()
                            )
                            mDPM!!.setTrustAgentConfiguration((mDeviceAdminSample)!!, agent, bundle)
                        }
                    } else {
                        Log.w(TAG, "Invalid component: $component")
                    }
                }
            }
        }

        /**
         * Set the summaries of all our `Preference` widgets to strings appropriate for their
         * current state. First we call our super's implementation of `reloadSummaries`. Then
         * we initialize `String cameraSummary` to the string with id R.string.camera_disabled
         * ("Device cameras disabled") if the `getCameraDisabled` method of `mDPM` returns
         * true or the string with id R.string.camera_enabled ("Device cameras enabled") if it returns
         * false, and then we set the summary of `CheckBoxPreference mDisableCameraCheckbox` to
         * `cameraSummary`.
         *
         *
         * We initialize `int disabled` with the bitmap of flags for the disabled keyguard features
         * return by the `getKeyguardDisabledFeatures` of `mDPM`. Then for the following
         * keyguard feature `CheckBoxPreference` widgets in our UI:
         *
         *  *
         * `mDisableKeyguardWidgetsCheckbox` "Disable keyguard widgets" -- If the KEYGUARD_DISABLE_WIDGETS_ALL
         * bit is set in `disable` we initialize `String keyguardWidgetSummary` to the
         * string with resource id R.string.keyguard_widgets_disabled ("Keyguard widgets disabled"),
         * if unset we initialize `String keyguardWidgetSummary` to the string with resource id
         * R.string.keyguard_widgets_enabled ("Keyguard widgets enabled"). Then we set the summary
         * of `mDisableKeyguardWidgetsCheckbox` to `keyguardWidgetSummary`.
         *
         *  *
         * `mDisableKeyguardSecureCameraCheckbox` "Disable keyguard secure camera" -- If the
         * KEYGUARD_DISABLE_SECURE_CAMERA bit is set in `disable` we initialize `String keyguardSecureCameraSummary`
         * to the string with resource id R.string.keyguard_secure_camera_disabled ("Keyguard secure camera disabled"),
         * if unset we initialize `String keyguardSecureCameraSummary` to the string with resource id
         * R.string.keyguard_secure_camera_enabled ("Keyguard secure camera enabled"). Then we set the summary
         * of `mDisableKeyguardSecureCameraCheckbox` to `keyguardSecureCameraSummary`.
         *
         *  *
         * `mDisableKeyguardNotificationCheckbox` "Disable keyguard notifications" -- If the
         * KEYGUARD_DISABLE_SECURE_NOTIFICATIONS bit is set in `disable` we initialize
         * `String keyguardSecureNotificationsSummary` to the string with resource id
         * R.string.keyguard_secure_notifications_disabled ("Keyguard notifications disabled"),
         * if unset we initialize `String keyguardSecureNotificationsSummary` to the string with
         * resource id R.string.keyguard_secure_notifications_enabled ("Keyguard notifications enabled").
         * Then we set the summary of `mDisableKeyguardNotificationCheckbox` to
         * `keyguardSecureNotificationsSummary`.
         *
         *  *
         * `mDisableKeyguardUnredactedCheckbox` "Disable keyguard unredacted notifications" -- If the
         * KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS bit is set in `disable` we initialize
         * `String keyguardUnredactedSummary` to the string with resource id R.string.keyguard_unredacted_notifications_disabled
         * ("Keyguard unredacted notifications disabled"), if unset we initialize `String keyguardUnredactedSummary`
         * to the string with resource id R.string.keyguard_unredacted_notifications_enabled ("Keyguard unredacted notifications enabled").
         * Then we set the summary of `mDisableKeyguardUnredactedCheckbox` to `keyguardUnredactedSummary`.
         *
         *  *
         * `mDisableKeyguardTrustAgentCheckbox` "Disable keyguard Trust Agents" -- If the
         * KEYGUARD_DISABLE_TRUST_AGENTS bit is set in `disable` we initialize `String keyguardEnableTrustAgentSummary`
         * to the string with resource id R.string.keyguard_trust_agents_disabled ("Keyguard Trust Agents disabled"), if unset we
         * initialize `String keyguardEnableTrustAgentSummary` to the string with resource id R.string.keyguard_trust_agents_enabled
         * ("Keyguard Trust Agents enabled"). Then we set the summary of `mDisableKeyguardTrustAgentCheckbox` to
         * `keyguardEnableTrustAgentSummary`.
         *
         *  *
         * `mDisableKeyguardFingerprintCheckbox` "Disable keyguard Fingerprint" -- If the
         * KEYGUARD_DISABLE_FINGERPRINT bit is set in `disable` we initialize
         * `String keyguardEnableFingerprintSummary` to the string with resource id
         * R.string.keyguard_fingerprint_disabled ("Keyguard Fingerprint disabled"), if unset we
         * initialize `String keyguardEnableFingerprintSummary` to the string with resource
         * id R.string.keyguard_fingerprint_enabled ("Keyguard Fingerprint enabled"). Then we set
         * the summary of `mDisableKeyguardFingerprintCheckbox` to `keyguardEnableFingerprintSummary`.
         *
         *  *
         * `mDisableKeyguardRemoteInputCheckbox` "Disable keyguard Remote Input" -- If the
         * KEYGUARD_DISABLE_REMOTE_INPUT bit is set in `disable` we initialize `String keyguardEnableRemoteInputSummary`
         * to the string with resource id R.string.keyguard_remote_input_disabled ("Keyguard Remote Input disabled"),
         * if unset we initialize `String keyguardEnableRemoteInputSummary` to the string with resource
         * id R.string.keyguard_remote_input_enabled ("Keyguard Remote Input enabled"). Then we set
         * the summary of `mDisableKeyguardRemoteInputCheckbox` to `keyguardEnableRemoteInputSummary`.
         *
         *
         * Now we deal with the two trust agent `EditTextPreference` widgets. We initialize `SharedPreferences prefs`
         * with an instance that the `getSharedPreferences` method of the `PreferenceManager` used by this fragment
         * returns for us to use. We initialize `boolean trustDisabled` to true if the KEYGUARD_DISABLE_TRUST_AGENTS
         * bit of `disabled` is set, and false if not. We initialize `String component` with the string stored in
         * `SharedPreferences prefs` under the key used by `EditTextPreference mTrustAgentComponent` defaulting
         * to null. We then set the summary of `mTrustAgentComponent` to `component`, and enable or disable it
         * depending on the value of `trustDisabled`. We initialize `String features` with the string stored in
         * `SharedPreferences prefs` under the key used by `EditTextPreference mTrustAgentFeatures` defaulting
         * to null. We then set the summary of `mTrustAgentFeatures` to `features`, and enable or disable it
         * depending on the value of `trustDisabled`.
         */
        override fun reloadSummaries() {
            super.reloadSummaries()
            val cameraSummary =
                getString(if (mDPM!!.getCameraDisabled(mDeviceAdminSample)) R.string.camera_disabled else R.string.camera_enabled)
            mDisableCameraCheckbox!!.summary = cameraSummary
            val disabled = mDPM!!.getKeyguardDisabledFeatures(mDeviceAdminSample)
            val keyguardWidgetSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL) != 0) R.string.keyguard_widgets_disabled else R.string.keyguard_widgets_enabled
            )
            mDisableKeyguardWidgetsCheckbox!!.summary = keyguardWidgetSummary
            val keyguardSecureCameraSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0) R.string.keyguard_secure_camera_disabled else R.string.keyguard_secure_camera_enabled
            )
            mDisableKeyguardSecureCameraCheckbox!!.summary = keyguardSecureCameraSummary
            val keyguardSecureNotificationsSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS) != 0) R.string.keyguard_secure_notifications_disabled else R.string.keyguard_secure_notifications_enabled
            )
            mDisableKeyguardNotificationCheckbox!!.summary = keyguardSecureNotificationsSummary
            val keyguardUnredactedSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS) != 0) R.string.keyguard_unredacted_notifications_disabled else R.string.keyguard_unredacted_notifications_enabled
            )
            mDisableKeyguardUnredactedCheckbox!!.summary = keyguardUnredactedSummary
            val keyguardEnableTrustAgentSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0) R.string.keyguard_trust_agents_disabled else R.string.keyguard_trust_agents_enabled
            )
            mDisableKeyguardTrustAgentCheckbox!!.summary = keyguardEnableTrustAgentSummary
            val keyguardEnableFingerprintSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT) != 0) R.string.keyguard_fingerprint_disabled else R.string.keyguard_fingerprint_enabled
            )
            mDisableKeyguardFingerprintCheckbox!!.summary = keyguardEnableFingerprintSummary
            val keyguardEnableRemoteInputSummary = getString(
                if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT) != 0) R.string.keyguard_remote_input_disabled else R.string.keyguard_remote_input_enabled
            )
            mDisableKeyguardRemoteInputCheckbox!!.summary = keyguardEnableRemoteInputSummary
            val prefs = preferenceManager.sharedPreferences
            val trustDisabled =
                (disabled and DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0
            val component = prefs.getString(mTrustAgentComponent!!.key, null)
            mTrustAgentComponent!!.summary = component
            mTrustAgentComponent!!.isEnabled = trustDisabled
            val features = prefs.getString(mTrustAgentFeatures!!.key, null)
            mTrustAgentFeatures!!.summary = features
            mTrustAgentFeatures!!.isEnabled = trustDisabled
        }

        /** Updates the device capabilities area (dis/enabling) as the admin is (de)activated
         *
         * @param enabled if true all the device capability `CheckBoxPreference` widgets are
         * enabled, if false they are disabled.
         */
        private fun enableDeviceCapabilitiesArea(enabled: Boolean) {
            mDisableCameraCheckbox!!.isEnabled = enabled
            mDisableKeyguardWidgetsCheckbox!!.isEnabled = enabled
            mDisableKeyguardSecureCameraCheckbox!!.isEnabled = enabled
            mDisableKeyguardNotificationCheckbox!!.isEnabled = enabled
            mDisableKeyguardUnredactedCheckbox!!.isEnabled = enabled
            mDisableKeyguardTrustAgentCheckbox!!.isEnabled = enabled
            mTrustAgentComponent!!.isEnabled = enabled
            mTrustAgentFeatures!!.isEnabled = enabled
        }
    }

    /**
     * PreferenceFragment for "password quality" preferences.
     */
    class QualityFragment : AdminSampleFragment(), Preference.OnPreferenceChangeListener {
        // UI elements
        /**
         * `PreferenceCategory` "Password quality" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_category_quality"
         */
        private var mQualityCategory: PreferenceCategory? = null

        /**
         * `ListPreference` "Password quality" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_quality"
         */
        private var mPasswordQuality: ListPreference? = null

        /**
         * `EditTextPreference` "Minimum length" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_length"
         */
        private var mMinLength: EditTextPreference? = null

        /**
         * `EditTextPreference` "Minimum letters" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_letters"
         */
        private var mMinLetters: EditTextPreference? = null

        /**
         * `EditTextPreference` "Minimum numeric" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_numeric"
         */
        private var mMinNumeric: EditTextPreference? = null

        /**
         * `EditTextPreference` "Minimum lower case" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_lower_case"
         */
        private var mMinLowerCase: EditTextPreference? = null

        /**
         * `EditTextPreference` "Minimum upper case" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_upper_case"
         */
        private var mMinUpperCase: EditTextPreference? = null

        /**
         * `EditTextPreference` "Minimum symbols" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_symbols"
         */
        private var mMinSymbols: EditTextPreference? = null

        /**
         * `EditTextPreference` "Minimum non-letter" in the `PreferenceScreen`
         * xml/device_admin_quality.xml with the key "key_minimum_non_letter"
         */
        private var mMinNonLetter: EditTextPreference? = null

        /**
         * Called to do initial creation of a `PreferenceFragment`. First we call our super's
         * implementation of `onCreate`, then we call the `addPreferencesFromResource`
         * method to inflate our XML resource file R.xml.device_admin_quality and add its preference
         * hierarchy to the current preference hierarchy. We then initialize the fields we use to
         * access the various [Preference] widgets in our UI by finding them using the android:key
         * strings they are identified by in the xml/device_admin_general.xml file. After doing so we
         * set their `OnPreferenceChangeListener` to this. Finally we call the `setEntryValues`
         * method of `ListPreference mPasswordQuality` to set the values to save for the preferences
         * when an entry is selected to our array `String[] mPasswordQualityValueStrings`.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.device_admin_quality)
            mQualityCategory = findPreference(KEY_CATEGORY_QUALITY) as PreferenceCategory
            mPasswordQuality = findPreference(KEY_QUALITY) as ListPreference
            mMinLength = findPreference(KEY_MIN_LENGTH) as EditTextPreference
            mMinLetters = findPreference(KEY_MIN_LETTERS) as EditTextPreference
            mMinNumeric = findPreference(KEY_MIN_NUMERIC) as EditTextPreference
            mMinLowerCase = findPreference(KEY_MIN_LOWER_CASE) as EditTextPreference
            mMinUpperCase = findPreference(KEY_MIN_UPPER_CASE) as EditTextPreference
            mMinSymbols = findPreference(KEY_MIN_SYMBOLS) as EditTextPreference
            mMinNonLetter = findPreference(KEY_MIN_NON_LETTER) as EditTextPreference
            mPasswordQuality!!.onPreferenceChangeListener = this
            mMinLength!!.onPreferenceChangeListener = this
            mMinLetters!!.onPreferenceChangeListener = this
            mMinNumeric!!.onPreferenceChangeListener = this
            mMinLowerCase!!.onPreferenceChangeListener = this
            mMinUpperCase!!.onPreferenceChangeListener = this
            mMinSymbols!!.onPreferenceChangeListener = this
            mMinNonLetter!!.onPreferenceChangeListener = this

            // Finish setup of the quality dropdown
            mPasswordQuality!!.entryValues = mPasswordQualityValueStrings
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of `onResume`. Then we enable or disable our UI element
         * `PreferenceCategory mQualityCategory` depending on the value of `mAdminActive`,
         * enabling it if we are in an active admin capacity and disabling it if we are not.
         */
        @Deprecated("Deprecated in Java")
        override fun onResume() {
            super.onResume()
            mQualityCategory!!.isEnabled = mAdminActive
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting.
         */
        override fun reloadSummaries() {
            super.reloadSummaries()
            // Show numeric settings for each policy API
            var local: Int = mDPM!!.getPasswordQuality(mDeviceAdminSample)
            var global: Int = mDPM!!.getPasswordQuality(null)
            mPasswordQuality!!.summary =
                localGlobalSummary(qualityValueToString(local), qualityValueToString(global))
            local = mDPM!!.getPasswordMinimumLength(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumLength(null)
            mMinLength!!.summary = localGlobalSummary(local, global)
            local = mDPM!!.getPasswordMinimumLetters(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumLetters(null)
            mMinLetters!!.summary = localGlobalSummary(local, global)
            local = mDPM!!.getPasswordMinimumNumeric(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumNumeric(null)
            mMinNumeric!!.summary = localGlobalSummary(local, global)
            local = mDPM!!.getPasswordMinimumLowerCase(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumLowerCase(null)
            mMinLowerCase!!.summary = localGlobalSummary(local, global)
            local = mDPM!!.getPasswordMinimumUpperCase(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumUpperCase(null)
            mMinUpperCase!!.summary = localGlobalSummary(local, global)
            local = mDPM!!.getPasswordMinimumSymbols(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumSymbols(null)
            mMinSymbols!!.summary = localGlobalSummary(local, global)
            local = mDPM!!.getPasswordMinimumNonLetter(mDeviceAdminSample)
            global = mDPM!!.getPasswordMinimumNonLetter(null)
            mMinNonLetter!!.summary = localGlobalSummary(local, global)
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * `onPreferenceChange` returns true, we return true having done nothing. Otherwise we
         * initialize `String valueString` by casting our parameter `Object newValue` to
         * string, and if it is empty we return false so that the preference is not updated. Next we
         * initialize `int value` to 0, and wrapped in a try block intended to catch NumberFormatException
         * in order to toast a "Bad value" error message we set `value` to the integer value of
         * `valueString`. Now we branch depending on which of our preference widgets match our
         * parameter `Preference preference`:
         *
         *  *
         * `mPasswordQuality` "Password quality" ListPreference -- we call the `setPasswordQuality`
         * method of `DevicePolicyManager mDPM` to set the password restrictions we are imposing
         * to `value`.
         *
         *  *
         * `mMinLength` "Minimum length" EditTextPreference -- we call the `setPasswordMinimumLength`
         * method of `DevicePolicyManager mDPM` to set the minimum allowed password length to `value`.
         *
         *  *
         * `mMinLetters` "Minimum letters" EditTextPreference -- we call the `setPasswordMinimumLetters`
         * method of `DevicePolicyManager mDPM` to set the minimum number of letters required in the password
         * to `value`.
         *
         *  *
         * `mMinNumeric` "Minimum numeric" EditTextPreference -- we call the `setPasswordMinimumNumeric`
         * method of `DevicePolicyManager mDPM` to set the minimum number of numerical digits required in the
         * password to `value`.
         *
         *  *
         * `mMinLowerCase` "Minimum lower case" EditTextPreference -- we call the `setPasswordMinimumLowerCase`
         * method of `DevicePolicyManager mDPM` to set the minimum number of lower case letters required in the
         * password to `value`.
         *
         *  *
         * `mMinUpperCase` "Minimum upper case" EditTextPreference -- we call the `setPasswordMinimumUpperCase`
         * method of `DevicePolicyManager mDPM` to set the minimum number of upper case letters required
         * in the password to `value`.
         *
         *  *
         * `mMinSymbols` "Minimum symbols" EditTextPreference -- we call the `setPasswordMinimumSymbols`
         * method of `DevicePolicyManager mDPM` to set the minimum number of symbols required in the password
         * to `value`.
         *
         *  *
         * `mMinNonLetter` "Minimum non-letter" EditTextPreference -- we call the `setPasswordMinimumNonLetter`
         * method of `DevicePolicyManager mDPM` to set the minimum number of non-letters required in the password
         * to `value`.
         *
         *
         * We then call our method `postReloadSummaries` to post a call to `reloadSummaries`
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (super.onPreferenceChange(preference, newValue)) {
                return true
            }
            val valueString = newValue as String
            if (TextUtils.isEmpty(valueString)) {
                return false
            }
            var value = 0
            try {
                value = valueString.toInt()
            } catch (nfe: NumberFormatException) {
                val warning = mActivity!!.getString(R.string.number_format_warning, valueString)
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show()
            }
            when {
                preference === mPasswordQuality -> {
                    mDPM!!.setPasswordQuality((mDeviceAdminSample)!!, value)
                }
                preference === mMinLength -> {
                    mDPM!!.setPasswordMinimumLength((mDeviceAdminSample)!!, value)
                }
                preference === mMinLetters -> {
                    mDPM!!.setPasswordMinimumLetters((mDeviceAdminSample)!!, value)
                }
                preference === mMinNumeric -> {
                    mDPM!!.setPasswordMinimumNumeric((mDeviceAdminSample)!!, value)
                }
                preference === mMinLowerCase -> {
                    mDPM!!.setPasswordMinimumLowerCase((mDeviceAdminSample)!!, value)
                }
                preference === mMinUpperCase -> {
                    mDPM!!.setPasswordMinimumUpperCase((mDeviceAdminSample)!!, value)
                }
                preference === mMinSymbols -> {
                    mDPM!!.setPasswordMinimumSymbols((mDeviceAdminSample)!!, value)
                }
                preference === mMinNonLetter -> {
                    mDPM!!.setPasswordMinimumNonLetter((mDeviceAdminSample)!!, value)
                }
                // Delay update because the change is only applied after exiting this method.
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries()
            return true
        }

        /**
         * Translates the `DevicePolicyManager` integer constant that is used to specify a password
         * quality into a string to display. We loop through our field `int[] mPasswordQualityValues`
         * using `int i` as the index, and if `mPasswordQualityValues[ i ]` is equal to our
         * parameter `int quality` we initialize `String[] qualities` with the resource
         * string array with id R.array.password_qualities and return `qualities[ i ]` to the caller.
         * If we do not find `quality` in the `mPasswordQualityValues` array we format the
         * radix 16 string value of `quality` into a string which we return to the caller.
         *
         * @param quality `DevicePolicyManager` integer constant that is used to specify a password
         * quality that is used by `getPasswordQuality` and `setPasswordQuality`
         * @return a string describing the password quality corresponding to our parameter `int quality`
         */
        private fun qualityValueToString(quality: Int): String {
            for (i in mPasswordQualityValues.indices) {
                if (mPasswordQualityValues[i] == quality) {
                    val qualities = mActivity!!.resources.getStringArray(R.array.password_qualities)
                    return qualities[i]
                }
            }
            return "(0x" + quality.toString(16) + ")"
        }

        companion object {
            /**
             * Password quality values. This list must match the list found in res/values/arrays.xml
             * for the "password_qualities" string-array.
             */
            val mPasswordQualityValues = intArrayOf(
                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
                DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
                DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
            )

            /**
             * Password quality values (as strings, for the ListPreference entryValues). This list must
             * match the list found in res/values/arrays.xml for the "password_qualities" string-array.
             */
            val mPasswordQualityValueStrings = arrayOf(
                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED.toString(),
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING.toString(),
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC.toString(),
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX.toString(),
                DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC.toString(),
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC.toString(),
                DevicePolicyManager.PASSWORD_QUALITY_COMPLEX.toString()
            )
        }
    }

    /**
     * PreferenceFragment for "password expiration" preferences, uses xml/device_admin_expiration.xml
     * as its `PreferenceScreen`.
     */
    class ExpirationFragment : AdminSampleFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
        /**
         * `PreferenceCategory` "Password history / Expiration" with the key key_category_expiration
         * in the xml/device_admin_expiration.xml `PreferenceScreen`
         */
        private var mExpirationCategory: PreferenceCategory? = null

        /**
         * `EditTextPreference` "Password history depth" with the key key_history
         * in the xml/device_admin_expiration.xml `PreferenceScreen`
         */
        private var mHistory: EditTextPreference? = null

        /**
         * `EditTextPreference` "Password expiration timeout (minutes)" with the key key_expiration_timeout
         * in the xml/device_admin_expiration.xml `PreferenceScreen`
         */
        private var mExpirationTimeout: EditTextPreference? = null

        /**
         * `PreferenceScreen` "Password expiration status" with the key key_expiration_status
         * in the xml/device_admin_expiration.xml `PreferenceScreen`
         */
        private var mExpirationStatus: PreferenceScreen? = null

        /**
         * Called to do initial creation of a fragment. First we call our super's implementation of
         * `onCreate`, then we call the `addPreferencesFromResource` method to inflate
         * the XML resource R.xml.device_admin_expiration and add its preference hierarchy to the
         * current preference hierarchy. We initialize `PreferenceCategory mExpirationCategory`
         * by finding the preference with key KEY_CATEGORY_EXPIRATION ("key_category_expiration"),
         * initialize `EditTextPreference mHistory` by finding the preference with key KEY_HISTORY
         * ("key_history"), initialize `EditTextPreference mExpirationTimeout` by finding the
         * preference with key KEY_EXPIRATION_TIMEOUT ("key_expiration_timeout"), and initialize
         * `PreferenceScreen mExpirationStatus` by finding the preference with key KEY_EXPIRATION_STATUS
         * ("key_expiration_status"). We then set the `OnPreferenceChangeListener` of `mHistory`,
         * `mExpirationTimeout` and `mExpirationStatus` to this.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.device_admin_expiration)
            mExpirationCategory = findPreference(KEY_CATEGORY_EXPIRATION) as PreferenceCategory
            mHistory = findPreference(KEY_HISTORY) as EditTextPreference
            mExpirationTimeout = findPreference(KEY_EXPIRATION_TIMEOUT) as EditTextPreference
            mExpirationStatus = findPreference(KEY_EXPIRATION_STATUS) as PreferenceScreen
            mHistory!!.onPreferenceChangeListener = this
            mExpirationTimeout!!.onPreferenceChangeListener = this
            mExpirationStatus!!.onPreferenceClickListener = this
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of `onResume`, then we call the `setEnabled` method of
         * `PreferenceCategory mExpirationCategory` to enable the preference if `mAdminActive`
         * is true or disable it if it is false.
         */
        @Deprecated("Deprecated in Java")
        override fun onResume() {
            super.onResume()
            mExpirationCategory!!.isEnabled = mAdminActive
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting. First
         * we call our super's implementation of `reloadSummaries`. Then we declare `local`
         * and `global`, set local to the local length of the password history returned by the
         * `getPasswordHistoryLength` method of `DevicePolicyManager mDPM` and `global`
         * to the global value it returns. We then set the summary of `EditTextPreference mHistory`
         * to the string that our method `localGlobalSummary` constructs from `local` and
         * `global`. We declare `long localLong` and `long globalLong`, set `localLong`
         * to the local password expiration timeout returned by the `getPasswordExpirationTimeout`
         * method of `DevicePolicyManager mDPM` and `globalLong` to the global value it returns.
         * We then set the summary of `EditTextPreference mExpirationTimeout` to the string that our
         * method `localGlobalSummary` constructs from `localLong` divided by the number
         * of milliseconds in a minute and `globalLong` divided by the number of milliseconds
         * in a minute. We initialize `String expirationStatus` with the string formed by our
         * `getExpirationStatus` method from the current password expiration time for both local
         * and global profiles, then set the summary of `PreferenceScreen mExpirationStatus`
         * to it.
         */
        override fun reloadSummaries() {
            super.reloadSummaries()
            val local: Int = mDPM!!.getPasswordHistoryLength(mDeviceAdminSample)
            val global: Int = mDPM!!.getPasswordHistoryLength(null)
            mHistory!!.summary = localGlobalSummary(local, global)
            val localLong: Long = mDPM!!.getPasswordExpirationTimeout(mDeviceAdminSample)
            val globalLong: Long = mDPM!!.getPasswordExpirationTimeout(null)
            mExpirationTimeout!!.summary = localGlobalSummary(
                localLong / MS_PER_MINUTE, globalLong / MS_PER_MINUTE
            )
            val expirationStatus = expirationStatus
            mExpirationStatus!!.summary = expirationStatus
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * `onPreferenceChange` returns true, we return true having done nothing. Otherwise we
         * initialize `String valueString` by casting our parameter `Object newValue` to
         * string and if it is empty we return false so the preference is not updated. Next we initialize
         * `int value` to 0, and wrapped in a try block intended to catch NumberFormatException
         * in order to toast a "Bad value" error message we set `value` to the integer value of
         * `valueString`. We then branch on the value of our parameter `Preference preference`:
         *
         *  *
         * `EditTextPreference mHistory` "Password history depth" we call the `setPasswordHistoryLength`
         * method of `DevicePolicyManager mDPM` to set the length of the password history
         * to `value`.
         *
         *  *
         * `EditTextPreference mExpirationTimeout` "Password expiration timeout (minutes)"
         * we call the `setPasswordExpirationTimeout` method of `DevicePolicyManager mDPM`
         * to set the password expiration timeout to `value`.
         *
         *
         * We then call our method `postReloadSummaries` to post a call to `reloadSummaries`
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (super.onPreferenceChange(preference, newValue)) {
                return true
            }
            val valueString = newValue as String
            if (TextUtils.isEmpty(valueString)) {
                return false
            }
            var value = 0
            try {
                value = valueString.toInt()
            } catch (nfe: NumberFormatException) {
                val warning = mActivity!!.getString(R.string.number_format_warning, valueString)
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show()
            }
            if (preference === mHistory) {
                mDPM!!.setPasswordHistoryLength((mDeviceAdminSample)!!, value)
            } else if (preference === mExpirationTimeout) {
                mDPM!!.setPasswordExpirationTimeout((mDeviceAdminSample)!!, value * MS_PER_MINUTE)
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries()
            return true
        }

        /**
         * Called when a Preference has been clicked. If our super's implementation of `onPreferenceClick`
         * returns true, we return true having done nothing. If our parameter `Preference preference` is
         * `PreferenceScreen mExpirationStatus` we initialize `String expirationStatus` with the
         * string formed by our `getExpirationStatus` method from the current password expiration time
         * for both local and global profiles, then set the summary of `PreferenceScreen mExpirationStatus`
         * to it and return true to the caller having consumed the click. If `preference` is not
         * equal to `mExpirationStatus` we return false to the caller.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference): Boolean {
            if (super.onPreferenceClick(preference)) {
                return true
            }
            if (preference === mExpirationStatus) {
                val expirationStatus = expirationStatus
                mExpirationStatus!!.summary = expirationStatus
                return true
            }
            return false
        }// expirations are absolute;  convert to relative for display

        // local expiration

        // global expiration

        /**
         * Create a summary string describing the expiration status for the sample app, as well as
         * the global (aggregate) status. We initialize `long localExpiration` with the local
         * password expiration time returned by the `getPasswordExpiration` method of
         * `DevicePolicyManager mDPM` and `long globalExpiration` to the global value,
         * then initialize `long now` with the current time in milliseconds. We declare
         * `String local` and if `localExpiration` is zero we set it to the string with
         * resource id R.string.expiration_status_none ("None"), otherwise we subtract `now`
         * from `localExpiration`. We initialize `String dms` to the string returned by our
         * method `timeToDaysMinutesSeconds` creates from the absolute value of `localExpiration`.
         * If `localExpiration` is greater than or equal to 0 we set `local` to the formatted
         * string created from `dms` using the format string with resource id R.string.expiration_status_future
         * ("Password will expire %1$s from now") otherwise we set it to the formatted string created from
         * `dms` using the format string with resource id R.string.expiration_status_past
         * ("Password expired %1$s ago").
         *
         *
         * We then declare `String global` and if `globalExpiration` is 0 we set it to the
         * string with resource id R.string.expiration_status_none ("None"), otherwise we subtract
         * `now` from `globalExpiration`. We initialize `String dms` to the string
         * returned by our method `timeToDaysMinutesSeconds` creates from the absolute value of
         * `globalExpiration`. If `globalExpiration` is greater than or equal to 0 we set
         * `global` to the formatted string created from `dms` using the format string with
         * resource id R.string.expiration_status_future ("Password will expire %1$s from now") otherwise
         * we set it to the formatted string created from `dms` using the format string with resource
         * id R.string.expiration_status_past ("Password expired %1$s ago"). Finally we return the formatted
         * string created from `local` and `global` using the format string with resource id
         * R.string.status_local_global ("Local=%1$s / Global=%2$s").
         */
        private val expirationStatus: String
            get() {
                // expirations are absolute;  convert to relative for display
                var localExpiration = mDPM!!.getPasswordExpiration(mDeviceAdminSample)
                var globalExpiration = mDPM!!.getPasswordExpiration(null)
                val now = System.currentTimeMillis()

                // local expiration
                val local: String
                if (localExpiration == 0L) {
                    local = mActivity!!.getString(R.string.expiration_status_none)
                } else {
                    localExpiration -= now
                    val dms = timeToDaysMinutesSeconds(mActivity, abs(localExpiration))
                    local = if (localExpiration >= 0) {
                        mActivity!!.getString(R.string.expiration_status_future, dms)
                    } else {
                        mActivity!!.getString(R.string.expiration_status_past, dms)
                    }
                }

                // global expiration
                val global: String
                if (globalExpiration == 0L) {
                    global = mActivity!!.getString(R.string.expiration_status_none)
                } else {
                    globalExpiration -= now
                    val dms = timeToDaysMinutesSeconds(mActivity, abs(globalExpiration))
                    global = if (globalExpiration >= 0) {
                        mActivity!!.getString(R.string.expiration_status_future, dms)
                    } else {
                        mActivity!!.getString(R.string.expiration_status_past, dms)
                    }
                }
                return mActivity!!.getString(R.string.status_local_global, local, global)
            }
    }

    /**
     * PreferenceFragment for "lock screen & wipe" preferences, uses the xml/device_admin_lock_wipe.xml
     * file as its `PreferenceScreen`.
     */
    class LockWipeFragment : AdminSampleFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
        /**
         * `PreferenceCategory` "Lock screen / Wipe" with the key key_category_lock_wipe in the
         * xml/device_admin_lock_wipe.xml `PreferenceScreen`
         */
        private var mLockWipeCategory: PreferenceCategory? = null

        /**
         * `EditTextPreference` "Max time to screen lock (minutes)" with the key key_max_time_screen_lock
         * in the xml/device_admin_lock_wipe.xml `PreferenceScreen`
         */
        private var mMaxTimeScreenLock: EditTextPreference? = null

        /**
         * `EditTextPreference` "Max password failures for local wipe" with the key key_max_fails_before_wipe
         * in the xml/device_admin_lock_wipe.xml `PreferenceScreen`
         */
        private var mMaxFailures: EditTextPreference? = null

        /**
         * `PreferenceScreen` "Lock screen now" with the key key_lock_screen in the
         * xml/device_admin_lock_wipe.xml `PreferenceScreen`
         */
        private var mLockScreen: PreferenceScreen? = null

        /**
         * `PreferenceScreen` "Wipe data" with the key key_wipe_data in the
         * xml/device_admin_lock_wipe.xml `PreferenceScreen`
         */
        private var mWipeData: PreferenceScreen? = null

        /**
         * `PreferenceScreen` "Wipe all data" with the key key_wipe_data_all in the
         * xml/device_admin_lock_wipe.xml `PreferenceScreen`
         */
        private var mWipeAppData: PreferenceScreen? = null

        /**
         * Called to do initial creation of a `PreferenceFragment`. First we call our super's
         * implementation of `onCreate`, then we call the `addPreferencesFromResource`
         * method to inflate our XML resource file R.xml.device_admin_lock_wipe and add its preference
         * hierarchy to the current preference hierarchy. We then initialize the fields we use to
         * access the various [Preference] widgets in our UI by finding them using the android:key
         * strings they are identified by in the xml/device_admin_lock_wipe.xml file. After doing so we
         * set their `OnPreferenceChangeListener` to this.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.device_admin_lock_wipe)
            mLockWipeCategory = findPreference(KEY_CATEGORY_LOCK_WIPE) as PreferenceCategory
            mMaxTimeScreenLock = findPreference(KEY_MAX_TIME_SCREEN_LOCK) as EditTextPreference
            mMaxFailures = findPreference(KEY_MAX_FAILS_BEFORE_WIPE) as EditTextPreference
            mLockScreen = findPreference(KEY_LOCK_SCREEN) as PreferenceScreen
            mWipeData = findPreference(KEY_WIPE_DATA) as PreferenceScreen
            mWipeAppData = findPreference(KEY_WIP_DATA_ALL) as PreferenceScreen
            mMaxTimeScreenLock!!.onPreferenceChangeListener = this
            mMaxFailures!!.onPreferenceChangeListener = this
            mLockScreen!!.onPreferenceClickListener = this
            mWipeData!!.onPreferenceClickListener = this
            mWipeAppData!!.onPreferenceClickListener = this
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of `onResume`. Then we enable or disable our UI element
         * `PreferenceCategory mLockWipeCategory` depending on the value of `mAdminActive`,
         * enabling it if we are in an active admin capacity and disabling it if we are not.
         */
        @Deprecated("Deprecated in Java")
        override fun onResume() {
            super.onResume()
            mLockWipeCategory!!.isEnabled = mAdminActive
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting. First
         * we call our super's implementation of `reloadSummaries`. We then declare `long localLong`
         * and `long globalLong`. Then we set `localLong` to the current local maximum time to unlock
         * returned by the `getMaximumTimeToLock` method of `DevicePolicyManager mDPM` and
         * `globalLong` to the global value returned. We then set the summary of `EditTextPreference mMaxTimeScreenLock`
         * to the string that our method `localGlobalSummary` constructs from `localLong`
         * divided by the number of milliseconds in a minute and `globalLong` divided by the number
         * of milliseconds in a minute. Then we declare `int local` and `int global`, set `local`
         * to the local current maximum number of login attempts that are allowed before the device or
         * profile is wiped returned by the `getMaximumFailedPasswordsForWipe` method of
         * `DevicePolicyManager mDPM` and `global` to the global value it returns. We then
         * set the summary of `EditTextPreference mMaxFailures` to the string that our method
         * `localGlobalSummary` constructs from `local` and `global`.
         */
        override fun reloadSummaries() {
            super.reloadSummaries()
            val localLong: Long = mDPM!!.getMaximumTimeToLock(mDeviceAdminSample)
            val globalLong: Long = mDPM!!.getMaximumTimeToLock(null)
            mMaxTimeScreenLock!!.summary = localGlobalSummary(
                localLong / MS_PER_MINUTE, globalLong / MS_PER_MINUTE
            )
            val local: Int = mDPM!!.getMaximumFailedPasswordsForWipe(mDeviceAdminSample)
            val global: Int = mDPM!!.getMaximumFailedPasswordsForWipe(null)
            mMaxFailures!!.summary = localGlobalSummary(local, global)
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * `onPreferenceChange` returns true, we return true having done nothing. Otherwise we
         * initialize `String valueString` by casting our parameter `Object newValue` to
         * string, and if it is empty we return false so that the preference is not updated. Next we
         * initialize `int value` to 0, and wrapped in a try block intended to catch NumberFormatException
         * in order to toast a "Bad value" error message we set `value` to the integer value of
         * `valueString`. Now we branch depending on which of our preference widgets match our
         * parameter `Preference preference`:
         *
         *  *
         * `EditTextPreference mMaxTimeScreenLock` "Max time to screen lock (minutes)"
         * we call the `setMaximumTimeToLock` method of `DevicePolicyManager mDPM`
         * to set the maximum time for user activity until the device will lock to `value`
         * times the number of milliseconds in a minute.
         *
         *  *
         * `EditTextPreference mMaxFailures` "Max password failures for local wipe" we first
         * call our method `alertIfMonkey` to make sure we are not being run by an automated
         * test, toasting the message "You can't wipe my data, you are a monkey!" if so and returning
         * true without doing more if is returns true. If it returns false we call the
         * `setMaximumFailedPasswordsForWipe` method of `DevicePolicyManager mDPM`
         * to set the number of failed password attempts at which point the device or profile will
         * be wiped to `value`.
         *
         *
         * We then call our method `postReloadSummaries` to post a call to `reloadSummaries`
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (super.onPreferenceChange(preference, newValue)) {
                return true
            }
            val valueString = newValue as String
            if (TextUtils.isEmpty(valueString)) {
                return false
            }
            var value = 0
            try {
                value = valueString.toInt()
            } catch (nfe: NumberFormatException) {
                val warning = mActivity!!.getString(R.string.number_format_warning, valueString)
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show()
            }
            if (preference === mMaxTimeScreenLock) {
                mDPM!!.setMaximumTimeToLock((mDeviceAdminSample)!!, value * MS_PER_MINUTE)
            } else if (preference === mMaxFailures) {
                if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                    return true
                }
                mDPM!!.setMaximumFailedPasswordsForWipe((mDeviceAdminSample)!!, value)
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries()
            return true
        }

        /**
         * Called when a Preference has been clicked. If our super's implementation of `onPreferenceClick`
         * returns true, we return true having done nothing. If our parameter `Preference preference` is
         * `PreferenceScreen mLockScreen` we call our method `alertIfMonkey` to check if we
         * are being run by an automated test, toasting the message "You can't lock my screen, you are a monkey!"
         * if so and if it returns true we return true having done nothing. If `alertIfMonkey` returned
         * false (indicating we are being run by a user) we call the `lockNow` method of
         * `DevicePolicyManager mDPM` to make the device lock immediately. If our parameter
         * `Preference preference` is `PreferenceScreen mWipeData` or `PreferenceScreen mWipeAppData`
         * we call our method `alertIfMonkey` to check if we are being run by an automated test,
         * toasting the message "You can't wipe my data, you are a monkey!" if so and if it returns true
         * we return true having done nothing. If `alertIfMonkey` returned false (indicating we are
         * being run by a user) we call our method `promptForRealDeviceWipe` with true if `preference`
         * is equal to `mWipeAppData` or false if it is `mWipeData`. Upon return from
         * `promptForRealDeviceWipe` we return true consuming the click. For any other value of
         * `preference` we return false indicating that we did not handle the click.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.         *
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference): Boolean {
            if (super.onPreferenceClick(preference)) {
                return true
            }
            if (preference === mLockScreen) {
                if (alertIfMonkey(mActivity, R.string.monkey_lock_screen)) {
                    return true
                }
                mDPM!!.lockNow()
                return true
            } else if (preference === mWipeData || preference === mWipeAppData) {
                if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                    return true
                }
                promptForRealDeviceWipe(preference === mWipeAppData)
                return true
            }
            return false
        }

        /**
         * Calls the `DevicePolicyManager mDPM` `wipeData` method to wipe data on the
         * device after displaying two warning alert dialogs. Wiping data is real, so we don't want
         * it to be easy, so we show two alerts before wiping. We initialize `DeviceAdminSample activity`
         * with the value of our field `DeviceAdminSample mActivity`, and initialize `AlertDialog.Builder builder`
         * with a new instance. We set the message of `builder` to the string with resource id
         * R.string.wipe_warning_first ("This will erase all of your data.  Are you sure?") and set the
         * text of its positive button to the string with resource id R.string.wipe_warning_first_ok
         * ("Yes") and its `OnClickListener` to an anonymous class which builds and launches a
         * second alert dialog when the positive button is clicked. We set the text of its negative button
         * to the string with resource id R.string.wipe_warning_first_no ("No") then show this first
         * alert dialog. The `OnClickListener` of the positive button of the second alert dialog
         * that is displayed when the positive button of the first alert dialog is clicked will really
         * call the `DevicePolicyManager mDPM` `wipeData` method to wipe data on the device
         * passing the flag WIPE_EXTERNAL_STORAGE to wipe the external data also if our parameter
         * `boolean wipeAllData` is true.
         *
         * @param wipeAllData if true, we pass the WIPE_EXTERNAL_STORAGE flag (also erase the device's
         * external storage, such as SD cards) to the `wipeData` method of
         * `DevicePolicyManager mDPM`, if false we pass 0.
         */
        private fun promptForRealDeviceWipe(wipeAllData: Boolean) {
            val activity = mActivity
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.wipe_warning_first)
            builder.setPositiveButton(R.string.wipe_warning_first_ok) { _, _ ->

                /**
                 * This method will be invoked when the positive button in the dialog is clicked. We initialize
                 * `AlertDialog.Builder builder` with a new instance. If the `wipeAllData`
                 * parameter of the method `promptForRealDeviceWipe` was true we set the message
                 * of `builder` to the string with resource id R.string.wipe_warning_second_full
                 * ("This is not a test. This WILL erase all of your data, including external storage!
                 * Are you really absolutely sure?"), if it was false we set the  message to the string
                 * with resource id R.string.wipe_warning_second ("This is not a test. This WILL erase
                 * all of your data! Are you really absolutely sure?"). We then set the text of the
                 * positive button to the string with resource id R.string.wipe_warning_second_ok
                 * ("BOOM!") and its `OnClickListener` to an anonymous class which calls the
                 * `wipeData` method of `DevicePolicyManager mDPM` with the flag WIPE_EXTERNAL_STORAGE
                 * if `wipeAllData` was true, or 0 if it was false. We then set the text of the
                 * negative button to the string with resource id R.string.wipe_warning_second_no ("Oops, run away!")
                 * and show the dialog of `builder`.
                 *
                 *  dialog the dialog that received the click
                 *  which the button that was clicked, BUTTON_POSITIVE in our case
                 */
                val builderLocal = AlertDialog.Builder(activity)
                if (wipeAllData) {
                    builderLocal.setMessage(R.string.wipe_warning_second_full)
                } else {
                    builderLocal.setMessage(R.string.wipe_warning_second)
                }
                /**
                 * This method will be invoked when the positive button in the dialog is clicked.
                 * We initialize `boolean stillActive` with the value returned by the
                 * `isActiveAdmin` method of `DeviceAdminSample mActivity` (this
                 * helper method just returns the value returned by the `isAdminActive`
                 * method of `DevicePolicyManager mDPM` for `ComponentName mDeviceAdminSample`
                 * (true if the given administrator component is currently active (enabled) in the system).
                 * If `stillActive` is true we call the `wipeData` method of
                 * `DevicePolicyManager mDPM` with the flag WIPE_EXTERNAL_STORAGE if
                 * `wipeAllData` was true, or 0 if it was false.
                 *
                 *  dialog the dialog that received the click
                 *  which the button that was clicked, BUTTON_POSITIVE in our case
                 */
                /**
                 * This method will be invoked when the positive button in the dialog is clicked.
                 * We initialize `boolean stillActive` with the value returned by the
                 * `isActiveAdmin` method of `DeviceAdminSample mActivity` (this
                 * helper method just returns the value returned by the `isAdminActive`
                 * method of `DevicePolicyManager mDPM` for `ComponentName mDeviceAdminSample`
                 * (true if the given administrator component is currently active (enabled) in the system).
                 * If `stillActive` is true we call the `wipeData` method of
                 * `DevicePolicyManager mDPM` with the flag WIPE_EXTERNAL_STORAGE if
                 * `wipeAllData` was true, or 0 if it was false.
                 *
                 *  dialog the dialog that received the click
                 *  which the button that was clicked, BUTTON_POSITIVE in our case
                 */
                builderLocal.setPositiveButton(R.string.wipe_warning_second_ok) { _, _ ->


                    val stillActive = mActivity!!.isActiveAdmin
                    if (stillActive) {
                        mDPM!!.wipeData(if (wipeAllData) DevicePolicyManager.WIPE_EXTERNAL_STORAGE else 0)
                    }
                }
                builderLocal.setNegativeButton(R.string.wipe_warning_second_no, null)
                builderLocal.show()
            }
            builder.setNegativeButton(R.string.wipe_warning_first_no, null)
            builder.show()
        }
    }

    /**
     * PreferenceFragment for "encryption" preferences, uses the xml/device_admin_encryption.xml
     * `PreferenceScreen` to load its preference widgets.
     */
    class EncryptionFragment : AdminSampleFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
        /**
         * `PreferenceCategory` "Encryption" with the key key_category_encryption in the
         * xml/device_admin_encryption.xml `PreferenceScreen`
         */
        private var mEncryptionCategory: PreferenceCategory? = null

        /**
         * `CheckBoxPreference` "Require encryption" with the key key_require_encryption in the
         * xml/device_admin_encryption.xml `PreferenceScreen`
         */
        private var mRequireEncryption: CheckBoxPreference? = null

        /**
         * `PreferenceScreen` "Activate encryption" with the key key_activate_encryption in the
         * xml/device_admin_encryption.xml `PreferenceScreen`
         */
        private var mActivateEncryption: PreferenceScreen? = null

        /**
         * Called to do initial creation of a fragment. First we call our super's implementation of
         * `onCreate`, then we call the `addPreferencesFromResource` method to inflate
         * the XML resource R.xml.device_admin_encryption and add its preference hierarchy to the
         * current preference hierarchy. We initialize `PreferenceCategory mEncryptionCategory`
         * by finding the preference with key KEY_CATEGORY_ENCRYPTION ("key_category_encryption"),
         * initialize `CheckBoxPreference mRequireEncryption` by finding the preference with key
         * KEY_REQUIRE_ENCRYPTION ("key_require_encryption"), and initialize `PreferenceScreen mActivateEncryption`
         * by finding the preference with key KEY_ACTIVATE_ENCRYPTION ("key_activate_encryption"). We
         * then set the `OnPreferenceChangeListener` of `mRequireEncryption` to this, and
         * the `OnPreferenceClickListener` of `mActivateEncryption` to this.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.device_admin_encryption)
            mEncryptionCategory = findPreference(KEY_CATEGORY_ENCRYPTION) as PreferenceCategory
            mRequireEncryption = findPreference(KEY_REQUIRE_ENCRYPTION) as CheckBoxPreference
            mActivateEncryption = findPreference(KEY_ACTIVATE_ENCRYPTION) as PreferenceScreen
            mRequireEncryption!!.onPreferenceChangeListener = this
            mActivateEncryption!!.onPreferenceClickListener = this
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of `onResume`, then we enable `PreferenceCategory mEncryptionCategory`
         * if `mAdminActive` is true or disable it if it is false. Finally we set the checked state of
         * `CheckBoxPreference mRequireEncryption` to the value returned by the `getStorageEncryption`
         * method of `DevicePolicyManager mDPM` (true if the admin(s) are requesting encryption, false if not).
         */
        @Deprecated("Deprecated in Java")
        override fun onResume() {
            super.onResume()
            mEncryptionCategory!!.isEnabled = mAdminActive
            mRequireEncryption!!.isChecked = mDPM!!.getStorageEncryption(mDeviceAdminSample)
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting. First
         * we call our super's implementation of `reloadSummaries`. Then we declare `local`
         * and `global` to be boolean, set local to the value returned by the `getStorageEncryption`
         * method of `DevicePolicyManager mDPM` and `global` to the global value it returns
         * (true if the admin(s) are requesting encryption, false if not). We then set the summary of
         * `CheckBoxPreference mRequireEncryption` to the string that our method `localGlobalSummary`
         * constructs from `local` and `global`. We then initialize `int deviceStatusCode`
         * with the value that the `getStorageEncryptionStatus` method of `DevicePolicyManager mDPM`
         * returns (current status of encryption. The value will be one of ENCRYPTION_STATUS_UNSUPPORTED
         * (indicates that encryption is not supported), ENCRYPTION_STATUS_INACTIVE (encryption is supported,
         * but is not currently active), ENCRYPTION_STATUS_ACTIVATING (encryption is not currently active,
         * but is currently being activated), ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY (encryption is active,
         * but an encryption key has not been set by the user), ENCRYPTION_STATUS_ACTIVE (encryption is active),
         * or ENCRYPTION_STATUS_ACTIVE_PER_USER (encryption is active and the encryption key is tied to the
         * user or profile). We initialize `String deviceStatus` with the string that our method
         * `statusCodeToString` retrieves to explain `deviceStatusCode`, then initialize
         * `String status` with the string formatted using the string with resource id
         * R.string.status_device_encryption ("Device encryption status=%1$s") from `deviceStatus`.
         * Finally we set the summary of `PreferenceScreen mActivateEncryption` to `status`.
         */
        override fun reloadSummaries() {
            super.reloadSummaries()
            val local: Boolean = mDPM!!.getStorageEncryption(mDeviceAdminSample)
            val global: Boolean = mDPM!!.getStorageEncryption(null)
            mRequireEncryption!!.summary = localGlobalSummary(local, global)
            val deviceStatusCode = mDPM!!.storageEncryptionStatus
            val deviceStatus = statusCodeToString(deviceStatusCode)
            val status = mActivity!!.getString(R.string.status_device_encryption, deviceStatus)
            mActivateEncryption!!.summary = status
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * `onPreferenceChange` returns true, we return true having done nothing. If our parameter
         * `Preference preference` is equal to `CheckBoxPreference mRequireEncryption` we
         * initialize `boolean newActive` by casting our parameter `Object newValue` to
         * Boolean. We then call the `setStorageEncryption` method of `DevicePolicyManager mDPM`
         * to request encryption if `newActive` is true or to release any previous request if it
         * is false. We then call our method `postReloadSummaries` to post a call to `reloadSummaries`
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value. If our parameter `Preference preference` is not equal to `mRequireEncryption`
         * we also return true.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (super.onPreferenceChange(preference, newValue)) {
                return true
            }
            if (preference === mRequireEncryption) {
                val newActive = newValue as Boolean
                mDPM!!.setStorageEncryption((mDeviceAdminSample)!!, newActive)
                // Delay update because the change is only applied after exiting this method.
                postReloadSummaries()
                return true
            }
            return true
        }

        /**
         * Called when a Preference has been clicked. If our super's implementation of `onPreferenceClick`
         * returns true, we return true having done nothing. If our parameter `Preference preference` is
         * `PreferenceScreen mActivateEncryption` we check whether we are being called by an automated
         * test by calling our method `alertIfMonkey`, which returns true if we are being run by an
         * automated test in which case we return true having done nothing (`alertIfMonkey` will have
         * displayed an alert dialog with the string with resource id R.string.monkey_encryption ("You can't
         * start encryption, you are a monkey!"). If the `getStorageEncryptionStatus` method of
         * `DevicePolicyManager mDPM` returns ENCRYPTION_STATUS_UNSUPPORTED (encryption is not supported)
         * we initialize `AlertDialog.Builder builder` with a new instance, set its message to the
         * string with resource id R.string.encryption_not_supported ("Encryption is not supported on this device."),
         * set the text of its positive button to the string with resource id R.string.encryption_not_supported_ok
         * ("OK"), show the dialog built from `builder` and return true to the caller. Otherwise
         * we initialize `Intent intent` with an instance whose action is ACTION_START_ENCRYPTION
         * (begin the process of encrypting data on the device). We then launch the activity of `intent`
         * for a result and return true to the caller.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.
         */
        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference): Boolean {
            if (super.onPreferenceClick(preference)) {
                return true
            }
            if (preference === mActivateEncryption) {
                if (alertIfMonkey(mActivity, R.string.monkey_encryption)) {
                    return true
                }
                // Check to see if encryption is even supported on this device (it's optional).
                if (mDPM!!.storageEncryptionStatus ==
                    DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
                ) {
                    val builder = AlertDialog.Builder(mActivity)
                    builder.setMessage(R.string.encryption_not_supported)
                    builder.setPositiveButton(R.string.encryption_not_supported_ok, null)
                    builder.show()
                    return true
                }
                // Launch the activity to activate encryption.  May or may not return!
                val intent = Intent(DevicePolicyManager.ACTION_START_ENCRYPTION)
                startActivityForResult(intent, REQUEST_CODE_START_ENCRYPTION)
                return true
            }
            return false
        }

        /**
         * Translates a `DevicePolicyManager` encryption status code to a descriptive string.
         * We initialize `int newStatus` to the resource id R.string.encryption_status_unknown
         * (the string "unknown") then switch on our parameter `int newStatusCode`:
         *
         *  *
         * ENCRYPTION_STATUS_UNSUPPORTED we set `newStatus` to the resource id
         * R.string.encryption_status_unsupported (the string: "unsupported") and break.
         *
         *  *
         * ENCRYPTION_STATUS_INACTIVE we set `newStatus` to the resource id
         * R.string.encryption_status_inactive (the string: "inactive") and break.
         *
         *  *
         * ENCRYPTION_STATUS_ACTIVATING we set `newStatus` to the resource id
         * R.string.encryption_status_activating (the string: "activating") and break.
         *
         *  *
         * ENCRYPTION_STATUS_ACTIVE we set `newStatus` to the resource id
         * R.string.encryption_status_active (the string: "active") and break.
         *
         *
         * Finally we return the string that the `getString` method of `DeviceAdminSample mActivity`
         * returns for the resource id in `newStatus`.
         *
         * @param newStatusCode `DevicePolicyManager` encryption status code
         * @return a string describing the meaning of `newStatusCode`
         */
        private fun statusCodeToString(newStatusCode: Int): String {
            var newStatus = R.string.encryption_status_unknown
            when (newStatusCode) {
                DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> newStatus =
                    R.string.encryption_status_unsupported
                DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> newStatus =
                    R.string.encryption_status_inactive
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING -> newStatus =
                    R.string.encryption_status_activating
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> newStatus =
                    R.string.encryption_status_active
            }
            return mActivity!!.getString(newStatus)
        }
    }

    /**
     * Sample implementation of a DeviceAdminReceiver.  Your controller must provide one,
     * although you may or may not implement all of the methods shown here.
     *
     *
     * All callbacks are on the UI thread and your implementations should not engage in any
     * blocking operations, including disk I/O.
     */
    class DeviceAdminSampleReceiver : DeviceAdminReceiver() {
        /**
         * Convenience method to toast a `DeviceAdminSampleReceiver` status message. We initialize
         * `String status` by using the format string with resource id R.string.admin_receiver_status
         * ("Sample Device Admin: %1$s") to format our parameter `String msg`, then make and show
         * a toast of `status`.
         *
         * @param context `Context` to use to access resources.
         * @param msg string to format into a "Sample Device Admin: %1$s" toast message
         */
        private fun showToast(context: Context, msg: String?) {
            val status = context.getString(R.string.admin_receiver_status, msg)
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }

        /**
         * Intercept standard device administrator broadcasts. This method is called when this
         * BroadcastReceiver is receiving an Intent broadcast. If the action of our parameter
         * `Intent intent` is ACTION_DEVICE_ADMIN_DISABLE_REQUESTED (Action sent to a device
         * administrator when the user has requested to disable it, but before this has actually been
         * done) we call the `abortBroadcast` method to set the flag indicating that this receiver
         * should abort the current broadcast (This will prevent any other broadcast receivers from
         * receiving the broadcast). Finally we call our super's implementation of `onReceive`.
         *
         * @param context The Context in which the receiver is running.
         * @param intent The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            if ((intent.action == ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
                abortBroadcast()
            }
            super.onReceive(context, intent)
        }

        /**
         * Called after the administrator is first enabled, as a result of receiving an intent with
         * the action [.ACTION_DEVICE_ADMIN_ENABLED] (This is the primary action that a device
         * administrator must implement to be allowed to manage a device. This will be sent to the
         * receiver when the user enables it for administration). We call our method `showToast`
         * to toast the string with resource id R.string.admin_receiver_status_enabled ("enabled").
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         */
        override fun onEnabled(context: Context, intent: Intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_enabled))
        }

        /**
         * Called when the user has asked to disable the administrator, as a result of receiving
         * [.ACTION_DEVICE_ADMIN_DISABLE_REQUESTED] (Action sent to a device administrator when
         * the user has requested to disable it, but before this has actually been done), giving you
         * a chance to present a warning message to them. The message is returned as the result; if
         * null is returned (the default implementation), no message will be displayed. We return the
         * string with resource id R.string.admin_receiver_status_disable_warning ("This is an optional
         * message to warn the user about disabling").
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         * @return Return the warning message to display to the user before being disabled; if null
         * is returned, no message is displayed.
         */
        override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
            return context.getString(R.string.admin_receiver_status_disable_warning)
        }

        /**
         * Called prior to the administrator being disabled, as a result of receiving
         * [.ACTION_DEVICE_ADMIN_DISABLED]. Upon return, you can no longer use the
         * protected parts of the [DevicePolicyManager] API. We call our method
         * `showToast` with the string resource id R.string.admin_receiver_status_disabled
         * ("disabled").
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         */
        override fun onDisabled(context: Context, intent: Intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_disabled))
        }

        /**
         * Called after the user has changed their device or profile challenge password, as a result of
         * receiving [.ACTION_PASSWORD_CHANGED]. At this point you can use
         * [DevicePolicyManager.getPasswordQuality]
         * to retrieve the active password characteristics. We call our method `showToast`
         * with the string resource id R.string.admin_receiver_status_pw_changed ("pw changed").
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         */
        @Deprecated("From {@code android.os.Build.VERSION_CODES.O}, use\n" + "          {@code onPasswordChanged(Context, Intent, UserHandle)} instead.")
        override fun onPasswordChanged(context: Context, intent: Intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_changed))
        }

        /**
         * Called after the user has failed at entering their device or profile challenge password,
         * as a result of receiving [.ACTION_PASSWORD_FAILED].  At this point you can use
         * [DevicePolicyManager.getCurrentFailedPasswordAttempts] to retrieve the number of
         * failed password attempts. We call our method `showToast` with the string resource
         * id R.string.admin_receiver_status_pw_failed ("pw failed")
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         */
        @Deprecated("From {@link android.os.Build.VERSION_CODES#O}, use\n" + "          {@code onPasswordFailed(Context, Intent, UserHandle)} instead.")
        override fun onPasswordFailed(context: Context, intent: Intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_failed))
        }

        /**
         * Called after the user has succeeded at entering their device or profile challenge password,
         * as a result of receiving [.ACTION_PASSWORD_SUCCEEDED].  This will only be received
         * the first time they succeed after having previously failed. We call our method `showToast`
         * with the string resource id R.string.admin_receiver_status_pw_succeeded ("pw succeeded").
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         */
        @Deprecated("From {@link android.os.Build.VERSION_CODES#O}, use\n" + "          {@code onPasswordSucceeded(Context, Intent, UserHandle)} instead.")
        override fun onPasswordSucceeded(context: Context, intent: Intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_succeeded))
        }

        /**
         * Called periodically when the device or profile challenge password is about to expire
         * or has expired.  It will typically be called at these times: on device boot, once per day
         * before the password expires, and at the time when the password expires.
         *
         *
         * If the password is not updated by the user, this method will continue to be called
         * once per day until the password is changed or the device admin disables password expiration.
         *
         *
         * The admin will typically post a notification requesting the user to change their password
         * in response to this call. The actual password expiration time can be obtained by calling
         * [DevicePolicyManager.getPasswordExpiration]
         *
         *
         * The admin should be sure to take down any notifications it posted in response to this call
         * when it receives [DeviceAdminReceiver.onPasswordChanged].
         *
         *
         * We initialize `DevicePolicyManager dpm` with a handle to the DEVICE_POLICY_SERVICE
         * system level service, then initialize `long expr` with the value returned by the
         * `getPasswordExpiration` method of `dpm` for the component name whose class
         * is `DeviceAdminSampleReceiver` (`getPasswordExpiration` returns the password
         * expiration time, in milliseconds since epoch). We then initialize `long delta` by
         * subtracting the current time in milliseconds from `expr`. We initialize `expired`
         * to true if `delta` is less than 0, and if `expired` is true we initialize
         * `String message` to the string with resource id R.string.expiration_status_past
         * ("Password expired %1$s ago"), if false to the string with resource id R.string.expiration_status_future
         * ("Password will expire %1$s from now"). We then call our method `showToast` with
         * `message` and log `message` as well.
         *
         * @param context The running context as per [.onReceive].
         * @param intent The received intent as per [.onReceive].
         */
        @Deprecated("From {@link android.os.Build.VERSION_CODES#O}, use\n" + "          {@code onPasswordExpiring(Context, Intent, UserHandle)} instead.")
        override fun onPasswordExpiring(context: Context, intent: Intent) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val expr = dpm.getPasswordExpiration(
                ComponentName(
                    context,
                    DeviceAdminSampleReceiver::class.java
                )
            )
            val delta = expr - System.currentTimeMillis()
            val expired = delta < 0L
            val message =
                context.getString(if (expired) R.string.expiration_status_past else R.string.expiration_status_future)
            showToast(context, message)
            Log.v(TAG, message)
        }
    }

    /**
     * UNUSED.
     */
    class DeviceAdminSampleReceiver2 : DeviceAdminReceiver()
    companion object {
        // Miscellaneous utilities and definitions
        /**
         * TAG used for logging
         */
        private const val TAG = "DeviceAdminSample"

        /**
         * Request code used when starting the activity to have the user enable our admin.
         */
        private const val REQUEST_CODE_ENABLE_ADMIN = 1

        /**
         * Request code used when starting the activity to activate encryption.
         */
        private const val REQUEST_CODE_START_ENCRYPTION = 2

        /**
         * Number of milliseconds in a minute
         */
        private const val MS_PER_MINUTE = 60 * 1000.toLong()

        /**
         * Number of milliseconds in an hour
         */
        private const val MS_PER_HOUR = 60 * MS_PER_MINUTE

        /**
         * Number of milliseconds in a day
         */
        private const val MS_PER_DAY = 24 * MS_PER_HOUR
        // The following keys are used to find each preference item
        /**
         * android:key for the "Enable admin" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_ENABLE_ADMIN = "key_enable_admin"

        /**
         * android:key for the "Disable all device cameras" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_CAMERA = "key_disable_camera"

        /**
         * android:key for the "Disable keyguard notifications" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_NOTIFICATIONS = "key_disable_notifications"

        /**
         * android:key for the "Disable keyguard unredacted notifications" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_UNREDACTED = "key_disable_unredacted"

        /**
         * android:key for the "Disable keyguard Trust Agents" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_TRUST_AGENTS = "key_disable_trust_agents"

        /**
         * android:key for the "Enabled Component Name" EditTextPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_TRUST_AGENT_COMPONENT = "key_trust_agent_component"

        /**
         * android:key for the "Enabled Features (comma-separated)" EditTextPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_TRUST_AGENT_FEATURES = "key_trust_agent_features"

        /**
         * android:key for the "Disable keyguard widgets" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_KEYGUARD_WIDGETS = "key_disable_keyguard_widgets"

        /**
         * android:key for the "Disable keyguard secure camera" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_KEYGUARD_SECURE_CAMERA = "key_disable_keyguard_secure_camera"

        /**
         * android:key for the "Disable keyguard Fingerprint" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_FINGERPRINT = "key_disable_fingerprint"

        /**
         * android:key for the "Disable keyguard Remote Input" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
         */
        private const val KEY_DISABLE_REMOTE_INPUT = "key_disable_remote_input"

        /**
         * android:key for the "Password quality" PreferenceCategory in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_CATEGORY_QUALITY = "key_category_quality"

        /**
         * android:key for the "Set password (user)" PreferenceScreen in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_SET_PASSWORD = "key_set_password"

        /**
         * android:key for the "Set password (via API)" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_RESET_PASSWORD = "key_reset_password"

        /**
         * android:key for the "Password quality" ListPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_QUALITY = "key_quality"

        /**
         * android:key for the "Minimum length" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_LENGTH = "key_minimum_length"

        /**
         * android:key for the "Minimum letters" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_LETTERS = "key_minimum_letters"

        /**
         * android:key for the "Minimum numeric" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_NUMERIC = "key_minimum_numeric"

        /**
         * android:key for the "Minimum lower case" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_LOWER_CASE = "key_minimum_lower_case"

        /**
         * android:key for the "Minimum upper case" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_UPPER_CASE = "key_minimum_upper_case"

        /**
         * android:key for the "Minimum symbols" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_SYMBOLS = "key_minimum_symbols"

        /**
         * android:key for the "Minimum non-letter" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
         */
        private const val KEY_MIN_NON_LETTER = "key_minimum_non_letter"

        /**
         * android:key for the "Password history / Expiration" PreferenceCategory in the xml/device_admin_expiration.xml PreferenceScreen
         */
        private const val KEY_CATEGORY_EXPIRATION = "key_category_expiration"

        /**
         * android:key for the "Password history depth" EditTextPreference in the xml/device_admin_expiration.xml PreferenceScreen
         */
        private const val KEY_HISTORY = "key_history"

        /**
         * android:key for the "Password expiration timeout (minutes)" EditTextPreference in the xml/device_admin_expiration.xml PreferenceScreen
         */
        private const val KEY_EXPIRATION_TIMEOUT = "key_expiration_timeout"

        /**
         * android:key for the "Password expiration status" PreferenceScreen in the xml/device_admin_expiration.xml PreferenceScreen
         */
        private const val KEY_EXPIRATION_STATUS = "key_expiration_status"

        /**
         * android:key for the "Lock screen / Wipe" PreferenceCategory in the xml/device_admin_lock_wipe.xml PreferenceScreen
         */
        private const val KEY_CATEGORY_LOCK_WIPE = "key_category_lock_wipe"

        /**
         * android:key for the "Max time to screen lock (minutes)" EditTextPreference in the xml/device_admin_lock_wipe.xml PreferenceScreen
         */
        private const val KEY_MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock"

        /**
         * android:key for the "Max password failures for local wipe" EditTextPreference in the xml/device_admin_lock_wipe.xml PreferenceScreen
         */
        private const val KEY_MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe"

        /**
         * android:key for the "Lock screen now" PreferenceScreen in the xml/device_admin_lock_wipe.xml PreferenceScreen
         */
        private const val KEY_LOCK_SCREEN = "key_lock_screen"

        /**
         * android:key for the "Wipe data" PreferenceScreen in the xml/device_admin_lock_wipe.xml PreferenceScreen
         */
        private const val KEY_WIPE_DATA = "key_wipe_data"

        /**
         * android:key for the "Wipe all data" PreferenceScreen in the xml/device_admin_lock_wipe.xml PreferenceScreen
         */
        private const val KEY_WIP_DATA_ALL = "key_wipe_data_all"

        /**
         * android:key for the "Encryption" PreferenceCategory in the xml/device_admin_encryption.xml PreferenceScreen
         */
        private const val KEY_CATEGORY_ENCRYPTION = "key_category_encryption"

        /**
         * android:key for the "Require encryption" CheckBoxPreference in the xml/device_admin_encryption.xml PreferenceScreen
         */
        private const val KEY_REQUIRE_ENCRYPTION = "key_require_encryption"

        /**
         * android:key for the "Activate encryption" PreferenceScreen in the xml/device_admin_encryption.xml PreferenceScreen
         */
        private const val KEY_ACTIVATE_ENCRYPTION = "key_activate_encryption"

        /**
         * Simple converter used for long expiration times reported in mSec. We initialize `long days`
         * by dividing our parameter `time` by the number of milliseconds in a day, `long hours`
         * by dividing our parameter `time` by the number of milliseconds in an hour then applying
         * modulo 24 to that value, `long minutes` by dividing our parameter `time` by the
         * number of milliseconds in an minute then applying modulo 60 to that value, and `long minutes`
         * by dividing our parameter `time` by the number of milliseconds in a minute then applying
         * modulo 60 to that value. Finally we return the string formatted from `days`, `hours`,
         * and `minutes` using the format string with resource id R.string.status_days_hours_minutes
         * ("%1$dd %2$dh %3$dm").
         *
         * @param context `Context` to use to access resources.
         * @param time time in milliseconds.
         * @return string representation of our parameter `time`
         */
        private fun timeToDaysMinutesSeconds(context: Context?, time: Long): String {
            val days = time / MS_PER_DAY
            val hours = (time / MS_PER_HOUR) % 24
            val minutes = (time / MS_PER_MINUTE) % 60
            return context!!.getString(R.string.status_days_hours_minutes, days, hours, minutes)
        }

        /**
         * If the "user" is a monkey, post an alert and notify the caller. This prevents automated test
         * frameworks from stumbling into annoying or dangerous operations. If the `isUserAMonkey`
         * method of `ActivityManager` returns true (the user interface is currently being messed
         * with by a monkey) we initialize `AlertDialog.Builder builder`, set its message to the
         * string with the resource id of our parameter `int stringId`, set the text of its positive
         * button to the string with resource id R.string.monkey_ok ("I admit defeat"), show the alert
         * dialog built from `builder` and return true to the caller. Otherwise we return false to
         * the caller.
         *
         * @param context `Context` to use to access resources.
         * @param stringId resource id of the message to use in the alert dialog.
         * @return Returns "true" if the user interface is currently being messed with by a monkey.
         */
        private fun alertIfMonkey(context: Context?, stringId: Int): Boolean {
            return if (ActivityManager.isUserAMonkey()) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(stringId)
                builder.setPositiveButton(R.string.monkey_ok, null)
                builder.show()
                true
            } else {
                false
            }
        }
    }
}
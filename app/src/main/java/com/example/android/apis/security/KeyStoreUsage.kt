/*
 * Copyright 2013 The Android Open Source Project
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
 * limitations under the License
 */
package com.example.android.apis.security

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Signature
import java.security.SignatureException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.util.*

import com.example.android.apis.R

/**
 * Shows how to use api to generate Key pairs, sign and verify.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.M)
@SuppressLint("SetTextI18n")
class KeyStoreUsage : AppCompatActivity() {
    /**
     * An instance of [java.security.KeyStore] through which this app
     * talks to the `AndroidKeyStore`. UNUSED
     */
    @Suppress("unused")
    var mKeyStore: KeyStore? = null

    /**
     * Used by the [ListView] in our layout to list the keys available in
     * our [KeyStore] by their alias names.
     */
    var mAdapter: AliasAdapter? = null

    /**
     * [Button] in the UI that causes a new keypair to be generated in the
     * [KeyStore].
     */
    var mGenerateButton: Button? = null

    /**
     * [Button] in the UI that causes data to be signed by a key we selected from
     * the list available in the [KeyStore].
     */
    var mSignButton: Button? = null

    /**
     * [Button] in the UI that causes data to be verified by a key we selected from
     * the list available in the [KeyStore].
     */
    var mVerifyButton: Button? = null

    /**
     * [Button] in the UI that causes a key entry to be deleted from the
     * [KeyStore].
     */
    var mDeleteButton: Button? = null

    /**
     * [EditText] field in the UI that holds plaintext.
     */
    var mPlainText: EditText? = null

    /**
     * [EditText] field in the UI that holds the signature.
     */
    var mCipherText: EditText? = null

    /**
     * The alias of the selected entry in the [KeyStore].
     */
    private var mSelectedAlias: String? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.keystore_usage.
     * We locate the [ListView] `val lv` with ID R.id.entries_list, allocate a new instance for
     * [AliasAdapter] field [mAdapter], set it as the adapter for `lv`, set the choice mode of
     * `lv` to CHOICE_MODE_SINGLE and set its [OnItemClickListener] to an lambda which sets [String]
     * field [mSelectedAlias] to the item that has been clicked and calls our method
     * [setKeyActionButtonsEnabled] to enable the views which need a key to use, namely [EditText]
     * field [mPlainText], [EditText] field [mCipherText], [Button] field [mSignButton], [Button]
     * field [mVerifyButton], and [Button] field [mDeleteButton].
     *
     * We locate the [EditText] with ID R.id.entry_name to set [EditText] `val aliasInput`, and
     * the [Button] with ID R.id.generate_button to initialize our [Button] field [mGenerateButton],
     * then set the `OnClickListener` of [mGenerateButton] to an lambda which reads the text from
     * [EditText] `aliasInput` into [String] `val alias`, checks to make sure it was not empty
     * (complaining about the error if it was) otherwise it clears any `aliasInput` error, disables
     * the [Button] field [mGenerateButton] and starts the [AsyncTask] class [GenerateTask] running
     * using `alias` as its argument.
     *
     * Next we locate the [Button] with ID R.id.sign_button to initialize our [Button] field
     * [mSignButton], set its `OnClickListener` to an lambda which sets [String] `val alias` to the
     * contents of our [String] field [mSelectedAlias], fetches the text from [EditText] field
     * [mPlainText] to the [String] variable `val data`, then if `alias` is not null calls
     * [setKeyActionButtonsEnabled] to temporarily disable the key action views, and then starts
     * the [AsyncTask] class [SignTask] running using the arguments `alias` and `data`.
     *
     * We locate the [Button] with ID R.id.verify_button to initialize our [Button] field [mVerifyButton],
     * and set its `OnClickListener` to an lambda which sets [String] `val alias` to the contents
     * of our [String] field [mSelectedAlias], fetches the text from [EditText] field [mPlainText]
     * to the [String] variable `val data`, fetches the text from [EditText] field [mCipherText] to
     * set [String] `val signature`, then if `alias` is not null calls [setKeyActionButtonsEnabled]
     * to temporarily disable the key action views, and then starts the [AsyncTask] classs [AsyncTask]
     * running using the arguments `alias`, `data` and `signature`.
     *
     * We locate the [Button] with ID R.id.delete_button to initialize our [Button] field [mDeleteButton],
     * and set its `OnClickListener` to an lambda which sets [String] `val alias` to the contents of
     * our [String] field [mSelectedAlias], and if it is not null calls [setKeyActionButtonsEnabled]
     * to temporarily disable the key action views, and then starts the [AsyncTask] class [DeleteTask]
     * running using the argument `alias`.
     *
     * We locate the [EditText] with ID R.id.plaintext to initialize our [EditText] field [mPlainText]
     * and set its `OnFocusChangeListener` to an lambda which sets the color of the text to the
     * correct color for its state based on the values in android.R.color.primary_text_dark.
     *
     * We locate the [EditText] with ID R.id.ciphertext to initialize our [EditText] field [mCipherText]
     * and set its `OnFocusChangeListener` to an lambda which sets the color of the text to the correct
     * color for its state based on the values in android.R.color.primary_text_dark.
     *
     * Finally we call our method [updateKeyList] which calls [setKeyActionButtonsEnabled] to
     * temporarily disable the key action views, and then starts the [AsyncTask] class
     * [UpdateKeyListTask] running.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.keystore_usage)

        /*
         * Set up our {@code ListView} with an adapter that allows
         * us to choose from the available entry aliases.
         */
        val lv = findViewById<ListView>(R.id.entries_list)
        mAdapter = AliasAdapter(applicationContext)
        lv.adapter = mAdapter
        lv.choiceMode = ListView.CHOICE_MODE_SINGLE
        lv.onItemClickListener = OnItemClickListener {
            _: AdapterView<*>?,
            _: View?,
            position: Int,
            _: Long ->
            mSelectedAlias = mAdapter!!.getItem(position)
            setKeyActionButtonsEnabled(true)
        }

        // This is alias the user wants for a generated key.
        val aliasInput = findViewById<EditText>(R.id.entry_name)
        mGenerateButton = findViewById(R.id.generate_button)
        mGenerateButton!!.setOnClickListener {
            /*
             * When the user presses the "Generate" button, we'll
             * check the alias isn't blank here.
             */
            val alias = aliasInput.text.toString()
            @Suppress("SENSELESS_COMPARISON")
            if (alias == null || alias.isEmpty()) {
                aliasInput.error = resources.getText(R.string.keystore_no_alias_error)
            } else {
                /*
                 * It's not blank, so disable the generate button while
                 * the generation of the key is happening. It will be
                 * enabled by the {@code AsyncTask} later after its
                 * work is done.
                 */
                aliasInput.error = null
                mGenerateButton!!.isEnabled = false
                GenerateTask().execute(alias)
            }
        }
        mSignButton = findViewById(R.id.sign_button)
        mSignButton!!.setOnClickListener {
            val alias = mSelectedAlias
            val data = mPlainText!!.text.toString()
            if (alias != null) {
                setKeyActionButtonsEnabled(false)
                SignTask().execute(alias, data)
            }
        }
        mVerifyButton = findViewById(R.id.verify_button)
        mVerifyButton!!.setOnClickListener {
            val alias = mSelectedAlias
            val data = mPlainText!!.text.toString()
            val signature = mCipherText!!.text.toString()
            if (alias != null) {
                setKeyActionButtonsEnabled(false)
                VerifyTask().execute(alias, data, signature)
            }
        }
        mDeleteButton = findViewById(R.id.delete_button)
        mDeleteButton!!.setOnClickListener {
            val alias = mSelectedAlias
            if (alias != null) {
                setKeyActionButtonsEnabled(false)
                DeleteTask().execute(alias)
            }
        }
        mPlainText = findViewById(R.id.plaintext)
        mPlainText!!.setOnFocusChangeListener { _: View?, _: Boolean ->
            @Suppress("DEPRECATION")
            mPlainText!!.setTextColor(resources.getColor(android.R.color.primary_text_dark, null))
        }
        mCipherText = findViewById(R.id.ciphertext)
        mCipherText!!.setOnFocusChangeListener { _: View?, _: Boolean ->
            @Suppress("DEPRECATION")
            mCipherText!!.setTextColor(resources
                    .getColor(android.R.color.primary_text_dark, null))
        }
        updateKeyList()
    }

    /**
     * The `Adapter` we use for our [AliasAdapter] field [mAdapter], it stores the alias strings
     * the user has used in its `ArrayAdapter<String>`
     */
    inner class AliasAdapter
    /**
     * Our constructor. We call our super's constructor specifying
     * android.R.layout.simple_list_item_single_choice as the resource ID for the layout file
     * containing a `TextView` to use when instantiating views (it is a `CheckedTextView`).
     *
     * @param context [Context] to use to access resources.
     */
    (context: Context?) : ArrayAdapter<String?>(
            context!!,
            android.R.layout.simple_list_item_single_choice
    ) {
        /**
         * This clears out all previous aliases and replaces it with the current entries. First we
         * remove all elements from the list, and then we add our `List<String>` parameter [items]
         * at the end of the array. Finally we call [notifyDataSetChanged] to notify the attached
         * observers that the underlying data has been changed and any View reflecting the data set
         * should refresh itself.
         *
         * @param items the list of alias entries we should now be using.
         */
        fun setAliases(items: List<String>?) {
            clear()
            addAll(items!!)
            notifyDataSetChanged()
        }
    }

    /**
     * Updates the list of keys. First we call our method [setKeyActionButtonsEnabled] to disable
     * the views used by the key actions: [EditText] field [mPlainText], [EditText] field
     * [mCipherText], [Button] field [mSignButton], [Button] field [mVerifyButton], and [Button]
     * field [mDeleteButton]. Then we start our [AsyncTask] class [UpdateKeyListTask] running to
     * do the actual updating.
     */
    private fun updateKeyList() {
        setKeyActionButtonsEnabled(false)
        UpdateKeyListTask().execute()
    }

    /**
     * Sets all the buttons related to actions that act on an existing key to enabled or disabled:
     * [EditText] field [mPlainText], [EditText] field [mCipherText], [Button] field [mSignButton],
     * [Button] field[mVerifyButton], and [Button] field [mDeleteButton].
     */
    private fun setKeyActionButtonsEnabled(enabled: Boolean) {
        mPlainText!!.isEnabled = enabled
        mCipherText!!.isEnabled = enabled
        mSignButton!!.isEnabled = enabled
        mVerifyButton!!.isEnabled = enabled
        mDeleteButton!!.isEnabled = enabled
    }

    /**
     * [AsyncTask] which updates the list of aliases used by [AliasAdapter] field [mAdapter].
     */
    @SuppressLint("StaticFieldLeak")
    private inner class UpdateKeyListTask : AsyncTask<Void?, Void?, Enumeration<String>?>() {
        /**
         * Returns an `Enumeration<String>` of all the names in the "AndroidKeyStore" keystore
         * object. First we fetch a [KeyStore] object of type "AndroidKeyStore" to initialize
         * [KeyStore] `val ks`, we load `ks`, and then return an `Enumeration<String> aliases`
         * listing all of the alias names of this keystore.
         *
         * @param params we do not use params, so these are `Void`
         * @return An object that implements the Enumeration interface for [String] objects,
         * it generates a series of elements, one at a time. Successive calls to the `nextElement`
         * method return successive elements of the series.
         */
        override fun doInBackground(vararg params: Void?): Enumeration<String>? {
            return try {

                /*
                 * Load the Android KeyStore instance using the the
                 * "AndroidKeyStore" provider to list out what entries are
                 * currently stored.
                 */
                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)
                ks.aliases()
            } catch (e: KeyStoreException) {
                Log.w(TAG, "Could not list keys", e)
                null
            } catch (e: NoSuchAlgorithmException) {
                Log.w(TAG, "Could not list keys", e)
                null
            } catch (e: CertificateException) {
                Log.w(TAG, "Could not list keys", e)
                null
            } catch (e: IOException) {
                Log.w(TAG, "Could not list keys", e)
                null
            }
        }

        /**
         * Runs on the UI thread after [doInBackground]. The parameter [result] is the
         * value returned by [doInBackground]. First we create an [ArrayList] for
         * `List<String>` `val aliases`, then we loop through all of the [String] objects in
         * our parameter [result] adding each of them to `aliases`. Finally we call the
         * method `mAdapter.setAliases` to clear out all previous aliases and replace them with
         * the contents of `aliases`.
         *
         * @param result The list of [KeyStore] aliases computed by [doInBackground].
         */
        override fun onPostExecute(result: Enumeration<String>?) {
            val aliases: MutableList<String> = ArrayList()
            while (result!!.hasMoreElements()) {
                aliases.add(result.nextElement())
            }
            @Suppress("UNCHECKED_CAST")
            mAdapter!!.setAliases((aliases as List<String>))
        }
    }

    /**
     * [AsyncTask] which is run to generate a new EC key pair entry in the Android Keystore.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class GenerateTask : AsyncTask<String?, Void?, Boolean>() {
        /**
         * Generate a new EC key pair entry in the Android Keystore by using the [KeyPairGenerator]
         * API. First we set [String] `val alias` from our parameter `params[0]` then we set
         * [KeyPairGenerator] `val kpg` to a [KeyPairGenerator] object that generates public/private
         * key pairs for the KEY_ALGORITHM_EC algorithm using the `KeyPairGeneratorSpi` implementation
         * from the "AndroidKeyStore" provider. We initialize `kpg` using a
         * [KeyGenParameterSpec.Builder] which uses `alias` as the alias of the entry in which the
         * generated key will appear in Android KeyStore, and whose purpose is both PURPOSE_SIGN and
         * PURPOSE_VERIFY, whose digests algorithms we set to DIGEST_SHA256 and DIGEST_SHA512 and
         * then build. Finally we instruct `kpg` to generate a new key pair, and return true to the
         * caller.
         *
         * @param params The alias for the key.
         * @return true if successful, false if an exception is thrown (it is ignored though)
         */
        override fun doInBackground(vararg params: String?): Boolean {
            val alias = params[0]
            return try {

                /*
                 * Generate a new EC key pair entry in the Android Keystore by
                 * using the KeyPairGenerator API. The private key can only be
                 * used for signing or verification and only with SHA-256 or
                 * SHA-512 as the message digest.
                 */
                val kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC,
                        "AndroidKeyStore"
                )
                kpg.initialize(KeyGenParameterSpec.Builder(
                                alias!!,
                                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                        )
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .build())
                @Suppress("UNUSED_VARIABLE")
                val kp = kpg.generateKeyPair()
                true
            } catch (e: NoSuchAlgorithmException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: InvalidAlgorithmParameterException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: NoSuchProviderException) {
                Log.w(TAG, "Could not generate key", e)
                false
            }
        }

        /**
         * Called on the UI thread when the background task returns. We call our method [updateKeyList]
         * to update the list of keys, and then enable the [Button] field [mGenerateButton].
         *
         * @param result we ignore this.
         */
        override fun onPostExecute(result: Boolean) {
            updateKeyList()
            mGenerateButton!!.isEnabled = true
        }

        /**
         * Called if the background task is cancelled, we simply enable the [Button] field
         * [mGenerateButton].
         */
        override fun onCancelled() {
            mGenerateButton!!.isEnabled = true
        }
    }

    /**
     * [AsyncTask] which is run to create a signature for some data.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class SignTask : AsyncTask<String?, Void?, String?>() {
        /**
         * Uses the keystore entry specified by `params[0]` to sign the data specified by
         * `params[1]` and returns the resulting signature. First we copy references to our
         * two parameters to [String] `val alias` and [String] `val dataString`. Then we load
         * [ByteArray] `val data` with the `byte[]` version of `dataString`. We initialize
         * [KeyStore] `val ks` with an instance of [KeyStore] providing the "AndroidKeyStore"
         * type, then instruct it to load. We fetch from `ks` the keystore entry for the alias
         * [String] `alias` to initialize [KeyStore.Entry] `val entry`. If `entry` is not
         * an instance of [KeyStore.PrivateKeyEntry] we log the error and return null. Otherwise
         * we initialize [Signature] `val s` with an instance that implements the "SHA256withECDSA"
         * signature algorithm. We initialize `s` for signing using the `PrivateKey` of `entry`,
         * update the data to be signed or verified by `s`, and then use `s` to initialize [ByteArray]
         * `val signature` with the signature bytes of all the data updated. We then return
         * `signature` as a Base64-encoded string using [Base64.DEFAULT].
         *
         * @param params the alias in the keystore to use (`params[0]`), and the data that we
         * are to sign (`params[1]`).
         * @return The signature of the data using the keystore alias entry passed us
         */
        override fun doInBackground(vararg params: String?): String? {
            val alias = params[0]
            val dataString = params[1]
            return try {
                val data = dataString!!.toByteArray()

                /*
                 * Use a PrivateKey in the KeyStore to create a signature over
                 * some data.
                 */
                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)
                val entry = ks.getEntry(alias, null)
                if (entry !is KeyStore.PrivateKeyEntry) {
                    Log.w(TAG, "Not an instance of a PrivateKeyEntry")
                    return null
                }
                val s = Signature.getInstance("SHA256withECDSA")
                s.initSign(entry.privateKey)
                s.update(data)
                val signature = s.sign()
                Base64.encodeToString(signature, Base64.DEFAULT)
            } catch (e: NoSuchAlgorithmException) {
                Log.w(TAG, "Could not generate key", e)
                null
            } catch (e: KeyStoreException) {
                Log.w(TAG, "Could not generate key", e)
                null
            } catch (e: CertificateException) {
                Log.w(TAG, "Could not generate key", e)
                null
            } catch (e: IOException) {
                Log.w(TAG, "Could not generate key", e)
                null
            } catch (e: UnrecoverableEntryException) {
                Log.w(TAG, "Could not generate key", e)
                null
            } catch (e: InvalidKeyException) {
                Log.w(TAG, "Could not generate key", e)
                null
            } catch (e: SignatureException) {
                Log.w(TAG, "Could not generate key", e)
                null
            }
        }

        /**
         * When the background task completes, we are called on the UI thread with our [String]
         * parameter [result] containing the signature it computed. We set the text of [EditText]
         * field [mCipherText] to our parameter [result], and call our method
         * [setKeyActionButtonsEnabled] to re-enable the views involved with the key actions.
         *
         * @param result signature of the data returned by the background task
         */
        override fun onPostExecute(result: String?) {
            mCipherText!!.setText(result)
            setKeyActionButtonsEnabled(true)
        }

        /**
         * Called on the UI thread when the background thread is cancelled. We set the text of
         * [EditText] field [mCipherText] to "error!", and call our method [setKeyActionButtonsEnabled]
         * to re-enable the views involved with the key actions.
         */
        override fun onCancelled() {
            mCipherText!!.setText("error!")
            setKeyActionButtonsEnabled(true)
        }
    }

    /**
     * [AsyncTask] which is run to verify a signature.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class VerifyTask : AsyncTask<String?, Void?, Boolean>() {
        /**
         * Verifies a signature in the background. First we copy references to our three parameters
         * to the [String]'s `val alias`, `val dataString` and `val signatureString`. We initialize
         * [ByteArray] `val data` with the byte version of `dataString`, declare [ByteArray]
         * `val signature` and try to decode the Base64 contained in `signatureString` into it
         * (setting `signature` to an zero element array if it fails). We initialize [KeyStore]
         * `val ks` with an instance of [KeyStore] providing the "AndroidKeyStore" type, then
         * instruct it to load. We fetch from `ks` the keystore entry for the alias named [String]
         * `alias` to initialize [KeyStore.Entry] `val entry`. If `entry` is not an instance of
         * [KeyStore.PrivateKeyEntry] we log the error and return false. Otherwise we initialize
         * [Signature] `val s` with an instance that implements the "SHA256withECDSA" signature
         * algorithm. We initialize `s` for verification, using the public key from the end entity
         * Certificate from the certificate chain of `entry`, update the data verified by `s`, and
         * then use `s` to verify `signature` returning the [Boolean] result of the verification
         * to the caller.
         *
         * @param params `params[0]` contains the alias to use from the keystore, `params[1]`
         * contains the data that has been signed, and `params[2]` contains the signature to
         * be verified.
         * @return true if the signature is valid, false if it is not
         */
        override fun doInBackground(vararg params: String?): Boolean {
            val alias = params[0]
            val dataString = params[1]
            val signatureString = params[2]
            return try {
                val data = dataString!!.toByteArray()
                val signature: ByteArray? = try {
                    Base64.decode(signatureString, Base64.DEFAULT)
                } catch (e: IllegalArgumentException) {
                    ByteArray(0)
                }

                /*
                 * Verify a signature previously made by a PrivateKey in our
                 * KeyStore. This uses the X.509 certificate attached to our
                 * private key in the KeyStore to validate a previously
                 * generated signature.
                 */
                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)
                val entry = ks.getEntry(alias, null)
                if (entry !is KeyStore.PrivateKeyEntry) {
                    Log.w(TAG, "Not an instance of a PrivateKeyEntry")
                    return false
                }
                val s = Signature.getInstance("SHA256withECDSA")
                s.initVerify(entry.certificate)
                s.update(data)
                s.verify(signature)
            } catch (e: NoSuchAlgorithmException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: KeyStoreException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: CertificateException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: IOException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: UnrecoverableEntryException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: InvalidKeyException) {
                Log.w(TAG, "Could not generate key", e)
                false
            } catch (e: SignatureException) {
                Log.w(TAG, "Could not generate key", e)
                false
            }
        }

        /**
         * Called on the UI thread after the background thread has finished verifying the signature.
         * If our parameter [result] is true we set the text color of [EditText] field [mCipherText]
         * to green, if false we set the text color of [EditText] field [mCipherText] to red. In
         * either case we call our method [setKeyActionButtonsEnabled] to re-enable the views
         * involved with the key actions.
         *
         * @param result true if the signature was verified, false if it was not.
         */
        override fun onPostExecute(result: Boolean) {
            if (result) {
                mCipherText!!.setTextColor(resources.getColor(R.color.solid_green, null))
            } else {
                mCipherText!!.setTextColor(resources.getColor(R.color.solid_red, null))
            }
            setKeyActionButtonsEnabled(true)
        }

        /**
         * Called on the UI thread when the background thread is cancelled. We set the text of
         * [EditText] field [mCipherText] to the string "error!", call our method
         * [setKeyActionButtonsEnabled] to re-enable the views involved with the key actions,
         * and set the text color of [EditText] field [mCipherText] to the color selected by the
         * android.R.color.primary_text_dark selector.
         */
        override fun onCancelled() {
            mCipherText!!.setText("error!")
            setKeyActionButtonsEnabled(true)
            @Suppress("DEPRECATION")
            mCipherText!!.setTextColor(resources.getColor(android.R.color.primary_text_dark, null))
        }
    }

    /**
     * [AsyncTask] used to delete an alias from the keystore.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class DeleteTask : AsyncTask<String?, Void?, Void?>() {
        /**
         * Deletes a previously generated or stored entry in the KeyStore. First we copy a reference
         * to our parameter to [String] `val alias`. We initialize [KeyStore] `val ks` with an
         * instance of [KeyStore] providing the "AndroidKeyStore" type, then instruct it to load.
         * We then call its `deleteEntry` method to delete the entry identified by `alias` from
         * the keystore. Finally we return null to the caller.
         *
         * @param params `params[0]` contains the alias that is to be deleted
         * @return Void.
         */
        override fun doInBackground(vararg params: String?): Void? {
            val alias = params[0]
            try {

                /*
                 * Deletes a previously generated or stored entry in the
                 * KeyStore.
                 */
                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)
                ks.deleteEntry(alias)
            } catch (e: NoSuchAlgorithmException) {
                Log.w(TAG, "Could not generate key", e)
            } catch (e: KeyStoreException) {
                Log.w(TAG, "Could not generate key", e)
            } catch (e: IOException) {
                Log.w(TAG, "Could not generate key", e)
            } catch (e: CertificateException) {
                Log.w(TAG, "Could not generate key", e)
            }
            return null
        }

        /**
         * Called on the UI thread when the background thread has finished. We simply call our
         * method [updateKeyList] to update the list of keys used by our UI.
         *
         * @param result unused, Void
         */
        override fun onPostExecute(result: Void?) {
            updateKeyList()
        }

        /**
         * Called on the UI thread when the background thread is cancelled. We simply call our
         * method [updateKeyList] to update the list of keys used by our UI.
         */
        override fun onCancelled() {
            updateKeyList()
        }
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "AndroidKeyStoreUsage"
    }
}
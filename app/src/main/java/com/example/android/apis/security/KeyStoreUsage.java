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

package com.example.android.apis.security;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Shows how to use api to generate Key pairs, sign and verify.
 */
@SuppressWarnings("CodeBlock2Expr")
@TargetApi(Build.VERSION_CODES.M)
@SuppressLint("SetTextI18n")
public class KeyStoreUsage extends AppCompatActivity {
    /**
     * TAG used for logging.
     */
    private static final String TAG = "AndroidKeyStoreUsage";

    /**
     * An instance of {@link java.security.KeyStore} through which this app
     * talks to the {@code AndroidKeyStore}. UNUSED
     */
    @SuppressWarnings("unused")
    KeyStore mKeyStore;

    /**
     * Used by the {@code ListView} in our layout to list the keys available in
     * our {@code KeyStore} by their alias names.
     */
    AliasAdapter mAdapter;

    /**
     * Button in the UI that causes a new keypair to be generated in the
     * {@code KeyStore}.
     */
    Button mGenerateButton;

    /**
     * Button in the UI that causes data to be signed by a key we selected from
     * the list available in the {@code KeyStore}.
     */
    Button mSignButton;

    /**
     * Button in the UI that causes data to be verified by a key we selected from
     * the list available in the {@code KeyStore}.
     */
    Button mVerifyButton;

    /**
     * Button in the UI that causes a key entry to be deleted from the
     * {@code KeyStore}.
     */
    Button mDeleteButton;

    /**
     * Text field in the UI that holds plaintext.
     */
    EditText mPlainText;

    /**
     * Text field in the UI that holds the signature.
     */
    EditText mCipherText;

    /**
     * The alias of the selected entry in the KeyStore.
     */
    private String mSelectedAlias;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.keystore_usage.
     * We locate the {@code ListView lv} with ID R.id.entries_list, allocate a new instance to for
     * {@code AliasAdapter mAdapter}, set it as the adapter for {@code lv}, set the choice mode of
     * {@code lv} to CHOICE_MODE_SINGLE and set its {@code OnItemClickListener} to an anonymous class
     * which sets {@code String mSelectedAlias} to the item that has been clicked and calls our method
     * {@code setKeyActionButtonsEnabled} to enable the views which need a key to use, namely
     * {@code EditText mPlainText}, {@code EditText mCipherText}, {@code Button mSignButton},
     * {@code Button mVerifyButton}, and {@code Button mDeleteButton}.
     * <p>
     * We locate the {@code EditText} with ID R.id.entry_name to set {@code EditText aliasInput}, and
     * the {@code Button} with ID R.id.generate_button to initialize our field {@code Button mGenerateButton},
     * then set the {@code OnClickListener} of {@code mGenerateButton} to an anonymous class which reads
     * the text from {@code EditText aliasInput} into {@code String alias}, checks to make sure it was
     * not empty (complaining about the error if it was) otherwise it clears any {@code aliasInput} error,
     * disables the {@code Button mGenerateButton} and starts the {@code AsyncTask GenerateTask} running
     * using {@code alias} as its argument.
     * <p>
     * Next we locate the button with ID R.id.sign_button to initialize our field {@code Button mSignButton},
     * set its {@code OnClickListener} to an anonymous class which sets {@code String alias} to the contents
     * of our field {@code String mSelectedAlias}, fetches the text from {@code EditText mPlainText} to
     * the variable {@code String data}, then if {@code alias} is not null calls {@code setKeyActionButtonsEnabled}
     * to temporarily disable the key action views, and then starts the {@code AsyncTask SignTask} running
     * using the arguments {@code alias} and {@code data}.
     * <p>
     * We locate the button with ID R.id.verify_button to initialize our field {@code Button mVerifyButton}, and
     * set its {@code OnClickListener} to an anonymous class which sets {@code String alias} to the contents
     * of our field {@code String mSelectedAlias}, fetches the text from {@code EditText mPlainText} to the variable
     * {@code String data}, fetches the text from {@code EditText mCipherText} to set {@code String signature}, then
     * if {@code alias} is not null calls {@code setKeyActionButtonsEnabled} to temporarily disable the key action
     * views, and then starts the {@code AsyncTask VerifyTask} running using the arguments {@code alias}, {@code data}
     * and {@code signature}.
     * <p>
     * We locate the button with ID R.id.delete_button to initialize our field {@code Button mDeleteButton}, and
     * set its {@code OnClickListener} to an anonymous class which sets {@code String alias} to the contents
     * of our field {@code String mSelectedAlias}, and if it is not null calls {@code setKeyActionButtonsEnabled}
     * to temporarily disable the key action views, and then starts the {@code AsyncTask DeleteTask} running using
     * the argument {@code alias}.
     * <p>
     * We locate the {@code EditText} with ID R.id.plaintext to initialize our field {@code EditText mPlainText} and
     * set its {@code OnFocusChangeListener} to an anonymous class which sets the color of the text to the correct
     * color for its state based on the values in android.R.color.primary_text_dark.
     * <p>
     * We locate the {@code EditText} with ID R.id.ciphertext to initialize our field {@code EditText mCipherText} and
     * set its {@code OnFocusChangeListener} to an anonymous class which sets the color of the text to the correct
     * color for its state based on the values in android.R.color.primary_text_dark.
     * <p>
     * Finally we call our method {@code updateKeyList} which calls {@code setKeyActionButtonsEnabled}
     * to temporarily disable the key action views, and then starts the {@code AsyncTask UpdateKeyListTask}
     * running.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.keystore_usage);

        /*
         * Set up our {@code ListView} with an adapter that allows
         * us to choose from the available entry aliases.
         */
        ListView lv = findViewById(R.id.entries_list);
        mAdapter = new AliasAdapter(getApplicationContext());
        lv.setAdapter(mAdapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            mSelectedAlias = mAdapter.getItem(position);
            setKeyActionButtonsEnabled(true);
        });

        // This is alias the user wants for a generated key.
        final EditText aliasInput = findViewById(R.id.entry_name);
        mGenerateButton = findViewById(R.id.generate_button);
        mGenerateButton.setOnClickListener(v -> {
            /*
             * When the user presses the "Generate" button, we'll
             * check the alias isn't blank here.
             */
            final String alias = aliasInput.getText().toString();
            //noinspection ConstantConditions
            if (alias == null || alias.length() == 0) {
                aliasInput.setError(getResources().getText(R.string.keystore_no_alias_error));
            } else {
                /*
                 * It's not blank, so disable the generate button while
                 * the generation of the key is happening. It will be
                 * enabled by the {@code AsyncTask} later after its
                 * work is done.
                 */
                aliasInput.setError(null);
                mGenerateButton.setEnabled(false);
                new GenerateTask().execute(alias);
            }
        });

        mSignButton = findViewById(R.id.sign_button);
        mSignButton.setOnClickListener(v -> {
            final String alias = mSelectedAlias;
            final String data = mPlainText.getText().toString();
            if (alias != null) {
                setKeyActionButtonsEnabled(false);
                new SignTask().execute(alias, data);
            }
        });

        mVerifyButton = findViewById(R.id.verify_button);
        mVerifyButton.setOnClickListener(v -> {
            final String alias = mSelectedAlias;
            final String data = mPlainText.getText().toString();
            final String signature = mCipherText.getText().toString();
            if (alias != null) {
                setKeyActionButtonsEnabled(false);
                new VerifyTask().execute(alias, data, signature);
            }
        });

        mDeleteButton = findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(v -> {
            final String alias = mSelectedAlias;
            if (alias != null) {
                setKeyActionButtonsEnabled(false);
                new DeleteTask().execute(alias);
            }
        });

        mPlainText = findViewById(R.id.plaintext);
        mPlainText.setOnFocusChangeListener((v, hasFocus) -> {
            mPlainText.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        });

        mCipherText = findViewById(R.id.ciphertext);
        mCipherText.setOnFocusChangeListener((v, hasFocus) -> {
            mCipherText.setTextColor(getResources()
                    .getColor(android.R.color.primary_text_dark));
        });

        updateKeyList();
    }

    /**
     * The {@code Adapter} we use for our {@code AliasAdapter mAdapter}, it stores the alias strings
     * the use has used in its {@code ArrayAdapter<String>}
     */
    @SuppressWarnings({"WeakerAccess", "InnerClassMayBeStatic"})
    private class AliasAdapter extends ArrayAdapter<String> {
        /**
         * Our constructor. We call our super's constructor specifying android.R.layout.simple_list_item_single_choice
         * as the resource ID for the layout file containing a TextView to use when instantiating views (it is a
         * {@code CheckedTextView}).
         *
         * @param context {@code Context} to use to access resources.
         */
        public AliasAdapter(Context context) {
            // We want users to choose a key, so use the appropriate layout.
            super(context, android.R.layout.simple_list_item_single_choice);
        }

        /**
         * This clears out all previous aliases and replaces it with the current entries. First we
         * remove all elements from the list, and then we add our parameter {@code List<String> items}
         * at the end of the array. Finally we call {@code notifyDataSetChanged} to notify the attached
         * observers that the underlying data has been changed and any View reflecting the data set
         * should refresh itself.
         *
         * @param items the list of alias entries we should now be using.
         */
        public void setAliases(List<String> items) {
            clear();
            addAll(items);
            notifyDataSetChanged();
        }
    }

    /**
     * Updates the list of keys. First we call our method {@code setKeyActionButtonsEnabled} to disable
     * the views used by the key actions: {@code EditText mPlainText}, {@code EditText mCipherText},
     * {@code Button mSignButton}, {@code Button mVerifyButton}, and {@code Button mDeleteButton}.
     * Then we start our {@code AsyncTask UpdateKeyListTask} running to do the actual updating.
     */
    private void updateKeyList() {
        setKeyActionButtonsEnabled(false);
        new UpdateKeyListTask().execute();
    }

    /**
     * Sets all the buttons related to actions that act on an existing key to enabled or disabled:
     * {@code EditText mPlainText}, {@code EditText mCipherText}, {@code Button mSignButton},
     * {@code Button mVerifyButton}, and {@code Button mDeleteButton}.
     */
    private void setKeyActionButtonsEnabled(boolean enabled) {
        mPlainText.setEnabled(enabled);
        mCipherText.setEnabled(enabled);
        mSignButton.setEnabled(enabled);
        mVerifyButton.setEnabled(enabled);
        mDeleteButton.setEnabled(enabled);
    }

    /**
     * {@code AsyncTask} which updates the list of aliases used by {@code AliasAdapter mAdapter}.
     */
    @SuppressLint("StaticFieldLeak")
    private class UpdateKeyListTask extends AsyncTask<Void, Void, Enumeration<String>> {
        /**
         * Returns an {@code Enumeration<String>} of all the names in the "AndroidKeyStore" keystore
         * object. First we fetch a {@code KeyStore} object of type "AndroidKeyStore" to initialize
         * {@code KeyStore ks}, we load {@code ks}, and then return an {@code Enumeration<String> aliases}
         * listing all of the alias names of this keystore.
         *
         * @param params we do not use params, so these are {@code Void}
         * @return An object that implements the Enumeration interface for {@code String} objects,
         * it generates a series of elements, one at a time. Successive calls to the nextElement
         * method return successive elements of the series.
         */
        @Override
        protected Enumeration<String> doInBackground(Void... params) {
            try {

                /*
                 * Load the Android KeyStore instance using the the
                 * "AndroidKeyStore" provider to list out what entries are
                 * currently stored.
                 */
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                @SuppressWarnings("UnnecessaryLocalVariable")
                Enumeration<String> aliases = ks.aliases();

                return aliases;
            } catch (KeyStoreException e) {
                Log.w(TAG, "Could not list keys", e);
                return null;
            } catch (NoSuchAlgorithmException e) {
                Log.w(TAG, "Could not list keys", e);
                return null;
            } catch (CertificateException e) {
                Log.w(TAG, "Could not list keys", e);
                return null;
            } catch (IOException e) {
                Log.w(TAG, "Could not list keys", e);
                return null;
            }
        }

        /**
         * Runs on the UI thread after {@code doInBackground}. The parameter {@code result} is the
         * value returned by {@code doInBackground}. First we create an {@code ArrayList} for
         * {@code List<String> aliases}, then we loop through all of the {@code String} objects in
         * our parameter {@code result} adding each of them to {@code aliases}. Finally we call the
         * method {@code mAdapter.setAliases} to clear out all previous aliases and replace them with
         * the contents of {@code aliases}.
         *
         * @param result The list of {@code KeyStore} aliases computed by {@code doInBackground}.
         */
        @Override
        protected void onPostExecute(Enumeration<String> result) {
            List<String> aliases = new ArrayList<>();
            while (result.hasMoreElements()) {
                aliases.add(result.nextElement());
            }
            mAdapter.setAliases(aliases);
        }
    }

    /**
     * {@code AsyncTask} which is run to generate a new EC key pair entry in the Android Keystore.
     */
    @SuppressLint("StaticFieldLeak")
    private class GenerateTask extends AsyncTask<String, Void, Boolean> {
        /**
         * Generate a new EC key pair entry in the Android Keystore by using the KeyPairGenerator API.
         * First we set {@code String alias} from our parameter {@code params[0]} then we set
         * {@code KeyPairGenerator kpg} to a KeyPairGenerator object that generates public/private
         * key pairs for the KEY_ALGORITHM_EC algorithm using the KeyPairGeneratorSpi implementation
         * from the "AndroidKeyStore" provider. We initialize {@code kpg} using a
         * {@code KeyGenParameterSpec.Builder} which uses {@code alias} as the alias of the entry in
         * which the generated key will appear in Android KeyStore, and whose purpose is both
         * PURPOSE_SIGN and PURPOSE_VERIFY, whose digests algorithms we set to DIGEST_SHA256 and
         * DIGEST_SHA512 and then build. Finally we instruct {@code kpg} to generate a new key pair,
         * and return true to the caller.
         *
         * @param params The alias for the key.
         * @return true if successful, false if and exception is thrown (it is ignored though)
         */
        @Override
        protected Boolean doInBackground(String... params) {
            final String alias = params[0];
            try {

                /*
                 * Generate a new EC key pair entry in the Android Keystore by
                 * using the KeyPairGenerator API. The private key can only be
                 * used for signing or verification and only with SHA-256 or
                 * SHA-512 as the message digest.
                 */
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC,
                        "AndroidKeyStore");
                kpg.initialize(new KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .build());

                @SuppressWarnings("unused")
                KeyPair kp = kpg.generateKeyPair();

                return true;
            } catch (NoSuchAlgorithmException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (InvalidAlgorithmParameterException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (NoSuchProviderException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            }
        }

        /**
         * Called on the UI thread when the background task returns. We call our method {@code updateKeyList}
         * to update the list of keys, and then enable the {@code Button mGenerateButton}.
         *
         * @param result we ignore this.
         */
        @Override
        protected void onPostExecute(Boolean result) {
            updateKeyList();
            mGenerateButton.setEnabled(true);
        }

        /**
         * Called if the background task is cancelled, we simply enable the {@code Button mGenerateButton}.
         */
        @Override
        protected void onCancelled() {
            mGenerateButton.setEnabled(true);
        }
    }

    /**
     * {@code AsyncTask} which is run to create a signature for some data.
     */
    @SuppressLint("StaticFieldLeak")
    private class SignTask extends AsyncTask<String, Void, String> {
        /**
         * Uses the keystore entry specified by {@code params[0]} to sign the data specified by
         * {@code params[1]} and returns the resulting signature. First we copy references to our
         * two parameters to {@code String alias} and {@code String dataString}. Then we load
         * {@code byte[] data} with the {@code byte[]} version of {@code dataString}. We initialize
         * {@code KeyStore ks} with an instance of {@code KeyStore} providing the "AndroidKeyStore"
         * type, then instruct it to load. We fetch from {@code ks} the keystore entry for the alias
         * {@code String alias} to initialize {@code KeyStore.Entry entry}. If {@code entry} is not
         * an instance of {@code PrivateKeyEntry} we log the error and return null. Otherwise we
         * initialize {@code Signature s} with an instance that implements the "SHA256withECDSA"
         * signature algorithm. We initialize {@code s} for signing using the {@code PrivateKey} of
         * {@code entry}, update the data to be signed or verified by {@code s}, and then use {@code s}
         * to initialize {@code byte[] signature} with the signature bytes of all the data updated.
         * We then return {@code byte[] signature} as a Base64-encoded string using Base64.DEFAULT.
         *
         * @param params the alias in the keystore to use ({@code params[0]}), and the data that we
         *               are to sign ({@code params[1]}).
         * @return The signature of the data using the keystore alias entry passed us
         */
        @Override
        protected String doInBackground(String... params) {
            final String alias = params[0];
            final String dataString = params[1];
            try {
                byte[] data = dataString.getBytes();

                /*
                 * Use a PrivateKey in the KeyStore to create a signature over
                 * some data.
                 */
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (!(entry instanceof PrivateKeyEntry)) {
                    Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                    return null;
                }
                Signature s = Signature.getInstance("SHA256withECDSA");
                s.initSign(((PrivateKeyEntry) entry).getPrivateKey());
                s.update(data);
                byte[] signature = s.sign();

                return Base64.encodeToString(signature, Base64.DEFAULT);
            } catch (NoSuchAlgorithmException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            } catch (KeyStoreException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            } catch (CertificateException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            } catch (IOException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            } catch (UnrecoverableEntryException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            } catch (InvalidKeyException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            } catch (SignatureException e) {
                Log.w(TAG, "Could not generate key", e);
                return null;
            }
        }

        /**
         * When the background task completes, we are called on the UI thread with our parameter
         * {@code String result} containing the signature it computed. We set the text of
         * {@code EditText mCipherText} to our parameter {@code result}, and call our method
         * {@code setKeyActionButtonsEnabled} to re-enable the views involved with the key actions.
         *
         * @param result signature of the data returned by the background task
         */
        @Override
        protected void onPostExecute(String result) {
            mCipherText.setText(result);
            setKeyActionButtonsEnabled(true);
        }

        /**
         * Called on the UI thread when the background thread is cancelled. We set the text of
         * {@code EditText mCipherText} to "error!", and call our method {@code setKeyActionButtonsEnabled}
         * to re-enable the views involved with the key actions.
         */
        @Override
        protected void onCancelled() {
            mCipherText.setText("error!");
            setKeyActionButtonsEnabled(true);
        }
    }

    /**
     * {@code AsyncTask} which is run to verify a signature.
     */
    @SuppressLint("StaticFieldLeak")
    private class VerifyTask extends AsyncTask<String, Void, Boolean> {
        /**
         * Verifies a signature in the background. First we copy references to our three parameters to
         * {@code String alias}, {@code String dataString} and {@code String signatureString}. We
         * initialize {@code byte[] data} with the byte version of {@code dataString}, declare
         * {@code byte[] signature} and try to decode the Base64 contained in {@code signatureString}
         * into it (setting {@code byte[]} to an zero element array if it fails). We initialize
         * {@code KeyStore ks} with an instance of {@code KeyStore} providing the "AndroidKeyStore"
         * type, then instruct it to load. We fetch from {@code ks} the keystore entry for the alias
         * {@code String alias} to initialize {@code KeyStore.Entry entry}. If {@code entry} is not
         * an instance of {@code PrivateKeyEntry} we log the error and return false. Otherwise we
         * initialize {@code Signature s} with an instance that implements the "SHA256withECDSA"
         * signature algorithm. We initialize {@code s} for verification, using the public key from
         * the end entity Certificate from the certificate chain of {@code entry}, update the data
         * verified by {@code s}, and then use {@code s} to verify {@code signature} setting our
         * variable {@code boolean valid} to the result of the verification which we return to the
         * caller.
         *
         * @param params {@code params[0]} contains the alias to use from the keystore, {@code params[1]}
         *               contains the data that has been signed, and {@code params[2]} contains the
         *               signature to be verified.
         * @return true if the signature is valid, false if it is not
         */
        @Override
        protected Boolean doInBackground(String... params) {
            final String alias = params[0];
            final String dataString = params[1];
            final String signatureString = params[2];
            try {
                byte[] data = dataString.getBytes();
                byte[] signature;
                try {
                    signature = Base64.decode(signatureString, Base64.DEFAULT);
                } catch (IllegalArgumentException e) {
                    signature = new byte[0];
                }

                /*
                 * Verify a signature previously made by a PrivateKey in our
                 * KeyStore. This uses the X.509 certificate attached to our
                 * private key in the KeyStore to validate a previously
                 * generated signature.
                 */
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (!(entry instanceof PrivateKeyEntry)) {
                    Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                    return false;
                }
                Signature s = Signature.getInstance("SHA256withECDSA");
                s.initVerify(((PrivateKeyEntry) entry).getCertificate());
                s.update(data);
                @SuppressWarnings("UnnecessaryLocalVariable")
                boolean valid = s.verify(signature);

                return valid;
            } catch (NoSuchAlgorithmException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (KeyStoreException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (CertificateException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (IOException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (UnrecoverableEntryException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (InvalidKeyException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            } catch (SignatureException e) {
                Log.w(TAG, "Could not generate key", e);
                return false;
            }
        }

        /**
         * Called on the UI thread after the background thread has finished verifying the signature.
         * If our parameter {@code result} is true we set the text color of {@code EditText mCipherText}
         * to green, if false we set the text color of {@code EditText mCipherText} to red. In either
         * case we call our method {@code setKeyActionButtonsEnabled} to re-enable the views involved
         * with the key actions.
         *
         * @param result true if the signature was verified, false if it was not.
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mCipherText.setTextColor(getResources().getColor(R.color.solid_green));
            } else {
                mCipherText.setTextColor(getResources().getColor(R.color.solid_red));
            }
            setKeyActionButtonsEnabled(true);
        }

        /**
         * Called on the UI thread when the background thread is cancelled. We set the text of
         * {@code EditText mCipherText} to the string "error!", call our method
         * {@code setKeyActionButtonsEnabled} to re-enable the views involved with the key actions,
         * and set the text color of {@code EditText mCipherText} to android.R.color.primary_text_dark
         */
        @Override
        protected void onCancelled() {
            mCipherText.setText("error!");
            setKeyActionButtonsEnabled(true);
            mCipherText.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        }
    }

    /**
     * {@code AsyncTask} used to delete an alias from the keystore.
     */
    @SuppressLint("StaticFieldLeak")
    private class DeleteTask extends AsyncTask<String, Void, Void> {
        /**
         * Deletes a previously generated or stored entry in the KeyStore. First we copy a reference
         * to our parameter to {@code String alias}. We initialize {@code KeyStore ks} with an instance
         * of {@code KeyStore} providing the "AndroidKeyStore" type, then instruct it to load. We then
         * call its {@code deleteEntry} method to delete the entry identified by {@code alias} from
         * the keystore. Finally we return null to the caller.
         *
         * @param params {@code params[0]} contains the alias that is to be deleted
         * @return Void.
         */
        @Override
        protected Void doInBackground(String... params) {
            final String alias = params[0];
            try {

                /*
                 * Deletes a previously generated or stored entry in the
                 * KeyStore.
                 */
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                ks.deleteEntry(alias);

            } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException e) {
                Log.w(TAG, "Could not generate key", e);
            }
            return null;
        }

        /**
         * Called on the UI thread when the background thread has finished. We simply call our method
         * {@code updateKeyList} to update the list of keys used by our UI.
         *
         * @param result unused, Void
         */
        @Override
        protected void onPostExecute(Void result) {
            updateKeyList();
        }

        /**
         * Called on the UI thread when the background thread is cancelled. We simply call our method
         * {@code updateKeyList} to update the list of keys used by our UI.
         */
        @Override
        protected void onCancelled() {
            updateKeyList();
        }
    }
}

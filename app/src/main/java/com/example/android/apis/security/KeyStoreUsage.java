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
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
@TargetApi(Build.VERSION_CODES.M)
@SuppressLint("SetTextI18n")
public class KeyStoreUsage extends Activity {
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
     *
     * We locate the {@code EditText} with ID R.id.entry_name to set {@code EditText aliasInput}, and
     * the {@code Button} with ID R.id.generate_button to initialize our field {@code Button mGenerateButton},
     * then set the {@code OnClickListener} of {@code mGenerateButton} to an anonymous class which reads
     * the text from {@code EditText aliasInput} into {@code String alias}, checks to make sure it was
     * not empty (complaining about the error if it was) otherwise it clears any {@code aliasInput} error,
     * disables the {@code Button mGenerateButton} and starts the {@code AsyncTask GenerateTask} running
     * using {@code alias} as its argument.
     *
     * Next we locate the button with ID R.id.sign_button to initialize our field {@code Button mSignButton},
     * set its {@code OnClickListener} to an anonymous class which sets {@code String alias} to the contents
     * of our field {@code String mSelectedAlias}, fetches the text from {@code EditText mPlainText} to
     * the variable {@code String data}, then if {@code alias} is not null calls {@code setKeyActionButtonsEnabled}
     * to temporarily disable the key action views, and then starts the {@code AsyncTask SignTask} running
     * using the arguments {@code alias} and {@code data}.
     *
     * We locate the button with ID R.id.verify_button to initialize our field {@code Button mVerifyButton}, and
     * set its {@code OnClickListener} to an anonymous class which sets {@code String alias} to the contents
     * of our field {@code String mSelectedAlias}, fetches the text from {@code EditText mPlainText} to the variable
     * {@code String data}, fetches the text from {@code EditText mCipherText} to set {@code String signature}, then
     * if {@code alias} is not null calls {@code setKeyActionButtonsEnabled} to temporarily disable the key action
     * views, and then starts the {@code AsyncTask VerifyTask} running using the arguments {@code alias}, {@code data}
     * and {@code signature}.
     *
     * We locate the button with ID R.id.delete_button to initialize our field {@code Button mDeleteButton}, and
     * set its {@code OnClickListener} to an anonymous class which sets {@code String alias} to the contents
     * of our field {@code String mSelectedAlias}, and if it is not null calls {@code setKeyActionButtonsEnabled}
     * to temporarily disable the key action views, and then starts the {@code AsyncTask DeleteTask} running using
     * the argument {@code alias}.
     *
     * We locate the {@code EditText} with ID R.id.plaintext to initialize our field {@code EditText mPlainText} and
     * set its {@code OnFocusChangeListener} to an anonymous class which sets the color of the text to the correct
     * color for its state based on the values in android.R.color.primary_text_dark.
     *
     * We locate the {@code EditText} with ID R.id.ciphertext to initialize our field {@code EditText mCipherText} and
     * set its {@code OnFocusChangeListener} to an anonymous class which sets the color of the text to the correct
     * color for its state based on the values in android.R.color.primary_text_dark.
     *
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
        ListView lv = (ListView) findViewById(R.id.entries_list);
        mAdapter = new AliasAdapter(getApplicationContext());
        lv.setAdapter(mAdapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedAlias = mAdapter.getItem(position);
                setKeyActionButtonsEnabled(true);
            }
        });

        // This is alias the user wants for a generated key.
        final EditText aliasInput = (EditText) findViewById(R.id.entry_name);
        mGenerateButton = (Button) findViewById(R.id.generate_button);
        mGenerateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        mSignButton = (Button) findViewById(R.id.sign_button);
        mSignButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String alias = mSelectedAlias;
                final String data = mPlainText.getText().toString();
                if (alias != null) {
                    setKeyActionButtonsEnabled(false);
                    new SignTask().execute(alias, data);
                }
            }
        });

        mVerifyButton = (Button) findViewById(R.id.verify_button);
        mVerifyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String alias = mSelectedAlias;
                final String data = mPlainText.getText().toString();
                final String signature = mCipherText.getText().toString();
                if (alias != null) {
                    setKeyActionButtonsEnabled(false);
                    new VerifyTask().execute(alias, data, signature);
                }
            }
        });

        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String alias = mSelectedAlias;
                if (alias != null) {
                    setKeyActionButtonsEnabled(false);
                    new DeleteTask().execute(alias);
                }
            }
        });

        mPlainText = (EditText) findViewById(R.id.plaintext);
        mPlainText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //noinspection deprecation
                mPlainText.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
            }
        });

        mCipherText = (EditText) findViewById(R.id.ciphertext);
        mCipherText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //noinspection deprecation
                mCipherText.setTextColor(getResources()
                           .getColor(android.R.color.primary_text_dark));
            }
        });

        updateKeyList();
    }

    /**
     * The {@code Adapter} we use for our {@code AliasAdapter mAdapter}, it stores the alias strings
     * the use has used in its {@code ArrayAdapter<String>}
     */
    @SuppressWarnings("WeakerAccess")
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
     * Updates the list of keys.
     */
    private void updateKeyList() {
        setKeyActionButtonsEnabled(false);
        new UpdateKeyListTask().execute();
    }

    /**
     * Sets all the buttons related to actions that act on an existing key to
     * enabled or disabled.
     */
    private void setKeyActionButtonsEnabled(boolean enabled) {
        mPlainText.setEnabled(enabled);
        mCipherText.setEnabled(enabled);
        mSignButton.setEnabled(enabled);
        mVerifyButton.setEnabled(enabled);
        mDeleteButton.setEnabled(enabled);
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateKeyListTask extends AsyncTask<Void, Void, Enumeration<String>> {
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

        @Override
        protected void onPostExecute(Enumeration<String> result) {
            List<String> aliases = new ArrayList<>();
            while (result.hasMoreElements()) {
                aliases.add(result.nextElement());
            }
            mAdapter.setAliases(aliases);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GenerateTask extends AsyncTask<String, Void, Boolean> {
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
                        KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
                kpg.initialize(new KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA512)
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

        @Override
        protected void onPostExecute(Boolean result) {
            updateKeyList();
            mGenerateButton.setEnabled(true);
        }

        @Override
        protected void onCancelled() {
            mGenerateButton.setEnabled(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SignTask extends AsyncTask<String, Void, String> {
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

        @Override
        protected void onPostExecute(String result) {
            mCipherText.setText(result);
            setKeyActionButtonsEnabled(true);
        }

        @Override
        protected void onCancelled() {
            mCipherText.setText("error!");
            setKeyActionButtonsEnabled(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class VerifyTask extends AsyncTask<String, Void, Boolean> {
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

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mCipherText.setTextColor(getResources().getColor(R.color.solid_green));
            } else {
                mCipherText.setTextColor(getResources().getColor(R.color.solid_red));
            }
            setKeyActionButtonsEnabled(true);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onCancelled() {
            mCipherText.setText("error!");
            setKeyActionButtonsEnabled(true);
            mCipherText.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteTask extends AsyncTask<String, Void, Void> {
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

        @Override
        protected void onPostExecute(Void result) {
            updateKeyList();
        }

        @Override
        protected void onCancelled() {
            updateKeyList();
        }
    }
}

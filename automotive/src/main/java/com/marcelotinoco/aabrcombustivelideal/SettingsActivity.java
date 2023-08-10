package com.marcelotinoco.aabrcombustivelideal;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.connection.CarConnection;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new CarConnection(this).getType().observe(this, this::onConnectionStateUpdated);

        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        LocaleHelper.setLocale(this, preferences.getString("display_language", Locale.getDefault().getLanguage()));

        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Log.d("TAG", Locale.getDefault().toLanguageTag());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                        PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS}, 1);
        }


    }

    private void onConnectionStateUpdated(int connectionState) {
        String message;
        switch (connectionState) {
            case CarConnection.CONNECTION_TYPE_NOT_CONNECTED:
                message = "Not connected to a head unit";
                isConnected = false;
                break;
            case CarConnection.CONNECTION_TYPE_NATIVE:
                message = "Connected to Android Automotive OS";
                Toast.makeText(this, "Cannot open settings activity while Android auto is connected.", Toast.LENGTH_LONG);
                this.finish();
                isConnected = true;
                break;
            case CarConnection.CONNECTION_TYPE_PROJECTION:
                message = "Connected to Android Auto";
                Toast.makeText(this, "Cannot open settings activity while Android auto is connected.", Toast.LENGTH_LONG);
                this.finish();
                break;
            default:
                message = "Unknown car connection type";
                isConnected = false;
                break;
        }
//        CarToast.makeText(getCarContext(), message, CarToast.LENGTH_SHORT).show();
        Log.d("Connection", message);

    }

    @Override
    public void onResume() {
        if (isConnected) {
            Toast.makeText(this, "Cannot open settings activity while Android auto is connected.", Toast.LENGTH_LONG);
            this.finish();
        }
        super.onResume();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("display_language")) {
            //Log.d("Prefs", "Changed " + key + " value " + sharedPreferences.getString(key,Locale.getDefault().getLanguage() ));
            LocaleHelper.setLocale(this, sharedPreferences.getString(key, Locale.getDefault().getLanguage()));
            finish();
            startActivity(getIntent());
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (!preferences.contains("speech_recognition_language")) {
                initLanguages();
            }

            EditTextPreference editTextPreference = getPreferenceManager().findPreference("confirmation_delay");
            editTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER  );

                }
            });
        }

        private void initLanguages() {
            final ListPreference listPreference = findPreference("speech_recognition_language");
            listPreference.setValue(Locale.getDefault().toLanguageTag());
        }
    }
}
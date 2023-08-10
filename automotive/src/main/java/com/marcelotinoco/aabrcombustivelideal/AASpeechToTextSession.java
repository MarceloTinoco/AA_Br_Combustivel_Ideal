package com.marcelotinoco.aabrcombustivelideal;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.ScreenManager;
import androidx.car.app.Session;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AASpeechToTextSession extends Session {
    public static final String TAG = "AASpeechToTextSession";
    public static final Integer RecordAudioRequestCode = 1;
    public AASpeechToTextService context;
    SpeechRecognizer speechRecognizer;

    Loader.OnLoadCompleteListener<Cursor> listener = (loader, cursor) -> {
        Map<String, String> results = new HashMap<>();
        if (cursor != null && !cursor.moveToFirst()) {
            Log.d(TAG, "No contact found");
        } else {
            do {
                String number = cursor.getString(4);

                if (number != null) {
                    String contact_name = cursor.getString(3);
                    if (results.get(number) == null || contact_name.length() > results.get(number).length()) {
                        results.put(number, contact_name);
                    }
                    Log.d(TAG, "Contact Info:");
                    Log.d(TAG, "Value:" + cursor.getString(1));
                    Log.d(TAG, "Value:" + cursor.getString(2));
                    Log.d(TAG, "Value:" + cursor.getString(3));
                    Log.d(TAG, "Value:" + cursor.getString(4));
                }
            } while (cursor.moveToNext());
        }
        ScreenManager screenManager = getCarContext().getCarService(ScreenManager.class);
        screenManager.push(new ContactsScreen(getCarContext(), results));
    };

    public AASpeechToTextSession(AASpeechToTextService context, SpeechRecognizer speechRecognizer) {
        this.context = context;
        this.speechRecognizer = speechRecognizer;


    }

    @Override
    @NonNull
    public Screen onCreateScreen(@NonNull Intent intent) {

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                showToast(getCarContext().getResources().getString(R.string.recording_in_progress));
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
                showToast(getCarContext().getResources().getString(R.string.recording_complete));
            }

            @Override
            public void onError(int i) {
                String description = "";
                switch (i) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        description = "ERROR_AUDIO";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        showToast(getCarContext().getResources().getString(R.string.speech_recongition_cancelled));
                        return;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        description = "ERROR_INSUFFICIENT_PERMISSIONS";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        description = "ERROR_NETWORK";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        description = "ERROR_NETWORK_TIMEOUT";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        showToast(getCarContext().getResources().getString(R.string.voice_command_not_recognized));
                        return;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        description = "ERROR_RECOGNIZER_BUSY";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        description = "ERROR_SERVER";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        description = "ERROR_SPEECH_TIMEOUT";
                        break;
                }
                showToast("onError " + i + " " + description);
            }

            @Override
            public void onResults(Bundle bundle) {
//                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d(TAG, "the string is   " + data);
                //                editText.setText(data.get(0));


                String[] PROJECTION =
                        {
                                ContactsContract.Contacts._ID,
                                ContactsContract.Contacts.LOOKUP_KEY,
                                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                        };
                String SELECTION =

                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? OR " +
                                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";

                if (data.get(0) != null) {
                    String[] sprit_arr = data.get(0).split(" ", 2);

                    String firstword = Normalizer.normalize(sprit_arr[0], Normalizer.Form.NFD)
                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    Log.d(TAG, "the first word is " + firstword);

                    String contact_command = Normalizer.normalize(getCarContext().getResources().getString(R.string.contact_hot_word), Normalizer.Form.NFD)
                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

                    Log.d(TAG, "the contact_command is   " + contact_command);

                    String language_command = Normalizer.normalize(getCarContext().getResources().getString(R.string.language_hot_word), Normalizer.Form.NFD)
                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

                    Log.d(TAG, "the language_command is   " + language_command);


                    if (contact_command.equalsIgnoreCase(firstword)) {
                        if (sprit_arr.length > 1) {
                            Log.d(TAG, "Matched contact command   " + contact_command);

                            String contact_string = sprit_arr[1];
                            showToast(getCarContext().getResources().getString(R.string.contact_hot_word) + " " + contact_string);

                            Log.d(TAG, "Search for contact: " + contact_string);
                            // OPTIONAL: Makes search string into pattern
                            String searchString = "%" + contact_string + "%";
                            // Puts the search string into the selection criteria
                            String[] selectionArgs = new String[2];

                            selectionArgs[0] = searchString;
                            selectionArgs[1] = Normalizer.normalize(searchString, Normalizer.Form.NFD)
                                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                            // Starts the query
                            CursorLoader res = new CursorLoader(
                                    context,
                                    ContactsContract.Data.CONTENT_URI,
                                    PROJECTION,
                                    SELECTION,
                                    selectionArgs,
                                    null
                            );

                            Log.d(TAG, "The cursor loader is: " + res);

                            if (ContextCompat.checkSelfPermission(getCarContext(), Manifest.permission.READ_CONTACTS) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                res.registerListener(0, listener);
                                res.startLoading();
                            } else {

                                CarToast.makeText(getCarContext(), getCarContext().getResources().getString(R.string.open_app_settings_in_phone_to_grant_recording_permissions), CarToast.LENGTH_LONG).show();
                            }

                        }
                    }
                    else if (language_command.equalsIgnoreCase(firstword)) {
                        if (sprit_arr.length > 1) {
                            Log.d(TAG, "Matched language command   " + language_command);

                            String language_string = sprit_arr[1];


                            String [] languages = context.getResources().getStringArray(R.array.speech_recognition_language_entries);
                            String [] languageIds = context.getResources().getStringArray(R.array.speech_recognition_language_values);
                            boolean found = false;
                            for (int i = 0; i < languages.length; i++) {
                                if (languages[i].toLowerCase().contains(language_string.toLowerCase())) {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCarContext());
                                    preferences.edit()
                                            .putString("speech_recognition_language", languageIds[i])
                                            .commit();
                                    showToast(getCarContext().getResources().getString(R.string.language_hot_word) + " " + languages[i]);
                                    found = true;
                                    break;

                                }
                            }
                            if (!found) {
                                showToast(getCarContext().getResources().getString(R.string.language_hot_word) + " " + language_string + " NOT FOUND");
                            }

                            String [] displayLanguages = context.getResources().getStringArray(R.array.display_language_entries);
                            String [] displayLanguageIds = context.getResources().getStringArray(R.array.display_language_values);
                            found = false;
                            for (int i = 0; i < displayLanguages.length; i++) {
                                if (displayLanguages[i].toLowerCase().contains(language_string.toLowerCase())) {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCarContext());
                                    preferences.edit()
                                            .putString("display_language", displayLanguageIds[i])
                                            .commit();
                                    found = true;
                                    break;

                                }
                            }
                            if (!found) {
                                showToast(getCarContext().getResources().getString(R.string.display_language) + " " + language_string + " NOT FOUND");
                            }

                        }
                    }
                    else {
                        ScreenManager screenManager = getCarContext().getCarService(ScreenManager.class);
                        screenManager.push(new ConfirmScreen(getCarContext(), data));
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        return new AASpeechToTextScreen(getCarContext(), this);
    }

    private void showToast(String message) {

        CarToast

                .makeText(getCarContext(), message, CarToast.LENGTH_LONG)
                .show();
    }
}

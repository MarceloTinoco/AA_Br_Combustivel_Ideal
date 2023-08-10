package com.marcelotinoco.aabrcombustivelideal;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.CarText;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.SectionedItemList;
import androidx.car.app.model.Template;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import java.util.Locale;

/**
 * A screen that shows a simple "Hello World!" message.
 *
 * <p>See {@link AASpeechToTextScreen} for the app's entry point to Android Auto.
 */
public class AASpeechToTextScreen extends Screen implements DefaultLifecycleObserver {
    private AASpeechToTextSession aaSpeechToTextSession;
    Intent speechRecognizerIntent;

//    private final static String TAG = "Fale o preço assim: Cinco ponto zero dois (5.02)";

    protected AASpeechToTextScreen(@NonNull CarContext carContext, AASpeechToTextSession aaSpeechToTextSession) {
        super(carContext);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCarContext());
        LocaleHelper.setLocale(getCarContext(), preferences.getString("display_language", Locale.getDefault().getLanguage()));

        this.aaSpeechToTextSession = aaSpeechToTextSession;

        this.getLifecycle().addObserver(this);
    }


    @NonNull
    @Override
    public Template onGetTemplate() {

//        CarToast.makeText(getCarContext(), getCarContext().getResources().getString(R.string.opening_mas_in), CarToast.LENGTH_LONG).show()
//        Log.d(TAG, "");

        return new ListTemplate.Builder()
                .setHeaderAction(Action.APP_ICON)
                .setTitle("AA Br COMBUSTÍVEL IDEAL")
//                .setText(getCarContext().getString(R.string.say_address))
//                .setTitle("Fale o preço assim: Cinco ponto zero dois (5.02)")
                .addSectionedList(SectionedItemList.create(new ItemList.Builder()
//                                .addItem(new Row.Builder()
//                                        .setTitle(getCarContext().getResources().getString(R.string.title_how_to))
//                                        .build())
//                                .addItem(new Row.Builder()
//                                        .setTitle(getCarContext().getResources().getString(R.string.title_how_to_speak))
//                                        .build())
                                .addItem(new Row.Builder()
                                        .setTitle("Clique e fale o preço da Gasolina:").setOnClickListener(() -> {
                                            startListening(speechRecognizerIntent);
                                        })
                                        .setImage(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_baseline_mic_none_24)).build())
                                        .build())
                                .addItem(new Row.Builder()
                                        .setTitle("Clique e fale o preço do Álcool:").setOnClickListener(() -> {
                                            startListening(speechRecognizerIntent);
                                        })
                                        .setImage(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_baseline_mic_none_24)).build())
                                        .build())
                                .addItem(new Row.Builder()
                                        .setTitle("Fale o preço/GNV(sem GNV fale 0.00):").setOnClickListener(() -> {
                                            startListening(speechRecognizerIntent);
                                        })
                                        .setImage(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_baseline_mic_none_24)).build())
                                        .build())
                                .addItem(new Row.Builder()
                                        .setTitle("CALCULAR:").setOnClickListener(() -> {
                                            aaSpeechToTextSession.speechRecognizer.stopListening();
                                        })
                                        .setImage(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.baseline_local_gas_station_24)).build())
                                        .build())
//                                .addItem(new Row.Builder()
//                                       .setTitle(getCarContext().getResources().getString(R.string.speech_recognition_language)
//                                                + ": " + PreferenceManager.getDefaultSharedPreferences(getCarContext())
//                                                .getString("speech_recognition_language", Locale.getDefault().getLanguage()))
//                                        .build())
                                .build(),
                        getCarContext().getResources().getString(R.string.say_address)
                                + getCarContext().getResources().getString(R.string.contact_hot_word)
                                + getCarContext().getResources().getString(R.string.contact_name_or)
                                + getCarContext().getResources().getString(R.string.language_hot_word)
                                + getCarContext().getResources().getString(R.string.language_name)
                ))
                .build();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        Log.d("TAG", "On pause");
        aaSpeechToTextSession.speechRecognizer.cancel();
    }

    void startListening(Intent speechRecognizerIntent) {

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        int duration = 500;
        toneGen1.startTone(ToneGenerator.TONE_PROP_PROMPT, duration);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                aaSpeechToTextSession.speechRecognizer.cancel();
                aaSpeechToTextSession.speechRecognizer.startListening(speechRecognizerIntent);
            }
        }, duration);

    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCarContext());

        String language = Locale.getDefault().toLanguageTag();
        language = preferences.getString("speech_recognition_language", language);

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, language);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, language);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_RESULTS, language);

        aaSpeechToTextSession.speechRecognizer.cancel();

        if (ContextCompat.checkSelfPermission(getCarContext(), Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {

//            startListening(speechRecognizerIntent);
            aaSpeechToTextSession.speechRecognizer.stopListening();
        } else {
            CarToast.makeText(getCarContext(), getCarContext().getResources().getString(R.string.open_in_phone_for_permissions), CarToast.LENGTH_LONG).show();
        }
    }

}

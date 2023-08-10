package com.marcelotinoco.aabrcombustivelideal;

import android.content.pm.ApplicationInfo;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

/**
 * Entry point for the hello world app.
 *
 * <p>{@link CarAppService} is the main interface between the app and Android Auto. For more
 * details, see the <a href="https://developer.android.com/training/cars/navigation">Android for
 * Cars Library developer guide</a>.
 */
public final class AASpeechToTextService extends CarAppService {


    private static final String TAG = "AASpeechToTextService";
    SpeechRecognizer speechRecognizer;

    @Override
    @NonNull
    public Session onCreateSession() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        return new AASpeechToTextSession(this, speechRecognizer);
    }


    @NonNull
    @Override
    public HostValidator createHostValidator() {
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
        } else {
            return new HostValidator.Builder(getApplicationContext())
                    .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                    .build();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        speechRecognizer.cancel();
        speechRecognizer.destroy();
    }
}
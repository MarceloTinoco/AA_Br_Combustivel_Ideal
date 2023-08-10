package com.marcelotinoco.aabrcombustivelideal;

import static androidx.car.app.model.Action.BACK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;

import java.util.Map;

/**
 * Creates a screen that demonstrates usage of the full screen {@link ListTemplate} to display a
 * full-screen list.
 */
public final class ContactsScreen extends Screen implements DefaultLifecycleObserver {
    Map<String, String> results;

    public ContactsScreen(@NonNull CarContext carContext, Map<String, String> results) {
        super(carContext);
        getLifecycle().addObserver(this);
        this.results = results;
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Log.d("AAAAAAAAA", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTt");
        ItemList.Builder listBuilder = new ItemList.Builder();

        for (Map.Entry<String, String> entry : results.entrySet()) {
            listBuilder.addItem(
                    new Row.Builder()
                            .setOnClickListener(() -> {
                                Log.d("TAG", "Contact " + entry.getKey() + " Clicked");
                                Intent intent = new Intent(Intent.ACTION_CALL);

                                intent.setData(Uri.parse("tel:" + entry.getKey()));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                if (ContextCompat.checkSelfPermission(getCarContext(), Manifest.permission.CALL_PHONE) ==
                                        PackageManager.PERMISSION_GRANTED) {
                                    getCarContext().startCarApp(intent);
                                } else {
                                    CarToast.makeText(getCarContext(), getCarContext().getResources().getString(R.string.open_app_settings_in_phone_to_grant_recording_permissions), CarToast.LENGTH_LONG).show();
                                }

                            })
                            .setTitle(entry.getValue())
                            .addText(entry.getKey())
                            .build());
        }
        return new ListTemplate.Builder()
                .setSingleList(listBuilder.build())
                .setTitle(getCarContext().getResources().getString(R.string.contacts))
                .setHeaderAction(BACK)
                .build();
    }

}

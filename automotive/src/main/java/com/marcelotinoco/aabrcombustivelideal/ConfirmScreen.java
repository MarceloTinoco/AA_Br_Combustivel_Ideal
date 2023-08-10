package com.marcelotinoco.aabrcombustivelideal;

import static androidx.car.app.model.Action.BACK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Creates a screen that demonstrates usage of the full screen {@link ListTemplate} to display a
 * full-screen list.
 */
public final class ConfirmScreen extends Screen implements DefaultLifecycleObserver {

//    String Gasolina;
//    String Alcool;
//    String GNV;

    ArrayList<String> result;
    int seconds = 900;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable = new Runnable() {

         @Override
        public void run() {
//            int seconds = 60;
            if (seconds == 0) {
                onStop();
                //           } else {
                //               seconds--;
                //               CarToast.makeText(
                //                       getCarContext(),
                //                       getCarContext().getResources().getString(R.string.opening_mas_in)
                //                               + seconds
                //                               + getCarContext().getResources().getString(R.string.seconds),
                //                       CarToast.LENGTH_LONG).show();
                //               handler.postDelayed(runnable, 1000);
            }
        }
    };

    public ConfirmScreen(@NonNull CarContext carContext, ArrayList<String> result) {

        super(carContext);
        getLifecycle().addObserver(this);
        this.result = result;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCarContext());
        this.seconds = Integer.valueOf(preferences.getString("confirmation_delay", "5"));
        handler.postDelayed(runnable, 1000);
    }

    public void openMap() {
        handler.removeCallbacks(runnable);

        StringBuilder builder = new StringBuilder();
        builder.append("geo:0,0?q= ");
        builder.append(result.stream().map((item) -> {
            try {
                return URLEncoder.encode(item, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }).collect(Collectors.joining("+")));

        Uri gmmIntentUri = Uri.parse(builder.toString());
        Intent mapIntent = new Intent(CarContext.ACTION_NAVIGATE, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getCarContext().startCarApp(mapIntent);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                getScreenManager().pop();
            }
        }, 5000);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {

//        for (int i = 0; i < 3; i++) {

//            if (i == 0);
//            {
//                Gasolina = String.valueOf(result);
//            }
//            if (i == 1);
//            {
//                Alcool = String.valueOf(result);
//            }
//            if (i == 2);
//            {
//                GNV = String.valueOf(result);
//            }

//        }

        ItemList.Builder listBuilder = new ItemList.Builder();
        listBuilder
                 .addItem(new Row.Builder()
                        .setTitle("Confira se o valor abaixo está correto, do combustível escolhido na tela anterior:")
                        .addText("R$ " + result.toString())
//                        .addText(Alcool.toString())
//                        .addText(GNV.toString())
                        .setOnClickListener(() -> {
                            onStop();
                        })
                        .build())
        //               .addItem(new Row.Builder()
        //                       .setOnClickListener(() -> {
        //                           openMap();
        //                       })
        //                       .setTitle(getCarContext().getResources().getString(R.string.open_map_now))
        //                       .build())
        //               .addItem(new Row.Builder()
        //                       .setOnClickListener(() -> {
        //                           handler.removeCallbacks(runnable);
        //                           getScreenManager().pop();
        //                       })
        //                       .setTitle(getCarContext().getResources().getString(R.string.new_speech_recognition))
        //                       .build())
        ;

        return new ListTemplate.Builder()
                .setSingleList(listBuilder.build())
                .setTitle(getCarContext().getResources().getString(R.string.route_to))
                .setHeaderAction(BACK)
                .build();
    }


//    double Vgasolina = Double.valueOf(Gasolina).doubleValue();
//    double Valcool = Double.valueOf(Alcool).doubleValue();
//    double Vgnv = Double.valueOf(GNV).doubleValue();

//    Double resultado = Valcool/Vgasolina;
//    Double resultado1 = (Valcool/10);
//    Double resultado2 = (Vgnv/17);
//    Double resultado3 = (Vgasolina/10);
//    Double resultado4 = (Vgnv/12);

//        if ((resultado2 < resultado1) && (resultado4 < resultado3) && (precoGas != 0.0D))
//    {
//        textResultado.setText("UTILIZE GNV");

//    }else{
//        if (resultado >=0.7){
//            textResultado.setText("UTILIZE GASOLINA");
//        }else{
//            textResultado.setText("UTILIZE ÁLCOOL");
//        }
//    }


    private void onStop() {
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        handler.removeCallbacks(runnable);
    }
}

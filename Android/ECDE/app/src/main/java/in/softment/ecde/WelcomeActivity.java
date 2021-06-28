package in.softment.ecde;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import in.softment.ecde.Models.MyLanguage;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_welcome);


        //LoadLocale
        loadLocale();
    }

    public void loadLocale(){
        SharedPreferences sharedPreferences = getSharedPreferences("lang",MODE_PRIVATE);
        String code = sharedPreferences.getString("mylang","pt");
        MyLanguage.lang = code;
        setLocale(code);
    }

    private void setLocale(String code){
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getResources().updateConfiguration(configuration,getResources().getDisplayMetrics());

        //SharedPref
        SharedPreferences.Editor sharedPreferences = getSharedPreferences("lang", Context.MODE_PRIVATE).edit();
        sharedPreferences.putString("mylang",code);
        sharedPreferences.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                //ProgressHud.show(this,"Loading...");

                Services.getCurrentUserData(this, FirebaseAuth.getInstance().getCurrentUser().getUid());

            }
            else {
                gotoSignInPage();
            }

        }
        else {
            gotoSignInPage();
        }
    }

    public void gotoSignInPage(){
        Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
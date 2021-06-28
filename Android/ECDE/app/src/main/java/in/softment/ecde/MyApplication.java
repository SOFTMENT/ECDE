package in.softment.ecde;

import android.app.Application;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
    }
}

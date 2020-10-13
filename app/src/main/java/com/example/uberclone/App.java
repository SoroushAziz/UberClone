package com.example.uberclone;

import com.parse.Parse;
import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("lHayCtR5gZVcs2LOAC4uLKMdzLkVfCR4CJOYtV2B")
                // if defined
                .clientKey("WFl3729fykrLN4hobY45KNsm0EDGydRliyKUzW4U")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
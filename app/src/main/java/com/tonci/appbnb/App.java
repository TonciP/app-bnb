package com.tonci.appbnb;

import android.app.Application;

import androidx.annotation.NonNull;

import com.tonci.appbnb.di.AppContainer;

public class App extends Application {

    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);
    }

    @NonNull
    public AppContainer getContainer() {
        return container;
    }
}

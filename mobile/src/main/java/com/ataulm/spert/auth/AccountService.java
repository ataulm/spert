package com.ataulm.spert.auth;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AccountService extends Service {

    private SpertAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        this.authenticator = new SpertAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (!AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            return null;
        }
        return authenticator.getIBinder();
    }

}

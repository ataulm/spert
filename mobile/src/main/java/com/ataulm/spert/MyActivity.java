package com.ataulm.spert;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ataulm.spert.auth.SpertAccountManager;

public class MyActivity extends AppCompatActivity {

    private SpertAccountManager spertAccountManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        spertAccountManager = SpertAccountManager.newInstance(this);
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        if (spertAccountManager.getAccount() == null) {
            spertAccountManager.startAddAccountProcess(this);
        } else {
            spertAccountManager.signOut();
            refreshUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    private void refreshUi() {
        Account account = spertAccountManager.getAccount();
        if (account == null) {
            ((TextView) findViewById(R.id.status)).setText("Not logged in");
            ((Button) findViewById(R.id.login_button)).setText("Sign in");
        } else {

            ((TextView) findViewById(R.id.status)).setText("Logged in as: " + account.name);
            ((Button) findViewById(R.id.login_button)).setText("Sign out");
        }
    }

}

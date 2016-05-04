package com.ataulm.spert.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ataulm.spert.BuildConfig;
import com.ataulm.spert.R;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OAuthSignInActivity extends AppCompatActivity {

    private SpertAccountManager accountManager;
    private AccountAuthenticatorResponse authenticatorResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_web);

        accountManager = SpertAccountManager.newInstance(this);
        authenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        authenticatorResponse.onRequestContinued();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebView webView = (WebView) findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String redirectUrl) {
                        if (redirectUrl.startsWith(BuildConfig.SPOTIFY_REDIRECT_URI)) {
                            String code = extractCodeFromUrl(redirectUrl);
                            onRedirectWith(code);
                            return true;
                        } else {
                            return super.shouldOverrideUrlLoading(view, redirectUrl);
                        }
                    }
                }
        );

        String authorizationEndpoint = getIntent().getData().toString();
        webView.loadUrl(authorizationEndpoint);
    }

    private static String extractCodeFromUrl(String url) {
        String query = URI.create(url).getQuery();
        String[] pairs = query.split("&");
        Map<String, String> queryParams = new HashMap<>(pairs.length);
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            try {
                queryParams.put(URLDecoder.decode(pair.substring(0, index), "UTF-8"), URLDecoder.decode(pair.substring(index + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Couldn't extract code param from URL: " + url, e);
            }
        }
        return queryParams.get("code");
    }

    private void onRedirectWith(String code) {
        new SpotifyOAuthTokenRequester()
                .getAccessTokenInExchangeFor(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AuthObserver());
        // TODO: no unsubscriptions??? cray
    }

    private class AuthObserver implements Observer<SpotifyOAuthTokenResponse> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Log.e("!!!", "quewralkf", e);
        }

        @Override
        public void onNext(SpotifyOAuthTokenResponse spotifyOAuthTokenResponse) {
            // TODO: should fetch the username from the Trakt api before making the account
            Account account = new Account("john smith", getString(R.string.account_type));
            AccessToken accessToken = AccessToken.from(spotifyOAuthTokenResponse);
            RefreshToken refreshToken = new RefreshToken(spotifyOAuthTokenResponse.refreshToken);

            accountManager.addAccount(account, accessToken, refreshToken);

            Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken.toString());

            setResult(RESULT_OK, intent);
            authenticatorResponse.onResult(intent.getExtras());
            finish();
        }

    }

}

package com.ataulm.spert.auth;


import com.ataulm.spert.BuildConfig;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Func1;

class SpotifyOAuthTokenRequester {

    public Observable<SpotifyOAuthTokenResponse> getAccessTokenInExchangeFor(String code) {
        return Observable.just("code=" + code)
                .map(getAccessToken("grant_type=authorization_code"));
    }

    private static Func1<String, SpotifyOAuthTokenResponse> getAccessToken(final String grantType) {
        return new Func1<String, SpotifyOAuthTokenResponse>() {

            @Override
            public SpotifyOAuthTokenResponse call(String token) {
                try {
                    MediaType textMediaType = MediaType.parse("application/x-www-form-urlencoded");
                    Request request = new Request.Builder()
                            .url("https://accounts.spotify.com/api/token")
                            .post(RequestBody.create(textMediaType, buildTokenRequestBody(grantType, token)))
                            .build();

                    Response response = new OkHttpClient().newCall(request).execute();
                    String result = response.body().string();

                    return SpotifyOAuthTokenResponse.from(result);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    private static String buildTokenRequestBody(String grantType, String token) {
        return grantType +
                "&" + token +
                "&client_id=" + BuildConfig.SPOTIFY_API_KEY +
                "&client_secret=" + BuildConfig.SPOTIFY_API_SECRET +
                "&redirect_uri=" + BuildConfig.SPOTIFY_REDIRECT_URI;
    }

    public Observable<SpotifyOAuthTokenResponse> getAccessTokenInExchangeFor(RefreshToken refreshToken) {
        return Observable.just("refresh_token=" + refreshToken.toString())
                .map(getAccessToken("grant_type=refresh_token"));
    }

}

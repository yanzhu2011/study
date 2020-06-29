package com.yz.springbootdemo.util;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

import java.io.IOException;

/**
 * @author yanzhu
 * @date 2020/6/29
 * @description
 */
public class AuthorizationCodeInstalledAppUtil extends AuthorizationCodeInstalledApp {

    public AuthorizationCodeInstalledAppUtil(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
        super(flow, receiver);
    }

    public Credential authorize(String userId, String accessToken) throws IOException {
        Credential credential = this.getFlow().loadCredential(userId);
        if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60L)) {
            return credential;
        }
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setExpiresInSeconds(3599L);
        response.setScope("https://www.googleapis.com/auth/drive");
        response.setTokenType("Bearer");
        return this.getFlow().createAndStoreCredential(response, userId);
    }
}

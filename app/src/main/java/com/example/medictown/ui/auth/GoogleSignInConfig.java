package com.example.medictown.ui.auth;

import android.content.Context;

import com.example.medictown.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

final class GoogleSignInConfig {
    private GoogleSignInConfig() {
    }

    static GoogleSignInClient createClient(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(resolveWebClientId(context))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context, gso);
    }

    private static String resolveWebClientId(Context context) {
        int generatedId = context.getResources().getIdentifier(
                "default_web_client_id",
                "string",
                context.getPackageName()
        );
        if (generatedId != 0) {
            String generatedClientId = context.getString(generatedId).trim();
            if (!generatedClientId.isEmpty()) {
                return generatedClientId;
            }
        }
        return context.getString(R.string.google_web_client_id);
    }
}

package com.linkdump.tchur.ld.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Preconditions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.linkdump.tchur.ld.R;
import com.linkdump.tchur.ld.repository.FirebaseDbContext;
import com.linkdump.tchur.ld.repository.sugar_orm.AppConfig;
import com.linkdump.tchur.ld.ui.ui_containers.LoginViewCoordinator;
import com.linkdump.tchur.ld.ui.ui_containers.ViewCoordinator;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    //ui
    LoginViewCoordinator loginViewCoordinator;
    FirebaseDbContext firebaseDbContext;
    final String TAG = "Log";




    /*




     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_login);
        firebaseDbContext = new FirebaseDbContext();
        loginViewCoordinator = new LoginViewCoordinator(LoginActivity.this, this);
        loginViewCoordinator.initialiseViewFromXml(R.layout.activity_login);
        setContentView(loginViewCoordinator.getRootView());
        checkBuildVersion();



        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);
        loginViewCoordinator.editTextUsername.setText(prefs.getString("email", ""));



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        firebaseDbContext.setGoogleSignInClient(GoogleSignIn.getClient(this, gso));

        loginViewCoordinator.buttonSignUp.setOnClickListener(view -> moveToSignUp());
        loginViewCoordinator.buttonLogin.setOnClickListener(v -> login());
        loginViewCoordinator.googleSignInButton.setOnClickListener(v -> signIn());

       //AppConfig config = AppConfig.findById(AppConfig.class, 1L);
    }

    private void moveToSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void login() {
        String email = loginViewCoordinator.editTextUsername.getText().toString();
        String password = loginViewCoordinator.editTextPassword.getText().toString();

        if (email.equals("")) {
            Toast.makeText(LoginActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
        } else if (password.equals("")) {
            Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
        } else {
            loginViewCoordinator.progressBar.setVisibility(View.VISIBLE);
            firebaseDbContext.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = firebaseDbContext.getFirebaseAuth().getCurrentUser();
                            loginViewCoordinator.progressBar.setVisibility(View.INVISIBLE);


                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            loginViewCoordinator.progressBar.setVisibility(View.INVISIBLE);


                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    });

        }
    }

    private void checkBuildVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = "chat";
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }
    }

    private void signIn() {
        Intent signInIntent = firebaseDbContext.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }





    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]



        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);



        firebaseDbContext.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = firebaseDbContext.getFirebaseAuth().getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(acct.getDisplayName())
                                .build();
                        Map<String, Object> mUser = new HashMap<>();
                        if (acct.getGivenName() != null) {
                            mUser.put("firstName", acct.getGivenName());
                        }
                        if (acct.getFamilyName() != null) {
                            mUser.put("lastName", acct.getFamilyName());
                        }
                        if (acct.getEmail() != null) {
                            mUser.put("email", acct.getEmail());
                        }
                        if (acct.getPhotoUrl() != null) {
                            mUser.put("photoUrl", acct.getPhotoUrl());
                        }
                        firebaseDbContext.getFirebaseFirestore().collection("users").document(user.getUid())
                                .set(mUser)
                                .addOnSuccessListener(aVoid -> Log.d("demo", "DocumentSnapshot successfully written!"))
                                .addOnFailureListener(e -> Log.w("demo", "Error writing document", e));


                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d(TAG, "User profile updated");
                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });

//                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                            startActivity(i);
//                            Toast.makeText(getApplicationContext()
//                                    , "Welcome " + user.getDisplayName() + "!"
//                                    , Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(),
                                "Failed Sign In",
                                Toast.LENGTH_SHORT).show();

                    }

                    // [START_EXCLUDE]
                    // [END_EXCLUDE]
                });
    }






    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseDbContext.getFirebaseAuth().getCurrentUser() != null) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}

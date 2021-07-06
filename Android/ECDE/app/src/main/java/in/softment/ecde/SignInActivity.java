package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class SignInActivity extends AppCompatActivity {
    EditText emailAddress,password;
    private FirebaseAuth mAuth;
    private static final int STORAGE_PERMISSION_CODE = 4655;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //REQUEST_PERMISSION
        requestStoragePermission();;


        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.google_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 909);

            }
        });


        emailAddress = findViewById(R.id.emailAddress);
        password = findViewById(R.id.password);
        //CREATE ACCOUNT
        findViewById(R.id.createNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SignInActivity.this, CreateNewAccount.class));

            }
        });

        //SignIn
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sEmail = emailAddress.getText().toString().trim();
                String sPassword = password.getText().toString().trim();
                if (sEmail.isEmpty()) {
                    Services.showCenterToast(SignInActivity.this,"Enter Email Address");
                }
                else {
                    if (sPassword.isEmpty()) {
                        Services.showCenterToast(SignInActivity.this,"Enter Password");
                    }
                    else {
                        ProgressHud.show(SignInActivity.this,"Sign In...");
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(sEmail,sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                        if (FirebaseAuth.getInstance().getCurrentUser() != null){
                                            if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                                    Services.sentEmailVerificationLink(SignInActivity.this);
                                            }
                                            else {
                                                Services.getCurrentUserData(SignInActivity.this, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            }

                                        }
                                        else {
                                            ProgressHud.dialog.dismiss();
                                        }
                                    }
                                    else {
                                        ProgressHud.dialog.dismiss();
                                        Services.showDialog(SignInActivity.this,"ERROR",task.getException().getLocalizedMessage());
                                    }
                            }
                        });
                    }
                }
            }
        });

        //Reset
        findViewById(R.id.resetPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sEmail = emailAddress.getText().toString().trim();
                if (sEmail.isEmpty()) {
                    Services.showCenterToast(SignInActivity.this,"Enter Email Address");
                    return;
                }
                ProgressHud.show(SignInActivity.this,"Resetting...");
                FirebaseAuth.getInstance().sendPasswordResetEmail(sEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ProgressHud.dialog.dismiss();
                        if (task.isSuccessful()) {
                            Services.showDialog(SignInActivity.this,"RESET PASSWORD","We have sent password reset link on your mail address.");
                        }
                        else {
                            Services.showDialog(SignInActivity.this, "ERROR",task.getException().getLocalizedMessage());
                        }
                    }
                });
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (mAuth.getCurrentUser() != null){
                                ProgressHud.show(SignInActivity.this,"");
                                addUserDataOnServer(mAuth.getUid(),mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getEmail(),mAuth.getCurrentUser().getPhotoUrl().toString());
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                           Services.showDialog(SignInActivity.this,"ERROR", Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        }
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 909) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
              Services.showDialog(SignInActivity.this,"ERROR",e.getLocalizedMessage());
            }
        }
    }


    public void requestStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//If the user has denied the permission previously your code will come to this block
//Here you can explain why you need this permission
//Explain here why you need this permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }

        }

    }

    public void addUserDataOnServer(String uid,String fullName, String emailAddress, String imageUrl){
        Map<String,Object> user = new HashMap<>();
        user.put("uid",uid);
        user.put("fullName",fullName);
        user.put("emailAddress",emailAddress);
        user.put("profileImage",imageUrl);
        user.put("registrationDate", FieldValue.serverTimestamp());


        FirebaseFirestore.getInstance().collection("User").document(uid).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                   Services.getCurrentUserData(SignInActivity.this,FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
            }
        });
    }

}
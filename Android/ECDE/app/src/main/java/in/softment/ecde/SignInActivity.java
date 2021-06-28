package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class SignInActivity extends AppCompatActivity {
    EditText emailAddress,password;
    private static final int STORAGE_PERMISSION_CODE = 4655;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //REQUEST_PERMISSION
        requestStoragePermission();;


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

}
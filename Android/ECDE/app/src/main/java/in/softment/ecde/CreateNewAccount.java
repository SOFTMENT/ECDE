package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.FileUtil;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;
import io.grpc.Compressor;

public class CreateNewAccount extends AppCompatActivity {

    private EditText fullName, emailAddress, password;
    private final int PICK_IMAGE_REQUEST = 1;
    private  Uri resultUri = null;
    private CircleImageView profile_image;
    private boolean isProfilePicSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);

        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        password = findViewById(R.id.password);
        profile_image = findViewById(R.id.user_profile);


        //TapToChangeImage
        findViewById(R.id.taptochange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowFileChooser();
            }
        });

        //CreateAccount
        findViewById(R.id.createAccountBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sFullName = fullName.getText().toString().trim();
                String sEmail = emailAddress.getText().toString().trim();
                String sPassword = password.getText().toString().trim();

                if (!isProfilePicSelected) {
                    Services.showCenterToast(CreateNewAccount.this, getString(R.string.choose_profile_pic));
                }
                else {
                    if (sFullName.isEmpty()) {
                        Services.showCenterToast(CreateNewAccount.this, getString(R.string.enter_full_name));
                    } else {
                        if (sEmail.isEmpty()) {
                            Services.showCenterToast(CreateNewAccount.this, getString(R.string.enter_email_address));
                        } else {
                            if (sPassword.isEmpty()) {
                                Services.showCenterToast(CreateNewAccount.this, getString(R.string.enter_password));
                            }
                            else {



                                     createAccount(sFullName, sEmail, sPassword);


                            }
                        }
                    }
                }
            }
        });

      //Back
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //SignIn
        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void createAccount(String sFullName,String sEmail, String sPassword){
        ProgressHud.show(CreateNewAccount.this, getString(R.string.creating_account));

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    uploadImageOnFirebase(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),Services.toUpperCase(sFullName),sEmail);
                } else {
                    ProgressHud.dialog.dismiss();
                    Services.handleFirebaseERROR(CreateNewAccount.this, ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode());
                }
            }
        });
    }

    private void uploadImageOnFirebase(String uid, String fullName, String emilId) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ProfilePicture").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+ ".png");
        UploadTask uploadTask = storageReference.putFile(resultUri);
        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    ProgressHud.dialog.dismiss();
                    throw Objects.requireNonNull(task.getException());
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    String downloadUri = String.valueOf(task.getResult());
                    addUserDataOnServer(uid, fullName, emilId, downloadUri);

                }
                else{
                    addUserDataOnServer(uid, fullName, emilId, "https://firebasestorage.googleapis.com/v0/b/ecde-24c9c.appspot.com/o/ProfilePicture%2Fuser.png?alt=media&token=e95347b6-c527-4f3e-bc3c-169ea498dd93");
                }


            }
        });
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
                    Services.sentEmailVerificationLink(CreateNewAccount.this);
                }
            }
        });
    }
    public void ShowFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {

            Uri filepath = data.getData();
            CropImage.activity(filepath).setOutputCompressQuality(60).setAspectRatio(1,1).setFixAspectRatio(true)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUriContent();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    profile_image.setImageBitmap(bitmap);
                    isProfilePicSelected = true;
                } catch (IOException e) {

                }

            }
        }
    }

}




//        TextView versionName = findViewById(R.id.versionName);
//        try {
//            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            String version = pInfo.versionName;
//            versionName.setText(version);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
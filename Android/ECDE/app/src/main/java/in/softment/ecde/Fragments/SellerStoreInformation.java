package in.softment.ecde.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.ecde.MainActivity;
import in.softment.ecde.Models.CitiesModel;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.R;
import in.softment.ecde.SellerStoreInformationActivity;
import in.softment.ecde.Utils.Cities;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

import static android.app.Activity.RESULT_OK;


public class SellerStoreInformation extends Fragment {


    private EditText storeName, storeAddress,  phoneNumber, aboutYourStore;
    private AutoCompleteTextView city;
    private final int PICK_IMAGE_REQUEST = 1;
    private Uri resultUri = null;
    private CircleImageView profile_image;
    private boolean isProfilePicSelected = false;
    String sStoreName;
    String sAddress;
    String sCity;
    String sPhoneNumber;
    String sAboutStore;
    private final int STORE_LOCATION_MAP = 120;

    private Context context;

    public SellerStoreInformation(Context context) {
        this.context = context;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_seller_store_information, container, false);
        profile_image = view.findViewById(R.id.user_profile);
        storeName = view.findViewById(R.id.storeName);
        storeAddress = view.findViewById(R.id.address);
        city = view.findViewById(R.id.city);
        city.setThreshold(1);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        aboutYourStore = view.findViewById(R.id.storeDescription);



        String[] cities = new Cities().names;
        Arrays.sort(cities);

        ArrayAdapter cityAdapter = new ArrayAdapter(context,R.layout.option_item,cities);
        city.setAdapter(cityAdapter);
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, ""+position, Toast.LENGTH_SHORT).show();
                city.setText(cities[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //TapToChangeImage
        view.findViewById(R.id.taptochange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 ShowFileChooser();
            }
        });

        //CreateAccount
        view.findViewById(R.id.createSeller).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sStoreName = storeName.getText().toString().trim();
                sAddress = storeAddress.getText().toString().trim();
                sCity= city.getText().toString().trim();
                sPhoneNumber = phoneNumber.getText().toString().trim();
                sAboutStore = aboutYourStore.getText().toString().trim();

                if (!isProfilePicSelected) {
                    Services.showCenterToast(context, "Add Store Image");
                }
                else {
                    if (sStoreName.isEmpty()) {
                        Services.showCenterToast(context, "Enter Store Name");
                    } else {
                        if (sAddress.isEmpty()) {
                            Services.showCenterToast(context, "Enter Store Address");
                        } else {
                            if (sCity.isEmpty()) {
                                Services.showCenterToast(context, "Enter Postal Code");
                            }
                            else {
                                if (sPhoneNumber.isEmpty()) {
                                    Services.showCenterToast(context, "Enter Phone Number");
                                }
                                else {
                                    if (sAboutStore.isEmpty()) {
                                        Services.showCenterToast(context, "Enter About Store");
                                    }
                                    else {
                                        uploadImageOnFirebase();
                                    }
                                }

                            }
                        }
                    }
                }
            }
        });


        
        return view;
    }

    private void uploadImageOnFirebase() {
        ProgressHud.show(context,"");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("StoreProfile").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+ ".png");
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
                    updateStoreInformationOnServer(downloadUri);

                }
                else{
                    updateStoreInformationOnServer("https://firebasestorage.googleapis.com/v0/b/ecde-24c9c.appspot.com/o/ProfilePicture%2Fuser.png?alt=media&token=e95347b6-c527-4f3e-bc3c-169ea498dd93");
                }


            }
        });
    }

    public void updateStoreInformationOnServer(String storeImage){

        Map<String,Object> user = new HashMap<>();
        user.put("storeName",sStoreName);
        user.put("storeImage",storeImage);
        user.put("storeAddress",sAddress);
        user.put("storeCity", sCity);
        user.put("phoneNumber",sPhoneNumber);
        user.put("storeAbout",sAboutStore);
        user.put("isSeller",true);



       FirebaseFirestore.getInstance().collection("User").document(UserModel.data.uid).set(user, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    UserModel.data.storeName = sStoreName;
                    UserModel.data.storeImage = storeImage;
                    UserModel.data.storeAddress = sAddress;
                    UserModel.data.storeCity = sCity;
                    UserModel.data.phoneNumber = sPhoneNumber;
                    UserModel.data.storeAbout = sAboutStore;
                    UserModel.data.isSeller = true;


                    ((MainActivity)context).notifyPagerAdapter();

                }
                else {
                    Services.showDialog(context,"ERROR", Objects.requireNonNull(task.getException()).getLocalizedMessage());
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


    public void cropUrl(Uri uri){
        resultUri = uri;
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
            profile_image.setImageBitmap(bitmap);
            isProfilePicSelected = true;
        } catch (IOException ignored) {

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null) {
            Uri filepath = data.getData();
            CropImage.activity(filepath).setOutputCompressQuality(60).start((MainActivity)context);
        }

    }
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity)context).initializeSellerFragment(this);
    }
}
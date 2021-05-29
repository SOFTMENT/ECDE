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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.softment.ecde.MainActivity;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.MyLanguage;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.R;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

import static android.app.Activity.RESULT_OK;

public class PostFragment extends Fragment {

    private Map<String, Uri> images = new HashMap<>();
    private int clickedImageViewPosition = 0;
    private RoundedImageView oneImage, twoImage, threeImage, fourImage;
    private LinearLayout oneLL, twoLL, threeLL, fourLL;
    private boolean oneImageSelected,twoImageSelected;
    private Context context;
    private AutoCompleteTextView chooseCategory, p_title, p_description, p_price;
    private int selectedCategoryIndex = -1;
    public PostFragment(Context context) {
        this.context = context;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post, container, false);



        chooseCategory = view.findViewById(R.id.chooseCategory);
        p_title = view.findViewById(R.id.p_title);
        p_description = view.findViewById(R.id.p_description);
        p_price = view.findViewById(R.id.p_price);
        view.findViewById(R.id.addProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = p_title.getText().toString().trim();
                String description = p_description.getText().toString().trim();
                String price = p_price.getText().toString();
                String category = chooseCategory.getText().toString();

                if (title.isEmpty()) {
                    Services.showCenterToast(context,"Enter Product Title");
                }
                else{
                    if (description.isEmpty()) {
                        Services.showCenterToast(context,"Enter Product Description");
                    }
                    else {
                        if (price.isEmpty()) {
                            Services.showCenterToast(context,"Enter Product Price");
                        }
                        else {
                            if (category.isEmpty()) {
                                Services.showCenterToast(context,"Choose Product Category");
                            }
                            else {
                                if (oneImageSelected && twoImageSelected){
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null){
                                        addProduct(CategoryModel.categoryModels.get(selectedCategoryIndex).getId(),FirebaseAuth.getInstance().getCurrentUser().getUid(), UserModel.data.fullName,UserModel.data.getProfileImage(),UserModel.data.token,title,description,price);
                                    }
                                    else {
                                        Services.logout(context);
                                    }

                                }
                                else {
                                    Services.showCenterToast(context,"Please Choose Product Image");
                                }
                            }

                        }
                    }
                }

            }
        });



        //IMAGES
        oneImage = view.findViewById(R.id.oneImage);
        twoImage = view.findViewById(R.id.twoImage);
        threeImage = view.findViewById(R.id.threeImage);
        fourImage = view.findViewById(R.id.fourImage);

        //Linear Layout
        oneLL = view.findViewById(R.id.oneLL);
        twoLL = view.findViewById(R.id.twoLL);
        threeLL = view.findViewById(R.id.threeLL);
        fourLL = view.findViewById(R.id.fourLL);


        oneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition  = 1;
                showFileChooser();
            }
        });

        twoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition  = 2;
                showFileChooser();
            }
        });

        threeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition  = 3;
                showFileChooser();
            }
        });

        fourImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition  = 4;
                showFileChooser();
            }
        });


        oneLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition  = 1;
                showFileChooser();
            }
        });


        twoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition = 2;
                showFileChooser();
            }
        });

        threeLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition = 3;
                showFileChooser();
            }
        });

        fourLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedImageViewPosition = 4;
                showFileChooser();
            }
        });
        return view;
    }

    private void uploadImageOnFirebase(String uid,String pid) {
        Map<String,String> sImages = new HashMap<>();

        for (String key: images.keySet()) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Products").child(uid).child(pid).child(key+ ".png");
            UploadTask uploadTask = storageReference.putFile(images.get(key));
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        ProgressHud.dialog.dismiss();
                        throw  task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {


                    if (task.isSuccessful()) {
                        String downloadUri = String.valueOf(task.getResult());
                        Log.d("VIJAY",key+"");
                        sImages.put(key,downloadUri);
                        Map<String, Object> map = new HashMap<>();
                        map.put("images",sImages);
                        FirebaseFirestore.getInstance().collection("Products").document(pid).set(map,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("imagesUplaoded","YES");
                                    }
                                    else {
                                        Log.d("imagesUploaded",task.getException().getLocalizedMessage());
                                    }
                            }
                        });
                    }



                }
            });
        }

    }

    public void addProduct(String cat_id, String uid, String sellerName,String sellerImage,String sellerToken,String title, String description, String price) {
        String id = FirebaseFirestore.getInstance().collection("Products").document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("cat_id",cat_id);
        map.put("uid",uid);
        map.put("title",title);
        map.put("description",description);
        map.put("price",price);
        map.put("sellerName",sellerName);
        map.put("sellerImage",sellerImage);
        map.put("sellerToken", sellerToken);
        map.put("date", FieldValue.serverTimestamp());

        ProgressHud.show(context,"Product Adding...");
        FirebaseFirestore.getInstance().collection("Products").document(id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    Services.showCenterToast(context,"Product Successfully Added");
                    uploadImageOnFirebase(uid,id);
                    images.clear();
                    oneImageSelected = false;
                    twoImageSelected = false;

                    oneLL.setVisibility(View.VISIBLE);
                    twoLL.setVisibility(View.VISIBLE);
                    threeLL.setVisibility(View.VISIBLE);
                    fourLL.setVisibility(View.VISIBLE);

                    oneImage.setVisibility(View.GONE);
                    twoImage.setVisibility(View.GONE);
                    threeImage.setVisibility(View.GONE);
                    fourImage.setVisibility(View.GONE);

                    p_title.setText("");
                    p_description.setText("");
                    p_price.setText("");
                    chooseCategory.setText("");

                    clickedImageViewPosition = -1;


                }
                else {
                    Services.showDialog(context,"ERROR",task.getException().getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null) {
            Log.d("ERROR","OH4");
            Uri filepath = data.getData();
            CropImage.activity(filepath).setOutputCompressQuality(60).start((MainActivity)context);
        }

    }
    public void showFileChooser() {

        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        }
        catch (Exception e) {

        }

    }

    public void cropUri(Uri resultUri ){
        if (clickedImageViewPosition == 1) {


            Bitmap bitmap = null;
            try {

                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
                oneImage.setImageBitmap(bitmap);
                oneImageSelected = true;
                oneImage.setVisibility(View.VISIBLE);
                oneLL.setVisibility(View.GONE);
                images.put("0",resultUri);
            } catch (IOException e) {
                Log.d("ERROR",e.getLocalizedMessage());
            }
        }else if (clickedImageViewPosition == 2) {


            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
                twoImage.setImageBitmap(bitmap);
                twoImageSelected = true;
                twoImage.setVisibility(View.VISIBLE);
                twoLL.setVisibility(View.GONE);
                images.put("1",resultUri);
            } catch (IOException e) {

            }
        }
        else if (clickedImageViewPosition == 3) {


            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
                threeImage.setImageBitmap(bitmap);
                threeImage.setVisibility(View.VISIBLE);
                threeLL.setVisibility(View.GONE);
                images.put("2",resultUri);
            } catch (IOException e) {

            }
        }
        else if (clickedImageViewPosition == 4) {

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
                fourImage.setImageBitmap(bitmap);
                fourImage.setVisibility(View.VISIBLE);
                fourLL.setVisibility(View.GONE);
                images.put("3",resultUri);
            } catch (IOException e) {

            }
        }
    }

    public void notifyAdapter(){
        ArrayList<String> categoryNames = new ArrayList<>();
        for (CategoryModel cat : CategoryModel.categoryModels) {
            if (MyLanguage.lang.equalsIgnoreCase("pt"))
                categoryNames.add(cat.getTitle_pt());
            else
                categoryNames.add(cat.getTitle_en());

        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(context,R.layout.option_item,categoryNames);
        chooseCategory.setAdapter(arrayAdapter);


        chooseCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               selectedCategoryIndex = i;
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity)context).initializePostFragment(this);
    }
}
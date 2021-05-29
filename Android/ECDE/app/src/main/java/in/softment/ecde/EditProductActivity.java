package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class EditProductActivity extends AppCompatActivity {

    private final Map<String, Uri> images = new HashMap<>();
    private Map<String,String> previous_image = new HashMap<>();
    private int clickedImageViewPosition = 0;
    private RoundedImageView oneImage, twoImage, threeImage, fourImage;
    private LinearLayout oneLL, twoLL, threeLL, fourLL;
    private boolean oneImageSelected,twoImageSelected;
    private AutoCompleteTextView  p_title, p_description, p_price;
    private ProductModel productModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);


        int index = getIntent().getIntExtra("index",-1);
        if (index == -1) {
            finish();
        }
        productModel = ProductModel.myproductsModels.get(index);
        if (productModel == null) {
            finish();
        }

        previous_image = productModel.getImages();

        p_title = findViewById(R.id.p_title);
        p_description = findViewById(R.id.p_description);
        p_price = findViewById(R.id.p_price);
        findViewById(R.id.addProduct).setOnClickListener(view -> {
            String title = p_title.getText().toString().trim();
            String description = p_description.getText().toString().trim();
            String price = p_price.getText().toString();


            if (title.isEmpty()) {
                Services.showCenterToast(EditProductActivity.this,"Enter Product Title");
            }
            else{
                if (description.isEmpty()) {
                    Services.showCenterToast(EditProductActivity.this,"Enter Product Description");
                }
                else {
                    if (price.isEmpty()) {
                        Services.showCenterToast(EditProductActivity.this,"Enter Product Price");
                    }
                    else {

                            if (oneImageSelected && twoImageSelected){
                                if (FirebaseAuth.getInstance().getCurrentUser() != null){
                                    updateProduct(productModel.cat_id,FirebaseAuth.getInstance().getCurrentUser().getUid(),title,description,price);
                                }
                                else {
                                    Services.logout(EditProductActivity.this);
                                }



                        } else {
                                Services.showCenterToast(EditProductActivity.this,"Please choose images");
                            }

                    }
                }
            }

        });


        //GoBack
        findViewById(R.id.back).setOnClickListener(view -> finish());


        //IMAGES
        oneImage = findViewById(R.id.oneImage);
        twoImage = findViewById(R.id.twoImage);
        threeImage = findViewById(R.id.threeImage);
        fourImage = findViewById(R.id.fourImage);

        //Linear Layout
        oneLL = findViewById(R.id.oneLL);
        twoLL = findViewById(R.id.twoLL);
        threeLL = findViewById(R.id.threeLL);
        fourLL = findViewById(R.id.fourLL);

        //INITIALIZE ITEMS
        p_title.setText(productModel.getTitle());
        p_description.setText(productModel.getDescription());
        p_price.setText(productModel.getPrice());



        if (productModel.getImages().containsKey("0")) {
            oneImageSelected = true;
            oneImage.setVisibility(View.VISIBLE);
            oneLL.setVisibility(View.GONE);
            Glide.with(this).load(productModel.getImages().get("0")).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(oneImage);
        }

        if (productModel.getImages().containsKey("1")) {
            twoImageSelected = true;
            twoImage.setVisibility(View.VISIBLE);
            twoLL.setVisibility(View.GONE);
            Glide.with(this).load(productModel.getImages().get("1")).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(twoImage);
        }

        if (productModel.getImages().containsKey("2")) {
            threeImage.setVisibility(View.VISIBLE);
            threeLL.setVisibility(View.GONE);
            Glide.with(this).load(productModel.getImages().get("2")).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(threeImage);
        }

        if (productModel.getImages().containsKey("3")) {
            fourImage.setVisibility(View.VISIBLE);
            fourLL.setVisibility(View.GONE);
            Glide.with(this).load(productModel.getImages().get("3")).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(fourImage);
        }

        oneImage.setOnClickListener(view -> {
            clickedImageViewPosition  = 1;
            showFileChooser();
        });

        twoImage.setOnClickListener(view -> {
            clickedImageViewPosition  = 2;
            showFileChooser();
        });

        threeImage.setOnClickListener(view -> {
            clickedImageViewPosition  = 3;
            showFileChooser();
        });

        fourImage.setOnClickListener(view -> {
            clickedImageViewPosition  = 4;
            showFileChooser();
        });


        oneLL.setOnClickListener(view -> {
            clickedImageViewPosition  = 1;
            showFileChooser();
        });


        twoLL.setOnClickListener(view -> {
            clickedImageViewPosition = 2;
            showFileChooser();
        });

        threeLL.setOnClickListener(view -> {
            clickedImageViewPosition = 3;
            showFileChooser();
        });

        fourLL.setOnClickListener(view -> {
            clickedImageViewPosition = 4;
            showFileChooser();
        });

    }

    private void uploadImageOnFirebase(String uid,String pid) {

        for (String key : images.keySet()) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Products").child(uid).child(pid).child(key+ ".png");
            UploadTask uploadTask = storageReference.putFile(images.get(key));
            Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    ProgressHud.dialog.dismiss();
                    throw  task.getException();
                }
                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Log.d("MYVALUE",key+"");
                    String downloadUri = String.valueOf(task.getResult());
                    previous_image.put(key,downloadUri);
                    Map<String, Object> map = new HashMap<>();
                    map.put("images",previous_image);
                    FirebaseFirestore.getInstance().collection("Products").document(pid).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("imagesUplaoded","YES");
                            }
                            else {
                                Log.d("imagesUploaded", Objects.requireNonNull(task.getException()).getLocalizedMessage());
                            }
                        }
                    });
                }



            });
        }

    }

    public void updateProduct(String cat_id, String uid, String title, String description, String price) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",productModel.id);
        map.put("cat_id",cat_id);
        map.put("uid",uid);
        map.put("title",title);
        map.put("description",description);
        map.put("price",price);
        map.put("images",productModel.getImages());
        map.put("date", productModel.getDate());
        map.put("sellerName",productModel.getSellerName());
        map.put("sellerImage",productModel.getSellerImage());
        map.put("sellerToken",productModel.getSellerToken());
        ProgressHud.show(EditProductActivity.this,"Product Updating...");

        FirebaseFirestore.getInstance().collection("Products").document(productModel.id).update(map).addOnCompleteListener(task -> {
            ProgressHud.dialog.dismiss();
            if (task.isSuccessful()) {
                uploadImageOnFirebase(uid,productModel.id);
                Services.showDialog(EditProductActivity.this,"UPDATED","Product Successfully Updated");

                images.clear();

            }
            else {
                Services.showDialog(EditProductActivity.this,"ERROR",task.getException().getLocalizedMessage());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null) {
            Log.d("ERROR","OH4");
            Uri filepath = data.getData();
            CropImage.activity(filepath).setOutputCompressQuality(60).start(EditProductActivity.this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                cropUri(result.getUri());

            }
        }

    }
    public void showFileChooser() {

        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        }
        catch (Exception ignored) {

        }

    }

    public void cropUri(Uri resultUri ){
        if (clickedImageViewPosition == 1) {


            Bitmap bitmap;
            try {

                bitmap = MediaStore.Images.Media.getBitmap(EditProductActivity.this.getContentResolver(), resultUri);
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
                bitmap = MediaStore.Images.Media.getBitmap(EditProductActivity.this.getContentResolver(), resultUri);
                twoImage.setImageBitmap(bitmap);
                twoImageSelected = true;
                twoImage.setVisibility(View.VISIBLE);
                twoLL.setVisibility(View.GONE);
                images.put("1",resultUri);
            } catch (IOException ignored) {

            }
        }
        else if (clickedImageViewPosition == 3) {


            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(EditProductActivity.this.getContentResolver(), resultUri);
                threeImage.setImageBitmap(bitmap);
                threeImage.setVisibility(View.VISIBLE);
                threeLL.setVisibility(View.GONE);
                images.put("2",resultUri);
            } catch (IOException ignored) {

            }
        }
        else if (clickedImageViewPosition == 4) {

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(EditProductActivity.this.getContentResolver(), resultUri);
                fourImage.setImageBitmap(bitmap);

                fourImage.setVisibility(View.VISIBLE);
                fourLL.setVisibility(View.GONE);
                images.put("3",resultUri);
            } catch (IOException ignored) {

            }
        }
    }






}
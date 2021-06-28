package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.SubcategoryModel;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;


public class EditSubcategoryActivity extends AppCompatActivity {

    private EditText cat_title_pt;
    private EditText cat_title_en;
    private SubcategoryModel subcategoryModel;
    private String cat_id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subcategory);




        subcategoryModel = (SubcategoryModel) getIntent().getSerializableExtra("subcategory");
        cat_id = getIntent().getStringExtra("cat_id");
        if (subcategoryModel == null) {
            finish();
        }

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        //Cat_title
        cat_title_pt = findViewById(R.id.categoryNamePT);
        cat_title_en = findViewById(R.id.categoryNameEN);

        //AddCategory
        findViewById(R.id.addCategory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title_pt = cat_title_pt.getText().toString().trim();
                String title_en = cat_title_en.getText().toString().trim();

                    if (title_pt.isEmpty()) {
                        Services.showCenterToast(EditSubcategoryActivity.this,"Enter Title");
                    }
                    else {
                        if (title_en.isEmpty()) {
                            Services.showCenterToast(EditSubcategoryActivity.this,"Enter English Title");
                        }
                        else {
                            ProgressHud.show(EditSubcategoryActivity.this,"Updating...");
                            String sub_cat_id =  subcategoryModel.getId();
                            uploadSubcategoryData(sub_cat_id,title_pt,title_en);

                        }

                    }


            }
        });



        cat_title_pt.setText(subcategoryModel.getTitle_pt());
        cat_title_en.setText(subcategoryModel.getTitle_en());


    }



    public void uploadSubcategoryData(String sub_cat_id,String title_pt, String title_en) {
        Map<String, String> map = new HashMap<>();
        map.put("id",sub_cat_id);
        map.put("title_pt",title_pt);
        map.put("title_en",title_en);

        FirebaseFirestore.getInstance().collection("Categories").document(cat_id).collection("Subcategories").document(sub_cat_id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    Services.showCenterToast(EditSubcategoryActivity.this,"Subcategory Updated");


                }
                else {
                    Services.showDialog(EditSubcategoryActivity.this,"ERROR",task.getException().getLocalizedMessage());
                }
            }
        });
    }


}
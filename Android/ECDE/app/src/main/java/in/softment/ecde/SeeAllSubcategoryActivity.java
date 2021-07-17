package in.softment.ecde;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import in.softment.ecde.Adapters.SeeAllSubcategoryAdapter;
import in.softment.ecde.Models.SubcategoryModel;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class SeeAllSubcategoryActivity extends AppCompatActivity {

    private SeeAllSubcategoryAdapter seeAllSubcategoryAdapter;
    private String cat_id;
    private String cat_name;
    private ArrayList<SubcategoryModel> subcategoryModels;
    private TextView no_product_available;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_subcategory);

        //back
        findViewById(R.id.back).setOnClickListener(v -> finish());

        no_product_available = findViewById(R.id.no_product_available);

        cat_id = getIntent().getStringExtra("cat_id");
        cat_name = getIntent().getStringExtra("cat_name");

        TextView categoryName = findViewById(R.id.categoryName);
        categoryName.setText(cat_name);

        subcategoryModels = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        seeAllSubcategoryAdapter = new SeeAllSubcategoryAdapter(this, subcategoryModels);
        recyclerView.setAdapter(seeAllSubcategoryAdapter);
        getSubCategotyData();

    }

    public void getSubCategotyData() {
        ProgressHud.show(this,"");
        String field = "title_pt";

        if (Services.getLocateCode(this).equalsIgnoreCase("pt"))
            field = "title_pt";
        else
            field = "title_en";


        FirebaseFirestore.getInstance().collection("Categories").document(cat_id).collection("Subcategories").orderBy(field, Query.Direction.ASCENDING).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                ProgressHud.dialog.dismiss();
                if (error == null) {
                    subcategoryModels.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            SubcategoryModel subcategoryModel = documentSnapshot.toObject(SubcategoryModel.class);
                            subcategoryModels.add(subcategoryModel);
                        }

                    }

                    if (subcategoryModels.size()>0){
                        no_product_available.setVisibility(View.GONE);
                    }
                    else {
                        no_product_available.setVisibility(View.VISIBLE);
                    }
                    seeAllSubcategoryAdapter.notifyDataSetChanged();

                }
                else {
                    Services.showDialog(SeeAllSubcategoryActivity.this,"ERROR",error.getLocalizedMessage());
                }
            }
        });
    }

    public void gotoSingleCategoryActivity(String sub_cat_id, String sub_cat_name){
        Intent intent = new Intent(this, SingleCategoryActivity.class);
        intent.putExtra("cat_id",cat_id);
        intent.putExtra("cat_name",cat_name);
        intent.putExtra("sub_cat_id",sub_cat_id);
        intent.putExtra("sub_cat_name",sub_cat_name);
        startActivity(intent);
    }
}
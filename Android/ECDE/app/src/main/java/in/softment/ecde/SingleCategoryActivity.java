package in.softment.ecde;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

import in.softment.ecde.Adapters.ProductAdapter;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class SingleCategoryActivity extends AppCompatActivity {
    private RecyclerView products_recyclerview;
    private ProductAdapter productAdapter;
    private ArrayList<ProductModel> productModels;
    private TextView no_product_available;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_category);

        no_product_available = findViewById(R.id.no_product_available);
        products_recyclerview = findViewById(R.id.product_recyclerview);
        products_recyclerview.setHasFixedSize(true);
        products_recyclerview.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));

        productModels = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productModels);
        products_recyclerview.setAdapter(productAdapter);

        String cat_id = getIntent().getStringExtra("cat_id");
        String cat_name = getIntent().getStringExtra("cat_name");
        String sub_cat_id = getIntent().getStringExtra("sub_cat_id");
        String sub_cat_name = getIntent().getStringExtra("sub_cat_name");

        TextView categoryName = findViewById(R.id.categoryName);
        categoryName.setText(cat_name);

        TextView subCategoryName = findViewById(R.id.sub_cat_name);
        subCategoryName.setText(sub_cat_name);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //SearchETTEXT
        EditText searchET = findViewById(R.id.searchEditText);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                productAdapter.filter(s.toString(),ProductModel.singleCatProductModels);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //getproducitems
        getProductItems(cat_id,sub_cat_id);
    }


    public void getProductItems(String cat_id,String sub_cat_id) {
        ProgressHud.show(this,getString(R.string.loading));
            FirebaseFirestore.getInstance().collection("Products").orderBy("date").limitToLast(100).whereEqualTo("cat_id",cat_id).whereEqualTo("sub_cat_id",sub_cat_id).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    ProgressHud.dialog.dismiss();
                    if (error == null) {

                        ProductModel.singleCatProductModels.clear();
                        if (value != null && !value.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                ProductModel productModel = documentSnapshot.toObject(ProductModel.class);
                               ProductModel.singleCatProductModels.add(productModel);
                            }
                            Collections.reverse(ProductModel.singleCatProductModels);
                        }
                        productModels.clear();
                        productModels.addAll(ProductModel.singleCatProductModels);
                        if (productModels.size() > 0) {
                            no_product_available.setVisibility(View.GONE);
                        }
                        else {
                            no_product_available.setVisibility(View.VISIBLE);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                    else {
                        Services.showDialog(SingleCategoryActivity.this,getString(R.string.error),error.getLocalizedMessage());
                    }
                }
            });

    }

}
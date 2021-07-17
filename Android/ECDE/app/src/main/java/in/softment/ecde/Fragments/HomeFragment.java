package in.softment.ecde.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import in.softment.ecde.Adapters.CategoriesAdaper;
import in.softment.ecde.Adapters.ProductAdapter;
import in.softment.ecde.MainActivity;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.R;
import in.softment.ecde.SeeAllCategoryActivity;



public class HomeFragment extends Fragment {
    private ArrayList<ProductModel> productModels;
    private EditText searchET;
    private RecyclerView categories_recyclerview;
    private RecyclerView products_recyclerview;
    private Context context;
    private CategoriesAdaper categoriesAdaper;
    private ProductAdapter productAdapter;
    public HomeFragment(Context context) {
       this.context = context;
    }

    public HomeFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchET = view.findViewById(R.id.searchEditText);

        categories_recyclerview = view.findViewById(R.id.cat_recyclerview);
        categories_recyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        categories_recyclerview.setLayoutManager(linearLayoutManager);

        ArrayList<Drawable> categories_back_view = new ArrayList<>();
        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view2));
        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view3));
        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view));
        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view6));
        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view4));
        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view5));
        categoriesAdaper = new CategoriesAdaper(context,categories_back_view, CategoryModel.categoryModels);
        categories_recyclerview.setAdapter(categoriesAdaper);


        products_recyclerview = view.findViewById(R.id.product_recyclerview);
        products_recyclerview.setHasFixedSize(true);
        products_recyclerview.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        productModels = new ArrayList<>();
        productAdapter = new ProductAdapter(context, productModels);
        products_recyclerview.setAdapter(productAdapter);

        view.findViewById(R.id.seeAllText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SeeAllCategoryActivity.class);
                context.startActivity(intent);
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                productAdapter.filter(s.toString(),ProductModel.latestproductModels);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    public void notifyProductAdapter(){
        productModels.clear();
        productModels.addAll(ProductModel.latestproductModels);
        productAdapter.notifyDataSetChanged();

    }
    public void notifyAdapter(){
        categoriesAdaper.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        ((MainActivity)context).initializeHomeFragment(this);


    }
}
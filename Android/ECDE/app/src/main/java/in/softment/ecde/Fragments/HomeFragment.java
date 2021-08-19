package in.softment.ecde.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.softment.ecde.Adapters.CategoriesAdaper;
import in.softment.ecde.Adapters.ProductAdapter;
import in.softment.ecde.MainActivity;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.R;
import in.softment.ecde.SeeAllCategoryActivity;
import in.softment.ecde.Utils.EndlessRecyclerOnScrollListener;
import in.softment.ecde.Utils.ProgressHud;


public class HomeFragment extends Fragment {
    private ArrayList<ProductModel> productModels;
    private EditText searchET;
    private RecyclerView categories_recyclerview;
    private RecyclerView products_recyclerview;
    private CategoriesAdaper categoriesAdaper;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private boolean isLoading = false;
    private MainActivity mainActivity;
    private boolean hasSearched = false;
    private Context context;

    public HomeFragment(){

    }

    public HomeFragment(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchET = view.findViewById(R.id.searchEditText);

        Client client = new Client("2EZEJI53BF", "a2e685c2d524d5b12212689b9f069e54");
        Index index = client.getIndex("Products");




        categories_recyclerview = view.findViewById(R.id.cat_recyclerview);
        categories_recyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        categories_recyclerview.setLayoutManager(linearLayoutManager);




//        ArrayList<Drawable> categories_back_view = new ArrayList<>();
//        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view2));
//        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view3));
//        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view));
//        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view6));
//        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view4));
//        categories_back_view.add(ContextCompat.getDrawable(context,R.drawable.categories_back_view5));
        categoriesAdaper = new CategoriesAdaper(context, CategoryModel.categoryModels);
        categories_recyclerview.setAdapter(categoriesAdaper);

        progressBar = view.findViewById(R.id.progressbar);

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
                        startActivity(intent);
                    }
                });


                view.findViewById(R.id.cancel_search).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchET.setText("");
                        if (hasSearched) {
                            hasSearched = false;
                            notifyProductAdapter();
                        }
                    }
                });

                searchET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.length() == 0){
                                if (hasSearched) {
                                    hasSearched = false;
                                    notifyProductAdapter();
                                }

                            }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

        searchET.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {

                                if (searchET.getText().toString().isEmpty()) {
                                    notifyProductAdapter();

                                } else {


                                    ProgressHud.show(context, "Searching...");


                                    index.searchAsync(new Query(searchET.getText().toString()), new CompletionHandler() {
                                        @Override
                                        public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                                            ProgressHud.dialog.dismiss();
                                            productModels.clear();
                                            hasSearched = true;
                                            if (e == null) {




                                                JsonElement mJson = null;
                                                try {
                                                    JSONArray jsonArray = jsonObject.getJSONArray("hits");

                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject hit = jsonArray.getJSONObject(i);

                                                        Gson gson = new Gson();

                                                        ProductModel object = gson.fromJson(hit.toString(), ProductModel.class);
                                                        productModels.add(object);

                                                    }

                                                } catch (JSONException jsonException) {
                                                    Log.d("Error", jsonException.getLocalizedMessage());
                                                }


                                            } else {
                                                Log.d("Error", e.getLocalizedMessage());
                                            }


                                            productAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    return true; // consume.
                                }
                            }
                            else{
                                    notifyProductAdapter();
                                }
                            }
                            return false; // pass on to other listeners.
                        }

                }
        );

        return view;
    }


    public void notifyProductAdapter(){
//        isLoading = false;
//        progressBar.setVisibility(View.GONE);
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
        this.context = context;
        ((MainActivity)context).initializeHomeFragment(this);


    }
}
package in.softment.ecde;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import in.softment.ecde.Adapters.SeeAllCategoriesAdapter;
import in.softment.ecde.Models.CategoryModel;

public class SeeAllCategoryActivity extends AppCompatActivity {

    private SeeAllCategoriesAdapter seeAllCategoriesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_category);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        seeAllCategoriesAdapter = new SeeAllCategoriesAdapter(this, CategoryModel.categoryModels);
        recyclerView.setAdapter(seeAllCategoriesAdapter);


        //back
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
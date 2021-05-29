package in.softment.ecde.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import in.softment.ecde.EditCategoriesActivity;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.MyLanguage;
import in.softment.ecde.R;

public class EditCategoriesAdapter extends RecyclerView.Adapter<EditCategoriesAdapter.ViewHolder> {

    private ArrayList<CategoryModel> categoryModels;
    private Context context;
    public EditCategoriesAdapter(Context context, ArrayList<CategoryModel> categoryModels) {
        this.context = context;
        this.categoryModels = categoryModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_categories_layout_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CategoryModel categoryModel = categoryModels.get(position);
        if (MyLanguage.lang.equalsIgnoreCase("pt"))
            holder.cat_title.setText(categoryModel.getTitle_pt());
        else
            holder.cat_title.setText(categoryModel.getTitle_en());

        holder.cat_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditCategoriesActivity.class);
                intent.putExtra("category",categoryModel);
                context.startActivity(intent);
            }
        });
        Glide.with(context).load(categoryModel.image).into(holder.cat_image);
    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    static public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView cat_image, cat_edit;
        private TextView cat_title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cat_edit = itemView.findViewById(R.id.cat_edit);
            cat_image = itemView.findViewById(R.id.cat_image);
            cat_title = itemView.findViewById(R.id.cat_title);
        }
    }
}

package in.softment.ecde.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.R;
import in.softment.ecde.SeeAllSubcategoryActivity;
import in.softment.ecde.Utils.Services;

public class SeeAllCategoriesAdapter extends RecyclerView.Adapter<SeeAllCategoriesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CategoryModel> categoryModels;

    public SeeAllCategoriesAdapter(Context context, ArrayList<CategoryModel> categoryModels){
        this.context = context;
        this.categoryModels = categoryModels;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.see_all_categories_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        CategoryModel categoryModel = categoryModels.get(position);

        if (Services.getLocateCode(context).equalsIgnoreCase("pt"))
            holder.cat_name.setText(categoryModel.getTitle_pt());
        else
            holder.cat_name.setText(categoryModel.getTitle_en());
        Glide.with(context).load(categoryModel.image).placeholder(R.drawable.placeholder1).into(holder.cat_image);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SeeAllSubcategoryActivity.class);
                intent.putExtra("cat_id",categoryModel.id);

                if (Services.getLocateCode(context).equalsIgnoreCase("pt"))
                    intent.putExtra("cat_name",categoryModel.getTitle_pt());
                else
                    intent.putExtra("cat_name",categoryModel.getTitle_en());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView cat_name;
        private RoundedImageView cat_image;
        private View view;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            cat_name = itemView.findViewById(R.id.categoryName);
            cat_image = itemView.findViewById(R.id.categoryImage);
            view = itemView;
        }
    }
}

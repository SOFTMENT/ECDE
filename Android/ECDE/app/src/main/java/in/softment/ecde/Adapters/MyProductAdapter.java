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

import java.util.ArrayList;

import in.softment.ecde.EditProductActivity;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.R;
import in.softment.ecde.StripeCheckoutActivity;
import in.softment.ecde.Utils.Services;

public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ProductModel> productModels;
    public MyProductAdapter(Context context,ArrayList<ProductModel> productModel) {
        this.context = context;
        this.productModels = productModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.myproduct_view_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        ProductModel productModel = productModels.get(position);
        if (productModel.getImages().size() > 0) {
            Glide.with(context).load(productModel.getImages().get("0")).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(holder.imageView);

        }
        holder.title.setText(productModel.title);
        holder.category.setText(CategoryModel.getCategoryNameById(productModel.getCat_id()));
        holder.date.setText(Services.convertDateToString(productModel.date));
        holder.price.setText("R$"+productModel.price);

        if(Services.isPromoting(productModel.adLastDate)) {
            holder.promote.setEnabled(false);
            holder.promote.setText(R.string.promoting);
        }
        else{
            holder.promote.setEnabled(true);
            holder.promote.setText(R.string.promote);
        }

        holder.promote.setOnClickListener(v -> {
            Intent intent = new Intent(context, StripeCheckoutActivity.class);
            intent.putExtra("productId",productModel.getId());
            context.startActivity(intent);


        });
        holder.view.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditProductActivity.class);
            intent.putExtra("index", position);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {

        return productModels.size();
    }

     public static class ViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imageView;
        private TextView title, category, date, price, promote;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            imageView = view.findViewById(R.id.image);
            title = view.findViewById(R.id.title);
            category = view.findViewById(R.id.category);
            date = view.findViewById(R.id.date);
            price = view.findViewById(R.id.price);
            promote = view.findViewById(R.id.promote);
        }
    }
}

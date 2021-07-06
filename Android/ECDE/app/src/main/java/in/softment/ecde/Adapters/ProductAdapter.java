package in.softment.ecde.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.logging.Handler;

import javax.xml.transform.Templates;

import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.R;
import in.softment.ecde.Utils.Services;
import in.softment.ecde.ViewProductActivity;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private final int ads_view = 2;
    private final int product_view = 1;
    private int count = 0;

    private ArrayList<ProductModel> productModels;
    public ProductAdapter(Context context, ArrayList<ProductModel> productModels){
        this.context = context;
        this.productModels = productModels;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == product_view)
        return new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.product_layout_view,parent,false));
        else
            return new AdsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ads_view_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        if (holder.getItemViewType() == product_view) {
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            int newIndex = position - (position / 10);
            ProductModel productModel = productModels.get(newIndex);
            if (productModel.images.size() > 0) {
                Glide.with(context).load(productModel.getImages().get("0")).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(productViewHolder.roundedImageView);
            }
            productViewHolder.productTitle.setText(Services.toUpperCase(productModel.title));
            productViewHolder.productPrice.setText("R$"+productModel.price);

            productViewHolder.view.setOnClickListener(view -> {

                Intent intent = new Intent(context, ViewProductActivity.class);
                intent.putExtra("product",productModel);
                context.startActivity(intent);
            });

            if (Services.isPromoting(productModel.adLastDate)) {
                productViewHolder.adsCard.setVisibility(View.VISIBLE);
            }
            else {
                productViewHolder.adsCard.setVisibility(View.GONE);
            }

        }
        else {

            AdsViewHolder adsViewHolder = (AdsViewHolder) holder;
            AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.admob_native_ads_unit))
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NotNull NativeAd NativeAd) {

                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder().build();
                            adsViewHolder.templateView.setStyles(styles);
                            adsViewHolder.templateView.setNativeAd(NativeAd);

                        }

                    })
                    .build();


            adLoader.loadAd(new AdRequest.Builder().build());
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (position % 10 == 9)  {
            return ads_view;
        }
        else {
            return product_view;
        }
    }

    @Override
    public int getItemCount() {
        if (productModels.size() > 0) {
            return productModels.size() + (productModels.size() / 10);

        }
        else {
            return 0;
        }

    }

   static public class ProductViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView roundedImageView;
        private TextView productTitle, productPrice;
        private View view;
        private CardView adsCard;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            roundedImageView = itemView.findViewById(R.id.product_image);
            productPrice = itemView.findViewById(R.id.product_price);
            productTitle = itemView.findViewById(R.id.product_title);
            adsCard = itemView.findViewById(R.id.adsCard);
        }
    }

    static public class AdsViewHolder extends RecyclerView.ViewHolder {
        private TemplateView templateView;
        public AdsViewHolder(@NonNull View itemView) {
            super(itemView);
            templateView = itemView.findViewById(R.id.my_template);
        }
    }

    public void filter(String text,ArrayList<ProductModel> mainProductModels){
        ArrayList<ProductModel> newproductModels = new ArrayList<>();

        for (ProductModel productModel : mainProductModels) {
            if (productModel.title.toLowerCase().contains(text.toLowerCase()) || productModel.storeCity.toLowerCase().contains(text.toLowerCase()) ) {
                newproductModels.add(productModel);
            }
        }
        productModels.clear();
        productModels.addAll(newproductModels);
        notifyDataSetChanged();

    }
}

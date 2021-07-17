package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.ecde.Adapters.MyHeaderPagerAdapter;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class ViewProductActivity extends AppCompatActivity {

    private ViewPager headerviewpager;
    private int current_pos = 0;
    private LinearLayout dotlayout;
    private ProductModel productModel;
    private boolean fromDeepLink = false;
    private  ImageView[] dots;
    private int previouspos = 0;
    private ImageView more;
    private static InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);


        //LOADINTERSTITALSADS
        loadInterstitialAds(this,getString(R.string.admob_interstitial_ads_unit));

        //MOREACTION
        more = findViewById(R.id.more);

        if (UserModel.data.emailAddress.equalsIgnoreCase("ecde.app@gmail.com")){
            more.setVisibility(View.VISIBLE);

        }
        else {
            more.setVisibility(View.GONE);
        }



        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });




        productModel = (ProductModel)getIntent().getSerializableExtra("product");
        fromDeepLink = getIntent().getBooleanExtra("fromDeepLink",false);

        if (productModel == null) {

            finish();
        }

        if (productModel.getImages().size() < 1) {
            finish();
        }
        //Slider & DotLayout
        dotlayout = findViewById(R.id.dotlayout);
        headerviewpager = findViewById(R.id.headerviewpager);
        ArrayList<String> images = new ArrayList<>(productModel.getImages().values());
        MyHeaderPagerAdapter myHeaderPagerAdapter = new MyHeaderPagerAdapter(this,images);
        headerviewpager.setAdapter(myHeaderPagerAdapter);


        TextView title = findViewById(R.id.title);
        TextView category = findViewById(R.id.category);

        TextView price = findViewById(R.id.price);
        TextView description = findViewById(R.id.description);
        TextView seller = findViewById(R.id.sellerName);
        LinearLayout deliveryLL = findViewById(R.id.deliveryLL);
        TextView deliveryDay = findViewById(R.id.deliveryDays);
        TextView condition = findViewById(R.id.condition);
        TextView quantity = findViewById(R.id.quantity);
        TextView city = findViewById(R.id.city);
        CircleImageView storeImage = findViewById(R.id.storeImage);
        if (productModel.getStoreImage() != null && !productModel.getStoreImage().isEmpty()) {
            Glide.with(this).load(productModel.getStoreImage()).placeholder(R.drawable.placeholder).into(storeImage);
        }


        title.setText(Services.toUpperCase(productModel.title));
        category.setText(CategoryModel.getCategoryNameById(this,productModel.getCat_id()));
        description.setText(productModel.description);
        if (productModel.getPrice() == 0) {
            price.setVisibility(View.GONE);
        }
        else {
            price.setVisibility(View.VISIBLE);
            price.setText("R$ "+productModel.getPrice());
        }

        seller.setText(Services.toUpperCase(productModel.sellerName));
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromDeepLink) {
                    Intent intent = new Intent(ViewProductActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    finish();
                }
            }
        });

        if (productModel.isProductNew){
            condition.setText(R.string.new_word);
        }
        else{
            condition.setText(R.string.used);
        }

        quantity.setText(String.valueOf(productModel.quantity));
        city.setText(productModel.storeCity);

        if (productModel.isDeliverProduct()) {
            deliveryLL.setVisibility(View.VISIBLE);
         //   deliveryFee.setText("R$ "+productModel.deliveryCharge);
            int days = productModel.maxDeliverDay;
            if (productModel.isSameDayDeliver()) {
                deliveryDay.setText(getString(R.string.day1));
            }
            else {
                deliveryDay.setText(days+" "+getString(R.string.days));
            }

        }
        else {
            deliveryLL.setVisibility(View.GONE);
        }


        CardView contactSellerBtn = findViewById(R.id.contactSeller);


        //ContactSeller
        contactSellerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (productModel.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    Services.showCenterToast(ViewProductActivity.this,getString(R.string.you_can_not_contact));
                }
                else {
                    if (mInterstitialAd == null)
                        gotoChatScreen();
                    else
                        showInterstitialsAds(ViewProductActivity.this);
                }


            }
        });

        //ShareProduct
        findViewById(R.id.shareProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProductURL();
            }
        });

        dots = new ImageView[productModel.getImages().size()];

        for (int i = 0 ; i < dots.length;i++) {
            dots[i] = new ImageView(this);
        }

        preparedots(0);
        headerviewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                        preparedots(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }



    private void createProductURL(){

        ProgressHud.show(ViewProductActivity.this, "");
        String productLinkText = "https://ecde.page.link/?"+
                "link=https://www.softment.in/?productId="+productModel.getId()+
                "&apn="+getPackageName()+
                "&st="+productModel.getTitle()+
                "&sd="+productModel.getDescription()+
                "&si="+productModel.getImages().get("0");



                FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(productLinkText)).buildShortDynamicLink().addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<ShortDynamicLink> task) {
                        ProgressHud.dialog.dismiss();
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.e("main ", "short link "+ shortLink.toString());
                            // share app dialog
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(intent);
                        }
                        else {
                            Services.showDialog(ViewProductActivity.this,"ERROR", Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        }
                    }
                });


    }

    private void showPopupMenu(){
        PopupMenu popupMenu = new PopupMenu(this,more);
        popupMenu.getMenuInflater().inflate(R.menu.product_action,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.delete) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProductActivity.this,R.style.AlertDialogTheme);
                builder.setTitle(R.string.delete_product_capital);
                builder.setMessage(R.string.are_you_sure_to_delete_item);
                builder.setCancelable(false);


               builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       ProgressHud.show(ViewProductActivity.this,getString(R.string.deleting));
                       FirebaseFirestore.getInstance().collection("Products").document(productModel.id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull @NotNull Task<Void> task) {


                               if (task.isSuccessful()){
                                   Services.showCenterToast(ViewProductActivity.this,getString(R.string.product_has_deleted));
                               }
                               ProgressHud.dialog.dismiss();
                               dialog.dismiss();
                               finish();
                           }
                       });
                   }
               });

               builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                   }
               });



                AlertDialog alertDialog = builder.create();


                alertDialog.show();



            }
            return true;
        });
        popupMenu.show();
    }


    private void preparedots(int dotcustomposi) {

        if (dotlayout.getChildCount() > 0) {
            dotlayout.removeAllViews();
        }


        for (int i = 0 ; i < dots.length;i++) {

            if (i == dotcustomposi) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this,R.drawable.active_dot));
            }
            else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this,R.drawable.inactive_dot));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10,0,10,0);
            dotlayout.addView(dots[i],layoutParams);

        }

    }


    public void gotoChatScreen(){
        Intent intent = new Intent(ViewProductActivity.this, ChatScreenActivity.class);
        intent.putExtra("sellerId",productModel.getUid());
        intent.putExtra("sellerImage",productModel.getStoreImage());
        intent.putExtra("sellerName",productModel.getSellerName());
        intent.putExtra("sellerToken",productModel.getSellerToken());
        startActivity(intent);
    }

    public void showInterstitialsAds(Activity context){
        if (mInterstitialAd != null)
            mInterstitialAd.show(context);

    }

    public void loadInterstitialAds(Context context, String adsUnit){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context,adsUnit, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();

                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        gotoChatScreen();
                    }
                });
            }




            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (fromDeepLink) {
            Intent intent = new Intent(ViewProductActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else {
            finish();
        }
    }
}
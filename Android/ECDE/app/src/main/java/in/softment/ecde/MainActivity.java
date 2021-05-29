package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import in.softment.ecde.Fragments.AccountFragment;
import in.softment.ecde.Fragments.ChatFragment;
import in.softment.ecde.Fragments.GigFragment;
import in.softment.ecde.Fragments.HomeFragment;
import in.softment.ecde.Fragments.PostFragment;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.LastMessageModel;
import in.softment.ecde.Models.MyLanguage;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.Utils.MyFirebaseMessagingService;
import in.softment.ecde.Utils.NewCode;
import in.softment.ecde.Utils.NonSwipeAbleViewPager;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class MainActivity extends AppCompatActivity implements Function1<MeowBottomNavigation.Model, Unit> {





   private MeowBottomNavigation meowBottomNavigation;
   private HomeFragment homeFragment;
   private PostFragment postFragment;
   private GigFragment gigFragment;
   private ChatFragment chatFragment;

   private NonSwipeAbleViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull @NotNull InitializationStatus initializationStatus) {
                List<String> testDeviceIds = Collections.singletonList("4C15EAC0ECF0FDD990A883F3CEA75CB1");
                RequestConfiguration configuration =
                        new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                MobileAds.setRequestConfiguration(configuration);
            }
        });



        //UpdateToken
        updateToken();

        //ViewPager
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(5);

        meowBottomNavigation = findViewById(R.id.bottomnavigation1);
        meowBottomNavigation.add(new MeowBottomNavigation.Model(0, R.drawable.ic_outline_home_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_outline_message_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_baseline_add_circle_outline_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_baseline_storefront_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.ic_baseline_person_outline_24));
        meowBottomNavigation.show(0,true);
        meowBottomNavigation.setOnShowListener(this);
        viewPager.setCurrentItem(0);

        ProgressHud.show(this,"Loading...");
    }

    public void updateToken(){

        Map<String,Object> map = new HashMap<>();
        map.put("token", MyFirebaseMessagingService.getToken(this));
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        FirebaseFirestore.getInstance().collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());

    }

    public void getMyProduct() {
        FirebaseFirestore.getInstance().collection("Products").orderBy("date").whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(MetadataChanges.INCLUDE, (value, error) -> {

            if (error == null) {

                ProductModel.myproductsModels.clear();
                if (value != null && !value.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        ProductModel productModel = documentSnapshot.toObject(ProductModel.class);
                        ProductModel.myproductsModels.add(productModel);
                    }
                    Collections.reverse(ProductModel.myproductsModels);
                }

                gigFragment.notifyAdapter();

            }
            else {
                Services.showDialog(MainActivity.this,"ERROR",error.getLocalizedMessage());
            }

        });
    }


    public void getLatestProduct() {
        FirebaseFirestore.getInstance().collection("Products").orderBy("date").limitToLast(100).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                ProgressHud.dialog.dismiss();
                if (error == null) {
                    ProductModel.latestproductModels.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            ProductModel productModel = documentSnapshot.toObject(ProductModel.class);
                            ProductModel.latestproductModels.add(productModel);
                        }
                        Collections.reverse(ProductModel.latestproductModels);
                    }


                   homeFragment.notifyProductAdapter();
                }
                else {
                    Services.showDialog(MainActivity.this,"ERROR",error.getLocalizedMessage());
                }
            }
        });
    }

    public void getCategotyData() {
        String field = "title_pt";
        if (MyLanguage.lang.equalsIgnoreCase("pt"))
           field = "title_pt";
        else
            field = "title_en";


        FirebaseFirestore.getInstance().collection("Categories").orderBy(field, Query.Direction.ASCENDING).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    CategoryModel.categoryModels.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            CategoryModel categoryModel = documentSnapshot.toObject(CategoryModel.class);
                            CategoryModel.categoryModels.add(categoryModel);
                        }

                    }

                    homeFragment.notifyAdapter();
                    postFragment.notifyAdapter();
                }
                else {
                    Services.showDialog(MainActivity.this,"ERROR",error.getLocalizedMessage());
                }
            }
        });
    }

    //getLastChatModelData
    public void getLastMessageData(){
        FirebaseFirestore.getInstance().collection("Chats").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("LastMessage").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    LastMessageModel.lastMessageModels.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            LastMessageModel lastMessageModel = documentSnapshot.toObject(LastMessageModel.class);
                            LastMessageModel.lastMessageModels.add(lastMessageModel);
                        }

                    }


                    chatFragment.notifyAdapter();

                }
                else {

                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new HomeFragment(this));
        adapter.addFrag(new ChatFragment(this));
        adapter.addFrag(new PostFragment(this));
        adapter.addFrag(new GigFragment(this));
        adapter.addFrag(new AccountFragment(this));
        viewPager.setAdapter(adapter);
    }



    @Override
    public Unit invoke(MeowBottomNavigation.Model model) {
        viewPager.setCurrentItem(model.getId());
        return null;
    }
    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {

            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);

        }


    }





    public void initializeGigFragment(GigFragment gigFragment) {
        this.gigFragment = gigFragment;

        //myProductData
        getMyProduct();

        //getCategoryData
        getCategotyData();

    }


    public void initializeHomeFragment(HomeFragment homeFragment){
        this.homeFragment = homeFragment;
        //getLatestProductData
        getLatestProduct();
    }

    public void initializePostFragment(PostFragment postFragment){
        this.postFragment = postFragment;
    }

    public void initializeChatFragment(ChatFragment chatFragment){
        this.chatFragment = chatFragment;
        getLastMessageData();
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                postFragment.cropUri(result.getUri());

            }
        }


    }

    public void changeBottomBarPossition(int id) {
        viewPager.setCurrentItem(id);
        meowBottomNavigation.show(id,true);
    }

}



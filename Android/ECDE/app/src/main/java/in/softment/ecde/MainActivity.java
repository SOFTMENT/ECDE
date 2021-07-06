package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.canhub.cropper.CropImage;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.makeramen.roundedimageview.RoundedImageView;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import in.softment.ecde.Fragments.AccountFragment;
import in.softment.ecde.Fragments.ChatFragment;
import in.softment.ecde.Fragments.GigFragment;
import in.softment.ecde.Fragments.HomeFragment;
import in.softment.ecde.Fragments.PostFragment;
import in.softment.ecde.Fragments.SellerStoreInformation;
import in.softment.ecde.Models.CategoryModel;
import in.softment.ecde.Models.LastMessageModel;
import in.softment.ecde.Models.MyLanguage;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.Models.UpdateType;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.Utils.MyFirebaseMessagingService;
import in.softment.ecde.Utils.NewCode;
import in.softment.ecde.Utils.NonSwipeAbleViewPager;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import meow.bottomnavigation.*;
public class MainActivity extends AppCompatActivity implements Function1<MeowBottomNavigation.Model, Unit> {

    private MeowBottomNavigation meowBottomNavigation;
    public HomeFragment homeFragment;
    public ChatFragment chatFragment;
    private NonSwipeAbleViewPager viewPager;
    public PostFragment postFragment;
    public GigFragment gigFragment;
    public SellerStoreInformation sellerStoreInformation;
    private ViewPagerAdapter viewPagerAdapter;

    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 11;
    int updateCode = AppUpdateType.FLEXIBLE;

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




        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        checkForUpdate();

        //UpdateToken
        updateToken();

        //ViewPager
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(5);

        meowBottomNavigation = findViewById(R.id.bottomnavigation1);
        meowBottomNavigation.add(new MeowBottomNavigation.Model(0, R.drawable.ic_outline_shopping_bag_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_outline_message_24));

        meowBottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_baseline_add_circle_outline_24));

        meowBottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_baseline_storefront_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.ic_baseline_person_outline_24));


        meowBottomNavigation.show(0,true);
        meowBottomNavigation.setOnShowListener(this);
        viewPager.setCurrentItem(0);


        //GET_CATEGORY

        ProgressHud.show(this,"");


    }



    public void checkForUpdate(){
        FirebaseFirestore.getInstance().collection("UpdateType").document("status").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                    if (error == null){
                        if (value != null && value.exists()){
                            UpdateType updateType = value.toObject(UpdateType.class);
                            Log.d("VIJAYCODE", Objects.requireNonNull(updateType).updateCode+" WAAH VIJAY");
                            updateCode = updateType.updateCode;
                        }
                    }

                mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(updateCode)){

                        try {
                            mAppUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo, updateCode , MainActivity.this, RC_APP_UPDATE);

                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }

                    } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                        //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip

                        popupSnackbarForCompleteUpdate();
                    } else {
                        Log.e("ECDE", "checkForAppUpdateAvailability: something else");
                    }
                });
            }
        });


    }

    InstallStateUpdatedListener installStateUpdatedListener = new
            InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    if (state.installStatus() == InstallStatus.DOWNLOADED){
                        //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                        popupSnackbarForCompleteUpdate();
                    } else if (state.installStatus() == InstallStatus.INSTALLED){
                        if (mAppUpdateManager != null){
                            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                        }

                    } else {
                        Log.i("ECDE", "InstallStateUpdatedListener: state: " + state.installStatus());
                    }
                }
            };

    public void updateToken(){

        Map<String,Object> map = new HashMap<>();
        map.put("token", MyFirebaseMessagingService.getToken(this));
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        FirebaseFirestore.getInstance().collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());

    }



    public void getLatestProduct() {
        FirebaseFirestore.getInstance().collection("Products").orderBy("date", Query.Direction.DESCENDING).limitToLast(999).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
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

                    }

                    ArrayList<ProductModel> featuredModels = new ArrayList<>();

                    int y = 0;


                    for(y = 0 ; y < ProductModel.latestproductModels.size() ; y++) {
                        if (Services.isPromoting(ProductModel.latestproductModels.get(y).adLastDate)) {
                            featuredModels.add(ProductModel.latestproductModels.get(y));
                            ProductModel.latestproductModels.remove(y);

                        }
                    }

                    Collections.shuffle(featuredModels);
                    int x = 0;
                    for (ProductModel featuredModel : featuredModels) {
                        if (x >= 2) {
                            break;
                        }
                        ProductModel.latestproductModels.add(x,featuredModel);
                        x = x +1;

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
                    if (postFragment != null)
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



    private void setupViewPager(ViewPager viewPager) {

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFrag(new HomeFragment(this));
        viewPagerAdapter.addFrag(new ChatFragment(this));
        if (UserModel.data.isSeller()){

            viewPagerAdapter.addFrag(new PostFragment(this));
        }

        else {

            viewPagerAdapter.addFrag(new SellerStoreInformation(this));

        }

        viewPagerAdapter.addFrag(new GigFragment(this));
        viewPagerAdapter.addFrag(new AccountFragment(this));

        viewPager.setAdapter(viewPagerAdapter);

    }




    public void notifyPagerAdapter() {
        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(2);
    }


    @Override
    public Unit invoke(MeowBottomNavigation.Model model) {
        viewPager.setCurrentItem(model.getId());
        return null;
    }
   class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 2) {
                if (UserModel.data.isSeller())  {
                    return new PostFragment(MainActivity.this);
                }
                else {
                    return new SellerStoreInformation(MainActivity.this);
                }
            }

            return mFragmentList.get(position);
        }

       @Override
       public int getItemPosition(@NonNull @NotNull Object object) {
            return POSITION_NONE;
       }

       @Override
        public int getCount() {

            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);

        }


    }


    public void initializeHomeFragment(HomeFragment homeFragment){
        this.homeFragment = homeFragment;
        //getLatestProductData
        getLatestProduct();
        getCategotyData();
    }



    public void initializeChatFragment(ChatFragment chatFragment){
        this.chatFragment = chatFragment;
        getLastMessageData();
    }

    public void initializePostFragment(PostFragment postFragment){
        this.postFragment = postFragment;
        //getCategoryData
        getCategotyData();
    }

    public void initializeSellerFragment(SellerStoreInformation sellerStoreInformation) {
        this.sellerStoreInformation = sellerStoreInformation;
    }

    public void initializeGigFragment(GigFragment gigFragment) {
        this.gigFragment = gigFragment;

        //myProductData
        getMyProduct();



    }



    public void changeBottomBarPossition(int id) {
        viewPager.setCurrentItem(id);
        meowBottomNavigation.show(id,true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                if (UserModel.data.isSeller())
                   postFragment.cropUri(result.getUriContent());
                else
                    sellerStoreInformation.cropUrl(result.getUriContent());
            }
        }
        else if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("ECDE", "onActivityResult: app download failed");
            }
        }


    }

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.relative_layout),
                        "New app is ready!",
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (mAppUpdateManager != null){
                mAppUpdateManager.completeUpdate();
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.salmon));
        snackbar.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Services.loadLocale(this);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        }
        else {
            viewPager.setCurrentItem(0);
            meowBottomNavigation.show(0,true);
        }
    }
}



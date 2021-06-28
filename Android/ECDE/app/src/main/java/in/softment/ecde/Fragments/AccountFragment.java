package in.softment.ecde.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.service.autofill.UserData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.ecde.BuildConfig;
import in.softment.ecde.MainActivity;
import in.softment.ecde.ManageCategoryActivity;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.PrivacyPolicyActivity;
import in.softment.ecde.R;
import in.softment.ecde.SellerStoreInformationActivity;
import in.softment.ecde.SupportActivity;
import in.softment.ecde.TermsAndConditions;
import in.softment.ecde.Utils.Services;
import in.softment.ecde.WelcomeActivity;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private Context context;
    private TextView buyOrSellTextView;
    private RelativeLayout addProductRR, myProductRR;

    public AccountFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);


        addProductRR = view.findViewById(R.id.addProductRR);
        myProductRR = view.findViewById(R.id.myProductRR);


        //ADMIN
        LinearLayout adminItemsView = view.findViewById(R.id.adminItemView);
        if (UserModel.data.emailAddress.equalsIgnoreCase("ecde.app@gmail.com") || UserModel.data.emailAddress.equalsIgnoreCase("vijay.ajay70111@gmail.com")) {
            adminItemsView.setVisibility(View.VISIBLE);
            RelativeLayout addCat = view.findViewById(R.id.addCat);
            addCat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(context, ManageCategoryActivity.class));
                }
            });

        }
        else {
            adminItemsView.setVisibility(View.GONE);
        }


        TextView langName = view.findViewById(R.id.languageName);
        langName.setText(getLanguageName());

        TextView versionName = view.findViewById(R.id.versionName);
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            versionName.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Logout
        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Services.logout(context);
            }
        });
           CircleImageView circleImageView = view.findViewById(R.id.profile_image);
           TextView name = view.findViewById(R.id.name);
           TextView email = view.findViewById(R.id.email);
           Glide.with(context).load(UserModel.data.profileImage).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(circleImageView);
           name.setText(UserModel.data.fullName);
            email.setText(UserModel.data.emailAddress);

       //ShareApp
        view.findViewById(R.id.shareApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Esquerda Compra Da Esquerda");
                    String shareMessage= "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });





        //AddProduct
        view.findViewById(R.id.addProductRR).setOnClickListener(v -> {

                ((MainActivity)context).changeBottomBarPossition(2);

        });


        //MyProducts
        view.findViewById(R.id.myProductRR).setOnClickListener(v -> {

                ((MainActivity)context).changeBottomBarPossition(3);

        });

        //support
        view.findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, SupportActivity.class));
            }
        });

        //Language
        view.findViewById(R.id.language).setOnClickListener(v -> {
            final String[] listItems = {"English","Portuguese"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Choose Language");
            builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        setLocale("en");
                        restart();
                    }
                    else if (which == 1){
                        setLocale("pt");
                        restart();
                    }
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();



        });


        //PrivacyPolicy
        view.findViewById(R.id.privacypolicy).setOnClickListener(v -> {
           context.startActivity(new Intent(context, PrivacyPolicyActivity.class));
        });

        //Terms&Conditions
        view.findViewById(R.id.termsandcondition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, TermsAndConditions.class));
            }
        });


        //AppDeveloper
        view.findViewById(R.id.developer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.softment.in"));
                startActivity(browserIntent);
            }
        });

        //RateUs
        view.findViewById(R.id.rateUs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(myAppLinkToMarket);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show();
                }
            }
        });

        //ContactUs
        view.findViewById(R.id.contactUs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }



    private void restart() {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    private void setLocale(String code){
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration,context.getResources().getDisplayMetrics());

        //SharedPref
        SharedPreferences.Editor sharedPreferences = context.getSharedPreferences("lang", MODE_PRIVATE).edit();
        sharedPreferences.putString("mylang",code);
        sharedPreferences.apply();
    }


    public String getLanguageName(){
        //SharedPref
        SharedPreferences sharedPreferences = context.getSharedPreferences("lang",MODE_PRIVATE);
        String code = sharedPreferences.getString("mylang","pt");

        if (code.equalsIgnoreCase("pt")) {
            return  "Portuguese";
        }
        else {
            return "English";
        }
    }

}
package in.softment.ecde.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import in.softment.ecde.Adapters.MyProductAdapter;
import in.softment.ecde.BuildConfig;
import in.softment.ecde.MainActivity;
import in.softment.ecde.Models.ProductModel;
import in.softment.ecde.R;

public class GigFragment extends Fragment {


    private Context context;
    private TextView message;
    private RecyclerView recyclerView;
    private MyProductAdapter myProductAdapter;
    public GigFragment(Context context) {
        this.context = context;
    }

    public GigFragment(){

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_gig, container, false);
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

        message = view.findViewById(R.id.message);
        recyclerView = view.findViewById(R.id.recyclerview);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        myProductAdapter = new MyProductAdapter(context, ProductModel.myproductsModels);
        recyclerView.setAdapter(myProductAdapter);
        return view;
    }

    public void notifyAdapter(){
        myProductAdapter.notifyDataSetChanged();
        if (ProductModel.myproductsModels.size() < 1) {
            message.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }
        else {
            message.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity)context).initializeGigFragment(this);
    }
}
package in.softment.ecde.Fragments;

import android.content.Context;
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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_gig, container, false);
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
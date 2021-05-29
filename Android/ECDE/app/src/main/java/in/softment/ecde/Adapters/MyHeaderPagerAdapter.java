package in.softment.ecde.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import in.softment.ecde.R;
import in.softment.ecde.ViewProductActivity;


public class MyHeaderPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> images;
    private LayoutInflater layoutInflater;
    public MyHeaderPagerAdapter(Context context, ArrayList<String> images) {
        this.images = images;
        this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {



        View itemview = layoutInflater.inflate(R.layout.headerrow,container, false);

        ImageView imageView = itemview.findViewById(R.id.imageview);
        Glide.with(context).load(images.get(position)).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.placeholder1).into(imageView);

        container.addView(itemview);
        return itemview;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView)object);
    }


}

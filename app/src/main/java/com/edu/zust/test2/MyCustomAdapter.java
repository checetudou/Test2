package com.edu.zust.test2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class MyCustomAdapter extends ArrayAdapter {
    private final Context context;

    protected String[] fnames;

    public MyCustomAdapter(Context context, int resId) {
        super(context, resId);
        this.context = context;
    }

    public void setData(String[] fnames) {
        this.fnames = fnames;

        for (int i=0; i<fnames.length; i++) {
            add(null);
        }
    }

    @NonNull
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.image_selection, parent, false);
        }

        // set the image for ImageView
        ImageView imageView = view.findViewById(R.id.grid_image);
        int id = context.getResources().getIdentifier(fnames[pos],
                "drawable", context.getPackageName());
        imageView.setImageResource(id);

        return view;
    }
}
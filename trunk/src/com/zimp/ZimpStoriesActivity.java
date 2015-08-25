package com.zimp;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ZimpStoriesActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.zimpstories);

	    Gallery gallery = (Gallery) findViewById(R.id.gallery);
	    gallery.setAdapter(new ImageAdapter(this));
	    gallery.setSelection(1);

	    gallery.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	        }
	    });
	}
	
	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;

	    private Integer[] mImageIds = {
	            R.drawable.story1,
	            R.drawable.story2,
	            R.drawable.story3
	    };
	    
	    private String[] mImageTexts = {
	            "My Hell",
	            "Zombie Rogue",
	            "Treasure"
	    };
	    	    

	    public ImageAdapter(Context c) {
	        mContext = c;
	        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.ZimpStories);
	        mGalleryItemBackground = attr.getResourceId(
            R.styleable.ZimpStories_android_galleryItemBackground, 0);
	        attr.recycle();
	    }

	    public int getCount() {
	        return mImageIds.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {

	        RelativeLayout borderImg = new RelativeLayout(getBaseContext());;   

	        ImageView imageView = new ImageView(mContext);
	        imageView.setImageResource(mImageIds[position]);
	        imageView.setLayoutParams(new Gallery.LayoutParams(250, 200));
	        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	        imageView.setBackgroundResource(mGalleryItemBackground);
	        
	        TextView result = new TextView(getBaseContext());			 
            result.setText(mImageTexts[position]);
            result.setGravity(Gravity.CENTER);
            result.setTextSize(15);
            result.setTextColor(0xffff0000);
            result.setTypeface(null, 1);
            result.setPadding(0, 200, 0, 0);
            result.setWidth(200);
            result.setShadowLayer(10, 10, 10, 0xffff0000);

	        borderImg.addView(imageView);
            borderImg.addView(result);
            
	        return borderImg;
	    }

	}
}

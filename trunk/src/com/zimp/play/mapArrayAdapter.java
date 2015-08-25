package com.zimp.play;

import java.util.ArrayList;

import com.zimp.R;
import com.zimp.gamedata.MapTiles;
import com.zimp.play.ZimpAActivity.GameState;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

class mapArrayAdapter extends BaseAdapter {

	Context context;
	ArrayList<MapTiles> list;
	Drawable blank = new ColorDrawable(Color.DKGRAY);
//	Drawable possiblePlace = new ColorDrawable(Color.argb(255, 150, 193, 32)); // Android green
//	Bitmap upArrow = null, downArrow = null, leftArrow = null, rightArrow = null;
	
	
	public mapArrayAdapter(Context context, ArrayList<MapTiles> tiles) {
		super();
		this.context = context;
		this.list = tiles;
		
/*		upArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.exittop);
		Matrix matrix = new Matrix();
		matrix.preRotate(-90 * 1);
		leftArrow = Bitmap.createBitmap(upArrow, 0, 0, upArrow.getWidth(), upArrow.getHeight(), matrix, true);
		downArrow = Bitmap.createBitmap(leftArrow, 0, 0, leftArrow.getWidth(), leftArrow.getHeight(), matrix, true);
		rightArrow = Bitmap.createBitmap(downArrow, 0, 0, downArrow.getWidth(), downArrow.getHeight(), matrix, true);
*/
	}

/*	public Bitmap activeTileBitmap(Bitmap bitmap) {
		Bitmap b = bitmap.copy(bitmap.getConfig(), true);
		int w = b.getWidth();
		int h = b.getHeight();
		int color = Color.argb(255, 255, 36, 33);
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < w ; j++) {
				b.setPixel(j, i, color);
			}

			for(int j = 0; j < w ; j++) {
				b.setPixel(j, h - i - 1, color);
			}

			for(int j = 0; j < h ; j++) {
				b.setPixel(i, j, color);
			}

			for(int j = 0; j < h ; j++) {
				b.setPixel(w - i - 1, j, color);
			}
		}
		return b;
	}*/
	
	@Override
	public View getView(int position, View v, ViewGroup vg) {
		ImageView imageView = null;
		//Log.e("asd", "getView called for position " + position);
		if (v == null) {
			imageView = new ImageView(this.context);
			//imageView.setLayoutParams(new GridView.LayoutParams(MapView.zoomLevels * 100, MapView.zoomLevels * 100));
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setPadding(0, 0, 0, 0);
		} else {
			imageView = (ImageView) v;
		}

		imageView.setLayoutParams(new GridView.LayoutParams(MapView.zoomLevels * 100, MapView.zoomLevels * 100));

		if (this.list.get(position) != null) {
			//Log.e("asd", "Found the tile ");
			MapTiles tile = (MapTiles) this.list.get(position);

			
			Bitmap b = BitmapFactory.decodeFile(tile.getTexturePath());
			if (tile.getRotation() > 0) {
				Matrix matrix = new Matrix();
				matrix.preRotate(-90 * tile.getRotation());
				
				Bitmap rotatedBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
				
				imageView.setImageBitmap(rotatedBitmap);
			} else {
				imageView.setImageBitmap(b);
			}
			/*
			 * Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
			 * bitmapOrg.getWidth(), bitmapOrg.getHeight(), matrix, true);
			 * 
			 * imageView.setImageResource((Integer) tile.resourceId); Matrix m =
			 * imageView.getImageMatrix();
			 * 
			 * if(tile.angle > 0) { Log.d("asd", "Rotating the tile by " + 90 *
			 * tile.angle); m.setRotate(90 * tile.angle,
			 * (imageView.getWidth()/2), (imageView.getHeight()/2));
			 * m.postRotate(90 * tile.angle, (imageView.getWidth()/2),
			 * (imageView.getHeight()/2)); imageView.setImageMatrix(m);
			 * imageView.invalidate(); //imageView.requestLayout();
			 * vg.requestLayout(); }
			 */
		} else {
			/*
			// Log.d("asd", "Tile empty at " + position + "  this.list.size" +
			// this.list.size());
			int activeMapTileY = (int)GameState.activeMapTileLoc/GameState.gridX;
			int activeMapTileX = (int)GameState.activeMapTileLoc%GameState.gridX;
			int currClickY = (int)position/GameState.gridX;
			int currClickX = (int)position%GameState.gridX;
			int tileAngle = -1;
			Bitmap b = null;
			
			if(activeMapTileX == currClickX) {
				if(activeMapTileY == currClickY + 1) {
					tileAngle = 2;
					b = upArrow;
				} else if(activeMapTileY + 1 == currClickY) {
					tileAngle = 0;
					b = downArrow;
				} else {
					tileAngle = -1;
				}
			} else if(activeMapTileY == currClickY) {
				if(activeMapTileX == currClickX + 1) {
					b = leftArrow;
					tileAngle = 1;
				} else if(activeMapTileX + 1 == currClickX) {
					b = rightArrow;
					tileAngle = 3;
				} else {
					tileAngle = -1;
				}
			} else {
				tileAngle = -1;
			}
			
			if((tileAngle >= 0) && ((GameState.activeMapTile.exits[(tileAngle + GameState.activeMapTile.getRotation() + 2) % 4]) == MapTiles.STANDARD_EXIT) || ((GameState.activeMapTile.exits[(tileAngle + GameState.activeMapTile.getRotation() + 2) % 4]) == MapTiles.SPECIAL_EXIT)) {
				imageView.setImageBitmap(b);
				//imageView.setImageDrawable(possiblePlace);
			} else {
				imageView.setImageDrawable(blank);
			}*/
			//Log.e("asd", "Showing the blank");

			imageView.setImageDrawable(blank);

		}
		// Bitmap bitmap =
		// BitmapFactory.decodeResource(ZimpAActivity.this.getResources(),
		// R.drawable.bedroom);
		// imageView.setImageBitmap(bitmap);

		return imageView;
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}

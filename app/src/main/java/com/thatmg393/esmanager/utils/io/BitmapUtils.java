package com.thatmg393.esmanager.utils.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.thatmg393.esmanager.utils.ActivityUtils;

import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {
	public static final int[] DEFAULT_RESIZE_KEEPASPECT = BitmapUtils.resizeKeepAspect(1920, 1080, 128, 128);
			
	public static Bitmap getRescaledDrawable(@DrawableRes int drawableId) {
		Drawable drawable = ContextCompat.getDrawable(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), drawableId);
		
		int[] resized = resizeKeepAspect(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), 128, 128);
		Bitmap rescaledBitmap = Bitmap.createBitmap(resized[0], resized[1], Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(rescaledBitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return rescaledBitmap;
	}
	
	public static Bitmap getRescaledBitmap(@NonNull Uri path) throws IOException {
		InputStream imageIS = ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext().getContentResolver().openInputStream(path);
		Bitmap originalBitmap = BitmapFactory.decodeStream(imageIS);
		imageIS.close();
		
		Bitmap rescaledBitmap = resize(originalBitmap, 128, 128);
		originalBitmap.recycle();
		
		return rescaledBitmap;
	}

	public static Bitmap resize(@NonNull Bitmap image, @NonNull int maxWidth, @NonNull int maxHeight) {
		if (maxHeight > 0 && maxWidth > 0) {
	  	  int[] resized = resizeKeepAspect(image.getWidth(), image.getHeight(), maxWidth, maxHeight);
			
			image = Bitmap.createScaledBitmap(image, resized[0], resized[1], true);
			return image;
  	  } else {
			return image;
  	  }
	}
	
	public static int[] resizeKeepAspect(
		@NonNull int width,
		@NonNull int height,
		@NonNull int maxWidth,
		@NonNull int maxHeight
	) {
		if (maxHeight > 0 && maxWidth > 0) {
			float ratioBitmap = (float) width / (float) height;
			float ratioMax = (float) maxWidth / (float) maxHeight;
			
			int finalWidth = maxWidth;
			int finalHeight = maxHeight;
			if (ratioMax > ratioBitmap) {
				finalWidth = (int) ((float)maxHeight * ratioBitmap);
			} else {
				finalHeight = (int) ((float)maxWidth / ratioBitmap);
			}
			return new int[] {finalWidth, finalHeight};
  	  } else {
			return new int[] {maxWidth, maxHeight};
		}
	}
}

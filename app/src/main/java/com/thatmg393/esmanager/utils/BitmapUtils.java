package com.thatmg393.esmanager.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.imageview.ShapeableImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class BitmapUtils {
	public static Bitmap getRescaledDrawable(@DrawableRes int drawableId) {
   	 Drawable drawable = ContextCompat.getDrawable(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), drawableId);
  	  Bitmap originalBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
        	    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Bitmap rescaledBitmap = resize(originalBitmap, 128, 128);
		
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
      	  int width = image.getWidth();
      	  int height = image.getHeight();
     	   float ratioBitmap = (float) width / (float) height;
     	   float ratioMax = (float) maxWidth / (float) maxHeight;

      	  int finalWidth = maxWidth;
     	   int finalHeight = maxHeight;
     	   if (ratioMax > ratioBitmap) {
      	      finalWidth = (int) ((float)maxHeight * ratioBitmap);
    	    } else {
       	     finalHeight = (int) ((float)maxWidth / ratioBitmap);
      	  }
    	    image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
     	   return image;
  	  } else {
       	 return image;
  	  }
	}
	
	public static void loadBitmapAsync(@NonNull ShapeableImageView imgView, Uri path) {
		CompletableFuture.runAsync(() -> {
			try {
				Bitmap b = getRescaledBitmap(path);
				if (b != null) {
					imgView.post(() -> imgView.setImageBitmap(b));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void loadDrawableAsync(@NonNull ShapeableImageView imgView, @DrawableRes int drawableRes) {
		CompletableFuture.runAsync(() -> {
			Bitmap d = getRescaledDrawable(drawableRes);
			if (d != null) {
				imgView.post(() -> imgView.setImageBitmap(d));
			}
		});
	}
}

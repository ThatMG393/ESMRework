package com.thatmg393.esmanager.fragments.project.editor;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.project.base.PathedTabFragment;

public class TabPictureFragment extends PathedTabFragment {
	private SubsamplingScaleImageView rootView;
	
	public TabPictureFragment(final String pathToFile) {
		super(pathToFile);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = new SubsamplingScaleImageView(requireActivity());
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Picasso.get()
			.load(getCurrentFile())
			.into(new Target() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
					rootView.setImage(ImageSource.bitmap(bitmap));
				}
				
				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) { }
				@Override
				public void onBitmapFailed(Exception error, Drawable errorDrawable) { }
			});
	}
	
	
	@Override
	public String toString() {
		return this.getClass().getName() + " for " + getCurrentFilePath();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		rootView = null;
	}
}

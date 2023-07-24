package com.thatmg393.esmanager.fragments.main;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.google.android.material.textview.MaterialTextView;

public class HomeFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MaterialTextView tv = new MaterialTextView(requireActivity());
		tv.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		tv.setText("Home Fragment\n(pretend its designed)");
		
		return tv;
	}
}

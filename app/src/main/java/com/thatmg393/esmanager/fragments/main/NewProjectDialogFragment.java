package com.thatmg393.esmanager.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.anggrayudi.storage.file.MimeType;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.BitmapUtils;
import com.thatmg393.esmanager.utils.StorageUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

public class NewProjectDialogFragment extends AppCompatDialogFragment {
	public static final String TAG = "NewProjectDialogFragment";
	
	private ProjectsFragment projectsFragment;
	private Toolbar newProjectToolbar;
	private ShapeableImageView newProjectPreview;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_project_dialog, parent, false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(STYLE_NO_FRAME, R.style.App_MainTheme);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		requireDialog().getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
		
		projectsFragment = (ProjectsFragment) getParentFragmentManager().findFragmentByTag(ProjectsFragment.TAG);
		newProjectToolbar = requireView().findViewById(R.id.new_project_toolbar);
		newProjectToolbar.setTitle("Create New Project");
		newProjectToolbar.inflateMenu(R.menu.main_new_project_menu);
		newProjectToolbar.setOnMenuItemClickListener((item) -> {
			createProject();
			return true;
		});
		newProjectToolbar.setNavigationOnClickListener((v) -> {
			dismiss();
		});
		
		newProjectPreview = requireView().findViewById(R.id.new_project_preview);
		newProjectPreview.setOnClickListener((v) -> {
			StorageUtils.pickFile(MimeType.IMAGE, (path) -> {
				Picasso.get()
					.load(path)
					.resize(BitmapUtils.DEFAULT_RESIZE_KEEPASPECT[0], BitmapUtils.DEFAULT_RESIZE_KEEPASPECT[1])
					.into(newProjectPreview);
			});
		});
		
		RelativeLayout advancedLayout = requireView().findViewById(R.id.new_project_advanced_layout);
		ShapeableImageView advanceLayoutArrow = advancedLayout.findViewById(R.id.new_project_advanced_arrow);
		ExpandableLayout advancedExpandableLayout = requireView().findViewById(R.id.new_project_advanced_expand_layout);
		
		advancedLayout.setOnClickListener((v) -> {
			advancedExpandableLayout.toggle();
			if (advancedExpandableLayout.isExpanded()) {
				RotateAnimation rotateCW = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				rotateCW.setInterpolator(new AccelerateDecelerateInterpolator());
				rotateCW.setFillAfter(true);
				rotateCW.setDuration(400);
				
				advanceLayoutArrow.startAnimation(rotateCW);
			} else {
				RotateAnimation rotateCCW = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				rotateCCW.setInterpolator(new AccelerateDecelerateInterpolator());
				rotateCCW.setFillAfter(true);
				rotateCCW.setDuration(400);
				
				advanceLayoutArrow.startAnimation(rotateCCW);
			}
		});
	}
	
	public void show(FragmentManager manager) {
		if (manager.findFragmentByTag(TAG) == null) {
			show(manager, TAG);
		}
	}
	
	private void createProject() {
		// TODO: Implement.
		ActivityUtils.getInstance().showToast("Function not implemented", Toast.LENGTH_SHORT);
		
		dismiss();
		projectsFragment.populateProjectList();
	}
}

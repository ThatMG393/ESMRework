package com.thatmg393.esmanager.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;

import io.github.rosemoe.sora.widget.SymbolInputView;

import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

public class TabEditorAdapter extends FragmentStateAdapter {
	public ArrayList<TabEditorFragment> fragments = new ArrayList<TabEditorFragment>();
	
	private RelativeLayout noEditorContainer;
	private TabLayout tabLayout;
	private ViewPager2 viewPager;
	private LinearLayout symInputContainer;
	
	public TabEditorAdapter(Lifecycle lifecycle, FragmentManager fragmentManager, View projectLayout) {
		super(fragmentManager, lifecycle);
		this.tabLayout = projectLayout.findViewById(R.id.project_editors_tab);
		this.viewPager = projectLayout.findViewById(R.id.project_editor_view);
		this.noEditorContainer = projectLayout.findViewById(R.id.project_no_editor_container);
		this.symInputContainer = projectLayout.findViewById(R.id.project_symbol_input_container);
		
		tabLayout.setVisibility(View.GONE);
		viewPager.setVisibility(View.GONE);
		symInputContainer.setVisibility(View.GONE);
		animateViewsIfNeeded();
	}
	
	public void newTab(String path) {
		if (checkTabAlreadyInList(path)) return;
		
		TabEditorFragment fragment = new TabEditorFragment(tabLayout.getContext(), path);
		
		fragments.add(fragment);
		notifyDataSetChanged();
		
		TabLayout.Tab tab = tabLayout.newTab();
		tab.setText(FilenameUtils.getName(path));
		tabLayout.addTab(tab);
		
		animateViewsIfNeeded();
	}
	
	public void removeTab(int position) {
		if (position < 0 || position > fragments.size()) return;
		
		fragments.remove(position);
		tabLayout.removeTabAt(position);
		
		notifyDataSetChanged();
		animateViewsIfNeeded();
	}
	
	
	public boolean checkTabAlreadyInList(String s) {
		for (TabEditorFragment fragment : fragments) {
			if (fragment.currentFilePath.equals(s)) return true;
		}
		return false;
	}
	
	public void animateViewsIfNeeded() {
		if (tabLayout.getTabCount() == 0
		 && tabLayout.getVisibility() == View.VISIBLE
		 && viewPager.getVisibility() == View.VISIBLE
		 && noEditorContainer.getVisibility() == View.GONE) {
			tabLayout.animate()
				.translationY(-tabLayout.getHeight())
				.setDuration(200)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						tabLayout.setVisibility(View.GONE);
					}
				});
			
			viewPager.animate()
				.translationX(-viewPager.getWidth())
				.setDuration(130)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						viewPager.setVisibility(View.GONE);
					}
				});
			symInputContainer.setVisibility(View.GONE);
			
			noEditorContainer.setVisibility(View.VISIBLE);
			noEditorContainer.animate()
				.translationY(0)
				.setDuration(180)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(null);
		} else if (tabLayout.getTabCount() > 0
				&& tabLayout.getVisibility() == View.GONE
				&& viewPager.getVisibility() == View.GONE
				&& noEditorContainer.getVisibility() == View.VISIBLE) {
			noEditorContainer.animate()
				.translationY(noEditorContainer.getHeight())
				.setDuration(180)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						noEditorContainer.setVisibility(View.GONE);
					}
				});
			
			viewPager.setVisibility(View.VISIBLE);
			viewPager.animate()
				.translationX(0)
				.setDuration(100)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(null);
			symInputContainer.setVisibility(View.VISIBLE);
			
			tabLayout.setVisibility(View.VISIBLE);
			tabLayout.animate()
				.translationY(0)
				.setDuration(200)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(null);
		}
	}
	
    @Override
    public int getItemCount() {
		return fragments.size();
	}

    @Override
    public Fragment createFragment(int position) {
		return fragments.get(position);
	}
	
	public ArrayList<TabEditorFragment> getFragmentList() {
		return this.fragments;
	}
}

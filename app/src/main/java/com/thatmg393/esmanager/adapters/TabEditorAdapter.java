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
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;

public class TabEditorAdapter extends FragmentStateAdapter {
	public ArrayList<TabEditorFragment> fragments = new ArrayList<>();
	
	private RelativeLayout noEditorContainer;
	public final TabLayout tabLayout;
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
		TabEditorFragment fragment = new TabEditorFragment(path);
		fragments.add(fragment);
		
		TabLayout.Tab tab = tabLayout.newTab();
		tab.setText(FilenameUtils.getName(path));
		tabLayout.addTab(tab);
		
		fragment.initEditor(tabLayout.getContext());
		
		notifyDataSetChanged();
		callOnNewTab(tab, fragment);
		animateViewsIfNeeded();
	}
	
	public void removeTab(int position) {
		fragments.remove(position);
		tabLayout.removeTabAt(position);
		
		notifyDataSetChanged();
		callOnRemoveTab(position);
		animateViewsIfNeeded();
	}
	
	public boolean checkTabAlreadyInList(String s) {
		for (int idx = 0; idx < fragments.size(); ++idx) {
			if (fragments.get(idx).getCurrentFilePath().equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	public int getIndexOfFragment(String path) {
		for (int idx = 0; idx < fragments.size(); ++idx) {
			if (fragments.get(idx).getCurrentFilePath().equals(path)) {
				return idx;
			}
		}
		return 0;
	}
	
	public void animateViewsIfNeeded() {
		if (tabLayout.getTabCount() == 0
		 && tabLayout.getVisibility() == View.VISIBLE
		 && viewPager.getVisibility() == View.VISIBLE
		 && noEditorContainer.getVisibility() == View.GONE) {
			tabLayout.animate()
				.translationY(-tabLayout.getHeight())
				.setDuration(300)
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
				.setDuration(200)
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
				.setDuration(140)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(null);
		} else if (tabLayout.getTabCount() > 0
				&& tabLayout.getVisibility() == View.GONE
				&& viewPager.getVisibility() == View.GONE
				&& noEditorContainer.getVisibility() == View.VISIBLE) {
			noEditorContainer.animate()
				.translationY(noEditorContainer.getHeight())
				.setDuration(140)
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
				.setDuration(200)
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
	
	private final ArrayList<OnTabUpdateListener> tabUpdateListener = new ArrayList<>();
	public void addOnTabUpdateListener(OnTabUpdateListener listener) {
		tabUpdateListener.add(listener);
	}
	public void removeOnTabUpdateListener(OnTabUpdateListener listener) {
		tabUpdateListener.remove(listener);
	}
	
	private void callOnNewTab(TabLayout.Tab tab, TabEditorFragment fragment) {
		tabUpdateListener.forEach((listener) -> listener.onNewTab(tab, fragment));
	}
	private void callOnRemoveTab(int position) {
		tabUpdateListener.forEach((listener) -> listener.onRemoveTab(position));
	}
	
	public static interface OnTabUpdateListener {
		public default void onNewTab(TabLayout.Tab tab, TabEditorFragment fragment) { }
		public default void onRemoveTab(int position) { }
	}
}

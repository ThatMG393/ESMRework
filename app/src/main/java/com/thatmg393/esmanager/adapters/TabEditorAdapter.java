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
	
	private FragmentManager fragmentManager;
	
	private RelativeLayout noEditorContainer;
	public final TabLayout tabLayout;
	private ViewPager2 viewPager;
	private LinearLayout symInputContainer;
	
	public TabEditorAdapter(Lifecycle lifecycle, FragmentManager fragmentManager, View projectLayout) {
		super(fragmentManager, lifecycle);
		this.fragmentManager = fragmentManager;
		
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
		notifyDataSetChanged();
		
		TabLayout.Tab tab = tabLayout.newTab();
		tab.setText(FilenameUtils.getName(path));
		tabLayout.addTab(tab);
		
		fragment.initEditor(tabLayout.getContext());
		
		dispatchOnNewTab(tab, fragment);
		animateViewsIfNeeded();
	}
	
	public void removeTab(int position) {
		tabLayout.removeTabAt(position);
		fragments.remove(position);
		notifyDataSetChanged();
		
		// Bug is when removing a tab at pos 0, the viewpager doesnt update propeely
		// and desyncing our tabs, fragments, viewpager.
		// Only happens on pos 0 and getItemCount() > 0
		if (position == 0 && getItemCount() > 0) viewPager.setAdapter(this);
		// Also happens on pos 0 and getItemCount() == 0
		else if (position == 0 && getItemCount() == 0) viewPager.setAdapter(this);
		
		dispatchOnRemoveTab(position);
		animateViewsIfNeeded();
	}
	
	public int getIndexOfFragment(String path) {
		int i = 0;
		for (TabEditorFragment fragment : fragments) {
			if (fragment.getCurrentFilePath().equals(path)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public void animateViewsIfNeeded() {
		if (tabLayout.getTabCount() == 0
		&& tabLayout.getVisibility() == View.VISIBLE
		&& viewPager.getVisibility() == View.VISIBLE
		&& noEditorContainer.getVisibility() == View.GONE) {
			viewPager.setVisibility(View.GONE);
			symInputContainer.setVisibility(View.GONE);
			tabLayout.setVisibility(View.GONE);
			noEditorContainer.setVisibility(View.VISIBLE);
		} else if (tabLayout.getTabCount() > 0
		&& tabLayout.getVisibility() == View.GONE
		&& viewPager.getVisibility() == View.GONE
		&& noEditorContainer.getVisibility() == View.VISIBLE) {
			noEditorContainer.setVisibility(View.GONE);
			tabLayout.setVisibility(View.VISIBLE);
			viewPager.setVisibility(View.VISIBLE);
			symInputContainer.setVisibility(View.VISIBLE);
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
	
	private void dispatchOnNewTab(TabLayout.Tab tab, TabEditorFragment fragment) {
		tabUpdateListener.forEach((listener) -> listener.onNewTab(tab, fragment));
	}
	private void dispatchOnRemoveTab(int position) {
		tabUpdateListener.forEach((listener) -> listener.onRemoveTab(position));
	}
	
	public static interface OnTabUpdateListener {
		public default void onNewTab(TabLayout.Tab tab, TabEditorFragment fragment) { }
		public default void onRemoveTab(int position) { } 
	}
}

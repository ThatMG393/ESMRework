package com.thatmg393.esmanager.adapters;

import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;

import com.thatmg393.esmanager.utils.ActivityUtils;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;

public class TabEditorAdapter extends FragmentStateAdapter {
	public ArrayList<TabEditorFragment> fragments = new ArrayList<>();
	
	private final RelativeLayout noEditorContainer;
	private final TabLayout tabLayout;
	private final ViewPager2 viewPager;
	private final HorizontalScrollView symInputContainer;
	
	public TabEditorAdapter(Lifecycle lifecycle, FragmentManager fragmentManager, View projectLayout) {
		super(fragmentManager, lifecycle);
		
		this.tabLayout = projectLayout.findViewById(R.id.project_editors_tab);
		this.viewPager = projectLayout.findViewById(R.id.project_editor_view);
		this.noEditorContainer = projectLayout.findViewById(R.id.project_no_editor_container);
		this.symInputContainer = projectLayout.findViewById(R.id.project_symbol_input_container);
		
		tabLayout.setVisibility(View.GONE);
		viewPager.setVisibility(View.GONE);
		symInputContainer.setVisibility(View.GONE);
		updateViewsIfNeeded();
	}
	
	public void newTab(String path) {
		if (getItemCount() <= 0) newTabInternal(path);
		else {
			TabEditorFragment fragmentInTab = null;
			for (TabEditorFragment fragment : fragments) {
				if (fragment.getCurrentFilePath() == path) {
					fragmentInTab = fragment;
					break;
				}
			}
			
			if (fragmentInTab != null) {
				TabLayout.Tab fragmentTab = fragmentInTab.getCurrentTab();
				if (fragmentTab.getPosition() != tabLayout.getSelectedTabPosition()) {
					fragmentTab.select();
				}
			} else newTabInternal(path);
		}
	}
	
	public void removeTab(int position) {
		TabEditorFragment fragment = fragments.get(position);
		if (fragment.isFileModified()) {
			ActivityUtils.getInstance().createAlertDialog(
				"Unsaved file",
				"Would you like to save: " + fragment.getCurrentFilePath() + "?",
				new Pair<>("No", (dialog, which) -> {
					dialog.dismiss();
					removeTabInternal(position);
				}),
				new Pair<>("Yes", (dialog, which) -> {
					dialog.dismiss();
					fragment.save();
					removeTabInternal(position);
				})
			).show();
		} else {
			removeTabInternal(position);
		}
	}
	
	private void newTabInternal(String path) {
		TabEditorFragment fragment = new TabEditorFragment(path);
		
		fragments.add(fragment);
		notifyItemInserted(fragments.size());
		
		TabLayout.Tab tab = tabLayout.newTab();
		tab.setText(FilenameUtils.getName(path));
		tabLayout.addTab(tab);
		
		if (tab.getPosition() != tabLayout.getSelectedTabPosition()) {
			tab.select();
		}
		
		fragment.setCurrentTabObject(tab);
		
		dispatchOnNewTab(tab, fragment);
		updateViewsIfNeeded();
	}
	
	private void removeTabInternal(int position) {
		tabLayout.removeTabAt(position);
		fragments.remove(position);
		notifyItemRemoved(position);
		
		// Bug is when removing a tab at pos 0, the viewpager doesnt update properly
		// and desyncing our tabs, fragments, and viewpager.
		// Only happens on pos 0 and getItemCount() > 0
		if (position == 0 && getItemCount() > 0) viewPager.setAdapter(this);
		// Also happens on pos 0 and getItemCount() == 0
		else if (position == 0 && getItemCount() == 0) viewPager.setAdapter(this);
		
		dispatchOnRemoveTab(position);
		updateViewsIfNeeded();
	}
	
	public void updateViewsIfNeeded() {
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

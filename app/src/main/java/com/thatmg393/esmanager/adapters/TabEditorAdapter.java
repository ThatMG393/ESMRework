package com.thatmg393.esmanager.adapters;

import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import android.widget.Toast;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.fragments.project.base.BaseTabFragment;
import com.thatmg393.esmanager.fragments.project.base.PathedTabFragment;
import com.thatmg393.esmanager.fragments.project.editor.TabPictureFragment;
import com.thatmg393.esmanager.interfaces.IOnTabUpdateListener;
import com.thatmg393.esmanager.models.TabModel;
import com.thatmg393.esmanager.utils.ActivityUtils;

import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

public class TabEditorAdapter extends FragmentStateAdapter {
	private ArrayList<TabModel> fragments = new ArrayList<>();
	
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
	
	public void newTab(String path, TabType type) {
		if (getItemCount() <= 0) newTabInternal(path, type);
		else {
			PathedTabFragment fragmentInTab = null;
			for (TabModel model : fragments) {
				if (model.fragment.getCurrentFilePath() == path) {
					fragmentInTab = model.fragment;
				}
			}
			
			if (fragmentInTab != null) {
				TabLayout.Tab fragmentTab = fragmentInTab.getCurrentTab();
				
				if (fragmentTab != null) {
					if (fragmentTab.getPosition() != tabLayout.getSelectedTabPosition()) fragmentTab.select();
				}
			} else newTabInternal(path, type);
		}
	}
	
	public void removeTab(int position) {
		PathedTabFragment fragment = fragments.get(position).fragment;
		
		if (fragment instanceof TabEditorFragment) {
			TabEditorFragment fragmentCasted = (TabEditorFragment) fragment;
			if (fragmentCasted.getEditorState() == TabEditorFragment.EditorState.MODIFIED) {
				ActivityUtils.getInstance().createAlertDialog(
					"Unsaved file",
					"Would you like to save: " + fragmentCasted.getCurrentFilePath() + "?",
					new Pair<>("No", (dialog, which) -> {
						dialog.dismiss();
						removeTabInternal(position);
					}),
					new Pair<>("Yes", (dialog, which) -> {
						dialog.dismiss();
						fragmentCasted.save();
						removeTabInternal(position);
					})
				).show();
			} else {
				removeTabInternal(position);
			}
		} else {
			removeTabInternal(position);
		}
	}
	
	private void newTabInternal(String path, TabType type) {
		PathedTabFragment fragment = null;
		
		switch (type.getType()) {
			case 0:
				fragment = new TabEditorFragment(path);
				break;
			case 1:
				fragment = new TabPictureFragment(path);
				break;
		}
		
		TabLayout.Tab tab = tabLayout.newTab();
		tab.setText(FilenameUtils.getName(path));
		
		fragments.add(new TabModel(
			tab, fragment
		));
		tabLayout.addTab(tab);
		
		notifyItemInserted(fragments.size());
		fragment.setCurrentTabObject(tab);
		
		if (tab.getPosition() != tabLayout.getSelectedTabPosition()) {
			tab.select();
		}
		
		dispatchOnNewTab(tab, fragment);
		updateViewsIfNeeded();
	}
	
	private void removeTabInternal(int position) {
		fragments.remove(position);
		tabLayout.removeTabAt(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, getItemCount());
		
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
	public long getItemId(int position) {
		return fragments.get(position).itemId;
	}
	
	@Override
	public boolean containsItem(long itemId) {
		boolean found = false;
		for (TabModel model : fragments) {
			if (model.itemId == itemId) {
				found = true;
				break;
			}
		}
		
		return found;
	}
	
	@Override
	public Fragment createFragment(int position) {
		return fragments.get(position).fragment;
	}
	
	public ArrayList<TabModel> getFragmentList() {
		return this.fragments;
	}
	
	private final ArrayList<IOnTabUpdateListener> tabUpdateListener = new ArrayList<>();
	public void addOnTabUpdateListener(IOnTabUpdateListener listener) {
		tabUpdateListener.add(listener);
	}
	public void removeOnTabUpdateListener(IOnTabUpdateListener listener) {
		tabUpdateListener.remove(listener);
	}
	
	private void dispatchOnNewTab(TabLayout.Tab tab, BaseTabFragment fragment) {
		tabUpdateListener.forEach((listener) -> listener.onNewTab(tab, fragment));
	}
	private void dispatchOnRemoveTab(int position) {
		tabUpdateListener.forEach((listener) -> listener.onRemoveTab(position));
	}
	
	public enum TabType {
		EDITOR(0),
		PICTURE(1);
		
		private final int type;
		
		private TabType(int type) {
			this.type = type;
		}
		
		public final int getType() {
			return this.type;
		}
	}
}

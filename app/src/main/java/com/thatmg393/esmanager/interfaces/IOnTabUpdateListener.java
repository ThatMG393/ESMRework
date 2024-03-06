package com.thatmg393.esmanager.interfaces;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.fragments.project.base.BaseTabFragment;

public interface IOnTabUpdateListener {
	public default void onNewTab(TabLayout.Tab tab, BaseTabFragment fragment) { }
	public default void onRemoveTab(int position) { }
}

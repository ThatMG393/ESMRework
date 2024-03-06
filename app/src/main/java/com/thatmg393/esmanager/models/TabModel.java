package com.thatmg393.esmanager.models;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.fragments.project.base.PathedTabFragment;

public class TabModel {
    public final TabLayout.Tab tab;
    public final PathedTabFragment fragment;
	
	public final String fullPath;
    public final long itemId;

    public TabModel(
		@NonNull TabLayout.Tab tab, 
		@NonNull PathedTabFragment fragment
	) {
        this.tab = tab;
        this.fragment = fragment;
		this.fullPath = fragment.getCurrentFilePath();
        this.itemId = new Long(fragment.hashCode());
    }
}

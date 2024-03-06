package com.thatmg393.esmanager.fragments.project.base;

import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout.Tab;

public class BaseTabFragment extends Fragment {
	private Tab currentTab;
	
	/* internal */
	public void setCurrentTabObject(Tab currentTab) {
		if (this.currentTab != null) return;
		this.currentTab = currentTab;
	}
	
	public Tab getCurrentTab() {
		return this.currentTab;
	}
}

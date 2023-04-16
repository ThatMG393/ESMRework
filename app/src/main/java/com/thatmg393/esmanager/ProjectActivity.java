package com.thatmg393.esmanager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.fragments.projectfragments.ProjectTabEditorFragment;
import com.thatmg393.esmanager.managers.LSPManager;

import io.github.rosemoe.sora.lsp.editor.LspEditorManager;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
	private static String projectPath = null;
	public static String getProjectPath() { return projectPath; }
	
	private TabLayout editorTabLayout;
	private ViewPager2 editorViewPager;
	private ProjectEditorViewPager editorViewPagerAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		projectPath = GlobalConstants.ESM_ROOT_FOLDER + "/Roblox AFS Script";
		super.onCreate(savedInstanceState);
    }
	
	@Override
	public void init() {
		super.init();
		setContentView(R.layout.activity_project);
		
		LSPManager.getInstance().startLSPForAll();
		
		editorViewPagerAdapter = new ProjectEditorViewPager(getSupportFragmentManager(), getLifecycle());
		
		editorViewPager = findViewById(R.id.project_editor_view);
		editorViewPager.setUserInputEnabled(false);
		editorViewPager.setAdapter(editorViewPagerAdapter);
		
		editorTabLayout = findViewById(R.id.project_editors_tab);
		editorTabLayout.addOnTabSelectedListener(this);
		
		newEditorView(projectPath + "/MGUI.lua");
		// newEditorView(projectPath + "/MGUI.lua");
	}
	
	public final void newEditorView(String filePath) {
		ProjectTabEditorFragment codeEditorFragment = new ProjectTabEditorFragment();
		codeEditorFragment.initializeEditor(getApplicationContext(), filePath);
		
		editorViewPagerAdapter.addFragment(codeEditorFragment);
		editorTabLayout.addTab(editorTabLayout.newTab().setText(FilenameUtils.getName(filePath)));
	}
	
	public void removeTab(int position) {
		if (editorTabLayout.getTabCount() >= 1 && position < editorTabLayout.getTabCount()) {
			editorTabLayout.removeTabAt(position);
			editorViewPagerAdapter.removeFragment(position);
		}
	}
	
	@Override
    public void onTabSelected(TabLayout.Tab tab) {
        editorViewPager.setCurrentItem(tab.getPosition());
    }
	
	private class ProjectEditorViewPager extends FragmentStateAdapter {
		private List<ProjectTabEditorFragment> lFrag = new ArrayList<ProjectTabEditorFragment>();
		
		public ProjectEditorViewPager(FragmentManager fManager, Lifecycle curLifecyle) {
        	super(fManager, curLifecyle);
    	}
		
		public void addFragment(ProjectTabEditorFragment ptef) {
			lFrag.add(ptef);
		}
		
		public void removeFragment(int position) {
			lFrag.remove(position);
		}
		
    	@Override
    	public Fragment createFragment(int position) {
			try {
				return lFrag.get(position);
			} catch (IndexOutOfBoundsException ignore) { }
			
			// Maybe create manually if null??
			return null;
    	}

   	 @Override
    	public int getItemCount() {
       	 return lFrag.size();
    	}
	}
	
	@Override
    public void onTabReselected(TabLayout.Tab tab) { }
	
	@Override
    public void onTabUnselected(TabLayout.Tab tab) { }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		_destroy();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		_destroy();
	}
	
	private void _destroy() {
		editorViewPagerAdapter.lFrag.clear();
		editorViewPager.removeAllViews();
		LspEditorManager.closeAllManager();
		LSPManager.getInstance().stopLSPServices();
	}
}
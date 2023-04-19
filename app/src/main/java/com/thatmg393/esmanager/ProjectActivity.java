package com.thatmg393.esmanager;

import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.fragments.projectfragments.ProjectFileTreeViewFragment;
import com.thatmg393.esmanager.fragments.projectfragments.ProjectTabEditorFragment;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.utils.FileUtils;

import io.github.rosemoe.sora.widget.SymbolInputView;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
	private static String projectPath = null;
	public static String getProjectPath() { return projectPath; }
	
	private DrawerLayout editorDrawerLayout;
	private Toolbar projectToolbar;
	private TabLayout editorTabLayout;
	private ViewPager2 editorViewPager;
	private NavigationView editorFileDrawer;
	
	private ProjectEditorViewPager editorViewPagerAdapter;
	private ProjectFileTreeViewFragment editorFileTreeViewFragment = new ProjectFileTreeViewFragment();
	
	private Menu optionMenu;
	
	private ArrayMap<String, Boolean> openedFilesPath = new ArrayMap<>();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		projectPath = GlobalConstants.ESM_ROOT_FOLDER + "/Roblox AFS Script";
		super.onCreate(savedInstanceState);
		toggleViews(true);
		
		SymbolInputView symbolInput = findViewById(R.id.project_symbol_input);
		symbolInput.addSymbols(
			new String[] {
				"->", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/"
			}, new String[] {
				"\t", "{ }", "}", "()", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/"
			}
		);
    }
	
	@Override
	public void init() {
		super.init();
		setContentView(R.layout.activity_project);
		LSPManager.getInstance().startLSPForAll();
		
		projectToolbar = findViewById(R.id.project_toolbar);
		setSupportActionBar(projectToolbar);
		
		editorDrawerLayout = findViewById(R.id.project_drawer_layout);
		ActionBarDrawerToggle drawerToggler = new ActionBarDrawerToggle(this, editorDrawerLayout, projectToolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
		editorDrawerLayout.addDrawerListener(drawerToggler);
		drawerToggler.syncState();
		
		editorFileDrawer = findViewById(R.id.project_file_drawer);
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.project_file_drawer_fragment, editorFileTreeViewFragment)
			.commit();
		
		editorViewPagerAdapter = new ProjectEditorViewPager(getSupportFragmentManager(), getLifecycle());
		
		editorViewPager = findViewById(R.id.project_editor_view);
		editorViewPager.setUserInputEnabled(false);
		editorViewPager.setAdapter(editorViewPagerAdapter);
		
		editorTabLayout = findViewById(R.id.project_editors_tab);
		editorTabLayout.addOnTabSelectedListener(this);
		
		editorFileTreeViewFragment.addListener((path) -> { newEditorView(path); });
	}
	
	public final void newEditorView(final String filePath) {
		if (openedFilesPath.get(filePath) != null && openedFilesPath.get(filePath)) {
			int position = 0;
			for (int index = 0; index < editorViewPagerAdapter.lFrag.size(); index++) {
				if (editorViewPagerAdapter.lFrag.get(index).currentFilePath == filePath) {
					position = index;
				}
			}
			
			if (editorTabLayout.getSelectedTabPosition() != position) {
				editorTabLayout.getTabAt(position).select();
				editorViewPager.setCurrentItem(position);
			}
			return;
		}
		
		ProjectTabEditorFragment codeEditorFragment = new ProjectTabEditorFragment();
		codeEditorFragment.initializeEditor(getApplicationContext(), filePath);
		
		editorViewPagerAdapter.addFragment(codeEditorFragment);
		editorTabLayout.addTab(editorTabLayout.newTab().setText(FilenameUtils.getName(filePath)));
		editorViewPager.setCurrentItem(editorTabLayout.getTabCount());
		
		openedFilesPath.put(filePath, true);
		toggleViews(false);
	}
	
	public void removeEditorView(int position) {
		if (editorTabLayout.getTabCount() == 0) return;

		openedFilesPath.put(editorViewPagerAdapter.lFrag.get(position).currentFilePath, false);
		if (editorTabLayout.getTabCount() == 1) {
			editorTabLayout.removeTabAt(position);
			editorViewPagerAdapter.removeFragment(position);
			
			toggleViews(true);
		} else if (editorTabLayout.getTabCount() > 1) {
			editorTabLayout.removeTabAt(position);
			editorViewPagerAdapter.removeFragment(position);
			editorViewPager.setCurrentItem(position);
		}
	}
	
	@Override
    public void onTabReselected(TabLayout.Tab tab) {
		PopupMenu popupMenu = new PopupMenu(getApplicationContext(), ((ViewGroup) editorTabLayout.getChildAt(0)).getChildAt(tab.getPosition()));
		popupMenu.inflate(R.menu.editor_tab_menu);
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
					case R.id.editor_close_tab:
						removeEditorView(tab.getPosition());
						return true;
				}
				return false;
			}
		});
		popupMenu.show();
	}
	
	@Override
    public void onTabSelected(TabLayout.Tab tab) {
        editorViewPager.setCurrentItem(tab.getPosition());
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.project_action_menu, menu);
		optionMenu = menu;
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.project_save_file:
				if (editorTabLayout.getTabCount() == 0) return true;
				
				ProjectTabEditorFragment tmpPtef = editorViewPagerAdapter.createFragment(editorTabLayout.getSelectedTabPosition());
				if (FileUtils.writeToFileUsingContent(tmpPtef.editor.getText(), tmpPtef.currentFilePath)) {
					Toast.makeText(getApplicationContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed to save file!", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.project_save_all_file:
				if (editorTabLayout.getTabCount() == 0) return true;
				
				editorViewPagerAdapter.lFrag.forEach((fragment) -> {
					if (!FileUtils.writeToFileUsingContent(fragment.editor.getText(), fragment.currentFilePath)) {
						Toast.makeText(getApplicationContext(), "Failed to save " + fragment.currentFilePath, Toast.LENGTH_SHORT).show();
					}
				});
				Toast.makeText(getApplicationContext(), "All files saved!", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.project_import_obj:
				Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
				return true;
		}
		
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		_destroy();
	}
	
	@Override
	public void onBackPressed() {
		if (editorDrawerLayout.isDrawerOpen(GravityCompat.END)) {
			editorDrawerLayout.closeDrawer(GravityCompat.END);
		} /* else if () {
			
		} */ else { 
			_destroy();
			super.onBackPressed();
		}
	}
	
	private void _destroy() {
		if (isChangingConfigurations()) return;
		
		try {
			getSupportFragmentManager().beginTransaction()
				.remove(editorFileTreeViewFragment)
				.commit();
		} catch (RuntimeException ignore) { }
		
		editorViewPagerAdapter.removeAllFragments();
		LSPManager.getInstance().stopLSPServices();
	}
	
	private void toggleViews(boolean noFragmentLeft) {
		if (noFragmentLeft) {
			findViewById(R.id.project_no_editor_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.project_symbol_input_container).setVisibility(View.GONE);
			
			editorTabLayout.setVisibility(View.GONE);
			editorViewPager.setVisibility(View.GONE);
			
			if (optionMenu != null) {
				optionMenu.findItem(R.id.project_save_file).setEnabled(false);
				optionMenu.findItem(R.id.project_save_all_file).setEnabled(false);
			}
		} else {
			findViewById(R.id.project_no_editor_layout).setVisibility(View.GONE);
			findViewById(R.id.project_symbol_input_container).setVisibility(View.VISIBLE);
			
			editorTabLayout.setVisibility(View.VISIBLE);
			editorViewPager.setVisibility(View.VISIBLE);
			
			if (optionMenu != null) {
				optionMenu.findItem(R.id.project_save_file).setEnabled(true);
				optionMenu.findItem(R.id.project_save_all_file).setEnabled(true);
			}
		}
	}
	
	@Override
    public void onTabUnselected(TabLayout.Tab tab) { }
	
	
	private class ProjectEditorViewPager extends FragmentStateAdapter {
		private List<ProjectTabEditorFragment> lFrag = new ArrayList<>();
		public ProjectEditorViewPager(FragmentManager fManager, Lifecycle curLifecyle) {
        	super(fManager, curLifecyle);
    	}
		
		public void addFragment(ProjectTabEditorFragment ptef) {
			lFrag.add(ptef);
		}
		public void removeFragment(int position) {
			getSupportFragmentManager().beginTransaction().remove(lFrag.get(position)).commit();
			lFrag.remove(position);
		}
		
		public void removeAllFragments() {
			if (lFrag.size() == 0) return;
			
			lFrag.forEach((fragment) -> {
				getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			});
			lFrag.clear();
			editorViewPager.removeAllViews();
		}
		
    	@Override
    	public ProjectTabEditorFragment createFragment(int position) {
			try {
				return lFrag.get(position);
			} catch (IndexOutOfBoundsException ignore) { 
				ignore.printStackTrace(System.err);
			}
			
			// Maybe create manually if null??
			return null;
    	}

   	 @Override
    	public int getItemCount() {
       	 return lFrag.size();
    	}
	}
}
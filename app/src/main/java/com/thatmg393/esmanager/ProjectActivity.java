package com.thatmg393.esmanager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.fragments.projectfragments.ProjectTabEditorFragment;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.utils.FileUtils;

import io.github.rosemoe.sora.lsp.editor.LspEditorManager;

import io.github.rosemoe.sora.widget.SymbolInputView;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
	private static String projectPath = null;
	public static String getProjectPath() { return projectPath; }
	
	private TabLayout editorTabLayout;
	private ViewPager2 editorViewPager;
	private ProjectEditorViewPager editorViewPagerAdapter;
	
	private Menu optionMenu;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		projectPath = GlobalConstants.ESM_ROOT_FOLDER + "/Roblox AFS Script";
		super.onCreate(savedInstanceState);
		setSupportActionBar((Toolbar) findViewById(R.id.project_toolbar));
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
		
		SymbolInputView symbolInput = findViewById(R.id.project_symbol_input);
		symbolInput.addSymbols(
			new String[] {
				""
			}, new String[] {
				""
			}
		);
		
		newEditorView(projectPath + "/MGUI.lua");
		newEditorView(projectPath + "/ThreadLib.lua");
	}
	
	public final void newEditorView(String filePath) {
		toggleViews(false);
		
		ProjectTabEditorFragment codeEditorFragment = new ProjectTabEditorFragment();
		codeEditorFragment.initializeEditor(getApplicationContext(), filePath);
		
		editorViewPagerAdapter.addFragment(codeEditorFragment);
		editorTabLayout.addTab(editorTabLayout.newTab().setText(FilenameUtils.getName(filePath)));
	}
	
	public void removeEditorView(int position) {
		if (editorTabLayout.getTabCount() == 0) return;
		else if (editorTabLayout.getTabCount() == 1) {
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
				ProjectTabEditorFragment tmpPtef = editorViewPagerAdapter.createFragment(editorTabLayout.getSelectedTabPosition());
				if (FileUtils.writeToFileUsingContent(tmpPtef.editor.getText(), tmpPtef.currentFilePath)) {
					Toast.makeText(getApplicationContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed to save file!", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.project_save_all_file:
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
		if (isChangingConfigurations()) return;
		editorViewPagerAdapter.removeAllFragments();
		LSPManager.getInstance().stopLSPServices();
	}
	
	private void toggleViews(boolean noFragmentLeft) {
		if (noFragmentLeft) {
			editorTabLayout.setVisibility(View.GONE);
			editorViewPager.setVisibility(View.GONE);
			
			findViewById(R.id.project_no_editor_layout).setVisibility(View.GONE);
			
			if (optionMenu != null) {
				optionMenu.findItem(R.id.project_save_file).setEnabled(false);
				optionMenu.findItem(R.id.project_save_all_file).setEnabled(false);
			}
		} else {
			editorTabLayout.setVisibility(View.VISIBLE);
			editorViewPager.setVisibility(View.VISIBLE);
			
			findViewById(R.id.project_no_editor_layout).setVisibility(View.VISIBLE);
			
			if (optionMenu != null) {
				optionMenu.findItem(R.id.project_save_file).setEnabled(true);
				optionMenu.findItem(R.id.project_save_all_file).setEnabled(true);
			}
		}
	}
}
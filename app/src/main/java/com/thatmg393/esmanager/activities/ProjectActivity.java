package com.thatmg393.esmanager.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.adapters.TabEditorAdapter;
import com.thatmg393.esmanager.fragments.project.FileTreeViewFragment;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.models.ProjectModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.EditorUtils;

import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.lsp.editor.LspEditorManager;
import io.github.rosemoe.sora.widget.SymbolInputView;

import java.util.Objects;

public class ProjectActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
	private DrawerLayout editorDrawerLayout;
	private Toolbar projectToolbar;
	private NavigationView editorFileDrawer;
	private FileTreeViewFragment editorFileTreeViewFragment;
	private TabLayout editorTabLayout;
	private TabEditorAdapter editorTabAdapter;
	private ViewPager2 editorViewPager;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		LSPManager.getInstance().setCurrentProject(
			new ProjectModel(
				"Roblox AFS Script",
				GlobalConstants.getInstance().ESM_ROOT_FOLDER + "/Roblox AFS Script",
				"v0.1",
				"ThatMG393"
			)
		);
		LSPManager.getInstance().registerLangServers();
		
		projectToolbar = findViewById(R.id.project_toolbar);
		setSupportActionBar(projectToolbar);
		
		editorDrawerLayout = findViewById(R.id.project_drawer_layout);
		ActionBarDrawerToggle drawerToggler = new ActionBarDrawerToggle(this, editorDrawerLayout, projectToolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
		editorDrawerLayout.addDrawerListener(drawerToggler);
		drawerToggler.syncState();
		
		editorFileTreeViewFragment = new FileTreeViewFragment();
		editorFileDrawer = findViewById(R.id.project_file_drawer);
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.project_file_drawer_fragment, editorFileTreeViewFragment)
			.commit();
		
		editorTabLayout = findViewById(R.id.project_editors_tab);
		editorTabLayout.addOnTabSelectedListener(this);
		
		editorViewPager = findViewById(R.id.project_editor_view);
		editorViewPager.setUserInputEnabled(false);
		
		editorTabAdapter = new TabEditorAdapter(getLifecycle(), getSupportFragmentManager(), findViewById(android.R.id.content));
		editorViewPager.setAdapter(editorTabAdapter);
		
		editorTabAdapter.addOnTabUpdateListener(new TabEditorAdapter.OnTabUpdateListener() {
			@Override
			public void onRemoveTab(int position) {
				invalidateOptionsMenu();
			}
		});
		
		editorFileTreeViewFragment.addTreeNodeListener((path) -> {
			int fragIdx = editorTabAdapter.getIndexOfFragment(path);
			if (fragIdx == -1) {
				editorTabAdapter.newTab(path);
			} else {
				if ((editorTabLayout.getSelectedTabPosition() - 1) != fragIdx) {
					Objects.requireNonNull(editorTabLayout.getTabAt(fragIdx)).select();
				}
			}
			
			if (editorDrawerLayout.isDrawerOpen(GravityCompat.END)) editorDrawerLayout.closeDrawer(GravityCompat.END);
		});
		
		FileProviderRegistry.getInstance().addFileProvider(
			new AssetsFileResolver(
				getAssets()
			)
		);
		
		GrammarRegistry.getInstance().loadGrammars("tm/languages/languages.json");
		EditorUtils.loadTMThemes();
		LSPManager.getInstance().startLSPForAllLanguage();
	}
	
	@Override
    public void onTabReselected(TabLayout.Tab tab) {
		ActivityUtils.getInstance().showPopupMenuAt(
			((ViewGroup) editorTabLayout.getChildAt(0)).getChildAt(tab.getPosition()),
			R.menu.editor_tab_menu,
			(menuItem) -> {
				if (menuItem.getItemId() == R.id.editor_close_tab) {
					editorTabAdapter.removeTab(tab.getPosition());
					return true;
				}
				return false;
			}
		);
	}
	
	@Override
    public void onTabSelected(TabLayout.Tab tab) {
        editorViewPager.setCurrentItem(tab.getPosition());
		System.out.println("selected on destroy?");
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (menu instanceof MenuBuilder) {
			((MenuBuilder)menu).setOptionalIconsVisible(true);
		}
		
		getMenuInflater().inflate(R.menu.project_action_menu_witheditor, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (LSPManager.getInstance().getEditorManager().getFocusedTabEditor() != null) {
			getMenuInflater().inflate(R.menu.project_action_menu_witheditor, menu);
		} else {
			getMenuInflater().inflate(R.menu.project_action_menu_noeditor, menu);
			
        	if (!ActivityUtils.getInstance().isUserUsingNavigationBar()) {
				MenuItem mnItem = menu.findItem(R.id.project_action_drawer_file_open);
				if (mnItem != null) mnItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == R.id.project_action_editor_save) {
			TabEditorFragment editorFragment = LSPManager.getInstance().getEditorManager().getFocusedTabEditor();
			if (editorFragment != null) {
				if (editorFragment.saveContents()) {
					Toast.makeText(getApplicationContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed to save file!", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_editor_save_all) {
			editorTabAdapter.getFragmentList().forEach((fragment) -> {
				if (!fragment.saveContents()) {
					Toast.makeText(getApplicationContext(), "Failed to save " + fragment.getCurrentFilePath(), Toast.LENGTH_SHORT).show();
				}
			});
			Toast.makeText(getApplicationContext(), "All files saved!", Toast.LENGTH_SHORT).show();
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_import_obj) {
			Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_editor_undo) {
			TabEditorFragment editorFragment = LSPManager.getInstance().getEditorManager().getFocusedTabEditor();
			if (editorFragment != null & editorFragment.getEditor().canUndo()) {
				editorFragment.getEditor().undo();
			}
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_editor_redo) {
			TabEditorFragment editorFragment = LSPManager.getInstance().getEditorManager().getFocusedTabEditor();
			if (editorFragment != null & editorFragment.getEditor().canRedo()) {
				editorFragment.getEditor().redo();
			}
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_drawer_file_open) {
			editorDrawerLayout.openDrawer(GravityCompat.END);
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
		} else { 
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
		
		LspEditorManager.closeAllManager();
		LSPManager.getInstance().stopLSPServices();
	}
	
	@Override
    public void onTabUnselected(TabLayout.Tab tab) { }
}
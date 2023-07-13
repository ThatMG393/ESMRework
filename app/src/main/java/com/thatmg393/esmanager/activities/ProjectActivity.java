package com.thatmg393.esmanager.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
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
import com.thatmg393.esmanager.utils.EditorUtils;

import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.lsp.editor.LspEditorManager;
import io.github.rosemoe.sora.widget.SymbolInputView;

import org.apache.commons.io.FilenameUtils;

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
		
		editorFileTreeViewFragment = new FileTreeViewFragment(LSPManager.getInstance().getCurrentProject().projectPath);
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
		
		editorFileTreeViewFragment.addTreeNodeListener((path) -> {
			editorTabAdapter.newTab(new TabEditorFragment(getApplicationContext(), path), FilenameUtils.getName(path));
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
		PopupMenu popupMenu = new PopupMenu(getApplicationContext(), ((ViewGroup) editorTabLayout.getChildAt(0)).getChildAt(tab.getPosition()));
		popupMenu.inflate(R.menu.editor_tab_menu);
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				if (menuItem.getItemId() == R.id.editor_close_tab) {
					editorTabAdapter.removeTab(tab.getPosition());
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
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == R.id.project_save_file) {
			if (editorTabLayout.getTabCount() == 0) return true;
				
			TabEditorFragment editorFragment = editorTabAdapter.getFragmentList().get(editorTabLayout.getSelectedTabPosition());
			if (editorFragment.saveContent()) {
				Toast.makeText(getApplicationContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Failed to save file!", Toast.LENGTH_SHORT).show();
			}
			return true;
		} else if (menuItem.getItemId() == R.id.project_save_all_file) {
			if (editorTabLayout.getTabCount() == 0) return true;
				
			editorTabAdapter.getFragmentList().forEach((fragment) -> {
				if (!fragment.saveContent()) {
					Toast.makeText(getApplicationContext(), "Failed to save " + fragment.currentFilePath, Toast.LENGTH_SHORT).show();
				}
			});
			Toast.makeText(getApplicationContext(), "All files saved!", Toast.LENGTH_SHORT).show();
			return true;
		} else if (menuItem.getItemId() == R.id.project_import_obj) {
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
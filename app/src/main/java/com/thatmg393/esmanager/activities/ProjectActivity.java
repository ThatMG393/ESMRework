package com.thatmg393.esmanager.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.adapters.TabEditorAdapter;
import com.thatmg393.esmanager.fragments.project.FileTreeViewFragment;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.fragments.project.base.BaseTabFragment;
import com.thatmg393.esmanager.fragments.project.base.PathedTabFragment;
import com.thatmg393.esmanager.interfaces.IOnTabUpdateListener;
import com.thatmg393.esmanager.managers.editor.EditorManager;
import com.thatmg393.esmanager.managers.editor.lsp.LSPManager;
import com.thatmg393.esmanager.managers.editor.project.ProjectManager;
import com.thatmg393.esmanager.models.ProjectModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.compat.IntentCompat;

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
	private SymbolInputView editorSymbolInput;
	
	@Override
	public void onInit(Bundle savedInstanceState) {
		super.onInit(savedInstanceState);
		
		ProjectManager.getInstance().setCurrentProject((ProjectModel) IntentCompat.getSerializableExtra(getIntent(), "projectInfo", ProjectModel.class));
		LSPManager.getInstance().registerLangServers();
		
		setContentView(R.layout.activity_project);
		
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
		editorTabAdapter = new TabEditorAdapter(getLifecycle(), getSupportFragmentManager(), findViewById(android.R.id.content));
		editorTabAdapter.addOnTabUpdateListener(new IOnTabUpdateListener() {
			@Override
			public void onRemoveTab(int position) {
				supportInvalidateOptionsMenu();
			}
		});
		
		editorViewPager = findViewById(R.id.project_editor_view);
		editorViewPager.setUserInputEnabled(false);
		editorViewPager.setAdapter(editorTabAdapter);
		
		editorSymbolInput = findViewById(R.id.project_symbol_input);
		editorSymbolInput.addSymbols(
			new String[] {
				"->", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/"
			}, new String[] {
				"\t", "{ }", "}", "()", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/"
			}
		);
		
		editorFileTreeViewFragment.addOnFileClickListener((path) -> {
			String pathExt = FilenameUtils.getExtension(path);
			
			switch (pathExt) {
				case "png":
				case "jpg":
				case "gif":
					editorTabAdapter.newTab(path, TabEditorAdapter.TabType.PICTURE);
					break;
				default:
					editorTabAdapter.newTab(path, TabEditorAdapter.TabType.EDITOR);
			}
			
			if (editorDrawerLayout.isDrawerOpen(GravityCompat.END)) editorDrawerLayout.closeDrawer(GravityCompat.END);
		});
		
		LSPManager.getInstance().startLSPForAllLanguage();
	}
	
	@Override
	public void onTabReselected(TabLayout.Tab tab) {
		ActivityUtils.getInstance().showPopupMenuAt(
			((ViewGroup) editorTabLayout.getChildAt(0)).getChildAt(tab.getPosition()),
			R.menu.project_tab_editor_menu,
			(menuItem) -> {
				if (menuItem.getItemId() == R.id.project_tab_editor_close) {
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
	}
	
	@Override
	@SuppressWarnings("RestrictedApi")
	public boolean onCreateOptionsMenu(Menu menu) {
		if (menu instanceof MenuBuilder) ((MenuBuilder)menu).setOptionalIconsVisible(true);
		
		getMenuInflater().inflate(R.menu.project_action_menu_noeditor, menu);
		return true;
	}
	
	@Override
	public void invalidateOptionsMenu() {
		super.invalidateOptionsMenu();
		
		PathedTabFragment frag = EditorManager.getInstance().getFocusedTabEditor();
		if (frag != null && frag instanceof TabEditorFragment) editorSymbolInput.bindEditor(((TabEditorFragment)frag).getEditor());
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (EditorManager.getInstance().getFocusedTabEditor() != null) {
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
			BaseTabFragment editorFragment = EditorManager.getInstance().getFocusedTabEditor();
			if (editorFragment != null) {
				if (editorFragment instanceof TabEditorFragment) {
					((TabEditorFragment) editorFragment).save();
				}
			}
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_editor_save_all) {
			editorTabAdapter.getFragmentList().forEach((model) -> {
				if (model.fragment instanceof TabEditorFragment) ((TabEditorFragment) model.fragment).save();
			});
			ActivityUtils.getInstance().showToast("All files saved!", Toast.LENGTH_SHORT);
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_import_obj) {
			ActivityUtils.getInstance().showToast("Function not implemented", Toast.LENGTH_SHORT);
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_editor_undo) {
			TabEditorFragment editorFragment = (TabEditorFragment) EditorManager.getInstance().getFocusedTabEditor();
			if (editorFragment != null & editorFragment.getEditor().canUndo()) editorFragment.getEditor().undo();
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_editor_redo) {
			TabEditorFragment editorFragment =  (TabEditorFragment) EditorManager.getInstance().getFocusedTabEditor();
			if (editorFragment != null & editorFragment.getEditor().canRedo()) editorFragment.getEditor().redo();
			return true;
		} else if (menuItem.getItemId() == R.id.project_action_drawer_file_open) {
			editorDrawerLayout.openDrawer(GravityCompat.END);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ActivityUtils.getInstance().registerActivity(this);
		if (editorFileTreeViewFragment != null) editorFileTreeViewFragment.onResume();
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
			ActivityUtils.getInstance().createAlertDialog(
				"Close project",
				"Are you sure?",
				new Pair<>("No", (dialog, which) -> dialog.dismiss()),
				new Pair<>("Yes", (dialog, which) -> {
					dialog.dismiss();
					_destroy();
					super.onBackPressed();
				})
			).show();
		}
	}
	
	private void _destroy() {
		if (isChangingConfigurations()) return;
		
		try {
			getSupportFragmentManager().beginTransaction()
				.remove(editorFileTreeViewFragment)
				.commit();
			
			ProjectManager.getInstance().getCurrentLspProject().closeAllEditors();
			LSPManager.getInstance().stopLSPForAllLanguage();
		} catch (RuntimeException ignore) { }
	}
	
	@Override
	public void onTabUnselected(TabLayout.Tab tab) { }
}
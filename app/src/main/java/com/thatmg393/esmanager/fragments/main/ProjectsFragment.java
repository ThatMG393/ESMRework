package com.thatmg393.esmanager.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.activities.ProjectActivity;
import com.thatmg393.esmanager.adapters.ModListAdapter;
import com.thatmg393.esmanager.fragments.main.base.ListFragment;
import com.thatmg393.esmanager.managers.rpc.impl.RPCSocketClient;
import com.thatmg393.esmanager.models.ModPropertiesModel;
import com.thatmg393.esmanager.models.ProjectModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.FileUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ProjectsFragment extends ListFragment<ModPropertiesModel> {
	public static final String TAG = "ProjectsFragment";
	private final String modInfoJson = "info.json";
	
	private RecyclerView projectsRecyclerView;
	private RelativeLayout projectsLoadingLayout;
	private RelativeLayout projectsEmptyLayout;
	private ModListAdapter projectsRecyclerAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		setDataType(ModPropertiesModel.class);
		return inflater.inflate(R.layout.fragment_main_project, parent, false);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(LIST_DATA_KEY, GSON.toJson(projectsRecyclerAdapter.getDataList()));
	}
	
	@Override
	public void initViews() {
		projectsRecyclerAdapter = new ModListAdapter(requireContext(), new ArrayList<ModPropertiesModel>());
		projectsRecyclerAdapter.setItemClickListener((v, pos) -> {
			ActivityUtils.getInstance().showPopupMenuAt(
				v,
				R.menu.main_project_menu,
				(menuItem) -> {
					if (menuItem.getItemId() == R.id.main_project_open) {
						ModPropertiesModel modProp = projectsRecyclerAdapter.getDataList().get(pos);
						
						Intent projectIntent = new Intent(requireActivity(), ProjectActivity.class);
						projectIntent.putExtra("projectInfo", new ProjectModel(
							modProp, modProp.getModPath()
						));
						startActivity(projectIntent);
						return true;
					} else if (menuItem.getItemId() == R.id.main_project_delete) {
						File project = new File(projectsRecyclerAdapter.getDataList().get(pos).getModPath());
						FileUtils.deleteRecursively(project);
						if (!project.exists()) {
							ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
							refreshOrPopulateRecyclerView();
						} else {
							ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
						}
						return true;
					}
					return false;
				}
			);
		});
		
		SwipeRefreshLayout projectsRefreshLayout = requireView().findViewById(R.id.fragment_project_refresh_layout);
		projectsRefreshLayout.setOnRefreshListener(() -> {
			updateViewStates(ReaderState.LOADING);
			refreshOrPopulateRecyclerView();
		});
		
		projectsRecyclerView = requireView().findViewById(R.id.fragment_project_recycler_view);
		projectsRecyclerView.setAdapter(projectsRecyclerAdapter);
		projectsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
		
		NewProjectDialogFragment newProjectDialog = new NewProjectDialogFragment();
		MaterialButton projectNewButton = requireView().findViewById(R.id.fragment_project_new_project);
		projectNewButton.setTranslationY(getResources().getDimension(com.intuit.sdp.R.dimen._7sdp));
		projectNewButton.setOnClickListener((v) -> newProjectDialog.show(getParentFragmentManager()));
		
		projectsLoadingLayout = requireView().findViewById(R.id.fragment_project_loading_container);
		projectsEmptyLayout = requireView().findViewById(R.id.fragment_project_empty_container);
		
		((MaterialTextView)projectsEmptyLayout.findViewById(R.id.list_empty_desc)).setText("No project/s found");
		
		registerLayouts(
			projectsRefreshLayout,
			projectsRecyclerView,
			projectsLoadingLayout,
			projectsEmptyLayout,
			() -> {
				try {
					File[] projectFolders = new File(GlobalConstants.getInstance().getESMRootFolder(), "Projects").listFiles((file, name) -> {
						return file.isDirectory();
					});
			
					if (projectsRecyclerAdapter.getDataList().size() > 0) projectsRecyclerView.post(() -> projectsRecyclerAdapter.clearData());
					if (projectFolders != null && projectFolders.length > 0) {
						for (File folder : projectFolders) {
							if (Thread.interrupted()) {
								updateViewStates(ReaderState.EMPTY); return;
							}
							
							File jsonFile = new File(folder.getAbsolutePath(), modInfoJson);
							if (jsonFile.exists() && jsonFile.isFile()) {
								try (InputStream jsonIS = new FileInputStream(jsonFile)) {
				 	  	 		JsonObject j = GSON.fromJson(IOUtils.toString(jsonIS, StandardCharsets.UTF_8), JsonObject.class);
							
									String projectName = j.get("name").getAsString();
									String projectDesc = j.get("description").getAsString();
									String projectAuthor = j.get("author").getAsString();
									String projectVersion = j.get("version").getAsString();
									String projectPreview = new File(folder.getAbsolutePath(), j.get("preview").getAsString()).getAbsolutePath();
									String projectPath = folder.getAbsolutePath();
							
									projectsRecyclerView.post(() -> projectsRecyclerAdapter.addData(new ModPropertiesModel(projectName, projectDesc, projectAuthor, projectVersion, DocumentFileCompat.fromFile(requireContext(), new File(projectPreview)).getUri().toString(), projectPath)));
								} catch (IOException | JsonSyntaxException e) {
									projectsRecyclerView.post(() -> projectsRecyclerAdapter.addData(new ModPropertiesModel(folder.getName(), null, null, null, null, folder.getAbsolutePath())));
								}
							}
		 	   		}
						updateViewStates(ReaderState.DONE);
					} else {
						updateViewStates(ReaderState.EMPTY);
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
					updateViewStates(ReaderState.EMPTY);
				
					projectsRecyclerView.post(() -> {
						ActivityUtils.getInstance().showToast("Failed to load projects\n" + e.getClass().getName() + "\n" + e.getMessage(), Toast.LENGTH_SHORT);
					});
				}
			}
		);
	}
}

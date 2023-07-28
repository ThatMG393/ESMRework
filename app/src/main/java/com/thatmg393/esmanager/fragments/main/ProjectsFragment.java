package com.thatmg393.esmanager.fragments.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.activities.ProjectActivity;
import com.thatmg393.esmanager.adapters.ModListAdapter;
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
import java.util.concurrent.Executors;

public class ProjectsFragment extends Fragment {
	private final String modInfoJson = "info.json";
	
	private RecyclerView projectsRecyclerView;
	private RelativeLayout projectsLoadingLayout;
	private RelativeLayout projectsEmptyLayout;
	private MaterialButton projectsNewProjectButton;
	private ModListAdapter projectsRecyclerAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_project, parent, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
		populateProjectList();
	}
	
	public void init() {
		projectsRecyclerAdapter = new ModListAdapter(requireContext(), new ArrayList<ModPropertiesModel>());
		
		projectsRecyclerView = requireView().findViewById(R.id.fragment_project_recycler_view);
		projectsRecyclerView.setAdapter(projectsRecyclerAdapter);
		projectsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
		
		projectsLoadingLayout = requireView().findViewById(R.id.fragment_project_loading_container);
		projectsEmptyLayout = requireView().findViewById(R.id.fragment_project_empty_container);
		
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
						//	System.out.println(projectsRecyclerAdapter.getDataList().get(pos).getModPath());
						File project = new File(projectsRecyclerAdapter.getDataList().get(pos).getModPath());
						FileUtils.deleteRecursively(project);
						if (!project.exists()) {
							ActivityUtils.getInstance().showToast(getString(R.string.file_drawer_toast_success), Toast.LENGTH_SHORT);
							populateProjectList();
						} else {
							ActivityUtils.getInstance().showToast(getString(R.string.file_drawer_toast_failed), Toast.LENGTH_SHORT);
						}
						return true;
					}
					return false;
				}
			);
		});
	}
	
	private void populateProjectList() {
		Executors.newSingleThreadExecutor().execute(() -> {
			File[] projectFolders = new File(GlobalConstants.ESM_ROOT_FOLDER, "Projects").listFiles((file, name) -> {
				return file.isDirectory();
			});
			
	  	  if (projectsRecyclerAdapter.getDataList().size() > 0) projectsRecyclerView.post(() -> projectsRecyclerAdapter.clearData());
	  	  if (projectFolders != null && projectFolders.length > 0) {
				for (File folder : projectFolders) {
					File jsonFile = new File(folder.getAbsolutePath(), modInfoJson);
					if (jsonFile.exists() && jsonFile.isFile()) {
						try (InputStream jsonIS = new FileInputStream(jsonFile)) {
				 	   	JsonObject j = RPCSocketClient.GSON.fromJson(IOUtils.toString(jsonIS, StandardCharsets.UTF_8), JsonObject.class);
							
				 		   String projectName = j.get("name").getAsString();
					 	   String projectDesc = j.get("description").getAsString();
			   			 String projectAuthor = j.get("author").getAsString();
				   		 String projectVersion = j.get("version").getAsString();
				   		 String projectPreview = new File(folder.getAbsolutePath(), j.get("preview").getAsString()).getAbsolutePath();
							String projectPath = folder.getAbsolutePath();
							
					 	   projectsRecyclerView.post(() -> projectsRecyclerAdapter.addData(new ModPropertiesModel(projectName, projectDesc, projectAuthor, projectVersion, projectPreview, projectPath)));
						} catch (IOException | JsonSyntaxException e) {
							projectsRecyclerView.post(() -> projectsRecyclerAdapter.addData(new ModPropertiesModel(folder.getName(), null, null, null, null, folder.getAbsolutePath())));
						}
					}
		 	   }
				projectsRecyclerView.post(() -> projectsRecyclerView.setVisibility(View.VISIBLE));
				projectsRecyclerView.post(() -> projectsLoadingLayout.setVisibility(View.GONE));
			} else {
				projectsRecyclerView.post(() -> projectsLoadingLayout.setVisibility(View.GONE));
				projectsRecyclerView.post(() -> projectsEmptyLayout.setVisibility(View.VISIBLE));
			}
  	  });
	}
}

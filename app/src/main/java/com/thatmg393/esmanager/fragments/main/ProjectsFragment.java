package com.thatmg393.esmanager.fragments.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
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
	
	private RelativeLayout projectMainLayout;
	private ListView projectListView;
	private MaterialButton projectNewProjectButton;
	private ModListAdapter projectListAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return projectMainLayout;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		projectListView.setEmptyView(noItems("No project/s found!"));
		populateProjectList();
	}
	
	public void init() {
		projectListAdapter = new ModListAdapter(requireContext(), new ArrayList<ModPropertiesModel>());
		
		projectListView = new ListView(requireActivity());
		projectListView.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		projectListView.setAdapter(projectListAdapter);
		projectListView.setOnItemClickListener((parent, v, pos, id) -> {
			ActivityUtils.getInstance().showPopupMenuAt(
				v,
				R.menu.main_project_menu,
				(menuItem) -> {
					if (menuItem.getItemId() == R.id.main_project_open) {
						projectListAdapter.getItem(pos);
						Intent projectIntent = new Intent(requireActivity(), ProjectActivity.class);
						projectIntent.putExtra("projectInfo", new ProjectModel(
							projectListAdapter.getItem(pos),
							GlobalConstants.ESM_ROOT_FOLDER + "Projects/" + projectListAdapter.getItem(pos).getModName()
						));
						return true;
					} else if (menuItem.getItemId() == R.id.main_project_delete) {
						DocumentFileCompat.fromSingleUri(requireContext(), Uri.parse(GlobalConstants.ESM_ROOT_FOLDER + "Projects/" + projectListAdapter.getItem(pos).getModName())).delete();
						populateProjectList();
						return true;
					}
					return false;
				}
			);
		});
		
		projectNewProjectButton = new MaterialButton(requireActivity());
		projectNewProjectButton.setText("New Project");
		
		projectMainLayout = new RelativeLayout(requireActivity());
		projectMainLayout.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		projectMainLayout.addView(projectListView);
	}
	
	private void populateProjectList() {
		Executors.newSingleThreadExecutor().execute(() -> {
			File[] projectFolders = new File(GlobalConstants.ESM_ROOT_FOLDER, "Projects").listFiles((file, name) -> {
				return file.isDirectory();
			});
			
	  	  if (projectListAdapter.getDataList().size() > 0) projectListAdapter.clearData();
	  	  if (projectFolders != null || projectFolders.length > 0) {
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
							
					 	   projectListView.post(() -> projectListAdapter.addData(new ModPropertiesModel(projectName, projectDesc, projectAuthor, projectVersion, projectPreview)));
						} catch (IOException | JsonSyntaxException e) {
							projectListView.post(() -> projectListAdapter.addData(new ModPropertiesModel(folder.getName(), null, null, null, null)));
						}
					}
		 	   }
			}
  	  });
	}
	
	private RelativeLayout noItems(String text) {
		ShapeableImageView noItemsIcon = new ShapeableImageView(requireActivity());
		noItemsIcon.setId(View.generateViewId());
		noItemsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_info));
		
   	 MaterialTextView noItemsDesc = new MaterialTextView(requireActivity());
		// noItemsDesc.setId(View.generateViewId());
   	 noItemsDesc.setTextColor(requireContext().getResources().getColor(R.color.colorPrimary));
    	noItemsDesc.setText(text);
   	 
		RelativeLayout noItemsLayout = new RelativeLayout(requireActivity());
		noItemsLayout.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		noItemsLayout.setVisibility(View.GONE);
		
		RelativeLayout.LayoutParams noItemsIconLP = new RelativeLayout.LayoutParams(
			new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
			)
		);
		noItemsIconLP.addRule(RelativeLayout.CENTER_IN_PARENT);
		noItemsLayout.addView(noItemsIcon, noItemsIconLP);
		
		RelativeLayout.LayoutParams noItemsDescLP = new RelativeLayout.LayoutParams(
			new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
			)
		);
		noItemsDescLP.addRule(RelativeLayout.CENTER_IN_PARENT);
		noItemsDescLP.addRule(RelativeLayout.BELOW, noItemsIcon.getId());
		noItemsDescLP.setMargins(0, 8, 0, 0);
		noItemsLayout.addView(noItemsDesc, noItemsDescLP);
		
   	 ((ViewGroup) projectListView.getParent()).addView(noItemsLayout);
  	  return noItemsLayout;
	}
}

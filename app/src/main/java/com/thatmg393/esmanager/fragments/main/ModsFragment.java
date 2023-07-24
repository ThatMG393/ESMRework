package com.thatmg393.esmanager.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.adapters.ModListAdapter;
import com.thatmg393.esmanager.managers.rpc.impl.RPCSocketClient;
import com.thatmg393.esmanager.models.ModPropertiesModel;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ModsFragment extends Fragment {
	private final String modInfoJson = "info.json";
	
	private LinearLayout modsMainLayout;
	private ListView modsListView;
	private ModListAdapter modsListAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return modsMainLayout;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		modsListView.setEmptyView(noItems("No mod/s found!"));
		populateModsList();
	}
	
	public void init() {
		modsListAdapter = new ModListAdapter(requireContext(), new ArrayList<ModPropertiesModel>());
		
		modsListView = new ListView(requireActivity());
		modsListView.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		modsListView.setAdapter(modsListAdapter);
		
		modsMainLayout = new LinearLayout(requireActivity());
		modsMainLayout.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		modsMainLayout.addView(modsListView);
	}
	
	private void populateModsList() {
		Executors.newSingleThreadExecutor().execute(() -> {
			DocumentFileCompat modFolder = DocumentFileCompat.fromTreeUri(requireContext(), GlobalConstants.ES_MOD_FOLDER);
			List<DocumentFileCompat> modFolders = modFolder.listFiles();
			
	  	  if (modsListAdapter.getDataList().size() > 0) modsListAdapter.clearData();
	  	  if (modFolders != null || modFolders.size() > 0) {
				for (DocumentFileCompat folder : modFolders) {
					if (folder.isFile()) continue;
					if (folder.getName().toLowerCase().equals("tools")) continue;
					
					DocumentFileCompat jsonFile = DocumentFileCompat.fromSingleUri(requireContext(), Uri.parse(folder.getUri().toString() + "%2F" + modInfoJson));
					if (jsonFile.exists() && jsonFile.isFile()) {
						try (InputStream jsonIS = requireContext().getContentResolver().openInputStream(jsonFile.getUri())) {
				 	   	JsonObject j = RPCSocketClient.GSON.fromJson(IOUtils.toString(jsonIS, StandardCharsets.UTF_8), JsonObject.class);
							
				 		   String modName = j.get("name").getAsString();
					 	   String modDesc = j.get("description").getAsString();
			   			 String modAuthor = j.get("author").getAsString();
				   		 String modVersion = j.get("version").getAsString();
				   		 String modPreview = folder.getUri().toString() + "%2F" + j.get("preview").getAsString().replace("/", "%2F");
							
					 	   modsListView.post(() -> modsListAdapter.addData(new ModPropertiesModel(modName, modDesc, modAuthor, modVersion, modPreview)));
						} catch (IOException | JsonSyntaxException e) {
							modsListView.post(() -> modsListAdapter.addData(new ModPropertiesModel(folder.getName(), null, null, null, null)));
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
		
   	 ((ViewGroup) modsListView.getParent()).addView(noItemsLayout);
  	  return noItemsLayout;
	}
}

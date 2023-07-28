package com.thatmg393.esmanager.fragments.main;

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

import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.adapters.ModListAdapter;
import com.thatmg393.esmanager.managers.rpc.impl.RPCSocketClient;
import com.thatmg393.esmanager.models.ModPropertiesModel;
import com.thatmg393.esmanager.utils.ActivityUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ModsFragment extends Fragment {
	private final String modInfoJson = "info.json";
	
	private RecyclerView modsRecyclerView;
	private RelativeLayout modsLoadingLayout;
	private RelativeLayout modsEmptyLayout;
	private ModListAdapter modsRecyclerAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_mod, parent, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
		populateModsList();
	}
	
	public void init() {
		modsRecyclerAdapter = new ModListAdapter(requireContext(), new ArrayList<ModPropertiesModel>());
		
		modsRecyclerView = requireView().findViewById(R.id.fragment_mod_recycler_view);
		modsRecyclerView.setAdapter(modsRecyclerAdapter);
		modsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
		
		modsLoadingLayout = requireView().findViewById(R.id.fragment_mod_loading_container);
		modsEmptyLayout = requireView().findViewById(R.id.fragment_mod_empty_container);
		
		((MaterialTextView)modsEmptyLayout.findViewById(R.id.list_empty_desc)).setText("No mods/s found");
	}
	
	private void populateModsList() {
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				DocumentFileCompat modFolder = DocumentFileCompat.fromTreeUri(requireContext(), GlobalConstants.ES_MOD_FOLDER);
				List<DocumentFileCompat> modFolders = modFolder.listFiles();
			
	  	 	 if (modsRecyclerAdapter.getDataList().size() > 0) modsRecyclerView.post(() -> modsRecyclerAdapter.clearData());
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
								String modPath = folder.getUri().toString();
								
					 	 	  modsRecyclerView.post(() -> modsRecyclerAdapter.addData(new ModPropertiesModel(modName, modDesc, modAuthor, modVersion, modPreview, modPath)));
							} catch (IOException | JsonSyntaxException e) {
								modsRecyclerView.post(() -> modsRecyclerAdapter.addData(new ModPropertiesModel(folder.getName(), null, null, null, null, folder.getUri().toString())));
							}
						}
		 	  	 }
					modsRecyclerView.post(() -> modsRecyclerView.setVisibility(View.VISIBLE));
					modsRecyclerView.post(() -> modsLoadingLayout.setVisibility(View.GONE));
				} else {
					modsRecyclerView.post(() -> modsLoadingLayout.setVisibility(View.GONE));
					modsRecyclerView.post(() -> modsEmptyLayout.setVisibility(View.VISIBLE));
				}
			} catch (Exception e) {
				modsRecyclerView.post(() -> modsLoadingLayout.setVisibility(View.GONE));
				modsRecyclerView.post(() -> modsEmptyLayout.setVisibility(View.VISIBLE));
					
				modsRecyclerView.post(() -> ActivityUtils.getInstance().showToast("Failed to load mods\n" + e.getClass().getName() + "\n" + e.getMessage(), Toast.LENGTH_SHORT));
			}
  	  });
	}
}

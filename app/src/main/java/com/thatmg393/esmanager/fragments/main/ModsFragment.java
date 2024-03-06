package com.thatmg393.esmanager.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.adapters.ModListAdapter;
import com.thatmg393.esmanager.fragments.main.base.ListFragment;
import com.thatmg393.esmanager.managers.rpc.impl.RPCSocketClient;
import com.thatmg393.esmanager.models.ModPropertiesModel;
import com.thatmg393.esmanager.utils.ActivityUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModsFragment extends ListFragment<ModPropertiesModel> {
	public static final String modInfoJson = "info.json";
	
	private RecyclerView modsRecyclerView;
	private RelativeLayout modsLoadingLayout;
	private RelativeLayout modsEmptyLayout;
	private ModListAdapter modsRecyclerAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		setDataType(ModPropertiesModel.class);
		return inflater.inflate(R.layout.fragment_main_mod, parent, false);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(LIST_DATA_KEY, GSON.toJson(modsRecyclerAdapter.getDataList()));
	}
	
	@Override
	public void initViews() {
		modsRecyclerAdapter = new ModListAdapter(requireContext(), new ArrayList<ModPropertiesModel>());
		
		SwipeRefreshLayout modsRefreshLayout = requireView().findViewById(R.id.fragment_mod_refresh_layout);
		modsRefreshLayout.setOnRefreshListener(() -> {
			updateViewStates(ReaderState.LOADING);
			refreshOrPopulateRecyclerView();
		});
		
		modsRecyclerView = requireView().findViewById(R.id.fragment_mod_recycler_view);
		modsRecyclerView.setAdapter(modsRecyclerAdapter);
		modsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
		
		modsLoadingLayout = requireView().findViewById(R.id.fragment_mod_loading_container);
		modsEmptyLayout = requireView().findViewById(R.id.fragment_mod_empty_container);
		
		((MaterialTextView)modsEmptyLayout.findViewById(R.id.list_empty_desc)).setText("No mods/s found");
		
		registerLayouts(
			modsRefreshLayout,
			modsRecyclerView,
			modsLoadingLayout,
			modsEmptyLayout,
			() -> {
				try {
					DocumentFileCompat modFolder = DocumentFileCompat.fromTreeUri(requireContext(), Uri.parse(GlobalConstants.getInstance().getESModFolder()));
					List<DocumentFileCompat> modFolders = modFolder.listFiles();
				
					if (modsRecyclerAdapter.getDataList().size() > 0) modsRecyclerView.post(() -> modsRecyclerAdapter.clearData());
					if (modFolders != null || modFolders.size() > 0) {
						for (DocumentFileCompat folder : modFolders) {
							if (Thread.interrupted()) {
								updateViewStates(ReaderState.EMPTY); return;
							}
							if (folder.isFile()) continue;
							if (folder.getName().toLowerCase(Locale.getDefault()).equals("tools")) continue;
					
							DocumentFileCompat jsonFile = DocumentFileCompat.fromSingleUri(requireContext(), Uri.parse(folder.getUri().toString() + "%2F" + modInfoJson));
							if (jsonFile.exists() && jsonFile.isFile()) {
								try (InputStream jsonIS = requireContext().getContentResolver().openInputStream(jsonFile.getUri())) {
									JsonObject j = GSON.fromJson(IOUtils.toString(jsonIS, StandardCharsets.UTF_8), JsonObject.class);
							
									String modPath = folder.getUri().toString();
									String modName = j.get("name").getAsString();
									String modDesc = j.get("description").getAsString();
									String modAuthor = j.get("author").getAsString();
									String modVersion = j.get("version").getAsString();
									String modPreview = modPath + "%2F" + j.get("preview").getAsString().replace("/", "%2F");
									
					 		 	  modsRecyclerView.post(() -> modsRecyclerAdapter.addData(new ModPropertiesModel(modName, modDesc, modVersion, modAuthor, modPreview, modPath)));
								} catch (IOException | JsonSyntaxException e) {
									modsRecyclerView.post(() -> modsRecyclerAdapter.addData(new ModPropertiesModel(folder.getName(), null, null, null, null, folder.getUri().toString())));
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
					
					modsRecyclerView.post(() -> {
						if (e instanceof UnsupportedOperationException && e.getMessage().toLowerCase(Locale.getDefault()).contains("is not tree uri")) {
							ActivityUtils.getInstance().showToast("Is this a Tree URI?\n" + GlobalConstants.getInstance().getESModFolder(), Toast.LENGTH_SHORT);
						} else {
							ActivityUtils.getInstance().showToast("Failed to load mods\n" + e.getClass().getName() + "\n" + e.getMessage(), Toast.LENGTH_SHORT);
						}
					});
				}
			}
		);
	}
}

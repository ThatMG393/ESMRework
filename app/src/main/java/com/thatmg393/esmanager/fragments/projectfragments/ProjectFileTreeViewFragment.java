package com.thatmg393.esmanager.fragments.projectfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.ProjectActivity;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.projectfragments.viewholders.FileViewHolder;
import com.thatmg393.esmanager.fragments.projectfragments.viewholders.FolderViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectFileTreeViewFragment extends Fragment {
	private RecyclerView rv;
	
	private TreeViewAdapter tvAdapter;
	private List<TreeNode> tnList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.project_treeview_fragment, container, false);
	}
	
	@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		initialize();
		
		TreeNode tmpNode = crawlStorageFiles(new File(ProjectActivity.getProjectPath()));
		if (tmpNode == null) {
			rv.setVisibility(View.GONE);
			requireView().findViewById(R.id.project_treeview_no_files_layout).setVisibility(View.VISIBLE);
		} else {
			tnList.add(tmpNode);
			tvAdapter.updateTreeNodes(tnList);
		}
	}
	
	public TreeNode crawlStorageFiles(File parentPath) {
		if (!parentPath.exists()) return null;
	
    	if (parentPath.isDirectory())  {
        	TreeNode node = new TreeNode(parentPath, R.layout.project_folder_tree_view);
        	for (File file : parentPath.listFiles()) {
            	node.addChild(crawlStorageFiles(file));
        	}
        	return node;
    	} else {
        	TreeNode node = new TreeNode(parentPath, R.layout.project_file_tree_view);
        	return node;
    	}
	}
	
	private void initialize() {
		TreeViewHolderFactory tvFactory = (v, layout) -> {
			if (layout == R.layout.project_file_tree_view) return new FileViewHolder(v);
			return new FolderViewHolder(v);
		};
		tvAdapter = new TreeViewAdapter(tvFactory);
		tvAdapter.setTreeNodeClickListener((treeNode, treeView) -> {
			listeners.forEach((listener) -> {
				File tmpF = (File) treeNode.getValue();
				if (!tmpF.isDirectory()) listener.onTreeNodeClick(tmpF.getAbsolutePath());
			});
		});
		
		rv = requireView().findViewById(R.id.project_treeview);
		rv.setAdapter(tvAdapter);
		rv.setLayoutManager(new LinearLayoutManager(getContext()));

		tnList = new ArrayList<TreeNode>();
	}
	
	private ArrayList<OnTreeNodeClick> listeners = new ArrayList<>();
	public void addListener(OnTreeNodeClick otnc) {
		listeners.add(otnc);
	}
	
	public static interface OnTreeNodeClick {
		public void onTreeNodeClick(String filePath);
	}
}

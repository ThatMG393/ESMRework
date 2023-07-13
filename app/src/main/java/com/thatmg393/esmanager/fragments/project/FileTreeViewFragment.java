package com.thatmg393.esmanager.fragments.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.viewholders.FileViewHolder;
import com.thatmg393.esmanager.viewholders.FolderViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTreeViewFragment extends Fragment {
	private File parentPath;
	
	private HorizontalScrollView recyclerScrollView;
	private RecyclerView recyclerView;
	private TreeViewAdapter treeAdapter;
	
	private List<TreeNode> nodes = new ArrayList<TreeNode>();
	
	private boolean isRefreshing;
	
	public FileTreeViewFragment(String path) {
		this.parentPath = new File(path);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return recyclerScrollView;
	}
	
	@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		refreshOrPopulateTreeView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshOrPopulateTreeView();
	}
	
	public synchronized void refreshOrPopulateTreeView() {
		if (isRefreshing) return;
		System.out.println(nodes.size());
			if (nodes.size() > 0) {
				nodes.clear();
				treeAdapter.updateTreeNodes(nodes);
				
				isRefreshing = false;
				refreshOrPopulateTreeView();
				return;
			}
			
			isRefreshing = true;
			
			TreeNode tmpNode = traverseFileSystem(parentPath);
			nodes.add(tmpNode);
			treeAdapter.updateTreeNodes(nodes);
		
		isRefreshing = false;
	}
	
	private TreeNode traverseFileSystem(File directory) {
		if (!directory.exists()) {
			return new TreeNode("Dir does not exist", R.layout.project_folder_tree_view);
		} else {
			if (directory.isDirectory()) {
				TreeNode directoryNode = new TreeNode(directory.getAbsolutePath(), R.layout.project_folder_tree_view);
			
				File[] files = directory.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						if (i > 15) break;
						directoryNode.addChild(traverseFileSystem(files[i]));
					}
					return directoryNode;
				}
				return directoryNode;
			} else {
				TreeNode fileNode = new TreeNode(directory.getAbsolutePath(), R.layout.project_file_tree_view);
				return fileNode;
			}
		}
	}
	
	private void init() {
		TreeViewHolderFactory treeFactory = (view, layout) -> {
			if (layout == R.layout.project_folder_tree_view) return new FolderViewHolder(view);
			return new FileViewHolder(view);
		};
		
		treeAdapter = new TreeViewAdapter(treeFactory);
		treeAdapter.setTreeNodeClickListener(new TreeViewAdapter.OnTreeNodeClickListener() {
			@Override
			public void onTreeNodeClick(TreeNode node, View treeView) {
				if (node.getLayoutId() == R.layout.project_file_tree_view) {
					callOnNodeClickListeners((String) node.getValue());
				}
			}
		});
		
		recyclerView = new RecyclerView(getContext());
		recyclerView.setAdapter(treeAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setVerticalScrollBarEnabled(true);
		recyclerView.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		
		recyclerScrollView = new HorizontalScrollView(getContext());
		recyclerScrollView.setFillViewport(true);
		recyclerScrollView.setLayoutParams(
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		);
		
		recyclerScrollView.addView(recyclerView);
	}
	
	private ArrayList<OnTreeNodeClick> nodeClickListeners = new ArrayList<OnTreeNodeClick>();
	public void addTreeNodeListener(OnTreeNodeClick listener) {
		nodeClickListeners.add(listener);
	}
	public void removeTreeNodeListener(TreeViewAdapter.OnTreeNodeClickListener listener) {
		nodeClickListeners.remove(listener);
	}
	public void callOnNodeClickListeners(String filePath) {
		nodeClickListeners.forEach((listener) -> listener.onTreeNodeClick(filePath));
	}
	
	public static interface OnTreeNodeClick {
		public void onTreeNodeClick(String filePath);
	}
}

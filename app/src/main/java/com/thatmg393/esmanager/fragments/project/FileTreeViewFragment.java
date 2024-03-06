package com.thatmg393.esmanager.fragments.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.anggrayudi.storage.file.CreateMode;
import com.anggrayudi.storage.file.FileUtils;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.managers.editor.project.ProjectManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.viewholders.tree.FileViewHolder;
import com.thatmg393.esmanager.viewholders.tree.FolderViewHolder;

import com.thatmg393.esmanager.viewholders.tree.NoFileViewHolder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileTreeViewFragment extends Fragment {
	private String parentPath;
	
	private HorizontalScrollView recyclerScrollView;
	private RecyclerView recyclerView;
	private TreeViewAdapter treeAdapter;
	
	private TreeNode rootNode;
	private TreeNode lastNodeThatGotClick;
	private boolean isRefreshing;
	
	public FileTreeViewFragment() {
		this.parentPath = ProjectManager.getInstance().getCurrentProject().projectPath;
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
		isRefreshing = true;
		
		ArrayList<TreeNode> tmpNodes = new ArrayList<>();
		tmpNodes.add(rootNode);
		
		treeAdapter.clearTreeNodes();
		treeAdapter.updateTreeNodes(tmpNodes);
		
		rootNode.getChildren().clear();
		listTopLevelOfDirectory(rootNode);
		
		isRefreshing = false;
		treeAdapter.expandNode(rootNode);
		
		if (lastNodeThatGotClick != null) treeAdapter.expandNodeToLevel(lastNodeThatGotClick, 0);
	}
	
	private void listTopLevelOfDirectory(TreeNode rootDir) {
		String rootPath = (String) rootDir.getValue();
		if (Files.exists(Paths.get(rootPath))) {
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(rootPath), entry -> Files.isDirectory(entry) || Files.isRegularFile(entry));
				DirectoryStream<Path> dirStream2 = Files.newDirectoryStream(Paths.get(rootPath), entry -> Files.isDirectory(entry) || Files.isRegularFile(entry))) {
				if (!dirStream2.iterator().hasNext()) {
					rootDir.addChild(new TreeNode("nofile", R.layout.project_nofile_tree_view));
				} else {
					for (Path path : dirStream) {
						if (Files.isDirectory(path)) {
							rootDir.addChild(new TreeNode(path.toRealPath().toString(), R.layout.project_folder_tree_view));
						} else {
							rootDir.addChild(new TreeNode(path.toRealPath().toString(), R.layout.project_file_tree_view));
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			rootDir.addChild(new TreeNode("nofile", R.layout.project_nofile_tree_view));
		}
	}
	
	private void init() {
		TreeViewHolderFactory treeFactory = (view, layout) -> {
			if (layout == R.layout.project_file_tree_view)
				return new FileViewHolder(view);
			if (layout == R.layout.project_folder_tree_view)
				return new FolderViewHolder(view);
			return new NoFileViewHolder(view);
		};
		
		treeAdapter = new TreeViewAdapter(treeFactory);
		treeAdapter.setTreeNodeClickListener((node, treeView) -> {
			if (!validateNodeValue((String) node.getValue())) return;
			
			if (node.getLayoutId() == R.layout.project_file_tree_view) {
				lastNodeThatGotClick = node;
				dispatchOnFileClick((String) node.getValue());
			} else {
				if (node.getChildren().size() == 0) {
					listTopLevelOfDirectory(node);
					treeAdapter.expandNode(node);
				} else {
					node.getChildren().clear();
				}
				lastNodeThatGotClick = node;
			}
		});
		treeAdapter.setTreeNodeLongClickListener((node, treeView) -> {
			if (!validateNodeValue((String) node.getValue())) return false;
			lastNodeThatGotClick = node;
			
			// FIXME: Refactor or something
			if (node.getLayoutId() == R.layout.project_file_tree_view) {
				ActivityUtils.getInstance().showPopupMenuAt(
					treeView,
					R.menu.project_drawer_file_file,
					(menuItem) -> {
						if (menuItem.getItemId() == R.id.project_drawer_file_rename) {
							EditText name = new EditText(requireActivity());
							name.setText(FilenameUtils.getName((String) node.getValue()));
							
							ActivityUtils.getInstance().createAlertDialog(
								getString(R.string.file_drawer_popup_rename),
								name,
								new Pair<>(getString(R.string.file_drawer_popup_cancel), (dialog, which) -> dialog.dismiss()),
								new Pair<>(getString(R.string.file_drawer_popup_rename), (dialog, which) -> {
									File target = new File((String) node.getValue());
									String newNameOfTarget = name.getText().toString();
									
									if (target.renameTo(new File(target.getParent(), newNameOfTarget))) {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
										refreshOrPopulateTreeView();
									} else {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
									}
								})
							).show();
							return true;
						} else if (menuItem.getItemId() == R.id.project_drawer_file_delete) {
							ActivityUtils.getInstance().createAlertDialog(
								getString(R.string.file_drawer_popup_delete),
								getString(R.string.file_drawer_popup_delete_confirmation),
								new Pair<>(getString(R.string.file_drawer_popup_cancel), (dialog, which) -> dialog.dismiss()),
								new Pair<>(getString(R.string.file_drawer_popup_delete), (dialog, which) -> {
									if (FileUtils.forceDelete(new File((String) node.getValue()))) {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
										refreshOrPopulateTreeView();
									} else {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
									}
								})
							).show();
							return true;
						}
						return false;
					}
				);
			} else {
				ActivityUtils.getInstance().showPopupMenuAt(
					treeView,
					R.menu.project_drawer_file_folder,
					(menuItem) -> {
						if (menuItem.getItemId() == R.id.project_drawer_file_new_file) {
							EditText name = new EditText(requireActivity());
							ActivityUtils.getInstance().createAlertDialog(
								getString(R.string.file_drawer_popup_new_file),
								name,
								new Pair<>(getString(R.string.file_drawer_popup_cancel), (dialog, which) -> { dialog.dismiss(); }),
								new Pair<>(getString(R.string.file_drawer_popup_create), (dialog, which) -> {
									File newFile = new File((String) node.getValue(), name.getText().toString());
									
									if (FileUtils.createNewFileIfPossible(newFile)) {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
										refreshOrPopulateTreeView();
									} else {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
									}
								})
							).show();
							return true;
						} else if (menuItem.getItemId() == R.id.project_drawer_file_new_folder) {
							EditText name = new EditText(requireActivity());
							ActivityUtils.getInstance().createAlertDialog(
								getString(R.string.file_drawer_popup_new_folder),
								name,
								new Pair<>(getString(R.string.file_drawer_popup_cancel), (dialog, which) -> { dialog.dismiss(); }),
								new Pair<>(getString(R.string.file_drawer_popup_create), (dialog, which) -> {
									File file = new File((String) node.getValue(), name.getText().toString());
									
									if (FileUtils.makeFolder(new File((String) node.getValue()), requireContext(), name.getText().toString(), CreateMode.SKIP_IF_EXISTS) != null) {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
										refreshOrPopulateTreeView();
									} else {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
									}
								})
							).show();
							return true;
						} else if (menuItem.getItemId() == R.id.project_drawer_file_rename) {
							EditText name = new EditText(requireActivity());
							name.setText(FilenameUtils.getName((String) node.getValue()));
							
							ActivityUtils.getInstance().createAlertDialog(
								getString(R.string.file_drawer_popup_rename),
								name,
								new Pair<>(getString(R.string.file_drawer_popup_cancel), (dialog, which) -> dialog.dismiss()),
								new Pair<>(getString(R.string.file_drawer_popup_rename), (dialog, which) -> {
									File target = new File((String) node.getValue());
									String newNameOfTarget = name.getText().toString();
									
									if (target.renameTo(new File(target.getParent(), newNameOfTarget))) {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
										refreshOrPopulateTreeView();
									} else {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
									}
								})
							).show();
							return true;
						} else if (menuItem.getItemId() == R.id.project_drawer_file_delete) {
							ActivityUtils.getInstance().createAlertDialog(
								getString(R.string.file_drawer_popup_delete),
								getString(R.string.file_drawer_popup_delete_confirmation),
								new Pair<>(getString(R.string.file_drawer_popup_cancel), (dialog, which) -> dialog.dismiss()),
								new Pair<>(getString(R.string.file_drawer_popup_delete), (dialog, which) -> {
									if (FileUtils.forceDelete(new File((String) node.getValue()))) {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_success), Toast.LENGTH_SHORT);
										refreshOrPopulateTreeView();
									} else {
										ActivityUtils.getInstance().showToast(getString(R.string.toast_failed), Toast.LENGTH_SHORT);
									}
								})
							).show();
							return true;
						}
						return false;
					}
				);
			}
			return true;
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
		recyclerScrollView.setPadding(8, 8, 8, 8);
		recyclerScrollView.addView(recyclerView);
		
		if (rootNode == null) rootNode = new TreeNode(parentPath, R.layout.project_folder_tree_view);
	}
	
	private boolean validateNodeValue(String value) {
		return !value.equals(getString(R.string.file_drawer_no_files));
	}
	
	private ArrayList<OnFileClick> fileClickListeners = new ArrayList<>();
	public void addOnFileClickListener(OnFileClick listener) {
		fileClickListeners.add(listener);
	}
	public void removeOnFileClickListener(OnFileClick listener) {
		fileClickListeners.remove(listener);
	}
	public void dispatchOnFileClick(String filePath) {
		fileClickListeners.forEach((listener) -> listener.onFileClick(filePath));
	}
	
	public static interface OnFileClick {
		public void onFileClick(String filePath);
	}
}

package com.thatmg393.esmanager.viewholders.tree;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;
import com.thatmg393.esmanager.R;

import java.io.File;

public class FolderViewHolder extends TreeViewHolder {
	private final TextView tv;
	
	public FolderViewHolder(@NonNull View itemView) {
		super(itemView);
		tv = itemView.findViewById(R.id.project_treeview_folder_name);
	}

	@Override
	public void bindTreeNode(TreeNode node) {
		super.bindTreeNode(node);
		File nodeName = new File((String) node.getValue());
		tv.setText(nodeName.getName());
		
		switch (nodeName.getName()) {
			case "audio":  
			case "meshes": 
			case "scripts": 
			case "textures": setDrawableRight(R.drawable.ic_info); break;
			default: setDrawableRight(R.drawable.ic_folder); break;
		}
	}
	
	private void setDrawableRight(@DrawableRes int drawable) {
		tv.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
	}
}

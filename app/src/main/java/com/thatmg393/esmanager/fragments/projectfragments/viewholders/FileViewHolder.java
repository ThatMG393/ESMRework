package com.thatmg393.esmanager.fragments.projectfragments.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;
import com.thatmg393.esmanager.R;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

public class FileViewHolder extends TreeViewHolder {
	private final TextView tv;
	
	public FileViewHolder(@NonNull View itemView) {
        super(itemView);
		tv = itemView.findViewById(R.id.project_treeview_file_name);
    }

    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);
		File nodeName = (File) node.getValue();
		tv.setText(nodeName.getName());
		
        switch (FilenameUtils.getExtension(nodeName.getAbsolutePath())) {
			case "lua": // TODO: Import icons
			case "json":
			case "wav":
			case "png":
			case "jpg":
			case "obj": setDrawableRight(R.drawable.ic_edit_black); break;
			default: setDrawableRight(R.drawable.ic_file_black); break;
		}
    }
	
	private void setDrawableRight(@DrawableRes int drawable) {
		tv.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
	}
}

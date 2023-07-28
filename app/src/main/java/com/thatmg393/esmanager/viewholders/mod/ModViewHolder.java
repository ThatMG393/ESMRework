package com.thatmg393.esmanager.viewholders.mod;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.thatmg393.esmanager.R;

public class ModViewHolder extends RecyclerView.ViewHolder {
    public final MaterialTextView nameTextView;
    public final MaterialTextView descTextView;
    public final MaterialTextView versionTextView;
    public final MaterialTextView authorTextView;
	public final ShapeableImageView previewView;
	
    public ModViewHolder(@NonNull View view) {
		super(view);
		
        this.nameTextView = view.findViewById(R.id.mod_list_layout_name);
        this.descTextView = view.findViewById(R.id.mod_list_layout_description);
        this.versionTextView = view.findViewById(R.id.mod_list_layout_version);
        this.authorTextView = view.findViewById(R.id.mod_list_layout_author);
		this.previewView = view.findViewById(R.id.mod_list_layout_preview);
    }
}

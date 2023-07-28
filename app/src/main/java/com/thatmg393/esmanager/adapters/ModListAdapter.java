package com.thatmg393.esmanager.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.interfaces.IOnRecyclerItemClickListener;
import com.thatmg393.esmanager.models.ModPropertiesModel;
import com.thatmg393.esmanager.utils.BitmapUtils;
import com.thatmg393.esmanager.viewholders.mod.ModViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModListAdapter extends RecyclerView.Adapter<ModViewHolder> {
    private final Context context;
    private final ArrayList<ModPropertiesModel> data;
	
	private IOnRecyclerItemClickListener itemClickListener;
	
    public ModListAdapter(@NonNull Context context, ArrayList<ModPropertiesModel> data) {
        this.context = context;
        this.data = data;
    }

    public void addData(ModPropertiesModel data) {
        if (data != null) {
            this.data.add(data);
        }
        notifyDataSetChanged();
    }

    public void updateData(List<ModPropertiesModel> data) {
        if (data != null && data.size() > 0) {
            this.data.clear();
            this.data.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        if (data != null && data.size() > 0) data.clear();
        notifyDataSetChanged();
    }

    public ArrayList<ModPropertiesModel> getDataList() {
        return data;
    }
	
	public void setItemClickListener(IOnRecyclerItemClickListener listener) {
		this.itemClickListener = listener;
	}
	
    @NonNull
    @Override
    public ModViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layout = LayoutInflater.from(context).inflate(R.layout.mod_list_layout, parent, false);
		ModViewHolder holder = new ModViewHolder(layout);
		
		layout.setOnClickListener((view) -> {
			if (itemClickListener != null) itemClickListener.onRecyclerItemClick(view, holder.getAdapterPosition());
		});
		
        return holder;
    }
	
	@Override
    public void onBindViewHolder(@NonNull ModViewHolder viewHolder, int position) {
        ModPropertiesModel modProp = data.get(position);

        if (modProp.getModName() != null) viewHolder.nameTextView.setText(applyFancyTexts(modProp.getModName()));
		else viewHolder.nameTextView.setText("Unknown");
		
        if (modProp.getModDescription() != null) viewHolder.descTextView.setText(applyFancyTexts(modProp.getModDescription()));
		else viewHolder.descTextView.setText("Unknown");
		
        if (modProp.getModVersion() != null) viewHolder.versionTextView.setText(applyFancyTexts(modProp.getModVersion()));
        else viewHolder.versionTextView.setText("Unknown");

        if (modProp.getModAuthor() != null) viewHolder.authorTextView.setText(applyFancyTexts(modProp.getModAuthor()));
        else viewHolder.authorTextView.setText("Unknown");
		
        if (modProp.getModPreview() != null) BitmapUtils.loadBitmapAsync(viewHolder.previewView, Uri.parse(modProp.getModPreview()));
		else viewHolder.previewView.setImageBitmap(BitmapUtils.getRescaledDrawable(R.drawable.ic_info));
    }
	
	@Override
    public int getItemCount() {
        return data.size();
    }
    
    private final Spanned applyFancyTexts(String text) {
        return HtmlCompat.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
    }
}

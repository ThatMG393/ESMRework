package com.thatmg393.esmanager.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.adapters.base.IBaseRecyclerAdapter;
import com.thatmg393.esmanager.interfaces.IOnRecyclerItemClickListener;
import com.thatmg393.esmanager.models.ModPropertiesModel;
import com.thatmg393.esmanager.utils.BitmapUtils;
import com.thatmg393.esmanager.viewholders.mod.ModViewHolder;

import java.util.ArrayList;

public class ModListAdapter extends RecyclerView.Adapter<ModViewHolder> implements IBaseRecyclerAdapter<ModPropertiesModel> {
	private final Context context;
	private final ArrayList<ModPropertiesModel> data;
	
	private IOnRecyclerItemClickListener itemClickListener;
	
	public ModListAdapter(@NonNull Context context, ArrayList<ModPropertiesModel> data) {
		this.context = context;
		this.data = data;
	}
	
	@Override
	public void addData(ModPropertiesModel data) {
		if (data != null) {
			this.data.add(data);
			notifyItemInserted(this.data.size());
		}
	}
	
	@Override
	public void updateData(ArrayList<ModPropertiesModel> data) {
		if (data != null && data.size() > 0) {
			this.data.clear();
			this.data.addAll(data);
			notifyItemRangeChanged(0, this.data.size());
		}
	}
	
	@Override
	public void clearData() {
		int dataSize = data.size();
		if (data != null && dataSize > 0) {
			data.clear();
			notifyItemRangeRemoved(0, dataSize);
		}
	}
	
	@Override
	public ArrayList<ModPropertiesModel> getDataList() {
		return this.data;
	}
	
	@Override
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
		ModPropertiesModel modProp = (ModPropertiesModel) data.get(position);
			
		String modName = modProp.getModName();
		String modDesc = modProp.getModDescription();
		String modVersion = modProp.getModVersion();
		String modAuthor = modProp.getModAuthor();
		String modPreview = modProp.getModPreview();
		
		if (modName != null) viewHolder.nameTextView.setText(applyFancyTexts(modName));
		else viewHolder.nameTextView.setText("Unknown");
		
		if (modDesc != null) viewHolder.descTextView.setText(applyFancyTexts(modDesc));
		else viewHolder.descTextView.setText("Unknown");
		
		if (modVersion != null) viewHolder.versionTextView.setText(applyFancyTexts(modVersion));
		else viewHolder.versionTextView.setText("0.0.0");

		if (modAuthor != null) viewHolder.authorTextView.setText(applyFancyTexts(modAuthor));
		else viewHolder.authorTextView.setText("Unknown");
		
		if (modPreview != null) {
			Picasso.get()
				.load(Uri.parse(modPreview))
				.placeholder(R.drawable.ic_import)
				.error(R.drawable.ic_info)
				.resize(BitmapUtils.DEFAULT_RESIZE_KEEPASPECT[0], BitmapUtils.DEFAULT_RESIZE_KEEPASPECT[1])
				.into(viewHolder.previewView);
		} else {
			Picasso.get()
				.load(R.drawable.ic_info)
				.resize(BitmapUtils.DEFAULT_RESIZE_KEEPASPECT[0], BitmapUtils.DEFAULT_RESIZE_KEEPASPECT[1])
				.into(viewHolder.previewView);
		}
	}
	
	@Override
	public int getItemCount() {
		return data.size();
	}
	
	private final Spanned applyFancyTexts(String text) {
		return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT);
	}
}

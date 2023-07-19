package com.thatmg393.esmanager.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.models.ModPropertiesModel;

import java.util.List;

public class ModListAdapter extends ArrayAdapter<ModPropertiesModel> {
    private final Context context;
    private final List<ModPropertiesModel> dataList;

    public ModListAdapter(@NonNull Context context, List<ModPropertiesModel> dataList) {
        super(context, R.layout.mod_list_layout, dataList);

        this.context = context;
        this.dataList = dataList;
    }
	
	public void addData(ModPropertiesModel data) {
        if (data != null) {
            this.dataList.add(data);
        }
        notifyDataSetChanged();
    }

    public void updateData(List<ModPropertiesModel> dataList) {
        if (dataList != null && dataList.size() > 0) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
		if (dataList != null && dataList.size() > 0) dataList.clear();
		notifyDataSetInvalidated();
	}

	public List<ModPropertiesModel> getDataList() {
		return dataList;
	}

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) { convertView = LayoutInflater.from(context).inflate(R.layout.mod_list_layout, parent, false); }
		
		ShapeableImageView previewView = convertView.findViewById(R.id.mod_list_layout_preview);
		MaterialTextView nameTextView = convertView.findViewById(R.id.mod_list_layout_name);
		MaterialTextView descTextView = convertView.findViewById(R.id.mod_list_layout_description);
		MaterialTextView versionTextView = convertView.findViewById(R.id.mod_list_layout_version);
		MaterialTextView authorTextView = convertView.findViewById(R.id.mod_list_layout_author);
		
		if (dataList == null || dataList.size() < 0) {
			previewView.setVisibility(View.GONE);
			versionTextView.setVisibility(View.GONE);
			authorTextView.setVisibility(View.GONE);
			
			nameTextView.setText("No mod/s found!");
			descTextView.setText("Please download some mods!");
			
			return convertView;
		}
		
		ModPropertiesModel tmp = dataList.get(position);
		
		nameTextView.setText(applyFancyTexts(tmp.getModName()));
		descTextView.setText(applyFancyTexts(tmp.getModDescription()));
		versionTextView.setText(applyFancyTexts(tmp.getModVersion()));
		authorTextView.setText(applyFancyTexts(tmp.getModAuthor()));
		previewView.setImageBitmap(BitmapFactory.decodeFile(tmp.getModPreview()));
		
        return convertView;
    }
	
	private final Spanned applyFancyTexts(String text) {
		return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
	}
}

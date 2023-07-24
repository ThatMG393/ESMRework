package com.thatmg393.esmanager.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.models.ModPropertiesModel;

import com.thatmg393.esmanager.utils.BitmapUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModListAdapter extends ArrayAdapter<ModPropertiesModel> {
    private final Context context;
    private final ArrayList<ModPropertiesModel> dataList;

    public ModListAdapter(@NonNull Context context, ArrayList<ModPropertiesModel> dataList) {
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

    public ArrayList<ModPropertiesModel> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.mod_list_layout, parent, false);

        ShapeableImageView previewView = convertView.findViewById(R.id.mod_list_layout_preview);
        MaterialTextView nameTextView = convertView.findViewById(R.id.mod_list_layout_name);
        MaterialTextView descTextView = convertView.findViewById(R.id.mod_list_layout_description);
        MaterialTextView versionTextView = convertView.findViewById(R.id.mod_list_layout_version);
        MaterialTextView authorTextView = convertView.findViewById(R.id.mod_list_layout_author);

        ModPropertiesModel modProp = dataList.get(position);

        if (modProp.getModName() != null) nameTextView.setText(applyFancyTexts(modProp.getModName()));
		else nameTextView.setText("Unknown");
		
        if (modProp.getModDescription() != null) descTextView.setText(applyFancyTexts(modProp.getModDescription()));
		else descTextView.setText("Unknown");
		
        if (modProp.getModVersion() != null) versionTextView.setText(applyFancyTexts(modProp.getModVersion()));
        else versionTextView.setText("Unknown");

        if (modProp.getModAuthor() != null) authorTextView.setText(applyFancyTexts(modProp.getModAuthor()));
        else authorTextView.setText("Unknown");

        try {
            if (modProp.getModPreview() != null) {
                previewView.setImageBitmap(BitmapUtils.getRescaledBitmap(Uri.parse(modProp.getModPreview())));
            } else {
                previewView.setImageBitmap(BitmapUtils.getRescaledDrawable(R.drawable.ic_info));
			}
        } catch (IOException e) {
			e.printStackTrace();
            previewView.setImageBitmap(BitmapUtils.getRescaledDrawable(R.drawable.ic_info));
        }
		
        return convertView;
    }

    private final Spanned applyFancyTexts(String text) {
        return HtmlCompat.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
    }
}

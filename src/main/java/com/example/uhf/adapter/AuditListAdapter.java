package com.example.uhf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.Utilities.Tagging;

import java.util.List;

/**
 * Created by SATYENDRA on 4/7/2019.
 */

public class AuditListAdapter extends ArrayAdapter<Tagging> {
    private List<Tagging> taggingList;
    private Context context;


    public AuditListAdapter(List<Tagging> taggingList, Context context) {
        super(context, R.layout.audit_list_items, taggingList);
        this.context = context;
        this.taggingList = taggingList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.audit_list_items, null, true);
        TextView tvItemId = (TextView) view.findViewById(R.id.tvItemId);
        TextView textTitle = (TextView)view.findViewById(R.id.textTitle);
        TextView textLocation = (TextView)view.findViewById(R.id.textLocation);
        //ImageView tagLoc = (ImageView) view.findViewById(R.id.tagLoc);
        //tagLoc.setVisibility(View.GONE);
        Tagging tagging = taggingList.get(position);
        tvItemId.setText(tagging.getItem_id());
        textTitle.setText(tagging.getTitle());
        textLocation.setText(tagging.getInv_location());
        return view;
    }
}

package com.example.uhf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.Utilities.Tagging;

import java.util.List;

public class ListClickAdapter extends ArrayAdapter<Tagging> {

    private List<Tagging> taggingList;
    private Context context;


    public ListClickAdapter(List<Tagging> taggingList, Context context) {
        super(context, R.layout.list_click_items, taggingList);
        this.context = context;
        this.taggingList = taggingList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_click_items, null, true);
        TextView tvAssetCode = (TextView) view.findViewById(R.id.tvAssetCode);
        TextView tvDateOfPurchase = (TextView)view.findViewById(R.id.tvDateOfPurchase);
        TextView tvDescription = (TextView)view.findViewById(R.id.tvDescription);
        TextView tvSerialNo = (TextView)view.findViewById(R.id.tvSerialNo);
        TextView tvLocation = (TextView)view.findViewById(R.id.tvLocation);
        TextView tvAssetType = (TextView)view.findViewById(R.id.tvAssetType);
        TextView tvInvStatus = (TextView)view.findViewById(R.id.tvInvStatus);
        TextView tvInvLocation = (TextView)view.findViewById(R.id.tvInvLocation);
        Tagging tagging = taggingList.get(position);
        tvAssetCode.setText(tagging.getItem_id());
        tvDateOfPurchase.setText(tagging.getPurchase_date());
        tvDescription.setText(tagging.getTitle());
        tvSerialNo.setText(tagging.getSerial_number());
        tvLocation.setText(tagging.getLocation_name());
        tvAssetType.setText(tagging.getCategory_name());
        tvInvStatus.setText(tagging.getInv_status());
        tvInvLocation.setText(tagging.getInv_location_name());
        return view;
    }
}

package com.example.uhf.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.uhf.R;
import com.example.uhf.Utilities.ServerUrls;
import com.example.uhf.Utilities.Tagging;
import com.example.uhf.Utilities.VolleySingleton;
import com.example.uhf.adapter.AuditListAdapter;
import com.example.uhf.adapter.ListClickAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.lidroid.xutils.view.ResLoader.getColor;

public class AuditActivity extends Activity {

    private ArrayList<HashMap<String, String>> tagList;
    private ArrayAdapter adapter;

    private LinearLayout layoutInventory;
    private RadioGroup RgInventory;
    private RadioButton RbAuditable;
    private RadioButton RbMissing;
    private RadioButton RbMislocated;
    private Button btPhyUpdate;
    private Button BtClear;
    private ListView LvTags;
    private AuditListAdapter auditListAdapter;
    private LinkedList<Tagging> auditableEqptList = new LinkedList<>();
    private LinkedList<Tagging> missingEqptList;
    private LinkedList<Tagging> mislocatedEqptList;
    ServerUrls serverUrls;
    private TextView tv_count;
    private LinkedHashMap<String, String> updatedEquipmentList = new LinkedHashMap<String, String>();
    private AlertDialog dialog;


//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActionBar actionBar = (ActionBar) getActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.banner));
        actionBar.setTitle("");
        actionBar.setIcon(android.R.color.transparent);
        serverUrls = new ServerUrls();
        tagList = new ArrayList<HashMap<String, String>>();
        LvTags = (ListView)findViewById(R.id.LvTags);
        tv_count = (TextView)findViewById(R.id.tv_count);
        layoutInventory = (LinearLayout)findViewById(R.id.layoutInventory);

        RgInventory = (RadioGroup)findViewById(R.id.RgInventory);
        RbAuditable = (RadioButton)findViewById(R.id.RbAuditable);
        RbMissing = (RadioButton) findViewById(R.id.RbMissing);
        RbMislocated = (RadioButton)findViewById(R.id.RbMislocated);

        btPhyUpdate = (Button)findViewById(R.id.btPhyUpdate);
        BtClear = (Button)findViewById(R.id.BtClear);
        RbAuditable.setChecked(true);
        auditedValidateConnection();

        RbAuditable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (layoutInventory.getVisibility()==View.GONE) {
                    layoutInventory.setVisibility(View.VISIBLE);
                }*/
                layoutInventory.setVisibility(View.GONE);
                auditedValidateConnection();

            }
        });
        RbMissing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutInventory.setVisibility(View.GONE);
                missingValidateConnection();

            }
        });
        RbMislocated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutInventory.setVisibility(View.GONE);
                misLocatedValidateConnection();

            }
        });
        btPhyUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RbAuditable.isChecked())
                {
                    if (auditableEqptList.size()>0)
                    {
                        physicallyUpdateEquipments(auditableEqptList);
                    }
                    else
                    {
                        Toast.makeText(AuditActivity.this, "Nothing to update", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(AuditActivity.this, "Please select audited", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //ValidateConnection(position);
            }
        });

    }

    public void getAuditedItemData(final String item_id) {

        final List<Tagging> auditableTagEqptList = new LinkedList<Tagging>();
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_AUDITED_EQPT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null)
                        {
                            pDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    Tagging tagging = new Tagging();

                                    tagging.setItem_id(jo.getString("item_id"));
                                    tagging.setInv_status(jo.getString("inv_status"));
                                    //tagging.setPurchase_date(jo.getString("date_of_purchase"));
                                    tagging.setTitle(jo.getString("title"));
                                    //tagging.setSerial_number(jo.getString("serial_no"));
                                    //tagging.setLocation_name(jo.getString("location_name"));
                                    // tagging.setCategory_name(jo.getString("category_name"));
                                    //tagging.setInv_location_name(jo.getString("inv_location_name"));

                                    auditableTagEqptList.add(tagging);

                                }
                                 showDialog(item_id, auditableTagEqptList);

                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());}
                        }
                        else
                        {
                            Log.e(TAG, "Couldn't get json from server.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap();
                params.put("eqpt_no", item_id);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    private void physicallyUpdateEquipments(LinkedList<Tagging> auditableEqptList) {
        for (Tagging eqptNo : auditableEqptList)
        {
            String[] eqptArray = {eqptNo.getItem_id()};
            updateEquipmentPhysically(eqptArray);
        }
    }

    private void updateEquipmentPhysically(final String[] eqptArray) {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        //pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_PHY_UPDATE_EQPT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null)
                        {
                            //pDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {

                                    JSONObject jo = jsonarray.getJSONObject(i);

                                    if(jo.getString("update_status").equalsIgnoreCase("Updated")){
                                        updatedEquipmentList.put(jo.getString("item_id"), jo.getString("update_status"));
                                    }
                                }
                                Log.d("UPDATED", "updatedEquipmentList: " +updatedEquipmentList.toString());

                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());}
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap();
                params.put("item_id", eqptArray[0]);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    private void getMislocatedData() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setVisibility(View.VISIBLE);
        mislocatedEqptList = new LinkedList<Tagging>();
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_MISLOCATED_EQPT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null)
                        {
                            //progressBar.setVisibility(View.GONE);
                            pDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    Tagging tagging = new Tagging();
                                    tagging.setItem_id(jo.getString("item_id"));
                                    tagging.setTitle(jo.getString("title"));
                                    tagging.setInv_location(jo.getString("location_name"));
                                    mislocatedEqptList.add(tagging);
                                }
                                auditListAdapter = new AuditListAdapter(mislocatedEqptList,getApplicationContext());
                                LvTags.setAdapter(auditListAdapter);
                                tv_count.setText("("+mislocatedEqptList.size()+")");
                                //Log.d("MISLOCATED", "mislocatedEqptList: " +mislocatedEqptList.toString());

                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());}
                        }
                        else
                        {
                            Log.e(TAG, "Couldn't get json from server.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            //send params from here if required
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    private void getMissingData() {

        missingEqptList = new LinkedList<Tagging>();
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_MISSING_EQPT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null)
                        {
                            pDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    Tagging tagging = new Tagging();
                                    tagging.setItem_id(jo.getString("item_id"));
                                    tagging.setInv_location(jo.getString("location_name"));
                                    tagging.setTitle(jo.getString("title"));
                                    missingEqptList.add(tagging);
                                }
                                auditListAdapter = new AuditListAdapter(missingEqptList,getApplicationContext());
                                LvTags.setAdapter(auditListAdapter);
                                tv_count.setText("("+missingEqptList.size()+")");
                                //Log.d("MISSING", "missingEqptList: " +missingEqptList.toString());

                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());}
                        }
                        else
                        {
                            Log.e(TAG, "Couldn't get json from server.");
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            //send params from here
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    private void getAuditableData() {

        auditableEqptList = new LinkedList<Tagging>();
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_AUDITABLE_EQPT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null)
                        {
                            pDialog.dismiss();
                            try
                            {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    Tagging tagging = new Tagging();
                                    tagging.setItem_id(jo.getString("item_id"));
                                    tagging.setInv_location(jo.getString("location_name"));
                                    tagging.setTitle(jo.getString("title"));
                                    auditableEqptList.add(tagging);
                                }
                                auditListAdapter = new AuditListAdapter(auditableEqptList,getApplicationContext());
                                LvTags.setAdapter(auditListAdapter);
                                tv_count.setText("("+auditableEqptList.size()+")");
                                //Log.d("AUDITABLE", "auditableEqptList: " +auditableEqptList.toString());

                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                            }

                        }
                        else
                        {
                            Log.e(TAG, "Couldn't get json from server.");
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            //Send params from here.....
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);

    }
    public void showDialog(String tagSubItem, List<Tagging> listSubItem)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        //alertDialog.setTitle("Hello");
        TextView title = new TextView(this);
        //title.setTextColor(getColor(this, android.R.color.holo_red_dark));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        title.setPadding(0,30,0,30);
        title.setLayoutParams(lp);
        title.setText(tagSubItem);
        title.setGravity(Gravity.CENTER);
        alertDialog.setCustomTitle(title);

        alertDialog.setIcon(R.drawable.rfid_logo);
        //alertDialog.setMessage("tag id found");
        LayoutInflater layoutInflater = this.getLayoutInflater();
        View view_sub_item = layoutInflater.inflate(R.layout.dialog_content,null);

        ListView listDialog = (ListView) view_sub_item.findViewById(R.id.listDialog);

        ListClickAdapter listClickAdapter = new ListClickAdapter(listSubItem, this);
        listDialog.setAdapter(listClickAdapter);
        alertDialog.setView(view_sub_item);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


            }
        });
        alertDialog.setNegativeButton("Total Items : "+String.valueOf(listSubItem.size()), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }
    private void auditedValidateConnection() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_APP_CONNECTION,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("kya",""+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("response");
                            if (result.equalsIgnoreCase("ok"))
                            {
                                /*if (RbAuditable.isChecked())
                                {
                                    //auditableTagEqptList = new LinkedList<Tagging>();
                                    Tagging eqptNo = auditListAdapter.getItem(position);
                                    getAuditedItemData(eqptNo.getItem_id());
                                    //Toast.makeText(AuditActivity.this, ""+eqptNo.getItem_id(), Toast.LENGTH_SHORT).show();
                                }
                                if (RbMissing.isChecked())
                                {
                                    //auditableTagEqptList = new LinkedList<Tagging>();
                                    Tagging eqptNo = auditListAdapter.getItem(position);
                                    getAuditedItemData(eqptNo.getItem_id());
                                    //Toast.makeText(AuditActivity.this, ""+eqptNo.get(0).getItem_id(), Toast.LENGTH_SHORT).show();
                                }
                                if(RbMislocated.isChecked())
                                {
                                    //auditableTagEqptList = new LinkedList<Tagging>();
                                    Tagging eqptNo = auditListAdapter.getItem(position);
                                    getAuditedItemData(eqptNo.getItem_id());
                                    //Toast.makeText(AuditActivity.this, ""+eqptNo.get(0).getItem_id(), Toast.LENGTH_SHORT).show();
                                }*/
                                getAuditableData();
                            }
                            else
                            {
                                //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(AuditActivity.this).addToRequestQue(stringRequest);
    }
    private void missingValidateConnection() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_APP_CONNECTION,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("kya",""+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("response");
                            if (result.equalsIgnoreCase("ok"))
                            {
                                getMissingData();
                            }
                            else
                            {
                               // Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(AuditActivity.this).addToRequestQue(stringRequest);
    }

    private void misLocatedValidateConnection() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_APP_CONNECTION,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("kya",""+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("response");
                            if (result.equalsIgnoreCase("ok"))
                            {
                                getMislocatedData();
                            }
                            else
                            {
                                //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(AuditActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(AuditActivity.this).addToRequestQue(stringRequest);
    }
    public void showServerErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AuditActivity.this);
        View view = LayoutInflater.from(AuditActivity.this).inflate(R.layout.network_error_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);
        TextView textMessage = (TextView) view.findViewById(R.id.textMessage);

        Button okBtn = (Button) view.findViewById(R.id.okBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteOrderFromCSCart(orderId, dialog);
                dialog.dismiss();

            }
        });

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();
    }
}

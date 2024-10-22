package com.example.uhf.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
//import android.support.v7.app.ActionBarActivity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.lidroid.xutils.view.ResLoader.getColor;

public class ReportActivity extends Activity {

    ListView LvTags;
    Button btFetchData;
    TextView tv_count;
    List<Tagging> allTaggingList = new ArrayList<Tagging>();
    List<String> allTaggingLocationList = new ArrayList<String>();
    List<String> allTaggingLocationNameList = new ArrayList<String>();
    Spinner spAllTaggingLocations;
    ServerUrls serverUrls;
    ArrayAdapter adapterLocation;
    AuditListAdapter auditListAdapter;
    private Dialog dialog;
    private int loc_index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setContentView(R.layout.activity_report);
        ActionBar actionBar = (ActionBar) getActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.banner));
        actionBar.setTitle("");
        actionBar.setIcon(android.R.color.transparent);
        serverUrls = new ServerUrls();
        spAllTaggingLocations = (Spinner)findViewById(R.id.spAllTaggingLocations);
        LvTags = (ListView)findViewById(R.id.LvTags);
        btFetchData = (Button)findViewById(R.id.btFetchData);
        tv_count = (TextView)findViewById(R.id.tv_count);
        ValidateConnection();

        btFetchData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (allTaggingLocationList.size() > 0) {
                    String sp_location = spAllTaggingLocations.getSelectedItem().toString();
                    if (loc_index != -1) {
                        ValidateConnectionFetchData(allTaggingLocationList.get(loc_index));
                    }else {
                        ValidateConnectionFetchData("All Locations");
                    }

                /*}else {
                    Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        LvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*Tagging eqptNo = auditListAdapter.getItem(position);
                getAuditableItemData(eqptNo.getItem_id());*/
            }
        });
    }

    private void getAllTaggingData(final String sp_location) {
        allTaggingList = new LinkedList<Tagging>();
        final ProgressDialog mypDialog = new ProgressDialog(this);
        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mypDialog.setMessage("Please Wait...");
        mypDialog.setCanceledOnTouchOutside(false);
        mypDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_ALL_TAGGED_ASSET,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mypDialog.dismiss();
                        if (response != null) {
                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");

                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    Tagging tagging = new Tagging();
                                    tagging.setItem_id(jo.getString("item_id"));
                                    tagging.setInv_location(jo.getString("location_name"));
                                    tagging.setTitle(jo.getString("title"));
                                    allTaggingList.add(tagging);
                                }
                                //Log.d("REPORT","RCBDEMO"+allTaggingList);
                                //Toast.makeText(ReportActivity.this, ""+allTaggingList.get(0).getItem_id(), Toast.LENGTH_SHORT).show();
                                auditListAdapter = new AuditListAdapter(allTaggingList, getApplicationContext());
                                LvTags.setAdapter(auditListAdapter);
                                tv_count.setText("("+allTaggingList.size()+")");

                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());}
                        } else {
                            Log.e(TAG, "Couldn't get json from server.");

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            //send params
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("location_id", sp_location);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);

    }

    private void getAllLocation() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_AUDIT_LOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        allTaggingLocationList = new ArrayList<String>();
                        allTaggingLocationNameList = new ArrayList<String>();


                        if (response != null) {
                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");

                                for (int i = 0; i < jsonarray.length(); i++) {

                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    allTaggingLocationList.add(jo.getString("location_id"));
                                    allTaggingLocationNameList.add(jo.getString("location_name"));


                                }
                                allTaggingLocationNameList.add(0,"All Locations");
                                if (!allTaggingLocationNameList.contains("All Locations") == true) {

                                    adapterLocation = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, allTaggingLocationList);
                                    adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spAllTaggingLocations.setAdapter(adapterLocation);
                                    spAllTaggingLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            loc_index = i-1;
                                            Log.e("Unilever Location","*** "+loc_index);
                                            //Toast.makeText(mContext, auditLocationList.get(i), Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                            return;
                                        }
                                    });
                                    //** End Audit Location Selector ** //
                                }
                                else
                                {
                                    adapterLocation = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, allTaggingLocationNameList);
                                    adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spAllTaggingLocations.setAdapter(adapterLocation);
                                    spAllTaggingLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                           loc_index = i-1;
                                           Log.e("Unilever Location","* "+loc_index);
                                            //Toast.makeText(mContext, auditLocationList.get(i), Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                            return;
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());}
                        } else {
                            Log.e(TAG, "Couldn't get json from server.");

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            //send params
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    public void getAuditableItemData(final String item_id) {

        final List<Tagging> auditableTagEqptList = new LinkedList<Tagging>();
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_AUDITABLE_SUB_ITEM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null)
                        {
                            pDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                Log.e("kya","**"+jsonarray);
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

        //alertDialog.setIcon(R.drawable.tini_trans);
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

    private void ValidateConnection() {

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
                                getAllLocation();
                            }
                            else
                            {
                                //Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(ReportActivity.this).addToRequestQue(stringRequest);
    }

    private void ValidateConnectionFetchData(final String sp_location) {

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
                                if (sp_location !=null) {
                                    getAllTaggingData(sp_location);
                                }else {
                                    Toast.makeText(ReportActivity.this, "Please enter location first", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                //Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(ReportActivity.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(ReportActivity.this).addToRequestQue(stringRequest);
    }
    public void showServerErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
        View view = LayoutInflater.from(ReportActivity.this).inflate(R.layout.network_error_dialog, null);
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

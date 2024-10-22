package com.example.uhf.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.uhf.R;
import com.example.uhf.Utilities.Common;
import com.example.uhf.Utilities.ServerUrls;
import com.example.uhf.Utilities.Tagging;
import com.example.uhf.Utilities.VolleySingleton;
import com.example.uhf.activity.AuditActivity;
import com.example.uhf.activity.Home;
import com.example.uhf.activity.Login;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.adapter.AuditListAdapter;
import com.example.uhf.adapter.ListClickAdapter;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.ISO15693Entity;
import com.rscja.deviceapi.exception.RFIDReadFailureException;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.pda.serialport.Tools;

import static android.content.ContentValues.TAG;
import static android.content.Context.AUDIO_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.lidroid.xutils.view.ResLoader.getColor;

public class UHFInventoryFragment extends KeyDwonFragment{

    private boolean loopFlag = false;
    private int inventoryFlag = 0;
    private LinearLayout llQValue;
    private UHFMainActivity mContext;
    Handler handler;
    Handler searchHandler;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    private RFIDWithUHFUART mRFID;

    Button BtClear;
    TextView tv_count;
    RadioGroup RgInventory;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;
    //RadioButton RbInventoryAnti;
    Spinner spAuditLocations;
    Spinner SpinnerQ;
    Button btUpdate;
    Button btSearch;
    ListView LvTags;
    ListView listDialog;

    byte initQ;
    private LinearLayout llContinuous;

    private HashMap<String, String> map;
    ServerUrls serverUrls;

    LinkedList<String> auditLocationList = new LinkedList<String>();
    List<String> auditLocationNameList = new ArrayList<String>();
    private int loc_index = -1;


    private ArrayAdapter adapterLocation;


    Tagging tagging = new Tagging();
    LinkedList<LinkedHashMap<String, Tagging>> taggingMapList = new LinkedList<LinkedHashMap<String, Tagging>>();
    LinkedHashMap<String, Tagging> accNoHashMap = new LinkedHashMap<String, Tagging>();
    LinkedHashMap<String, Tagging> tagIdHashMap = new LinkedHashMap<String, Tagging>();
    LinkedHashSet<String> scannedItemSet = new LinkedHashSet<String>();
    Map<String, LinkedHashSet> locationWiseScannedSetMap = new HashMap<String, LinkedHashSet>();
    HashMap<String, String> scanTagSubItemListMap;
    ArrayList<HashMap<String, String>> scanTagSubItemList;
    int locationFlag = 0;
    AuditListAdapter auditListAdapter;
    private Dialog dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");

        return inflater
                .inflate(R.layout.uhf_inventory_fragment, container, false);

    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        serverUrls = new ServerUrls();
        initSound();
        //IntentFilter filter = new IntentFilter();
        //filter.addAction("android.rfid.FUN_KEY");
        //requireContext().registerReceiver(keyReceiver, filter);


        mContext = (UHFMainActivity) getActivity();

        tagList = new ArrayList<HashMap<String, String>>();

        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);
        RbInventorySingle = (RadioButton) getView()
                .findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) getView()
                .findViewById(R.id.RbInventoryLoop);

        SpinnerQ = (Spinner) getView().findViewById(R.id.SpinnerQ);
        spAuditLocations = (Spinner) getView().findViewById(R.id.spAuditLocations);
        btUpdate = (Button) getView().findViewById(R.id.btUpdate);
        btSearch = (Button) getView().findViewById(R.id.btSearch);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[] { "tagUii", "title", "location", "status" },
                new int[] { R.id.TvTagUii, R.id.TvTitle, R.id.TvLocation, R.id.tagLoc });


        BtClear.setOnClickListener(new BtClearClickListener());
        RgInventory
                .setOnCheckedChangeListener(new RgInventoryCheckedListener());

        SpinnerQ.setEnabled(false);
        SpinnerQ.setOnItemSelectedListener(new QItemSelectedListener());

        llQValue = (LinearLayout) getView().findViewById(R.id.llQValue);


        LvTags.setAdapter(adapter);
        clearData();

        Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());

        searchHandler =new Handler() {

            @Override
            public void handleMessage(Message msg)
            {

                //ISO15693Entity entity = (ISO15693Entity) msg.obj;
                String result = msg.obj + "";
                //addEPCToList(entity.getId(), "Success", 0);
                //Log.e("kya", ""+entity.getId());


                String[] strs = result.split("@");
                if (strs[3].equals("success"))
                    addEPCToList(strs[0], strs[1], strs[2],"0");
                else
                    addEPCToList(strs[0], strs[1], strs[2],"1");

                scannedItemSet.add(strs[0].toString());
                //locationWiseScannedSetMap.put(spAuditLocations.getText().toString(), scannedItemSet);
                locationWiseScannedSetMap.put(spAuditLocations.getSelectedItem().toString(), scannedItemSet);


            }
        };



        SpinnerQ.setSelection(3);

        generateTaggingMapList();
        generateAuditLocationList();

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (spAuditLocations.getSelectedItem().toString().equalsIgnoreCase("Select Location"))
                {
                    Toast.makeText(mContext, "Please Select the Location", Toast.LENGTH_SHORT).show();
                }
                else
                {
                   ValidateConnection();
                }

            }
        });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locationWiseScannedSetMap.isEmpty()){
                    int result =0;
                    LinkedHashSet<String> entityList = locationWiseScannedSetMap.get(spAuditLocations.getSelectedItem().toString());
                    List<Tagging> newTaggingList = new ArrayList<Tagging>();
                    Iterator<String> itr = entityList.iterator();
                    while (itr.hasNext()) {
                        String eNTITY = itr.next();
                        if (accNoHashMap.containsKey(eNTITY)) {
                            tagging = new Tagging();
                            tagging.setTag_id(accNoHashMap.get(eNTITY).getTag_id());
                            tagging.setInv_status("Found");
                            tagging.setInv_location(spAuditLocations.getSelectedItem().toString());
                            tagging.setUpdated(true);
                            //tagging.setEmp_id(Integer.parseInt(Common.USER_ID));
                            newTaggingList.add(tagging);
                            //Log.e("kya",""+Integer.parseInt(Common.USER_ID));
                        }
                    }
                    result = updateInventory(newTaggingList);
                    UpdateInventoryValidateConnection(newTaggingList);
                    if (result>0) {
                        //Toast.makeText(mContext, ""+newTaggingList.size()+" Items updated in " +spAuditLocations.getSelectedItem().toString()+" location", Toast.LENGTH_SHORT).show();
                        showAlertDialog(newTaggingList.size(), spAuditLocations.getSelectedItem().toString());
                        scannedItemSet.clear();
                        locationWiseScannedSetMap.clear();
                        spAuditLocations.setEnabled(true);
                    } else {
                        spAuditLocations.setEnabled(true);
                        Toast.makeText(mContext, "No items scanned, Please scan and Update Inventory.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    spAuditLocations.setEnabled(true);
                    Toast.makeText(mContext, "No items scanned, Please scan and Update Inventory.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*String selectedItem = adapter.getItem(position).toString();
                String[] strArr = {selectedItem,""};
                String[] strArr1 = strArr[0].split("tagUii");
                String[] strArr2 = strArr1[1].split("=");
                String strArr3 = strArr2[1].replace("}","");
                String clickedItem = strArr3;
                scanTagSubItem(clickedItem);*/
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        generateTaggingMapList();
    }

    private void scanTagSubItem(final String clickedItem) {
        final List<Tagging> listSubItem = new ArrayList<Tagging>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_ASSET_SUBITEM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null) {
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
                                    listSubItem.add(tagging);
                                }

                                showDialog(clickedItem, listSubItem);


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
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap();
                params.put("item_id", clickedItem);

                return params;
            }
        };
        VolleySingleton.getInstance(mContext).addToRequestQue(stringRequest);
    }




    @Override
    public void onPause() {
        Log.e("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        stopInventory();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("MY", "UHFReadTagFragment.onPause");
        stopInventory();
    }

    private void addEPCToList(String epc, String rssi, int i) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);

            map = new HashMap<String, String>();

            map.put("tagUii", epc);
            map.put("tagCount", String.valueOf(1));
            map.put("status", String.valueOf(i));
            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(
                        tagList.get(index).get("tagCount"), 10) + 1;

                map.put("tagCount", String.valueOf(tagcount));
                if (map.containsValue("0"))
                    map.put("status", String.valueOf(R.drawable.checkmark));
                else
                    map.put("status", String.valueOf(R.drawable.delete));

                tagList.set(index, map);

            }

            adapter.notifyDataSetChanged();

        }
    }

    private void addEPCToList(String accNo,String title, String location, String i) {
        if (!TextUtils.isEmpty(accNo)) {
            int index = checkIsExist(accNo);
            map = new HashMap<String, String>();
            map.put("tagUii", accNo);
            map.put("title", title);
            map.put("location", location);
            if (i.equals("0"))
                map.put("status", String.valueOf(R.drawable.checkmark));
            else
                map.put("status", String.valueOf(R.drawable.delete));
            //map.put("status", String.valueOf(i));
            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("("+adapter.getCount()+")");
            } else {
                /*int tagcount = Integer.parseInt(
                        tagList.get(index).get("tagCount"), 10) + 1;*/

                //map.put("tagCount", String.valueOf(tagcount));

                tagList.set(index, map);

            }

            adapter.notifyDataSetChanged();

        }
    }

    public class BtClearClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
            generateAuditLocationList();

        }
    }

    private void clearData() {
        tv_count.setText("0");
        spAuditLocations.setEnabled(true);
        tagList.clear();
        locationWiseScannedSetMap.clear();


        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
    }


    public class RgInventoryCheckedListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            llQValue.setVisibility(GONE);
            llContinuous.setVisibility(GONE);

            if (checkedId == RbInventorySingle.getId()) {

                inventoryFlag = 0;
                SpinnerQ.setEnabled(false);
                llContinuous.setVisibility(VISIBLE);
            }
            else {

                inventoryFlag = 0;
                SpinnerQ.setEnabled(false);

                llContinuous.setVisibility(VISIBLE);

            }
        }
    }

    public class QItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            initQ = Byte.valueOf((String) SpinnerQ.getSelectedItem(), 10);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }



    public void generateTaggingMapList(){
        //new ReadTaggingData().execute();
        accNoHashMap = new LinkedHashMap<String, Tagging>();
        tagIdHashMap = new LinkedHashMap<String, Tagging>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_ALL_TAG_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    Tagging missingTaggingListObj = new Tagging();
                                    JSONObject jo = jsonarray.getJSONObject(i);

                                    missingTaggingListObj.setItem_id(jo.getString("item_id"));
                                    missingTaggingListObj.setTag_id(jo.getString("tag_id"));
                                    missingTaggingListObj.setTitle(jo.getString("title"));
                                    missingTaggingListObj.setLocation_id(jo.getString("location_id"));
                                    missingTaggingListObj.setLocation_name(jo.getString("location_name"));

                                    accNoHashMap.put(jo.getString("item_id"), missingTaggingListObj);
                                    tagIdHashMap.put(jo.getString("tag_id"), missingTaggingListObj);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                            }
                        }
                        else
                        {
                            Toast.makeText(mContext, "There is no data available", Toast.LENGTH_SHORT).show();
                            //showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data

        };
        VolleySingleton.getInstance(mContext).addToRequestQue(stringRequest);

    }

    public int updateInventory(List<Tagging> newTaggingList) {
        String msg = "";
        int counter = 0;
        for (Tagging taggingg : newTaggingList) {
            //String[] strArray = {taggingg.getTag_id(),taggingg.getInv_location()};
            //msg=new InventoryLocationUpdate().execute(strArray).toString();
            updateInventoryLocation(taggingg.getTag_id(), taggingg.getInv_location());
            counter++;
        }
        return counter;
    }
    private void UpdateInventoryValidateConnection(final List<Tagging> newTaggingList) {

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
                                updateInventory(newTaggingList);
                            }
                            else
                            {
                                //Toast.makeText(mContext, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(mContext, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(mContext).addToRequestQue(stringRequest);
    }

    private void updateInventoryLocation(final String tag_id, final String inv_location) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_UPDATE_INV_LOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null)
                        {

                        }
                        else
                        {

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
                params.put("tag_id", tag_id);
                params.put("inv_location", inv_location);
                return params;
            }
        };
        VolleySingleton.getInstance(mContext).addToRequestQue(stringRequest);
    }

    private void scanAndReadTag() {
        int switchCase = 0;
        if (btSearch.getText().equals("Scan"))
        {
            BtClear.setEnabled(false);
            btUpdate.setEnabled(false);
            spAuditLocations.setEnabled(false);
            switchCase = 1;

            switch (switchCase) {
                case 0:
                {
                    Toast.makeText(mContext, "Please Select Search Type", Toast.LENGTH_SHORT);
                }
                break;
                case 1:
                {
                    /*
*/                    btSearch.setText("Stop");
                    ((UHFMainActivity)getActivity()).sendData(btSearch.getText().toString());
                    btSearch.setBackground(getResources().getDrawable(R.drawable.button_bg2));

                    loopFlag = true;

                    new TagScanThread(StringUtils.toInt("10", 0),spAuditLocations.getSelectedItem().toString()).start();


                    /*if (mContext.mReader.startInventoryTag( 0,  0)) {
                        btSearch.setText("Stop");
                        ((UHFMainActivity)getActivity()).sendData(btSearch.getText().toString());
                        btSearch.setBackground(getResources().getDrawable(R.drawable.button_bg2));

                        loopFlag = true;

                        new TagScanThread(StringUtils.toInt("10", 0),spAuditLocations.getSelectedItem().toString()).start();

                    }
                    else if(mContext.mReader.stopInventory()){

                        if (mContext.mReader.startInventoryTag( 0,  0)) {
                            btSearch.setText("Stop");
                            ((UHFMainActivity)getActivity()).sendData(btSearch.getText().toString());
                            btSearch.setBackground(getResources().getDrawable(R.drawable.button_bg2));

                            loopFlag = true;

                            new TagScanThread(StringUtils.toInt("10", 0),spAuditLocations.getSelectedItem().toString()).start();

                        }

                    }*/
                }

                break;

                default:
                    break;
            }
        }
        else if(loopFlag){
            stopInventory();
        }else {
            stopInventory();
        }
    }



    private void setViewEnabled(boolean enabled) {

        //et_between.setEnabled(enabled);
        SpinnerQ.setEnabled(enabled);
        //btnFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    private void stopInventory() {

        if (loopFlag) {

            loopFlag = false;

            setViewEnabled(true);
            btUpdate.setText("Update");
            BtClear.setEnabled(true);
            btUpdate.setEnabled(true);
            //UHFMainActivity.mUhfrManager.asyncStopReading();
            btSearch.setText("Scan");
            btSearch.setBackground(getResources().getDrawable(R.drawable.button_bg_up3));
            /*if (mContext.mReader.stopInventory()) {
                btUpdate.setText(mContext.getString(R.string.btInventory));

            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);

            }
*/
        }
    }


    /*private void stopSearch() {

        if (loopFlag) {

            loopFlag = false;

            //if (mContext.mReader.stopInventory()) {
            UHFMainActivity.mUhfrManager.asyncStopReading();
                btSearch.setText("Scan");
                ((UHFMainActivity)getActivity()).sendData(btSearch.getText().toString());
                btSearch.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg3));
                if(locationWiseScannedSetMap.size()<=0){
                    spAuditLocations.setEnabled(true);
                }
                BtClear.setEnabled(true);
                btUpdate.setEnabled(true);
            *//*} else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);

            }*//*

        }
        else{
            Toast.makeText(mContext, scannedItemSet.size(),Toast.LENGTH_SHORT).show();
            //UHFMainActivity.mUhfrManager.asyncStopReading();
            if (mContext.mReader.stopInventory()) {

                btSearch.setText("Scan");
                ((UHFMainActivity)getActivity()).sendData(btSearch.getText().toString());
                btSearch.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg3));
            }
            else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);

            }
        }
    }*/

    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }

        String tempStr = "";

        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);

            tempStr = temp.get("tagUii");


            if (strEPC.equals(tempStr))
            {
                existFlag = i;
                break;
            }
        }

        return existFlag;
    }

    class TagScanThread extends Thread {
        Tagging tagging = new Tagging();
        private int mBetween = 80;
        private int block = 0;

        private String selectedLocation;
        Message msg = null;


        public TagScanThread(int iBetween, String location ){
            mBetween = iBetween;
            selectedLocation = location;

        }
        List<Reader.TAGINFO> list1;
        public void run() {
          //  String strTid;
           // String strResult;
            //String[] res = null;
          //  ISO15693Entity entity = null;


            /*while (loopFlag) {

                entity = mContext.mRFID.inventorySingleTag(); // inventory();

                if(UHFMainActivity.mUhfrManager != null) {
                    list1 = UHFMainActivity.mUhfrManager.tagInventoryByTimer((short) 50);
                    Log.e("UNL New List", list1 + "");
                    String data = null;
                    //  handler1.sendEmptyMessage(1980);
                    if (list1 != null && list1.size() > 0) {
                        Log.e(TAG, list1.size() + "");

                        playSound(1);


                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;

                            data = Tools.Bytes2HexString(epcdata, epcdata.length);

                            Log.e("UNL DATA", "***" + data);


                        }

                    }

                    msg = new Message();
                    //  msg.obj = data +"@"+"Success";
                    // searchHandler.sendMessage(msg);
                    Log.e("MY UNL", "***" + data);
                    //  Log.e("DATABASE","***"+tagIdHashMap);
                    if (tagIdHashMap.containsKey(data)) {
                        if (data != null) {

                            Log.e("Tag Id", "***" + tagIdHashMap.get(data).getLocation_id());

                            String myLocation = tagIdHashMap.get(data).getLocation_id().trim();
                            tagging = tagIdHashMap.get(data);
                            if (myLocation.equals(selectedLocation.trim())) {
                                Log.e("Inside EPC", "***");
                                msg.obj = tagging.getItem_id() + "@" +tagging.getTitle()+ "@" +tagging.getLocation_name()+ "@"
                                        + "success";
                                searchHandler.sendMessage(msg);

                            } else {
                                Log.e("Inside Else", "***" + tagging.getItem_id());
                                msg.obj = tagging.getItem_id() + "@" +tagging.getTitle()+ "@" +tagging.getLocation_name()+ "@"
                                        + "failure";
                                searchHandler.sendMessage(msg);
                            }
                        }
                    } else {
                        Log.e("Inside Else","***"+data);
                    }

                    try {
                        sleep(mBetween);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }



*//*
                //res = mContext.mReader.readTagFromBuffer();
                msg = new Message();
                try {
                    entity = mContext.mRFID.inventory();

                    if (entity != null) {
                        *//**//*msg.arg1 = 1;
                        msg.obj = entity;
                        searchHandler.sendMessage(msg);*//**//*
                        Log.e("kya",""+entity.getId());

                        if(tagIdHashMap.containsKey(entity.getId()))
                        {
                            tagging = tagIdHashMap.get(entity.getId());
                            if (tagIdHashMap.get(entity.getId()).getLocation_id().equals(selectedLocation))
                            {
                                msg.obj = tagging.getItem_id() + "@"
                                        + "0"+ "@" +tagging.getTitle()+ "@" +tagging.getLocation_id();
                                searchHandler.sendMessage(msg);
                            }
                            else
                            {
                                msg.obj = tagging.getItem_id() + "@"
                                        + "1"+ "@" +tagging.getTitle()+ "@" +tagging.getLocation_id();
                                searchHandler.sendMessage(msg);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("kya", "***");
                }



                try {
                    sleep(mBetween);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*//*

            }*/
        }
    }

   /* @Override
    public void myOnKeyDwon() {

        if(spAuditLocations.getSelectedItem().toString().equalsIgnoreCase("Select Location")){
            Toast.makeText(mContext, "Select Audit Location...!", Toast.LENGTH_SHORT).show();
        }
        else{
            if (!tagIdHashMap.isEmpty()) {
                ValidateConnection();
            }else {
                //Toast.makeText(mContext, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                showServerErrorDialog();
            }
        }
    }*/


    public void generateAuditLocationList(){
        //new AuditLocation().execute();
        auditLocationList = new LinkedList<String>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_AUDIT_LOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response !=null) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < jsonarray.length(); i++) {

                                    JSONObject jo = jsonarray.getJSONObject(i);
                                    auditLocationList.add(jo.getString("location_id"));
                                    auditLocationNameList.add(jo.getString("location_name"));
                                }

                                //** Audit Location Selector ** //
                                if (!auditLocationList.contains("Select Location")==true) {
                                    auditLocationList.add(0,"Select Location");
                                    adapterLocation = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, auditLocationList);
                                    adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spAuditLocations.setAdapter(adapterLocation);
                                    spAuditLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
                                    adapterLocation = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, auditLocationList);
                                    adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spAuditLocations.setAdapter(adapterLocation);
                                    spAuditLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            //Toast.makeText(mContext, auditLocationList.get(i), Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                            return;
                                        }
                                    });
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
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
            //send data if required
        };
        VolleySingleton.getInstance(mContext).addToRequestQue(stringRequest);

    }

    public void showDialog(String tagSubItem, List<Tagging> listSubItem)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        //alertDialog.setTitle("Hello");
        TextView title = new TextView(mContext);
        title.setTextColor(getColor(mContext, android.R.color.holo_red_dark));
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
        LayoutInflater layoutInflater = mContext.getLayoutInflater();
        View view_sub_item = layoutInflater.inflate(R.layout.dialog_content,null);

        listDialog = (ListView) view_sub_item.findViewById(R.id.listDialog);

        ListClickAdapter listClickAdapter = new ListClickAdapter(listSubItem, mContext);
        listDialog.setAdapter(listClickAdapter);
        alertDialog.setView(view_sub_item);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
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
                                scanAndReadTag();
                            }
                            else
                            {
                               showServerErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            showServerErrorDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showServerErrorDialog();
            }
        })
        {
            //Send data to server
        };
        VolleySingleton.getInstance(getActivity()).addToRequestQue(stringRequest);
    }
    public void showServerErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.network_error_dialog, null);
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
    public void showAlertDialog(int totalItem, String location) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.sure_alert_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);
        TextView textMessage = (TextView) view.findViewById(R.id.textMessage);
        TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
        textTitle.setText("Inventory Updated Successfully!!!");
        if (totalItem >1)
            textMessage.setText(totalItem+" Assets Updated in "+location+" Location");
        else
            textMessage.setText(totalItem+" Asset Updated in "+location+" Location");
        Button okBtn = (Button) view.findViewById(R.id.okBtn);
        Button cancelBtn = (Button)view.findViewById(R.id.cancelBtn);
        View dividerView = (View)view.findViewById(R.id.dividerView);
        dividerView.setVisibility(GONE);
        cancelBtn.setVisibility(GONE);
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

    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;
    private void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(getContext(), R.raw.scan, 1));
        soundMap.put(2, soundPool.load(getContext(), R.raw.serror, 1));
        am = (AudioManager) getContext().getSystemService(AUDIO_SERVICE);
    }
    public void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio,
                    volumnRatio,
                    1,
                    0,
                    1
            );
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    //key receiver
    private long startTime = 0;
    private boolean keyUpFalg = true;
    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int keyCode = intent.getIntExtra("keyCode", 0);
            if (keyCode == 0) {//H941
                keyCode = intent.getIntExtra("keycode", 0);
            }
//            Log.e("key ","keyCode = " + keyCode) ;
            boolean keyDown = intent.getBooleanExtra("keydown", false);
//			Log.e("key ", "down = " + keyDown);
            if (keyUpFalg && keyDown && System.currentTimeMillis() - startTime > 500) {
                keyUpFalg = false;
                startTime = System.currentTimeMillis();
                if ((//keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2
                        keyCode == KeyEvent.KEYCODE_F3 ||
//                                 keyCode == KeyEvent.KEYCODE_F4 ||
                                keyCode == KeyEvent.KEYCODE_F4  || keyCode == KeyEvent.KEYCODE_F7)) {
//                Log.e("key ","inventory.... " ) ;
                    if (!loopFlag) {
                        if (spAuditLocations.getSelectedItem().toString().equalsIgnoreCase("Select Location")) {
                            Toast.makeText(mContext, "Select Audit Location...!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!tagIdHashMap.isEmpty()) {
                                ValidateConnection();
                            } else {
                                //Toast.makeText(mContext, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
                                showServerErrorDialog();
                            }
                        }
                    }else {
                        stopInventory();
                    }

                }
                return;
            } else if (keyDown) {
                startTime = System.currentTimeMillis();
            } else {
                keyUpFalg = true;
            }

        }
    };

    
}

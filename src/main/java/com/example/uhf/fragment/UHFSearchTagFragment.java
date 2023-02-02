package com.example.uhf.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.uhf.R;
import com.example.uhf.Utilities.ServerUrls;
import com.example.uhf.Utilities.Tagging;
import com.example.uhf.Utilities.VolleySingleton;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.entity.ISO15693Entity;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import cn.pda.serialport.Tools;

import static android.content.ContentValues.TAG;
import static android.content.Context.AUDIO_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class UHFSearchTagFragment extends KeyDwonFragment {


    private boolean loopFlag = false;
    private int inventoryFlag = 0;
    private LinearLayout llQValue;
    Handler handler;
    Handler searchHandler;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;

    Button BtClear;
    TextView tv_count;
    RadioGroup RgInventory;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;
    //RadioButton RbInventoryAnti;
    Spinner SpinnerQ;
    Button BtInventory;
    Button btSearch;
    ListView LvTags;

    private Button btnFilter;//过滤

    byte initQ;

    private EditText et_between;
    private ImageView imgSuccess;

    private LinearLayout llContinuous;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;
    PopupWindow popFilter;


    ServerUrls serverUrls;
    Tagging tagging = new Tagging();
    LinkedList<LinkedHashMap<String, Tagging>> taggingMapList = new LinkedList<LinkedHashMap<String, Tagging>>();
    //LinkedHashMap<String, Tagging> taggingMap = new LinkedHashMap<String, Tagging>();
    //LinkedHashMap<String, Tagging> updatedtaggingMap = new LinkedHashMap<String, Tagging>();
    LinkedHashMap<String, Tagging> accNoHashMap = new LinkedHashMap<String, Tagging>();
    LinkedHashMap<String, Tagging> tagIdHashMap = new LinkedHashMap<String, Tagging>();

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            validateUniqueID(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");


        return inflater
                .inflate(R.layout.uhf_search_tag_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        serverUrls = new ServerUrls();

        mContext = (UHFMainActivity) getActivity();

        tagList = new ArrayList<HashMap<String, String>>();

        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);
        String tr = "";
        RbInventorySingle = (RadioButton) getView()
                .findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) getView()
                .findViewById(R.id.RbInventoryLoop);
		/*RbInventoryAnti = (RadioButton) getView()
				.findViewById(R.id.RbInventoryAnti);*/
        SpinnerQ = (Spinner) getView().findViewById(R.id.SpinnerQ);
        //BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        btSearch = (Button) getView().findViewById(R.id.btSearch);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        et_between = (EditText) getView().findViewById(R.id.et_between);
        et_between.addTextChangedListener(textWatcher);
        //et_between.setText("10");
        et_between.requestFocus(); //设置光标在文字之后

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[] { "tagUii", "tagLen", "tagCount", "tagRssi" },
                new int[] { R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi });

        BtClear.setOnClickListener(new BtClearClickListener());
        RgInventory
                .setOnCheckedChangeListener(new RgInventoryCheckedListener());
        //BtInventory.setOnClickListener(new BtInventoryClickListener());
        SpinnerQ.setEnabled(false);
        SpinnerQ.setOnItemSelectedListener(new QItemSelectedListener());

        llQValue = (LinearLayout) getView().findViewById(R.id.llQValue);

        imgSuccess = (ImageView)getView().findViewById(R.id.imgSuccess);



        LvTags.setAdapter(adapter);
        clearData();

        Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                // Bundle bundle = msg.getData();
                // String tagEPC = bundle.getString("tagEPC");

                String result = msg.obj + "";
                String[] strs = result.split("@");
                addEPCToList(strs[0],strs[1]);

            }
        };
//** Own Handler Created**//
        searchHandler =new Handler() {

            @Override
            public void handleMessage(Message msg) {

                // Bundle bundle = msg.getData();
                // String tagEPC = bundle.getString("tagEPC");

                String result = msg.obj + "";

                String[] strs = result.split("@");
                addEPCToList(strs[0],strs[1]);
                btSearch.setText("Search");
                Toast.makeText(mContext, result,Toast.LENGTH_LONG).show();

            }
        };
        //** End Own Handler Created**//
        SpinnerQ.setSelection(3);

        generateTaggingMapList();

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSearch.getText().toString().equalsIgnoreCase("Search")){
                    String searchKey = et_between.getText().toString().trim();
                    if(tagIdHashMap.containsKey(searchKey) || accNoHashMap.containsKey(searchKey)){

                        //Toast.makeText(mContext, "Found", Toast.LENGTH_LONG).show();
                        scanAndReadTag();
                        RbInventorySingle.setEnabled(false);
                        RbInventoryLoop.setEnabled(false);

                    }

                }
                else if(btSearch.getText().toString().equalsIgnoreCase("Searching")){
                    //et_between.getText().clear();
                    //btSearch.setText("Search");
                    scanAndReadTag();
                    RbInventorySingle.setEnabled(false);
                    RbInventoryLoop.setEnabled(false);
                    Toast.makeText(mContext, "Stopping...", Toast.LENGTH_LONG).show();
                }

            }
        });



    }


    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }



    private void addEPCToList(String epc,String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);

            map = new HashMap<String, String>();

            map.put("tagUii", epc);
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);

            // mContext.getAppContext().uhfQueue.offer(epc + "\t 1");

            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(
                        tagList.get(index).get("tagCount"), 10) + 1;

                map.put("tagCount", String.valueOf(tagcount));

                tagList.set(index, map);

            }

            adapter.notifyDataSetChanged();

        }
    }

    //****************************Text Watcher*********************************************
/*	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
		validateUniqueID(charSequence.toString());
	}

	@Override
	public void afterTextChanged(Editable s) {

	}*/
    private void validateUniqueID(String uniqueID)
    {
        if (RbInventoryLoop.isChecked())
        {
            if (accNoHashMap.containsKey(uniqueID))
            {
                imgSuccess.setVisibility(VISIBLE);
                btSearch.setEnabled(true);
            }
            else if (!accNoHashMap.containsKey(uniqueID)|| uniqueID.isEmpty())
            {
                imgSuccess.setVisibility(GONE);
                btSearch.setEnabled(false);
            }

        }
        else if (RbInventorySingle.isChecked())
        {
            if (tagIdHashMap.containsKey(uniqueID))
            {
                imgSuccess.setVisibility(VISIBLE);
                btSearch.setEnabled(true);
            }
            else if (!tagIdHashMap.containsKey(uniqueID)|| uniqueID.isEmpty())
            {
                imgSuccess.setVisibility(GONE);
                btSearch.setEnabled(false);
            }
        }

    }
//****************************Text Watcher*********************************************


    public class BtClearClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            clearData();


        }
    }

    private void clearData() {
        tv_count.setText("0");
        tagList.clear();
        et_between.setText("");
        imgSuccess.setVisibility(View.INVISIBLE);
        RbInventorySingle.setEnabled(true);
        RbInventoryLoop.setEnabled(true);

        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
    }

    public class RgInventoryCheckedListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            llQValue.setVisibility(GONE);
            llContinuous.setVisibility(GONE);

            if (checkedId == RbInventorySingle.getId()) {
                // 单步识别
                inventoryFlag = 0;
                SpinnerQ.setEnabled(false);
                llContinuous.setVisibility(VISIBLE);
            }
            else {
                // 单标签循环识别
                inventoryFlag = 0;
                SpinnerQ.setEnabled(false);

                llContinuous.setVisibility(VISIBLE);

            }/* else {
				// 防碰撞识别
				inventoryFlag = 2;
				SpinnerQ.setEnabled(true);
				llContinuous.setVisibility(View.VISIBLE);
				llQValue.setVisibility(View.VISIBLE);
			}*/
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

	/*public class BtInventoryClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			//new ReadTaggingData().execute();
			//taggingMapList = tagService.doInBackground();
			//taggingMap = taggingMapList.get(0);
			//updatedtaggingMap = taggingMapList.get(1);

			readTag();
		}
	}*/

    public void generateTaggingMapList(){


        accNoHashMap = new LinkedHashMap<String, Tagging>();
        tagIdHashMap = new LinkedHashMap<String, Tagging>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_ALL_TAG_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response !=null)
                        {
                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonarray = jsonObject.getJSONArray("result");

                                for (int i = 0; i < jsonarray.length(); i++) {

                                    Tagging missingTaggingListObj = new Tagging();
                                    JSONObject jo = jsonarray.getJSONObject(i);

                                    missingTaggingListObj.setItem_id(jo.getString("item_id"));
                                    missingTaggingListObj.setTag_id(jo.getString("tag_id"));
                                    missingTaggingListObj.setTitle(jo.getString("title"));


                                    accNoHashMap.put(jo.getString("item_id"),missingTaggingListObj);
                                    tagIdHashMap.put(jo.getString("tag_id"),missingTaggingListObj);

                                }
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
            //params
        };
        VolleySingleton.getInstance(mContext).addToRequestQue(stringRequest);

    }

    //**Own Created Method scanAndReadTag() **//
    private void scanAndReadTag() {
        if (btSearch.getText().equals("Search") )
        {
            int switchCase = 0;
            String tagId = "";

            if(RbInventoryLoop.isChecked()){
                tagId = getTagIdByUniqueItemNumber(et_between.getText().toString().trim());
                switchCase = 1;
            }
            if(RbInventorySingle.isChecked()){
                tagId = et_between.getText().toString().trim();
                switchCase = 2;
            }


            switch (switchCase) {
                case 0:// 单步
                {
                    Toast.makeText(mContext, "Please Select Search Type", Toast.LENGTH_LONG);
//					mContext.playSound(2);
                }
                break;
                case 1:// Search By Unique Item Number.
                {
                    btSearch.setText("Searching");
                    loopFlag = true;
                    //String tagId = getTagIdByUniqueItemNumber(et_between.getText().toString().trim());
                    new TagScanThread(StringUtils.toInt("10", 0),tagId).start();

                    /*if (mContext.mRFID.startInventoryTag( 0,  0)) {
                        btSearch.setText("Searching");
                        loopFlag = true;
                        //String tagId = getTagIdByUniqueItemNumber(et_between.getText().toString().trim());
                        new TagScanThread(StringUtils.toInt("10", 0),tagId).start();

                    }
                    else {
                        //mContext.mRFID.stopInventory();
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
                    }*/
                }
                break;
                case 2:// Search By Unique TagId/RFID Number.
                {

                    /*if (mContext.mReader.startInventoryTag( 0,  0)) {
                        btSearch.setText("Searching");
                        loopFlag = true;
                        new TagScanThread(StringUtils.toInt("10", 0),tagId).start();
                    }
                    else {
                        mContext.mReader.stopInventory();
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
                    }*/
                }
                break;

                default:
                    break;
            }
        } else {// Stop identifying
            stopSearch();
        }
    }
    //**End  scanAndReadTag() **//

    //**Own Created Method getTagIdByUniqueItemNumber() **//

    public String getTagIdByUniqueItemNumber(String UniqueItemNumber){
        String returnString = null;
        if(UniqueItemNumber != null){
            if(accNoHashMap.containsKey(UniqueItemNumber)){
                Tagging tt = new Tagging();
                tt = accNoHashMap.get(UniqueItemNumber);
                returnString = tt.getTag_id();
            }
        }
        return returnString;
    }
    //**End  getTagIdByUniqueItemNumber() **//



    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        //RbInventoryAnti.setEnabled(enabled);
        et_between.setEnabled(enabled);
        SpinnerQ.setEnabled(enabled);
        btnFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }


    /**
     * 停止识别
     */
    private void stopInventory() {

        if (loopFlag) {

            loopFlag = false;

            setViewEnabled(true);

            /*if (mContext.mReader.stopInventory()) {
                //BtInventory.setText(mContext.getString(R.string.btInventory));
                btSearch.setText("Search");
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);

            }*/

        }
    }

    //**Own Created Method**//
    private void stopSearch() {

        if (loopFlag) {

            loopFlag = false;



            /*if (mContext.mReader.stopInventory()) {

                btSearch.setText("Search");
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);

            }*/

        }
        else{
            /*if (mContext.mReader.stopInventory()) {

                btSearch.setText("Search");
            }
            else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);

            }*/
        }
    }

    //**End**//
    /**
     * 判断EPC是否在列表中
     *
     * @param strEPC
     *            索引
     * @return
     */
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

            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }

        return existFlag;
    }

    class TagThread extends Thread {

        private int mBetween = 80;
        HashMap<String, String> map;

        public TagThread(int iBetween) {
            mBetween = iBetween;
        }

        public void run() {
            String strTid;
            String strResult;

            String[] res = null;

            while (loopFlag) {

                // strUII = mContext.mReader.readUidFormBuffer();
                //
                // Log.i("UHFReadTagFragment", "TagThread uii=" + strUII);
                //
                // if (StringUtils.isNotEmpty(strUII)) {
                // strEPC = mContext.mReader.convertUiiToEPC(strUII);
                //
                // Message msg = handler.obtainMessage();
                // // Bundle bundle = new Bundle();
                // // bundle.putString("tagEPC", strEPC);
                //
                // // msg.setData(bundle);
                //
                // msg.obj = strEPC;
                // handler.sendMessage(msg);
                //
                // }

                /*res = mContext.mReader.readTagFromBuffer();//.readTagFormBuffer();

                if (res != null) {

                    strTid = res[0];
                    if (!strTid.equals("0000000000000000")&&!strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + "EPC:"
                            + mContext.mReader.convertUiiToEPC(res[1]) + "@"
                            + res[2];
                    handler.sendMessage(msg);
                }*/
                try {
                    sleep(mBetween);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }
    class TagScanThread extends Thread {
        Tagging tt = new Tagging();
        private int mBetween = 80;
        private String uniqueItemId = "";
        HashMap<String, String> map;
        private String selectedLocation;
        Message msg = null;

        public TagScanThread(int iBetween, String uniqueItemId) {
            mBetween = iBetween;
            this.uniqueItemId = uniqueItemId;
        }
        List<Reader.TAGINFO> list1;
        public void run() {
            String strTid;
            String strResult;

            String[] res = null;
            ISO15693Entity entity = null;

            while (loopFlag) {
                if(UHFMainActivity.mUhfrManager != null) {
                    list1 = UHFMainActivity.mUhfrManager.tagInventoryByTimer((short) 50);
                    Log.e("UNL New List", list1 + "");
                    String data = null;
                    //  handler1.sendEmptyMessage(1980);
                    if (list1 != null && list1.size() > 0) {
                        Log.e(TAG, list1.size() + "");

                        playSound(1);
                   /* if(isPlay) {
                        Util.play(1, 0);
                    }*/

                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;

                            data = Tools.Bytes2HexString(epcdata, epcdata.length);

                            int rssi = tfs.RSSI;
                            //    Message msg = new Message();
                            //  msg.what = 1;
                            //  Bundle b = new Bundle();
                            // b.putString("data", data);
                            Log.e("UNL DATA", "***" + data);
                            // b.putString("rssi", rssi + "");
                            //   msg.setData(b);
                            //  handler1.sendMessage(msg);

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

            }
        }
    }

    @Override
    public void myOnKeyDwon() {
        if(btSearch.getText().toString().equalsIgnoreCase("Search")){
            String searchKey = et_between.getText().toString().trim();
            if(btSearch.isEnabled()) {
                if (tagIdHashMap.containsKey(searchKey) || accNoHashMap.containsKey(searchKey)) {

                    //Toast.makeText(mContext, "Found", Toast.LENGTH_LONG).show();
                    scanAndReadTag();
                    RbInventorySingle.setEnabled(false);
                    RbInventoryLoop.setEnabled(false);

                }
            }
            else {
                Toast.makeText(mContext, "Enter Valid Asset Id...", Toast.LENGTH_LONG).show();
            }

        }
        else if(btSearch.getText().toString().equalsIgnoreCase("Searching")){
            //et_between.getText().clear();
            //btSearch.setText("Search");
            if(btSearch.isEnabled()) {
                scanAndReadTag();
                btSearch.setText("Search");
                RbInventorySingle.setEnabled(false);
                RbInventoryLoop.setEnabled(false);
                Toast.makeText(mContext, "Stopping...", Toast.LENGTH_LONG).show();
            }
        }
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
                    if(btSearch.getText().toString().equalsIgnoreCase("Search")){
                        String searchKey = et_between.getText().toString().trim();
                        if(btSearch.isEnabled()) {
                            if (tagIdHashMap.containsKey(searchKey) || accNoHashMap.containsKey(searchKey)) {

                                //Toast.makeText(mContext, "Found", Toast.LENGTH_LONG).show();
                                scanAndReadTag();
                                RbInventorySingle.setEnabled(false);
                                RbInventoryLoop.setEnabled(false);

                            }
                        }
                        else {
                            Toast.makeText(mContext, "Enter Valid Asset Id...", Toast.LENGTH_LONG).show();
                        }

                    }
                    else if(btSearch.getText().toString().equalsIgnoreCase("Searching")){
                        //et_between.getText().clear();
                        //btSearch.setText("Search");
                        if(btSearch.isEnabled()) {
                            scanAndReadTag();
                            btSearch.setText("Search");
                            RbInventorySingle.setEnabled(false);
                            RbInventoryLoop.setEnabled(false);
                            Toast.makeText(mContext, "Stopping...", Toast.LENGTH_LONG).show();
                        }
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

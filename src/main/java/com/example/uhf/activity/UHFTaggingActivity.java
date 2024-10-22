package com.example.uhf.activity;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.uhf.Utilities.ScanUtil;
import com.example.uhf.Utilities.ServerUrls;
import com.example.uhf.Utilities.Tagging;
import com.example.uhf.Utilities.VolleySingleton;
import com.example.uhf.tools.UIHelper;
import com.handheld.uhfr.UHFRManager;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pda.serialport.Tools;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static android.content.ContentValues.TAG;


public class UHFTaggingActivity extends Activity {

	private final static String TAG = "MainActivity";
	public static String TAG_LOCATION = "location";
	public static boolean isSaved = false;
	public RFIDWithUHFUART mReader;
	Common common = new Common();

	private TextView spTaggingLocation, spTaggingItemId, spTaggingSerialNo;
	private EditText et_read_epc, et_description;
	private Button btSearchTag, btSave, btClear;
	private ImageView imgPainting;
	private List<Tagging> taggingList;

    private ArrayList<String> locationIdList = new ArrayList<String>();
    private ArrayList<String> itemIdList = new ArrayList<String>();
    ArrayList<String> spTitle;
	SpinnerDialog spinnerDialog;
    ArrayAdapter<String> adapter;

	CheckBox ckbSave;
	ServerUrls serverUrls;
	private String doubledValue="";
	private SharedPreferences mSharedPreferences;
	private ScanUtil instance;
	public static UHFRManager mUhfrManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.uhf_tagging_fragment);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			ActionBar actionBar = (ActionBar) getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.banner));
		actionBar.setTitle("");
		actionBar.setIcon(android.R.color.transparent);
		/*IntentFilter filter = new IntentFilter();
		filter.addAction("android.rfid.FUN_KEY");
		registerReceiver(keyReceiver, filter);*/
        serverUrls = new ServerUrls();
		//	initUHF();
			initSound();
			//initViewPageData();
	        //initViewPager();
	        //initTabs();
		//*************************************************************************************
		mSharedPreferences = getSharedPreferences("UHF", MODE_PRIVATE);
		//Paper.init(this);
		spTaggingLocation = (TextView) findViewById(R.id.spTaggingLocation);
		spTaggingItemId = (TextView) findViewById(R.id.spTaggingItemId);
		//spTaggingSerialNo = (TextView) findViewById(R.id.spTaggingSerialNo);
		et_read_epc = (EditText) findViewById(R.id.et_read_epc);
		et_description = (EditText) findViewById(R.id.et_description);
		btSearchTag = (Button) findViewById(R.id.btSearchTag);
		btSave = (Button) findViewById(R.id.btSave);
		btClear = (Button) findViewById(R.id.btClear);
		ckbSave = (CheckBox)findViewById(R.id.ckbSave);
		imgPainting = (ImageView)findViewById(R.id.imgPainting);


		spTaggingLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getAllLocation();

			}
		});

        spTaggingItemId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (!spTaggingLocation.getText().toString().isEmpty()) {
            		getItemId(spTaggingLocation.getText().toString());

				}
				else {
					Toast.makeText(UHFTaggingActivity.this, "Select Location First", Toast.LENGTH_SHORT).show();
				}
            }
        });

		btSearchTag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//read tag id here...
				//fetchImageFromUrl("showPainingSmallImage","SAMC-OKH-GF-CHESS-101");
				//getAssetDetails("SAMC-OKH-GF-CHESS-101");
				//String userName = Paper.book().read(Common.USER_KEY);
				String userName = mSharedPreferences.getString(Common.USER_KEY,"");

				if (!btSearchTag.getText().toString().equalsIgnoreCase("Delete"))
					readTagForTagging();
				else
					alertDialog(et_read_epc.getText().toString(),spTaggingItemId.getText().toString(),userName);

			}
		});
		ckbSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if (isChecked)
				{
					if (spTaggingLocation.getText().toString().isEmpty())
					{
						ckbSave.setChecked(false);
						Toast.makeText(UHFTaggingActivity.this, "Select tagging location first", Toast.LENGTH_LONG).show();
					}
					else {
						//Paper.book("location").write(TAG_LOCATION, spTaggingLocation.getText().toString());
						isSaved = true;
						spTaggingLocation.setClickable(false);
					}
				}
				else
				{
					//Paper.book("location").destroy();
					isSaved = false;
					spTaggingLocation.setText(null);
					spTaggingLocation.setClickable(true);
				}
			}
		});

		//String location = Paper.book("location").read(TAG_LOCATION);
		String location = " paper";
		if (location !=null)
		{
			spTaggingLocation.setText(location);
			ckbSave.setChecked(true);
			isSaved = true;
		}
		else
		{
			ckbSave.setChecked(false);
			isSaved = false;
		}

		btSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (et_read_epc.getText().toString().isEmpty()||spTaggingLocation.getText().toString().isEmpty()||spTaggingItemId.getText().toString().isEmpty()) {
					Toast.makeText(UHFTaggingActivity.this, "Tagging data should not be empty", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//String userName = Paper.book().read(Common.USER_KEY);
					String userName = "paer";
					if(userName ==null) {
						saveData(et_read_epc.getText().toString(),
								spTaggingLocation.getText().toString(),
								spTaggingItemId.getText().toString(),
								et_description.getText().toString(),
								Common.userName);
					}
					else
					{
						saveData(et_read_epc.getText().toString(),
								spTaggingLocation.getText().toString(),
								spTaggingItemId.getText().toString(),
								et_description.getText().toString(),
								userName);
					}
				}
			}
		});
		btClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				clearData();
			}
		});


	}
	private String readTag() {
		List<Reader.TAGINFO> list1;
		String data = null;
		if(UHFTaggingActivity.mUhfrManager != null) {
			list1 = UHFTaggingActivity.mUhfrManager.tagInventoryByTimer((short) 50);
			Log.e("UNL New List", list1 + "");

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

					//int rssi = tfs.RSSI;
					//    Message msg = new Message();
					//  msg.what = 1;
					//  Bundle b = new Bundle();
					// b.putString("data", data);
					Log.e("UNL DATA", "***" + data);
					// b.putString("rssi", rssi + "");
					//   msg.setData(b);
					//  handler1.sendMessage(msg);

				}

			} else {
				UIHelper.ToastMessage(getApplicationContext(),
						R.string.uhf_msg_tag_not_in_rang);
//					mContext.playSound(2);
			}
		}
		else {
			UIHelper.ToastMessage(getApplicationContext(),
					R.string.uhf_msg_tag_not_in_rang);
//					mContext.playSound(2);
		}
		return data;
	}



	private void getImageByAssetId(String asset_id){
		new DownloadImageTask((ImageView) findViewById(R.id.imgPainting))
				.execute(serverUrls.URL_IMAGE_BY_ASSET_ID+asset_id);


	}

	@Override
	protected void onStart() {
		Log.e(TAG, "UNL [onStart] start");
		super.onStart();
		if (Build.VERSION.SDK_INT == 29) {
			instance = ScanUtil.getInstance(this);
			instance.disableScanKey("134");
		}
		mUhfrManager = UHFRManager.getInstance();// Init Uhf module
		if(mUhfrManager!=null){
			Reader.READER_ERR err = mUhfrManager.setPower(mSharedPreferences.getInt("readPower",33), mSharedPreferences.getInt("writePower",33));//set uhf module power

			if(err== Reader.READER_ERR.MT_OK_ERR){
				mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
				Toast.makeText(getApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
						"\n"+"Read Power:"+mSharedPreferences.getInt("readPower",33)+
						"\n"+"Write Power:"+mSharedPreferences.getInt("writePower",33),Toast.LENGTH_LONG).show();
//                showToast(getString(R.string.inituhfsuccess));
			}else {

				Reader.READER_ERR err1 = mUhfrManager.setPower(30, 30);//set uhf module power
				if(err1== Reader.READER_ERR.MT_OK_ERR) {
					mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
					Toast.makeText(getApplicationContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
							"\n" + "Read Power:" + 30 +
							"\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
				}else {
					showToast(getString(R.string.inituhffail));
				}
			}
		}else {
			showToast(getString(R.string.inituhffail));
		}
		Log.e(TAG, "[onStart] end");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == android.R.id.home)
		{
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void readTagForTagging() {
		if (!btSearchTag.getText().toString().equalsIgnoreCase("Delete")) {
			String tag_id = readTag();

			if (tag_id != null) {
				et_read_epc.setText(tag_id);
				Toast.makeText(UHFTaggingActivity.this, tag_id, Toast.LENGTH_LONG).show();
				//checkIfTagIdExist(et_read_epc.getText().toString());

			} else {
				Toast.makeText(UHFTaggingActivity.this, "Please restart this application", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(UHFTaggingActivity.this, "First Clear the information", Toast.LENGTH_SHORT).show();
		}
	}

	private void checkIfTagIdExist(final String tagId){
		locationIdList = new ArrayList<String>();
		spTitle = new ArrayList<String>();
		spTitle.add("Select or search location");
		final ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();
		StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_VALIDATE_RFID_TAG,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response !=null){
							try {
								pDialog.dismiss();
								JSONObject jsonObject = new JSONObject(response);
								JSONArray jsonArray = jsonObject.getJSONArray("result");
								if (jsonArray.length()!=0) {
									for (int i = 0; i < jsonArray.length(); i++) {
										JSONObject jo = jsonArray.getJSONObject(i);
										spTaggingLocation.setText(jo.getString("location_id"));
										spTaggingItemId.setText(jo.getString("item_id"));
										et_description.setText(jo.getString("title"));
										//(jo.getString("item_id"));
										btSearchTag.setText("Delete");
										btSearchTag.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg2));
										playSound(2);
										disableTextFields();

									}

								}



							} catch (JSONException e) {
								e.printStackTrace();
							}

						}else{
							Log.e(TAG, "Couldn't get json from server.");
							pDialog.dismiss();
						}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				pDialog.dismiss();
				Log.e(TAG, "Couldn't get json from server.");
			}
		})
		{
			//send params
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {

				Map<String, String> params = new HashMap();
				params.put("tag_id", tagId);

				return params;
			}
		};
		VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
	}
	private void getAllLocation(){

		locationIdList = new ArrayList<String>();
		spTitle = new ArrayList<String>();
        spTitle.add("Select or search location");
		final ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();

		StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_TAGGING_ASSET_LOCATION,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response !=null){
							try {
								pDialog.dismiss();
								JSONObject jsonObject = new JSONObject(response);
								JSONArray jsonArray = jsonObject.getJSONArray("result");
								for (int i=0; i<jsonArray.length(); i++){
									JSONObject jo = jsonArray.getJSONObject(i);
									locationIdList.add(jo.getString("location_id"));

								}
								adapter = new ArrayAdapter<String>(UHFTaggingActivity.this, android.R.layout.simple_spinner_item, spTitle);
								spinnerDialog=new SpinnerDialog(UHFTaggingActivity.this,locationIdList,"Select or Search Location","Close");// With No Animation
								//spinnerDialog=new SpinnerDialog(UHFTaggingActivity.this,items,"Select or Search City",R.style.DialogAnimations_SmileWindow,"Close Button Text");
								spinnerDialog.setCancellable(false);
								spinnerDialog.setShowKeyboard(false);
								spinnerDialog.showSpinerDialog();
								spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
									@Override
									public void onClick(String item, int position) {
										spTaggingLocation.setText(item);
										//Toast.makeText(UHFTaggingActivity.this, item + "  " + position+"", Toast.LENGTH_SHORT).show();

									}
								});

							} catch (JSONException e) {
								e.printStackTrace();
							}

						}else{
							Log.e(TAG, "Couldn't get json from server.");
							pDialog.dismiss();
						}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				pDialog.dismiss();
			}
		})
		{
			//send params
		};
		VolleySingleton.getInstance(this).addToRequestQue(stringRequest);


	}
	private void getItemId(final String locationId){
		itemIdList = new ArrayList<String>();
        spTitle = new ArrayList<String>();
        spTitle.add("Select or search asset id");
		final ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();
		StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_ALL_UNTAGGED_ASSET_BY_LOCATION,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response !=null)
						{
							try {
								pDialog.dismiss();
								JSONObject jsonObject = new JSONObject(response);
								JSONArray jsonArray = jsonObject.getJSONArray("result");
								for (int i=0; i<jsonArray.length();i++)
								{
									JSONObject jo = jsonArray.getJSONObject(i);
									//itemIdList.add(jo.getString("asset_id")+"#"+jo.getString("asset_id"));
                                    itemIdList.add(jo.getString("asset_id"));
								}
								adapter = new ArrayAdapter<String>(UHFTaggingActivity.this, android.R.layout.simple_spinner_item, spTitle);
								spinnerDialog=new SpinnerDialog(UHFTaggingActivity.this,  itemIdList,"Select or Search Item","Close");// With No Animation
								//spinnerDialog=new SpinnerDialog(UHFTaggingActivity.this,items,"Select or Search City",R.style.DialogAnimations_SmileWindow,"Close Button Text");
								spinnerDialog.setCancellable(false);
								spinnerDialog.setShowKeyboard(false);
								spinnerDialog.showSpinerDialog();
								spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
									@Override
									public void onClick(String item, int position) {
										spTaggingItemId.setText(item);
										getAssetDetails(item);
										//getImageByAssetId(item);

										//Toast.makeText(UHFTaggingActivity.this, item + "  " + position+"", Toast.LENGTH_SHORT).show();
									}
								});

							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
						else
						{
							Log.e(TAG, "Couldn't get json from server.");
							pDialog.dismiss();
						}

					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				pDialog.dismiss();
			}
		})
		{
			//send params
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {

				Map<String, String> params = new HashMap();
				params.put("location_id", locationId);
				return params;
			}
		};
		VolleySingleton.getInstance(this).addToRequestQue(stringRequest);

	}
	private void getAssetDetails(final String item_id){
		StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_ASSET_DETAILS_BY_ASSET_ID,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response !=null)
						{
							try {

								JSONObject jsonObject = new JSONObject(response);
								JSONArray jsonArray = jsonObject.getJSONArray("result");
								for (int i=0; i<jsonArray.length();i++)
								{
									JSONObject jo = jsonArray.getJSONObject(i);
									et_description.setText(jo.getString("title"));
									Log.d("TITLE","TITLE"+jo.getString("title"));
								}


							} catch (JSONException e) {
								e.printStackTrace();
							}
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
			//send params to server...
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {

				Map<String, String> params = new HashMap();

				params.put("asset_id", item_id);

				return params;
			}
		};
		VolleySingleton.getInstance(this).addToRequestQue(stringRequest);

	}
	private void saveData(final String tagId, final String locationId, final String itemId, final String description, final String userName)
	{
		final ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();
		StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_TAG_NEW_ASSET,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response !=null)
						{
							try {
								pDialog.dismiss();
								JSONObject jsonObject = new JSONObject(response);
								String result = jsonObject.getString("response");
								showMessageDialog(result,"tagged");

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						else
						{
							pDialog.dismiss();
						}

					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				pDialog.dismiss();
			}
		})
		{
			//send params to server...
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {

				Map<String, String> params = new HashMap();
				params.put("tag_id", tagId);
				params.put("location_id", locationId);
				params.put("asset_id", itemId);
				params.put("title", description);
				params.put("concern_person", userName);
				params.put("user_id", userName);
				return params;
			}
		};
		VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
	}
	private void deleteTaggedItem(final String tag_id, final String asset_id, final String userName) {
		final ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();
		StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_DELETE_TAGGED_ASSET,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response !=null)
						{
							try {
								pDialog.dismiss();
								JSONObject jsonObject = new JSONObject(response);
								String result = jsonObject.getString("response");
								showMessageDialog(result,"tagged");

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						else
						{
							pDialog.dismiss();
						}

					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				pDialog.dismiss();
			}
		})
		{
			//send params to server...
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {

				Map<String, String> params = new HashMap();
				params.put("tag_id", tag_id);
				params.put("asset_id", asset_id);
				params.put("concern_person", userName);

				return params;
			}
		};
		VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
	}
	private void showMessageDialog(String response, String message)
	{
		final Dialog messageDialog = new Dialog(UHFTaggingActivity.this);
		messageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		messageDialog.setContentView(R.layout.message_dialog);
		messageDialog.setCancelable(false);
		//messageDialog.setTitle("Alert");
		//messageDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
		Button okBtn = (Button) messageDialog.findViewById(R.id.okBtn);
		ImageView imgSuccess = (ImageView)messageDialog.findViewById(R.id.imgSuccess);
		TextView txtMessage = (TextView)messageDialog.findViewById(R.id.txtMessage);

		if (!response.equalsIgnoreCase("success"))
		{
			imgSuccess.setImageResource(R.drawable.already);
			txtMessage.setText(response);
		}
		else
		{
			txtMessage.setText("Your asset has been "+message+" successfully!");
		}
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				messageDialog.dismiss();
				clearData();
			}
		});
		messageDialog.show();
	}
	private void alertDialog(final String tag_id, final String asset_id, final String userName){
		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(UHFTaggingActivity.this);
		alertDialog.setTitle("Delete ! ");
		alertDialog.setIcon(R.drawable.remove);
		//alertDialog.setTitle("Alert !");
		alertDialog.setMessage("Are you sure you want to delete this asset from database ?");

		alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				//String userName = Paper.book().read(Common.USER_KEY);
				String userName = "";
				if (userName == null)
				{
					deleteTaggedItem(tag_id, asset_id, Common.userName);
				}
				else
				{
					deleteTaggedItem(tag_id, asset_id, userName);
				}

			}
		});
		alertDialog.show();
	}

	private void clearData(){
		btSearchTag.setText("Search");
		btSearchTag.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg3));
		imgPainting.setImageResource(R.drawable.empty);
		et_read_epc.setText(null);
		spTaggingItemId.setText(null);
		et_description.setText(null);
		if (!ckbSave.isChecked())
			spTaggingLocation.setText(null);
        enableTextFields();

	}

	/*private void searchLocationDialog()
	{

		final Dialog dialog_data = new Dialog(UHFTaggingActivity.this);
		dialog_data.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog_data.getWindow().setGravity(Gravity.CENTER);
		dialog_data.setContentView(R.layout.pop_up_dialog);
		WindowManager.LayoutParams lp_number_picker = new WindowManager.LayoutParams();
		Window window = dialog_data.getWindow();
		lp_number_picker.copyFrom(window.getAttributes());

		lp_number_picker.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp_number_picker.height = WindowManager.LayoutParams.WRAP_CONTENT;

		window.setGravity(Gravity.CENTER);
		window.setAttributes(lp_number_picker);
		TextView alertdialog_textview = (TextView) dialog_data.findViewById(R.id.alertdialog_textview);
		//alertdialog_textview.setText(selected_string);
		//alertdialog_textview.setHint(selected_string);
		*//*Button dialog_cancel_btn = (Button) dialog_data.findViewById(R.id.dialog_cancel_btn);
		dialog_cancel_btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(dialog_data != null)
				{
					dialog_data.dismiss();
					dialog_data.cancel();
				}

			}
		});*//*
		EditText filterText = (EditText) dialog_data.findViewById(R.id.alertdialog_edittext);
		ListView alertdialog_Listview = (ListView) dialog_data.findViewById(R.id.alertdialog_Listview);
		alertdialog_Listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, locationIdList);
		alertdialog_Listview.setAdapter(adapter);
		alertdialog_Listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id)
			{
				//Utility.hideKeyboard(this,v);


				spTaggingLocation.setText(String.valueOf(a.getItemAtPosition(position)));


				if(dialog_data != null)
				{
					dialog_data.dismiss();
					dialog_data.cancel();
				}
			}
		});

		filterText.addTextChangedListener(new TextWatcher()
		{
			List<String> locatioList;
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void onTextChanged(CharSequence query, int start, int before, int count)
			{


				adapter.getFilter().filter(query);


			}

			@Override
			public void afterTextChanged(Editable s)
			{

			}
		});
		dialog_data.show();


	}*/
	//**********************************************************************************************
	public void initUHF() {

		/*mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);*/
		try {
			mReader = RFIDWithUHFUART.getInstance();
		} catch (Exception ex) {

			Toast.makeText(this, ""+ex.getMessage(), Toast.LENGTH_SHORT).show();

			return;
		}

		if (mReader != null) {
			new InitTask().execute();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	@Override
	protected void onDestroy() {

		if (mReader != null) {
			mReader.free();
		}
		super.onDestroy();
	}
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			return mReader.init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (!result) {
				Toast.makeText(UHFTaggingActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(UHFTaggingActivity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}
	HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	private SoundPool soundPool;
	private float volumnRatio;
	private AudioManager am;
	private void initSound(){
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
		soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
		soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
		am = (AudioManager) this.getSystemService(AUDIO_SERVICE);
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
	/*private String readTag() {
		String strUII = mReader.inventorySingleTag();
		String strEPC=null;
		if (!TextUtils.isEmpty(strUII)) {
			strEPC = mReader.convertUiiToEPC(strUII);
			playSound(1);
			et_read_epc.setText(strEPC);

			//tv_count.setText("" + adapter.getCount());
		} else {
			UIHelper.ToastMessage(getApplicationContext(),
					R.string.uhf_msg_tag_not_in_rang);
//					mContext.playSound(2);
		}
		return strEPC;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == 139 ||keyCode == 280) {

			if (event.getRepeatCount() == 0) {
				readTagForTagging();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/*//**********************************************************************************************
	private void setImageFromByts(String imgByts){
		byte[] byteArray =  Base64.decode(imgByts, Base64.DEFAULT) ;
		Bitmap bmp1 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		imgPainting.setImageBitmap(bmp1);
	}
	private void setJsonImageFromByts(byte[] byteArray){
		Bitmap bmp1 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		imgPainting.setImageBitmap(bmp1);
	}*/
	private void disableTextFields(){
		spTaggingLocation.setClickable(false);
		spTaggingItemId.setClickable(false);
		et_description.setClickable(false);
		et_description.setFocusable(false);
		ckbSave.setClickable(false);
		btSave.setClickable(false);
	}
	private void enableTextFields(){
		spTaggingLocation.setClickable(true);
		spTaggingItemId.setClickable(true);
		et_description.setClickable(true);
		et_description.setFocusableInTouchMode(true);
		ckbSave.setClickable(true);
		btSave.setClickable(true);
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
		ProgressDialog pDialog = new ProgressDialog(UHFTaggingActivity.this);


		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(true);
			pDialog.show();
			//getAssetDetails(item);
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			pDialog.dismiss();
			bmImage.setImageBitmap(result);

		}
	}
	private Toast mToast;
	private void showToast(String info) {
		if (mToast == null) {
			mToast = Toast.makeText(this, info, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(info);
		}
		mToast.show();
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
					//String userName = Paper.book().read(Common.USER_KEY);
					String userName = "paper";
					if (!btSearchTag.getText().toString().equalsIgnoreCase("Delete"))
						readTagForTagging();
					else
						alertDialog(et_read_epc.getText().toString(),spTaggingItemId.getText().toString(),userName);

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

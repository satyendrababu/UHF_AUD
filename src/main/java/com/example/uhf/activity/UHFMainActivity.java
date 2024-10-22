package com.example.uhf.activity;


import com.example.uhf.R;
import com.example.uhf.Utilities.ScanUtil;
import com.example.uhf.fragment.UHFInventoryFragment;
import com.example.uhf.fragment.UHFKillFragment;
import com.example.uhf.fragment.UHFLockFragment;
import com.example.uhf.fragment.UHFReadEPCFragment;
import com.example.uhf.fragment.UHFReadFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.fragment.UHFSearchTagFragment;
import com.example.uhf.fragment.UHFSetFragment;
import com.example.uhf.fragment.UHFWriteFragment;
import com.handheld.uhfr.UHFRManager;
import com.rscja.utility.StringUtility;
import com.uhf.api.cls.Reader;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.HashMap;


public class UHFMainActivity extends BaseTabFragmentActivity {

	private final static String TAG = "MainActivity";

	String SCAN_BUTTON_VALUE = "";
	private SharedPreferences mSharedPreferences;
	private ScanUtil instance;
	//public static UHFRManager mUhfrManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);

		mSharedPreferences = getSharedPreferences("UHF", MODE_PRIVATE);

		ActionBar actionBar = (ActionBar) getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.banner));
		actionBar.setTitle("");
		actionBar.setIcon(android.R.color.transparent);

	    	initSound();
	        initHF();
	        initViewPageData();
	        initViewPager();
	        initTabs();



	}

	@Override
	protected void onStart() {
		Log.e(TAG, "UNL [onStart] start");
		super.onStart();
		if (Build.VERSION.SDK_INT == 29) {
			instance = ScanUtil.getInstance(this);
			instance.disableScanKey("134");
		}
		/*mUhfrManager = UHFRManager.getInstance();// Init Uhf module
		if(mUhfrManager!=null){
			Reader.READER_ERR err = mUhfrManager.setPower(mSharedPreferences.getInt("readPower",33), mSharedPreferences.getInt("writePower",33));//set uhf module power

			if(err== Reader.READER_ERR.MT_OK_ERR){
				mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
				*//*Toast.makeText(getApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
						"\n"+"Read Power:"+mSharedPreferences.getInt("readPower",33)+
						"\n"+"Write Power:"+mSharedPreferences.getInt("writePower",33),Toast.LENGTH_LONG).show();*//*
               showToast("Reader Connected!!!");

			}else {

				Reader.READER_ERR err1 = mUhfrManager.setPower(30, 30);//set uhf module power
				if(err1== Reader.READER_ERR.MT_OK_ERR) {
					mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
					*//*Toast.makeText(getApplicationContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
							"\n" + "Read Power:" + 30 +
							"\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();*//*
					showToast("Reader Connected!!!");
				}else {
					//showToast(getString(R.string.inituhffail));
					showToast("READER FAILED");
				}
			}
		}else {
			//showToast(getString(R.string.inituhffail));
			showToast("READER FAILED");
		}
		Log.e(TAG, "[onStart] end");*/
	}

	@Override
	    protected void initViewPageData() {
	        lstFrg.add(new UHFInventoryFragment());
	        lstFrg.add(new UHFSearchTagFragment());
	        lstFrg.add(new UHFReadEPCFragment());
	        lstFrg.add(new UHFKillFragment());
	        lstFrg.add(new UHFLockFragment());
	        lstFrg.add(new UHFSetFragment());


	        lstTitles.add(getString(R.string.uhf_msg_tab_inventory));
	        lstTitles.add(getString(R.string.uhf_msg_tab_search));
	        lstTitles.add(getString(R.string.uhf_msg_tab_epc));
	        lstTitles.add(getString(R.string.uhf_msg_tab_kill));
	        lstTitles.add(getString(R.string.uhf_msg_tab_lock));
	        lstTitles.add(getString(R.string.uhf_msg_tab_set));

	    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
	protected void onDestroy() {

		/*if (mUhfrManager != null) {
			mUhfrManager.asyncStopReading();
			//mUhfrManager.stopTagInventory();
		}*/
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		/*if (mUhfrManager != null) {
			mUhfrManager.asyncStopReading();
		}*/
	}

	public void sendData(String BUTTON_VALUE)
	{
		SCAN_BUTTON_VALUE = BUTTON_VALUE;
	}

	/*public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			return mRFID.init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (!result) {
				Toast.makeText(UHFMainActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(UHFMainActivity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}*/

	public boolean vailHexInput(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}


		if (str.length() % 2 == 0) {
			return StringUtility.isHexNumberRex(str);
		}

		return false;
	}
	HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	private SoundPool soundPool;
	private float volumnRatio;
	private AudioManager am;
	private void initSound(){
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
		soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
		soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
		am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
	}
	/**
	 * 播放提示音
	 *
	 * @param id 成功1，失败2
	 */
	public void playSound(int id) {

		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumnRatio = audioCurrentVolumn / audioMaxVolumn;
		try {
			soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
					volumnRatio, // 右声道音量
					1, // 优先级，0为最低
					0, // 循环次数，0无不循环，-1无永远循环
					1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
			);
		} catch (Exception e) {
			e.printStackTrace();

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
}

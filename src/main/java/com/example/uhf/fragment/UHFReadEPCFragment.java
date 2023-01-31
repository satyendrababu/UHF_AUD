package com.example.uhf.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.entity.ISO15693Entity;

import java.util.ArrayList;
import java.util.HashMap;


public class UHFReadEPCFragment extends KeyDwonFragment {

	private boolean loopFlag = false;
	private int inventoryFlag = 1;
	private LinearLayout llQValue;
	Handler handler;
	private ArrayList<HashMap<String, String>> tagList;
	SimpleAdapter adapter;

	Button BtClear;
	TextView tv_count;
	RadioGroup RgInventory;
	RadioButton RbInventorySingle;
	RadioButton RbInventoryLoop;
	RadioButton RbInventoryAnti;
	Spinner SpinnerQ;
	Button BtInventory;
	ListView LvTags;

	private Button btnFilter;//过滤

	byte initQ;

	private EditText et_between;
	private LinearLayout llContinuous;

	private UHFMainActivity mContext;

	private HashMap<String, String> map;

	PopupWindow popFilter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		Log.i("MY", "UHFReadTagFragment.onCreateView");

		return inflater
				.inflate(R.layout.uhf_read_epc_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i("MY", "UHFReadTagFragment.onActivityCreated");
		super.onActivityCreated(savedInstanceState);

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

		SpinnerQ = (Spinner) getView().findViewById(R.id.SpinnerQ);
		BtInventory = (Button) getView().findViewById(R.id.BtInventory);
		LvTags = (ListView) getView().findViewById(R.id.LvTags);

		et_between = (EditText) getView().findViewById(R.id.et_between);
		et_between.setText("10");
		et_between.requestFocus();

		llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

		adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
				new String[] { "tagUii", "tagLen", "tagCount", "tagRssi" },
				new int[] { R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
						R.id.TvTagRssi });

		BtClear.setOnClickListener(new BtClearClickListener());
		RgInventory
				.setOnCheckedChangeListener(new RgInventoryCheckedListener());
		BtInventory.setOnClickListener(new BtInventoryClickListener());
		SpinnerQ.setEnabled(false);
		SpinnerQ.setOnItemSelectedListener(new QItemSelectedListener());

		llQValue = (LinearLayout) getView().findViewById(R.id.llQValue);

		btnFilter = (Button) getView().findViewById(R.id.btnFilter);
		btnFilter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (popFilter == null) {
					View viewPop = LayoutInflater.from(mContext).inflate(R.layout.popwindow_filter, null);

					popFilter = new PopupWindow(viewPop, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);

					popFilter.setTouchable(true);
					popFilter.setOutsideTouchable(true);
					popFilter.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
					popFilter.setBackgroundDrawable(new BitmapDrawable());

					final EditText etPtr = (EditText) viewPop.findViewById(R.id.etPtr);
					final EditText etData = (EditText) viewPop.findViewById(R.id.etData);
					final RadioButton rbEPC = (RadioButton) viewPop.findViewById(R.id.rbEPC);
					final RadioButton rbTID = (RadioButton) viewPop.findViewById(R.id.rbTID);
					Button btnOk = (Button) viewPop.findViewById(R.id.btnOk);
					Button btn_disable = (Button) viewPop.findViewById(R.id.btnDisable);

					CheckBox cb_filter = (CheckBox) viewPop.findViewById(R.id.cb_filter);

					btnOk.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							int bank = -1;
							if (rbEPC.isChecked()) {
								bank = 1;
							} else if (rbTID.isChecked()) {
								bank = 2;
							}

							String data = etData.getText().toString().trim();
							int ptr = StringUtils.toInt(etPtr.getText().toString(), 0);
							String rex = "[\\da-fA-F]*";
							if(!data.matches(rex)) {

								UIHelper.ToastMessage(mContext, R.string.msg_data_hex);
								return;
							}

							/*if (mContext.mReader.setFilter(bank, ptr, data)) {
								UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_succ);
							} else {
								UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail);
							}*/

							popFilter.dismiss();
						}
					});

					btn_disable.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String dataStr = "";
							etPtr.setText("0");
							etData.setText(dataStr);
							/*if(mContext.mReader.setFilter(1, 0, dataStr) && mContext.mReader.setFilter(2, 0, dataStr)) {
								UIHelper.ToastMessage(mContext, R.string.msg_disable_succ);
							} else {
								UIHelper.ToastMessage(mContext, R.string.msg_disable_fail);
							}*/
							popFilter.dismiss();
						}
					});

					cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked) { //启用过滤
								int bank = -1;
								if (rbEPC.isChecked()) {
									bank = 1;
								} else if (rbTID.isChecked()) {
									bank = 2;
								}

								String data = etData.getText().toString().trim();
								int ptr = StringUtils.toInt(etPtr.getText().toString(), 0);
								String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
								if(!data.matches(rex)) {
									buttonView.setChecked(false);
									UIHelper.ToastMessage(mContext, R.string.msg_data_hex);
//									mContext.playSound(2);
									return;
								}

								/*if (mContext.mReader.setFilter(bank, ptr, data)) {
									UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_succ);
								} else {
									UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail);
//									mContext.playSound(2);
								}*/
							} else { //禁用过滤
								String dataStr = "";
								etPtr.setText("0");
								etData.setText(dataStr);
								/*if(mContext.mReader.setFilter(1, 0, dataStr) && mContext.mReader.setFilter(2, 0, dataStr)) {
									UIHelper.ToastMessage(mContext, R.string.msg_disable_succ);
								} else {
									UIHelper.ToastMessage(mContext, R.string.msg_disable_fail);
//									mContext.playSound(2);
								}*/
							}
							popFilter.dismiss();
						}
					});
				}

				if (popFilter.isShowing()) {
					popFilter.dismiss();
					popFilter = null;
				} else {
					popFilter.showAsDropDown(view);
				}

			}
		});

		LvTags.setAdapter(adapter);
		clearData();

		Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				// Bundle bundle = msg.getData();
				// String tagEPC = bundle.getString("tagEPC");

				String result = msg.obj + "";
				//String[] strs = result.split("@");
				addEPCToList(result,"");

			}
		};

		SpinnerQ.setSelection(3);
	}
	
	@Override
	public void onPause() {
		Log.i("MY", "UHFReadTagFragment.onPause");
		super.onPause();
		
		// 停止识别
		stopInventory();
	}

	/**
	 * 添加EPC到列表中
	 * 
	 * @param epc
	 */
	private void addEPCToList(String epc, String rssi) {
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

	public class BtClearClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			clearData();
			stopInventory();

		}
	}

	private void clearData() {
		tv_count.setText("0");
		BtInventory.setText("Start");
		tagList.clear();

		Log.i("MY", "tagList.size " + tagList.size());

		adapter.notifyDataSetChanged();
	}

	public class RgInventoryCheckedListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			llQValue.setVisibility(View.GONE);
			llContinuous.setVisibility(View.GONE);

			if (checkedId == RbInventorySingle.getId()) {
				// 单步识别
				inventoryFlag = 0;
				SpinnerQ.setEnabled(false);
			} else if (checkedId == RbInventoryLoop.getId()) {
				// 单标签循环识别
				inventoryFlag = 1;
				SpinnerQ.setEnabled(false);

				llContinuous.setVisibility(View.VISIBLE);

			} else {
				// 防碰撞识别
				inventoryFlag = 2;
				SpinnerQ.setEnabled(true);
				llContinuous.setVisibility(View.VISIBLE);
				llQValue.setVisibility(View.VISIBLE);
			}
		}
	}

	public class QItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

			initQ = Byte.valueOf((String) SpinnerQ.getSelectedItem(), 10);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	public class BtInventoryClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			if (BtInventory.getText().equals("Start")) {
				BtInventory.setText("Stop");
				readTag();
			}else {
				BtInventory.setText("Start");
				stopInventory();
			}
		}
	}

	private void readTag() {
		if (BtInventory.getText().equals("Stop"))// 识别标签
		{
			switch (inventoryFlag) {
			case 0:// 单步
			{

				/*String strUII = mContext.mRFID.inventorySingleTag();
				if (!TextUtils.isEmpty(strUII)) {
					String strEPC = mContext.mReader.convertUiiToEPC(strUII);
					addEPCToList(strEPC,"N/A");

					tv_count.setText("" + adapter.getCount());
				} else {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_fail);
//					mContext.playSound(2);
				}*/

			}
				break;
			case 1:// 单标签循环
			{
				loopFlag = true;
				setViewEnabled(false);

				new TagThread(StringUtils.toInt(et_between.getText()
						.toString().trim(), 0)).start();
				/*if (mContext.mReader.startInventoryTag((byte) 0, (byte) 0)) {
					BtInventory.setText(mContext
							.getString(R.string.title_stop_Inventory));
					loopFlag = true;
					setViewEnabled(false);

					new TagThread(StringUtils.toInt(et_between.getText()
							.toString().trim(), 0)).start();
				} else {
					mContext.mReader.stopInventory();
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
				}*/
			}

				break;
			case 2:// 防碰撞
			{
				/*if (mContext.mReader.startInventoryTag((byte) 1, initQ)) {
					BtInventory.setText(mContext
							.getString(R.string.title_stop_Inventory));
					loopFlag = true;
					setViewEnabled(false);
					new TagThread(StringUtils.toInt(et_between.getText()
							.toString().trim(), 0)).start();
				} else {
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
		} else {
			stopInventory();
		}
	}

	private void setViewEnabled(boolean enabled) {
		RbInventorySingle.setEnabled(enabled);
		RbInventoryLoop.setEnabled(enabled);

		et_between.setEnabled(enabled);
		SpinnerQ.setEnabled(enabled);
		btnFilter.setEnabled(enabled);
		BtClear.setEnabled(enabled);
	}


	private void stopInventory() {

		if (loopFlag) {

			loopFlag = false;

			setViewEnabled(true);

			/*if (mContext.mReader.stopInventory()) {
				BtInventory.setText(mContext.getString(R.string.btInventory));
			} else {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_inventory_stop_fail);

			}*/

		}
	}


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
			ISO15693Entity entity = null;

			while (loopFlag) {

				entity = mContext.mRFID.inventory();//.readTagFormBuffer();

				if (entity != null) {

					strTid = entity.getId();
					/*if (!strTid.equals("0000000000000000")&&!strTid.equals("000000000000000000000000")) {
						strResult = strTid;
					} else {
						strResult = "";
					}*/
					Log.e("kya",""+strTid);
					Message msg = handler.obtainMessage();
					msg.obj = strTid;
					handler.sendMessage(msg);
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

	@Override
	public void myOnKeyDwon() {
		readTag();
	}

}

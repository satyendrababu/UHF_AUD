package com.example.uhf.fragment;


import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.utility.StringUtility;

import android.app.AlertDialog;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class UHFSetFragment extends KeyDwonFragment implements OnClickListener {
    private UHFMainActivity mContext;

    private Button btnSetFre;
    private Button btnGetFre;
    private Spinner spMode;

    private LinearLayout ll_freHop;


    private Button btnSetPower;

    private Button btnGetPower;

    private Spinner spPower;

    private EditText et_worktime;

    private EditText et_waittime;

    private Button btnWorkWait;

    private Spinner spFreHop; //频点列表

    private Button btnSetFreHop; //设置频点设置

    private TextView tv_normal_set; //普通设置(点击5次设置频点设置)

    private Button btnGetWait; //获取空占比

    private Button btnSetAgreement; //设置协议

    private Spinner SpinnerAgreement; //协议列表

    private Button btnSetLinkParams; //设置链路参数

    private Button btnGetLinkParams; //获取链路参数

    private Spinner splinkParams; //链路参数列表

    private Button btnSetQTParams; //设置QT参数

    private Button btnGetQTParams; //获取QT参数

    private CheckBox cbQt; //打开QT

    private CheckBox cbTagFocus; //打开tagFocus

    private CheckBox cbFastID; //打开FastID

    private CheckBox cbEPC_TID; //打开EPC+TID


    private RadioButton rb_America; //美国频点

    private RadioButton rb_Others; //其他频点
    private ArrayAdapter adapter; //频点列表适配器

   //session
    private  Spinner SpSession;


    private  Spinner SpInv;


    private  Button btnGetSession;


    private  Button btnSetSession;

    private DisplayMetrics metrics;
    private AlertDialog dialog;
    private long[] timeArr;

    private Handler mHandler = new Handler();
    private int arrPow; //输出功率

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater
                .inflate(R.layout.activity_uhfset, container, false);
        ViewUtils.inject(this, root);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (UHFMainActivity) getActivity();

        btnSetFre = (Button) getView().findViewById(R.id.BtSetFre);
        btnGetFre = (Button) getView().findViewById(R.id.BtGetFre);

        spMode = (Spinner) getView().findViewById(R.id.SpinnerMode);
        spMode.setOnItemSelectedListener(new MyOnTouchListener());

        SpSession=(Spinner)getView().findViewById(R.id.spsession);
        SpInv=(Spinner)getView().findViewById(R.id.spinv);

        btnSetFre.setOnClickListener(new SetFreOnclickListener());
        btnGetFre.setOnClickListener(new GetFreOnclickListener());
        btnWorkWait.setOnClickListener(new SetPWMOnclickListener());
        btnGetWait.setOnClickListener(this);

        btnSetSession.setOnClickListener(this);
        btnGetSession.setOnClickListener(this);
        btnSetFreHop.setOnClickListener(this);
        tv_normal_set.setOnClickListener(this);
        btnSetAgreement.setOnClickListener(this);
        btnSetQTParams.setOnClickListener(this);
        btnGetQTParams.setOnClickListener(this);
        btnSetLinkParams.setOnClickListener(this);
        btnGetLinkParams.setOnClickListener(this);
        cbTagFocus.setOnCheckedChangeListener(new OnMyCheckedChangedListener());
        cbFastID.setOnCheckedChangeListener(new OnMyCheckedChangedListener());
        cbEPC_TID.setOnCheckedChangeListener(new OnMyCheckedChangedListener());

        SpSession.setSelection(1);
        SpInv.setSelection(0);
        /*String ver = mContext.mReader.getHardwareType();
        arrPow = R.array.arrayPower;
        if (ver != null && ver.contains("RLM")) {
            arrPow = R.array.arrayPower2;
        }*/
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, arrPow, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPower.setAdapter(adapter);



    }

    @Override
    public void onResume() {
        super.onResume();
        /*
		开启子线程获取参数，Handler更新UI,防止fragment打开卡顿
		 */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getFre();
                getPwm();
                getLinkParams();
                OnClick_GetPower(null);
            }
        });
    }

    /**
     * 工作模式下拉列表点击选中item监听
     */
    public class MyOnTouchListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (position == 3) {
                ll_freHop.setVisibility(View.VISIBLE);
                rb_America.setChecked(true); //默认美国频点
            } else if (position != 3) {
                ll_freHop.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class SetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            // byte[] bBaseFre = new byte[2];
            //
            // if (mContext.mReader.setFrequency(
            // (byte) spMode.getSelectedItemPosition(), (byte) 0,
            // bBaseFre, (byte) 0, (byte) 0, (byte) 0)) {
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_set_frequency_succ);
            // } else {
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_set_frequency_fail);
            // }

            /*if (mContext.mReader.setFrequencyMode((byte) spMode
                    .getSelectedItemPosition())) {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_frequency_succ);
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_frequency_fail);
//                mContext.playSound(2);
            }*/
        }
    }

    public void getFre() {
        /*int idx = mContext.mReader.getFrequencyMode();

        if (idx != -1) {
            int count = spMode.getCount();
            spMode.setSelection(idx > count - 1 ? count - 1 : idx);

            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_frequency_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_read_frequency_fail);
        }*/
    }

    public void getPwm() {
        /*int[] pwm = mContext.mReader.getPwm();

        if (pwm == null || pwm.length < 2) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_pwm_fail);
            return;
        }

        et_worktime.setText(pwm[0] + "");
        et_waittime.setText(pwm[1] + "");

        et_worktime.setSelection(et_worktime.getText().toString().length());
        et_waittime.setSelection(et_waittime.getText().toString().length());*/
    }

    /**
     * 获取链路参数
     */
    public void getLinkParams() {
        /*int idx = mContext.mReader.getRFLink();
        if (idx != -1) {
            splinkParams.setSelection(idx);

//			UIHelper.ToastMessage(mContext,
//					R.string.uhf_msg_get_para_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_get_para_fail);
        }*/
    }

    public class SetPWMOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            /*if (mContext.mReader.setPwm(StringUtility.string2Int(et_worktime.getText().toString(), 0),
                    StringUtility.string2Int(et_waittime.getText().toString(), 0))) {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_pwm_succ);
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_pwm_fail);
//                mContext.playSound(2);
            }*/
        }
    }

    public class GetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            // String strFrequency = mContext.mReader.getFrequency();
            //
            // if (StringUtils.isNotEmpty(strFrequency)) {
            //
            // etFreRange.setText(strFrequency);
            //
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_frequency_succ);
            //
            // } else {
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_frequency_fail);
            // }

            getFre();
        }
    }

    public class OnMyCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                /*case R.id.cbTagFocus:
                    *//*if (mContext.mReader.setTagFocus(isChecked)) {
                        if (isChecked) {
                            cbTagFocus.setText(R.string.tagFocus_off);
                        } else {
                            cbTagFocus.setText(R.string.tagFocus);
                        }
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }*//*
                    break;
                case R.id.cbFastID:
                    *//*if (mContext.mReader.setFastID(isChecked)) {
                        if (isChecked) {
                            cbFastID.setText(R.string.fastID_off);
                        } else {
                            cbFastID.setText(R.string.fastID);
                        }
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }*//*
                    break;
                case R.id.cbEPC_TID:
                    *//*if (mContext.mReader.setEPCTIDMode(isChecked)) {
                        if (isChecked) {
                            cbEPC_TID.setText(R.string.EPC_TID_off);
                        } else {
                            cbEPC_TID.setText(R.string.EPC_TID);
                        }
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }*//*
                    break;*/
            }
        }
    }


    public void OnClick_GetPower(View view) {
        /*int iPower = mContext.mReader.getPower();

        Log.i("UHFSetFragment", "OnClick_GetPower() iPower=" + iPower);

        if (iPower > -1) {
            int position = iPower - 5;
            int count = spPower.getCount();
            spPower.setSelection(position > count - 1 ? count - 1 : position);

            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_power_succ);

        } else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_power_fail);
        }*/

    }


    public void OnClick_SetPower(View view) {
        int iPower = spPower.getSelectedItemPosition() + 5;

        Log.i("UHFSetFragment", "OnClick_SetPower() iPower=" + iPower);

        /*if (mContext.mReader.setPower(iPower)) {

            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_succ);
        } else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail);
//            mContext.playSound(2);
        }*/

    }

    /**
     * 设置频点
     *
     * @param value 频点数值
     * @return 是否设置成功
     */
    private boolean setFreHop(float value) {
        /*boolean result = mContext.mReader.setFreHop(value);
        if (result) {

            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_set_frehop_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_set_frehop_fail);
//            mContext.playSound(2);
        }*/
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        /*switch (v.getId()) {
            case R.id.btnSetFreHop: //设置频点
//			showFrequencyDialog();
                View view = spFreHop.getSelectedView();
                if (view instanceof TextView) {
                    String freHop = ((TextView) view).getText().toString().trim();
                    setFreHop(Float.valueOf(freHop)); //设置频点
                }
                break;
            case R.id.btnGetWait: //获取空占比
                getPwm();
                break;
            case R.id.btnSetAgreement: //设置协议
                *//*if (mContext.mReader.setProtocol(SpinnerAgreement.getSelectedItemPosition())) {
                    UIHelper.ToastMessage(mContext, R.string.setAgreement_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.setAgreement_fail);
//                    mContext.playSound(2);
                }*//*
                break;
            case R.id.btnSetQTParams: //设置QT参数
                if (!cbQt.isChecked()) {
                    UIHelper.ToastMessage(mContext, R.string.please_on);
//                    mContext.playSound(2);
                    return;
                }
                *//*if (mContext.mReader.setQTPara(cbQt.isChecked())) {
                    UIHelper.ToastMessage(mContext, R.string.setQTParams_succ);

                } else {
                    UIHelper.ToastMessage(mContext, R.string.setQTParams_fail);
//                    mContext.playSound(2);
                }*//*
                break;
            case R.id.btnGetQTParams: //获取QT参数
                *//*int[] QTParams = mContext.mReader.getQTPara();
                if (QTParams[0] == 1) {
                    cbQt.setChecked(QTParams[1] == 1);
                    UIHelper.ToastMessage(mContext,
                            R.string.getQTParams_succ);
                } else {
                    UIHelper.ToastMessage(mContext,
                            R.string.getQTParams_fail);
                }*//*
                break;
            case R.id.btnSetLinkParams: //设置链路参数
                *//*if (mContext.mReader.setRFLink(splinkParams.getSelectedItemPosition())) {

                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_succ);

                } else {
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_fail);
//                    mContext.playSound(2);
                }*//*
                break;
            case R.id.btnGetLinkParams: //获取链路参数
                getLinkParams();
                break;

            case R.id.btnSetSession:

                // //设置SESSION只针对盘点EPC有效，对返回EPC+TID,EPC+TID+USER无效
                int seesionid =SpSession .getSelectedItemPosition();
                int inventoried = SpInv.getSelectedItemPosition();
                if(seesionid<0 || inventoried<0){
                    return;
                }
                *//*char[] p=mContext.mReader.GetGen2();
                if(p!=null && p.length>=14) {
                    int target = p[0];
                    int action = p[1];
                    int t = p[2];
                    int q = p[3];
                    int startQ = p[4];
                    int minQ = p[5];
                    int maxQ = p[6];
                    int dr = p[7];
                    int coding = p[8];
                    int p1 = p[9];
                    int Sel = p[10];
                    int Session = p[11];
                    int g = p[12];
                    int linkFrequency = p[13];
                    StringBuilder sb=new StringBuilder();
                    sb.append("target=");sb.append(target);
                    sb.append(" ,action=");sb.append(action);
                    sb.append(" ,t=");sb.append(t);
                    sb.append(" ,q=");sb.append(q);
                    sb.append(" startQ=");sb.append(startQ);
                    sb.append(" minQ=");sb.append(minQ);
                    sb.append(" maxQ=");sb.append(maxQ);
                    sb.append(" dr=");sb.append(dr);
                    sb.append(" coding=");sb.append(coding);
                    sb.append(" p=");sb.append(p1);
                    sb.append(" Sel=");sb.append(Sel);
                    sb.append(" Session=");sb.append(Session);
                    sb.append(" g=");sb.append(g);
                    sb.append(" linkFrequency=");sb.append(linkFrequency);
                    sb.append("seesionid=");sb.append(seesionid);
                    sb.append(" inventoried=");sb.append(inventoried);
                    Log.i(TAG,sb.toString());
                    if(mContext.mReader.SetGen2(target, action, t, q, startQ, minQ, maxQ, dr, coding, p1, Sel, seesionid, inventoried, linkFrequency)){
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    }else{
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
                    }
                }else{
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_fail);
                }*//*
                break;
            case R.id.btnGetSession:
                *//*char[] pp=mContext.mReader.GetGen2();
                if(pp!=null && pp.length>=14) {
                    int target = pp[0];
                    int action = pp[1];
                    int t = pp[2];
                    int q = pp[3];
                    int startQ = pp[4];
                    int minQ = pp[5];
                    int maxQ = pp[6];
                    int dr = pp[7];
                    int coding = pp[8];
                    int p1 = pp[9];
                    int Sel = pp[10];
                    int Session = pp[11];
                    int g = pp[12];
                    int linkFrequency = pp[13];
                    StringBuilder sb=new StringBuilder();
                    sb.append("target=");sb.append(target);
                    sb.append(" ,action=");sb.append(action);
                    sb.append(" ,t=");sb.append(t);
                    sb.append(" ,q=");sb.append(q);
                    sb.append(" startQ=");sb.append(startQ);
                    sb.append(" minQ=");sb.append(minQ);
                    sb.append(" maxQ=");sb.append(maxQ);
                    sb.append(" dr=");sb.append(dr);
                    sb.append(" coding=");sb.append(coding);
                    sb.append(" p=");sb.append(p1);
                    sb.append(" Sel=");sb.append(Sel);
                    sb.append(" Session=");sb.append(Session);
                    sb.append(" g=");sb.append(g);
                    sb.append(" linkFrequency=");sb.append(linkFrequency);
                    Log.i(TAG,sb.toString());
                    SpSession.setSelection(Session);
                    SpInv.setSelection(g);
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_succ);
                }*//*
            default:
                break;
        }*/
    }

    /**
     * 显示频点设置
     */
    private void showFrequencyDialog() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//	        builder.setTitle(R.string.btSetFrequency);
            View view = getActivity().getLayoutInflater().inflate(R.layout.uhf_dialog_frequency, null);
            ListView listView = (ListView) view.findViewById(R.id.listView_frequency);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_dismissDialog);
            iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                }
            });

            String[] strArr = getResources().getStringArray(R.array.arrayFreHop);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.item_text1, strArr));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        float value = Float.valueOf(tv.getText().toString().trim());
                        setFreHop(value); //设置频点
                        dialog.dismiss();
                    }
                }

            });

            builder.setView(view);
            dialog = builder.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = getWindowWidth() - 100;
            params.height = getWindowHeight() - 200;
            dialog.getWindow().setAttributes(params);
        } else {
            dialog.show();
        }
    }

    /**
     * 判断是否为累计点击5次且时间少于1600毫秒（调用一次即点击一次）
     *
     * @return
     */
    private boolean isFiveClick() {
        if (timeArr == null) {
            timeArr = new long[5];
        }
        System.arraycopy(timeArr, 1, timeArr, 0, timeArr.length - 1);
        timeArr[timeArr.length - 1] = System.currentTimeMillis();
        return System.currentTimeMillis() - timeArr[0] < 1600;
    }


    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getWindowWidth() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public int getWindowHeight() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.heightPixels;
    }


    public void onClick_rbAmerica(View view) {

        adapter = ArrayAdapter.createFromResource(mContext, R.array.arrayFreHop_us, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFreHop.setAdapter(adapter);
    }


    public void onClick_rbOthers(View view) {

        adapter = ArrayAdapter.createFromResource(mContext, R.array.arrayFreHop, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFreHop.setAdapter(adapter);

    }
}

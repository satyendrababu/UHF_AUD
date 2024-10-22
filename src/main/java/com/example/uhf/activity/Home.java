package com.example.uhf.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.uhf.R;
import com.example.uhf.Utilities.Common;
import com.example.uhf.Utilities.Tagging;
import com.example.uhf.adapter.AuditListAdapter;
import com.example.uhf.adapter.ListClickAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static com.lidroid.xutils.view.ResLoader.getColor;


public class Home extends Activity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{

    private int index = 0;
    private LinearLayout layout_inventory, layout_audit, layout_report, layout_tagging;
    private SliderLayout sliderLayout;
    TextView txtMarquee;

    public List<String> counter;
    View badge;
    ImageView badgeImage;
    TextView notificationCount;
    Button btnLogOut;
    Button btnPos, btnNeg;
    AuditListAdapter adapter;
    Tagging tagging;
    List<Tagging> taggingList;
    ListView listDialog;
    private AlertDialog dialog;
    private ProgressDialog pDialog;
    private SharedPreferences mSharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSharedPreferences = getSharedPreferences("UHF", MODE_PRIVATE);
        ActionBar actionBar = (ActionBar) getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        pDialog = new ProgressDialog(Home.this);
        //actionBar.setTitle("    ");
      //  actionBar.setLogo(R.drawable.banner);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.banner));
        actionBar.setTitle("");
        actionBar.setIcon(android.R.color.transparent);

        layout_inventory = (LinearLayout)findViewById(R.id.layout_inventory);
        layout_audit = (LinearLayout)findViewById(R.id.layout_audit);
        layout_report = (LinearLayout)findViewById(R.id.layout_report);
        layout_tagging = (LinearLayout)findViewById(R.id.layout_tagging);

        layout_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();
                Intent intent_inventory = new Intent(Home.this, UHFMainActivity.class);
                startActivity(intent_inventory);

            }
        });
        layout_audit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(Home.this, "Audit Clicked", Toast.LENGTH_SHORT).show();
                Intent intent_audit = new Intent(Home.this, AuditActivity.class);
                startActivity(intent_audit);
            }
        });
        layout_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(Home.this, "Report Clicked", Toast.LENGTH_SHORT).show();
                Intent intent_audit = new Intent(Home.this, ReportActivity.class);
                startActivity(intent_audit);
            }
        });
        layout_tagging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showLogoutDialog();
                Intent intent_audit = new Intent(Home.this, UHFTaggingActivity.class);
                startActivity(intent_audit);

            }
        });

        txtMarquee = (TextView)findViewById(R.id.txtMarquee);
        btnLogOut = findViewById(R.id.btnLogOut);
        txtMarquee.setSelected(true);

        sliderLayout = (SliderLayout)findViewById(R.id.slider);

        HashMap<String,Integer> file_maps = new HashMap<String, Integer>();
        file_maps.put("Asset Inventory Management System",R.drawable.asset);
        file_maps.put("Vehicle Tracking System",R.drawable.vts);
        file_maps.put("Library Management System",R.drawable.library);
        file_maps.put("Waste Management System", R.drawable.wms);

        for(String name : file_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            sliderLayout.addSlider(textSliderView);
        }
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(4000);
        sliderLayout.addOnPageChangeListener(this);

        //Badge layout for audit counter
        LinearLayout audit_badge = (LinearLayout)layout_audit.getChildAt(0);
        View view = audit_badge.getChildAt(1);
        badgeImage = (ImageView) view;
        badge = LayoutInflater.from(this).inflate(R.layout.badge_layout, audit_badge, false);
        notificationCount  = (TextView) badge.findViewById(R.id.notifications_badge);
        notificationCount.setText("56");
        //audit_badge.addView(badge);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        pDialog.dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // your code
            showCloseAlertDialog();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_logout:
                //showCloseAlertDialog();a
                showLogoutDialog();
                break;

            default:
                break;*/
        }

        return true;
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    public void showDialog()
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        //alertDialog.setTitle("Hello");
        TextView title = new TextView(Home.this);
        //title.setTextColor(getColor(Home.this, android.R.color.holo_red_dark));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT);
        title.setText("Are you sure!");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        title.setPadding(0,30,0,30);
        title.setLayoutParams(lp);

        title.setGravity(Gravity.CENTER);
        alertDialog.setCustomTitle(title);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view_sub_item = layoutInflater.inflate(R.layout.dialog_content,null);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();


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

    public void LogoutDialog()
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        //alertDialog.setTitle("Hello");
        TextView title = new TextView(Home.this);
        //title.setTextColor(getColor(Home.this, android.R.color.holo_red_dark));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT);
        title.setText("Are you sure you want to logout from this application?");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        title.setPadding(0,30,0,30);
        title.setLayoutParams(lp);

        title.setGravity(Gravity.CENTER);
        alertDialog.setCustomTitle(title);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view_sub_item = layoutInflater.inflate(R.layout.dialog_content,null);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Home.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //Paper.book().destroy();
                finish();


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
    public void showCloseAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        View view = LayoutInflater.from(Home.this).inflate(R.layout.sure_alert_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);
        TextView textMessage = (TextView) view.findViewById(R.id.textMessage);
        textMessage.setText("Are you sure want to Exit?");
        Button okBtn = (Button) view.findViewById(R.id.okBtn);
        Button cancelBtn = (Button)view.findViewById(R.id.cancelBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteOrderFromCSCart(orderId, dialog);
                finish();

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();
    }
    public void showLogoutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        View view = LayoutInflater.from(Home.this).inflate(R.layout.sure_alert_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);
        TextView textMessage = (TextView) view.findViewById(R.id.textMessage);
        Button okBtn = (Button) view.findViewById(R.id.okBtn);
        Button cancelBtn = (Button)view.findViewById(R.id.cancelBtn);
        textMessage.setText("Are you sure want to Logout?");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteOrderFromCSCart(orderId, dialog);
                //Paper.book().destroy();
                mSharedPreferences.edit().remove(Common.USER_KEY).apply();
                Intent intent = new Intent(Home.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();
    }

}

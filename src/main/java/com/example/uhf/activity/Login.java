package com.example.uhf.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.uhf.R;
import com.example.uhf.Utilities.AppUtils;
import com.example.uhf.Utilities.Common;
import com.example.uhf.Utilities.ServerUrls;
import com.example.uhf.Utilities.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Login extends Activity {

    private EditText edtUserName, edtPassword;
    private Button btnLogin;
    private Boolean notConnectedFlag = true;
    private EditText edtIpAddress, edtPort;
    private Button btnSaveIp;
    private CheckBox ckbRemember;
    Common common;
    ServerUrls serverUrls;
    private AlertDialog dialog;
    private Button btnCheckServerIp;
    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);
        mSharedPreferences = getSharedPreferences("UHF", MODE_PRIVATE);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setLogo(R.drawable.dspl);
        serverUrls = new ServerUrls();
        common = new Common();
        //actionBar.setCustomView(R.layout.custom_action_bar);
        //init paper for key values storage
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        ckbRemember = (CheckBox) findViewById(R.id.ckbRemember);
        btnCheckServerIp = (Button) findViewById(R.id.btnCheckServerIp);

        //Paper.init(this);

        validateConnection();
        //need to change local db
//        String user = Paper.book().read(Common.USER_KEY);
//        String pwd = Paper.book().read(Common.PWD_KEY);
        String user = mSharedPreferences.getString(Common.USER_KEY,"");
        String pwd = mSharedPreferences.getString(Common.PWD_KEY,"");

        if (user !=null) {
            if (!user.isEmpty()) {
                assert pwd != null;
                if (!pwd.isEmpty()) {
                    ckbRemember.setChecked(true);
                    validateUser(user, pwd);

                }
            }
        }


        btnCheckServerIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipAddressDialoge();
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //For temporary basis
                /*Intent intent = new Intent(Login.this, Home.class);
                startActivity(intent);
                finish();*/

                //Save user and password

                if (edtUserName.getText().toString().isEmpty() && edtPassword.getText().toString().isEmpty()) {
                    Toast.makeText(Login.this, "Enter User name and password", Toast.LENGTH_SHORT).show();
                } else {
                    validateUser(edtUserName.getText().toString(), edtPassword.getText().toString());
                    if (ckbRemember.isChecked()) {
                        //need to change local db
                        mSharedPreferences.edit().putString(Common.USER_KEY, edtUserName.getText().toString()).apply();
                        mSharedPreferences.edit().putString(Common.PWD_KEY, edtPassword.getText().toString()).apply();
                        /*Paper.book().write(Common.USER_KEY, edtUserName.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());*/
                    }
                }
            }
        });
        //Check remember



    }

    private void validateConnection() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_APP_CONNECTION,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("kya", "" + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("response");
                            if (result.equalsIgnoreCase("ok")) {
                                Toast.makeText(Login.this, "Success", Toast.LENGTH_SHORT).show();
                                notConnectedFlag = false;
                            } else {
                                //Toast.makeText(Login.this, "Failure", Toast.LENGTH_SHORT).show();
                                if (notConnectedFlag)
                                    showNetworkErrorDialog();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(Login.this, "Failure", Toast.LENGTH_SHORT).show();
                showNetworkErrorDialog();
            }
        }) {
            //Send data to server
        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    private void ipAddressDialoge() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        //alertDialog.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View ip_address_layout = inflater.inflate(R.layout.ip_address_layout, null);
        edtIpAddress = (EditText) ip_address_layout.findViewById(R.id.etIpAddress);
        edtPort = (EditText) ip_address_layout.findViewById(R.id.etPort);
        btnSaveIp = (Button) ip_address_layout.findViewById(R.id.btSave);

        /*String ip_port = ServerUrls.getDefaults("IPvalue", getApplicationContext());
        if (ip_port != null) {
            String ip[] = ip_port.split(":");
            String ip1 = ip[0];
            String port1 = ip[1];
            edtIpAddress.setText(ip1);
            edtPort.setText(port1);
        }
        else
        {

        }
        btnSaveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = edtIpAddress.getText().toString();
                String port = edtPort.getText().toString();
                if (ip.isEmpty() && port.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Enter Server IP and Port", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String ip_port = ip + ":" + port;
                    ServerUrls.setDefaults("IPvalue", ip_port, getApplicationContext());
                    Toast.makeText(getApplicationContext(), ip_port + "  has been set", Toast.LENGTH_LONG).show();
                    edtIpAddress.setText("");
                    edtPort.setText("");
                    finish();
                }
            }
        });*/
        String ip_port = common.readFile();

        if (!ip_port.isEmpty()) {
            String ip[] = ip_port.split(":");
            String ip1 = ip[0];
            String port1 = ip[1];
            edtIpAddress.setText(ip1);
            edtPort.setText(port1);
        } else {

        }
        btnSaveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = edtIpAddress.getText().toString();
                String port = edtPort.getText().toString();
                if (ip.isEmpty() && port.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter Server IP and Port", Toast.LENGTH_LONG).show();
                } else {
                    String ip_port = ip + ":" + port;
                    //Paper.book().write(Common.URL_CONTEXT,ip_port);
                    mSharedPreferences.edit().putString(Common.URL_CONTEXT,ip_port).apply();

                    if(common.saveFile(ip_port)) {
                        //ServerUrls.ContextUrl = "http://"+Common.readFile()+"/Inventory/";
                        Toast.makeText(getApplicationContext(), ip_port + "  has been set", Toast.LENGTH_LONG).show();
                        edtIpAddress.setText("");
                        edtPort.setText("");
                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(), "Ip Address not saved", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });

        alertDialog.setView(ip_address_layout);
        alertDialog.show();
    }

    private void validateUser(final String userName, final String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrls.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("response");
                            if (result.equalsIgnoreCase("success")) {
                                //Toast.makeText(Login.this, "Success", Toast.LENGTH_SHORT).show();
                                Common.userName = userName;
                                Intent intent = new Intent(Login.this, Home.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Incorrect user name or password", Toast.LENGTH_SHORT).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(Login.this, "Incorrect user name or password", Toast.LENGTH_SHORT).show();
                showNetworkErrorDialog();
                //Toast.makeText(Login.this, "Server Disconnected, Check Your Network", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap();
                params.put("user_id", userName);
                params.put("password", password);
                return params;
            }

        };
        VolleySingleton.getInstance(this).addToRequestQue(stringRequest);
    }

    public void showNetworkErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        View view = LayoutInflater.from(Login.this).inflate(R.layout.network_error_dialog, null);
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

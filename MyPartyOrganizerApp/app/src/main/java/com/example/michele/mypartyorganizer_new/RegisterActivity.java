package com.example.michele.mypartyorganizer_new;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private String URL;
    private EditText textName;
    private EditText textPassword;
    private EditText textCity;
    private EditText textAddress;
    private Button buttonRegister;
    private Button buttonLogin;
    private LinearLayout progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register_activity);
        URL = getString(R.string.server_address) +"organizers";
        if (Build.VERSION.SDK_INT < 26) {
            TextView logo = (TextView) findViewById(R.id.logo);
            logo.setTextSize(30.0f);
            logo.setTypeface(logo.getTypeface(), Typeface.ITALIC);
        }
        textName = (EditText) findViewById(R.id.name);
        textPassword = (EditText) findViewById(R.id.password);
        textCity = (EditText) findViewById(R.id.city);
        textAddress = (EditText) findViewById(R.id.address);
        buttonRegister = (Button) findViewById(R.id.register);
        buttonLogin = (Button) findViewById(R.id.login);
        progress = (LinearLayout) findViewById(R.id.layout_progress);
        buttonRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
    }

    // Register new account
    public void actionRegister() throws NoSuchAlgorithmException {
        buttonRegister.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        try {
            final String name = textName.getText().toString();
            final String password = textPassword.getText().toString();
            final String hash_psw = MainActivity.hash(password);
            final String city = textCity.getText().toString();
            final String address = textAddress.getText().toString();
            final String requestBody;
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("password", hash_psw);
            jsonBody.put("city", city);
            jsonBody.put("address", address);
            requestBody = jsonBody.toString();
            StringRequest req = new StringRequest(Request.Method.POST, URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            switch (response) {
                                case "Ok":
                                    AlertDialog.Builder build = new AlertDialog.Builder(RegisterActivity.this);
                                    build.setTitle(R.string.account_registered);
                                    build.setMessage(R.string.account_registered_dialog);
                                    build.setPositiveButton(R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                    finish();
                                                }
                                            });
                                    build.setIcon(android.R.drawable.ic_dialog_info);
                                    build.show();
                                    break;
                                case "Name already in use":
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                    builder.setTitle(R.string.name_already_in_use);
                                    builder.setMessage(R.string.name_already_in_use_dialog);
                                    builder.setPositiveButton(R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                    // Do nothing
                                                }
                                            });
                                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                                    builder.show();
                                    break;
                                default:
                                    AlertDialog.Builder b = new AlertDialog.Builder(RegisterActivity.this);
                                    b.setTitle(R.string.error);
                                    b.setMessage(R.string.error_dialog);
                                    b.setPositiveButton(R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                    // Do nothing
                                                }
                                            });
                                    b.setIcon(android.R.drawable.ic_dialog_alert);
                                    b.show();
                                    break;
                            }
                            progress.setVisibility(View.GONE);
                            buttonRegister.setVisibility(View.VISIBLE);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progress.setVisibility(View.GONE);
                            buttonRegister.setVisibility(View.VISIBLE);
                            Toast.makeText(RegisterActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        }
                    })
            {
                @Override
                public String getBodyContentType() {
                    return String.format("application/json; charset=utf-8");
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
            progress.setVisibility(View.GONE);
            buttonRegister.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v==buttonRegister) {
            if (checkFields()) {
                if (checkMaxLength()) {
                    try {
                        actionRegister();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    AlertDialog.Builder build = new AlertDialog.Builder(RegisterActivity.this);
                    build.setTitle(R.string.register_max_length_title);
                    build.setMessage(R.string.register_max_length);
                    build.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    // Do nothing
                                }
                            });
                    build.setIcon(android.R.drawable.ic_dialog_alert);
                    build.show();
                }
            }
            else {
                AlertDialog.Builder build = new AlertDialog.Builder(RegisterActivity.this);
                build.setTitle(R.string.field_missing);
                build.setMessage(R.string.field_missing_dialog);
                build.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                // Non fa niente
                            }
                        });
                build.setIcon(android.R.drawable.ic_dialog_alert);
                build.show();
            }
        }
        else if(v==buttonLogin) {
            // Go back to MainActivity
            finish();
        }
    }

    // Check if fields are empty
    public boolean checkFields() {
        boolean res = false;
        final String name = textName.getText().toString();
        final String password = textPassword.getText().toString();
        final String city = textCity.getText().toString();
        final String address = textAddress.getText().toString();
        if(name != null && !name.isEmpty() &&  password != null && !password.isEmpty() &&  city != null && !city.isEmpty() &&  address != null && !address.isEmpty())
            res = true;
        return res;
    }

    public boolean checkMaxLength() {
        boolean res = false;
        final String name = textName.getText().toString();
        final String password = textPassword.getText().toString();
        final String city = textCity.getText().toString();
        final String address = textAddress.getText().toString();
        if (name.length() <= 25 && password.length() <= 25 && city.length() <= 25 && address.length() <= 25)
            res = true;
        return res;
    }

}

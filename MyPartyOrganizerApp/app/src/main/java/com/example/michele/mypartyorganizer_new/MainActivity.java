package com.example.michele.mypartyorganizer_new;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText textName;
    private EditText textPassword;
    private Button buttonLogin;
    private Button buttonRegister;
    private String URL;
    private LinearLayout progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        URL = getString(R.string.server_address) + "autenticationOrg/";
        // If billabong font is not supported change the style of the logo
        if (Build.VERSION.SDK_INT < 26) {
            TextView logo = (TextView) findViewById(R.id.logo);
            logo.setTextSize(30.0f);
            logo.setTypeface(logo.getTypeface(), Typeface.ITALIC);
        }
        textName = (EditText) findViewById(R.id.name);
        textPassword = (EditText) findViewById(R.id.password);
        progress = (LinearLayout) findViewById(R.id.layout_progress);
        buttonLogin = (Button) findViewById(R.id.login);
        buttonLogin.setOnClickListener(this);
        buttonRegister = (Button) findViewById((R.id.register));
        buttonRegister.setOnClickListener(this);
    }

    // Login
    private void actionLogin() throws NoSuchAlgorithmException {
        buttonLogin.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        final String name = textName.getText().toString();
        final String password = textPassword.getText().toString();
        final String hash_psw = hash(password);
        StringRequest req = new StringRequest(Request.Method.GET, URL +name+ "/" +hash_psw,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progress.setVisibility(View.GONE);
                        buttonLogin.setVisibility(View.VISIBLE);
                        switch (response) {
                            case "Login done!":
                                Intent i = new Intent(getApplicationContext(), PartyListActivity.class);
                                Bundle b = new Bundle();
                                b.putString("name", name); // The name of the organizer
                                i.putExtras(b);
                                startActivity(i);
                                finish();
                                break;
                            case "Invalid credentials.":
                                AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                                build.setTitle(R.string.error);
                                build.setMessage(R.string.invalid_credentials);
                                build.setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface d, int id) {
                                                // Do nothing
                                            }
                                        });
                                build.setIcon(android.R.drawable.ic_dialog_alert);
                                build.show();
                                break;
                            default:
                                // Not specified error
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.error);
                                builder.setMessage(R.string.error_dialog);
                                builder.setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface d, int id) {
                                                // Do nothing
                                            }
                                        });
                                builder.setIcon(android.R.drawable.ic_dialog_alert);
                                builder.show();
                                break;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.setVisibility(View.GONE);
                        buttonLogin.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this,error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    @Override
    public void onClick(View v) {
        // Listener of click on LOGIN button
        if(v==buttonLogin) {
            if (checkFields()) {
                try {
                    actionLogin();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            else {
                // One or both fields are empty
                AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                build.setTitle(R.string.field_missing);
                build.setMessage(R.string.field_missing_dialog);
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
        // Listener of click on RegisterActivity link
        else if (v==buttonRegister) {
            // Switching to Register screen
            Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(i);
        }
    }

    // Check if fields are empty
    public boolean checkFields() {
        boolean res = false;
        final String name = textName.getText().toString();
        final String password = textPassword.getText().toString();
        if(name != null && !name.isEmpty() &&  password != null && !password.isEmpty())
            res = true;
        return res;
    }

    // Hash the password with MD5
    public static String hash(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte byteData[] = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++)
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        return sb.toString();
    }

}
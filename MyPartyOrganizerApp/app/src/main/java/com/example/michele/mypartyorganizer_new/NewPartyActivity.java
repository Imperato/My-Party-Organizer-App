package com.example.michele.mypartyorganizer_new;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import utils.DatePickerFragment;

public class NewPartyActivity extends AppCompatActivity implements View.OnClickListener {

    private int year;
    private int month;
    private int day;
    private String organizerName;
    private String organizerId;
    private String organizerCity;
    private String organizerAddress;
    private double latitude;
    private double longitude;
    static final int PICK_LOCATION_REQUEST = 1;  // The request code
    private EditText name;
    private Button create;
    private EditText city;
    private EditText address;
    private EditText price;
    private EditText description;
    private TextView txtLocation;
    private String URL;
    private LinearLayout progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_party_activity);
        URL = getString(R.string.server_address);
        Intent i = getIntent();
        organizerName = i.getStringExtra("name");
        organizerCity = i.getStringExtra("city");
        organizerAddress = i.getStringExtra("address");
        latitude = 0;
        longitude = 0;
        actionGetOrgId(organizerName);
        city = (EditText) findViewById(R.id.city);
        address = (EditText) findViewById(R.id.address);
        city.setText(organizerCity);
        address.setText(organizerAddress);
        name = (EditText) findViewById(R.id.name);
        city = (EditText) findViewById(R.id.city);
        address = (EditText) findViewById(R.id.address);
        price = (EditText) findViewById(R.id.price);
        description = (EditText) findViewById(R.id.description);
        txtLocation = (TextView) findViewById(R.id.location_text_view);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar1);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        // Empower the Up button on the ActionBar
        ab.setDisplayHomeAsUpEnabled(true);
        create = (Button) findViewById(R.id.create);
        create.setOnClickListener(this);
        progress = (LinearLayout) findViewById(R.id.layout_progress);
        ab.setTitle(R.string.new_party);
    }

    // Create a party
    public void createParty() throws ParseException, JSONException {
        create.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        // Get the date
        String date = String.valueOf(year)+ "/" +String.valueOf(month)+ "/" +String.valueOf(day);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date parsed = format.parse(date);
        java.sql.Date sql = new java.sql.Date(parsed.getTime());
        // Get the other data
        String partyName = name.getText().toString();
        String partyCity = city.getText().toString();
        String partyAddress = address.getText().toString();
        String partyPrice = price.getText().toString();
        String partyDescription = description.getText().toString();
        // Create the body of the request
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", partyName);
        jsonBody.put("organizerId", organizerId);
        jsonBody.put("organizerName", organizerName);
        jsonBody.put("date", String.valueOf(sql));
        jsonBody.put("city", partyCity);
        jsonBody.put("address", partyAddress);
        jsonBody.put("price", partyPrice);
        jsonBody.put("description", partyDescription);
        jsonBody.put("latitude", String.valueOf(latitude));
        jsonBody.put("longitude", String.valueOf(longitude));
        // POST request
        final String requestBody = jsonBody.toString();
        StringRequest req = new StringRequest(Request.Method.POST, URL + "parties",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "Ok":
                                AlertDialog.Builder build = new AlertDialog.Builder(NewPartyActivity.this);
                                build.setTitle(R.string.party_created);
                                build.setMessage(R.string.party_created_dialog);
                                build.setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface d, int id) {
                                                finish();
                                            }
                                        });
                                build.setIcon(android.R.drawable.ic_dialog_info);
                                build.show();
                                break;
                            default:
                                AlertDialog.Builder b = new AlertDialog.Builder(NewPartyActivity.this);
                                b.setTitle(R.string.error);
                                b.setMessage(R.string.error_dialog);
                                b.setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface d, int id) {
                                                // Non fa niente
                                            }
                                        });
                                b.setIcon(android.R.drawable.ic_dialog_alert);
                                b.show();
                                break;
                        }
                        progress.setVisibility(View.GONE);
                        create.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NewPartyActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        create.setVisibility(View.VISIBLE);
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
    }

    // Get the user's id from his name
    public void actionGetOrgId(String name) {
        StringRequest req = new StringRequest(Request.Method.GET, URL +"organizers/" +name,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "Bad":
                                AlertDialog.Builder build = new AlertDialog.Builder(NewPartyActivity.this);
                                build.setTitle(R.string.error);
                                build.setMessage(R.string.error_id_dialog);
                                build.setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface d, int id) {
                                                // Non fa niente
                                            }
                                        });
                                build.setIcon(android.R.drawable.ic_dialog_alert);
                                build.show();
                                break;
                            default:
                                organizerId = response;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NewPartyActivity.this,error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // User clicked Up button
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_party, menu);
        return true;
    }

    // Show the Calendar Dialog to pick the date
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    // Get the date from DatePickerDialog
    public void setDate(int year, int month, int day) {
        int month1 = ++month;
        this.day=day;
        this.month=month1;
        this.year=year;
        String showDate = String.valueOf(day)+ "/" +String.valueOf(month1)+ "/" +String.valueOf(year);
        TextView date_text_view = (TextView) findViewById(R.id.date_text_view);
        date_text_view.setText(showDate);
    }

    // Start MapActivity to pick the location
    public void startMapActivityForResult(View v) {
        Intent i = new Intent(getApplicationContext(), MapActivity.class);
        i.putExtra("latitude", latitude);
        i.putExtra("longitude", longitude);
        startActivityForResult(i,PICK_LOCATION_REQUEST);
    }

    // Get the location from MapActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request I'm responding to
        if (requestCode == PICK_LOCATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                latitude = data.getDoubleExtra("latitude",0);
                longitude = data.getDoubleExtra("longitude",0);
                String myLat = String.valueOf(latitude);
                String myLong = String.valueOf(longitude);
                String myLat1 = myLat.substring(0, Math.min(myLat.length(),10));
                String myLong1 = myLong.substring(0, Math.min(myLong.length(),10));
                String location = myLat1 + " " + myLong1;
                txtLocation.setText(location);
            }
        }
    }

    // Listener of click on CREATE button
    @Override
    public void onClick(View v) {
        if (v == create){
            if (checkFields()) {
                if (checkMaxLength()) {
                    if (checkDescriptionMaxLength()) {
                        try {
                            createParty();
                        } catch (ParseException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        AlertDialog.Builder build = new AlertDialog.Builder(NewPartyActivity.this);
                        build.setTitle(R.string.description_max_length_title);
                        build.setMessage(R.string.description_max_length);
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
                    AlertDialog.Builder build = new AlertDialog.Builder(NewPartyActivity.this);
                    build.setTitle(R.string.new_party_max_length_title);
                    build.setMessage(R.string.new_party_max_length);
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
                AlertDialog.Builder build = new AlertDialog.Builder(NewPartyActivity.this);
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
    }

    public boolean checkFields() {
        boolean res = false;
        final String name1 = name.getText().toString();
        final String city1 = city.getText().toString();
        final String address1 = address.getText().toString();
        final String price1 = price.getText().toString();
        final String description1 = description.getText().toString();
        if(name1 != null && !name1.isEmpty() &&  city1 != null && !city1.isEmpty() &&  address1 != null && !address1.isEmpty() &&  price1 != null && !price1.isEmpty() &&  description1 != null && !description1.isEmpty() &&  year != 0 &&  month != 0 &&  day != 0)
            res = true;
        return res;
    }

    public boolean checkMaxLength() {
        boolean res = false;
        final String name1 = name.getText().toString();
        final String city1 = city.getText().toString();
        final String address1 = address.getText().toString();
        if (name1.length() <= 90 && city1.length() <= 90 && address1.length() <= 90)
            res = true;
        return res;
    }

    public boolean checkDescriptionMaxLength() {
        boolean res = false;
        final String description1 = description.getText().toString();
        if (description1.length() <= 2800)
            res = true;
        return res;
    }

}

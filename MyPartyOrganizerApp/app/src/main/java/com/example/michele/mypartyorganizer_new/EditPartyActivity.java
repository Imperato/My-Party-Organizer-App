package com.example.michele.mypartyorganizer_new;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import utils.DatePickerFragment2;

public class EditPartyActivity extends AppCompatActivity implements View.OnClickListener {

    private String id;
    private String organizerName;
    private String name;
    private String date;
    private String city;
    private String address;
    private double price;
    private String description;
    private int day;
    private int month;
    private int year;
    private double latitude;
    private double longitude;
    private EditText txtName;
    private TextView txtDate;
    private TextView txtLocation;
    private EditText txtCity;
    private EditText txtAddress;
    private EditText txtPrice;
    private EditText txtDescription;
    private Button btnEdit;
    private String URL;
    static final int PICK_LOCATION_REQUEST = 1;  // The request code
    private LinearLayout progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_party_activity);
        URL = getString(R.string.server_address) + "parties/";
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar3);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        // Abilita il bottone up
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.edit_party);
        txtName = (EditText) findViewById(R.id.name);
        txtDate = (TextView) findViewById(R.id.date_text_view);
        txtLocation = (TextView) findViewById(R.id.location_text_view);
        txtCity = (EditText) findViewById(R.id.city);
        txtAddress = (EditText) findViewById(R.id.address);
        txtPrice = (EditText) findViewById(R.id.price);
        txtDescription = (EditText) findViewById(R.id.description);
        Intent i = getIntent();
        id = i.getStringExtra("id");
        organizerName = i.getStringExtra("organizerName");
        name = i.getStringExtra("name");
        date = i.getStringExtra("date");
        city = i.getStringExtra("city");
        address = i.getStringExtra("address");
        price = i.getDoubleExtra("price", 0);
        description = i.getStringExtra("description");
        day = i.getIntExtra("day", 0);
        month = i.getIntExtra("month", 0);
        year = i.getIntExtra("year", 0);
        latitude = i.getDoubleExtra("latitude", 0);
        longitude = i.getDoubleExtra("longitude", 0);
        actionFillFields();
        btnEdit = (Button) findViewById(R.id.edit);
        btnEdit.setOnClickListener(this);
        progress = (LinearLayout) findViewById(R.id.layout_progress);
    }

    // Edit a party
    public void editParty(String id) throws ParseException, JSONException {
        btnEdit.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        // Get the date
        String date = String.valueOf(year)+ "/" +String.valueOf(++month)+ "/" +String.valueOf(day);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date parsed = format.parse(date);
        java.sql.Date sql = new java.sql.Date(parsed.getTime());
        // Get the other data
        String partyName = String.valueOf(txtName.getText());
        String partyCity = String.valueOf(txtCity.getText());
        String partyAddress = String.valueOf(txtAddress.getText());
        String partyPrice = String.valueOf(txtPrice.getText());
        String partyDescription = String.valueOf(txtDescription.getText());
        // Create the body
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", partyName);
        jsonBody.put("date", String.valueOf(sql));
        jsonBody.put("city", partyCity);
        jsonBody.put("address", partyAddress);
        jsonBody.put("price", partyPrice);
        jsonBody.put("description", partyDescription);
        jsonBody.put("latitude", String.valueOf(latitude));
        jsonBody.put("longitude", String.valueOf(longitude));
        // PUT request
        final String requestBody = jsonBody.toString();
        StringRequest req = new StringRequest(Request.Method.PUT, URL +id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "Ok":
                                AlertDialog.Builder build = new AlertDialog.Builder(EditPartyActivity.this);
                                build.setTitle(R.string.party_edited);
                                build.setMessage(R.string.party_edited_dialog);
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
                                AlertDialog.Builder b = new AlertDialog.Builder(EditPartyActivity.this);
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
                        btnEdit.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditPartyActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        btnEdit.setVisibility(View.VISIBLE);
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

    @Override
    public void onClick(View v) {
        if (v == btnEdit) {
            if (checkFields()) {
                if (checkMaxLength()) {
                    if (checkDescriptionMaxLength()) {
                        try {
                            editParty(id);
                        } catch (ParseException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        AlertDialog.Builder build = new AlertDialog.Builder(EditPartyActivity.this);
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
                    AlertDialog.Builder build = new AlertDialog.Builder(EditPartyActivity.this);
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
                AlertDialog.Builder build = new AlertDialog.Builder(EditPartyActivity.this);
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
    }

    public void actionFillFields() {
        txtName.setText(name);
        txtDate.setText(date);
        txtCity.setText(city);
        txtAddress.setText(address);
        if (latitude != 0 && longitude != 0) {
            String myLat = String.valueOf(latitude);
            String myLong = String.valueOf(longitude);
            String myLat1 = myLat.substring(0, Math.min(myLat.length(),10));
            String myLong1 = myLong.substring(0, Math.min(myLong.length(),10));
            String location = myLat1 + " " + myLong1;
            txtLocation.setText(location);
        }
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        // Change the separator from , to .
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("0.00", otherSymbols);
        txtPrice.setText(formatter.format(price));
        txtDescription.setText(description);
    }

    public boolean checkFields() {
        boolean res = false;
        String partyName = String.valueOf(txtName.getText());
        String partyCity = String.valueOf(txtCity.getText());
        String partyAddress = String.valueOf(txtAddress.getText());
        String partyPrice = String.valueOf(txtPrice.getText());
        String partyDescription = String.valueOf(txtDescription.getText());
        if(partyName != null && !partyName.isEmpty() &&  partyCity != null && !partyCity.isEmpty() &&  partyAddress != null && !partyAddress.isEmpty() &&  partyPrice != null && !partyPrice.isEmpty() &&  partyDescription != null && !partyDescription.isEmpty())
            res = true;
        return res;
    }

    public boolean checkMaxLength() {
        boolean res = false;
        final String name1 = txtName.getText().toString();
        final String city1 = txtCity.getText().toString();
        final String address1 = txtAddress.getText().toString();
        if (name1.length() <= 90 && city1.length() <= 90 && address1.length() <= 90)
            res = true;
        return res;
    }

    public boolean checkDescriptionMaxLength() {
        boolean res = false;
        final String description1 = txtDescription.getText().toString();
        if (description1.length() <= 2800)
            res = true;
        return res;
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment2 newFragment = new DatePickerFragment2();
        Bundle args = new Bundle();
        args.putInt("day", day);
        args.putInt("month", month);
        args.putInt("year", year);
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void setDate(int year, int month, int day) {
        this.day=day;
        this.month=month;
        this.year=year;
        String showDate = String.valueOf(day)+ "/" +String.valueOf(++month)+ "/" +String.valueOf(year);
        TextView date_text_view = (TextView) findViewById(R.id.date_text_view);
        date_text_view.setText(showDate);
    }

    public void startMapActivityForResult(View v) {
        Intent i = new Intent(getApplicationContext(), MapActivity.class);
        i.putExtra("latitude", latitude);
        i.putExtra("longitude", longitude);
        startActivityForResult(i,PICK_LOCATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// User clicked up button
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

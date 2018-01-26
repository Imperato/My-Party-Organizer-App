package com.example.michele.mypartyorganizer_new;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PartyActivity extends AppCompatActivity {

    private String id;
    private String organizerName;
    private String name;
    private String date;
    private String date_parsed;
    private String city;
    private String address;
    private int tickets;
    private double price;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private TextView txtName;
    private TextView txtDate;
    private TextView txtLocation;
    private TextView txtTickets;
    private TextView txtPrice;
    private TextView txtDescription;
    private String URL;
    private LinearLayout progress;
    private LinearLayout l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_details_activity);
        progress = (LinearLayout)findViewById(R.id.layout_progress);
        l = (LinearLayout)findViewById(R.id.linear_layout);
        URL = getString(R.string.server_address) + "parties/";
        Intent i = getIntent();
        id = i.getStringExtra("id");
        date = i.getStringExtra("date");
        organizerName = i.getStringExtra("organizerName");
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar2);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.details);
        txtName = (TextView) findViewById(R.id.name);
        txtName.setSelected(true); //To let the view scroll if name is too long
        txtDate = (TextView) findViewById(R.id.date);
        txtLocation = (TextView) findViewById(R.id.location);
        txtLocation.setSelected(true);
        txtTickets = (TextView) findViewById(R.id.tickets);
        txtPrice = (TextView) findViewById(R.id.price);
        txtDescription = (TextView) findViewById(R.id.description);
        actionPopulateActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        l.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        actionPopulateActivity();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        l.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        actionPopulateActivity();
    }

    // Populate the activity with the data of a party
    public void actionPopulateActivity() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "details/" +id,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            name = response.getString("name");
                            city = response.getString("city");
                            address = response.getString("address");
                            date = response.getString("date");
                            location = city+ ", " +address;
                            tickets = response.getInt("tickets");
                            price = response.getDouble("price");
                            description = response.getString("description");
                            latitude = response.getDouble("latitude");
                            longitude = response.getDouble("longitude");
                            txtName.setText(name);
                            // Show date with the correct pattern
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date d = format.parse(String.valueOf(date));
                                format.applyPattern("dd/MM/yyyy");
                                date_parsed = String.valueOf(format.format(d));
                                txtDate.setText(String.valueOf(format.format(d)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            txtLocation.setText(location);
                            String t = getString(R.string.tickets_sold)+ " " +String.valueOf(tickets);
                            txtTickets.setText(t);
                            DecimalFormat formatter = new DecimalFormat("€ 0.00");
                            txtPrice.setText(formatter.format(price));
                            txtDescription.setText(description);
                            progress.setVisibility(View.GONE);
                            l.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
                        Toast.makeText(PartyActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            public String getBodyContentType() {
                return String.format("application/json; charset=utf-8");
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.party_details, menu);
        return true;
    }

    // Delete a party
    private void actionDeleteParty() throws NoSuchAlgorithmException {
        l.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        StringRequest req = new StringRequest(Request.Method.DELETE, URL +id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Ok")) {
                            AlertDialog.Builder build = new AlertDialog.Builder(PartyActivity.this);
                            build.setTitle(R.string.party_deleted);
                            build.setMessage(R.string.party_deleted_dialog);
                            build.setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d, int id) {
                                            finish();
                                        }
                                    });
                            build.setIcon(android.R.drawable.ic_dialog_info);
                            build.show();
                        }
                        else {
                            AlertDialog.Builder build = new AlertDialog.Builder(PartyActivity.this);
                            build.setTitle(R.string.error);
                            build.setMessage(R.string.error_dialog);
                            build.setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d, int id) {
                                            // Non fa niente
                                        }
                                    });
                            build.setIcon(android.R.drawable.ic_dialog_alert);
                            build.show();
                        }
                        progress.setVisibility(View.GONE);
                        l.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        l.setVisibility(View.VISIBLE);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                // Edit the party
                // Break up the date of the party into day, month and year and send it to EditPartyActivity
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                try {
                    Date parsed = format.parse(date);
                    cal.setTime(parsed);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                Intent i = new Intent(PartyActivity.this, EditPartyActivity.class);
                i.putExtra("id", id);
                i.putExtra("name", name);
                i.putExtra("date", date_parsed);
                i.putExtra("city", city);
                i.putExtra("address", address);
                i.putExtra("price", price);
                i.putExtra("tickets", tickets);
                i.putExtra("description", description);
                i.putExtra("day", day);
                i.putExtra("month", month);
                i.putExtra("year", year);
                i.putExtra("organizerName", organizerName);
                i.putExtra("latitude", latitude);
                i.putExtra("longitude", longitude);
                startActivity(i);
                return true;
            case R.id.delete:
                // Delete the party
                AlertDialog.Builder build = new AlertDialog.Builder(this);
                build.setTitle(R.string.delete_party);
                build.setMessage(R.string.delete_party_dialog);
                build.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                try {
                                    actionDeleteParty();
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                build.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                // Don't delete party
                            }
                        });
                build.setIcon(android.R.drawable.ic_dialog_alert);
                build.show();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

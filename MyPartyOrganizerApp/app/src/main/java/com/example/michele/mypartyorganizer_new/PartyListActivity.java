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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import pojo.PartyRow;
import utils.MyListAdapter;

public class PartyListActivity extends AppCompatActivity {

    private String name;
    private String city;
    private String address;
    private String URL;
    private LinearLayout progress;
    private LinearLayout no_parties;
    private ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_list_activity);
        URL = getString(R.string.server_address);
        // Get an ActionBar corresponding to this Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.my_parties);
        name = getIntent().getStringExtra("name");
        progress = (LinearLayout) findViewById(R.id.layout_progress);
        lv = (ListView) findViewById(R.id.list_view);
        no_parties = (LinearLayout) findViewById(R.id.no_parties);
        try {
            actionGetOrganizerParties(name);
            actionGetCityAddress(name);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // Listener of click on party list element
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final PartyRow item = (PartyRow) parent.getItemAtPosition(position);
                Intent i = new Intent(PartyListActivity.this, PartyActivity.class);
                i.putExtra("id", item.getId());
                i.putExtra("date", String.valueOf(item.getDate()));
                i.putExtra("organizerName", name);
                startActivity(i);
            }
        });
    }

    // Update data of the parties when go back to this Activity
    @Override
    public void onResume() {
        super.onResume();
        // Show progress spinner
        lv.setVisibility(View.GONE);
        no_parties.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        try {
            actionGetOrganizerParties(name);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        lv.setVisibility(View.GONE);
        no_parties.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        try {
            actionGetOrganizerParties(name);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Get the parties created by the user
    private void actionGetOrganizerParties(String name) throws NoSuchAlgorithmException {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL +"parties/" +name,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<PartyRow> partyList = new ArrayList<>();
                        ListView lv = (ListView) findViewById(R.id.list_view);
                        // Process the JSON
                        try{
                            // Loop through the array elements
                            for(int i=0;i<response.length();i++){
                                // Get current json object
                                JSONObject party = response.getJSONObject(i);
                                // Get the current party (json object) data
                                String partyId = party.getString("id");
                                String partyName = party.getString("name");
                                String partyDate = party.getString("date");
                                int partyTickets = party.getInt("tickets");
                                // Convert partyDate String into java.sql.Date
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date parsed = format.parse(partyDate);
                                java.sql.Date sql = new java.sql.Date(parsed.getTime());
                                // Create and add the new line
                                PartyRow pr = new PartyRow(partyId, partyName,sql,partyTickets);
                                partyList.add(pr);
                            }
                            // Hide progress spinner
                            progress.setVisibility(View.GONE);
                            if (partyList.isEmpty())
                                no_parties.setVisibility(View.VISIBLE);
                            else {
                                lv.setVisibility(View.VISIBLE);
                                MyListAdapter a = new MyListAdapter(PartyListActivity.this, R.layout.party, partyList);
                                lv.setAdapter(a);
                            }
                        } catch (JSONException | ParseException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(PartyListActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            public String getBodyContentType() {
                return String.format("application/json; charset=utf-8");
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    // Delete account
    private void actionDeleteAccount(String name) throws NoSuchAlgorithmException {
        lv.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        StringRequest req = new StringRequest(Request.Method.DELETE, URL +"organizers/" +name,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Ok")) {
                            AlertDialog.Builder build = new AlertDialog.Builder(PartyListActivity.this);
                            build.setTitle(R.string.account_deleted);
                            build.setMessage(R.string.account_deleted_dialog);
                            build.setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d, int id) {
                                            Intent i = new Intent(PartyListActivity.this, MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                            build.setIcon(android.R.drawable.ic_dialog_info);
                            build.show();
                        }
                        else {
                            AlertDialog.Builder build = new AlertDialog.Builder(PartyListActivity.this);
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
                        lv.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        lv.setVisibility(View.VISIBLE);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    // Get city and address of the user from his name
    public void actionGetCityAddress(String name) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL +"organizers/location/" +name,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            city=response.getString("city");
                            address=response.getString("address");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(PartyListActivity.this, error.toString(), Toast.LENGTH_LONG).show();
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

    // Create options icon on the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add elements to the ActionBar
        getMenuInflater().inflate(R.menu.settings_item, menu);
        return true;
    }

    // When user clicks on one of the ActionBar options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                // Logout
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.delete_account:
                // Delete account
                AlertDialog.Builder build = new AlertDialog.Builder(this);
                build.setTitle(R.string.delete_account);
                build.setMessage(R.string.delete_account_dialog);
                build.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                // If user is sure delete account
                                try {
                                    actionDeleteAccount(name);
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                build.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                // Don't delete account
                            }
                        });
                build.setIcon(android.R.drawable.ic_dialog_alert);
                build.show();
                return true;
            case R.id.new_party:
                // Add new party
                Intent in = new Intent(getApplicationContext(), NewPartyActivity.class);
                // The user data
                in.putExtra("name", name);
                in.putExtra("city", city);
                in.putExtra("address", address);
                startActivity(in);
            default:
                // User action was not recognized
                return super.onOptionsItemSelected(item);
        }
    }

}

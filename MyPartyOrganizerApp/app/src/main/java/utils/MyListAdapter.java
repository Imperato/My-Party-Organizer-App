package utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.michele.mypartyorganizer_new.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pojo.PartyRow;

/**
 * Created by michele on 29/08/17.
 */

public class MyListAdapter extends ArrayAdapter<PartyRow> {

    public MyListAdapter(Context context, int resource, List<PartyRow> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.party, null);
        }
        PartyRow p = getItem(position);
        if (p != null) {
            TextView tt1 = v.findViewById(R.id.party_name);
            TextView tt2 = v.findViewById(R.id.party_date);
            TextView tt3 = v.findViewById(R.id.party_tickets);
            if (tt1 != null) {
                tt1.setText(p.getName());
            }
            if (tt2 != null) {
                java.sql.Date d = p.getDate();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = format.parse(String.valueOf(d));
                    format.applyPattern("dd/MM/yyyy");
                    tt2.setText(String.valueOf(format.format(date)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (tt3 != null) {
                String s1 = getContext().getString(R.string.tickets);
                String s = s1+ " " +String.valueOf(p.getTickets());
                tt3.setText(s);
            }
        }
        return v;
    }

}

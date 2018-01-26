package utils;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.example.michele.mypartyorganizer_new.EditPartyActivity;

/**
 * Created by michele on 13/09/17.
 */

public class DatePickerFragment2 extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private int year;
    private int month;
    private int day;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        day = getArguments().getInt("day");
        month = getArguments().getInt("month");
        year = getArguments().getInt("year");
        // Create a new instance of DataPickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet (DatePicker view, int year, int month, int day) {
        ((EditPartyActivity) getActivity()).setDate(year, month, day);
    }
}

package seemoo.fitbit.interactions;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import seemoo.fitbit.activities.MainFragment;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Utilities;

/**
 * Lets the user set a new date for the device.
 */
class SetDateInteraction extends BluetoothInteraction {

    private MainFragment mainFragment;
    private Toast toast;
    private Commands commands;
    private Calendar calendar;
    private boolean result = false;

    /**
     * Creates an instance of set date interaction.
     *
     * @param mainFragment The current mainFragment.
     * @param toast    The toast, to send messages to the user.
     * @param commands The instance of commands.
     */
    SetDateInteraction(MainFragment mainFragment, Toast toast, Commands commands) {
        this.mainFragment = mainFragment;
        this.toast = toast;
        this.commands = commands;
        calendar = Calendar.getInstance();
    }

    /**
     * {@inheritDoc}
     * Returns, whether the set date process is finished by the user or not.
     *
     * @return True, if the set date process is finished by the user.
     */
    @Override
    boolean isFinished() {
        return result;
    }

    /**
     * {@inheritDoc}
     * Enables notifications and pauses this interaction, until the user has picked a date.
     *
     * @return True.
     */
    @Override
    boolean execute() {
        commands.comEnableNotifications1();
        selectDate();
        setTimer(-1);
        return true;
    }

    /**
     * {@inheritDoc}
     * Checks, if the device acknowledges the set date.
     *
     * @param value The received data.
     * @return True, if the set date is acknowledged by the device. If not, null.
     */
    @Override
    InformationList interact(final byte[] value) {
        if (Utilities.byteArrayToHexString(value).equals(ConstantValues.ACKNOWLEDGEMENT)) {
            result = true;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Disables notifications and shows, whether the date is set or not.
     *
     * @return Null.
     */
    @Override
    InformationList finish() {
        commands.comDisableNotifications1();
        if (result) {
            mainFragment.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText("Date set.");
                    toast.show();
                }
            });
            Log.e(TAG, "Date set.");
        } else {
            mainFragment.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText("Error: Date not set.");
                    toast.show();
                }
            });
            Log.e(TAG, "Error: Date not set.");
        }
        return null;
    }

    /**
     * Lets the user select the date to override.
     */
    private void selectDate() {
        final int currentYear = calendar.get(Calendar.YEAR);
        final int currentMonth = calendar.get(Calendar.MONTH);
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        mainFragment.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                DatePickerDialog mDatePickerDialog = new DatePickerDialog(mainFragment.getActivity(), new DatePickerDialog.OnDateSetListener() {


                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectTime();
                    }
                }, currentYear, currentMonth, currentDay);
                mDatePickerDialog.setTitle("Select date:");
                mDatePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "", mDatePickerDialog);
                mDatePickerDialog.setCancelable(false);
                mDatePickerDialog.show();
            }
        });
    }

    /**
     * Lets the user select the time to override.
     */
    private void selectTime() {
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMinute = calendar.get(Calendar.MINUTE);
        mainFragment.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TimePickerDialog mTimePickerDialog = new TimePickerDialog(mainFragment.getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        commands.comSetDate(Utilities.longToHexString(calendar.getTime().getTime() / 1000));
                        Log.e(TAG, "Date: " + calendar.getTime().toString());
                        setTimer(1000);
                    }
                }, currentHour, currentMinute, true);
                mTimePickerDialog.setTitle("Select time:");
                mTimePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "", mTimePickerDialog);
                mTimePickerDialog.setCancelable(false);
                mTimePickerDialog.show();
            }
        });
    }

}

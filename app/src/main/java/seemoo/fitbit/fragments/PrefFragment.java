package seemoo.fitbit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;

import static android.content.Context.MODE_PRIVATE;

public class PrefFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);
        setHasOptionsMenu(false);

        //Check for the three Settings if they are set in the SharedPrefereneces and check the Boxes correspondingly

        final String keyAdditionalRawOutput = getResources().getString(R.string.settings_workactivity_1);
        CheckBoxPreference checkBoxAdditionalRawOutput = (CheckBoxPreference) findPreference(keyAdditionalRawOutput);
        final SharedPreferences spAdditionalRawOutput = this.getActivity().getSharedPreferences(keyAdditionalRawOutput, MODE_PRIVATE);
        checkBoxAdditionalRawOutput.setChecked(spAdditionalRawOutput.getBoolean(keyAdditionalRawOutput, false));

        final String keyAdditionalAlarmInformation = getResources().getString(R.string.settings_workactivity_2);
        CheckBoxPreference checkBoxAdditionalAlarmInformation = (CheckBoxPreference) findPreference(keyAdditionalAlarmInformation);
        final SharedPreferences spAdditionalAlarmInformation = this.getActivity().getSharedPreferences(keyAdditionalAlarmInformation, MODE_PRIVATE);
        checkBoxAdditionalAlarmInformation.setChecked(spAdditionalAlarmInformation.getBoolean(keyAdditionalAlarmInformation, false));

        final String keySaveDumpInformation = getResources().getString(R.string.settings_workactivity_3);
        CheckBoxPreference checkBoxSaveDumpInformation = (CheckBoxPreference) findPreference(keySaveDumpInformation);
        final SharedPreferences spSaveDumpInformation = this.getActivity().getSharedPreferences(keySaveDumpInformation, MODE_PRIVATE);
        checkBoxSaveDumpInformation.setChecked(spSaveDumpInformation.getBoolean(keySaveDumpInformation, false));

        final String keyChangeDirectory = getResources().getString(R.string.settings_workactivity_4);
        Preference prefChangeDirectory = findPreference(keyChangeDirectory);

        // Each Setting needs an own OnClickListener, which sets the Boolean in the SharedPreferences
        // The Listener is called after the CheckBox is checked/unchecked, so cB.isChecked gives the correct Value
        checkBoxAdditionalRawOutput.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cB = (CheckBoxPreference) preference;

                SharedPreferences.Editor editor = spAdditionalRawOutput.edit();
                editor.putBoolean(keyAdditionalRawOutput, cB.isChecked());
                editor.apply();

                return true;
            }
        });

        checkBoxAdditionalAlarmInformation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cB = (CheckBoxPreference) preference;

                SharedPreferences.Editor editor = spAdditionalAlarmInformation.edit();
                editor.putBoolean(keyAdditionalAlarmInformation, cB.isChecked());
                editor.apply();

                return true;
            }
        });

        checkBoxSaveDumpInformation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cB = (CheckBoxPreference) preference;

                SharedPreferences.Editor editor = spSaveDumpInformation.edit();
                editor.putBoolean(keySaveDumpInformation, cB.isChecked());
                editor.apply();

                return true;
            }
        });

        prefChangeDirectory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                triggerDirectoryFragment();
                return true;
            }
        });
    }

    //This method opens the DirectoryFragment form the WorkActivity / MainFragment
    private void triggerDirectoryFragment(){
        WorkActivity activity = (WorkActivity) this.getActivity();

        activity.changeToDirectoryPickerFragment();
    }
}

package seemoo.fitbit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.view.menu.ActionMenuItem;
import android.support.v7.view.menu.MenuItemWrapperICS;
import android.view.MenuItem;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;

import static android.content.Context.MODE_PRIVATE;

public class prefFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);
        setHasOptionsMenu(false);

        //Check for the three Settings if they are set in the SharedPrefereneces and check the Boxes correspondingly

        final String setting1 = getResources().getString(R.string.settings_workactivity_1);
        CheckBoxPreference checkBox1 = (CheckBoxPreference) findPreference(setting1);
        final SharedPreferences sharedPref1 = this.getActivity().getSharedPreferences(setting1, MODE_PRIVATE);
        checkBox1.setChecked(sharedPref1.getBoolean(setting1, false));

        final String setting2 = getResources().getString(R.string.settings_workactivity_2);
        CheckBoxPreference checkBox2 = (CheckBoxPreference) findPreference(setting2);
        final SharedPreferences sharedPref2 = this.getActivity().getSharedPreferences(setting2, MODE_PRIVATE);
        checkBox2.setChecked(sharedPref2.getBoolean(setting2, false));

        final String setting3 = getResources().getString(R.string.settings_workactivity_3);
        CheckBoxPreference checkBox3 = (CheckBoxPreference) findPreference(setting3);
        final SharedPreferences sharedPref3 = this.getActivity().getSharedPreferences(setting3, MODE_PRIVATE);
        checkBox3.setChecked(sharedPref3.getBoolean(setting3, false));

        final String setting4 = getResources().getString(R.string.settings_workactivity_4);
        Preference pref4 = findPreference(setting4);

        // Each Setting needs an own OnClickListener, which sets the Boolean in the SharedPreferences
        // The Listener is called after the CheckBox is checked/unchecked, so cB.isChecked gives the correct Value
        checkBox1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cB = (CheckBoxPreference) preference;

                SharedPreferences.Editor editor = sharedPref1.edit();
                editor.putBoolean(setting1, cB.isChecked());
                editor.apply();

                return true;
            }
        });

        checkBox2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cB = (CheckBoxPreference) preference;

                SharedPreferences.Editor editor = sharedPref2.edit();
                editor.putBoolean(setting2, cB.isChecked());
                editor.apply();

                return true;
            }
        });

        checkBox3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cB = (CheckBoxPreference) preference;

                SharedPreferences.Editor editor = sharedPref3.edit();
                editor.putBoolean(setting3, cB.isChecked());
                editor.apply();



                return true;
            }
        });

        pref4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

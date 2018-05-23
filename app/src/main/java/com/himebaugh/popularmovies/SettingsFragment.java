package com.himebaugh.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        // Add visualizer preferences, defined in the XML file in res->xml->pref_visualizer
        addPreferencesFromResource(R.xml.pref_settings);

        // Get the preference screen
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        // Get the number of preferences
        int count = preferenceScreen.getPreferenceCount();

        // Go through all of the preferences, and set up their preference summary.
        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            // You don't need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }

        Preference preference = findPreference(getString(R.string.pref_sort_key));
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Figure out which preference was changed
        Preference preference = findPreference(key);
        if (preference != null) {
            // Updates the summary for the preference
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    /**
     * Updates the summary for the preference
     *
     * @param preference The preference to be updated
     * @param value      The value that the preference was updated to
     */
    private void setPreferenceSummary(Preference preference, String value) {

        if (preference instanceof ListPreference) {

            // For list preferences, figure out the label of the selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            // For EditTextPreferences, set the summary to the value's simple string representation.
            preference.setSummary(value);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Toast error = Toast.makeText(getContext(), "Please select a number between 0.1 and 3", Toast.LENGTH_SHORT);

        // Double check that the preference is the size preference
//            String sizeKey = getString(R.string.pref_size_key);
//            if (preference.getKey().equals(sizeKey)) {
//                String stringSize = (String) newValue;
//                try {
//                    float size = Float.parseFloat(stringSize);
//                    // If the number is outside of the acceptable range, show an error.
//                    if (size > 3 || size <= 0) {
//                        error.show();
//                        return false;
//                    }
//                } catch (NumberFormatException nfe) {
//                    // If whatever the user entered can't be parsed to a number, show an error
//                    error.show();
//                    return false;
//                }
//            }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Need to update this way because can't use BaseFragment
        //((MainActivity) getActivity()).setActionBarTitle("Settings");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


}


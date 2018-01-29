package com.arnm.syph.simplefileexplorer.AppOptions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.arnm.syph.simplefileexplorer.R;

import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity {

    String lang = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        lang = sharedPrefs.getString(getString(R.string.list_localization), Locale.getDefault().getLanguage());

        LinearLayout root = (LinearLayout)findViewById(android.R.id.content).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(v -> finish());

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        Preference listLocalization;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            listLocalization = findPreference(getString(R.string.list_localization));
            listLocalization.setOnPreferenceChangeListener((preference, newValue) -> {

                Context context;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (context = getContext()) == null)
                    context = getActivity();

                //Change app language
                LocaleHelper.setLocale(context, newValue.toString());

                getActivity().recreate();

                return true;
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        AppSettingsSingleton.getInstance().setDirectOpen(sharedPrefs.getBoolean(getString(R.string.check_box_direct_open), false));
        AppSettingsSingleton.getInstance().setInfoMenu(sharedPrefs.getBoolean(getString(R.string.check_box_info_menu), false));
        AppSettingsSingleton.getInstance().setIdenticalFileCopyAction(Integer.valueOf(sharedPrefs.getString(getString(R.string.list_action_identical_file), "0")));
        AppSettingsSingleton.getInstance().setBackReachParent(sharedPrefs.getBoolean(getString(R.string.check_box_back_nav_bar_action), false));
        AppSettingsSingleton.getInstance().setBackInfiniteLoop(sharedPrefs.getBoolean(getString(R.string.check_box_back_nav_bar), false));
        if ((lang.compareTo(sharedPrefs.getString(getString(R.string.list_localization), Locale.getDefault().getLanguage()))) != 0) {
            Intent data = new Intent();
            data.putExtra("langChange","1");
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
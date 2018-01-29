package com.arnm.syph.simplefileexplorer.AppOptions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.arnm.syph.simplefileexplorer.R;

public class SettingsExplorerFragmentActivity extends AppCompatPreferenceActivity {

    String nameFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout)findViewById(android.R.id.content).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(v -> finish());


        Intent mIntent = getIntent();
        Bundle extra = mIntent.getExtras();
        String name = null;
        if (extra != null)
            name = extra.getString("ExplorerName");

        if (name == null)
            finish();

        // load settings fragment
        Bundle bundle = new Bundle();
        bundle.putString("ExplorerName", name);
        nameFrag = name;
        MainPreferenceFragment explorerPreferenceFragment = new MainPreferenceFragment();
        explorerPreferenceFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, explorerPreferenceFragment).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (nameFrag == null)
            return;
        SharedPreferences fragPref = this.getSharedPreferences(nameFrag, MODE_PRIVATE);
        AppSettingsSingleton.fragSettingsVar fragPrefSig = AppSettingsSingleton.getInstance().getExplorerSettingsItemByName(nameFrag);
        fragPrefSig.setShowHiddenFiles(fragPref.getBoolean(getString(R.string.check_box_show_hidden_files), true));
        fragPrefSig.setDefaultPath(fragPref.getString(getString(R.string.edit_text_default_folder), getString(R.string.sdcard_path)));
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();

            String name = getArguments().getString("ExplorerName");
            if (name == null)
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            prefMgr.setSharedPreferencesName(name);
            //prefMgr.setSharedPreferencesName("test_preferences");

            addPreferencesFromResource(R.xml.pref_explorer_fragment);
        }
    }
}
package com.arnm.syph.simplefileexplorer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.arnm.syph.simplefileexplorer.AppOptions.AppSettingsSingleton;
import com.arnm.syph.simplefileexplorer.AppOptions.LocaleHelper;
import com.arnm.syph.simplefileexplorer.AppOptions.SettingsActivity;
import com.arnm.syph.simplefileexplorer.AppOptions.SettingsExplorerFragmentActivity;
import com.arnm.syph.simplefileexplorer.Explorer.ExplorerFileCopySingleton;
import com.arnm.syph.simplefileexplorer.Explorer.ExplorerFragment;
import com.arnm.syph.simplefileexplorer.notif.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedPrefs;
    AppSettingsSingleton appSettingsSingleton;
    ExplorerFileCopySingleton explorerFileCopy;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
    private ViewPager mPager;
    private MenuItem menuActionPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSettingsSingleton = AppSettingsSingleton.getInstance();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        LocaleHelper.setLocale(this, sharedPrefs.getString(getString(R.string.list_localization), Locale.getDefault().getLanguage()));

        setContentView(R.layout.activity_main);

        appSettingsSingleton.setDirectOpen(sharedPrefs.getBoolean(getString(R.string.check_box_direct_open), false));
        appSettingsSingleton.setInfoMenu(sharedPrefs.getBoolean(getString(R.string.check_box_info_menu), false));
        appSettingsSingleton.setIdenticalFileCopyAction(Integer.valueOf(sharedPrefs.getString(getString(R.string.list_action_identical_file), "0")));
        appSettingsSingleton.setBackReachParent(sharedPrefs.getBoolean(getString(R.string.check_box_back_nav_bar_action), false));
        appSettingsSingleton.setBackInfiniteLoop(sharedPrefs.getBoolean(getString(R.string.check_box_back_nav_bar), false));

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.mainToolBar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (checkAndRequestStoragePermissions(true)) {
            mPager = findViewById(R.id.viewpager);
            setViewPager();

            TabLayout tabLayout = findViewById(R.id.explorerTab);
            tabLayout.setupWithViewPager(mPager);

            explorerFileCopy = ExplorerFileCopySingleton.getInstance();
        }
    }

    private boolean checkAndRequestStoragePermissions(boolean demand) {
        int rstorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int wstorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (rstorage != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (wstorage != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (demand && !listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return checkAndRequestStoragePermissions(false);
        }
        return listPermissionsNeeded.isEmpty();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menuActionPref = menu.findItem(R.id.action_close);
        if (adapter != null && adapter.getCount() <= 1)
            menuActionPref.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (appSettingsSingleton != null) {
            List<AppSettingsSingleton.fragSettingsVar> prefFrags = appSettingsSingleton.getExplorerSettingsList();

            for(int i = 0; i < prefFrags.size(); i++) {
                AppSettingsSingleton.fragSettingsVar prefOneFrag = prefFrags.get(i);
                SharedPreferences fragPref = this.getSharedPreferences(prefOneFrag.getName(), MODE_PRIVATE);
                SharedPreferences.Editor mOneFragEdit = fragPref.edit();
                if (fragPref.getBoolean(getString(R.string.switch_last_folder), false))
                    mOneFragEdit.putString(getString(R.string.edit_text_default_folder), prefOneFrag.getLastPath());
                if (fragPref.getBoolean(getString(R.string.switch_last_display_type), false))
                    mOneFragEdit.putString(getString(R.string.list_default_display_type), String.valueOf(prefOneFrag.getDisplayType()));
                if (fragPref.getBoolean(getString(R.string.switch_sort_order), false))
                    mOneFragEdit.putString(getString(R.string.list_default_sort_order), String.valueOf(prefOneFrag.getSortOrder()));
                mOneFragEdit.apply();
            }
        }

        explorerFileCopy.clearExplorerList();
    }

    private void setViewPager() {

        appSettingsSingleton.clearList();
        int size = sharedPrefs.getInt("fragmentSize", 0);

        for(int i = 0; i < size; i++) {
            String fragName = sharedPrefs.getString("Frag_" + i, null);
            if (fragName != null)
                setAdapterFrag(fragName, 0);
        }

        if (adapter.getCount() == 0)
            adapterAdd("Default", 0);

        adapter.setCurrentTab(0);
        addChangeListener();

        mPager.setAdapter(adapter);
    }

    private void setAdapterFrag(String name, int type){
        if (type == 0){
            setPreferenceFrag(name);
            ExplorerFragment f = new ExplorerFragment();
            f.setTitle(name);
            adapter.addFragment(f, name, 0);
        }
    }

    private void setPreferenceFrag(String name){
        if (name == null)
            return;
        SharedPreferences fragPref = this.getSharedPreferences(name, MODE_PRIVATE);
        appSettingsSingleton.addExplorerSettingsItem(name);
        AppSettingsSingleton.fragSettingsVar fragPrefSig = appSettingsSingleton.getExplorerSettingsItemByName(name);
        fragPrefSig.setShowHiddenFiles(fragPref.getBoolean(getString(R.string.check_box_show_hidden_files), true));
        fragPrefSig.setDefaultPath(fragPref.getString(getString(R.string.edit_text_default_folder), getString(R.string.sdcard_path)));
        fragPrefSig.setLastPath(fragPref.getString(getString(R.string.edit_text_default_folder), getString(R.string.sdcard_path)));
        fragPrefSig.setDisplayType(Integer.valueOf(fragPref.getString(getString(R.string.list_default_display_type), "1")));
        fragPrefSig.setSortOrder(Integer.valueOf(fragPref.getString(getString(R.string.list_default_sort_order), "0")));
    }

    private void addChangeListener(){

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageSelected(int pos) {
                // TODO Auto-generated method
                // Adding it as an option later
                if (sharedPrefs.getBoolean(getString(R.string.key_reload_on_swipe), false)){
                    ExplorerFragment f = (ExplorerFragment) adapter.getItem(pos);
                    f.upSelect();
                }
                adapter.setCurrentTab(pos);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // TODO Auto-generated method stub
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_order:
                prepChangeOrder();
                return true;
            case R.id.action_display:
                prepChangeType();
                return true;
            case R.id.action_reload:
                ExplorerFragment f;
                if ((f = getCurrExFragment()) != null) {
                    setPreferenceFrag(f.getTitle());
                    f.upSelect();
                }
                return true;
            case R.id.action_close:
                delCurrFragment();
                return true;
            case R.id.action_preferences:
                explorerPreferences();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addFrag) {
            prepAdapterAdd();
        } else if (id == R.id.nav_search) {

        } else if (id == R.id.nav_share) {
            callShareActivity();
        } else if (id == R.id.nav_settings) {
            startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), 1);
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (Integer.valueOf(data.getStringExtra("langChange")) == 1)
                    recreate();
            }
        }
    }

    private void prepAdapterAdd(){
        CharSequence[] array = getResources().getStringArray(R.array.tabs_type);
        new AlertDialog.Builder(this)
                .setItems(array, (dialog, which) -> {
                    dialog.dismiss();
                    askNameAdapter(array[which], which);
                }).setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel()).show();
    }

    private void askNameAdapter(CharSequence typeName, int type){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(typeName);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        builder.setView(input);
        builder.setPositiveButton(R.string.btn_accept, (dialog, which) -> adapterAdd(input.getText().toString(), type));
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void adapterAdd(String name, int type){
        if (type == 0){
            int size = sharedPrefs.getInt("fragmentSize", 0);
            SharedPreferences.Editor mFragEdit = sharedPrefs.edit();
            mFragEdit.putInt("fragmentSize", size + 1);
            mFragEdit.remove("Frag_" + size);
            mFragEdit.putString("Frag_" + size, name);
            mFragEdit.apply();

            appSettingsSingleton.addExplorerSettingsItem(name);
            setPreferencesFromGeneral(name);
            ExplorerFragment f = new ExplorerFragment();
            adapter.addFragment(f, name, 0);
            f.setTitle(name);
            mPager.setAdapter(adapter);
            if (adapter.getCount() > 1)
                menuActionPref.setVisible(true);
        }
    }

    private void setPreferencesFromGeneral(String name){
        if (name == null)
            return;
        SharedPreferences fragPref = this.getSharedPreferences(name, MODE_PRIVATE);

        SharedPreferences.Editor mFragEdit = fragPref.edit();
        AppSettingsSingleton.fragSettingsVar fragPrefSig = appSettingsSingleton.getExplorerSettingsItemByName(name);
        mFragEdit.putBoolean(getString(R.string.check_box_show_hidden_files), sharedPrefs.getBoolean(getString(R.string.check_box_general_show_hidden_files), true));
        fragPrefSig.setShowHiddenFiles(sharedPrefs.getBoolean(getString(R.string.check_box_general_show_hidden_files), true));
        mFragEdit.putString(getString(R.string.edit_text_default_folder), sharedPrefs.getString(getString(R.string.general_default_folder), getString(R.string.sdcard_path)));
        fragPrefSig.setDefaultPath(sharedPrefs.getString(getString(R.string.general_default_folder), getString(R.string.sdcard_path)));
        fragPrefSig.setLastPath(sharedPrefs.getString(getString(R.string.general_default_folder), getString(R.string.sdcard_path)));
        mFragEdit.putBoolean(getString(R.string.switch_last_folder), sharedPrefs.getBoolean(getString(R.string.check_box_general_last_folder), true));
        mFragEdit.putString(getString(R.string.list_default_display_type), sharedPrefs.getString(getString(R.string.list_general_default_display_type), "1"));
        mFragEdit.putBoolean(getString(R.string.switch_last_display_type), sharedPrefs.getBoolean(getString(R.string.check_box_default_last_display_type), true));
        fragPrefSig.setDisplayType(Integer.valueOf(sharedPrefs.getString(getString(R.string.list_general_default_display_type), "1")));
        fragPrefSig.setSortOrder(Integer.valueOf(sharedPrefs.getString(getString(R.string.list_general_default_sort_order), "0")));

        mFragEdit.apply();
    }

    private void explorerPreferences(){
        Intent myIntent = new Intent(MainActivity.this, SettingsExplorerFragmentActivity.class);
        CharSequence name = adapter.getPageTitle(adapter.getCurrentTab());
        if (name == null) {
            Notification.showToast(MainActivity.this, getString(R.string.error_retrieve_name));
            return;
        }
        myIntent.putExtra("ExplorerName", name);
        startActivity(myIntent);
    }

    private void prepChangeOrder(){
        if (adapter.getCurrentTab() != -1 && adapter.getTypeFragment(adapter.getCurrentTab()) == 0) {
            CharSequence[] sortType = getResources().getStringArray(R.array.sort_types);
            AlertDialog.Builder fileNavigation = new AlertDialog.Builder(MainActivity.this);
            fileNavigation.setTitle("Display mode : ");
            fileNavigation.setItems(sortType, (dialog, which) -> {
                ExplorerFragment f;
                if ((f = getCurrExFragment()) != null) {
                    AppSettingsSingleton.fragSettingsVar fragSettings;
                    if ((fragSettings = appSettingsSingleton.getExplorerSettingsItemByName(f.getTitle())) != null) {
                        fragSettings.setSortOrder(which);
                        f.upSelect();
                    }
                }
            });
            fileNavigation.show();
        }
    }

    private void prepChangeType(){
        if (adapter.getCurrentTab() != -1 && adapter.getTypeFragment(adapter.getCurrentTab()) == 0) {
            CharSequence[] displayType = {
                    "Simple", "Details", "Icon", "Content"
            };
            AlertDialog.Builder fileNavigation = new AlertDialog.Builder(MainActivity.this);
            fileNavigation.setTitle("Display mode : ");
            fileNavigation.setItems(displayType, (dialog, which) -> {
                ExplorerFragment f;
                if ((f = getCurrExFragment()) != null) {
                    f.changeViewType(which);
                }
            });
            fileNavigation.show();
        }
    }

    private void delCurrFragment(){
        AlertDialog.Builder delFragmentDialog = new AlertDialog.Builder(MainActivity.this);
        delFragmentDialog.setTitle("Delete Tab : ");
        delFragmentDialog.setMessage("By deleting this tab you will lost all preferences relative to it, are you sure ?");
        delFragmentDialog.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        delFragmentDialog.setPositiveButton(R.string.btn_accept, (dialog, which) -> {
            if (adapter.getCount() == 1)
                return;
            CharSequence fragName = adapter.getPageTitle(adapter.getCurrentTab());
            if (fragName != null){
                SharedPreferences preferences = getSharedPreferences(fragName.toString(), 0);
                preferences.edit().clear().apply();
            }
            adapter.delCurrentFragment();
            mPager.setAdapter(adapter);
            if (adapter.getCurrentTab() >= adapter.getCount())
                adapter.setCurrentTab(0);

            SharedPreferences.Editor mEdit1 = sharedPrefs.edit();

            int size = sharedPrefs.getInt("fragmentSize", 0);
            for(int i = 0; i < size; i++)
                mEdit1.remove("Frag_" + i);

            List<String> namesFrags = adapter.getListTitle();
            mEdit1.putInt("fragmentSize", namesFrags.size());
            for(int i = 0; i < namesFrags.size(); i++) {
                mEdit1.remove("Frag_" + i);
                mEdit1.putString("Frag_" + i, namesFrags.get(i));
            }

            mEdit1.apply();

            if (adapter.getCount() <= 1)
                menuActionPref.setVisible(false);
        });
        delFragmentDialog.show();
    }

    private ExplorerFragment getCurrExFragment(){
        ExplorerFragment f;
        if (adapter.getCurrentTab() == -1 || adapter.getTypeFragment(adapter.getCurrentTab()) != 0)
            return  null;
        if ((f = (ExplorerFragment) adapter.getItem(adapter.getCurrentTab())) == null)
            return  null;
        return f;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else {
            ExplorerFragment f;
            if ((f = getCurrExFragment()) != null) {
                setPreferenceFrag(f.getTitle());
                if (f.goBackHistory())
                    return;
            }
        }
        super.onBackPressed();
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final List<Integer> mFragmentType = new ArrayList<>();
        private int currentTab = 0;

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title, int typeFragment) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mFragmentType.add(typeFragment);
        }

        void delCurrentFragment() {
            if (currentTab == -1)
                return;
            mFragmentList.remove(currentTab);
            mFragmentTitleList.remove(currentTab);
            mFragmentType.remove(currentTab);
        }

        List<String> getListTitle() {
            return mFragmentTitleList;
        }

        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        int getCurrentTab() {
            return currentTab;
        }

        void setCurrentTab(int currentTab) {
            this.currentTab = currentTab;
        }

        int getTypeFragment(int position) {
            return mFragmentType.get(position);
        }
    }

    private void callShareActivity(){
        Intent myIntent = new Intent(MainActivity.this, ShareFileActivity.class);
        MainActivity.this.startActivity(myIntent);
    }
}

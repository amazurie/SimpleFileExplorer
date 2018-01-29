package com.arnm.syph.simplefileexplorer.Explorer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.arnm.syph.simplefileexplorer.AppOptions.AppSettingsSingleton;
import com.arnm.syph.simplefileexplorer.R;
import com.arnm.syph.simplefileexplorer.notif.Notification;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.UserPrincipal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by Syph on 26/12/2017.
 */

public class ExplorerFragment extends Fragment implements View.OnClickListener {

    private Boolean isFabOpen = false;
    private boolean isInfoFabOpen = false;
    private FloatingActionButton fabInfoMenu, fab, fab1, fab2, fab3, fab4, fab5, fab6;
    private TextView textViewFab1, textViewFab2, textViewFab3, textViewFab4, textViewFab5, textViewFab6;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, rotate_fab_button_forward, rotate_fab_button_backward;

    private AppSettingsSingleton.fragSettingsVar fragSettings = null;
    GridLayoutManager mGridLayoutManager;
    private RecyclerView recyclerView;
    ExplorerFileCopySingleton.FileList explorerFileCopy;
    RecAdapter adapter;

    private String title = "Default";
    private List<ItemObjects> items = null;
    private String dirPath = null;
    private TextView myPath;
    private boolean selectMode;

    public ExplorerFragment() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inf = inflater.inflate(R.layout.fragment_explorer, container, false);

        selectMode = false;
        myPath = inf.findViewById(R.id.path);
        items = new ArrayList<>();

        ImageButton imgBtnUp = inf.findViewById(R.id.imgBtnUp);
        imgBtnUp.setOnClickListener(v -> getParent());

        ImageButton imgBtnHome = inf.findViewById(R.id.imgBtnHome);
        imgBtnHome.setOnClickListener(v -> goHome());

        recyclerView = inf.findViewById(R.id.explorerView);
        recyclerView.setHasFixedSize(true);

        mGridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(mGridLayoutManager);

        adapter = new RecAdapter(items, setListener());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        addFab(inf);

        if ((savedInstanceState != null) && (savedInstanceState.getSerializable(getString(R.string.serialize_title_frag)) != null))
            title = (String)savedInstanceState.getSerializable(getString(R.string.serialize_title_frag));

        fragSettings = AppSettingsSingleton.getInstance().getExplorerSettingsItemByName(title);
        if (fragSettings != null) {
            setDirPath(fragSettings.getDefaultPath());
            changeViewType(fragSettings.getDisplayType());
        } else
            setDirPath(getString(R.string.sdcard_path));

        ExplorerFileCopySingleton explorerFileCopySigleton = ExplorerFileCopySingleton.getInstance();
        explorerFileCopySigleton.newExplorerList();
        explorerFileCopySigleton.addExplorerListItem(title);
        int id = explorerFileCopySigleton.getExplorerListItemId(title);
        explorerFileCopy = explorerFileCopySigleton.getExplorerListItem(id);

        adapter.clearList();
        getDir();

        return inf;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(getString(R.string.serialize_title_frag), title);
    }

    private void addFab(View inf) {
        fabInfoMenu = inf.findViewById(R.id.fabInfoMenu);
        fab = inf.findViewById(R.id.fab);
        fab1 = inf.findViewById(R.id.fab1);
        textViewFab1 = inf.findViewById(R.id.textViewFab1);
        fab2 = inf.findViewById(R.id.fab2);
        textViewFab2 = inf.findViewById(R.id.textViewFab2);
        fab3 = inf.findViewById(R.id.fab3);
        textViewFab3 = inf.findViewById(R.id.textViewFab3);
        fab4 = inf.findViewById(R.id.fab4);
        textViewFab4 = inf.findViewById(R.id.textViewFab4);
        fab5 = inf.findViewById(R.id.fab5);
        textViewFab5 = inf.findViewById(R.id.textViewFab5);
        fab6 = inf.findViewById(R.id.fab6);
        textViewFab6 = inf.findViewById(R.id.textViewFab6);

        fabInfoMenu.setOnClickListener(this);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);
        fab4.setOnClickListener(this);
        fab5.setOnClickListener(this);
        fab6.setOnClickListener(this);

        Context context = obtainContext();
        fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward);
        rotate_fab_button_forward = AnimationUtils.loadAnimation(context, R.anim.rotate_fab_button_forward);
        rotate_fab_button_backward = AnimationUtils.loadAnimation(context, R.anim.rotate_fab_button_backward);
    }

    public void changeViewType(int type) {
        if (type == adapter.getViewType())
            return;
        fragSettings.setDisplayType(type);
        if (type == 0) {
            mGridLayoutManager.setSpanCount(1);
            adapter = new RecAdapter(items, setListener());
            adapter.setViewType(0);
            recyclerView.setAdapter(adapter);
        } else if (type == 1){
            mGridLayoutManager.setSpanCount(1);
            adapter = new RecAdapter(items, setListener());
            adapter.setViewType(1);
            recyclerView.setAdapter(adapter);
        }
        else if (type == 2) {
            mGridLayoutManager.setSpanCount(4);
            adapter = new RecAdapter(items, setListener());
            adapter.setViewType(2);
            recyclerView.setAdapter(adapter);
        } else if (type == 3){
            mGridLayoutManager.setSpanCount(1);
            adapter = new RecAdapter(items, setListener());
            adapter.setViewType(3);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fabInfoMenu:
                for (ItemObjects item: items) {
                    if (item.isSelected()) {
                        File file = new File(item.getPath());
                        Context context = obtainContext();
                        if (!file.exists() || context == null)
                            return;
                        if (file.isDirectory())
                            dirMenu(file, context);
                        else
                            fileMenu(file, context);
                    }
                }
                break;
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab1:
                if (selectMode)
                    deleteMultiFile();
                break;
            case R.id.fab2:
                getFromCopy();
                break;
            case R.id.fab3:
                try {
                    doPaste(-1);
                    selectMode = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    Notification.showToast(getContext(), getString(R.string.error_occurred));
                }
                break;
            case R.id.fab4:
                prepCopy(true);
                break;
            case R.id.fab5:
                prepCopy(false);
                break;
            case R.id.fab6:
                prepAdding();
                break;
        }
    }

    public boolean goBackHistory(){
        if (fragSettings.getHistorySize() <= 1 && !AppSettingsSingleton.getInstance().isBackReachParent())
            return AppSettingsSingleton.getInstance().isBackInfiniteLoop();
        if (AppSettingsSingleton.getInstance().isBackReachParent()) {
            File file = new File(dirPath);
            if (!file.exists() || file.getParent() == null)
                return AppSettingsSingleton.getInstance().isBackInfiniteLoop();
            dirPath = file.getParent();
        } else {
            dirPath = fragSettings.getLastHistory();
            fragSettings.delLastHistory();
        }
        upSelect();
        fragSettings.delLastHistory();
        return true;
    }

    private void getFromCopy(){
        Context context;
        if ((context = obtainContext()) == null)
            return;
        int explorerCount = ExplorerFileCopySingleton.getInstance().getExplorerListSize();
        int i = 0;
        CharSequence[] explorerTitle = new CharSequence[explorerCount];
        while (i < explorerCount){
            explorerTitle[i] = ExplorerFileCopySingleton.getInstance().getExplorerListItem(i).getName();
            i++;
        }
        AlertDialog.Builder explorerMenu = new AlertDialog.Builder(context);
        explorerMenu.setTitle(R.string.copy_from);
        explorerMenu.setItems(explorerTitle, (DialogInterface dialog, int item) -> {
            try {
                if (ExplorerFileCopySingleton.getInstance().getExplorerListItem(item) != null) {
                    doPaste(item);
                    selectMode = false;
                    upSelect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        explorerMenu.show();
    }

    public ItemClickListener setListener(){

        return (View v, int position, boolean isLongClick) -> {
            Context context;
            if ((context = obtainContext()) == null)
                return;
            File file;
            file = new File(items.get(position).getPath());
            if (selectMode || isLongClick) {
                if (isLongClick && items.get(position).isSelected() && adapter.isOnlyOneSelected()) {
                    if (file.isDirectory()) {
                        dirMenu(file, context);
                        return;
                    }
                }
                if (items.get(position).isSelected()){
                    items.get(position).setSelected(false);
                    if (!adapter.isAnItemSelect()) {
                        selectMode = false;
                        if (isInfoFabOpen) {
                            fabInfoMenu.setVisibility(View.INVISIBLE);
                            fabInfoMenu.setClickable(false);
                            isInfoFabOpen = false;
                        }
                        changeFabMenuButtonState();
                    }
                    else if (adapter.isOnlyOneSelected() && AppSettingsSingleton.getInstance().isInfoMenu()) {
                        if (!isInfoFabOpen) {
                            fabInfoMenu.setVisibility(View.VISIBLE);
                            fabInfoMenu.setClickable(true);
                            isInfoFabOpen = true;
                        }
                    }
                } else {
                    items.get(position).setSelected(true);
                    if (!selectMode) {
                        if (AppSettingsSingleton.getInstance().isInfoMenu()) {
                            if (!isInfoFabOpen) {
                                fabInfoMenu.setVisibility(View.VISIBLE);
                                fabInfoMenu.setClickable(true);
                                isInfoFabOpen = true;
                            }
                        }
                        changeFabMenuButtonState();
                    }
                    else {
                        if (isInfoFabOpen) {
                            fabInfoMenu.setVisibility(View.INVISIBLE);
                            fabInfoMenu.setClickable(false);
                            isInfoFabOpen = false;
                        }
                    }
                    selectMode = true;
                }
                adapter.notifyDataSetChanged();
                return ;
            }
            if (file.isDirectory()) {
                if (file.canRead()) {
                    setDirPath(items.get(position).getPath());
                    upSelect();
                    adapter.notifyDataSetChanged();
                }
                else {
                    new AlertDialog.Builder(context
                    ).setIcon(R.drawable.folder_error)
                            .setTitle(String.format(getString(R.string.error_reading_folder), file.getName()))
                            .setNeutralButton(R.string.btn_accept, (dialog, which) -> {
                            }).show();
                }
            } else
                fileMenu(file, context);
        };
    }

    private void dirMenu(File file, Context context){
        final CharSequence[] items1 = getResources().getStringArray(R.array.file_menu);
        AlertDialog.Builder fileNavigation = new AlertDialog.Builder(context);
        fileNavigation.setTitle(String.format(getString(R.string.file_menu_title), file.getName()));
        fileNavigation.setItems(items1, (dialog, which) -> {
            if (which == 0){
                if (file.canRead()) {
                    setDirPath(file.getPath());
                    upSelect();
                    adapter.notifyDataSetChanged();
                }
                else {
                    new AlertDialog.Builder(context
                    ).setIcon(R.drawable.folder_error)
                            .setTitle(String.format(getString(R.string.error_reading_folder), file.getName()))
                            .setNeutralButton(R.string.btn_accept, (dialog1, which1) -> {
                            }).show();
                }
            } else if (which == 1)
                fileRename(file);
            else if (which == 2)
                dialogFileInfo(file);
            else if (which == 3)
                prepUnitCopy(file.getPath(), false);
            else if (which == 4)
                prepUnitCopy(file.getPath(), true);
            else if (which == 5)
                deleteFile(file.getPath(), file.getName(), true);
        });
        fileNavigation.show();
    }

    private void fileMenu(File file, Context context){
        final CharSequence[] items1 = getResources().getStringArray(R.array.file_menu);
        AlertDialog.Builder fileNavigation = new AlertDialog.Builder(context);
        fileNavigation.setTitle(String.format(getString(R.string.file_menu_title), file.getName()));
        fileNavigation.setItems(items1, (dialog, which) -> {
            if (which == 0)
                openFile(file);
            else if (which == 1)
                fileRename(file);
            else if (which == 2)
                dialogFileInfo(file);
            else if (which == 3)
                prepUnitCopy(file.getPath(), false);
            else if (which == 4)
                prepUnitCopy(file.getPath(), true);
            else if (which == 5)
                deleteFile(file.getPath(), file.getName(), true);
        });
        fileNavigation.show();
    }

    private void dialogFileInfo(File file){
        Context context;
        if ((context = obtainContext()) == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dlgView = getLayoutInflater().inflate(R.layout.dialog_info_file, recyclerView, false);
        builder.setTitle(R.string.file_info_title);

        TextView name = dlgView.findViewById(R.id.fileName);
        TextView type = dlgView.findViewById(R.id.fileType);
        TextView location = dlgView.findViewById(R.id.fileLocation);
        TextView owner = dlgView.findViewById(R.id.fileOwner);
        TextView permissions = dlgView.findViewById(R.id.filePermissions);
        TextView size = dlgView.findViewById(R.id.fileSize);
        TextView lastModification = dlgView.findViewById(R.id.fileLastModification);

        Button btnDismiss = dlgView.findViewById(R.id.btn_info_dismiss);
        name.setText(file.getName());
        if (file.isDirectory())
            type.setText(R.string.file_info_dir);
        else {
            int fileType = getFileType(file.getName());
            if (fileType == 1)
                type.setText(R.string.file_info_doc);
            else if (fileType == 2)
                type.setText(R.string.file_info_pdf);
            else if (fileType == 3)
                type.setText(R.string.file_info_pp);
            else if (fileType == 4)
                type.setText(R.string.file_info_exl);
            else if (fileType == 5)
                type.setText(R.string.file_info_zip);
            else if (fileType == 6)
                type.setText(R.string.rtf);
            else if (fileType == 7)
                type.setText(R.string.file_info_aud);
            else if (fileType == 8)
                type.setText(R.string.file_info_midi);
            else if (fileType == 9)
                type.setText(R.string.gif);
            else if (fileType == 10)
                type.setText(R.string.file_info_img);
            else if (fileType == 11)
                type.setText(R.string.file_info_txt);
            else if (fileType == 12)
                type.setText(R.string.file_info_vid);
            else
                type.setText(R.string.file_info_file);
        }
        location.setText(file.getAbsolutePath());
        permissions.setText(getFilePermissions(file));
        String fileSize = getFileLength(file.length());
        while (fileSize.startsWith(" "))
            fileSize = fileSize.substring(1);
        size.setText(fileSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                UserPrincipal user = Files.getOwner(file.toPath());
                if (user != null)
                    owner.setText(user.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            dlgView.findViewById(R.id.owner).setVisibility(View.GONE);
            owner.setVisibility(View.GONE);
        }
        SimpleDateFormat spf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        lastModification.setText(spf.format(file.lastModified()));

        builder.setView(dlgView);
        AlertDialog dialog = builder.create();
        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getFilePermissions(File file){
        String rwd = "-";
        if (file.canRead())
            rwd = "r";
        if (file.canWrite())
            rwd += "w";
        else
            rwd += "-";
        if (file.canExecute())
            rwd += "d";
        else
            rwd += "-";
        return rwd;
    }

    private void getDir() {
        File f = new File(dirPath);
        if (!f.exists()) {
            Notification.showToast(getActivity(), String.format(getString(R.string.error_path), dirPath));
            String location = dirPath;
            if ((location = correctionLocation(location, 0)) == null) {
                if (new File(getString(R.string.sdcard_path)).exists())
                    setDirPath(getString(R.string.sdcard_path));
                else {
                    Notification.showToast(getActivity(), getString(R.string.no_location));
                    return;
                }
            }
            else {
                Notification.showToast(getActivity(), String.format(getString(R.string.correction_path_terminate), location));
                setDirPath(location);
            }
            f = new File(dirPath);
            if (fragSettings != null)
                fragSettings.setDefaultPath(dirPath);
        }
        if (f.exists() && !f.isDirectory() && f.getParent() != null)
            Notification.showToast(getActivity(), String.format(getString(R.string.error_reading_go_parent), f.getName()));
        while (f.exists() && !f.isDirectory() && f.getParent() != null)
            f = new File(f.getParent());
        if (!f.exists())
            return ;
        myPath.setText(dirPath);
        File[] files = f.listFiles();
        if (files == null) {
            Context context;
            if ((context = obtainContext()) != null) {
                new AlertDialog.Builder(context
                ).setIcon(R.drawable.folder_error)
                        .setTitle(String.format(getString(R.string.error_reading_folder), f.getName()))
                        .setNeutralButton(R.string.btn_accept, (dialog, which) -> {
                        }).show();
                return ;
            }
        }
        if (files == null)
            return ;
        sortFile(files);
        SimpleDateFormat lastModified = new SimpleDateFormat(getString(R.string.date_format), java.util.Locale.getDefault());
        for (File file : files) {
            prepAddItem(file, lastModified);
        }
        if (fragSettings != null) {
            fragSettings.addHistory(fragSettings.getLastPath());
            fragSettings.setLastPath(dirPath);
        }
    }

    private void prepAddItem(File file, SimpleDateFormat lastModified){
        if(file.getName().startsWith(".") && !fragSettings.isShowHiddenFiles())
            return;
        if (file.isDirectory())
            addItem(file, R.drawable.folder_icon, lastModified);
        else {
            int type = getFileType(FilenameUtils.getExtension(file.getName()));
            if (type == 1)
                addItem(file, R.drawable.file_icon_doc, lastModified);
            else if (type == 2)
                addItem(file, R.drawable.file_icon_pdf, lastModified);
            else if (type == 3)
                addItem(file, R.drawable.file_icon_ppt, lastModified);
            else if (type == 4)
                addItem(file, R.drawable.file_icon_xls, lastModified);
            else if (type == 5)
                addItem(file, R.drawable.file_icon_compressed, lastModified);
            else if (type == 6)
                addItem(file, R.drawable.file_icon_rtf, lastModified);
            else if (type == 7)
                addItem(file, R.drawable.file_icon_aud, lastModified);
            else if (type == 8)
                addItem(file, R.drawable.file_icon_midi, lastModified);
            else if (type == 9)
                addItem(file, R.drawable.file_icon_gif, lastModified);
            else if (type == 10) {
                addItem(file, -1, lastModified);
            }
            else if (type == 11)
                addItem(file, R.drawable.file_icon_txt, lastModified);
            else if (type == 12)
                addItem(file, -2, lastModified);
            else
                addItem(file, R.drawable.file_icon, lastModified);
        }
    }

    private void addItem(File file, int image, SimpleDateFormat lastModified){
        String canonPath = file.getPath();
        if((!file.getName().startsWith(".") || fragSettings.isShowHiddenFiles()) && !canonPath.isEmpty()) {
            if (file.isDirectory())
                items.add(new ItemObjects(file.getName(), image, lastModified.format(file.lastModified()) + " " + getFilePermissions(file), null, canonPath));
            else
                items.add(new ItemObjects(file.getName(), image, lastModified.format(file.lastModified()) + " " + getFilePermissions(file), getFileLength(file.length()), canonPath));
        }
        else if (canonPath.isEmpty())
            Notification.showToast(getActivity(), getString(R.string.error_occurred));
    }

    private String  getFileLength(long length) {
        if (length < 1024)
            return "  " + String.valueOf(length) + " octets";
        if ((float)length < 1048576) {
            if ((float)length / 1024 % 1 == 0)
                return "  " + String.valueOf(length / 1024) + " ko";
            return "  " + String.format(Locale.getDefault(), "%.2f", (float)length / 1024) + " ko";
        }
        if ((float)length < 1073741824) {
            if ((float)length / 1024 % 1 == 0)
                return "  " + String.valueOf(length / Math.pow(1024, 2)) + " mo";
            return "  " + String.format(Locale.getDefault(), "%.2f", (float) length / Math.pow(1024, 2)) + " mo";
        }
        if ((float)length / 1024 % 1 == 0)
            return "  " + String.valueOf(length / Math.pow(1024, 3)) + " go";
        return "  " + String.format(Locale.getDefault(), "%.2f", (float) length / Math.pow(1024, 3)) + " go";
    }

    private String correctionLocation(String location, int turn){
        if (location == null)
            return null;
        String tmp = location;
        File f = new File(location);
        if (!f.exists()){
            if (turn <= 0 && (tmp = correctionLocation(tmp, 1)) != null)
                return tmp;
            tmp = location.trim();
            if (!(f = new File(tmp)).exists()){
                if (turn <= 1 && (tmp = correctionLocation(tmp, 2)) != null)
                    return tmp;
                tmp = location.replace(" ", "");
                if (!(f = new File(tmp)).exists()) {
                    if (turn <= 2 && (tmp = correctionLocation(tmp, 3)) != null)
                        return tmp;
                    tmp = location.replaceAll("\\s+","");
                    if (!(f = new File(tmp)).exists()) {
                        if (turn <= 3 && (tmp = correctionLocation(tmp, 4)) != null)
                            return tmp;
                        tmp = location.replaceAll("[^a-zA-Z0-9/]+","");
                        if (!(f = new File(tmp)).exists()) {
                            if (turn <= 4 && (tmp = correctionLocation(tmp, 5)) != null)
                                return tmp;
                            tmp = location.replaceAll("[^a-zA-Z/]+","");
                            if (!(f = new File(tmp)).exists()) {
                                if (turn <= 5 && (tmp = correctionLocation(tmp, 6)) != null)
                                    return tmp;
                            }
                        }
                    }
                }
            }
        }
        if (f.exists())
            return tmp;
        int endIndex = location.lastIndexOf("/");
        if (endIndex != -1 && endIndex != 0) {
            tmp = location.substring(0, endIndex);
            if ((tmp = correctionLocation(tmp, 0)) != null)
                return tmp;
        }
        return null;
    }

    private void openFile(File file){
        if (AppSettingsSingleton.getInstance().isDirectOpen()) {
            directOpenFile(file);
            return;
        }

        Context context;
        if ((context = obtainContext()) == null)
            return;

        final CharSequence[] items1 = getResources().getStringArray(R.array.file_open);
        AlertDialog.Builder fileNavigation = new AlertDialog.Builder(context);
        fileNavigation.setTitle("Open " + file.getName() + " as : ");
        fileNavigation.setItems(items1, (dialog, which) -> {
            Uri fileUri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String extension = FilenameUtils.getExtension(file.getName());

            if (which == 0)
                chooseIntentDateType(intent, fileUri, extension);
            else if (which == 1)
                intent.setDataAndType(fileUri, "text/plain");
            else if (which == 2)
                intent.setDataAndType(fileUri, "application/pdf");
            else if (which == 3)
                intent.setDataAndType(fileUri, "image/*");
            else if (which == 4)
                intent.setDataAndType(fileUri, "video/*");
            else
                intent.setDataAndType(fileUri, "*/*");

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (activities.size() < 0)
                intent.setDataAndType(fileUri, "*/*");

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        fileNavigation.show();
    }

    private void directOpenFile(File file){
        Context context;
        if ((context = obtainContext()) == null)
            return;

        Uri fileUri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String extension = FilenameUtils.getExtension(file.getName());

        chooseIntentDateType(intent, fileUri, extension);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void chooseIntentDateType(Intent intent, Uri fileUri,String extension){
        int type = getFileType(extension);

        if (type == 1)
            intent.setDataAndType(fileUri, "application/msword");
        else if (type == 2)
            intent.setDataAndType(fileUri, "application/pdf");
        else if (type == 3)
            intent.setDataAndType(fileUri, "application/vnd.ms-powerpoint");
        else if (type == 4)
            intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        else if (type == 5)
            intent.setDataAndType(fileUri, "application/zip");
        else if (type == 6)
            intent.setDataAndType(fileUri, "application/rtf");
        else if (type == 7)
            intent.setDataAndType(fileUri, "audio/x-wav");
        else if (type == 8)
            intent.setDataAndType(fileUri, "audio/midi");
        else if (type == 9)
            intent.setDataAndType(fileUri, "image/gif");
        else if (type == 10)
            intent.setDataAndType(fileUri, "image/jpeg");
        else if (type == 11)
            intent.setDataAndType(fileUri, "text/plain");
        else if (type == 12)
            intent.setDataAndType(fileUri, "video/*");
        else
            intent.setDataAndType(fileUri, "*/*");
    }

    private int getFileType(String extension){
        if (extension.compareTo("doc") == 0 || extension.compareTo("docx") == 0) {
            // Word document
            return 1;
        } else if(extension.compareTo("pdf") == 0) {
            // PDF file
            return 2;
        } else if(extension.compareTo("ppt") == 0 || extension.compareTo("pptx") == 0) {
            // Powerpoint file
            return 3;
        } else if(extension.compareTo("xls") == 0 || extension.compareTo("xlsx") == 0) {
            // Excel file
            return 4;
        } else if(extension.compareTo("zip") == 0 || extension.compareTo("rar") == 0)  {
            // ZIP Files
            return 5;
        } else if(extension.compareTo("rtf") == 0) {
            // RTF file
            return 6;
        } else if(extension.compareTo("wav") == 0 || extension.compareTo("mp3") == 0 || extension.compareTo("flac") == 0) {
            // WAV audio file
            return 7;
        } else if(extension.compareTo("mid") == 0 || extension.compareTo("xmf") == 0 || extension.compareTo("mxmf") == 0) {
            // Midi audio file
            return 8;
        } else if(extension.compareTo("gif") == 0) {
            // GIF file
            return 9;
        } else if(extension.compareTo("jpg") == 0 || extension.compareTo("jpeg") == 0 || extension.compareTo("png") == 0
                || extension.compareTo("bmp") == 0 || extension.compareTo("webp") == 0) {
            // JPG file
            return 10;
        } else if(extension.compareTo("txt") == 0 || extension.compareTo("log") == 0 || extension.compareTo("xml") == 0) {
            // Text file
            return 11;
        } else if(extension.compareTo("3gp") == 0 || extension.compareTo("mpg") == 0 || extension.compareTo("mpeg") == 0
                || extension.compareTo("mpe") == 0 || extension.compareTo("mp4") == 0 || extension.compareTo("avi") == 0
                || extension.compareTo("webm") == 0 || extension.compareTo("mkv") == 0 || extension.compareTo("ogg") == 0) {
            // Video files
            return 12;
        } else {
            return 0;
        }
    }

    private void getParent(){
        File f = new File(dirPath);
        if (f.getParent() != null)
            setDirPath(f.getParent());
        else
            return ;
        upSelect();
    }

    private void goHome(){
        setDirPath(fragSettings.getDefaultPath());
        upSelect();
    }

    private void fileRename(File file){
        Context context;
        if ((context = obtainContext()) == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(String.format(getString(R.string.rename_title_dialog), file.getName()));
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(file.getName());
        builder.setView(input);
        builder.setPositiveButton(R.string.btn_accept, (dialog, which) -> {
            String m_Text = input.getText().toString();
            if (file.getName().compareTo(m_Text) == 0)
                return;
            try {
                List<String> allFiles = new ArrayList<>();
                getAllCopiedFiles(allFiles, file.getPath());
                pasteFile(allFiles, dirPath, m_Text, 0, true, context);
            } catch (IOException e) {
                Notification.showToast(context, getString(R.string.error_occurred));
                e.printStackTrace();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void prepAdding(){
        Context context;
        if ((context = obtainContext()) == null)
            return;
        final String[] m_Text = new String[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dlgView = getLayoutInflater().inflate(R.layout.dialog_text_choice, recyclerView, false);
        builder.setTitle(R.string.new_menu_item);
        Spinner dlgSpinner = dlgView.findViewById(R.id.dlg_new_type);
        EditText input = dlgView.findViewById(R.id.dlg_text_input);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        ArrayAdapter<String> array = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.file_types));
        array.setDropDownViewResource(android.R.layout.simple_spinner_item);
        dlgSpinner.setAdapter(array);
        builder.setView(dlgView);
        Context finalContext = context;
        builder.setPositiveButton(R.string.btn_accept, (dialog, which) -> {
            m_Text[0] = input.getText().toString();
            if (dlgSpinner.getSelectedItemId() == 0)
                fileCreate(m_Text[0], finalContext);
            else
                directoryCreate(m_Text[0], finalContext);
            selectMode = false;
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    protected void fileCreate(String name, Context context){
        File newFile = new File(dirPath + "/" + name);
        if (!newFile.exists()) {
            try {
                if (!newFile.createNewFile())
                    Notification.showToast(context, getString(R.string.error_occurred));
                else {
                    upSelect();
                    Notification.showToast(context, getString(R.string.file_created));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void directoryCreate(String name, Context context){
        File newFile = new File(dirPath + "/" + name + "/");
        if (!newFile.exists()) {
            if (!newFile.mkdirs())
                Notification.showToast(context, getString(R.string.error_occurred));
            else {
                upSelect();
                Notification.showToast(context, getString(R.string.folder_created));
            }
        }
    }

    protected void prepUnitCopy(String path, boolean cut){
        explorerFileCopy.clearList();
        explorerFileCopy.listAdd(path);
        explorerFileCopy.setCut(cut);
    }

    protected void prepCopy(boolean cut){
        if (items == null)
            return;
        explorerFileCopy.clearList();
        for(ItemObjects item: items){
            if (item.isSelected())
                explorerFileCopy.listAdd(item.getPath());
        }
        explorerFileCopy.setCut(cut);
        Context context;
        if ((context = obtainContext()) != null) {
            if (cut)
                Notification.showToast(context, getString(R.string.cut_success));
            else
                Notification.showToast(context, getString(R.string.copy_success));
        }
    }

    public void pasteFile(List<String> sources, String parentPath, String targetName, int numFile, boolean isCut, Context context)
            throws IOException {

        if (sources.size() <= numFile || sources.get(numFile) == null) {
            if (isCut) {
                for (String path : sources) {
                    File file2 = new File(path);
                    if (file2.exists()) {
                        if (file2.isDirectory() && file2.listFiles().length == 0)
                            deleteFile(path, file2.getName(), false);
                    }
                }
            }
            upSelect();
            return;
        }

        File source = new File(sources.get(numFile));
        File target;
        if (targetName != null)
            target = new File(parentPath + "/" + targetName);
        else
            target = new File(parentPath + "/" + source.getName());

        if (source.isDirectory()) {
            if (!target.exists())
                if (!target.mkdir()) {
                    Notification.showToast(getActivity(), getString(R.string.error_occurred));
                    return;
                }
            try {
                pasteFile(sources, target.getPath(), null, numFile + 1, isCut, context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if ((explorerFileCopy.getReplaceKeepAction() == 0 || explorerFileCopy.getReplaceKeepAction() > 3) && target.exists())
                new AlertDialog.Builder(context).setIcon(R.drawable.folder_error)
                        .setTitle(R.string.identical_files)
                        .setMessage(String.format(getString(R.string.copy_message_identical), target.getName()))
                        .setPositiveButton(R.string.copy_btn_replace, (dialog, which) -> {
                            //explorerFileCopy.setReplaceKeepAction(1);
                            execCopyFile(sources.get(numFile), target, 1);
                            if (isCut)
                                deleteFile(source.getPath(), source.getName(), false);
                            try {
                                pasteFile(sources, parentPath, null, numFile + 1, isCut, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .setNeutralButton(R.string.copy_btn_keep_both, (dialog2, which) -> {
                            //explorerFileCopy.setReplaceKeepAction(2);
                            execCopyFile(sources.get(numFile), target, 2);
                            if (isCut)
                                deleteFile(source.getPath(), source.getName(), false);
                            try {
                                pasteFile(sources, parentPath, null, numFile + 1, isCut, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton(R.string.copy_btn_ignore, (dialog3, which) -> {
                            if (isCut)
                                deleteFile(source.getPath(), source.getName(), false);
                            try {
                                pasteFile(sources, parentPath, null, numFile + 1, isCut, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialog3.cancel();
                        }).show();
            else {
                execCopyFile(sources.get(numFile), target, 0);
                if (isCut)
                    deleteFile(source.getPath(), source.getName(), false);
                try {
                    pasteFile(sources, parentPath, null, numFile + 1, isCut, context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void execCopyFile(String sourceLocation, File target, int action){
        try {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out;
            File renamed;

            int preferenceAction = explorerFileCopy.getReplaceKeepAction();
            if (!target.exists() || (preferenceAction == 1 || action == 1))
                out = new FileOutputStream(target);
            else if ((preferenceAction == 0 || preferenceAction == 2 || action == 0 || action == 2)&& (renamed = renameCopy(target)) != null)
                out = new FileOutputStream(renamed);
            else
                return;

            // Copy the bits from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private File renameCopy(File target) {
        String fileNameWithOutExt = FilenameUtils.removeExtension(target.getName());
        String fileExtension = null;
        if (target.getName().contains("."))
            fileExtension = "." + FilenameUtils.getExtension(target.getName());
        File fileRename;
        int i = 0;
        if (fileExtension != null) {
            fileRename = new File(target.getParentFile(), fileNameWithOutExt + getString(R.string.copy_rename) + fileExtension);
            while (fileRename.exists() && i < 251)
                fileRename = new File(target.getParentFile(), fileNameWithOutExt + String.format(getString(R.string.copy_rename_increment), ++i) + fileExtension);
        }
        else {
            fileRename = new File(target.getParentFile(), fileNameWithOutExt + getString(R.string.copy_rename));
            while (fileRename.exists() && i < 251)
                fileRename = new File(target.getParentFile(), fileNameWithOutExt + String.format(getString(R.string.copy_rename_increment), ++i));
        }
        if (i >= 251) {
            return null;
        }
        return fileRename;
    }

    //TODO copy progression dialog
    public void doPaste(int item) throws IOException {

        Context context;
        if ((context = obtainContext()) == null)
            return;

        ExplorerFileCopySingleton.FileList e;
        if (item == -1)
            e = explorerFileCopy;
        else if ((e = ExplorerFileCopySingleton.getInstance().getExplorerListItem(item)) == null || e.isEmpty())
            return ;
        explorerFileCopy.setReplaceKeepAction(AppSettingsSingleton.getInstance().getIdenticalFileCopyAction());

        List<String> allFiles = new ArrayList<>();
        int i = e.getSize();

        while (--i >= 0){
            String copy = e.getItem(i);
            File source = new File(copy);
            String targetPath = dirPath + "/" + source.getName();
            if (source.isDirectory() && targetPath.compareTo(copy) != 0 && targetPath.startsWith(copy) && targetPath.length() > copy.length()) {
                Notification.showToast(context, getString(R.string.error_copy_loop));
                return;
            }
            getAllCopiedFiles(allFiles, e.getItem(i));
        }
        if (allFiles.isEmpty())
            return;

        pasteFile(allFiles, dirPath, null, 0, e.isCut(), context);

        explorerFileCopy.setReplaceKeepAction(0);
        if (e.isCut()){
            e.clearList();
            e.setCut(false);
        }
    }

    public void getAllCopiedFiles(List<String> allFiles, String filePath){

        allFiles.add(filePath);

        File source = new File(filePath);
        if (source.isDirectory()) {
            File[] children = source.listFiles();
            for (File aChildren : children)
                getAllCopiedFiles(allFiles, aChildren.getPath());
        }
    }

    //TODO delete progression dialog
    protected void deleteMultiFile () {
        Context context;
        if ((context = obtainContext()) == null)
            return;
        Context finalContext = context;
        new AlertDialog.Builder(context).setIcon(R.drawable.folder_error)
                .setTitle(R.string.multiple_deletion)
                .setPositiveButton(getString(R.string.btn_affirmation), (dialog2, which) -> {
                    for (ItemObjects item: items) {
                        if (item.isSelected()){
                            File delFile = new File(item.getPath());
                            if (delFile.exists() && delFile.isDirectory()) {
                                if (!recDelete(delFile, finalContext))
                                    break;
                            }
                            else if (delFile.exists()) {
                                if (!delFile.delete()){
                                    Notification.showToast(finalContext, getString(R.string.delete_error));
                                    break;
                                }
                            } else{
                                Notification.showToast(finalContext, getString(R.string.files_not_found));
                                break;
                            }
                        }
                    }
                    selectMode = false;
                    upSelect();
                }).setNegativeButton(this.getString(R.string.btn_cancel), (dialog3, which) -> dialog3.cancel()).show();
    }

    //TODO delete progression dialog
    protected void deleteFile (String path, String name, boolean dialog) {
        Context context;
        if ((context = obtainContext()) == null)
            return;
        if (!dialog){
            File delFile = new File(path);
            if (delFile.exists() && delFile.isDirectory()) {
                if (!recDelete(delFile, context))
                    return;
            } else if (delFile.exists()){
                if (!delFile.delete())
                    Notification.showToast(context, String.format(getString(R.string.deletion_error_file), delFile.getName()));
            }
            upSelect();
            return;
        }
        Context finalContext = context;
        new AlertDialog.Builder(context).setIcon(R.drawable.folder_error)
                .setTitle(String.format(getString(R.string.deletion_error_file), name))
                .setPositiveButton(R.string.btn_affirmation, (dialog2, which) -> {
                    File delFile = new File(path);
                    if (delFile.exists()){
                        if (delFile.exists() && delFile.isDirectory()) {
                            if (!recDelete(delFile, finalContext))
                                return;
                        } else if (!delFile.delete())
                            Notification.showToast(context, String.format(getString(R.string.deletion_error_file), delFile.getName()));
                    }
                    upSelect();
                }).setNegativeButton(this.getString(R.string.btn_cancel), (dialog3, which) -> dialog3.cancel()).show();
    }

    private boolean recDelete(File dir, Context context){
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String aChildren : children) {
                File delFile = new File(dir, aChildren);
                if (delFile.isDirectory())
                    recDelete(delFile, context);
                else if (delFile.exists()) {
                    if (!delFile.delete()) {
                        Notification.showToast(context, getString(R.string.delete_error));
                        return false;
                    }
                } else {
                    Notification.showToast(context, getString(R.string.files_not_found));
                    return false;
                }
            }
            if (!dir.delete())
                Notification.showToast(context, String.format(getString(R.string.deletion_error_folder), dir.getName()));
        }
        return true;
    }

    private void sortFile(File[] files){
        if (files == null || files.length <= 1)
            return;
        if (fragSettings.getSortOrder() == 0) {
            Arrays.sort(files, (file1, file2) -> file1.getName().compareTo(file2.getName()));
        }
        else if (fragSettings.getSortOrder() == 1) {
            Arrays.sort(files, (file1, file2) -> ((file1.length() > file2.length()) ? -1 : (file1.length() > file2.length()) ? 1 : 0));
        }
        else if (fragSettings.getSortOrder() == 2) {
            Arrays.sort(files, (file1, file2) -> ((file1.isDirectory() && !file2.isDirectory()) ? -1 : ((file1.isDirectory() && file2.isDirectory())
                    || (!file1.isDirectory() && !file2.isDirectory())) ? 0 : 1));
        }
        else if (fragSettings.getSortOrder() == 3) {
            Arrays.sort(files, (file1, file2) -> ((file1.lastModified() > file2.lastModified()) ? -1 : (file1.lastModified() > file2.lastModified()) ? 1 : 0));
        }
    }

    private Context obtainContext(){
        Context context;
        if ((context = getContext()) == null && (context = getActivity()) == null) {
            Notification.showToast(getActivity(), getString(R.string.error_occurred));
            return null;
        }
        return context;
    }

    public void upSelect(){
        adapter.clearList();
        selectMode = false;
        if (isInfoFabOpen) {
            fabInfoMenu.setVisibility(View.INVISIBLE);
            fabInfoMenu.setClickable(false);
        }
        isInfoFabOpen = false;
        if (isFabOpen)
            changeFabMenuButtonState();
        getDir();
    }

    public void animateFAB(){

        if (isFabOpen){

            fab.startAnimation(rotate_backward);
            textViewFab1.setVisibility(View.INVISIBLE);
            textViewFab2.setVisibility(View.INVISIBLE);
            textViewFab3.setVisibility(View.INVISIBLE);
            textViewFab4.setVisibility(View.INVISIBLE);
            textViewFab5.setVisibility(View.INVISIBLE);
            textViewFab6.setVisibility(View.INVISIBLE);

            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab4.startAnimation(fab_close);
            fab5.startAnimation(fab_close);
            fab6.startAnimation(fab_close);

            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab4.setClickable(false);
            fab5.setClickable(false);
            fab6.setClickable(false);

            isFabOpen = false;

            Log.d("Fab", "close");

        } else {

            isFabOpen = true;

            fab.startAnimation(rotate_forward);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab6.startAnimation(fab_open);

            changeFabMenuButtonState();

            fab2.setClickable(true);
            fab3.setClickable(true);
            fab6.setClickable(true);
            textViewFab2.setVisibility(View.VISIBLE);
            textViewFab3.setVisibility(View.VISIBLE);
            textViewFab6.setVisibility(View.VISIBLE);

            Log.d("Fab","open");

        }
    }

    private void changeFabMenuButtonState() {

        if (!adapter.isAnItemSelect() && isFabOpen) {

            fab1.startAnimation(fab_close);
            ConstraintLayout.LayoutParams fab1Params = (ConstraintLayout.LayoutParams) fab1.getLayoutParams();
            fab1Params.height = 1;
            fab1.setLayoutParams(fab1Params);
            fab1.setClickable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                fab1.setElevation(0);
            ConstraintLayout.LayoutParams textViewFab1Params = (ConstraintLayout.LayoutParams) textViewFab1.getLayoutParams();
            textViewFab1Params.height = 0;
            textViewFab1.setLayoutParams(textViewFab1Params);
            textViewFab1.setVisibility(View.INVISIBLE);

            fab4.startAnimation(fab_close);
            ConstraintLayout.LayoutParams fab4Params = (ConstraintLayout.LayoutParams) fab4.getLayoutParams();
            fab4Params.height = 1;
            fab4.setLayoutParams(fab4Params);
            fab4.setClickable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                fab4.setElevation(0);
            ConstraintLayout.LayoutParams textViewFab4Params = (ConstraintLayout.LayoutParams) textViewFab4.getLayoutParams();
            textViewFab4Params.height = 0;
            textViewFab4.setLayoutParams(textViewFab4Params);
            textViewFab4.setVisibility(View.INVISIBLE);

            fab5.startAnimation(fab_close);
            ConstraintLayout.LayoutParams fab5Params = (ConstraintLayout.LayoutParams) fab5.getLayoutParams();
            fab5Params.height = 1;
            fab5.setLayoutParams(fab5Params);
            fab5.setClickable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                fab5.setElevation(0);
            ConstraintLayout.LayoutParams textViewFab5Params = (ConstraintLayout.LayoutParams) textViewFab5.getLayoutParams();
            textViewFab5Params.height = 0;
            textViewFab5.setLayoutParams(textViewFab5Params);
            textViewFab5.setVisibility(View.INVISIBLE);

        } else if (isFabOpen) {

            ConstraintLayout.LayoutParams fab1Params = (ConstraintLayout.LayoutParams) fab1.getLayoutParams();
            fab1Params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            fab1.setLayoutParams(fab1Params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                fab1.setElevation(getResources().getDimension(R.dimen.FabButtonElevation));
            ConstraintLayout.LayoutParams textViewFab1Params = (ConstraintLayout.LayoutParams) textViewFab1.getLayoutParams();
            textViewFab1Params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            textViewFab1.setLayoutParams(textViewFab1Params);

            ConstraintLayout.LayoutParams fab4Params = (ConstraintLayout.LayoutParams) fab4.getLayoutParams();
            fab4Params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            fab4.setLayoutParams(fab4Params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                fab4.setElevation(getResources().getDimension(R.dimen.FabButtonElevation));
            ConstraintLayout.LayoutParams textViewFab4Params = (ConstraintLayout.LayoutParams) textViewFab4.getLayoutParams();
            textViewFab4Params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            textViewFab4.setLayoutParams(textViewFab4Params);

            ConstraintLayout.LayoutParams fab5Params = (ConstraintLayout.LayoutParams) fab5.getLayoutParams();
            fab5Params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            fab5.setLayoutParams(fab5Params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                fab5.setElevation(getResources().getDimension(R.dimen.FabButtonElevation));
            ConstraintLayout.LayoutParams textViewFab5Params = (ConstraintLayout.LayoutParams) textViewFab5.getLayoutParams();
            textViewFab5Params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            textViewFab5.setLayoutParams(textViewFab5Params);

            fab1.startAnimation(fab_open);
            fab4.startAnimation(fab_open);
            fab5.startAnimation(fab_open);
            fab1.setClickable(true);
            fab4.setClickable(true);
            fab5.setClickable(true);
            textViewFab1.setVisibility(View.VISIBLE);
            textViewFab4.setVisibility(View.VISIBLE);
            textViewFab5.setVisibility(View.VISIBLE);
        }
    }
}
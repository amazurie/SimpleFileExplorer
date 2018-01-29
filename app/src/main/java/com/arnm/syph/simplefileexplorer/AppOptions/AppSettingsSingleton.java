package com.arnm.syph.simplefileexplorer.AppOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Syph on 15/01/2018.
 */

public class AppSettingsSingleton {
    private static AppSettingsSingleton explorerSettingsInstance = null;

    private List<AppSettingsSingleton.fragSettingsVar> fragSettingsList = null;
    private boolean directOpen = false;
    private boolean infoMenu = false;
    private int identicalFileCopyAction = 0;
    private boolean backReachParent = false;
    private boolean backInfiniteLoop = false;

    public static AppSettingsSingleton getInstance() {
        if (explorerSettingsInstance == null){
            explorerSettingsInstance = new AppSettingsSingleton();
        }
        return explorerSettingsInstance;
    }

    private AppSettingsSingleton() {
        fragSettingsList = new ArrayList<>();
    }

    public List<fragSettingsVar> getExplorerSettingsList() {
        return fragSettingsList;
    }

    public fragSettingsVar getExplorerSettingsItemByName(String name) {
        for (fragSettingsVar fragSettings: fragSettingsList) {
            if (name.compareTo(fragSettings.getName()) == 0)
                return fragSettings;
        }
        return null;
    }

    public void addExplorerSettingsItem(String name){
        if (name != null)
            fragSettingsList.add(new fragSettingsVar(name));
    }

    public void clearList(){
        fragSettingsList.clear();
    }

    public boolean isDirectOpen() {
        return directOpen;
    }

    public void setDirectOpen(boolean directOpen) {
        this.directOpen = directOpen;
    }

    public boolean isInfoMenu() {
        return infoMenu;
    }

    public void setInfoMenu(boolean infoMenu) {
        this.infoMenu = infoMenu;
    }

    public int getIdenticalFileCopyAction() {
        return identicalFileCopyAction;
    }

    public void setIdenticalFileCopyAction(int identicalFileCopyAction) {
        this.identicalFileCopyAction = identicalFileCopyAction;
    }

    public boolean isBackReachParent() {
        return backReachParent;
    }

    public void setBackReachParent(boolean backReachParent) {
        this.backReachParent = backReachParent;
    }

    public boolean isBackInfiniteLoop() {
        return backInfiniteLoop;
    }

    public void setBackInfiniteLoop(boolean backInfiniteLoop) {
        this.backInfiniteLoop = backInfiniteLoop;
    }

    public class fragSettingsVar {
        private String name = null;
        private String defaultPath = null;
        private String lastPath = null;
        private Integer displayType = 1;
        private Integer sortOrder = 1;
        private boolean showHiddenFiles = false;
        private List<String> history = new ArrayList<>();

        fragSettingsVar(String name){
            this.name = name;
        }

        public String getDefaultPath() {
            return defaultPath;
        }

        public void setDefaultPath(String defaultPath) {
            this.defaultPath = defaultPath;
        }

        public String getName() {
            return name;
        }

        public String getLastPath() {
            return lastPath;
        }

        public void setLastPath(String lastPath) {
            this.lastPath = lastPath;
        }

        public Integer getDisplayType() {
            return displayType;
        }

        public void setDisplayType(Integer displayType) {
            this.displayType = displayType;
        }

        public boolean isShowHiddenFiles() {
            return showHiddenFiles;
        }

        public void setShowHiddenFiles(boolean showHiddenFiles) {
            this.showHiddenFiles = showHiddenFiles;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getLastHistory(){
            if (history.size() <= 0)
                return null;
            return history.get(history.size() - 1);
        }

        public int getHistorySize(){
            return history.size();
        }

        public void addHistory(String historyPath){
            history.add(historyPath);
        }

        public void delLastHistory(){
            int id = history.size() - 1;
            if (id > 0)
                history.remove(id);
        }
    }

}

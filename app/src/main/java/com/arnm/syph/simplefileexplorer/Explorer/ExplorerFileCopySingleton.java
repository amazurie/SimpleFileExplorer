package com.arnm.syph.simplefileexplorer.Explorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Syph on 08/01/2018.
 */

public class ExplorerFileCopySingleton {
    private static ExplorerFileCopySingleton fileCopyInstance = null;

    private List<FileList> explorerFileCopy = null;
    private boolean currCopy = false;

    public static ExplorerFileCopySingleton getInstance() {
        if (fileCopyInstance == null){
            fileCopyInstance = new ExplorerFileCopySingleton();
        }
        return fileCopyInstance;
    }

    private ExplorerFileCopySingleton() {
        explorerFileCopy = null;
        currCopy = false;
    }

    void newExplorerList(){
        if (explorerFileCopy == null)
            explorerFileCopy = new ArrayList<FileList>();
    }

    public void addExplorerListItem(String name){
        explorerFileCopy.add(new FileList(name));
    }

    public int getExplorerListItemId(String name){
        int i = explorerFileCopy.size();
        while (i-- > 0)
            if (name.compareToIgnoreCase(explorerFileCopy.get(i).getName()) == 0)
                return i;
        return -1;
    }

    public FileList getExplorerListItem(int id){
        return explorerFileCopy.get(id);
    }

    public void clearExplorerList(){
        int i = explorerFileCopy.size();
        while (--i >= 0)
            explorerFileCopy.get(i).clearList();
        explorerFileCopy.clear();
    }

    public void delExplorerList(int id){
        explorerFileCopy.remove(id);
    }

    public boolean isCurrCopy() {
        return this.currCopy;
    }

    public int getExplorerListSize(){
        return explorerFileCopy.size();
    }

    public void setCurrCopy(boolean currCopy) {
        this.currCopy = currCopy;
    }

    private void setExplorerFileItemCut(int id, boolean cut){
        explorerFileCopy.get(id).setCut(cut);
    }

    private boolean getExplorerFileItemCut(int id){
        return explorerFileCopy.get(id).isCut();
    }

    public class FileList {
        private List<String> fileCopyList = null;
        private String name;
        private int replaceKeepAction = 0;
        private int sameFileAction = 0;
        private boolean isCut = false;

        public FileList(String name) {
            fileCopyList = new ArrayList<>();
            this.name = name;
        }

        public String getItem(int position) {
            return fileCopyList.get(position);
        }

        public int getSize() {
            return fileCopyList.size();
        }

        public boolean isEmpty(){
            return fileCopyList.isEmpty();
        }

        public void listAdd(String path) {
            fileCopyList.add(path);
        }

        public void clearList(){
            fileCopyList.clear();
        }

        public boolean isCut() {
            return isCut;
        }

        public void setCut(boolean cut) {
            isCut = cut;
        }

        public String getName() {
            return name;
        }

        public int getSameFileAction() {
            return sameFileAction;
        }

        public void setSameFileAction(int sameFileAction) {
            this.sameFileAction = sameFileAction;
        }

        public int getReplaceKeepAction() {
            return replaceKeepAction;
        }

        public void setReplaceKeepAction(int replaceKeepAction) {
            this.replaceKeepAction = replaceKeepAction;
        }
    }

}

package com.arnm.syph.simplefileexplorer.Explorer;

import android.graphics.Bitmap;

/**
 * Created by Syph on 22/12/2017.
 */

public class ItemObjects {

    private String fileName;
    private int imagePath;
    private Bitmap image = null;
    private String fileInfo;
    private String path;
    private String size;
    private boolean isSelected;
    private boolean alreadyProcess = false;

    ItemObjects(String fileName, int imagePath, String fileInfo, String size, String path) {
        this.fileName = fileName;
        this.imagePath = imagePath;
        this.fileInfo = fileInfo;
        this.path = path;
        this.size = size;
        this.isSelected = false;
    }

    public String getFileName() {
        return fileName;
    }

    public void setImagePath(int image) {
        this.imagePath = image;
    }

    int getImagePath() {
        return imagePath;
    }

    boolean isSelected() {
        return isSelected;
    }

    String getFileInfo() {
        return fileInfo;
    }

    void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    boolean isAlreadyProcess() {
        return alreadyProcess;
    }

    void setAlreadyProcessTrue() {
        this.alreadyProcess = alreadyProcess;
    }
}

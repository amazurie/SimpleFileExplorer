package com.arnm.syph.simplefileexplorer.Explorer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnm.syph.simplefileexplorer.R;

/**
 * Created by Syph on 22/12/2017.
 */

public class ExplorerView extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

    private ItemClickListener mListener;

    private TextView textViewView;
    ImageView imageView;
    private TextView fileInfoView;

    ExplorerView(View itemView, ItemClickListener listener) {
        super(itemView);

        mListener = listener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        textViewView = itemView.findViewById(R.id.fileName);
        imageView = itemView.findViewById(R.id.image);
        fileInfoView = itemView.findViewById(R.id.fileInfo);
    }

    void bind(ItemObjects item, int viewType){
        textViewView.setText(item.getFileName());
        if (item.isAlreadyProcess()) {
            if (item.getImage() != null)
                imageView.setImageBitmap(item.getImage());
            else
                imageView.setImageResource(item.getImagePath());
        } else if (item.getImagePath() == -1 && item.getPath() != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap decodedImg = BitmapFactory.decodeFile(item.getPath(), options);
            double i = 0.9;
            while (decodedImg.getWidth() * i > 240 && i > 0.1)
                i -= 0.1;
            Double width = decodedImg.getWidth() * i;
            Double height = decodedImg.getHeight() * i;
            decodedImg = Bitmap.createScaledBitmap(decodedImg, width.intValue(), height.intValue(), true);
            if (options.outWidth > 0 && options.outHeight > 0) {
                imageView.setImageBitmap(decodedImg);
                item.setImage(decodedImg);
            } else {
                imageView.setImageResource(R.drawable.file_icon_img);
                item.setImagePath(R.drawable.file_icon_img);
            }
        }
        else if (item.getImagePath() == -2 && item.getPath() != null) {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(item.getPath(), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            Matrix matrix = new Matrix();
            if (thumb.getHeight() > 0 && thumb.getWidth() > 0) {
                int width = thumb.getWidth();
                int height = thumb.getHeight();
                int startWidth = 0;
                int startHeight = 0;
                if (thumb.getWidth() > thumb.getHeight()) {
                    startWidth = thumb.getWidth() / 4;
                    width = thumb.getHeight();
                }
                else {
                    startHeight = thumb.getHeight() / 4;
                    height = thumb.getWidth();
                }
                Bitmap bitmap = Bitmap.createBitmap(thumb, startWidth, startHeight, width, height, matrix, true);
                imageView.setImageBitmap(bitmap);
                item.setImage(bitmap);
            }
            else {
                imageView.setImageResource(R.drawable.file_icon_vid);
                item.setImagePath(R.drawable.file_icon_vid);
            }
        }
        else
            imageView.setImageResource(item.getImagePath());
        String info = item.getFileInfo();
        if (viewType != 3 && item.getSize() != null)
            info += item.getSize();
        fileInfoView.setText(info);
        item.setAlreadyProcessTrue();
    }

    @Override
    public void onClick(View v) {
        mListener.onClick(v, getAdapterPosition(), false);
    }

    @Override
    public boolean onLongClick(View v) {
        mListener.onClick(v, getAdapterPosition(), true);
        return true;
    }
}

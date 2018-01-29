package com.arnm.syph.simplefileexplorer.Explorer;

import android.view.View;

/**
 * Created by Syph on 26/12/2017.
 */

public interface ItemClickListener {

    void onClick(View v, int position, boolean isLongClick);
}
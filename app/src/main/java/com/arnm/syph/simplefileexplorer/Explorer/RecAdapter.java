package com.arnm.syph.simplefileexplorer.Explorer;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arnm.syph.simplefileexplorer.R;

import java.util.List;

/**
 * Created by Syph on 22/12/2017.
 */

public class RecAdapter extends RecyclerView.Adapter<ExplorerView> {

    private Context context;
    private ItemClickListener mListener;
    private int viewType = 0;
    private List<ItemObjects> list;

    RecAdapter(List<ItemObjects> list, ItemClickListener listener) {
        this.list = list;
        mListener = listener;
    }

    @Override
    public ExplorerView onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        context = viewGroup.getContext();
        View view;
        if (viewType == 0)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_item_simple, viewGroup, false);
        else if (viewType == 1)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_item_details, viewGroup,false);
        else if (viewType == 2) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_item_icon, viewGroup, false);
            TextView txt = view.findViewById(R.id.fileName);
            txt.setTextSize(12);
        } else if (viewType == 3)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_item_content, viewGroup, false);
        else
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_item_details, viewGroup,false);
        return new ExplorerView(view, mListener);
    }

    void setViewType(int viewType){
        this.viewType = viewType;
    }

    int getViewType(){
        return viewType;
    }

    @Override
    public void onBindViewHolder(ExplorerView viewHolder, int position) {
        ItemObjects myObject = list.get(position);
        viewHolder.imageView.setImageDrawable(null);
        viewHolder.bind(myObject, viewType);
        final ItemObjects item = list.get(position);
        viewHolder.itemView.setBackgroundColor(item.isSelected() ? context.getResources().getColor(R.color.itemSelection) : Color.TRANSPARENT);
    }

    void clearList(){
        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(0, size);
    }

    boolean isOnlyOneSelected(){
        int i = 0;

        for(ItemObjects item : list) {
            if (item.isSelected())
                i++;
            if (item.isSelected() && i > 1)
                return false;
        }
        return true;
    }

    boolean isAnItemSelect(){
        for(ItemObjects item : list)
            if (item.isSelected())
                return true;
        return false;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

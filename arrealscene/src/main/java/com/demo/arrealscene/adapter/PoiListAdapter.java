package com.demo.arrealscene.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.amap.api.services.core.PoiItem;
import com.demo.arrealscene.R;
import com.demo.arrealscene.base.BaseRecyclerViewAdapter;
import java.util.List;

/**
 * Created by Be on 2017/1/24.
 * Poi列表适配器
 */

public class PoiListAdapter extends BaseRecyclerViewAdapter<PoiViewHolder, PoiItem> {


    public PoiListAdapter(Context context, List<PoiItem> data) {
        super(context, data);
    }

    @Override
    public PoiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View view = View.inflate(parent.getContext(), R.layout.item_poidetail, null);
        View view = inflater.inflate(R.layout.item_poidetail, parent, false);
        return new PoiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PoiViewHolder holder, int position) {
        if (holder == null) {
            System.out.println("null");
        } else {
            holder.poiName.setText(data.get(position).getTitle());

        }
    }
}

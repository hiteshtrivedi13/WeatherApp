package com.example.weather.home;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weather.R;
import com.example.weather.model.SearchedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hitesh on 3/20/16.
 */
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder>
{

    private List<SearchedData> searchedDataList;
    private OnItemClickListener listener;

    public SearchListAdapter(OnItemClickListener listener)
    {
        this.listener = listener;
        searchedDataList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.name.setText(searchedDataList.get(position).getName());
    }

    @Override
    public int getItemCount()
    {
        if(searchedDataList != null)
            return searchedDataList.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView name;

        public ViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            name = (TextView) itemView.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.mainLayout:
                    if(listener != null)
                    {
                        Location location = new Location("");
                        location.setLatitude(searchedDataList.get(getAdapterPosition()).getLat());
                        location.setLongitude(searchedDataList.get(getAdapterPosition()).getLng());
                        listener.onItemClick(location);
                    }
                        break;
            }
        }
    }

    public void updateData(List<SearchedData> searchedDataList)
    {
        if(searchedDataList != null)
        {
            this.searchedDataList.clear();
            this.searchedDataList.addAll(searchedDataList);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Location location);
    }
}

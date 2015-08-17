package com.sutromedia.android.lib.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class GenericListAdapter extends BaseAdapter {

    //nested interfaces / classes
    public interface IListViewItemRenderer {
        void onSetupView(int viewId, View view, Object data);
        void onItemClicked(int viewId, Object data);
    }

    private class AdapterEntry {
        int     m_uiTemplate;
        Object  m_data;
        
        AdapterEntry(Object data, int ui) {
            m_uiTemplate  = ui;
            m_data = data;
        }
    }    

    //implementation
    private Context m_context;
    private List<AdapterEntry> mItems = new ArrayList<AdapterEntry>();
    private Hashtable<Integer,Integer> mViews = new Hashtable<Integer,Integer>();

    public GenericListAdapter(Context context) {
        m_context = context;
    }

    public void setupWith(ListView list) {
        list.setAdapter(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doClickNotification(position);
            }
        });        
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position).m_data;
    }

    public int getItemTemplate(int position) {
        return mItems.get(position).m_uiTemplate;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public void addItem(Object data, int templateId) {
        mItems.add(new AdapterEntry(data, templateId));
        if (!mViews.containsKey(templateId)) {
            mViews.put(templateId, mViews.size());
        }
    }
    
    public List<Object> getAll() {
        List<Object> all = new ArrayList<Object>();
        for (AdapterEntry entry : mItems) {
            all.add(entry.m_data);
        }
        return all;
    }
    
    public void clear() {
        mItems.clear();
    }
    
    public int getViewTypeCount () {
        return mViews.size();
    }
    
    public int getItemViewType (int position) {
        return mViews.get(getItemTemplate(position));
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        int viewId = getItemTemplate(position);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(viewId, parent, false);        
        } 

        if (getRenderer()!=null) {
            getRenderer().onSetupView(viewId, view, getItem(position));
        }
        return view;
    }
    
    private void doClickNotification(int position) {
        if (getRenderer() != null) {
            int viewId = getItemTemplate(position);
            getRenderer().onItemClicked(viewId, getItem(position));
        }
    }    
    
    private IListViewItemRenderer getRenderer() {
        IListViewItemRenderer renderer = null;
        if (m_context instanceof IListViewItemRenderer) {
            renderer = (IListViewItemRenderer)m_context;
        }
        
        return renderer;
    }
}

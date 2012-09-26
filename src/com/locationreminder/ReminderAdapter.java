package com.locationreminder;

import java.util.ArrayList;
import java.util.HashMap;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReminderAdapter extends BaseAdapter{
	Context mContext;
	ArrayList<HashMap<String, String>> reminderlist;
	HashMap<String, String> hMap;

	public ReminderAdapter(Context mContext,ArrayList<HashMap<String, String>> reminderlist) 
	{
		// TODO Auto-generated constructor stub
		this.mContext=mContext;
		this.reminderlist=reminderlist;	
	}

	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return reminderlist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return reminderlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// TODO Auto-generated method stub
		LinearLayout rowLayout = null;
		if (convertView == null) {
			rowLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.reminders, parent, false);
		} 
		else 
		{
			rowLayout = (LinearLayout) convertView;
		}
		TextView title = (TextView) rowLayout.findViewById(R.id.title);
		TextView distance = (TextView) rowLayout.findViewById(R.id.distance);
		TextView address = (TextView) rowLayout.findViewById(R.id.address);
		
		hMap=reminderlist.get(position);
		title.setText(hMap.get("title"));
		distance.setText(hMap.get("distance")+" "+hMap.get("meter")+" away");
		address.setText(hMap.get("address"));
		
		return rowLayout;
	}

	

}

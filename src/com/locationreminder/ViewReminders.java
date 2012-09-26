package com.locationreminder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.android.maps.GeoPoint;



import android.R.integer;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewReminders extends Activity{	
	Context context=ViewReminders.this;
	ListView listView;
	ReminderAdapter reminderAdapter;
	SharedPreferences sharedPreferences;
	HashMap<String, String> hMap;
	ArrayList<HashMap<String, String>> reminderlist;
	String hashString;
	double longitude,latitude,distance;
	 LocationManager locationManager;
	 Location locationObj;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		setContentView(R.layout.viewreminders);
		sharedPreferences=getSharedPreferences("LocationReminders",MODE_PRIVATE);
		listView=(ListView)findViewById(R.id.listview);
		
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

	       locationObj=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		    if(locationObj==null)
		    {
		    	locationObj=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		    }
		    if(locationObj==null)
		    {
		    	locationObj=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		    }
	       
		if( locationObj!=null) {
			updateDistances();
			reminderAdapter=new ReminderAdapter(this,reminderlist);
		    listView.setAdapter(reminderAdapter);
		} else {
			Toast.makeText(ViewReminders.this, "Please Check Your Internet/GPS settings", Toast.LENGTH_LONG).show();
			this.finish();
		}
		
	
	    
	    
	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ViewReminders.this,ReminderDetails.class);
				intent.putExtra("title",reminderlist.get(position).get("title").toString());
                startActivity(intent);
				
			}
		}); 
		
	}
	
	/**
	 * updates current distance from the current location to destination
	 */
	public void updateDistances()
	{
		
		SharedPreferences.Editor editor=sharedPreferences.edit();
		Set<String> titleSet = sharedPreferences.getAll().keySet();
		Iterator iterator=titleSet.iterator();
		reminderlist=new ArrayList<HashMap<String,String>>();
		while(iterator.hasNext())
		{
			hMap=HashmapConversion.StringToHashmap(sharedPreferences.getString(iterator.next().toString(),null));
			reminderlist.add(hMap);
		}
		
		latitude=locationObj.getLatitude();
        longitude=locationObj.getLongitude();		
        Location locationA = new Location("point A");  
		locationA.setLatitude(latitude);
		locationA.setLongitude(longitude); 

		Location locationB = new Location("point B");  
		for(int i=0;i<reminderlist.size();i++)
		{
			hMap=reminderlist.get(i);
			locationB.setLatitude(Double.valueOf(hMap.get("latitude").trim()));  
			locationB.setLongitude(Double.valueOf(hMap.get("longitude").trim()));  		
			distance = locationA.distanceTo(locationB);
			if(hMap.get("meter").equalsIgnoreCase("Kms"))
			{
				Log.d("reminder","distance: "+distance);
				distance=((double)(int)(distance))/1000;
			}
			else
			{
				distance=(int)distance;
				
			}
			hMap.put("distance",Double.toString(distance));
			hashString=HashmapConversion.HashmapToString(hMap);
			editor.putString(hMap.get("title"),hashString);			
		}
		editor.commit();
	}
	
}

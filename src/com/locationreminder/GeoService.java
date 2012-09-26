package com.locationreminder;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;
// commtting for git
public class GeoService extends Service{
	LocationManager locationManager;
	LocationListener locationListener;
	HashMap<String, String> hMap=null;
	SharedPreferences sharedPreferences;
	ArrayList<HashMap<String, String>> reminderlist=null;
	Context context;
	double latitude,longitude;
	
	public NotificationManager mNotificationManager;
	public CharSequence tickerText,contentTitle,contentText;
	public long when;
	public Notification notification ;
	public Intent notificationIntent ;
	public PendingIntent contentIntent;
	String provider=null;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sharedPreferences=getSharedPreferences("LocationReminders",MODE_PRIVATE);
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationListener=new MyLocationListener();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);		
		
		if( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			provider = LocationManager.GPS_PROVIDER;
		} else if( locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null )  {
			provider = LocationManager.NETWORK_PROVIDER;
		} else if( locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null )  {
			provider = LocationManager.PASSIVE_PROVIDER;
		}
		
		if( locationListener!=null )
			locationManager.removeUpdates(locationListener);
		
        if( provider!=null) {
        	 locationManager.requestLocationUpdates(provider, 0,10, locationListener);
        }
    
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub	
		super.onDestroy();	
	
	}
	
	/**
	 * Checking whether any reminder is near by this place
	 * if it is, then will notify the user
	 */
	public void getReminders()
	{
		context=GeoService.this;
		SharedPreferences.Editor editor=sharedPreferences.edit();
		Set<String> titleSet = sharedPreferences.getAll().keySet();
		Iterator iterator=titleSet.iterator();
		reminderlist=new ArrayList<HashMap<String,String>>();
		while(iterator.hasNext())
		{
			hMap=HashmapConversion.StringToHashmap(sharedPreferences.getString(iterator.next().toString(),null));
			reminderlist.add(hMap);
		}
		
			Location location;
		    location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		    if(location==null)
		    {
		    	location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		    }
		    if(location==null)
		    {
		    	location=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		    }
        
		    if( location!=null ) {
			    latitude = location.getLatitude();
			    longitude = location.getLongitude(); 
		    } else {
		    	return;
		    }
		    
        Location locationA = new Location("point A");  
		locationA.setLatitude(latitude);
		locationA.setLongitude(longitude); 

		Location locationB = new Location("point B");  
		for(int i=0;i<reminderlist.size();i++)
		{
			hMap=reminderlist.get(i);
			locationB.setLatitude(Double.valueOf(hMap.get("latitude").trim()));  
			locationB.setLongitude(Double.valueOf(hMap.get("longitude").trim()));  
			double distance = locationA.distanceTo(locationB);
			hMap.put("distance",Double.toString(distance));
			String hashString=HashmapConversion.HashmapToString(hMap);
			editor.putString(hMap.get("title"),hashString);			
		}
		editor.commit();
	}
	
	
	/**
	 * A location listener to identify location changes
	 */
	public class MyLocationListener implements LocationListener
    {

		@Override
		public void onLocationChanged(Location location) {
			getReminders();
			
			double lat=location.getLatitude();
	        double lon=location.getLongitude();	
	        Location locationA = new Location("point A");  
			locationA.setLatitude(lat);
			locationA.setLongitude(lon); 

			Location locationB = new Location("point B");  
			for(int i=0;i<reminderlist.size();i++)
			{
				hMap=reminderlist.get(i);
				locationB.setLatitude(Double.valueOf(hMap.get("latitude").trim()));  
				locationB.setLongitude(Double.valueOf(hMap.get("longitude").trim()));  
				double distance = locationA.distanceTo(locationB);
				if(hMap.get("meter").equalsIgnoreCase("Kms"))
				{
					distance=((double)(int)(distance))/1000;
						
				}
				else
				{
						distance=(int)distance;
				}
				if(distance<Double.valueOf(hMap.get("radius").trim())&&hMap.get("status").equals("active"))
				{
					Log.d("reminder",hMap.get("title")+" is "+distance+" "+hMap.get("meter")+" away");
					
					mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);		
					int icon = R.drawable.launcher;
					tickerText = "Geo Reminder";
					when =  System.currentTimeMillis();
					int id=(int) when;
					notification = new Notification(icon, tickerText, when);		
					contentTitle = hMap.get("title");
					contentText =hMap.get("title")+" is "+distance+" "+hMap.get("meter")+" away";
					notificationIntent = new Intent(context.getApplicationContext(),ReminderDetails.class);
					notificationIntent.putExtra("title",hMap.get("title"));
					contentIntent = PendingIntent.getActivity(context, id, notificationIntent, 0);
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					mNotificationManager.notify(id, notification);
					
				}
			}
		  
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
    	
    }

}

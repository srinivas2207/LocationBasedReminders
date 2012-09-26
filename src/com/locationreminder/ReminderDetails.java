package com.locationreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class ReminderDetails extends Activity{
	TextView title,radius,distance,roaddistance,walkingdistance;
	EditText address,message,currentAddress;
	SharedPreferences sharedPreferences;
	HashMap<String, String> hMap;
	boolean status=true;
	String titleData;
	String hashData;
	SharedPreferences.Editor editor;
	double sourceLat,sourceLon;
	String[] dist;
	Location locationObj;
	GoogleMapsData googleMapsData =new GoogleMapsData();
    
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminderdetails);
		LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		

		locationObj = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (locationObj == null) {
			locationObj = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (locationObj == null) {
			locationObj = locationManager
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		
		sourceLat=locationObj.getLatitude();
		sourceLon=locationObj.getLongitude();	
		
		sharedPreferences=getSharedPreferences("LocationReminders",MODE_PRIVATE);
		titleData=getIntent().getStringExtra("title");
		
		title=(TextView)findViewById(R.id.title);

		radius=(TextView)findViewById(R.id.radius);
		distance=(TextView)findViewById(R.id.distance);
		address=(EditText)findViewById(R.id.address);
		message=(EditText)findViewById(R.id.message);
		roaddistance=(TextView)findViewById(R.id.rdistance);
		walkingdistance=(TextView)findViewById(R.id.wdistance);
		currentAddress = (EditText)findViewById(R.id.currentAddress);
		
		hashData=sharedPreferences.getString(titleData,null);
		hMap=HashmapConversion.StringToHashmap(hashData);
		
		title.setText(hMap.get("title"));

		radius.setText(hMap.get("radius")+" "+hMap.get("meter"));
		double currDistance=findDistance(hMap.get("latitude"), hMap.get("longitude"),sourceLat,sourceLon);
		if(hMap.get("meter").equalsIgnoreCase("Kms"))
		{
			currDistance=((double)(int)(currDistance))/1000;
		}
		else
		{
			currDistance=(int)currDistance;
		}
		
			distance.setText(currDistance+" "+hMap.get("meter"));
		
		message.setText(hMap.get("message"));
		address.setText(hMap.get("address"));
		
		setCurrentAddress();
		
		if(hMap.get("status").equals("active"))
			status=true;
		else
			status=false;
		
		Thread rThread=new Thread(new DistanceMeasure(Double.toString(sourceLat),Double.toString(sourceLon), hMap.get("latitude"), hMap.get("longitude"),"driving"));
		rThread.start();
		Thread wThread=new Thread(new DistanceMeasure(Double.toString(sourceLat),Double.toString(sourceLon), hMap.get("latitude"), hMap.get("longitude"),"walking"));
		wThread.start();
	}

	
	
	/**
	 * setting current address
	 */
	
	public void setCurrentAddress() {
		    List<Address> addresses = googleMapsData.getFromLocation(sourceLat,sourceLon, 1);			
			String location="";

			if(addresses.size()>0)
			{	
					location+=addresses.get(0).getAddressLine(0);
				
			}     
	        
	        currentAddress.setText(location);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.reminderdetails_menu, menu);
    	return true;
	}
	 @Override
	    public boolean onPrepareOptionsMenu(Menu menu) {

	        MenuItem enable = menu.findItem(R.id.enable);
	   
	        if (status==true) 
	        {
	            enable.setTitle("Disable");
	        } else 
	        {
	        	 enable.setTitle("Enable");
	        }

	        return super.onPrepareOptionsMenu(menu);
	    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) 
		{
			case R.id.roadmap:
				Intent roadIntent=new Intent(ReminderDetails.this,RoadMap.class);
				String radius;
				if(hMap.get("meter").equalsIgnoreCase("Kms"))
					radius=Double.toString(Double.parseDouble(hMap.get("radius"))*1000);
				else
					radius=hMap.get("radius");
				String[] destination=new String[]{hMap.get("latitude"),hMap.get("longitude"),radius};
				roadIntent.putExtra("destination",destination);
				startActivity(roadIntent);
			break;
			case R.id.delete:
				editor=sharedPreferences.edit();
				editor.remove(titleData);
				editor.commit();
				Intent intent=new Intent(ReminderDetails.this,LocationReminderActivity.class);
				startActivity(intent);
				
				break;
			case R.id.enable:	
				if(status==true)
				{
					editor=sharedPreferences.edit();
					hMap.put("status","inactive");
					hashData=HashmapConversion.HashmapToString(hMap);
					editor.putString(titleData,hashData);
					editor.commit();
					status=false;
				}
				else
				{
					editor=sharedPreferences.edit();
					hMap.put("status","active");
					hashData=HashmapConversion.HashmapToString(hMap);
					editor.putString(titleData,hashData);
					editor.commit();	
					status=true;
				}
				
				break;
	
			default:
				break;
		}
		return true;
	}
	
	/**
	 * To find distance between two sets of longitude and latitude
	 * @return distance in meters
	 */
	public double findDistance(String lat,String lon,double srclat,double srclon)
	{
        Location locationA = new Location("point A");  
		locationA.setLatitude(srclat);
		locationA.setLongitude(srclon); 
	
		Location locationB = new Location("point B");  
		locationB.setLatitude(Double.valueOf(lat.trim()));  
		locationB.setLongitude(Double.valueOf(lon.trim()));  
		return locationA.distanceTo(locationB);
		
	}
	
	Handler myUpdateHandler = new Handler() {
        public void handleMessage(Message msg) {
                switch (msg.what) {
                case 0:
                	roaddistance.setText(dist[0]+"("+dist[1]+")");
                        break;
                case 1:
                	walkingdistance.setText(dist[0]+"("+dist[1]+")");
                    break;
                default:
                        break;
                }
                super.handleMessage(msg);
        }
   };
	
   
   /**
    * This thread used to find the walking and driving distance between source and destination
    */
	public class DistanceMeasure implements Runnable
	{
		String srcLat,srcLon,destLat,destLon,travelType;
		public DistanceMeasure(String srcLat,String srcLon,String destLat,String destLon,String travelType) 
		{
			this.srcLat=srcLat;
			this.destLat=destLat;
			this.srcLon=srcLon;
			this.destLon=destLon;
			this.travelType=travelType;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String urlStr = "http://maps.google.com/maps/api/directions/json?origin="+srcLat+","+srcLon+"&destination="+destLat+","+destLon+"&mode="+travelType+"&sensor=false";
			Log.d("d&d","url:"+urlStr);
			String[] results=new String[2];
            InputStream is = null;
          	String result = "";
          	JSONObject jArray = null;
          	try{
          		HttpClient httpclient = new DefaultHttpClient();
          		HttpPost httppost = new HttpPost(urlStr);
          		HttpResponse response = httpclient.execute(httppost);
          		HttpEntity entity = response.getEntity();
          		is = entity.getContent();

          	}catch(Exception e){
          		Log.e("log_tag", "Error in http connection "+e.toString());
          	}

          	//convert response to string
          	try{
          		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
          		StringBuilder sb = new StringBuilder();
          		String line = null;
          		while ((line = reader.readLine()) != null) {
          			sb.append(line);
          		}
          		is.close();
          		result=sb.toString();
          	}catch(Exception e){
          		Log.e("log_tag", "Error converting result "+e.toString());
          	}
          	
              
              JSONArray responseArray = null;
              try {
            		jArray = new JSONObject(result);
                    responseArray = jArray.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                    JSONObject legsobject=responseArray.getJSONObject(0);
                    results[0]=legsobject.getJSONObject("distance").getString("text").toString();
                    results[1]=legsobject.getJSONObject("duration").getString("text").toString();
                    dist=results;
                    
                    
                    JSONArray steps=legsobject.getJSONArray("steps");
                    JSONObject jobj=null;
                    for(int i=0;i<steps.length();i++)
                    {
                    	 jobj=steps.getJSONObject(i).getJSONObject("start_location");                  	
                    	 Log.d("log_tag","points: "+jobj.getString("lat")+","+jobj.getString("lat"));
                    	 jobj=steps.getJSONObject(i).getJSONObject("end_location");
                    }
                    Log.d("log_tag","steps: "+steps.length());
                    
                    Message m = new Message();
                    if(travelType.equalsIgnoreCase("walking"))
                    m.what=1;
                    else
                    m.what=0;
                    myUpdateHandler.sendMessage(m);
              } catch (JSONException e) {
            	  	 Log.e("log_tag", "getting error");
            	  	e.printStackTrace();
              }
			
		}
		
	}
}

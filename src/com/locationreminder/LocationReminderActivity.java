package com.locationreminder;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.locationreminder.GeoService.MyLocationListener;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class LocationReminderActivity extends Activity implements OnClickListener
{
	EditText longitude,latitude,message,radius,address,title;
	Button cLocation,mLocation,save,clear;
	Spinner length;
	LocationManager locationManager;
	SharedPreferences sharedPreferences;
	HashMap<String, String> hMap;
	String[] meter=new String[] {"Mts","Kms"};
	String meterValue=meter[0];
	Location location;
	GoogleMapsData googleMapsData = new GoogleMapsData();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sharedPreferences=getSharedPreferences("LocationReminders",MODE_PRIVATE);
        longitude=(EditText)findViewById(R.id.longitude);
        latitude=(EditText)findViewById(R.id.latitude);
        message=(EditText)findViewById(R.id.message);
        radius=(EditText)findViewById(R.id.radius);
        address=(EditText)findViewById(R.id.address);
        title=(EditText)findViewById(R.id.title);
        cLocation=(Button)findViewById(R.id.currentLocation);
        mLocation=(Button)findViewById(R.id.mapLocation);
        save=(Button)findViewById(R.id.save);
        clear=(Button)findViewById(R.id.clear);
        length=(Spinner)findViewById(R.id.length);
        
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

	    location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	    if(location==null)
	    {
	    	location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    }
	    if(location==null)
	    {
	    	location=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
	    }
	    
        
        
        Intent intent=new Intent(LocationReminderActivity.this,GeoService.class);
        startService(intent);
        
        cLocation.setOnClickListener(this);
        mLocation.setOnClickListener(this);
        save.setOnClickListener(this);
        clear.setOnClickListener(this);
        
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,meter);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		length.setAdapter(adapter);		
		length.setSelection(adapter.getPosition(meterValue));
		length.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) 
			{
				meterValue=parent.getItemAtPosition(position).toString();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) 
			{
				
			}
		});
        
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode==RESULT_OK)
    	{
	    	String lon=data.getStringExtra("longitude");
	    	String lat=data.getStringExtra("latitude");
	    	String addr=data.getStringExtra("address");
	    	longitude.setText(lon);
	    	latitude.setText(lat);
	    	address.setText(addr); 
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.reminder_menu, menu);
    	return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.view:		
			Intent intent=new Intent(LocationReminderActivity.this,ViewReminders.class);
			startActivity(intent);
			break;
		case R.id.clear:
			
			break;

		default:
			break;
		}
		return true;
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.save:
				
				if(title.getText().toString().equals("") || longitude.getText().toString().equals("")
				  ||latitude.getText().toString().equals("") || message.getText().toString().equals("")
				  ||address.getText().toString().equals("") || radius.getText().toString().equals(""))
				{
					Toast.makeText(LocationReminderActivity.this,"Plz Fill All Details !",Toast.LENGTH_LONG).show();
				}
				else
				{
					SharedPreferences.Editor editor=sharedPreferences.edit();							
					hMap=new HashMap<String, String>();
					hMap.put("title", title.getText().toString());
					hMap.put("longitude", longitude.getText().toString());
					hMap.put("latitude",latitude.getText().toString());
					hMap.put("message",message.getText().toString());
					hMap.put("address", address.getText().toString());
					hMap.put("radius",radius.getText().toString());
					hMap.put("status","active");
					hMap.put("meter",meterValue);
					String data=HashmapConversion.HashmapToString(hMap);
					editor.putString(title.getText().toString(),data);
					editor.commit();
					Toast.makeText(LocationReminderActivity.this,"Reminder added successfully!",Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.currentLocation:
				if( location!=null ) {
					
					double lat=location.getLatitude();
			        double lon=location.getLongitude();

			        List<Address> addresses = googleMapsData.getFromLocation(lat,lon, 1);			
					String location="";

					if(addresses.size()>0)
					{	
							location+=addresses.get(0).getAddressLine(0);
						
					}     
			        address.setText(location);
			        latitude.setText(Double.toString(lat));
			        longitude.setText(Double.toString(lon));
			        
				} else {
					Toast.makeText(LocationReminderActivity.this, "Please Check Your Internet/GPS settings", Toast.LENGTH_LONG).show();
				}
				
		        
				break;
			case R.id.mapLocation:
				Intent intent=new Intent(LocationReminderActivity.this,GoogleMaps.class);
				startActivityForResult(intent, 1);
				break;
			case R.id.clear:
				longitude.setText("");
				latitude.setText("");
				message.setText("");
				radius.setText("");
				address.setText("");
				title.setText("");
				break;

		default:
			break;
		}
		
	}
	
	
			
}
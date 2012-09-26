package com.locationreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapView.LayoutParams;  


import android.view.View;
import android.widget.LinearLayout;

public class GoogleMaps extends MapActivity {
	MapController mapController;
	double latitude,longitude;
	MapView mapView;
	GeoPoint geoPoint,currentPoint;
	Location locationObj;
	String location="";
	GoogleMapsData googleMapsData = new GoogleMapsData();
	public MapOverlay mapOverlay;
	public static final String TAG = GoogleMaps.class.getSimpleName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);
        LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mapView=(MapView)findViewById(R.id.map);
	       mapView.setBuiltInZoomControls(true);
	       mapView.displayZoomControls(true);
	       mapView.setStreetView(true);
	       
	       LinearLayout zoomLayout=(LinearLayout)findViewById(R.id.zoomin);
	       View zoomView=mapView.getZoomControls();
	       
	       zoomLayout.addView(zoomView, 
	               new LinearLayout.LayoutParams(
	                   LayoutParams.WRAP_CONTENT, 
	                   LayoutParams.WRAP_CONTENT)); 
	    
	       mapController=mapView.getController();
       
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
		    latitude=locationObj.getLatitude();
		    longitude=locationObj.getLongitude();
		    currentPoint=new GeoPoint((int)(latitude*1E6),(int)(longitude*1E6));	       
		    mapController.animateTo(currentPoint);
		} else {
			Toast.makeText(GoogleMaps.this, "Please Check Your Internet/GPS settings", Toast.LENGTH_LONG).show();
		}
		
	        mapController.setZoom(17);	       
	        mapOverlay=new MapOverlay();
	        List<Overlay> listOverlays=mapView.getOverlays();
	        listOverlays.clear();
	        listOverlays.add(mapOverlay);
	        mapView.invalidate();
       
           
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	class MapOverlay extends com.google.android.maps.Overlay
	{
		
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,long when) 
		{
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow, when);
			
			Point screenPoint = new Point();			
			mapView.getProjection().toPixels(currentPoint, screenPoint);			
			Bitmap bitmapImage=BitmapFactory.decodeResource(getResources(), R.drawable.mappin1);
			canvas.drawBitmap(bitmapImage,screenPoint.x,screenPoint.y-32,null);
			return true;
						
		}
		
		@Override
		public boolean onTap(GeoPoint p, MapView mapView) 
		{
			// TODO Auto-generated method stub
			currentPoint=p;
			Geocoder geoCoder=new Geocoder(getBaseContext(),Locale.getDefault());
		      
			return true;
		}
		
		
		@Override
		public boolean onTouchEvent(MotionEvent e, MapView mapView) {
			// TODO Auto-generated method stub
			super.onTouchEvent(e, mapView);
			
//			if(e.getAction()==1)
//			{
//				currentPoint=mapView.getProjection().fromPixels((int)e.getX(), (int)e.getY());
//				Geocoder geoCoder=new Geocoder(getBaseContext(),Locale.getDefault());
//				List<Address> addresses = getFromLocation(
//			            currentPoint.getLatitudeE6()  / 1E6, 
//			            currentPoint.getLongitudeE6() / 1E6, 1);
//			
//				location="";
//				Log.d(TAG,"addresses"+addresses.size());
//				if(addresses.size()>0)
//				{	
//						location+=addresses.get(0).getAddressLine(0);
//					
//				}       
//				return true;
//			}
			
			return false;
		}
		
		
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
    	return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.pick:
			List<Address> addresses = googleMapsData.getFromLocation(
		            currentPoint.getLatitudeE6()  / 1E6, 
		            currentPoint.getLongitudeE6() / 1E6, 1);
		
			location="";
			Log.d(TAG,"addresses"+addresses.size());
			if(addresses.size()>0)
			{	
					location+=addresses.get(0).getAddressLine(0);
				
			} 
				
			Intent intent = new Intent();
			intent.putExtra("longitude",""+ currentPoint.getLongitudeE6()/1e6);
			intent.putExtra("latitude",""+ currentPoint.getLatitudeE6()/1e6);
			intent.putExtra("address", location);
			setResult(RESULT_OK, intent);
			finish(); 
			break;
		case R.id.details:
			List<Address> addresses1 = googleMapsData.getFromLocation(
		            currentPoint.getLatitudeE6()  / 1E6, 
		            currentPoint.getLongitudeE6() / 1E6, 1);
		
			location="";
			Log.d(TAG,"addresses"+addresses1.size());
			if(addresses1.size()>0)
			{	
					location+=addresses1.get(0).getAddressLine(0);
				
			}
			location=" "+location.replace(",",",\n");
			location+="\n\nLongitude: "+(currentPoint.getLongitudeE6()/1E6);
			location+="\nLatitude: "+(currentPoint.getLatitudeE6()/1E6);
			new AlertDialog.Builder(GoogleMaps.this)
		    .setTitle("About The Place")
		    .setMessage(location)
		    //.setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        }
		    }).show();
			break;
		default:
			break;
		}
		return true;
	}
}
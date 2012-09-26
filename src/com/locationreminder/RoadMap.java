package com.locationreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
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

public class RoadMap extends MapActivity {
	String start, end;
	int menu_status = 0;
	String radius;
	ArrayList<String> directions, distances, durations;
	String totalDistance, totalDuration;
	MapController mapController;
	double latitude, longitude;
	MapView mapView;
	GeoPoint source, destination;
	String location = "";
	public MyOverLay mapOverlay;
	ArrayList<GeoPoint> pointList, poly;
	Location locationObj;
	public static final String TAG = RoadMap.class.getSimpleName();
	String provider = null;
	MyLocationListener myLocationListener;
	LocationManager locationManager ;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mapView = (MapView) findViewById(R.id.map);
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);
		mapView.setStreetView(true);

		LinearLayout zoomLayout = (LinearLayout) findViewById(R.id.zoomin);
		View zoomView = mapView.getZoomControls();

		zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mapController = mapView.getController();

		String[] dest = getIntent().getStringArrayExtra("destination");
		latitude = Double.parseDouble(dest[0]);
		longitude = Double.parseDouble(dest[1]);
		radius = dest[2];
		destination = new GeoPoint((int) (latitude * 1E6),
				(int) (longitude * 1E6));
		end = latitude + "," + longitude;

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			provider = LocationManager.GPS_PROVIDER;
			locationObj = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} else if( locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null )  {
			provider = LocationManager.NETWORK_PROVIDER;
			locationObj = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} else if( locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null )  {
			provider = LocationManager.PASSIVE_PROVIDER;
			locationObj = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		
		
		if (locationObj != null) {
			latitude = locationObj.getLatitude();
			longitude = locationObj.getLongitude();
		} else {
			Toast.makeText(RoadMap.this,
					"Please Check Your Internet/GPS settings",
					Toast.LENGTH_LONG).show();
			this.finish();
		}
		
		start = latitude + "," + longitude;
		source = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
		mapController.animateTo(source);
		mapController.setZoom(17);

		drawRoute("driving");
		
		myLocationListener = new MyLocationListener();
		locationManager.requestLocationUpdates(provider, 0, 10, myLocationListener);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This class used to draw routes and positions on the map
	 */
	class MyOverLay extends com.google.android.maps.Overlay {
		private GeoPoint gp1;
		private GeoPoint gp2;
		private int mRadius = 6;
		private int mode = 0;
		private int defaultColor;
		private String text = "";
		private Bitmap img = null;
		private String radius = null;

		public MyOverLay(GeoPoint gp1, GeoPoint gp2, int mode) {
			this.gp1 = gp1;
			this.gp2 = gp2;
			this.mode = mode;

		}

		public MyOverLay(GeoPoint centre, String radius) {
			this.gp1 = centre;
			this.radius = radius;
			this.mode = 4;
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			Projection projection = mapView.getProjection();
			if (shadow == false) {
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				Point point = new Point();
				if (gp1 != null)
					projection.toPixels(gp1, point);
				if (mode == 1) {
					paint.setColor(Color.BLUE);
					RectF oval = new RectF(point.x - mRadius,
							point.y - mRadius, point.x + mRadius, point.y
									+ mRadius);
					canvas.drawOval(oval, paint);
				} else if (mode == 2) {
					paint.setColor(Color.parseColor("#006400"));
					Point point2 = new Point();
					if (gp2 != null)
						projection.toPixels(gp2, point2);
					paint.setStrokeWidth(5);
					paint.setAlpha(120);
					// canvas.drawLine(point.x, point.y, point2.x,point2.y,
					// paint);

					Point sample = new Point();
					float[] pts = new float[(poly.size() - 1) * 4];
					int count = 0;
					for (int i = 0; i < poly.size() - 1; i++) {

						projection.toPixels(poly.get(i), sample);
							pts[count++] = sample.x;
							pts[count++] = sample.y;
						projection.toPixels(poly.get(i + 1), sample);
							pts[count++] = sample.x;
							pts[count++] = sample.y;

					}
					Log.d("map", "drawing path");
					canvas.drawLines(pts, 0, pts.length, paint);

				} else if (mode == 3) {
					paint.setColor(Color.RED);
					Point point2 = new Point();
					projection.toPixels(gp2, point2);
					paint.setStrokeWidth(5);
					paint.setAlpha(120);
					canvas
							.drawLine(point.x, point.y, point2.x, point2.y,
									paint);
					RectF oval = new RectF(point2.x - mRadius, point2.y
							- mRadius, point2.x + mRadius, point2.y + mRadius);
					paint.setAlpha(255);
					canvas.drawOval(oval, paint);
				} else if (mode == 4) {
					float rad = projection.metersToEquatorPixels(Float
							.parseFloat(this.radius));
					Log.d("maps", "radius is: " + rad);
					Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
					circlePaint.setColor(0x30000000);
					circlePaint.setStyle(Style.FILL_AND_STROKE);
					canvas.drawCircle(point.x, point.y, rad, circlePaint);

					circlePaint.setColor(0x99000000);
					circlePaint.setStyle(Style.STROKE);
					canvas.drawCircle(point.x, point.y, rad, circlePaint);

				}
			}
			return super.draw(canvas, mapView, shadow, when);
		}

	}

	
	/**
	 * For downloading the route details using google API
	 */
	public class DrawRoadDirections extends
			AsyncTask<String, GeoPoint[], String[]> {

		@Override
		protected String[] doInBackground(String... params) {
			String polyline;
			poly = new ArrayList<GeoPoint>();

			directions = new ArrayList<String>();
			durations = new ArrayList<String>();
			distances = new ArrayList<String>();
			String travelType = params[0];
			GeoPoint[] geoPoint = new GeoPoint[2];
			GeoPoint gp1, gp2;
			String urlStr = "http://maps.google.com/maps/api/directions/json?origin="
					+ start
					+ "&destination="
					+ end
					+ "&mode="
					+ travelType
					+ "&sensor=false";
			Log.d("d&d", "url:" + urlStr);
			InputStream is = null;
			String result = "";
			JSONObject jArray = null;
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(urlStr);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();

			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
			}

			// convert response to string
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				is.close();
				result = sb.toString();
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
			}

			JSONArray responseArray = null;
			try {
				jArray = new JSONObject(result);
				responseArray = jArray.getJSONArray("routes").getJSONObject(0)
						.getJSONArray("legs");
				JSONObject legsobject = responseArray.getJSONObject(0);
				totalDistance = legsobject.getJSONObject("distance").getString(
						"text").toString();
				totalDuration = legsobject.getJSONObject("duration").getString(
						"text").toString();
				JSONArray steps = legsobject.getJSONArray("steps");
				JSONObject jobj = null;
				for (int i = 0; i < steps.length(); i++) {
					jobj = steps.getJSONObject(i).getJSONObject("distance");
					distances.add(jobj.getString("text"));
					jobj = steps.getJSONObject(i).getJSONObject("duration");
					durations.add(jobj.getString("text"));
					directions.add(steps.getJSONObject(i).getString(
							"html_instructions"));

					polyline = steps.getJSONObject(i).getJSONObject("polyline")
							.getString("points");

					decodePoly(polyline);
				}

			} catch (JSONException e) {
				Log.e("log_tag", "getting error");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(GeoPoint[]... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			GeoPoint[] geoPoint = values[0];
			// displayRoute(geoPoint);
			// mapView.getOverlays().add(new
			// MyOverLay(geoPoint[0],geoPoint[1],2));
		}

		@Override
		protected void onPostExecute(String[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mapController.animateTo(source);
			mapView.getOverlays().add(new MyOverLay(null, null, 2));
		}

	}

	
	
	/**
	 * Decodes the encoded poly strng into a geopoints and stores in an arraylist
	 * @param encoded encoded polyline strings provided by google API
	 */
	private void decodePoly(String encoded) {
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
					(int) (((double) lng / 1E5) * 1E6));
			poly.add(p);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.roadmap_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem mode = menu.findItem(R.id.mode);
		if (menu_status == 0) {
			mode.setTitle("Walking Route");
		} else {
			mode.setTitle("Driving Route");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.mode:
			if (menu_status == 0) {
				drawRoute("walking");
				menu_status = 1;
			} else {
				drawRoute("driving");
				menu_status = 0;
			}
			break;
		case R.id.directions:
			String message = "";
			String title;
			String direction = "";
			if (menu_status == 0)
				title = "Driving Directions";
			else
				title = "Walking Directions";
			message += "Total Distance: " + totalDistance + "\n";
			message += "Total Duration: " + totalDuration + "\n\n";
			for (int i = 0; i < distances.size(); i++) {

				direction = directions.get(i);
				direction = Html.fromHtml(direction).toString();
				Log.d("map", direction);
				direction = direction.replaceAll("(\\r|\\n|\\t)", " ");
				direction = direction.trim();
				message += "step " + (i + 1) + ": " + direction + "\n"
						+ "Distance: " + distances.get(i) + " Time: "
						+ durations.get(i) + "\n\n";

			}
			new AlertDialog.Builder(RoadMap.this).setTitle(title).setMessage(
					message).setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					}).show();
			break;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * used to draw path between source and destination
	 * @param routeType specifies the type of root (walking/driving etc)
	 */
	public void drawRoute( String routeType) {
		
		mapView.getOverlays().clear();
		mapView.getOverlays().add(new MyOverLay(source, source, 1));
		mapView.getOverlays().add(new MyOverLay(destination, destination, 3));
		mapView.getOverlays().add(new MyOverLay(destination, radius));
		DrawRoadDirections drawRoadDirections = new DrawRoadDirections();
		drawRoadDirections.execute(routeType);
		
	}

	
	/**
	 * This class notifies the location changes 
	 */
	public class MyLocationListener implements LocationListener
    {

		@Override
		public void onLocationChanged(Location location) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			start = latitude + "," + longitude;
			source = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
			if(menu_status == 0) 
				drawRoute("walking");
			else
				drawRoute("driving");	
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		
    }
	
	@Override
	protected void onDestroy() {
		if( myLocationListener!=null )
			locationManager.removeUpdates(myLocationListener);
		
		this.finish();
		
		super.onDestroy();
	}


}
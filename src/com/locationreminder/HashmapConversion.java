package com.locationreminder;


import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class HashmapConversion {
	
	private static final String TAG = HashmapConversion.class.getSimpleName();

	/**
	 * Covert hashmap to string
	 * @param hMap hashmap
	 * @return
	 */
	public static String HashmapToString(HashMap<String, String> hMap)
	{
		ArrayList<String> keyList=new ArrayList<String>(hMap.keySet());
		ArrayList<String> valueList=new ArrayList<String>(hMap.values());
		String str=new String();
		str="";
		for(int i=0;i<keyList.size();i++)
		{
			str+=keyList.get(i);
			str+="=";
			str+=valueList.get(i);
			if(i!=keyList.size()-1)
				str+="&";
		}
		Log.d(TAG, str);
		return str;		
	}
	/**
	 * Coverts Hashmap to string
	 * @param str string
	 * @return
	 */
	public static HashMap<String, String> StringToHashmap(String str)
	{
		 Log.d(TAG, str);
		 HashMap<String, String> hMap=new HashMap<String, String>();		
		 String[] keyValuePairs=str.split("&");		
		 for (String keyValuePair : keyValuePairs) 
		 {  
			 String[] keyValue = keyValuePair.split("=");  	
			 hMap.put(keyValue[0],keyValue[1]);				   		     
		}  
		return hMap;		
	}

}

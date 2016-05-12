package com.sanyeshu.mylocation3;

import java.util.Calendar;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class MainServer extends Service implements Runnable{
	
	public static String phone = "";
	private String str = "";
	
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = null;
	
	@Override
	public void onDestroy() {
		if(mLocationClient!= null)
		{
			mLocationClient.unRegisterLocationListener(myListener);
		}
		stopLocation();
		super.onDestroy();
	}
	
	private void stopLocation() {
		if (mLocationClient!=null)
		{
			mLocationClient.stop();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		try{
			SharedPreferences sp = getSharedPreferences("location", Activity.MODE_PRIVATE); 
			phone = sp.getString("phone", "");
			location();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void location() {
		if (mLocationClient == null)
		{
			 mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
			 initLocation();
			 myListener = new MyLocationListener();
			 mLocationClient.registerLocationListener( myListener );    //注册监听函数
		}
		 mLocationClient.start();
		 mLocationClient.requestLocation();
	}
	
	private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死  
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
	
	public class MyLocationListener implements BDLocationListener {
		 
        @Override
        public void onReceiveLocation(BDLocation location) {
        	
        	try{
        		JSONObject jo =new JSONObject();
        		jo.putOpt("location", location.getAddrStr());
        		jo.putOpt("lat", location.getLatitude());
        		jo.putOpt("lng", location.getLongitude());
        		jo.putOpt("type", location.getLocType());
        		str = jo.toString();
        		Calendar calendar = Calendar.getInstance();
        		String res = ""+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + location.getAddrStr() + location.getLocType();
        		if(MainActivity.gHandler!=null)
                {
                	Message msg = new Message();
                	msg.what = MainActivity.UPDATE_TEXT;
                	msg.obj = res;
                	MainActivity.gHandler.sendMessage(msg);
                }
    			new Thread(MainServer.this).start();
    			
    			stopLocation();
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        }
	}

	@Override
	public void run() {
		 HttpRequest.get("http://122.114.50.50:9999/myLocation2",true,"str",str,"phone",phone).connectTimeout(60000).code();
//		 HttpRequest.get("http://192.168.0.254:8080/myLocation2",true,"str",str,"phone",phone).connectTimeout(60000).code();
	}

}

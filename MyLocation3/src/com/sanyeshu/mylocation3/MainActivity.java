package com.sanyeshu.mylocation3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	public static final int UPDATE_TEXT = 0;
	private EditText mTextViewPhone;
	private Button mButtonOk;
	private TextView mTextViewStr;
	public static Handler gHandler;
	private PendingIntent sender = null;
	private AlarmManager am = null;
	public static final int UPDATE_TIME = 300000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}
	
	public void init()
	{
		SharedPreferences sp = getSharedPreferences("location", Activity.MODE_PRIVATE); 
		String tStrPhone = sp.getString("phone", "");
		
		mTextViewPhone = (EditText) findViewById(R.id.phone);
		mTextViewStr = (TextView) findViewById(R.id.textView_str);
		mButtonOk = (Button) findViewById(R.id.ok);
		mTextViewPhone.setText(tStrPhone);
		mButtonOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				SharedPreferences sp = getSharedPreferences("location", Activity.MODE_PRIVATE); 
				Editor editor = sp.edit(); 
				
				String phone = mTextViewPhone.getText().toString().trim();
				editor.putString("phone", phone);
				editor.commit();
				
//				Intent tIntent = new Intent("com.sanyeshu.mylocation3.MyService");
//				startService(tIntent);
				setAlarm(MainActivity.this);
				
				mButtonOk.setEnabled(false);
			}
		});
		gHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_TEXT:
					if(null!=mTextViewStr)
					{
						mTextViewStr.setText(msg.obj.toString());
						mButtonOk.setEnabled(true);
					}
					break;

				default:
					break;
				}
			}
		};


	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent Intent1 = new Intent("com.sanyeshu.mylocation3.MyService");
			stopService(Intent1);
			unSetAlarm(this);
			finish();
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void setAlarm(Context context)
	{
	     Intent intent = new Intent("com.sanyeshu.mylocation3.MyService");
         sender = PendingIntent.getService(context, 0, intent, 0);

         am = (AlarmManager) getSystemService(ALARM_SERVICE);
         am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), UPDATE_TIME, sender);
	}
	
	private void unSetAlarm(Context context)
	{
		if(am!=null&&sender!=null)
		{
			am.cancel(sender);
		}
		else
		{
			Intent intent = new Intent("com.sanyeshu.mylocation3.MyService");
			sender = PendingIntent.getService(context, 0, intent, 0);
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.cancel(sender);
		}
		sender = null;
		am = null;
	}
}

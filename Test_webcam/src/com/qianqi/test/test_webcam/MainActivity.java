package com.qianqi.test.test_webcam;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements PreviewCallback{
	
	public static String phone = "";
	Camera camera;
	public static final String SAVE_IMAGE_PATH = getDcimDirectory("webcam_save").getPath() + File.separator;
	Thread t =null;
//	public List<String> pickList=new ArrayList<String>();
	public static boolean ifFirst = false;
	public boolean ifpickup = false;

	public static SurfaceView mySurfaceView;
	public static SurfaceHolder myHolder;
	public static Context context;
	public static Handler handler;
	
	public static int index = 0;
	public static String img = "";
	
	EditText textPhone;
	Button b1;
	Button b2;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		handler = new Handler(){
			public void handleMessage(Message msg) {   
	               switch (msg.what) {   
	                    case 1:  
	                    	Toast.makeText(MainActivity.context, "启动成功,为确保使用请不要退出 此页面", Toast.LENGTH_SHORT).show();
	                    	break;
	                    case 2:  
	                    	Toast.makeText(MainActivity.context, "网络断开，请稍等再启动...", Toast.LENGTH_SHORT).show();
	                         break;   
	                    case 3:
	                    	b1.setEnabled(true);
	                    	break;
	               }   
	               super.handleMessage(msg);   
	          } 
		};
		
		textPhone = (EditText)findViewById(R.id.editText1);
		
		SharedPreferences sp = getSharedPreferences("webcam", Activity.MODE_PRIVATE); 
		textPhone.setText(sp.getString("phone", ""));
		
		b1 = (Button)findViewById(R.id.button1);
		b1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ifFirst = true;
				
				SharedPreferences sp = getSharedPreferences("webcam", Activity.MODE_PRIVATE); 
				Editor editor = sp.edit(); 
				
				phone = textPhone.getText().toString().trim();
				editor.putString("phone", phone);
				editor.commit();
				
				
				pickup();
				
				b1.setEnabled(false);
				b2.setEnabled(true);
			}
		});
		
		b2 = (Button)findViewById(R.id.button2);
			b2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				unpickup();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		
		mySurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
		myHolder = mySurfaceView.getHolder();  
		b2.setEnabled(false);
	}

	public static File getDcimDirectory(String dirName) {
		if (dirName == null) {
			return null;
		}

		File dcimDirectory = null;
		String dcimPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM).getPath();
		dcimDirectory = new File(dcimPath, dirName);
		if (!dcimDirectory.exists()) {
			dcimDirectory.mkdirs();
		}

		return dcimDirectory;
	}
	
	
	public void startt()
	{
		ifpickup = false;
		
		t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				Message msg = new Message();
				msg.what = 3;
				MainActivity.handler.sendMessage(msg);
			}
		});
		
		t.start();
	}
	
//	public static Bitmap rotate(Bitmap b, int degrees) {
//        if (degrees != 0 && b != null) {
//            Matrix m = new Matrix();
//            m.setRotate(degrees,
//                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
//            try {
//                Bitmap b2 = Bitmap.createBitmap(
//                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
//                if (b != b2) {
//                    b.recycle(); 
//                    b = b2;
//                }
//            } catch (OutOfMemoryError ex) {
//            }
//        }
//        return b;
//    }
	
//	private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) 
//	{
//	    byte [] yuv = new byte[imageWidth*imageHeight*3/2];
//	    // Rotate the Y luma
//	    int i = 0;
//	    for(int x = 0;x < imageWidth;x++)
//	    {
//	        for(int y = imageHeight-1;y >= 0;y--)                               
//	        {
//	            yuv[i] = data[y*imageWidth+x];
//	            i++;
//	        }
//	    }
//	    // Rotate the U and V color components 
//	    i = imageWidth*imageHeight*3/2-1;
//	    for(int x = imageWidth-1;x > 0;x=x-2)
//	    {
//	        for(int y = 0;y < imageHeight/2;y++)                                
//	        {
//	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
//	            i--;
//	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
//	            i--;
//	        }
//	    }
//	    return yuv;
//	}
	
	public static void saveFile(Bitmap bm, String fileName) {   
        File dirFile = new File(SAVE_IMAGE_PATH);   
        if(!dirFile.exists()){   
            dirFile.mkdir();   
        }   
        File myCaptureFile = new File(SAVE_IMAGE_PATH + fileName);   
        BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
	        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);   
	        bos.flush();   
	        bos.close();   
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}   

    }
	
	public void pickup()
	{
		if(ifpickup)
		{
			return;
		}
		if(camera!=null)
		{
			camera.stopPreview();
			camera.release();
		}
        camera = null;
		
		camera=Camera.open();
		
        try { 
        	
            Camera.Parameters param = camera.getParameters(); 
            param.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            param.setFlashMode(Parameters.FLASH_MODE_AUTO);
            param.setColorEffect(Camera.Parameters.EFFECT_NONE); 

            param.setPreviewSize(320, 240);
            camera.setParameters(param); 
            setCameraDisplayOrientation(this,0,camera);
            camera.setPreviewDisplay(MainActivity.myHolder); 
            camera.setPreviewCallback(this);
            
    		camera.startPreview(); 
             
        } catch (Exception e) { 
            camera.release(); 
        } 
        
        ifpickup = true;
        
        Thread tr = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(ifpickup)
				{
					if(!img.equals(""))
					{
//						String img = pickList.get(0);
						try {
							String res = HttpRequest.get("http://211.149.172.120:9999/webcam",true,"phone",phone,"other","0").connectTimeout(12000).body();
//							String res = HttpRequest.get("http://192.168.0.254:8080/webcam",true,"phone",phone,"other","0").connectTimeout(12000).body();
							if (res == null || res.equals(""))
							{
								res = "0";
							}
							index = Integer.parseInt(res);
							if (index > 0)
							{
								index = index - 1 ;
								HttpRequest.get("http://211.149.172.120:9999/webcam",true,"phone",phone,"img",img,"other",""+index).connectTimeout(12000).code();
//								HttpRequest.get("http://192.168.0.254:8080/webcam",true,"phone",phone,"img",img,"other",""+index).connectTimeout(12000).code();
								
							}
							if(ifFirst)
							{
								Message msg = new Message();
								msg.what = 1;
								MainActivity.handler.sendMessage(msg);
								ifFirst = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							Message msg = new Message();
							msg.what = 2;
							MainActivity.handler.sendMessage(msg);
						}
//						pickList.remove(0);
					}
					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(camera!=null)
				{
					camera.stopPreview();
					camera.release();
				}
		        camera = null;
		        
		        Message msg = new Message();
				msg.what = 3;
				MainActivity.handler.sendMessage(msg);
			}
			
		});
        tr.start();
	
	}
	
	public void unpickup()
	{
		if(ifpickup)
		{
			ifpickup = false;
		}
	}
	
	 private Bitmap rotateBitmap(YuvImage yuvImage, int orientation, Rect rectangle)
	    {
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    yuvImage.compressToJpeg(rectangle, 100, os);

	    Matrix matrix = new Matrix();
	    matrix.postRotate(orientation);
	    byte[] bytes = os.toByteArray();
	    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	    return Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	    }
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (!ifpickup||null==camera)
		{
			return;
		}
//		if(pickList.size()>=100)
//		{
//			pickList.remove(0);
//		}
		
        YuvImage yuvimage = new YuvImage(data,ImageFormat.NV21,camera.getParameters().getPreviewSize().width,camera.getParameters().getPreviewSize().height,null);
        Bitmap btm =rotateBitmap(yuvimage,90,new Rect(0,0,camera.getParameters().getPreviewSize().width,camera.getParameters().getPreviewSize().height));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        btm.compress(Bitmap.CompressFormat.JPEG, 30, baos);

        img = Base64Encoder.encode(baos.toByteArray());
//        pickList.add(str);
        
	}
	
	public static int getDisplayRotation(Activity activity) {  
	    int rotation = activity.getWindowManager().getDefaultDisplay()  
	       .getRotation();  
	    switch (rotation) {  
	        case Surface.ROTATION_0: return 0;  
	        case Surface.ROTATION_90: return 90;  
	        case Surface.ROTATION_180: return 180;  
	        case Surface.ROTATION_270: return 270;  
	    }  
	    return 0;  
	}  
	
	@SuppressLint("NewApi")
	public static void setCameraDisplayOrientation(Activity activity,  
	        int cameraId, Camera camera) {  
	    // See android.hardware.Camera.setCameraDisplayOrientation for  
	    // documentation.  
	    Camera.CameraInfo info = new Camera.CameraInfo();  
	    Camera.getCameraInfo(cameraId, info);  
	    int degrees = getDisplayRotation(activity);  
	    int result;  
	    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {  
	        result = (info.orientation + degrees) % 360;  
	        result = (360 - result) % 360;  // compensate the mirror  
	    } else {  // back-facing  
	        result = (info.orientation - degrees + 360) % 360;  
	    }  
	    camera.setDisplayOrientation(result);  
	}  

}

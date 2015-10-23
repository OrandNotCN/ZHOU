package com.zhou.flashlight;


import com.zhou.tracing.R;
import com.zhou.tracing.core.BaiduLoacation;
import com.zhou.tracing.core.LocationService;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlashLightActivity extends Activity {
	
	
	
	private static boolean isOn=false;
	private static Camera camera=null;
	private static Parameters parameters=null;
	private RelativeLayout light_bg_layout;
	private TextView lightButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,      
                WindowManager.LayoutParams. FLAG_FULLSCREEN);  
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.flash_light);
        initView();
    	initTrace1();
    }
    
    
    
    private void initTrace1() {
		// TODO Auto-generated method stub
    	Intent bindIntent = new Intent(this, LocationService.class);
		startService(bindIntent);
	}



	private void initTrace() {
		// TODO Auto-generated method stub
    	if(BaiduLoacation.isStart()){
    		return ;
    	}else{
    		new BaiduLoacation(this).start();
    	}
	}



	private void initView() {
		// TODO Auto-generated method stub

        lightButton = (TextView)findViewById(R.id.light_button);
        lightButton.getLayoutParams().height = getResources().getDisplayMetrics().heightPixels/3;
        lightButton.getLayoutParams().width = getResources().getDisplayMetrics().widthPixels/3;
        lightButton.setOnClickListener(click_on_off);
        
        light_bg_layout=(RelativeLayout)findViewById(R.id.flash_light_layout);
	}



	@SuppressWarnings("deprecation")
	public void changeBackground(boolean isOn) {
		// TODO Auto-generated method stub
		if(isOn){
			light_bg_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.light_on));
		}else{
			light_bg_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.light_off));
			
		}
	}
	
	private  OnClickListener click_on_off = new OnClickListener() {

		public void onClick(View arg0) {
			
			
			if(isOn==false){
				camera = Camera.open();   
				parameters = camera.getParameters();
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
				camera.setParameters(parameters);
				camera.startPreview(); 
				isOn=true;
				changeBackground(isOn);
				
			}else{
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				camera.release();
				isOn=false;
				changeBackground(isOn);
			}
		}
	};
	
	


	private long exitTime=0;// �˳�ʱ��
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO �����η��ؼ��˳�Ӧ�ó���
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
					// �жϼ��ʱ�� ����2����˳�Ӧ��
					if ((System.currentTimeMillis() - exitTime) > 2000) {
						// Ӧ����
						String applicationName = getResources().getString(
								R.string.app_name);
						String msg = "�ٰ�һ�η��ؼ��˳�" + applicationName;
						//String msg1 = "�ٰ�һ�η��ؼ��ص�����";
						Toast.makeText(FlashLightActivity.this, msg, 0).show();
						// �������η��ؼ����µ�ʱ���
						exitTime = System.currentTimeMillis();
					} else {
						doubleBack();
					}
					return true;
				}
				return super.onKeyDown(keyCode, event);
	}
    
    
    public void doubleBack(){ //�رճ���
		if(isOn){//���عر�ʱ
			FlashLightActivity.this.finish();
			android.os.Process.killProcess(android.os.Process.myPid());//�رս���
		}else if(!isOn){//���ش�ʱ
			
			if(camera!=null){
				camera.release();
			}
			
			FlashLightActivity.this.finish();
			android.os.Process.killProcess(android.os.Process.myPid());//�رս���
			isOn = false;//���⣬�򿪿��غ��˳������ٴν��벻�򿪿���ֱ���˳�ʱ���������
			
		}
    }
    
}
package com.zhou.tracing.core;


import android.content.Context;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class BaiduLoacation {
	private static LocationClient mLocationClient;
	private String tempcoor="gcj02";
	private LocationMode tempMode = LocationMode.Battery_Saving;
	private  final static int TIME_INTERVAL = 30 ; //��ʮ���ӻ�ȡһ�ε���λ��
	
	public BaiduLoacation(Context context){

		mLocationClient = ((LocationApplication)context.getApplicationContext()).mLocationClient;
		InitLocation();
	}
	
	public void start(){
		Log.e("BaiduLoacation","start()");
		mLocationClient.start();
	}
	
	public static boolean isStart(){
		if(mLocationClient != null){
			return mLocationClient.isStarted();
		}
		return false;
	}

	private void InitLocation() {
		// TODO Auto-generated method stub
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);//���ö�λģʽ
		option.setCoorType(tempcoor);//���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
        option.setScanSpan(TIME_INTERVAL*1000);//��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
        option.setIsNeedAddress(true);//��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
        option.setOpenGps(true);//��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
        option.setLocationNotify(true);//��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
        option.setIgnoreKillProcess(false);//��ѡ��Ĭ��false����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ��ɱ��
        option.SetIgnoreCacheException(false);//��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
		mLocationClient.setLocOption(option);
		
	}
	

}

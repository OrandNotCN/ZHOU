package com.zhou.tracing.core;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * ��������Service
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("location", "����Ӧ��");
//		new BaiduLoacation(context).start();
//		//�˴�����Ҫ���κβ���������������ʱ��Ӧ�û���ܹ㲥�����Զ�����Applicationʵʩ��Service  �����Ժ����������ܵĴ���
		Intent bindIntent = new Intent(context, LocationService.class);
		context.startService(bindIntent);
	}

}

package com.p300sdk.demo;

import java.util.Arrays;

import com.p300sdk.demo.fragment.DeviceFragment;
import com.p300sdk.demo.fragment.HistoryDataFragment;
import com.p300sdk.demo.fragment.RealtimeDataFragment;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.p300_2.P300Helper;
import com.sleepace.sdk.util.SdkLog;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends BaseActivity {
	
	private RadioGroup rgTab;
	private FragmentManager fragmentMgr;
	private Fragment deviceFragment, realTimeDataFragment, historyDataFragment;
	private P300Helper mHelper;
	private ProgressDialog upgradeDialog;
	
	//缓存数据
	public static String deviceName, deviceId, mac, power, version;
	public static boolean realtimeDataOpen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHelper = P300Helper.getInstance(this);
		findView();
		initListener();
		initUI();
		checkPermission();
	}
	
	private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
	private boolean checkPermission(){
	    boolean grant = true;
        if(Build.VERSION.SDK_INT>=23){
            //判断是否有权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                grant = false;
                SdkLog.log(TAG+" checkPermission not grant-------------");
                //请求权限
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                //向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    SdkLog.log(TAG+" checkPermission tips-------------");
                }
            }else{
                SdkLog.log(TAG+" checkPermission ok-------------");
            }
        }else{
            SdkLog.log(TAG+" checkPermission low system-------------");
        }
        return grant;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
    	super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SdkLog.log(TAG+" onRequestPermissionsResult permissions:" + Arrays.toString(permissions)+",grantResults:"+ Arrays.toString(grantResults));
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (permissions.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户同意使用该权限
            } else {
                finish();
            }
        }
    }
	
	@Override
	protected void findView() {
		// TODO Auto-generated method stub
		super.findView();
		rgTab = (RadioGroup) findViewById(R.id.rg_tab);
	}


	@Override
	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		mHelper.addConnectionStateCallback(stateCallback);
		rgTab.setOnCheckedChangeListener(checkedChangeListener);
	}


	@Override
	protected void initUI() {
		// TODO Auto-generated method stub
		super.initUI();
		fragmentMgr = getFragmentManager();
		deviceFragment = new DeviceFragment();
		realTimeDataFragment = new RealtimeDataFragment();
		historyDataFragment = new HistoryDataFragment();
		
		rgTab.check(R.id.rb_device);
//		rgTab.check(R.id.rb_control);
//		rgTab.check(R.id.rb_data);
		ivBack.setVisibility(View.GONE);
		
		upgradeDialog = new ProgressDialog(this);
		upgradeDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
		upgradeDialog.setMax(100);
		upgradeDialog.setIcon(android.R.drawable.ic_dialog_info);
		upgradeDialog.setTitle(R.string.fireware_update);
		upgradeDialog.setMessage(getString(R.string.upgrading));
		upgradeDialog.setCancelable(false);
		upgradeDialog.setCanceledOnTouchOutside(false);
	}
	
	
	public void setTitle(int res){
		tvTitle.setText(res);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == ivBack){
			exit();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public void exit(){
		mHelper.release();
		clearCache();
//		Intent intent = new Intent(this, SplashActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
		finish();
	}
	
	
	private void clearCache(){
		deviceName = null;
		deviceId = null;
		power = null;
		mac = null;
		version = null;
	}
	
	private IConnectionStateCallback stateCallback = new IConnectionStateCallback() {
		@Override
		public void onStateChanged(IDeviceManager manager, CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onStateChanged state:" + state);
			if(state == CONNECTION_STATE.DISCONNECT){
				
			}
		}
	};
	
	
	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			FragmentTransaction trans = fragmentMgr.beginTransaction();
			if(checkedId == R.id.rb_device){
				trans.replace(R.id.content, deviceFragment);
			}else if(checkedId == R.id.rb_control){
				trans.replace(R.id.content, realTimeDataFragment);
			}else if(checkedId == R.id.rb_data){
				trans.replace(R.id.content, historyDataFragment);
			}
			trans.commit();
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			
		}
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHelper.removeConnectionStateCallback(stateCallback);
	}
	
	public void showUpgradeDialog(){
		upgradeDialog.show();
	}
	
	public void setUpgradeProgress(int progress){
		upgradeDialog.setProgress(progress);
	}
	
	public void hideUpgradeDialog(){
		upgradeDialog.dismiss();
	}
	
}









































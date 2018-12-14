package com.p300sdk.demo.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import com.p300sdk.demo.DemoApp;
import com.p300sdk.demo.MainActivity;
import com.p300sdk.demo.R;
import com.p300sdk.demo.ReportActivity;
import com.p300sdk.demo.util.ActivityUtil;
import com.sleepace.sdk.constant.StatusCode;
import com.sleepace.sdk.interfs.IMonitorManager;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.p300_2.domain.DeviceInfo;
import com.sleepace.sdk.p300_2.domain.HistoryData;
import com.sleepace.sdk.p300_2.domain.RealTimeData;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.util.StringUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RealtimeDataFragment extends BaseFragment {
	private View vLast24HourData;
	private Button btnRealtimeData;
	private TextView tvSleepStatus, tvHeartRate, tvBreathRate;
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_control, null);
		// SdkLog.log(TAG+" onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		vLast24HourData = root.findViewById(R.id.last_24_hour_data);
		btnRealtimeData = (Button) root.findViewById(R.id.btn_realtime_data);
		tvSleepStatus = (TextView) root.findViewById(R.id.tv_sleep_status);
		tvHeartRate = (TextView) root.findViewById(R.id.tv_heartrate);
		tvBreathRate = (TextView) root.findViewById(R.id.tv_breathrate);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		vLast24HourData.setOnClickListener(this);
		btnRealtimeData.setOnClickListener(this);
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.time_data);
		progressDialog = new ProgressDialog(mActivity);
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SdkLog.log(TAG + " onResume connect:" + getP300Helper().isConnected());
		initBtnState();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		SdkLog.log(TAG + " onDestroyView----------------");
	}

	private void initBtnState() {
		if (MainActivity.realtimeDataOpen) {
			btnRealtimeData.setTag("close");
			btnRealtimeData.setText(R.string.end_real_data);
		} else {
			btnRealtimeData.setTag("open");
			btnRealtimeData.setText(R.string.obtain_24data);
		}
	}

	private void initRealtimeDataView(RealTimeData data) {
		if (data == null || data.getSleepState() == 0) {
			tvSleepStatus.setText("--");
			tvHeartRate.setText("--");
			tvBreathRate.setText("--");
		} else {
			if (data.getSleepState() == 0) {
				tvSleepStatus.setText(R.string.left_bed);
				tvHeartRate.setText("--");
				tvBreathRate.setText("--");
			} else {
				tvSleepStatus.setText(R.string.in_bed);
				tvHeartRate.setText(data.getHeartRate() + getString(R.string.beats_min));
				tvBreathRate.setText(data.getBreathRate() + getString(R.string.times_min));
			}
		}
	}

	private static HistoryData historyData;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if (v == vLast24HourData) {
			if (!getP300Helper().isConnected()) {
				if(DemoApp.USER_ID > 0 && !TextUtils.isEmpty(MainActivity.deviceName) && !TextUtils.isEmpty(MainActivity.mac)) {
					progressDialog.setTitle("");
					progressDialog.setMessage(getString(R.string.connect_device));
					progressDialog.show();
					int timestamp = (int) (System.currentTimeMillis() / 1000 & 0xFFFFFFFF);
					TimeZone tz = TimeZone.getDefault();
					int timezone = tz.getRawOffset() / 1000;
					Calendar calendar = Calendar.getInstance();
					int dstOffset = calendar.get(Calendar.DST_OFFSET) / 1000;
					getP300Helper().login(MainActivity.deviceName, MainActivity.mac, DemoApp.USER_ID, timestamp, timezone, dstOffset, 20000, loginCallback);
				}else {
					Toast.makeText(mActivity, R.string.tips_connect_device, Toast.LENGTH_SHORT).show();
				}
				return;
			}

			// if(historyData == null) {
			Calendar cal = Calendar.getInstance();
			// cal.set(Calendar.MINUTE, 0);
			// cal.set(Calendar.SECOND, 0);
			int endTime = (int) (cal.getTimeInMillis() / 1000);
			SdkLog.log(TAG + " getLast24HourData date:" + StringUtil.DATE_FORMAT.format(cal.getTime()));
			progressDialog.setTitle(R.string.data24);
			progressDialog.setMessage(getString(R.string.syncing));
			progressDialog.show();
			getP300Helper().getLast24HourData(endTime, DemoApp.USER_SEX, new IResultCallback<HistoryData>() {
				@Override
				public void onResultCallback(final CallbackData<HistoryData> cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG + " getLast24HourData " + cd);
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (isAdded()) {
								progressDialog.dismiss();
								if (cd.isSuccess()) {
									historyData = cd.getResult();
									Intent intent = new Intent(mActivity, ReportActivity.class);
									intent.putExtra(ReportActivity.EXTRA_REPORT_TYPE, ReportActivity.REPORT_TYPE_24);
									intent.putExtra(ReportActivity.EXTRA_DATA, historyData);
									startActivity(intent);
								} else {
									Toast.makeText(mActivity, R.string.sync_falied, Toast.LENGTH_SHORT).show();
								}
							}
						}
					});
				}
			});
			
			// } else {
			// Intent intent = new Intent(mActivity, ReportActivity.class);
			// intent.putExtra(ReportActivity.EXTRA_REPORT_TYPE,
			// ReportActivity.REPORT_TYPE_24);
			// intent.putExtra(ReportActivity.EXTRA_DATA, historyData);
			// startActivity(intent);
			// }
		} else if (v == btnRealtimeData) {
			if(DemoApp.USER_ID > 0 && !TextUtils.isEmpty(MainActivity.deviceName) && !TextUtils.isEmpty(MainActivity.mac)) {
				Object tag = v.getTag();
				if (tag == null || "open".equals(tag)) {
					getP300Helper().startRealTimeData(3000, realtimeCallback);
				} else {
					getP300Helper().stopRealTimeData(3000, realtimeCallback);
				}
			}else {
				Toast.makeText(mActivity, R.string.tips_connect_device, Toast.LENGTH_SHORT).show();
			}
		}
	}

	
	private IResultCallback<DeviceInfo> loginCallback = new IResultCallback<DeviceInfo>() {
		@Override
		public void onResultCallback(final CallbackData<DeviceInfo> cd) {
			// TODO Auto-generated method stub
			if(!ActivityUtil.isActivityAlive(mActivity)) {
				return;
			}
			SdkLog.log(TAG+" loginCallback " + cd);
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();
					if(cd.isSuccess()) {
//						MainActivity.realtimeDataOpen = false;
//						initBtnState();
//						initRealtimeDataView(null);
						vLast24HourData.performClick();
					} else {
						if(cd.getStatus() == StatusCode.NOT_ENABLE) {
							Toast.makeText(mActivity, R.string.phone_bluetooth_not_open, Toast.LENGTH_SHORT).show();
						}else {
							new AlertDialog.Builder(mActivity)
							.setTitle(R.string.device_connect_fail)
							.setMessage(R.string.device_w_ble_connect_failed_tip)
							.setPositiveButton(android.R.string.ok, null).create().show();
						}
					}
				}
			});
		}
	};
	
	private IResultCallback realtimeCallback = new IResultCallback() {
		@Override
		public void onResultCallback(final CallbackData cd) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG + " realtimeCB cd:" + cd + ",isAdd:" + isAdded());
			if (!isAdded()) {
				return;
			}

			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (cd.getCallbackType() == IMonitorManager.METHOD_REALTIME_DATA_OPEN) {
						MainActivity.realtimeDataOpen = true;
						initBtnState();
					} else if (cd.getCallbackType() == IMonitorManager.METHOD_REALTIME_DATA) {// 实时数据
						RealTimeData realTimeData = (RealTimeData) cd.getResult();
						initRealtimeDataView(realTimeData);
					} else if (cd.getCallbackType() == IMonitorManager.METHOD_REALTIME_DATA_CLOSE) {
						MainActivity.realtimeDataOpen = false;
						initBtnState();
						initRealtimeDataView(null);
					}
				}
			});
		}
	};
}

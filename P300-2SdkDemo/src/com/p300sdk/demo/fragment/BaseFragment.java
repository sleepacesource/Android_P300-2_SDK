package com.p300sdk.demo.fragment;

import com.p300sdk.demo.MainActivity;
import com.sleepace.sdk.p300_2.P300Helper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment implements OnClickListener{
	
	protected String TAG = getClass().getSimpleName();
	protected MainActivity mActivity;
	private P300Helper mHelper;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity) getActivity();
		mHelper = P300Helper.getInstance(mActivity);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	public P300Helper getP300Helper() {
		return mHelper;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
	}


	protected void initListener() {
		// TODO Auto-generated method stub
	}

	protected void initUI() {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}




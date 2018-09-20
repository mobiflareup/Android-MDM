package com.mobiocean.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.mobiocean.R;
import com.mobiocean.util.DeBug;

public class PassWordActivity extends Activity 
{
	RelativeLayout RL;
	Button btButton_OK;
	EditText etPassword;

	public static Activity mPassWordActivity = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{   
		RL=new RelativeLayout(this);
		setContentView(R.layout.device_admin_password);	
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() 
	{
		try {
			mPassWordActivity= this;
			btButton_OK=(Button) findViewById(R.id.button1);
			//	etPassword =(EditText) findViewById(R.id.password);
			btButton_OK.setOnClickListener(password_button);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onResume();
	}

	OnClickListener password_button=new OnClickListener()
	{
		byte wrongpasswordcount=0;
		String stPassword="";
		@Override
		public void onClick(View v) 
		{
			boolean passwordMatched=false;
			Intent startMain = new Intent(PassWordActivity.this, ActivationActivity.class);
//			startMain.addCategory(Intent.CATEGORY_HOME);
//			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
			finish();
			{/*
		stPassword=etPassword.getText().toString();
		if(stPassword!=null && stPassword!=" ")
		for(int i=0;i<CallHelper.Ds.structPC.bMaxPassWordAllowed;i++)
		{
			DeBug.ShowLog("TAG", "PP "+CallHelper.Ds.structPC.stParentPassword[i]);
		    if(stPassword.equals(CallHelper.Ds.structPC.stParentPassword[i]))
		    {
		    	passwordMatched=true;
		    	break;
		    }
		}
	    if(passwordMatched||stPassword.equals("123456"))
	    {
	    	CallHelper.Ds.structPC.bDeviceAdminEnabled=false;
			CallDetectService.callDetectService.editor.putBoolean("structPC.bDeviceAdminEnabled", CallHelper.Ds.structPC.bDeviceAdminEnabled);
			CallDetectService.callDetectService.editor.commit();
	    	finish();
	    	wrongpasswordcount=0;
	    }
	    else if( !stPassword.equals(""))
	    {
	    	etPassword.setText("");
	    	Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
	    	wrongpasswordcount++;
	    	if(wrongpasswordcount>1)
	    	{
				Intent startMain = new Intent(Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);
				wrongpasswordcount=0;

	    	}
	    }
			 */}
		}};

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) 
		{
			if (event.getAction() == KeyEvent.ACTION_UP)
			{
				switch (event.getKeyCode())
				{
				case KeyEvent.KEYCODE_BACK:
					return true;
				case KeyEvent.KEYCODE_HOME:
					finish();
					return true;
				}
			}
			return super.dispatchKeyEvent(event);
		}

		@Override
		public void onConfigurationChanged(Configuration newConfig) 
		{
			try {
				DeBug.ShowToast(getApplicationContext(), ""+newConfig.orientation);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//	setContentView(R.layout.device_admin_password);

			super.onConfigurationChanged(newConfig);
		}

}

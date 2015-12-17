package com.example.esptouch.demo_activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.example.carset.R; 
import com.example.esptouch.task.__IEsptouchTask;
import com.example.esptouch.EsptouchTask;
import com.example.esptouch.IEsptouchListener;
import com.example.esptouch.IEsptouchResult;
import com.example.esptouch.IEsptouchTask; 

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class EsptouchDemoActivity extends Activity implements OnClickListener {
    private String PATH;
    private String FAVOSSIDPATH ="/ESP8266ssid.xml";
    private String FAVOPASSPATH ="/ESP8266pass.xml";
    
    private List<String> ssid_list = new ArrayList<String>(); 
    private List<String> pass_list = new ArrayList<String>(); 
	   private String str_ssid="",str_pass="";
	    private String str_ssid_FLAG="",str_pass_FLAG="";
		private int index;
	private static final String TAG = "EsptouchDemoActivity";

	private TextView mTvApSsid;

	private EditText mEdtApPassword;

	private Button mBtnConfirm;
	
	private Switch mSwitchIsSsidHidden;

	private EspWifiAdminSimple mWifiAdmin;
	
	private Spinner mSpinnerTaskCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.esptouch_demo_activity);
		readSSID(FAVOSSIDPATH);
	    readPASS(FAVOPASSPATH);
		mWifiAdmin = new EspWifiAdminSimple(this);
		mTvApSsid = (TextView) findViewById(R.id.tvApSssidConnected);
		mEdtApPassword = (EditText) findViewById(R.id.edtApPassword);
		mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
		mSwitchIsSsidHidden = (Switch) findViewById(R.id.switchIsSsidHidden);
		mBtnConfirm.setOnClickListener(this);
		initSpinner();
	}
	
	private void initSpinner()
	{
		mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
		int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
		int length = spinnerItemsInt.length;
		Integer[] spinnerItemsInteger = new Integer[length];
		for(int i=0;i<length;i++)
		{
			spinnerItemsInteger[i] = spinnerItemsInt[i];
		}
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_list_item_1, spinnerItemsInteger);
		mSpinnerTaskCount.setAdapter(adapter);
		mSpinnerTaskCount.setSelection(1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// display the connected ap's ssid
		String apSsid = mWifiAdmin.getWifiConnectedSsid();
		if (apSsid != null) {
			mTvApSsid.setText(apSsid);
		} else {
			mTvApSsid.setText("");
		}
		
		index=-1;
		int len = ssid_list.size();
		for(int i=0; i<len; i++){
			if(apSsid.equals(ssid_list.get(i))){ 
				mEdtApPassword.setText(pass_list.get(i)); 
				str_ssid_FLAG = apSsid;
				str_pass_FLAG = pass_list.get(i);
				index = i;
				i=len;
			}
		}
		// check whether the wifi is connected
		boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
		mBtnConfirm.setEnabled(!isApSsidEmpty);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {

		
		if (v == mBtnConfirm) {
			
			if(mEdtApPassword.getText().toString().length()==0){
				new AlertDialog.Builder(this) 
	    	    .setTitle(R.string.str_tip_name_pass) 
	    	    .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {
	    	        	 
	    	        }
	    	    }).show();
			}else{
				String apSsid = mTvApSsid.getText().toString();
				String apPassword = mEdtApPassword.getText().toString();
				String apBssid = mWifiAdmin.getWifiConnectedBssid();
				Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();
				String isSsidHiddenStr = "NO";
				String taskResultCountStr = Integer.toString(mSpinnerTaskCount
						.getSelectedItemPosition());
				if (isSsidHidden) 
				{
					isSsidHiddenStr = "YES";
				}
				if (__IEsptouchTask.DEBUG) {
					Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
							+ ", " + " mEdtApPassword = " + apPassword);
				}
				
				str_ssid = apSsid;
				str_pass = apPassword;
				
				new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
						isSsidHiddenStr, taskResultCountStr);
			}
			
		}
	}
	
	private class EsptouchAsyncTask2 extends AsyncTask<String, Void, IEsptouchResult> {

		private ProgressDialog mProgressDialog;

		private IEsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(EsptouchDemoActivity.this);
			mProgressDialog
					.setMessage(getText(R.string.str_configurationing));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					synchronized (mLock) {
						if (__IEsptouchTask.DEBUG) {
							Log.i(TAG, "progress dialog is canceled");
						}
						if (mEsptouchTask != null) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					getText(R.string.str_wait), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
		}

		@Override
		protected IEsptouchResult doInBackground(String... params) {
			synchronized (mLock) {
				String apSsid = params[0];
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				boolean isSsidHidden = false;
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
						isSsidHidden, EsptouchDemoActivity.this);
			}
			IEsptouchResult result = mEsptouchTask.executeForResult();
			return result;
		}

		@Override
		protected void onPostExecute(IEsptouchResult result) {
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
					"Confirm");
			// it is unnecessary at the moment, add here just to show how to use isCancelled()
			if (!result.isCancelled()) {
				if (result.isSuc()) {
					  new AlertDialog.Builder(EsptouchDemoActivity.this)
			          .setIcon(android.R.drawable.ic_dialog_info)
			          .setTitle(R.string.tip)
			          .setMessage(R.string.str_set_s) 
			          .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int whichButton) {
							    dialog.dismiss();    
							    finish();
			              }
			          }).show();
//					mProgressDialog.setMessage("Esptouch success, bssid = "
//							+ result.getBssid() + ",InetAddress = "
//							+ result.getInetAddress().getHostAddress());
				} else {
//					mProgressDialog.setMessage("Esptouch fail");
					  new AlertDialog.Builder(EsptouchDemoActivity.this)
			          .setIcon(android.R.drawable.ic_dialog_info)
			          .setTitle(R.string.tip)
			          .setMessage(R.string.str_configuration_fail) 
			          .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int whichButton) {
							    dialog.dismiss();     
			              }
			          }).show();
				}
			}
		}
	}
	
	private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String text = result.getBssid() +R.string.str_con_wifi;
				Toast.makeText(EsptouchDemoActivity.this, text,
						Toast.LENGTH_LONG).show();
			}

		});
	}

	private IEsptouchListener myListener = new IEsptouchListener() {

		@Override
		public void onEsptouchResultAdded(final IEsptouchResult result) {
			onEsptoucResultAddedPerform(result);
		}
	};
	
	private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

		private ProgressDialog mProgressDialog;

		private IEsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(EsptouchDemoActivity.this);
			mProgressDialog
					.setMessage(getText(R.string.str_configurationing));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					synchronized (mLock) {
						if (__IEsptouchTask.DEBUG) {
							Log.i(TAG, "progress dialog is canceled");
						}
						if (mEsptouchTask != null) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					getText(R.string.str_wait), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
		}

		@Override
		protected List<IEsptouchResult> doInBackground(String... params) {
			int taskResultCount = -1;
			synchronized (mLock) {
				String apSsid = params[0];
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				String taskResultCountStr = params[4];
				boolean isSsidHidden = false;
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
						isSsidHidden, EsptouchDemoActivity.this);
				mEsptouchTask.setEsptouchListener(myListener);
			}
			List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
			return resultList;
		}

		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
					"Confirm");
			IEsptouchResult firstResult = result.get(0);
			// check whether the task is cancelled and no results received
			if (!firstResult.isCancelled()) {
//				int count = 0; 
//				final int maxDisplayCount = 5; 
				if (firstResult.isSuc()) {
			           try {  
			          	    if(str_ssid_FLAG.equals(str_ssid)){
			          	    	if(!str_pass_FLAG.equals(str_pass)){
			          		    	if(str_ssid.length()>0 && str_pass.length()>0 && index!=-1){
			    	              	    ssid_list.set(index, str_ssid);
			    	              	    pass_list.set(index, str_pass);
			    	  				    writeSSID(FAVOSSIDPATH);
			    	  				    writePASS(FAVOPASSPATH);
			              	    	}
			          	    	}
			          	    }else{
			          	    	if(str_ssid.length()>0 && str_pass.length()>0){
				              	    ssid_list.add(str_ssid);
				              	    pass_list.add(str_pass);
				  				    writeSSID(FAVOSSIDPATH);
				  				    writePASS(FAVOPASSPATH);
			          	    	}
			          	    }
	    	            } catch (Exception e) {  
	    	                // TODO Auto-generated catch block  
	    	                e.printStackTrace();  
	    	            } 
	
	          	    
	            	   
					  new AlertDialog.Builder(EsptouchDemoActivity.this)
			          .setIcon(android.R.drawable.ic_dialog_info)
			          .setTitle(R.string.tip)
			          .setMessage(R.string.str_set_s) 
			          .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int whichButton) {
 
				            	dialog.dismiss();    
								finish();
			              }
			          }).show();  
				} else { 
					
					 str_ssid = null;
					 str_pass = null;
					 
					  new AlertDialog.Builder(EsptouchDemoActivity.this)
			          .setIcon(android.R.drawable.ic_dialog_info)
			          .setTitle(R.string.tip)
			          .setMessage(R.string.str_configuration_fail) 
			          .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int whichButton) {
							    dialog.dismiss();     
			              }
			          }).show();
				}
			}
		}
	}
	
	

	private void readSSID(String res){  
		PATH =this.getFilesDir().getPath();
		String path =PATH + res;
		
    	File dirFile=new File(PATH);
    	if(!dirFile.exists()){
    		dirFile.mkdir();
    	}
	
		String str=null;
		
		if(!ssid_list.isEmpty()){
			ssid_list.clear();
		}
        try {
        	FileInputStream fis =new  FileInputStream (path);  
        	BufferedReader in = new BufferedReader(new InputStreamReader(fis));  
	        while( (str=in.readLine()) != null ){
	        	ssid_list.add(Uri.decode(str)); 
	        }
	        in.close();
        }
        catch(FileNotFoundException e){   
        }
        catch(Exception e){   
        }
         
	}

	private void readPASS(String res){  
		PATH =this.getFilesDir().getPath();
		String path =PATH + res;
		
    	File dirFile=new File(PATH);
    	if(!dirFile.exists()){
    		dirFile.mkdir();
    	}
	
		String str=null;
		
		if(!pass_list.isEmpty()){
			pass_list.clear();
		}
        try {
        	FileInputStream fis =new  FileInputStream (path);  
        	BufferedReader in = new BufferedReader(new InputStreamReader(fis));  
	        while( (str=in.readLine()) != null ){
	        	pass_list.add(Uri.decode(str)); 
	        }
	        in.close();
        }
        catch(FileNotFoundException e){   
        }
        catch(Exception e){   
        }
         
	}	
	
	
 private void writeSSID(String res){   
		PATH =this.getFilesDir().getPath();
		String path =PATH + res;
        
    	File dirFile=new File(PATH);
    	if(!dirFile.exists()){
    		dirFile.mkdir();
    	}
    	
		int len = ssid_list.size();
		File write = new File(path);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(write));
			for(int i=0; i<len; i++){ 
				bw.write(Uri.encode(ssid_list.get(i))+"\r\n");
			} 
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 
	}
 private void writePASS(String res){   
		PATH =this.getFilesDir().getPath();
		String path =PATH + res;
		
    	File dirFile=new File(PATH);
    	if(!dirFile.exists()){
    		dirFile.mkdir();
    	}
    	
		int len = pass_list.size();
		File write = new File(path);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(write));
			for(int i=0; i<len; i++){ 
				bw.write(Uri.encode(pass_list.get(i))+"\r\n");
			} 
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 
	}
}

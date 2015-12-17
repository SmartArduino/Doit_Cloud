package com.example.carset;

     

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity; 
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;


@SuppressLint("HandlerLeak")
public class RegActivity extends Activity implements OnClickListener{
   
	private Button reg_Button;
	private EditText edit_uid,edit_pass; 
	private String uid,pass,open_id;
    private ProgressDialog pd;  
    static public final String SET_UID = "UID"; 
	
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
       
        	switch(msg.what){
        	case 0:     
        		 pd = ProgressDialog.show(RegActivity.this, getText(R.string.str_reging), getText(R.string.str_wait)); 
    			break; 
        	case 1:       
	        	  SharedPreferences settings = getSharedPreferences(SET_UID, 0);  
	        	  Editor editor = settings.edit(); 
	        	  editor.putBoolean("CHECK", true);
	        	  editor.putString("OPEN_ID", open_id);
	        	  editor.putString("UID", uid);
	        	  editor.putString("PASS", pass); 
	        	  editor.commit();
	        	  
       		     pd.dismiss();  
//       		     Intent intent = new Intent(RegActivity.this, ListCarActivity.class);
//   		         startActivity(intent);  
 			     Intent intent=new Intent();  
 	             intent.putExtra("UID", uid); 
 	             intent.putExtra("PASS", pass); 
                 intent.putExtra("OPEN_ID", open_id);    
                 setResult(Activity.RESULT_OK, intent); 
       		     finish();
       		     
	            break; 	
        	case -1:      
        		 Toast.makeText(RegActivity.this,R.string.str_reg_fail,Toast.LENGTH_LONG).show();
            	
        		 pd.dismiss();
	            break; 
        	case -2:      
 
	            break;  
    		default:
    			break;
        	}
        }
	};
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.reg);
		 
		initView();
		  
	}

	private void initView() {
		reg_Button=(Button) findViewById(R.id.reg_buton);
		reg_Button.setOnClickListener(this); 
		 
		edit_uid =(EditText) findViewById(R.id.reg_uid);
		edit_pass=(EditText) findViewById(R.id.reg_pass);
		   
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.reg_buton: 
		    Reg(edit_uid.getText().toString(),edit_pass.getText().toString());
		    break;
		default:
			break;
		}
	}
	
	private void Reg(String uid, String pass){
		if(uid.length()==0 || pass.length()==0){
			new AlertDialog.Builder(this) 
    	    .setTitle(R.string.str_tip_name_pass) 
    	    .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) {
    	        	 
    	        }
    	    }).show();
		}else{
			this.uid=uid;
			this.pass=pass;
			new setdata().start();
		}
	}
	
	
	  private String encode(String src){
			String des=Uri.encode(src);
			return des;
		}
	  
	  
	  private class setdata extends Thread{
	    	 public void run() {
	    		 
	    	       Message msg1=new Message();
	    		   msg1.what=0;
	    		   handler.sendMessage(msg1);
	    		    
	    		   String res = null;
	    		   boolean flag=false;
	    		   
	    	       try {
		  	    	 	 String uri = "http://wechat.doit.am/mobile_app/reg.php?uid="+encode(uid)+
			        			 "&password="+encode(pass);
			        			  
			    	   	 MyHttp myGet = new MyHttp(uri);
			    	     String des =  myGet.httpPost();
			    	     
			    	 	 if(des.length()>0){
			    	 		  JSONObject jsonEvents =  new JSONObject(des);
			    	 		  res= jsonEvents.getString("ret"); 
			    	 		  if(res.equals("1")){
			    	 			  ////////////////////////////
			    	 			   flag=true;
//				   	    	       Message msg2=new Message();
//					    		   msg2.what=1;
//					    		   handler.sendMessage(msg2);
			    	 		  }else{
					    	       Message msg=new Message();
					    		   msg.what=-1;
					    		   handler.sendMessage(msg);
					    		   
				    	    	   return;
			    	 		  }
			    	     }else{ 
				    	       Message msg=new Message();
				    		   msg.what=-1;
				    		   handler.sendMessage(msg);
				    		   
			    	    	   return;
			    	     }
	    	    	  
	    	          } catch (Exception e) { 
		    	           Message msg=new Message();
			    		   msg.what=-1;
			    		   handler.sendMessage(msg);
			    		   
		    	    	   return;
	    	        }
	    	       
	    	       /////////////////////////////////////////////
	    	       if(flag){
		    	       try {
			  	    	 	 String uri = "http://wechat.doit.am/mobile_app/get_open_id.php?uid="+encode(uid)+
				        			 "&password="+encode(pass);
				        			  
				    	   	 MyHttp myGet = new MyHttp(uri);
				    	     String des =  myGet.httpPost();
				    	     
				    	 	 if(des.length()>0){
				    	 		  JSONObject jsonEvents =  new JSONObject(des);
				    	 		  res= jsonEvents.getString("ret"); 
				    	 		  if(res.equals("1")){
				    	 			 open_id = jsonEvents.getString("open_id"); 
				    	 			 LoginActivity.open_id = open_id;
				    	 			 
				    	 			 Message msg2=new Message();
				    	 		     msg2.what=1;
				    	 		     handler.sendMessage(msg2);
				    	 		  }else{
						    	       Message msg=new Message();
						    		   msg.what=-1;
						    		   handler.sendMessage(msg);
						    		   
					    	    	   return;
				    	 		  }
				    	     }else{ 
					    	       Message msg=new Message();
					    		   msg.what=-1;
					    		   handler.sendMessage(msg);
					    		   
				    	    	   return;
				    	     }
		    	    	  
		    	          } catch (Exception e) {
		    	           // TODO Auto-generated catch block
			    	           e.printStackTrace();
			    	           
			    	           Message msg=new Message();
				    		   msg.what=-1;
				    		   handler.sendMessage(msg);
				    		   
			    	    	   return;
		    	        }
	    	       }
	    

	        } 
	 
	    }
}
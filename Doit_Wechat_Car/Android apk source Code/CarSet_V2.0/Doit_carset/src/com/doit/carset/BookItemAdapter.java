package com.doit.carset;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Vector;
 
 


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.app.Dialog;
public class BookItemAdapter extends BaseAdapter{
//    private static final String PATH = "/data/data/com.doit.carset/files/";
	private LayoutInflater mInflater; 
	private Context mContext;
	private Vector<BookModel> mModels = new Vector<BookModel>();
	private ListView mListView;
	SyncImageLoader syncImageLoader;
	 
//	private int index;
//	private TextView favoView;
	
//	   private String getStringMD5(String originString)
//	    {
//		     String resultString = null;
//		
//		     try {
//		      resultString = new String(originString);
//		      MessageDigest md = MessageDigest.getInstance("MD5"); 
//		      resultString = bytesToString(md.digest(resultString.getBytes()));
//		     }catch (Exception ex){
//		
//		     }
//		     return resultString;
//	    }
//	    
//	    private String bytesToString(byte[] data)
//	    {
//		     char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
//		       'e', 'f' };
//		     char[] temp = new char[data.length * 2];
//		     for (int i = 0; i < data.length; i++)
//		     {
//		      byte b = data[i];
//		      temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
//		      temp[i * 2 + 1] = hexDigits[b & 0x0f];
//		     }
//		     return new String(temp);
//	    }
//	    
//	    private void saveUrlImageFile(Bitmap bm, String fileName) throws Exception { 
//	    	final String able=".png";
//
//	        File myFile = new File(PATH+fileName+able);
//	        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myFile));
//	        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
//	        bos.flush();
//	        bos.close();
//	    }
//	    
//		 private class addFavo extends Thread{
//	    	  public  void run() {
//	    		  synchronized (MyActivity.list) {  
//	  	    	       String icon_name=PATH+getStringMD5(MyActivity.mainlist.get(index))+".png"; 
//	  			       Bitmap bitmap;
//	  						try {
//	  							bitmap = BitmapFactory.decodeStream(new URL(MyActivity.mainlist.get(index)).openStream());
//	  							saveUrlImageFile(bitmap,getStringMD5(MyActivity.mainlist.get(index)));
//	  							
//	  							MyActivity.favo_list.add((icon_name));
//	  							MyActivity.favo_listName.add(MyActivity.mainlistName.get(index));
//	  							MyActivity.favo_listHttp.add((MyActivity.mainlistHttp.get(index)));
//	  		                	   
//	  						} catch (MalformedURLException e) { 
//	  							e.printStackTrace();
//	  						} catch (Exception e) {  
//	  							e.printStackTrace();
//	  						} 
//	    		  }
//		  
//	          }
//	     }
		 
		 
		 
	    
	public BookItemAdapter(Context context,ListView listView){
		mInflater = LayoutInflater.from(context);
		syncImageLoader = new SyncImageLoader(); 
		mContext = context;
		mListView = listView; 
		mListView.setOnScrollListener(onScrollListener);
	}

	
	public void addBook(String device_img,String device_id,String device_key,String device_name,String device_stat){
		BookModel model = new BookModel();
		model.device_img =device_img;
		model.device_id = device_id;
		model.device_key = device_key;
		model.device_name = device_name;
		model.device_stat = device_stat;
		 
		mModels.add(model);
	}
	
	
	public void setModel(String device_img,String device_id,String device_key,String device_name,String device_stat, int index){
		BookModel model = new BookModel();
		model.device_img =device_img;
		model.device_id = device_id;
		model.device_key = device_key;
		model.device_name = device_name;
		model.device_stat = device_stat;
		 
		mModels.set(index, model);
	}
	
	
	public void clean(){
		mModels.clear();
	}

	//@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mModels.size();
	}

	//@Override
	public Object getItem(int position) {
		if(position >= getCount()){
			return null;
		}
		return mModels.get(position);
	}

	//@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	//@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.book_item_adapter, null);
		}
		BookModel model = mModels.get(position);
		convertView.setTag(position);
		
		ImageView iv = (ImageView) convertView.findViewById(R.id.sItemIcon);
		iv.setBackgroundResource(R.drawable.rc_item_bg);
		
		TextView sItemTitle =  (TextView) convertView.findViewById(R.id.sItemTitle);
		sItemTitle.setText(model.device_name);
		
		TextView like =  (TextView) convertView.findViewById(R.id.likeText);
		like.setText(model.device_id);
		
//		TextView commend =  (TextView) convertView.findViewById(R.id.commendText);
//		commend.setText(model.sort_id);
		
		
		
		TextView sItemInfo =  (TextView) convertView.findViewById(R.id.sItemInfo); 
		sItemInfo.setTag(position);
		if(!model.device_stat.equals("0")){
			sItemInfo.setBackgroundResource(R.drawable.online);
		}else {
			sItemInfo.setBackgroundResource(R.drawable.offline);
		}
//		sItemInfo.setOnClickListener(new View.OnClickListener() {
//						////@Override
//						public void onClick(View v) { 
//							favoView = (TextView) v;
//							index = Integer.parseInt(v.getTag().toString());
//							BookModel model = mModels.get(index);
//							
//							if(model.favo_flag==1){
// 
//							}else if (model.favo_flag==-1){
//				        		new AlertDialog.Builder(mContext) 
//								.setTitle(" Tips")
//				                .setMessage(" Add this template to favorite ?")
//				                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
//				                    public void onClick(DialogInterface dialog, int which) {
//				                    	 
//				                    }
//				                })
//				                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
//				                    public void onClick(DialogInterface dialog, int whichButton) { 
//										BookModel model = mModels.get(index); 
//										model.favo_flag = 1; 
//										mModels.set(index,model);
//										
//										TextView sItemInfo =  (TextView) favoView.findViewById(R.id.sItemInfo); 
//									    sItemInfo.setBackgroundResource(R.drawable.favo_bt_style);
//										
//										new addFavo().start(); 
//				                     }
//				                }).show();
//							}
//						}
//		});
        
		
		
		syncImageLoader.loadImage(position,model.device_img,imageLoadListener);
		return  convertView;
	}
	
	 

	SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener(){

		//@Override
		@SuppressWarnings("deprecation")
		public void onImageLoad(Integer t, Drawable drawable) {
			//BookModel model = (BookModel) getItem(t);
			View view = mListView.findViewWithTag(t);
			if(view != null){
				ImageView iv = (ImageView) view.findViewById(R.id.sItemIcon);
				iv.setBackgroundDrawable(drawable);
			}
		}
		//@Override
		public void onError(Integer t) {
			BookModel model = (BookModel) getItem(t);
			View view = mListView.findViewWithTag(model);
			if(view != null){
				ImageView iv = (ImageView) view.findViewById(R.id.sItemIcon);
				iv.setBackgroundResource(R.drawable.rc_item_bg);
			}
		}
		
	};
	
	public void loadImage(){
		int start = mListView.getFirstVisiblePosition();
		int end =mListView.getLastVisiblePosition();
		if(end >= getCount()){
			end = getCount() -1;
		}
		syncImageLoader.setLoadLimit(start, end);
		syncImageLoader.unlock();
	}
	
	AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
		
		//@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
//					DebugUtil.debug("SCROLL_STATE_FLING");
					syncImageLoader.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//					DebugUtil.debug("SCROLL_STATE_IDLE");
					loadImage();
					//loadImage();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					syncImageLoader.lock();
					break;
	
				default:
					break;
			}
			
		}
		
		//@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}
	};
}

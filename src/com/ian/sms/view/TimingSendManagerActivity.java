package com.ian.sms.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ian.sms.db.DatabaseHelper;
import com.ian.sms.mode.TimingSendMessageItem;
import com.ian.sms.service.SMSListenerService;
import com.ian.sms.tool.DateTool;
import com.ian.sms.tool.SmsTool;
import com.ian.sms.tool.StringTool;

/**
 * 定时发送短信管理界面
 * @author ives
 * @date Feb 18, 20139:51:12 PM
 * @version 1.0
 * @comment
 */
public class TimingSendManagerActivity extends BaseActivity{
	Spinner sortSpinner;
	ListView timing_listview;
	TimingAdapter adapter;
	ImageButton ibtn_insert;
	ArrayList<TimingSendMessageItem> allMessages;
	
	Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timing);
		init();
		handler = new Handler();
		handler.obtainMessage();
		updateUIBroadCastReceiver = new UpdateUIBroadCastReceiver();
		IntentFilter filter = new IntentFilter(UpdateUIBroadCastReceiver.ACTION);
		registerReceiver(updateUIBroadCastReceiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String where = TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_PENDING +" and "+TimingSendMessageItem.TIMING_NEXTDATE+">"+System.currentTimeMillis()+" and "+TimingSendMessageItem.TIMING_NEXTDATE+"<"+(System.currentTimeMillis()+SMSListenerService.READ_DB_INTERVAL);
		allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, where, TimingSendMessageItem.TIMING_NEXTDATE+" desc");
		sortSpinner.setSelection(1);
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(updateUIBroadCastReceiver);
	}

	private void init(){
		sortSpinner = (Spinner) findViewById(R.id.spinner_sort);
		timing_listview = (ListView) findViewById(R.id.timing_listview);
		ibtn_insert = (ImageButton) findViewById(R.id.editMessage);
		String[] titles = {"全部","待发送","已发送","草稿","未发送"};
		ArrayAdapter<String> spinner_adapter  = new ArrayAdapter<String>(this,R.layout.timing_spinner_item,titles);
		allMessages = new ArrayList<TimingSendMessageItem>();
		//设置下拉列表的风格  
		spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		sortSpinner.setAdapter(spinner_adapter);
		sortSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				refreshUIBySpinner(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		timing_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
			}
		});
		timing_listview.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showChoiceDialog(position);
				return true;
			}
		});
		ibtn_insert.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(TimingSendManagerActivity.this,TimingEditActvity.class));
			}
		});
		
		adapter = new TimingAdapter(this);
		timing_listview.setAdapter(adapter);
	}
	/**
	 * 根据spinner刷新界面
	 * @date 2013-3-8下午11:09:00
	 * @comment 
	 * @param index
	 */
	private void refreshUIBySpinner(int index){
		switch(index){
		case 0:
			allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, null, "date desc");
			break;
		case 1:
			String where = TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_PENDING +" and "+TimingSendMessageItem.TIMING_NEXTDATE+">"+System.currentTimeMillis()+" and "+TimingSendMessageItem.TIMING_NEXTDATE+"<"+(System.currentTimeMillis()+SMSListenerService.READ_DB_INTERVAL);
			allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, where, TimingSendMessageItem.TIMING_NEXTDATE+" desc");
			break;
		case 2:
			allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_FINISH, "date desc");
			break;
		case 3:
			allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_DRAFT, "date desc");
			break;
		case 4:
			allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_PENDING, "date desc");
			break;
		default:
			allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, null, "date desc");
			break;
		}
		adapter.notifyDataSetChanged();
	}
	/**
	 * 显示选择对话框
	 * @date 2013-3-6下午1:08:12
	 * @comment 
	 * @param index
	 */
	private void showChoiceDialog(final int index){
		final CharSequence[] items = { "删除", "现在发送" };
		AlertDialog.Builder builder = new AlertDialog.Builder(TimingSendManagerActivity.this);
		builder.setTitle("请选择");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if(items[item].equals("删除")){
					DatabaseHelper.delete(TimingSendManagerActivity.this, DatabaseHelper.TABLE_NAME_TIMING, TimingSendMessageItem.TIMING_ID+"="+allMessages.get(index).getId());
					int selectedPosition = sortSpinner.getSelectedItemPosition();
					switch(selectedPosition){
					case 0:
						allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, null, "date desc");
						break;
					case 1:
						String where = TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_PENDING +" and "+TimingSendMessageItem.TIMING_NEXTDATE+">"+System.currentTimeMillis()+" and "+TimingSendMessageItem.TIMING_NEXTDATE+"<"+(System.currentTimeMillis()+SMSListenerService.READ_DB_INTERVAL);
						allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, where, TimingSendMessageItem.TIMING_NEXTDATE+" desc");
						break;
					case 2:
						allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_FINISH, "date desc");
						break;
					case 3:
						allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_DRAFT, "date desc");
						break;
					default:
						allMessages = DatabaseHelper.getAllTimingMessage(TimingSendManagerActivity.this, null, "date desc");
						break;
					}
					adapter.notifyDataSetChanged();
				
				}else if(items[item].equals("现在发送")){
					
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	private static class ViewHolder {
		TextView title;
		TextView body;
		TextView date;
		TextView status;
		ImageView head;
	}
	class TimingAdapter extends BaseAdapter{
		private LayoutInflater layoutinflater;
		Context context;
		public TimingAdapter(Context context) {
			layoutinflater = LayoutInflater.from(context); 
			this.context = context;
		}
		@Override
		public int getCount() {
			return allMessages.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = layoutinflater.inflate(R.layout.view_timing_item, null);
				viewHolder = new ViewHolder();
				viewHolder.body = (TextView) convertView.findViewById(R.id.messagecontent);
				viewHolder.title = (TextView) convertView.findViewById(R.id.messagetitle);
				viewHolder.date = (TextView) convertView.findViewById(R.id.date);
				viewHolder.status = (TextView) convertView.findViewById(R.id.tv_status);
				viewHolder.head = (ImageView) convertView.findViewById(R.id.head);
				convertView.setTag(viewHolder);
			} else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.body.setText(allMessages.get(position).getBody());
			String date_str = allMessages.get(position).getNextDate();
			if(date_str!=null)
			viewHolder.date.setText(DateTool.formatTimeStampString(context,Long.valueOf(date_str),true));
			viewHolder.status.setText(allMessages.get(position).getSatusForString());
			ArrayList<String> address = StringTool.getListToString(allMessages.get(position).getAddress(), ",");
			if(address.size()>1){
				viewHolder.title.setText("多人");
			}else{
				String name = SmsTool.getNameFromPhone(address.get(0), context);
				if(name==null){
					viewHolder.title.setText(address.get(0));
				}else{
					viewHolder.title.setText(name);
				}
				
			}
			return convertView;
		}
		
	}
	UpdateUIBroadCastReceiver updateUIBroadCastReceiver;
	public class UpdateUIBroadCastReceiver extends BroadcastReceiver{
		public static final String ACTION = "com.ian.sms.reciver.updateui.timingsend";
		@Override
		public void onReceive(Context context, Intent intent) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					refreshUIBySpinner(sortSpinner.getSelectedItemPosition());
				}
			});
		}
	}
}

package com.ian.sms.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ian.sms.db.DatabaseHelper;
import com.ian.sms.mode.AutoReplyItem;
import com.ian.sms.mode.TimingSendMessageItem;
import com.ian.sms.service.SMSListenerService;

/**
 * 回复管理界面
 * @author ives
 * @date Feb 18, 20139:48:49 PM
 * @version 1.0
 * @comment
 */
public class ReplyManagerActivity extends BaseActivity{
	ImageButton btn_addReply;
	ListView lv_replys;
	ReplyAdapter adapter;
	ArrayList<AutoReplyItem>reply_datas;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reply);
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		reply_datas = DatabaseHelper.getAllReplyMessage(this, null, null);
		adapter.notifyDataSetChanged();
	}

	private void init(){
		btn_addReply = (ImageButton) findViewById(R.id.addbutton);
		lv_replys = (ListView) findViewById(R.id.reply_listview);
		btn_addReply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ReplyManagerActivity.this,AddReplyItemActivity.class));
			}
		});
		
		lv_replys.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ReplyManagerActivity.this,AddReplyItemActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("item", reply_datas.get(position));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		lv_replys.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showChoiceDialog(position);
				return true;
			}
		});
		reply_datas = DatabaseHelper.getAllReplyMessage(this, null, null);
		adapter = new ReplyAdapter(this);
		lv_replys.setAdapter(adapter);
		
		
	}
	/**
	 * 显示选择对话框
	 * @date 2013-3-6下午1:12:49
	 * @comment 
	 * @param index
	 */
	private void showChoiceDialog(final int index){
		final CharSequence[] items = { "删除", "编辑","启动","停用" };
		AlertDialog.Builder builder = new AlertDialog.Builder(ReplyManagerActivity.this);
		builder.setTitle("请选择");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if(item==0){
					DatabaseHelper.delete(ReplyManagerActivity.this, DatabaseHelper.TABLE_NAME_REPLY, AutoReplyItem.ID+"="+reply_datas.get(index).getId());
					reply_datas = DatabaseHelper.getAllReplyMessage(ReplyManagerActivity.this, null, null);
					adapter.notifyDataSetChanged();
				
				}else if(item==1){
					Intent intent = new Intent(ReplyManagerActivity.this,AddReplyItemActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("item", reply_datas.get(index));
					intent.putExtras(bundle);
					startActivity(intent);
				}else if(item==2){
					if(reply_datas.get(index).isStart())return;
					ContentValues values = new ContentValues();
					values.put(AutoReplyItem.ISSTART, 1);
					DatabaseHelper.update(ReplyManagerActivity.this,DatabaseHelper.TABLE_NAME_REPLY, values, AutoReplyItem.ID+"="+reply_datas.get(index).getId(), null);
					reply_datas.get(index).setStart(true);
					adapter.notifyDataSetChanged();
				}else if(item ==3){
					if(reply_datas.get(index).isStart()==false)return;
					ContentValues values = new ContentValues();
					values.put(AutoReplyItem.ISSTART, 0);
					DatabaseHelper.update(ReplyManagerActivity.this,DatabaseHelper.TABLE_NAME_REPLY, values, AutoReplyItem.ID+"="+reply_datas.get(index).getId(), null);
					reply_datas.get(index).setStart(false);
					adapter.notifyDataSetChanged();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private static class ViewHolder {
		TextView time;
		TextView scope;
		TextView body;
		TextView status;
	}
	class ReplyAdapter extends BaseAdapter{
		Context context;
		LayoutInflater layoutinflater;
		public ReplyAdapter(Context context) {
			this.context = context;
			layoutinflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return reply_datas.size();
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
				viewHolder = new ViewHolder();
				convertView =  layoutinflater.inflate(R.layout.view_reply_item, null);
				viewHolder.time  = (TextView) convertView.findViewById(R.id.tv_time_value);
				viewHolder.scope  = (TextView) convertView.findViewById(R.id.tv_scope_value);
				viewHolder.body  = (TextView) convertView.findViewById(R.id.tv_body);
				viewHolder.status  = (TextView) convertView.findViewById(R.id.tv_status);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if(reply_datas.get(position).isCustomTime()){
				viewHolder.time.setText(reply_datas.get(position).getDay_startTime()+"--"+reply_datas.get(position).getDay_stopTime());
			}else{
				viewHolder.time.setText("全天");
			}
			
			if(!reply_datas.get(position).isAllContacts()){//自定义接收人
				if(reply_datas.get(position).isIncludeStranger()){//是否包含陌生人
					viewHolder.scope.setText("自定义联系人+陌生人");
				}else{
					viewHolder.scope.setText("自定义联系人");
				}
			}else{
				if(reply_datas.get(position).isIncludeStranger()){
					viewHolder.scope.setText("所有人");
				}else{
					viewHolder.scope.setText("所有联系人");
				}
			}
			viewHolder.body.setText(reply_datas.get(position).getBody());
			if(reply_datas.get(position).isStart()){
				viewHolder.status.setText("状态：启用");
			}else{
				viewHolder.status.setText("状态：停用");
			}
			return convertView;
		}
		
	}
}

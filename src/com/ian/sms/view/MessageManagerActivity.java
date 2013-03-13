package com.ian.sms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ian.sms.mode.SmsGroupInfo;
import com.ian.sms.tool.DateTool;
import com.ian.sms.tool.Globals;
import com.ian.sms.tool.SmsTool;

/**
 * 消息管理界面
 * 
 * @author ives
 * @date Feb 18, 20139:46:06 PM
 * @version 1.0
 * @comment
 */
public class MessageManagerActivity extends BaseActivity {

	OnClickListener onclickListener;
	SmsListAdapter adapter;
	private ListView listview;
	private List<SmsGroupInfo> groupInfos;
	//是否处于多选模式
	public boolean isMultiselectMode = false;
	//删除按钮，默认隐藏，在批量操作模式下才出现
	ImageButton ibtn_delete;
	ImageButton ibtn_insertnewsms;
	TextView tv_title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkAutoStartTalkActivity();
		//刷新数据
		adapter.notifyDataSetChanged();
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){//拦截back按钮事件
			if(isMultiselectMode==true){
				isMultiselectMode = false;
				ibtn_delete.setVisibility(View.GONE);
				ibtn_insertnewsms.setVisibility(View.VISIBLE);
				tv_title.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	private void init(){
		groupInfos = SmsTool.getThreadSmsData(this);
		listview = (ListView) this.findViewById(R.id.ListView_Group_Sms);
		ibtn_delete = (ImageButton) this.findViewById(R.id.ibtn_delete);
		ibtn_insertnewsms = (ImageButton) findViewById(R.id.ibtn_insertnewsms);
		tv_title = (TextView) findViewById(R.id.message_activity_title);
		
		adapter =new SmsListAdapter(this); 
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(MessageManagerActivity.this,TalkActivity.class);
				intent.putExtra("threadid", groupInfos.get(position).threadId);
				startActivity(intent);
			}
		});
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
				isMultiselectMode = true;//设置为多选模式
				//更新界面
				adapter.notifyDataSetChanged();
				ibtn_delete.setVisibility(View.VISIBLE);
				ibtn_insertnewsms.setVisibility(View.GONE);
				tv_title.setVisibility(View.GONE);
				return true;
			}
			
		});
		ibtn_insertnewsms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MessageManagerActivity.this,FSendActivity.class);
				intent.putExtra("hideback", true);
				startActivity(intent);
			}
		});
		ibtn_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String>choices = adapter.getCheckboxs();
				for (int i = 0; i < choices.size(); i++) {
					int result = SmsTool.delete_sys_group(MessageManagerActivity.this, choices.get(i));
				}
				Globals.MAX_ID = SmsTool.select_sms_MAXID(MessageManagerActivity.this);
				//刷新界面
//				isMultiselectMode = false;
//				ibtn_delete.setVisibility(View.GONE);
//				ibtn_insertnewsms.setVisibility(View.VISIBLE);
//				tv_title.setVisibility(View.VISIBLE);
				
				groupInfos =  SmsTool.getThreadSmsData(MessageManagerActivity.this);
				adapter.reflushSelected(groupInfos.size());
				adapter.notifyDataSetChanged();
			}
		});
	}
	/**
	 * 检查是否需要自动跳转
	 * @date Feb 28, 201312:22:28 AM
	 * @comment
	 */
	private void checkAutoStartTalkActivity(){
		//刷新界面数据
		groupInfos = SmsTool.getThreadSmsData(this);
		adapter.notifyDataSetChanged();
		//查看是否有新消息
		if(MainActivity.threadid==null)return;
		if(MainActivity.threadid.equals(""))return;
		Intent intent = new Intent(MessageManagerActivity.this,TalkActivity.class);
		String autoStatrTalk = MainActivity.threadid;
		intent.putExtra("threadid", autoStatrTalk);
		MainActivity.threadid = null;
		startActivity(intent);
	}
	
	private static class ViewHolder {
		TextView title;
		TextView body;
		TextView date;
		ImageView head;
		CheckBox check;
		TextView draft;
		View error;
	}

	class SmsListAdapter extends BaseAdapter {
		private LayoutInflater layoutinflater;
		private Context context;
		//记录被选中的checkbox
		HashMap<Integer, Boolean> isSelected;
		public SmsListAdapter(Context c) {
			layoutinflater = LayoutInflater.from(c);
			this.context = c;
			isSelected = new HashMap<Integer, Boolean>();
			for (int i = 0; i < groupInfos.size(); i++) {
                isSelected.put(i, false);
            }
		}
		
		public void reflushSelected(int size){
			for (int i = 0; i < size; i++) {
                isSelected.put(i, false);
            }
		}
		@Override
		public int getCount() {
			return groupInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}
		/**
		 * 取得所有被选中的threadid
		 * @date 2013-3-2下午11:50:16
		 * @comment 
		 * @return
		 */
		public ArrayList<String> getCheckboxs(){
			ArrayList<String>result = new ArrayList<String>();
			for (int i = 0; i < isSelected.size(); i++) {
				if(isSelected.get(i)==true){//如果此ID被选中，则取出对应threadid
					result.add(groupInfos.get(i).threadId);
				}
			}
			return result;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = layoutinflater.inflate(R.layout.view_sms_group_item, null);
				viewHolder = new ViewHolder();
				viewHolder.body = (TextView) convertView.findViewById(R.id.messagecontent);
				viewHolder.title = (TextView) convertView.findViewById(R.id.messagetitle);
				viewHolder.date = (TextView) convertView.findViewById(R.id.tv_date);
				viewHolder.check = (CheckBox) convertView.findViewById(R.id.checkbox);
				viewHolder.head = (ImageView) convertView.findViewById(R.id.head);
				viewHolder.draft = (TextView) convertView.findViewById(R.id.tv_draft);
				viewHolder.error = convertView.findViewById(R.id.ibtn_error);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final int index = position;
			viewHolder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					isSelected.put(index, isChecked);
				}
			});
			//根据编辑模式设置控件属性
			if(isMultiselectMode){
				viewHolder.check.setVisibility(View.VISIBLE);
				viewHolder.check.setFocusable(true);
			}else{
				viewHolder.check.setVisibility(View.GONE);
				viewHolder.check.setFocusable(false);
			}
			if(viewHolder.check!=null){
				boolean booleanValue = false;
				if(isSelected.get(position)!=null ){
					booleanValue = isSelected.get(position).booleanValue();
				}
				viewHolder.check.setChecked(booleanValue);
			}
			
			//设置标题
			StringBuilder title_str = new StringBuilder();
			if(groupInfos.get(position).name==null || groupInfos.get(position).name.trim().equals("")){
				title_str.append(groupInfos.get(position).address);
			}else{
				title_str.append(groupInfos.get(position).name);
			}
			if(groupInfos.get(position).smsNoReadCount>0){//如果有未读短信
				viewHolder.title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
				title_str.append("("+groupInfos.get(position).smsNoReadCount+"/"+groupInfos.get(position).smsAllCount+")");
			}else{
				viewHolder.title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//正常
				title_str.append("("+groupInfos.get(position).smsAllCount+")");
			}
			viewHolder.title.setText(title_str);
			viewHolder.body.setText(groupInfos.get(position).smsBody);
			viewHolder.date.setText(DateTool.formatTimeStampString(context,groupInfos.get(position).date,false));
			if(groupInfos.get(position).smsFailedCount>0)viewHolder.error.setVisibility(View.VISIBLE);
			else viewHolder.error.setVisibility(View.GONE);
			if(groupInfos.get(position).smsDraftCount>0)viewHolder.draft.setVisibility(View.VISIBLE);
			else viewHolder.draft.setVisibility(View.GONE);
			return convertView;
		}
	}
}
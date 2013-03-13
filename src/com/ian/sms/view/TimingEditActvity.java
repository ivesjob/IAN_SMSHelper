package com.ian.sms.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ian.sms.db.DatabaseHelper;
import com.ian.sms.mode.ContactsItem;
import com.ian.sms.mode.TimingSendMessageItem;
import com.ian.sms.service.SMSListenerService;
import com.ian.sms.tool.BinaryTool;
import com.ian.sms.tool.DateTool;
import com.ian.sms.tool.ListTool;

public class TimingEditActvity extends BaseActivity{
	ListView lv_setting;
	ListView lv_receiveUsers;
	ArrayList<ContactsItem> persons;
	LinkedList<Lv_Setting_Item> setting_datas;
	Lv_SettingAdapter ettingAdapter;
	ReceiveUsersAdapter receiveUsersAdapter;
	
	ImageButton ibtn_back;
	ImageButton ibtn_save;
	ImageButton ibtn_insert;
	ImageButton ibtn_send;
	ImageButton ibtn_addContacts;
	TextView tv_Contacts;
	TextView tv_body;
	
	int year,month,day,hour,minute,weekDaySign;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timing_edit);
		init();
	}

	private void init(){
		year = DateTool.getYear();
		month = DateTool.getMonthOfYear();
		day = DateTool.getDayOfMonth();
		hour = DateTool.getHour();
		minute = DateTool.getMinute();
		
		
		lv_receiveUsers = (ListView) findViewById(R.id.receiveusers);
		lv_setting = (ListView) findViewById(R.id.lv_setting);
		
		ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
		ibtn_save = (ImageButton) findViewById(R.id.ibtn_savetoDraft);
		ibtn_addContacts = (ImageButton) findViewById(R.id.btn_contexts_add);
		ibtn_insert = (ImageButton) findViewById(R.id.insertdiy);
		ibtn_send = (ImageButton) findViewById(R.id.sendbutton);
		
		tv_Contacts = (TextView) findViewById(R.id.edit_contexts_add);
		tv_body = (TextView) findViewById(R.id.edit_body);
		
		persons = new ArrayList<ContactsItem>();
		setting_datas = new LinkedList<TimingEditActvity.Lv_Setting_Item>();
		setting_datas.add(new Lv_Setting_Item("周期 :","不重复"));
		setting_datas.add(new Lv_Setting_Item("日期 :",DateTool.getDate()));
		setting_datas.add(new Lv_Setting_Item("时间 :",DateTool.getTimeToString()));
		
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.ibtn_back:
					finish();
					break;
				case R.id.ibtn_savetoDraft:
					saveToDraft();
					break;
				case R.id.btn_contexts_add:
					startActivityForResult(new Intent(TimingEditActvity.this,ContactChoiceActivity.class), ContactChoiceActivity.OPEN_CONTACTCHOICEACTIVITY_RESULTCODE);
					break;
				case R.id.insertdiy:
						
					break;
				case R.id.sendbutton:
					save();
					break;
				default:
					break;
				}
			}
		};
		ibtn_back.setOnClickListener(clickListener);
		ibtn_addContacts.setOnClickListener(clickListener);
		ibtn_insert.setOnClickListener(clickListener);
		ibtn_send.setOnClickListener(clickListener);
		ibtn_save.setOnClickListener(clickListener);

		ettingAdapter = new Lv_SettingAdapter(this);
		receiveUsersAdapter = new ReceiveUsersAdapter(this);
		lv_receiveUsers.setAdapter(receiveUsersAdapter);
		lv_setting.setAdapter(ettingAdapter);
		lv_setting.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if(position==0){
					final CharSequence[] items = { "不重复", "每天", "每周","每月" };
					AlertDialog.Builder builder = new AlertDialog.Builder(TimingEditActvity.this);
					builder.setTitle("请选择");
					Dialog  dialog ;
					builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
						@Override
					    public void onClick(DialogInterface dialog, int item) {
							if(item==1){
								if(setting_datas.size()==3){
									setting_datas.remove(1);
								}
							}else if(item==2){
								if(setting_datas.size()==2){
									setting_datas.removeLast();
									setting_datas.add(new Lv_Setting_Item("日期 :","星期"+DateTool.getWeekDay()));
									setting_datas.add(new Lv_Setting_Item("时间 :",DateTool.getTimeToString()));
								}else{
									setting_datas.get(1).value = "星期"+DateTool.getWeekDay();	
								}
								
							}else{
								if(setting_datas.size()==2){
									setting_datas.removeLast();
									setting_datas.add(new Lv_Setting_Item("日期 :",DateTool.getDate()));
									setting_datas.add(new Lv_Setting_Item("时间 :",DateTool.getTimeToString()));
								}else if(setting_datas.size()==3){
									setting_datas.get(1).value = DateTool.getDate();
								}
							}
					      setting_datas.get(0).value = items[item].toString();
					      ettingAdapter.notifyDataSetChanged();
					      dialog.dismiss();
						}
					});
					  dialog = builder.create();
					dialog.show();
				}else if(position==1){
					if(setting_datas.get(position).name.equals("日期 :")){
						if(setting_datas.get(0).value.equals("每周")){
							showWeekPickerDialog();
						}else{
							showDatePickerDialog();	
						}
					}else if(setting_datas.get(position).name.equals("时间 :")){
						showTimePickerDialog();
					}
				}else if(position==2){
					showTimePickerDialog();
				}
			}
		});
	}
	private void save(){
		if (TextUtils.isEmpty(tv_body.getText())) {//检查内容是否为空
            return;
        }
		//检查收信人是否为空
		if(persons.size()<1 && TextUtils.isEmpty(tv_Contacts.getText())){
			return;
		}
		//检查收信人文本格式
		
		//将所有接收人收集起来
		String body = tv_body.getText().toString();
		String phons_str = tv_Contacts.getText().toString().trim();
		StringBuilder builder = new StringBuilder(phons_str);
		
		for (int i = 0; i < persons.size(); i++) {
			builder.append(","+persons.get(i).getPhone());
		}
		
		int repeat = 0;
		int status = TimingSendMessageItem.TIMING_STATUS_PENDING;
		String date_str = null;
		Date date;
		if(setting_datas.get(0).value.equals("不重复")){
			repeat = TimingSendMessageItem.TIMING_REPEAT_ONE;
			date = new Date(year-1900, month, day, hour, minute);
			date_str = String.valueOf(date.getTime());
		}else if(setting_datas.get(0).value.equals("每天")){
			repeat = TimingSendMessageItem.TIMING_REPEAT_REPEAT_DAY;
			Calendar c = Calendar.getInstance();
			if(c.get(Calendar.HOUR_OF_DAY)>hour){//如果今天超出了执行时间，就明天开始执行
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
			}
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			date_str = String.valueOf(c.getTimeInMillis());
		}else if(setting_datas.get(0).value.equals("每周")){
			repeat = TimingSendMessageItem.TIMING_REPEAT_REPEAT_WEEK;
			//得到下次执行时间
			long nextExecuteTime = DateTool.getNextExecuteTime_Week1(weekDaySign,hour,minute);
			date_str = String.valueOf(nextExecuteTime);
		}else if(setting_datas.get(0).value.equals("每月")){
			repeat = TimingSendMessageItem.TIMING_REPEAT_REPEAT_MONTH;
			int execute_month = month;
			if(DateTool.getDayOfMonth()>day){
				++execute_month;
			}
			date = new Date(DateTool.getYear()-1900, execute_month, day, hour, minute);
			date_str = String.valueOf(date.getTime());
		}
		//存入数据库
		ContentValues values = new ContentValues();
		values.put(TimingSendMessageItem.TIMING_DATE, date_str);
		values.put(TimingSendMessageItem.TIMING_BODY, body);
		values.put(TimingSendMessageItem.TIMING_ADDRESS, builder.toString());
		values.put(TimingSendMessageItem.TIMING_REPEAT, repeat);
		values.put(TimingSendMessageItem.TIMING_STATUS, status);
		values.put(TimingSendMessageItem.TIMING_NEXTDATE, date_str);
		values.put(TimingSendMessageItem.TIMING_WEEKDAYSIGN, weekDaySign);
		
		long result = DatabaseHelper.insert(this, DatabaseHelper.TABLE_NAME_TIMING, values);
		if(result>0){//成功
			Toast.makeText(TimingEditActvity.this, "添加成功", Toast.LENGTH_SHORT).show();
			//通知service刷新一次数据库
			Intent intent = new Intent(SMSListenerService.EXECUTING_RECEIVED_ACTION);
			intent.putExtra(SMSListenerService.EXECUTE_TYPE_NAME, SMSListenerService.EXECUTE_TYPE_TIMING);
			intent.putExtra(SMSListenerService.EXECUTE_TIMING_ACTION_NAME, SMSListenerService.EXECUTE_TIMING_ACTION_REFRESHDB);
			sendBroadcast(intent);
		}else{//失败
			Toast.makeText(TimingEditActvity.this, "添加失败,请检查原因", Toast.LENGTH_LONG).show();
		}
		//刷新界面
		 tv_body.setText("");
		 tv_Contacts.setText("");
		 persons.clear();
		 receiveUsersAdapter.notifyDataSetChanged();
	}
	private void saveToDraft(){
		save();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    switch (resultCode) {
		case ContactChoiceActivity.OPEN_CONTACTCHOICEACTIVITY_RESULTCODE:
			ArrayList<ContactsItem> persons = (ArrayList<ContactsItem>) data.getExtras().getSerializable("persons");
			if(persons==null || persons.size()<1)return;
			this.persons.addAll(persons);
			ListTool.removeDuplicate(this.persons);
			receiveUsersAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	} 
	private void showWeekPickerDialog(){
		final CharSequence[] items = { "星期天","星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		final int[] selected = {0,0,0,0,0,0,0};//标志哪些下标被选中
		  Builder builder = new AlertDialog.Builder(this);   
          builder.setTitle("多选列表对话框");   
          Dialog dialog;
          DialogInterface.OnMultiChoiceClickListener mutiListener = new DialogInterface.OnMultiChoiceClickListener() {   
                  @Override  
                  public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {   
                	  if(isChecked)
                		  selected[which] = 1;
                	  else
                		  selected[which] = 0;  
                  }   
              };   
          builder.setMultiChoiceItems(items, null, mutiListener);   
          builder.setNegativeButton("取消", null);  
          builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String result = "";
				String showStr = "";
				for (int i = 0; i < selected.length; i++) {
					result+=selected[i];
					if(selected[i]==1)showStr+=(items[i]+",");
				}
				setting_datas.get(1).value = showStr;
				TimingEditActvity.this.weekDaySign = BinaryTool.binary2Int(result);
				ettingAdapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
          dialog = builder.create();
          dialog.show();
	}
	private void showDatePickerDialog(){
		DatePickerDialog dialog = new DatePickerDialog(TimingEditActvity.this, new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
				TimingEditActvity.this.year = year;
				TimingEditActvity.this.month = monthOfYear;
				TimingEditActvity.this.day = dayOfMonth;
				if(setting_datas.size()==3){
					setting_datas.get(1).value = year+"-"+month+"-"+day;
					ettingAdapter.notifyDataSetChanged();
				}
				
			}
		}, DateTool.getYear(), DateTool.getMonthOfYear(), DateTool.getDayOfMonth());
		dialog.show();
	}
	private void showTimePickerDialog(){
		TimePickerDialog dialog = new TimePickerDialog(TimingEditActvity.this, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				TimingEditActvity.this.hour = hourOfDay;
				TimingEditActvity.this.minute  = minute;
				setting_datas.get(setting_datas.size()-1).value = hourOfDay+":"+minute;
				ettingAdapter.notifyDataSetChanged(); 
			}
		}, DateTool.getHour(), DateTool.getMinute(), true);
		dialog.show();
	}
	class Lv_SettingAdapter extends BaseAdapter{
		Context context;
		LayoutInflater inflater;
		public Lv_SettingAdapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return setting_datas.size();
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
			convertView = inflater.inflate(R.layout.view_timing_setting, null);
			TextView tv1 = (TextView) convertView.findViewById(R.id.name);
			TextView tv2 = (TextView) convertView.findViewById(R.id.contents);
			tv1.setText(setting_datas.get(position).name);
			tv2.setText(setting_datas.get(position).value);
			return convertView;
		}
	}
	class  Lv_Setting_Item{
		public String name;
		public String value;
		public Lv_Setting_Item(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
	}
	class ReceiveUsersAdapter extends BaseAdapter {
		LayoutInflater inflater;

		public ReceiveUsersAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return persons.size();
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
			// 得到自定义的单项数据结构对象
			ReceiveUsersViewHolder holder = null;
			if (convertView == null) {
				holder = new ReceiveUsersViewHolder();
				convertView = inflater.inflate(R.layout.view_receiveuser_item, null);
				holder.info = (TextView) convertView.findViewById(R.id.usermessage);
				holder.btn_cancel = (ImageButton) convertView.findViewById(R.id.delete_button);
				convertView.setTag(holder);
			} else {
				holder = (ReceiveUsersViewHolder) convertView.getTag();
			}
			if(persons.get(position).getName()==null || persons.get(position).getName().equals("")){
				holder.info.setText(persons.get(position).getPhone());
			}else{
				holder.info.setText(persons.get(position).getName()+" ("+persons.get(position).getPhone()+")");	
			}
			
			final int index = position;
			holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					persons.remove(index);
					receiveUsersAdapter.notifyDataSetChanged();
				}
			});
			return convertView;
		}
	}
	public final class ReceiveUsersViewHolder {
		public TextView info;
		public ImageButton btn_cancel;
	}
}

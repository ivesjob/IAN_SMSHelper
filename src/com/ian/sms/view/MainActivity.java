package com.ian.sms.view;

import java.math.BigInteger;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.ian.sms.service.SMSListenerService;
import com.ian.sms.tool.BinaryTool;

/**
 * 主界面实现
 * 
 * @author ives
 * @date 2013-2-18下午5:42:03
 * @version 1.0
 * @comment 主界面实现
 */
public class MainActivity extends TabActivity implements
		OnCheckedChangeListener {
	private TabHost host;// 标题
	private RadioGroup radioderGroup;// 用于容纳各个标题的group
	/**各个tag的标志**/
	public static final String TAB_TAG_SMS = "SMS";
	public static final String TAB_TAG_FSEND = "FSEND";
	public static final String TAB_TAG_REPLY = "REPLY";
	public static final String TAB_TAG_TIMING = "TIMING";
	public static final String TAB_TAG_MORE = "MORE";
	
	/*是否自动跳转到对话页面*/
	public static String threadid = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
		startServices();
		checkTag();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent); 
		checkTag();
	}

	/**
	 * 初始化iew
	 * @date Feb 27, 20136:43:16 PM
	 * @comment
	 */
	private void initView(){
		// 实例化TabHost
		host = this.getTabHost();
		// 添加选项卡
		host.addTab(host.newTabSpec(TAB_TAG_SMS).setIndicator(TAB_TAG_SMS).setContent(new Intent(this, MessageManagerActivity.class)));
		host.addTab(host.newTabSpec(TAB_TAG_FSEND).setIndicator(TAB_TAG_FSEND).setContent(new Intent(this, FSendActivity.class)));
		host.addTab(host.newTabSpec(TAB_TAG_REPLY).setIndicator(TAB_TAG_REPLY).setContent(new Intent(this, ReplyManagerActivity.class)));
		host.addTab(host.newTabSpec(TAB_TAG_TIMING).setIndicator(TAB_TAG_TIMING).setContent(new Intent(this, TimingSendManagerActivity.class)));
		radioderGroup = (RadioGroup) findViewById(R.id.main_radio);
		radioderGroup.setOnCheckedChangeListener(this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_button0:
			host.setCurrentTabByTag(TAB_TAG_SMS);
			break;
		case R.id.radio_button1:
			host.setCurrentTabByTag(TAB_TAG_FSEND);
			break;
		case R.id.radio_button2:
			host.setCurrentTabByTag(TAB_TAG_REPLY);
			break;
		case R.id.radio_button3:
			host.setCurrentTabByTag(TAB_TAG_TIMING);
			break;
		case R.id.radio_button4:
			host.setCurrentTabByTag(TAB_TAG_MORE);
			break;
		}
	}
	/**
	 * 检查是否需要跳转到指定tab
	 * @date Feb 27, 20136:43:44 PM
	 * @comment
	 */
	private void checkTag(){
		String tag = getIntent().getStringExtra("tab");
		threadid = getIntent().getStringExtra("threadid");
		if(tag!=null){
			jumpTab(tag);
		}
	}
	/**
	 * 跳转到指定的选项卡
	 * @date Feb 27, 20136:30:58 PM
	 * @comment 
	 * @param tag
	 */
	private void jumpTab(String tag){
		if(tag.equals(TAB_TAG_SMS)){
			host.setCurrentTabByTag(TAB_TAG_SMS);
			host.setCurrentTab(0);
		}else if(tag.equals(TAB_TAG_FSEND)){
			host.setCurrentTabByTag(TAB_TAG_FSEND);
			host.setCurrentTab(1);
		}else if(tag.equals(TAB_TAG_REPLY)){
			host.setCurrentTabByTag(TAB_TAG_REPLY);
			host.setCurrentTab(2);
		}else if(tag.equals(TAB_TAG_TIMING)){
			host.setCurrentTabByTag(TAB_TAG_TIMING);
			host.setCurrentTab(3);
		}else if(tag.equals(TAB_TAG_MORE)){
			host.setCurrentTabByTag(TAB_TAG_MORE);
			host.setCurrentTab(4);
		} 
	}

	private void startServices(){
		startService(new Intent(SMSListenerService.SERVICE_SMSListener_TAG));
	}
	
	
}

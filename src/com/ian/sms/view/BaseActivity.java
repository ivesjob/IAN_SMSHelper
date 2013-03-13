package com.ian.sms.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * activity自定义父类
 * @author ives
 * @date Feb 18, 201310:27:53 PM
 * @version 1.0
 * @comment 用于管理activity的style
 */
public class BaseActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

}

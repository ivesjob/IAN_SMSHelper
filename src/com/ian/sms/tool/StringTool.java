package com.ian.sms.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class StringTool {
	
	public static ArrayList<String> getListToString(String str,String delimiters){
		StringTokenizer tokenizer = new StringTokenizer(str,delimiters);
		ArrayList<String> result = new ArrayList<String>();
		  while (tokenizer.hasMoreTokens()) {
			  String temp =tokenizer.nextToken();
			  if(temp!=null && !temp.trim().equals(""))
			  result.add(temp);
		  }
		  return result;
	}
	
	public static Set<String> getSetToString(String str,String delimiters){
		StringTokenizer tokenizer = new StringTokenizer(str,delimiters);
		Set<String> result = new HashSet<String>();
		  while (tokenizer.hasMoreTokens()) {
			  String temp =tokenizer.nextToken();
			  if(temp!=null && !temp.trim().equals(""))
			  result.add(temp);
		  }
		  return result;
	}
}

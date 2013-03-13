package com.ian.sms.tool;

import java.math.BigInteger;

public class BinaryTool {
	public static int binary2Int(String binary){
		BigInteger i = new BigInteger(binary,2);
		return Integer.valueOf(i.toString(10));
	}
	public static String int2Binary(int value){
		BigInteger i = new BigInteger(String.valueOf(value),10);
		String result =i.toString(2);
		if(result.length()<7){
			String s = new String("0000000"+result);
			return s.substring(s.length()-7);
		}
		return result;
	}
}

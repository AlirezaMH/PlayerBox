package com.alirezamh.android.playerbox;

import android.content.Context;


/**
 * Created by admin on 8/3/2016.
 */

public class PlayerUtility {
    public static long timeDiffrence = 0;


    /*
	 *
	 */
    private static String format(String num){
        if(num.length() < 2){
            num = "0"+num;
        }
        return num;
    }
    /*
     *
     */
    private static String format(int number){
        String num = number + "";
        if(num.length() < 2){
            num = "0"+num;
        }
        return num;
    }


    public static long getTime(long ms){
        return ms + timeDiffrence * 1000;
    }
    public static long getTime(int sec){
        return (sec + timeDiffrence) * 1000;
    }
    public static long getTime(String sec){
        return getTime(Integer.parseInt(sec));
    }

    public static boolean isConnected(Context cntx, boolean error){
//        if(!Utility.isConnected(cntx)){
//            if(error) Toast.makeText(cntx, R.string.errorInternetConnection, Toast.LENGTH_LONG).show();
//            return false;
//        }
        return true;
    }


}

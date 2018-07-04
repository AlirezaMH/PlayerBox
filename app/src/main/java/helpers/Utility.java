package helpers;

import android.util.DisplayMetrics;
import android.view.Display;

import com.alirezamh.android.playerboxdemo.MainActivity;

/**
 * Author:      Alireza Mahmoodi
 * Created:     8/12/2017
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */
public class Utility {


    public static float getDisplayRatio(){
        DisplayMetrics outMetrics = getScreenMetric();
        return (float) outMetrics.heightPixels / (float) outMetrics.widthPixels;
    }

    public static float getHeightByRatio(float ratio){
        DisplayMetrics outMetrics = getScreenMetric();
        float density  = getScreenDensity();
        float dpWidth  = outMetrics.widthPixels / density;
        float dpHeight = dpWidth / ratio;

        return dpHeight;
    }
    public static float getHeightByRatioInPixel(float ratio){
        DisplayMetrics outMetrics = getScreenMetric();
        float dpHeight = outMetrics.widthPixels / ratio;

        return dpHeight;
    }

    public static float getScreenDensity(){
        return MainActivity.instance.getResources().getDisplayMetrics().density;
    }

    public static int dp(int pixle){
        return (int) (getScreenDensity() * pixle);
    }

    public static DisplayMetrics getScreenMetric(){
        Display display = MainActivity.instance.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        return outMetrics;
    }

    public static float getScreenHeightInPixel(){
        DisplayMetrics outMetrics = getScreenMetric();
        return outMetrics.heightPixels;
    }
    public static float getScreenHeight(){
        return getScreenHeightInPixel() / getScreenDensity();
    }

    public static float getScreenWidthInPixel(){
        DisplayMetrics outMetrics = getScreenMetric();
        return outMetrics.widthPixels;
    }
    public static float getScreenWidth(){
        return getScreenWidthInPixel() / getScreenDensity();
    }

}

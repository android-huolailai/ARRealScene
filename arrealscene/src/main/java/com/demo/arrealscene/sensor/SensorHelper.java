package com.demo.arrealscene.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

/**
 * Created by yixiaofei on 2017/3/28 0028.
 */

public class SensorHelper implements SensorEventListener{

    public interface OnSensorListener{
        void onSensorChanged(SensorEvent sensorEvent);
    }

    private static SensorHelper sensorHelper;

    public static boolean flag = false;

    private Context mContext;

    private SensorManager sensorManager = null;

    private Sensor sensor;

    private OnSensorListener onSensorListener;

    public static synchronized SensorHelper getSensorHelper(Context context){
        if(sensorHelper==null){
            sensorHelper = new SensorHelper(context);
        }
        return sensorHelper;
    }

    public SensorHelper(Context paramContext)
    {
        this.mContext = paramContext;

        sensorManager = ((SensorManager)this.mContext.getSystemService(Context.SENSOR_SERVICE));

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void setOnSensorListener(OnSensorListener onSensorListener) {
        this.onSensorListener = onSensorListener;
    }

    public void unRegisterSensor(){
        sensorManager.unregisterListener(this);
    }
    public static int getAndroidSDKVersion()
    {
        try
        {
            int i = Integer.valueOf(Build.VERSION.SDK).intValue();
            return i;
        }
        catch (NumberFormatException localNumberFormatException)
        {
            Log.e("e", localNumberFormatException.toString());
        }
        return 0;
    }
    public boolean getFlag()
    {
        if (getAndroidSDKVersion() < 8) {
            flag = false;
        }
        return flag;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(onSensorListener!=null){
            onSensorListener.onSensorChanged(sensorEvent);
        }
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            int orientation = (int) sensorEvent.values[0];
//            if (orientation == 1)
//            {
//                flag = true;
//            }
//        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

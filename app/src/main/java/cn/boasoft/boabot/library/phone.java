package cn.boasoft.boabot.library;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class phone {
    private final Context context;

    public phone(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            perm perm = new perm(context);
            if(perm.check(Manifest.permission.ANSWER_PHONE_CALLS)){
                TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                tm.endCall();
            }else{
                try {
                    endCallITelephony();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Class<?> c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                Object telephonyService = m.invoke(tm);
                if(telephonyService != null) {
                    Class<?> telephonyServiceClass = Class.forName(telephonyService.getClass().getName());
                    Method endCallMethod = telephonyServiceClass.getDeclaredMethod("endCall");
                    endCallMethod.invoke(telephonyService);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                try {
                    endCallITelephony();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void endCallITelephony() throws Exception {
        Class<?> c = Class.forName("android.os.ServiceManager");
        Method method = c.getMethod("getService", String.class);
        IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
        ITelephony telephony = ITelephony.Stub.asInterface(iBinder);
        if(telephony != null) telephony.endCall();
    }
}

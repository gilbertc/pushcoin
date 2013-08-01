package com.pushcoin.client.core;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import android.widget.Toast;

import com.acs.smartcard.Reader;

public class PushCoinService extends Service {

   private final String TAG = PushCoinService.class.getSimpleName();
   private void Log(String message)
   {
      android.util.Log.d(TAG, message);
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) 
   {
      Log("onStartCommand");

      Bundle bundle = intent.getExtras();
      if (bundle != null)
      {
         Intent usbIntent = (Intent) bundle.getParcelable("UsbIntent");
         if (usbIntent != null)
         {
            Toast.makeText(getApplicationContext(), usbIntent.getAction(), Toast.LENGTH_LONG).show();
            Log(usbIntent.getAction());
         }
      }

      return Service.START_NOT_STICKY;
   }

   @Override
   public IBinder onBind(Intent intent) 
   {
      return null;
   }
} 

package com.pushcoin.client.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class UsbReceiver extends BroadcastReceiver {

   private final String TAG = UsbReceiver.class.getSimpleName();
   private void Log(String message)
   {
      android.util.Log.d(TAG, message);
   }

   @Override
   public void onReceive(Context context, Intent intent) {
      Intent service = new Intent(context, PushCoinService.class);

      Bundle bundle = new Bundle();
      bundle.putParcelable("UsbIntent", intent);

      service.putExtras(bundle);
      context.startService(service);
   }
} 

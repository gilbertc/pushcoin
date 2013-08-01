package com.pushcoin.client.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

import com.pushcoin.client.core.PushCoinService;

public class TestAppActivity extends Activity
{
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
   }

   @Override
   public void onResume()
   {
      super.onResume();
      Intent service = new Intent(this, PushCoinService.class);
      startService(service);
   }

   @Override
   public void onPause()
   {
      super.onPause();
   }

}

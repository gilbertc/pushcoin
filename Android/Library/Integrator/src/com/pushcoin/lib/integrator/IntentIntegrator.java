/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.lib.integrator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.pushcoin.ifce.connect.data.CallbackParams;
import com.pushcoin.ifce.connect.data.Cancelled;
import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.ChargeResult;
import com.pushcoin.ifce.connect.data.PollParams;
import com.pushcoin.ifce.connect.data.QueryParams;
import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.ifce.connect.data.Error;
import com.pushcoin.ifce.connect.Actions;
import com.pushcoin.ifce.connect.Keys;
import com.pushcoin.ifce.connect.Messages;
import com.pushcoin.ifce.connect.listeners.ChargeResultListener;
import com.pushcoin.ifce.connect.listeners.PollResultListener;
import com.pushcoin.ifce.connect.listeners.QueryResultListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.util.Log;

public class IntentIntegrator {

	public static final int REQUEST_CODE = 0x0000a088; // Only use bottom 16
														// bits
	public static final String PUSHCOIN_GATEWAY_PACKAGE_NAME = "com.pushcoin.srv.gateway";
	public static final Collection<String> TARGET_ALL_KNOWN = list(PUSHCOIN_GATEWAY_PACKAGE_NAME);

	private final Context context;
	private String title;
	private String message;
	private String buttonYes;
	private String buttonNo;
	private Collection<String> targetApplications;

	public IntentIntegrator(Context context) {
		this.context = context.getApplicationContext();
		this.title = context.getString(R.string.default_install_service_title);
		this.message = context
				.getString(R.string.default_install_service_prompt);
		this.buttonYes = context.getString(R.string.default_yes);
		this.buttonNo = context.getString(R.string.default_no);
		this.targetApplications = TARGET_ALL_KNOWN;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitleByID(int titleID) {
		title = context.getString(titleID);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessageByID(int messageID) {
		message = context.getString(messageID);
	}

	public String getButtonYes() {
		return buttonYes;
	}

	public void setButtonYes(String buttonYes) {
		this.buttonYes = buttonYes;
	}

	public void setButtonYesByID(int buttonYesID) {
		buttonYes = context.getString(buttonYesID);
	}

	public String getButtonNo() {
		return buttonNo;
	}

	public void setButtonNo(String buttonNo) {
		this.buttonNo = buttonNo;
	}

	public void setButtonNoByID(int buttonNoID) {
		buttonNo = context.getString(buttonNoID);
	}

	public Collection<String> getTargetApplications() {
		return targetApplications;
	}

	public void setTargetApplications(Collection<String> targetApplications) {
		this.targetApplications = targetApplications;
	}

	public void setSingleTargetApplication(String targetApplication) {
		this.targetApplications = Collections.singleton(targetApplication);
	}

	public AlertDialog invokeActivity(Activity activity, String action, Parcelable p) {
		Intent intent = new Intent(PUSHCOIN_GATEWAY_PACKAGE_NAME + action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		if (p != null)
			intent.putExtra(Keys.KEY_PARAMS, p);

		String targetAppPackage = findTargetAppPackage(intent);
		if (targetAppPackage == null) {
			return showDownloadDialog(activity);
		}
		intent.setPackage(targetAppPackage);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivityForResult(activity, intent, REQUEST_CODE);
		return null;
	}

	public AlertDialog bootstrap(Activity activity) {
		return invokeActivity(activity, Actions.ACTION_BOOTSTRAP, null);
	}

	public boolean query(QueryParams params, final QueryResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_QUERY_RESULT:
					listener.onResult(new QueryResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onResult(new Error(message.getData()));
				}
			};
		};
		return invokeService(Actions.ACTION_QUERY, params, handler);
	}

	public boolean charge(ChargeParams params,
			final ChargeResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_CHARGE_RESULT:
					listener.onResult(new ChargeResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onResult(new Error(message.getData()));
				}
			};
		};
		return invokeService(Actions.ACTION_CHARGE, params, handler);
	}

	public boolean poll(PollParams params, final PollResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_CHARGE_RESULT:
					listener.onResult(new ChargeResult(message.getData()));
					break;
				case Messages.MSGID_QUERY_RESULT:
					listener.onResult(new QueryResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onResult(new Error(message.getData()));
					break;
				case Messages.MSGID_CANCELLED:
					listener.onResult(new Cancelled(message.getData()));
					break;
				}
			};
		};
		return invokeService(Actions.ACTION_POLL, params, handler);
	}

	public boolean idle() {
		return invokeService(Actions.ACTION_IDLE, null, null);
	}

	public boolean invokeService(String action, CallbackParams params,
			Handler handler) {
		Intent intent = new Intent();
		intent.setClassName(PUSHCOIN_GATEWAY_PACKAGE_NAME,
				PUSHCOIN_GATEWAY_PACKAGE_NAME + ".services.PushCoinService");
		intent.setAction(PUSHCOIN_GATEWAY_PACKAGE_NAME + action);

		if (params != null) {
			if (handler != null) {
				params.setMessenger(new Messenger(handler));
			}
			intent.putExtras(params.getBundle());
		}

		this.context.startService(intent);
		return true;
	}

	public AlertDialog settings(Activity activity) {
		return invokeActivity(activity, Actions.ACTION_SETTINGS, null);
	}

	protected void startActivityForResult(Activity activity, Intent intent, int requestCode) {
		activity.startActivityForResult(intent, requestCode);
	}

	private String findTargetAppPackage(Intent intent) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> availableApps = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (availableApps != null) {
			for (ResolveInfo availableApp : availableApps) {
				String packageName = availableApp.activityInfo.packageName;
				if (targetApplications.contains(packageName)) {
					return packageName;
				}
			}
		}
		return null;
	}

	private AlertDialog showDownloadDialog(final Activity activity) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Uri uri = Uri.parse("market://details?id="
								+ PUSHCOIN_GATEWAY_PACKAGE_NAME);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						try {
							activity.startActivity(intent);
						} catch (ActivityNotFoundException anfe) {
							// Hmm, market is not installed
							Log.w("IntentIntegrator",
									"Android Market is not installed; cannot install PushCoin");
						}
					}
				});
		downloadDialog.setNegativeButton(buttonNo,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
		return downloadDialog.show();
	}

	private static Collection<String> list(String... values) {
		return Collections.unmodifiableCollection(Arrays.asList(values));
	}
}

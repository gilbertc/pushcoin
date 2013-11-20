package com.pushcoin.lib.integrator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.QueryParams;
import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.ifce.connect.data.Error;
import com.pushcoin.ifce.connect.data.Result;
import com.pushcoin.ifce.connect.Actions;
import com.pushcoin.ifce.connect.Keys;
import com.pushcoin.ifce.connect.Messages;
import com.pushcoin.ifce.connect.listeners.QueryResultListener;
import com.pushcoin.srv.gateway.services.PaymentService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
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

	private final Activity activity;
	private String title;
	private String message;
	private String buttonYes;
	private String buttonNo;
	private Collection<String> targetApplications;

	public IntentIntegrator(Activity activity) {
		this.activity = activity;
		this.title = activity.getString(R.string.default_install_service_title);
		this.message = activity
				.getString(R.string.default_install_service_prompt);
		this.buttonYes = activity.getString(R.string.default_yes);
		this.buttonNo = activity.getString(R.string.default_no);
		this.targetApplications = TARGET_ALL_KNOWN;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitleByID(int titleID) {
		title = activity.getString(titleID);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessageByID(int messageID) {
		message = activity.getString(messageID);
	}

	public String getButtonYes() {
		return buttonYes;
	}

	public void setButtonYes(String buttonYes) {
		this.buttonYes = buttonYes;
	}

	public void setButtonYesByID(int buttonYesID) {
		buttonYes = activity.getString(buttonYesID);
	}

	public String getButtonNo() {
		return buttonNo;
	}

	public void setButtonNo(String buttonNo) {
		this.buttonNo = buttonNo;
	}

	public void setButtonNoByID(int buttonNoID) {
		buttonNo = activity.getString(buttonNoID);
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

	public AlertDialog invokeActivity(String action, Parcelable p) {
		Intent intent = new Intent(PUSHCOIN_GATEWAY_PACKAGE_NAME + action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		if (p != null)
			intent.putExtra(Keys.KEY_PARAMS, p);

		String targetAppPackage = findTargetAppPackage(intent);
		if (targetAppPackage == null) {
			return showDownloadDialog();
		}
		intent.setPackage(targetAppPackage);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivityForResult(intent, REQUEST_CODE);
		return null;
	}

	public AlertDialog bootstrap() {
		return invokeActivity(Actions.ACTION_BOOTSTRAP, null);
	}

	public boolean query(QueryParams params, final QueryResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_QUERY_RESULT:
					listener.onQueryResult(new QueryResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onQueryResult(new Error(message.getData()));
				}
			};
		};
		return invokeService(Actions.ACTION_QUERY, params.getBundle(), handler);
	}
	
	public boolean charge(ChargeParams params, final ChargeResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_CHARGE_RESULT:
					listener.onResult(new ChargeResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onChargeResult(new Error(message.getData()));
				}
			};
		};
		return invokeService(Actions.ACTION_CHARGE, params.getBundle(), handler);
	}
	
	public boolean poll(PollParams params, final PollResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_CHARGE_RESULT:
					listener.onPollResult(new ChargeResult(message.getData()));
					break;
				case Messages.MSGID_QUERY_RESULT:
					listener.onPollResult(new QueryResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onPollResult(new Error(message.getData()));
				}
			};
		};
		return invokeService(Actions.ACTION_POLL, params.getBundle(), handler);
	}
	
	public boolean idle(IdleParams params, final IdleResultListener listener) {
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Messages.MSGID_IDLE_RESULT:
					listener.onIdleResult(new IdleResult(message.getData()));
					break;
				case Messages.MSGID_ERROR:
					listener.onIdleResult(new Error(message.getData()));
				}
			};
		};
		return invokeService(Actions.ACTION_IDLE, params.getBundle(), handler);
	}
	
	public boolean invokeService(String action, Bundle bundle, Handler handler)
	{
		Intent intent = new Intent(PUSHCOIN_GATEWAY_PACKAGE_NAME + action);
		Messenger messenger = new Messenger(handler);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		
		String targetAppPackage = findTargetAppPackage(intent);
		if (targetAppPackage == null) {
			return false;
		}
		
		intent.setPackage(targetAppPackage);

		if (bundle == null)
			bundle = new Bundle();
		
		bundle.putParcelable(Keys.KEY_MESSENGER, messenger);
		intent.putExtras(bundle);
		this.activity.startService(intent);
	}

	public AlertDialog settings() {
		return invokeActivity(Actions.ACTION_SETTINGS, null);
	}

	protected void startActivityForResult(Intent intent, int requestCode) {
		activity.startActivityForResult(intent, requestCode);
	}

	private String findTargetAppPackage(Intent intent) {
		PackageManager pm = activity.getPackageManager();
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

	private AlertDialog showDownloadDialog() {
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

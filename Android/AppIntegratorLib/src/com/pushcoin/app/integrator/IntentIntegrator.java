package com.pushcoin.app.integrator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.pushcoin.core.interfaces.Actions;
import com.pushcoin.core.utils.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

public class IntentIntegrator {

	private static Logger log = Logger.getLogger(IntentIntegrator.class);

	public static final int REQUEST_CODE = 0x0000a088; // Only use bottom 16
														// bits
	public static final String DEFAULT_TITLE = "Install PushCoin?";
	public static final String DEFAULT_MESSAGE = "This application requires PushCoin. Would you like to install it?";
	public static final String DEFAULT_YES = "Yes";
	public static final String DEFAULT_NO = "No";
	public static final String PC_PACKAGE = "com.pushcoin.app.main";
	public static final Collection<String> TARGET_ALL_KNOWN = list(PC_PACKAGE);

	private final Activity activity;
	private String title;
	private String message;
	private String buttonYes;
	private String buttonNo;
	private Collection<String> targetApplications;

	public IntentIntegrator(Activity activity) {
		this.activity = activity;
		this.title = DEFAULT_TITLE;
		this.message = DEFAULT_MESSAGE;
		this.buttonYes = DEFAULT_YES;
		this.buttonNo = DEFAULT_NO;
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

	public AlertDialog invoke(String action, Bundle bundle) {
		Intent intent = new Intent(PC_PACKAGE + action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		if (bundle != null) {
			intent.putExtras(bundle);
		}

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
		return invoke(Actions.ACTION_BOOTSTRAP, null);
	}

	public AlertDialog charge(Bundle bundle) {
		return invoke(Actions.ACTION_CHARGE, bundle);
	}

	public AlertDialog settings() {
		return invoke(Actions.ACTION_SETTINGS, null);
	}

	protected void startActivityForResult(Intent intent, int code) {
		activity.startActivityForResult(intent, code);
	}

	private String findTargetAppPackage(Intent intent) {
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> availableApps = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (availableApps != null) {
			for (ResolveInfo availableApp : availableApps) {
				String packageName = availableApp.activityInfo.packageName;
				log.d(packageName);
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
						Uri uri = Uri
								.parse("market://details?id=" + PC_PACKAGE);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						try {
							activity.startActivity(intent);
						} catch (ActivityNotFoundException anfe) {
							// Hmm, market is not installed
							log.w("Android Market is not installed; cannot install PushCoin");
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

	public static IntentResult parseActivityResult(int requestCode,
			int resultCode, Intent intent) {
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				return new IntentResult(true);
			}
			return new IntentResult(false);
		}
		return null;
	}

	private static Collection<String> list(String... values) {
		return Collections.unmodifiableCollection(Arrays.asList(values));
	}
}

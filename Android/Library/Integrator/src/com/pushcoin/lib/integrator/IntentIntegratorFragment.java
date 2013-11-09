package com.pushcoin.lib.integrator;

import android.app.Fragment;
import android.content.Intent;

public final class IntentIntegratorFragment extends IntentIntegrator {

	private final Fragment fragment;

	public IntentIntegratorFragment(Fragment fragment) {
		super(fragment.getActivity());
		this.fragment = fragment;
	}

	@Override
	protected void startActivityForResult(Intent intent, int requestCode) {
		fragment.startActivityForResult(intent, requestCode);
	}
}

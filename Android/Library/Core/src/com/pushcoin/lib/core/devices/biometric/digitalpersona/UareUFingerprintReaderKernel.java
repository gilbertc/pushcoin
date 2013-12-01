package com.pushcoin.lib.core.devices.biometric.digitalpersona;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.AsyncTask;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid.Format;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.CaptureResult;
import com.digitalpersona.uareu.Reader.ImageProcessing;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.pushcoin.lib.core.payment.biometric.FingerprintQuery;
import com.pushcoin.lib.core.query.IQuery;
import com.pushcoin.lib.core.query.QueryListener;
import com.pushcoin.lib.core.utils.Logger;

public class UareUFingerprintReaderKernel {
	private static Logger log = Logger
			.getLogger(UareUFingerprintReaderKernel.class);

	private static UareUFingerprintReaderKernel instance = null;
	static {
		instance = new UareUFingerprintReaderKernel();
	}

	public static UareUFingerprintReaderKernel getInstance() {
		return instance;
	}

	private Camera camera = null;
	private Reader reader = null;
	private boolean isRunning = false;
	private QueryListener listener = null;
	private Engine engine = null;

	public UareUFingerprintReaderKernel() {

	}

	public void getReaders() {
		try {
			ReaderCollection readers = UareUGlobal.GetReaderCollection();
			readers.GetReaders();

			log.d("reader size: " + readers.size());

			if (reader != null) {
				if (readers.size() <= 0
						|| (readers.get(0).GetDescription().serial_number
								.equals(reader.GetDescription().serial_number) == false)) {
					clearReader();
				}
			}

			if (reader != null) {
				log.d("no change to reader");
				return;
			}

			if (readers.size() == 0) {
				log.d("no reader found");
				return;
			}

			engine = UareUGlobal.GetEngine();
			reader = readers.get(0);

			log.d("found readers: " + readers.size() + "; using "
					+ reader.GetDescription().serial_number);

		} catch (Exception ex) {
			log.e("getReaders", ex);
		}
	}

	private class CaptureTask extends AsyncTask<Void, Void, IQuery> {
		protected IQuery doInBackground(Void... params) {
			try {
				if (reader != null) {
					reader.Open(Priority.EXCLUSIVE);
					log.d("fingerprint reader opened");
				}
				return capture();
			} catch (UareUException e) {
				log.e("CaptureTask", e);
				return null;
			} finally {
				if (reader != null) {
					try {
						reader.Close();
						log.d("fingerprint reader closed");
					} catch (UareUException ex) {
						log.e("capture-close", ex);
					}
				}
			}
		}

		protected void onPostExecute(IQuery result) {
			if (listener != null && result != null) {
				listener.onQueryDiscovered(result);
			}
			disable();
		}
	}

	public void enable(QueryListener listener) {
		if (this.isRunning == true)
			return;

		disableCamera();
		this.isRunning = true;
		this.listener = listener;

		getReaders();
		new CaptureTask().execute();
	}

	public void disable() {
		if (this.isRunning == false)
			return;

		this.isRunning = false;
		this.listener = null;
		enableCamera();

		try {
			if (this.reader != null)
				this.reader.CancelCapture();
		} catch (Exception ex) {
			log.e("disable", ex);
		}
	}

	public boolean isEnabled() {
		return this.isRunning && this.listener != null;
	}

	private IQuery capture() throws UareUException {

		try {
			log.d("capturing");

			while (isRunning) {
				if (engine == null || reader == null)
					return null;

				CaptureResult res = reader.Capture(Format.ANSI_381_2004,
						ImageProcessing.IMG_PROC_DEFAULT, 500, -1);

				if (res == null || res.image == null)
					continue;

				Fmd fmd = engine.CreateFmd(res.image, Fmd.Format.ANSI_378_2004);
				if (fmd == null)
					continue;

				return new FingerprintQuery(fmd.getData(), fmd.getData().length);
			}

		} catch (Exception ex) {
			log.e("capture", ex);
		}
		return null;
	}

	private void clearReader() throws UareUException {
		if (reader != null) {
			reader = null;
		}
	}

	// re-enable camera on exit
	public void enableCamera() {
		try {
			if (camera != null) {
				camera.release();
				camera = null;
			}
		} catch (Exception e) {
		}
	}

	// prevents gallery app from popping up whenever a finger is detected by the
	// fingerprint reader
	private void disableCamera() {
		try {
			if (camera == null) {
				for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
					CameraInfo camInfo = new CameraInfo();
					Camera.getCameraInfo(camNo, camInfo);

					if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT)) {
						camera = Camera.open(camNo);
					}
				}
				if (camera == null) {
					camera = Camera.open();
				}
			}
		} catch (Exception e) {

		}
	}

}

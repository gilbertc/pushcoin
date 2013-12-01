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
		disableCamera();
		log.d("disabled camera");
	}
	
	@Override
	public void finalize() throws Throwable
	{
		enableCamera();
		log.d("enabled camera");
		super.finalize();
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
				return;
			}

			if (readers.size() == 0) {
				log.d("no reader found");
				throw new Exception();
				// return;
			}

			engine = UareUGlobal.GetEngine();
			reader = readers.get(0);

			log.d("found readers: " + readers.size() + "; using "
					+ reader.GetDescription().serial_number);

		} catch (Exception ex) {
			log.e("getReaders", ex);
		}
	}

	private class CaptureTask extends AsyncTask<Reader, Void, IQuery> {
		protected IQuery doInBackground(Reader... params) {

			Reader thisReader = params[0];
			try {
				thisReader.Open(Priority.EXCLUSIVE);
				log.d("fingerprint reader opened");
				return capture(thisReader);
			} catch (UareUException e) {
				log.e("CaptureTask", e);
				return null;
			} finally {
				if (thisReader != null) {
					try {
						thisReader.Close();
						log.d("fingerprint reader closed");
					} catch (Exception ex) {
						log.e("CaptureTask-Close", ex);
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

		this.isRunning = true;
		this.listener = listener;

		getReaders();

		if (this.reader == null || this.engine == null) {
			log.d("no reader for capture");
		} else {
			new CaptureTask().execute(this.reader);
		}
	}

	public void disable() {
		if (this.isRunning == false)
			return;

		this.isRunning = false;
		this.listener = null;

		clearReader();
	}

	public boolean isEnabled() {
		return this.isRunning && this.listener != null;
	}

	private IQuery capture(Reader thisReader) throws UareUException {

		try {
			log.d("capturing");

			while (isRunning) {
				if (engine == null || thisReader == null)
					return null;

				CaptureResult res = thisReader.Capture(Format.ANSI_381_2004,
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

	private void clearReader() {
		if (reader != null) {
			try {
				reader.CancelCapture();
				reader.Close();
			} catch (UareUException ex) {
			}
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

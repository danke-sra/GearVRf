#if UNITY_ANDROID && !UNITY_EDITOR
#define KSENSOR_ENABLE
#endif

using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;

// Oculus Sensor
public class KSensor : SensorInterface {
#if KSENSOR_ENABLE
	public delegate void KSensorCallbackDelegate(float w, float x, float y, float z, long time);

	[DllImport ("GVRFPlugin")]
	private static extern void SetKSensorCallback(KSensorCallbackDelegate fp);

	[DllImport ("GVRFPlugin")]
	private static extern void KSensorStart();

	[DllImport ("GVRFPlugin")]
	private static extern void KSensorStop();
#endif

	private SensorListener mListener;

	public KSensor() {
#if KSENSOR_ENABLE
		SetKSensorCallback(new KSensorCallbackDelegate(this.callbackProc));
#endif
	}

	void callbackProc(float w, float x, float y, float z, long time) {
		// Callback received
		// Debug.LogFormat ("{0} {1} {2} {3}", w, x, y, z);
		if (mListener == null)
			return;

		mListener.onNewData(this, w, x, y, z, time);
	}

	public void setListener(SensorListener listener) {
		mListener = listener;
	}

	public void start() {
#if KSENSOR_ENABLE
		KSensorStart ();
#endif
	}

	public void stop() {
#if KSENSOR_ENABLE
		KSensorStop ();
#endif
	}
}

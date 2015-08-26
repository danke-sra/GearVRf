#if UNITY_ANDROID && !UNITY_EDITOR
#define ANDROID_ROTATION_SENSOR_ENABLE
#endif

using UnityEngine;
using System.Collections;

// Android Rotation Sensor
public class AndroidRotationSensor : SensorInterface {
	private SensorListener mListener;

	public void setListener(SensorListener listener) {
		mListener = listener;
	}

	public void start() {
	}

	public void stop() {
	}
}

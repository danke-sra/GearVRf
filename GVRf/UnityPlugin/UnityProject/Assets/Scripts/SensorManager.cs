using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class SensorManager : SensorListener {
	private static SensorManager sInstance;

	private List<SensorInterface> mSensors;

	private SensorManager() {
		// Initialize all sensors
		mSensors = new List<SensorInterface> ();

		// Add sensors in order of priorities. The first active sensor will be used.
		addSensor (new KSensor ());
		addSensor (new AndroidRotationSensor ());

		//!DEBUG
		GameObject.Find ("SensorText").SetActive (false);
	}

	private void addSensor(SensorInterface sensor) {
		mSensors.Add (sensor);
		sensor.setListener (this);

		sensor.start ();
	}

	public static SensorManager Instance
	{
		get 
		{
			if (sInstance == null)
			{
				sInstance = new SensorManager();
			}
			return sInstance;
		}
	}

	public void onConnected(SensorInterface sensor) {
	}

	public void onDisconnected(SensorInterface sensor) {
	}
	
	public void onNewData(SensorInterface sensor, float w, float x, float y, float z, long time) {
		//!DEBUG
		GameObject.Find ("SensorText").SetActive(true);

		Debug.LogFormat ("{0} {1} {2} {3}", w, x, y, z);
	}
}

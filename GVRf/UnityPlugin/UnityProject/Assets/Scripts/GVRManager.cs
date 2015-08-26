using UnityEngine;
using System.Collections;

public class GVRManager {
	private static GVRManager sInstance;

	private SensorManager mSensorManager;

	private GVRManager() {
		mSensorManager = SensorManager.Instance;
	}
	
	public static GVRManager Instance
	{
		get 
		{
			if (sInstance == null)
			{
				sInstance = new GVRManager();
			}
			return sInstance;
		}
	}

	public SensorManager getSensorManager() {
		return mSensorManager;
	}
}

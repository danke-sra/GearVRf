using UnityEngine;
using System.Collections;

public interface SensorListener {
	void onConnected(SensorInterface sensor);
	void onDisconnected(SensorInterface sensor);
	void onNewData(SensorInterface sensor, float w, float x, float y, float z, long time);
};

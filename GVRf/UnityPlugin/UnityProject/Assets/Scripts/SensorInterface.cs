using UnityEngine;
using System.Collections;

public interface SensorInterface {
	void setListener(SensorListener listener);
	void start();
	void stop();
}
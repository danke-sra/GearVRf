// on OpenGL ES there is no way to query texture extents from native texture id
#if UNITY_ANDROID && !UNITY_EDITOR
	#define UNITY_GLES_RENDERER
#endif

using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;

public class GVRCamera : MonoBehaviour {
	// Native plugin rendering events are only called if a plugin is used
	// by some script. This means we have to DllImport at least
	// one function in some active script.
	// For this example, we'll call into plugin's SetTimeFromUnity
	// function and pass the current time so the plugin can animate.

	[DllImport ("GVRFPlugin")]
	private static extern void SetTimeFromUnity(float t);

	// We'll also pass native pointer to a texture in Unity.
	[DllImport ("GVRFPlugin")]
#if UNITY_GLES_RENDERER
	private static extern void SetLeftTextureFromUnity(System.IntPtr texture, int id, int w, int h);
#else
	private static extern void SetLeftTextureFromUnity(System.IntPtr texture);
#endif

#if UNITY_GLES_RENDERER
	[DllImport ("GVRFPlugin")]
	private static extern void SetRightTextureFromUnity(System.IntPtr texture, int id, int w, int h);
#else
	// private static extern void SetRightTextureFromUnity(System.IntPtr texture);
#endif

	Camera cameraLeft;
	Camera cameraRight;

	Texture2D textureLeft;
	Texture2D textureRight;

	// Use this for initialization
	void Start () {
#if UNITY_ANDROID && !UNITY_EDITOR
		// Disable center camera on Android (it is used only for Editor mode)
		GameObject.Find ("GVRCameraCenter").SetActive (false);
#endif

		cameraLeft = GameObject.Find("GVRCameraLeft").GetComponent<Camera>();
		cameraRight = GameObject.Find("GVRCameraRight").GetComponent<Camera>();

		if (cameraLeft == null || cameraRight == null) {
			Debug.LogError ("Camera is null");
			return;
		}

		Debug.LogFormat ("CameraLeft = {0}, CameraRight = {1}", cameraLeft, cameraRight);

		RenderTexture texLeft = cameraLeft.targetTexture;
		RenderTexture texRight = cameraRight.targetTexture;
		if (texLeft == null || texRight == null) {
			Debug.Log("Camera render texture is null");
			return;
		}

		Debug.LogFormat ("texLeft = {0}, texRight = {1}", texLeft, texRight);

		textureLeft = new Texture2D (texLeft.width, texLeft.height, TextureFormat.ARGB32, false);
		textureLeft.filterMode = FilterMode.Point;
		textureLeft.Apply ();

		textureRight = new Texture2D (texRight.width, texRight.height, TextureFormat.ARGB32, false);
		textureRight.filterMode = FilterMode.Point;
		textureRight.Apply ();

		Debug.LogFormat ("textureLeftID = {0} ({1}x{2}), textureRightID = {3} ({4}x{5})", 
		                 textureLeft.GetNativeTextureID(),
		                 textureLeft.width, textureLeft.height,
		                 textureRight.GetNativeTextureID(),
		                 textureRight.width, textureRight.height);

#if UNITY_GLES_RENDERER
		SetLeftTextureFromUnity(textureLeft.GetNativeTexturePtr(), textureLeft.GetNativeTextureID(), textureLeft.width, textureLeft.height);
		SetRightTextureFromUnity(textureRight.GetNativeTexturePtr(), textureRight.GetNativeTextureID(), textureRight.width, textureRight.height);
#else
		// Empty
#endif
	}
	
	// Update is called once per frame
	void Update () {
	
	}

	void OnPostRender() {
		// Set time for the plugin
		// SetTimeFromUnity (Time.timeSinceLevelLoad);

		if (textureLeft == null || textureRight == null || cameraLeft == null || cameraRight == null) {
			return;
		}

/*
		Debug.LogFormat ("textureLeftID = {0} ({1}x{2}), textureRightID = {3} ({4}x{5})", 
		                 textureLeft.GetNativeTextureID(),
		                 textureLeft.width, textureLeft.height,
		                 textureRight.GetNativeTextureID(),
		                 textureRight.width, textureRight.height);
*/

		// Pass texture pointer to the plugin
#if UNITY_GLES_RENDERER
		SetLeftTextureFromUnity(textureLeft.GetNativeTexturePtr(), textureLeft.GetNativeTextureID(), textureLeft.width, textureLeft.height);
		SetRightTextureFromUnity(textureRight.GetNativeTexturePtr(), textureRight.GetNativeTextureID(), textureRight.width, textureRight.height);
#else
		// Empty
#endif

#if UNITY_GLES_RENDERER
		GetRTPixels(textureLeft, cameraLeft.targetTexture);
		GetRTPixels(textureRight, cameraRight.targetTexture);

		GL.IssuePluginEvent(1);
#endif
	}

	static public void GetRTPixels(Texture2D tex2d, RenderTexture rt) {	
		// Remember currently active render texture
		RenderTexture currentActiveRT = RenderTexture.active;
		
		// Set the supplied RenderTexture as the active one
		RenderTexture.active = rt;
		
		// Create a new Texture2D and read the RenderTexture image into it
		// Texture2D tex = new Texture2D(rt.width, rt.height);
		tex2d.ReadPixels(new Rect(0, 0, rt.width, rt.height), 0, 0);
		tex2d.Apply ();

		// Restorie previously active render texture
		RenderTexture.active = currentActiveRT;
	}
}
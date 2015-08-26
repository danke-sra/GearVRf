// Example low level rendering Unity plugin
// OpenGL ES implementation

#include "UnityPluginInterface.h"

#include <math.h>
#include <stdio.h>
#include <assert.h>

// --------------------------------------------------------------------------
// Include headers for the graphics APIs we support

#if UNITY_IPHONE
	#include <OpenGLES/ES2/gl.h>
#elif UNITY_ANDROID
	#include <GLES3/gl3.h>
#endif

#include <distortion/distorter.h>
#include <distortion/distortion_grid.h>
#include <sensor/ksensor/KSensorReader.h>

const int GRID_HORIZONTAL_COUNT = 50;
const int GRID_VERTICAL_COUNT = 50;
const float g_distortion_offset = 0.0;			// fake
const float metric_real_screen_width = 4;		// fake
const float metric_real_screen_height = 7;		// fake

// TODO: remove those if use distortion by Zemax
const float k0 = 1.0f;
const float k1 = 0.125f;
const float k2 = 0.125f;
const float k3 = 0.0f;
const float disparity = -.04;
const float distortion_scale = 0.9;

static float g_Time = 0.0f;
extern "C" void SetTimeFromUnity(float t)
{
	g_Time = t;
}

static void*	g_TexturePointerL	= 0;
static int		g_TextureIdL		= 0;
static int		g_TexWidthL			= 0;
static int		g_TexHeightL		= 0;
extern "C" void SetLeftTextureFromUnity(void* texturePtr, int id, int w, int h)
{
	g_TexturePointerL	= texturePtr;
	g_TextureIdL		= id;
	g_TexWidthL			= w;
	g_TexHeightL		= h;
}

static void*	g_TexturePointerR	= 0;
static int		g_TextureIdR		= 0;
static int		g_TexWidthR			= 0;
static int		g_TexHeightR		= 0;
extern "C" void SetRightTextureFromUnity(void* texturePtr, int id, int w, int h)
{
	g_TexturePointerR	= texturePtr;
	g_TextureIdR		= id;
	g_TexWidthR			= w;
	g_TexHeightR		= h;
}

static gvr::DistortionGrid g_distortGrid;
static gvr::Distorter g_distorter;

static gvr::KSensorCB g_cbKSensor;
static gvr::KSensorReader g_kSensorReader;

// --------------------------------------------------------------------------
// UnitySetGraphicsDevice

static int g_DeviceType = -1;

static void localKSensorCB(float w, float x, float y, float z, long long time) {
	if (g_cbKSensor) {
		g_cbKSensor(w, x, y, z, time);
	}
}

extern "C" void EXPORT_API UnitySetGraphicsDevice (void* device, int deviceType, int eventType)
{
	// Set device type to -1, i.e. "not recognized by our plugin"
	g_DeviceType = -1;

	if(deviceType == kGfxRendererOpenGLES20Mobile)
	{
		::printf("OpenGLES 2.0 device\n");
		g_DeviceType = deviceType;
	}
	else if(deviceType == kGfxRendererOpenGLES30)
	{
		::printf("OpenGLES 3.0 device\n");
		g_DeviceType = deviceType;
	}

	// Initialize distorter
	g_distortGrid.update(
			GRID_HORIZONTAL_COUNT, GRID_VERTICAL_COUNT,
			g_distortion_offset,
			metric_real_screen_width,
			metric_real_screen_height,
			k0, k1, k2, k3,
			disparity, distortion_scale
	);
}

// --------------------------------------------------------------------------
// SetDefaultGraphicsState
//
// Helper function to setup some "sane" graphics state. Rendering state
// upon call into our plugin can be almost completely arbitrary depending
// on what was rendered in Unity before.
// Before calling into the plugin, Unity will set shaders to null,
// and will unbind most of "current" objects (e.g. VBOs in OpenGL case).
//
// Here, we set culling off, lighting off, alpha blend & test off, Z
// comparison to less equal, and Z writes off.

static void SetDefaultGraphicsState ()
{
	// Unknown graphics device type? Do nothing.
	if(g_DeviceType == -1)
		return;

	glDisable(GL_CULL_FACE);
	glDisable(GL_BLEND);
	glDisable(GL_DEPTH_TEST);
}

// --------------------------------------------------------------------------
// UnityRenderEvent
// This will be called for GL.IssuePluginEvent script calls; eventID will
// be the integer passed to IssuePluginEvent. In this example, we just ignore
// that value.

extern "C" void EXPORT_API UnityRenderEvent (int eventID)
{
	// Unknown graphics device type? Do nothing.
	if(g_DeviceType == -1)
		return;

	// Actual functions defined below
	SetDefaultGraphicsState();

	switch (eventID) {
	case 1:
		if (g_TexturePointerL)
		{
			glViewport(0, 0, g_TexWidthL, g_TexHeightL);
			g_distorter.render(&g_distortGrid, g_TextureIdL);
		}

		if (g_TexturePointerR)
		{
			glViewport(g_TexWidthL, 0, g_TexWidthR, g_TexHeightR);
			g_distorter.render(&g_distortGrid, g_TextureIdR);
		}
		break;
	}
}

// --------------------------------------------------------------------------
// K-sensor API

extern "C" void EXPORT_API SetKSensorCallback(gvr::KSensorCB funcPtr)
{
	g_cbKSensor = funcPtr;
}

extern "C" void EXPORT_API KSensorStart()
{
	if (!g_cbKSensor)
		return;

	g_kSensorReader.start(g_cbKSensor);
}

extern "C" void EXPORT_API KSensorStop()
{
	g_kSensorReader.stop();
}

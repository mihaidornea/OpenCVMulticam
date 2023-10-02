# OpenCVMulticam

### Android application that renders the feeds from two cameras (one internal via Camera2 API, one external via UVCCamera) and processes them using OpenCV C++ and renders a cube on top of them using OpenGL ES C++

## Project Structure

# App

Application module that consists of the following packages:

- application: contains the application class and koin injection modules
- shared: contains the base classes for viewmodels and fragments, extensions and utilities
- presentation: Consists of the Main Activity and it's associated viewmodel and the camera fragment
  that uses two viewmodels. CameraViewModel handles internal camera operations and processing, while
  ExternalCameraViewModel handles the external camera operations. The external camera permission
  requests and basic commands are handled by CameraFragment() which from the AndroidUSBCamera
  library and is based on UVCCamera.

# App/cpp

C++ Files that are used for OpenCV processing and OpenGL rendering.

- native-lib.cpp implements JNI calls and operations needed for processing frames from the cameras
  using OpenCV: rotations and color space conversions.
- openGLJni.cpp implements the JNI calls required for rendering a cube with OpenGL which will then
  be displayed on a surface in the fragment
- renderer.cpp: implements the OpenGL rendering functionality and displays the result on a
  AndroidNativeWindow which is the SurfaceView passed from the CameraFragment 
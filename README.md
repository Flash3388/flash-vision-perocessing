# frcvision-rpi

Testing vision using the raspberry PI image for FRC

Using the example provided with the image, we set up a server which performs a simple processing on an image from a camera, and uploads it to the camera server.

# What does the code do?

The code will start a camera server, using pre-defined configuration, and run a vision processing thread on the first
defined camera.

The vision processing will take an image from the camera, grayscale it, and upload it to the camera server into a different stream.

# Configuration

The configuration is defined in `/boot/frc.json` by default, and contains the following information:

- team number
- status of the network tables instance used (for communicating with the robot - server or client)
- settings for cameras to display in the camera server

```JSON
{
	"team": <team number>,
	"ntmode": <"client" or "server", "client" if unspecified>
	"cameras": [
	   {
	       "name": <camera name>
	       "path": <path, e.g. "/dev/video0">
	       "pixel format": <"MJPEG", "YUYV", etc>   // optional
	       "width": <video mode width>              // optional
	       "height": <video mode height>            // optional
	       "fps": <video mode fps>                  // optional
	       "brightness": <percentage brightness>    // optional
	       "white balance": <"auto", "hold", value> // optional
	       "exposure": <"auto", "hold", value>      // optional
	       "properties": [                          // optional
	           {
	               "name": <property name>
	               "value": <property value>
	           }
	       ],
	       "fov": <field of view of the camera in radians>
	   }
	],
	"templateMatchingMethod": <value from the TemplateMatchingMethod enum, for example "SQDIFF_NORMED">,
	"visionTemplatePath": <full path to the template image>,
	"templateMatchingScaleFactor": <scale factor between the original image and the template, for example 2.0>
}
```

# Building

To build the code run the gradle wrapper: `./gradlew build` for UNIX systems and `gradlew.bat build` for Windows systems.

# Deploy

To deploy the code to the Raspberry PI, simple run `./deploy.sh`. This script will use `scp` to copy both the compiled code and the `runCamera` script.

# Running

Using `ssh` run the `runInteractive` script in `/home/pi`.
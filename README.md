# frcvision-rpi

Testing vision using the raspberry PI image for FRC

Using the example provided with the image, we set up a server which performs a simple processing on an image from a camera, and uploads it to the camera server.

# Building

To build the code run the gradle wrapper: `./gradlew build` for UNIX systems and `gradlew.bat build` for Windows systems.

# Deploy

To deploy the code to the Raspberry PI, simple run `./deploy.sh`. This script will use `scp` to copy both the compiled code and the `runCamera` script.

# Running

Using `ssh` run the `runInteractive` script in `/home/pi`.
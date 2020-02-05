#!/bin/sh

scp build/distribution/frcvision.zip flash@flash-jetson.local:/home/flash
scp runCamera pi@frcvision.local:/home/pi
ssh flash@flash-jetson.local rm -r /home/flash/frcvision && unzip /home/flash/frcvision.zip

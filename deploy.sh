#!/bin/sh

scp build/distribution/frcvision.zip flash@flash-jetson.local:/home/flash
scp runCamera flash@flash-jetson.local:/home/flash
ssh flash@flash-jetson.local rm -r /home/flash/frcvision && unzip /home/flash/frcvision.zip
ssh flash@flash-jetson.local chmod +x /home/flash/runCamera

#!/bin/bash

# WARNING!!! THIS SCRIPT REQUIRES INTERNET CONNECTION!!

echo ====================================================
echo Updating system
echo ====================================================

# install dependencies
sudo apt-get -y update && 
sudo apt-get -y dist-upgrade && 
sudo apt-get -y install oracle-java8-jdk wiringpi

echo ====================================================
echo Installing Pi4j
echo ====================================================

# install pi4j
curl -s get.pi4j.com | sudo bash

echo ====================================================
echo Downloading Minecraft Link
echo ====================================================

# we make install dir
sudo mkdir /usr/minecraftlink

# download Minecraft Link
sudo wget https://www.pylo.co/static/mcreator/link/pi/minecraft_link_1.0_pi.jar -P /usr/minecraftlink

echo ====================================================
echo Installing Minecraft Link service
echo ====================================================

# create service file
sudo su -c 'sudo cat >/etc/systemd/system/minecraftlink.service <<EOL
[Unit]
Description=Minecraft Link
After=network.target
 
[Service]
Type=simple
WorkingDirectory=/usr/minecraftlink
ExecStart=/bin/bash -c "java -jar /usr/minecraftlink/minecraft_link_1.0_pi.jar"
Restart=always
User=pi
 
[Install]
WantedBy=multi-user.target
EOL'

# activate the service
sudo chmod 644 /etc/systemd/system/minecraftlink.service
sudo systemctl daemon-reload
sudo systemctl enable minecraftlink.service
sudo systemctl start minecraftlink.service

# wait for the service to start

sleep 5

# check the status
sudo systemctl status minecraftlink.service

echo ====================================================
echo Minecraft Link was installed. Service is now running
echo and will be running by default in the startup.
echo
echo To check the service status, use this command:
echo
echo sudo systemctl status minecraftlink.service
echo
echo ====================================================
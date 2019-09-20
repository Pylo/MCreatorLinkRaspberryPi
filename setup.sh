#!/bin/bash

# Run the following line to setup MCreator Link Service on your Pi:
# curl -sL http://mcreator.net/linkpi | sudo bash

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
cd /tmp
sudo wget http://get.pi4j.com/download/pi4j-1.2-SNAPSHOT.deb
sudo dpkg -i pi4j-1.2-SNAPSHOT.deb

echo ====================================================
echo Downloading MCreator Link
echo ====================================================

# we make install dir
sudo mkdir /opt/mcreatorlink

# download MCreator Link
sudo wget -O /opt/mcreatorlink/mcreator_link_pi.jar https://github.com/Pylo/MCreatorLinkRaspberryPi/releases/download/1.1/minecraft-link-raspberrypi.jar

echo ====================================================
echo Installing MCreator Link service
echo ====================================================

# create service file
sudo su -c 'sudo cat >/etc/systemd/system/mcreatorlink.service <<EOL
[Unit]
Description=MCreator Link
After=network.target
 
[Service]
Type=simple
WorkingDirectory=/opt/mcreatorlink
ExecStart=/bin/bash -c "sudo java -cp '.:mcreator_link_pi.jar:/opt/pi4j/lib/*' net.mcreator.minecraft.link.raspberrypi.Service"
Restart=always
User=root
 
[Install]
WantedBy=multi-user.target
EOL'

# activate the service
sudo chmod 644 /etc/systemd/system/mcreatorlink.service
sudo systemctl daemon-reload
sudo systemctl enable mcreatorlink.service
sudo systemctl start mcreatorlink.service

# wait for the service to start

sleep 5

# check the status
sudo systemctl status mcreatorlink.service

echo ====================================================
echo MCreator Link was installed. Service is now running
echo and will be running by default in the startup.
echo
echo To check the service status, use this command:
echo
echo sudo systemctl status mcreatorlink.service
echo
echo ====================================================

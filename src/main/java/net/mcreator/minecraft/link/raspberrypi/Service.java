/*
 * Copyright 2018 Pylo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mcreator.minecraft.link.raspberrypi;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Service for all PI4J compatible devices
 */
public class Service {

	private static final Logger LOGGER = Logger.getLogger(Service.class.getName());

	static InetAddress link_api_address = null;

	static int refreshRate = 20;
	static boolean pollInputs = false;

	static RaspberryPiIO raspberryIO;

	static LinkNetworkInterface networkInterface;

	/**
	 * Main method to start the Minecraft Link Raspberry Pi Service
	 *
	 * @param args Right now, no arguments are accepted
	 */
	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$s] [%2$s] %5$s%6$s%n");
		try {
			LOGGER.info("Loading Raspberry Pi Pi4j Bridge");
			raspberryIO = new RaspberryPiIO();

			LOGGER.info("Loading network interface and joining multicast group");
			networkInterface = new LinkNetworkInterface();

			LOGGER.info("Staring input polling thread");
			new Thread(() -> {
				while (true) {
					try {
						if (pollInputs && link_api_address != null) {
							StringBuilder digitalStates = new StringBuilder("digrd:");
							for (int i = 0; i < raspberryIO.getDigitalPinCount(); i++) {
								digitalStates.append(raspberryIO.digitalRead(i));
							}
							digitalStates.append("\n");
							networkInterface.sendUDPPacket(digitalStates.toString(), link_api_address,
									LinkNetworkInterface.REMOTE_PORT);
						}
						Thread.sleep(refreshRate);
					} catch (InterruptedException e) {
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			LOGGER.info("Staring multicast listening loop");
			networkInterface.startMainLoop();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

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

import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.exception.UnsupportedPinPullResistanceException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

class CommandProcessor {

	private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class.getName());

	/**
	 * Call this method to process inbound command
	 *
	 * @param commandFull Full command String
	 * @param packet      Network packet that contained the command
	 * @throws UnknownHostException In case of connect command, this exception can be thrown if the host requesting the connection can not be resolved
	 */
	static void processCommand(String commandFull, DatagramPacket packet) throws UnknownHostException {
		LOGGER.info("Command in: " + commandFull);
		String[] fullData = commandFull.split("\\?");
		String command = fullData[0].trim();
		String data = null;
		if (fullData.length > 1)
			data = fullData[1].trim();
		switch (command) {
		case "ident":
			Service.networkInterface.sendUDPPacket(
					"tnedi:Minecraft Link (1.1);" + Service.raspberryIO.getBoard().name().replace("_", " ") + "; "
							+ Service.raspberryIO.getDigitalPinCount() + ";" + RaspberryPiIO.ANALOG_PIN_COUNT + "\n ",
					packet.getAddress(), packet.getPort());
			break;
		case "connect":
			Service.link_api_address = InetAddress.getByName(data);
			break;
		case "prate":
			if (data != null)
				Service.refreshRate = Integer.parseInt(data);
			break;
		case "pstrt":
			Service.pollInputs = true;
			break;
		case "pstop":
			Service.pollInputs = false;
			break;
		case "msg":
			System.out.println("MSG:" + data);
			break;
		case "pinmd":
			if (data != null) {
				String[] params = data.split(":");
				if (params.length > 1) {
					int port = Integer.parseInt(params[0].trim());
					String pinmode = params[1].trim();
					switch (pinmode) {
					case "in":
						try {
							Service.raspberryIO.pinMode(port, PinMode.DIGITAL_INPUT, PinPullResistance.PULL_DOWN);
						} catch (UnsupportedPinPullResistanceException e) {
							try {
								Service.raspberryIO.pinMode(port, PinMode.DIGITAL_INPUT, PinPullResistance.OFF);
							} catch (Exception e2) {
								e.printStackTrace();
							}
						}
						break;
					case "in_p":
						try {
							Service.raspberryIO.pinMode(port, PinMode.DIGITAL_INPUT, PinPullResistance.PULL_UP);
						} catch (UnsupportedPinPullResistanceException e) {
							try {
								Service.raspberryIO.pinMode(port, PinMode.DIGITAL_INPUT, PinPullResistance.OFF);
							} catch (Exception e2) {
								e.printStackTrace();
							}
						}
						break;
					case "out":
						Service.raspberryIO.pinMode(port, PinMode.DIGITAL_OUTPUT, PinPullResistance.OFF);
						break;
					}
				}
			}
			break;
		case "diwrt":
			if (data != null) {
				String[] params = data.split(":");
				if (params.length > 1) {
					int port = Integer.parseInt(params[0].trim());
					byte value = Byte.parseByte(params[1].trim());
					Service.raspberryIO.digitalWrite(port, value);
				}
			}
			break;
		}
	}

}

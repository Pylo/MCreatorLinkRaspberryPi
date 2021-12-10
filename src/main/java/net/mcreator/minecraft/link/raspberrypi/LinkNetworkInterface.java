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
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;

class LinkNetworkInterface {

	private static final int LOCAL_PORT = 25563;
	static final int REMOTE_PORT = 25564;

	private static final Logger LOGGER = Logger.getLogger(LinkNetworkInterface.class.getName());

	private final MulticastSocket socket;

	/**
	 * This class takes care of the Link network communication
	 *
	 * @throws IOException If multicast setup or interface detection failed
	 */
	LinkNetworkInterface() throws IOException {
		socket = new MulticastSocket(LOCAL_PORT);
		socket.setBroadcast(true);

		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface iface = interfaces.nextElement();
			if (iface.isLoopback() || !iface.isUp())
				continue;

			Enumeration<InetAddress> addresses = iface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				if (addr.getAddress().length != 4)
					continue;

				socket.setInterface(addr);
				socket.joinGroup(InetAddress.getByName("224.0.2.63"));

				LOGGER.info("Added iface to listening group: " + addr);
			}
		}
	}

	/**
	 * This is blocking method that never stops. It polls for the inbound messages from the Link controller.
	 */
	void startMainLoop() {
		while (true) {
			try {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				CommandProcessor.processCommand(new String(buf).split("\n")[0].trim(), packet);
				Thread.sleep(Service.refreshRate);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Helper method to send UDP datagram
	 *
	 * @param data        Data of the datagram
	 * @param inetAddress Destination address
	 * @param port        Destination port
	 */
	void sendUDPPacket(String data, InetAddress inetAddress, int port) {
		if (inetAddress != null)
			try (DatagramSocket datagramSocket = new DatagramSocket()) {
				datagramSocket.send(new DatagramPacket(data.getBytes(), data.getBytes().length, inetAddress, port));
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}

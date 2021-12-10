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

import com.pi4j.io.gpio.*;
import com.pi4j.system.SystemInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class RaspberryPiIO {

	static final int ANALOG_PIN_COUNT = 0;

	private final SystemInfo.BoardType board;
	private final Map<Integer, GpioPinDigitalMultipurpose> multipurposePins = new HashMap<>();
	private final int digitalPinCount;

	/**
	 * Call this method to setup IO, count pins and map them
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	RaspberryPiIO() throws IOException, InterruptedException {
		GpioController gpio = GpioFactory.getInstance();

		Pin[] pins;

		// get a collection of raw pins based on the board type (model)
		this.board = SystemInfo.getBoardType();
		if (board == SystemInfo.BoardType.RaspberryPi_ComputeModule) {
			// get all pins for compute module
			pins = RCMPin.allPins();
		} else {
			// get exclusive set of pins based on RaspberryPi model (board type)
			pins = RaspiPin.allPins(board);
		}

		digitalPinCount = pins.length;

		for (Pin pin : pins) {
			GpioPinDigitalMultipurpose multipurposePin = gpio
					.provisionDigitalMultipurposePin(pin, PinMode.DIGITAL_INPUT);
			multipurposePin.setShutdownOptions(true);
			multipurposePins.put(pin.getAddress(), multipurposePin);
		}

	}

	/**
	 * Returns the number of digial pins on the Pi device
	 *
	 * @return Digital pin count
	 */
	int getDigitalPinCount() {
		return digitalPinCount;
	}

	/**
	 * Retruns the BoardType object
	 *
	 * @return BoardType object
	 */
	SystemInfo.BoardType getBoard() {
		return board;
	}

	/**
	 * Call this method to set the pin mode of the given pin
	 *
	 * @param pin               Pin to set the mode for
	 * @param mode              Pin mode
	 * @param pinPullResistance Pin pull resistance
	 */
	void pinMode(int pin, PinMode mode, PinPullResistance pinPullResistance) {
		if (multipurposePins.get(pin) != null) {
			multipurposePins.get(pin).setMode(mode);
			multipurposePins.get(pin).setPullResistance(pinPullResistance);
		}
	}

	/**
	 * Call this to set the pin digital value
	 *
	 * @param pin   Pin number
	 * @param value Value (should be 0 or 1)
	 */
	void digitalWrite(int pin, byte value) {
		if (multipurposePins.get(pin) != null)
			if (value == 0)
				multipurposePins.get(pin).low();
			else
				multipurposePins.get(pin).high();
	}

	/**
	 * Reads the given pin state (logic level)
	 *
	 * @param pin Pin to check
	 * @return 1 if pin is in high logic level state, 0 otherwise
	 */
	byte digitalRead(int pin) {
		if (multipurposePins.get(pin) != null)
			if (multipurposePins.get(pin).getState() == PinState.HIGH)
				return 1;
		return 0;

	}

}
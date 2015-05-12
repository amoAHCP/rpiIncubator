package ch.trivadis.com.sensors;

import java.io.IOException;

public class GroveSensors {

	private final GrovePi grovePi;

	public GroveSensors(GrovePi grovePi) {
		this.grovePi = grovePi;
	}

	// --- Factory methods for sensors

	public Ultrasonic createUltrasonic(int pin) throws IOException {
		return new Ultrasonic(grovePi.createI2cPin(pin));
	}

}

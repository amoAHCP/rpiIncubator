package ch.trivadis.com.sensors;

import java.io.IOException;

public class Ultrasonic {

	private static final int ULTRASONIC_READ_CMD = 7;

	private final I2cPin pin;

	public Ultrasonic(I2cPin pin) throws IOException {
		this.pin = pin;
	}

	public int getDistance() throws IOException {
		pin.writeCommand(ULTRASONIC_READ_CMD);
		pin.sleep(200);
		byte[] buffer = pin.read(3);
		return ((buffer[1] & 0xFF) << 8) + (buffer[2] & 0xFF);
	}

	public static void main(String[] args) throws IOException {
		// Ultrasonic connected to digital pin D4
		final Ultrasonic ultrasonic = new Ultrasonic(
				new GrovePi().createI2cPin(4));
		for (;;) {
			System.out.println("Distance: " + ultrasonic.getDistance());
		}

	}
}

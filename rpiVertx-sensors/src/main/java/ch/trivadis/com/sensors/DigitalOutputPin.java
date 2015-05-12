package ch.trivadis.com.sensors;



import java.io.IOException;

/**
 * A pin on the GrovePi board which will be used for writing digital values
 * only.
 * 
 * @author Johannes Bergmann
 */
public class DigitalOutputPin extends GrovePi.Pin {

	DigitalOutputPin(GrovePi grovePi, int pin) throws IOException {
		super(grovePi, pin);
		grovePi.setPinModeOutput(pin);
	}

	public void write(int value) throws IOException {
		grovePi.digitalWrite(pin, value);
	}
}

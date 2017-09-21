package org.dnd4d;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class LedController {
    private final GpioPinDigitalOutput pin;

    public LedController(Pin id, PinState initialState) {
        final GpioController gpioController = GpioFactory.getInstance();
        pin = gpioController.provisionDigitalOutputPin(id, "PinLED", initialState);
    }

    public void on() {
        pin.high();
    }

    public void off() {
        pin.low();
    }
}

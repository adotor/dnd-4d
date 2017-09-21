package org.dnd4d;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LedControllerTest {
    private LedController sut;

    @Before
    public void setUp() throws Exception {
        sut = new LedController(RaspiPin.GPIO_01, PinState.LOW);
    }

    // @Test Unable to test because the GPIO-Library needs native linked binaries.
    public void led_canBeTurnedOn() {

        sut.on();

        final GpioController gpioController = GpioFactory.getInstance();
        assertTrue(((GpioPinDigital) gpioController.getProvisionedPin(RaspiPin.GPIO_01)).isHigh());
    }
}
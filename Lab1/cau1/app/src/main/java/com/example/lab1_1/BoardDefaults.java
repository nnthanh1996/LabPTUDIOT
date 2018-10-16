/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lab1_1;

import android.os.Build;

@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_IMX6UL_PICO = "imx6ul_pico";
    private static final String DEVICE_IMX7D_PICO = "imx7d_pico";

    /**
     * Return the GPIO pin that the LED is connected on.
     * For example, on Intel Edison Arduino breakout, pin "IO13" is connected to an onboard LED
     * that turns on when the GPIO pin is HIGH, and off when low.
     */
    final class PIN{
        public final String n1;
        public final String n2;
        public final String n3;
        public PIN(String a, String b, String c){
            this.n1 = a;
            this.n2 = b;
            this.n3 = c;
        }
    }

    public static String[] getGPIOForLED() {
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                //PIN var = new PIN("5","6","13");
                //String a="BCM5";
                return new String[]{"BCM13","BCM6","BCM5"};
                //return "BCM6";
            case DEVICE_IMX6UL_PICO:
                //return "GPIO4_IO22";
            case DEVICE_IMX7D_PICO:
                //return "GPIO2_IO02";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}

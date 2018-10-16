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

package com.example.lab1_5;

import android.app.Activity;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

/**
 * Sample usage of the Gpio API that blinks an LED at a fixed interval defined in
 * {@link #INTERVAL_BETWEEN_BLINKS_MS}.
 *
 * Some boards, like Intel Edison, have onboard LEDs linked to specific GPIO pins.
 * The preferred GPIO pin to use on each board is in the {@link BoardDefaults} class.
 *
 */
public class BlinkActivity extends Activity {
    private static final String TAG = BlinkActivity.class.getSimpleName();
    private static int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private Handler mHandler = new Handler();
    private Gpio mLedGpio_R, mLedGpio_G, mLedGpio_B;
    //private boolean mLedState = false;
    private int flag = 0;
    // 0 - all led off, 1 - led red on, 2 - led green on, 3 - led blue on
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");

        try {
            //String pinName = BoardDefaults.getGPIOForLED();
            String[] pinName = BoardDefaults.getGPIOForLED();
            mLedGpio_R = PeripheralManager.getInstance().openGpio(pinName[0]);
            mLedGpio_R.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio_G = PeripheralManager.getInstance().openGpio(pinName[1]);
            mLedGpio_G.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio_B = PeripheralManager.getInstance().openGpio(pinName[2]);
            mLedGpio_B.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Log.i(TAG, "Start blinking LED RGB GPIO pin");
            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED RGB GPIO pin");
        try {
            mLedGpio_R.close();
            mLedGpio_G.close();
            mLedGpio_B.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio_R = null;
            mLedGpio_G = null;
            mLedGpio_B = null;
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (mLedGpio_R == null || mLedGpio_G == null || mLedGpio_B == null) {
                return;
            }
            try {
                // Toggle the GPIO state

                //mLedState = !mLedState;
                //mLedGpio.setValue(mLedState);
                if(flag == 0){ // R on
                    mLedGpio_R.setValue(true);
                    mLedGpio_G.setValue(false);
                    mLedGpio_B.setValue(false);
                    flag = flag + 1;
			        INTERVAL_BETWEEN_BLINKS_MS = 500;
                    Log.d(TAG, "R on " + INTERVAL_BETWEEN_BLINKS_MS);
                } else if(flag == 1){ // G on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(true);
                    mLedGpio_B.setValue(false);
                    flag = flag + 1;
			        INTERVAL_BETWEEN_BLINKS_MS = 2000;
                    Log.d(TAG, "G on " + INTERVAL_BETWEEN_BLINKS_MS);
                }else if(flag == 2){ // B on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(false);
                    mLedGpio_B.setValue(true);
                    flag = 0;
			        INTERVAL_BETWEEN_BLINKS_MS = 3000;
                    Log.d(TAG, "B on " + INTERVAL_BETWEEN_BLINKS_MS);
                }
                //Log.d(TAG, "State set to " + mLedState);

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}

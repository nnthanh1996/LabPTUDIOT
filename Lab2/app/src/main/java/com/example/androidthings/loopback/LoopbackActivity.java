/*
 * Copyright 2016 The Android Open Source Project
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

package com.example.androidthings.loopback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

/**
 * Example activity that provides a UART loopback on the
 * specified device. All data received at the specified
 * baud rate will be transferred back out the same UART.
 */
public class LoopbackActivity extends Activity {
    private static final String TAG = "LoopbackActivity";

    // UART Configuration Parameters
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;

    private static final int CHUNK_SIZE = 512;

    // Lab 1

    private static int INTERVAL_BETWEEN_BLINKS_MS_cau5 = 1000;

    private static final int INTERVAL_BETWEEN_BLINKS_MS = 3000;

    private Handler mHandler = new Handler();
    private Gpio mLedGpio_R, mLedGpio_G, mLedGpio_B;
    //private boolean mLedState = false;
    private int flag = 1;

    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 100;
    private static final double PULSE_PERIOD_MS = 0.1;

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 1;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 100;

    private Pwm mPwm;
    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;

    // Lab2
    private HandlerThread mInputThread;
    private Handler mInputHandler;

    private UartDevice mLoopbackDevice;

    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            transferUartData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Loopback Created");



        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        // Attempt to access the UART device
        try {

            String pinNamePWM = BoardDefaults.getPWMPort();
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            mPwm = PeripheralManager.getInstance().openPwm(pinNamePWM);
            // Always set frequency and initial duty cycle before enabling PWM
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);

            //Lab1

            String[] pinName = BoardDefaults.getGPIOForLED();
            mLedGpio_R = PeripheralManager.getInstance().openGpio(pinName[0]);
            mLedGpio_R.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio_G = PeripheralManager.getInstance().openGpio(pinName[1]);
            mLedGpio_G.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio_B = PeripheralManager.getInstance().openGpio(pinName[2]);
            mLedGpio_B.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            openUart(BoardDefaults.getUartName(), BAUD_RATE);
            // Read any initially buffered data
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        // Close the PWM port.
        Log.i(TAG, "Closing port");
        try {
            mPwm.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mPwm = null;
        }

        Log.d(TAG, "Loopback Destroyed");

        // Terminate the worker thread
        if (mInputThread != null) {
            mInputThread.quitSafely();
        }

        // Attempt to close the UART device
        try {
            closeUart();
        } catch (IOException e) {
            Log.e(TAG, "Error closing UART device:", e);
        }
    }

    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Queue up a data transfer
            transferUartData();
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    /* Private Helper Methods */

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     *                 such as 9600, 19200, 38400, 57600, 115200, etc.
     *
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name, int baudRate) throws IOException {
        mLoopbackDevice = PeripheralManager.getInstance().openUartDevice(name);
        // Configure the UART
        mLoopbackDevice.setBaudrate(baudRate);
        mLoopbackDevice.setDataSize(DATA_BITS);
        mLoopbackDevice.setParity(UartDevice.PARITY_NONE);
        mLoopbackDevice.setStopBits(STOP_BITS);

        mLoopbackDevice.registerUartDeviceCallback(mInputHandler, mCallback);
    }

    /**
     * Close the UART device connection, if it exists
     */
    private void closeUart() throws IOException {
        if (mLoopbackDevice != null) {
            mLoopbackDevice.unregisterUartDeviceCallback(mCallback);
            try {
                mLoopbackDevice.close();
            } finally {
                mLoopbackDevice = null;
            }
        }
    }

    /**
     * Loop over the contents of the UART RX buffer, transferring each
     * one back to the TX buffer to create a loopback service.
     *
     * Potentially long-running operation. Call from a worker thread.
     */



    private void transferUartData() {
        if (mLoopbackDevice != null) {
            // Loop until there is no more data in the RX buffer.
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                //char c = Character.toUpperCase((char)key);
                while ((read = mLoopbackDevice.read(buffer, buffer.length)) > 0) {
                    int data = (int) buffer[0];
                    String key = String.valueOf((char)data);
                    mLoopbackDevice.write(buffer, read);
                    switch(key) {
                        case "0":
                            Log.d(TAG, key);
                            break;
                        case "1":
                            Log.d(TAG, key);
                            mHandler.removeCallbacks(mBlinkRunnable);
                            mHandler.removeCallbacks(mChangePWMRunnable2);
                            mHandler.removeCallbacks(mChangePWMRunnable);
                            mHandler.removeCallbacks(mBlinkRunnable2);
                            mHandler.post(mBlinkRunnable1);
                            break;
                        case "2":
                            Log.d(TAG, key);
                            mHandler.removeCallbacks(mBlinkRunnable);
                            mHandler.removeCallbacks(mChangePWMRunnable2);
                            mHandler.removeCallbacks(mChangePWMRunnable);
                            mHandler.removeCallbacks(mBlinkRunnable1);
                            mHandler.post(mBlinkRunnable2);
                            break;
                        case "3":
                            Log.d(TAG, key);
                            mHandler.removeCallbacks(mBlinkRunnable);
                            mHandler.removeCallbacks(mChangePWMRunnable2);
                            mHandler.removeCallbacks(mBlinkRunnable2);
                            mHandler.removeCallbacks(mBlinkRunnable1);
                            mHandler.post(mChangePWMRunnable);
                            break;
                        case "4":
                            Log.d(TAG, key);
                            mHandler.removeCallbacks(mBlinkRunnable);
                            mHandler.removeCallbacks(mChangePWMRunnable);
                            mHandler.removeCallbacks(mBlinkRunnable2);
                            mHandler.removeCallbacks(mBlinkRunnable1);
                            mHandler.post(mChangePWMRunnable2);
                            break;
                        case "5":
                            Log.d(TAG, key);
                            mHandler.removeCallbacks(mChangePWMRunnable2);
                            mHandler.removeCallbacks(mChangePWMRunnable);
                            mHandler.removeCallbacks(mBlinkRunnable2);
                            mHandler.removeCallbacks(mBlinkRunnable1);
                            mHandler.post(mBlinkRunnable);
                            break;
                        case "F":
                            Log.d(TAG, key);
                            mHandler.removeCallbacks(mChangePWMRunnable2);
                            mHandler.removeCallbacks(mChangePWMRunnable);
                            mHandler.removeCallbacks(mBlinkRunnable2);
                            mHandler.removeCallbacks(mBlinkRunnable1);
                            mHandler.removeCallbacks(mBlinkRunnable);
                            break;
                        default:

                    }
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
        }
    }

    private Runnable mBlinkRunnable1 = new Runnable() {
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
                    Log.d(TAG, "R on");
                } else if(flag == 1){ // G on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(true);
                    mLedGpio_B.setValue(false);
                    flag = flag + 1;
                    Log.d(TAG, "G on");
                }else if(flag == 2){ // B on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(false);
                    mLedGpio_B.setValue(true);
                    flag = 0;
                    Log.d(TAG, "B on");
                }
                //Log.d(TAG, "State set to " + mLedState);

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable1, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable mBlinkRunnable2 = new Runnable() {
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

                    //==== Khong co button nen test tam vay.
                    //if (INTERVAL_BETWEEN_BLINKS_MS < 2000) {
                    //    INTERVAL_BETWEEN_BLINKS_MS = INTERVAL_BETWEEN_BLINKS_MS + 500;
                    //} else {
                    //    INTERVAL_BETWEEN_BLINKS_MS = 500;
                    //}
                    //==== END

                    Log.d(TAG, "R on" + INTERVAL_BETWEEN_BLINKS_MS);
                } else if(flag == 1){ // G on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(true);
                    mLedGpio_B.setValue(false);
                    flag = flag + 1;
                    Log.d(TAG, "G on" + INTERVAL_BETWEEN_BLINKS_MS);
                }else if(flag == 2){ // B on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(false);
                    mLedGpio_B.setValue(true);
                    flag = 0;
                    Log.d(TAG, "B on" + INTERVAL_BETWEEN_BLINKS_MS);
                }
                //Log.d(TAG, "State set to " + mLedState);

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable2, INTERVAL_BETWEEN_BLINKS_MS);

            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable mChangePWMRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the port is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            // Change the duration of the active PWM pulse, but keep it between the minimum and
            // maximum limits.
            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
            // will bounce from MIN to MAX.

            mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            if(mActivePulseDuration>=MAX_ACTIVE_PULSE_DURATION_MS){
                mActivePulseDuration=MIN_ACTIVE_PULSE_DURATION_MS;
                //=== Set led
                try{
                    if(flag == 0){ // R on
                        mLedGpio_R.setValue(true);
                        mLedGpio_G.setValue(false);
                        mLedGpio_B.setValue(false);
                        flag = flag + 1;
                        Log.d(TAG, "R on");
                    } else if(flag == 1){ // G on
                        mLedGpio_R.setValue(false);
                        mLedGpio_G.setValue(true);
                        mLedGpio_B.setValue(false);
                        flag = flag + 1;
                        Log.d(TAG, "G on");
                    }else if(flag == 2){ // B on
                        mLedGpio_R.setValue(false);
                        mLedGpio_G.setValue(false);
                        mLedGpio_B.setValue(true);
                        flag = 0;
                        Log.d(TAG, "B on");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error on PeripheralIO API", e);
                }
            }
            // Bounce mActivePulseDuration back from the limits
            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

            try {
                // Duty cycle is the percentage of active (on) pulse over the total duration of the
                // PWM pulse
                mPwm.setPwmDutyCycle(mActivePulseDuration);
                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable mChangePWMRunnable2 = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the port is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            // Change the duration of the active PWM pulse, but keep it between the minimum and
            // maximum limits.
            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
            // will bounce from MIN to MAX.
//            if (mIsPulseIncreasing) {
//                mActivePulseDuration += 2;
//            } else {
//                mActivePulseDuration -= 2;
//            }
            mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            if(mActivePulseDuration>=MAX_ACTIVE_PULSE_DURATION_MS){
                mActivePulseDuration=MIN_ACTIVE_PULSE_DURATION_MS;
                //=== Set led
                try{
                    if(flag == 0){ // R on
                        mLedGpio_R.setValue(true);
                        mLedGpio_G.setValue(false);
                        mLedGpio_B.setValue(false);
                        flag = flag + 1;
                        Log.d(TAG, "R on");
                    } else if(flag == 1){ // G on
                        mLedGpio_R.setValue(false);
                        mLedGpio_G.setValue(true);
                        mLedGpio_B.setValue(false);
                        flag = flag + 1;
                        Log.d(TAG, "G on");
                    }else if(flag == 2){ // B on
                        mLedGpio_R.setValue(false);
                        mLedGpio_G.setValue(false);
                        mLedGpio_B.setValue(true);
                        flag = 0;
                        Log.d(TAG, "B on");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error on PeripheralIO API", e);
                }
            }
            // Bounce mActivePulseDuration back from the limits
            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

            try {
                // Duty cycle is the percentage of active (on) pulse over the total duration of the
                // PWM pulse
                mPwm.setPwmDutyCycle(mActivePulseDuration);
                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

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
                    INTERVAL_BETWEEN_BLINKS_MS_cau5 = 500;
                    Log.d(TAG, "R on " + INTERVAL_BETWEEN_BLINKS_MS);
                } else if(flag == 1){ // G on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(true);
                    mLedGpio_B.setValue(false);
                    flag = flag + 1;
                    INTERVAL_BETWEEN_BLINKS_MS_cau5 = 2000;
                    Log.d(TAG, "G on " + INTERVAL_BETWEEN_BLINKS_MS);
                }else if(flag == 2){ // B on
                    mLedGpio_R.setValue(false);
                    mLedGpio_G.setValue(false);
                    mLedGpio_B.setValue(true);
                    flag = 0;
                    INTERVAL_BETWEEN_BLINKS_MS_cau5 = 3000;
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

package com.example.alannguyen.cau3a;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Sample usage of the PWM API that changes the PWM pulse width at a fixed interval defined in
 * {@link #INTERVAL_BETWEEN_STEPS_MS}.
 *
 */
public class PwmActivity extends Activity {
    private static final String TAG = PwmActivity.class.getSimpleName();

    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 100;
    private static final double PULSE_PERIOD_MS = 0.1;

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 1;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 100;

    //led
    private Gpio mLedGpio_R, mLedGpio_G, mLedGpio_B;
    private int flag = 1;

    //
    private Handler mHandler = new Handler();
    private Pwm mPwm;
    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting PwmActivity");

        try {
            String pinName = BoardDefaults.getPWMPort();
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            mPwm = PeripheralManager.getInstance().openPwm(pinName);
            // Always set frequency and initial duty cycle before enabling PWM
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);
            // Post a Runnable that continuously change PWM pulse width, effectively changing the
            // servo position
            //String pinName = BoardDefaults.getGPIOForLED();
            String[] pinNameled = BoardDefaults.getGPIOForLED();
            mLedGpio_R = PeripheralManager.getInstance().openGpio(pinNameled[0]);
            mLedGpio_R.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpio_G = PeripheralManager.getInstance().openGpio(pinNameled[1]);
            mLedGpio_G.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio_B = PeripheralManager.getInstance().openGpio(pinNameled[2]);
            mLedGpio_B.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            //=====
            Log.d(TAG, "Start changing PWM pulse");
            mHandler.post(mChangePWMRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending Runnable from the handler.
        mHandler.removeCallbacks(mChangePWMRunnable);
        // Close the PWM port.
        Log.i(TAG, "Closing port");
        try {
            mPwm.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mPwm = null;
        }
    }

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

}
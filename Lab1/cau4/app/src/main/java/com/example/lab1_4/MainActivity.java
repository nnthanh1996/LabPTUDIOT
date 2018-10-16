package com.example.lab1_4;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import android.util.Log;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Gpio blueIO, greenIO, redIO;
    private Gpio mButtonGpio;
    private static final String pinName="BCM21";
    private static int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.i(TAG, "Starting MainActivity");
        try {
            //LED
            blueIO = PeripheralManager.getInstance().openGpio("BCM5");
            blueIO.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            greenIO = PeripheralManager.getInstance().openGpio("BCM6");
            greenIO.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            redIO = PeripheralManager.getInstance().openGpio("BCM13");
            redIO.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            redIO.setValue(false);
            blueIO.setValue(false);
            greenIO.setValue(false);

            //Button
            //pinName = BoardDefaults.getGPIOForButton();
            mButtonGpio = PeripheralManager.getInstance().openGpio(pinName);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio button) {
                    Log.i(TAG, "GPIO changed, button pressed");
                    // Return true to continue listening to events
                    try {
                        if (button.getValue()) {
                            // Pin is LOW - check lai
                            Log.i(TAG, pinName + ":low");
                            //Thao tac thay doi brightness
                            switch (flag){
                                case 0 :
                                    // Sua den R
                                    flag = flag +1;
                                    break;
                                case 1 :
                                    // Sua den G
                                    flag = flag +1;
                                    break;
                                case 2 :
                                    // Sua den B
                                    flag = flag +1;
                                    break;
                                case 3 :
                                    // Reset
                                    flag = 0;
                                    break;
                            }
                        }
//                        } else {
//                            // Pin is HIGH
//                            Log.i(TAG, pinName + ":high" );
//                            //Nhan nut la LED tắt
//
//                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    return true;
                }

                @Override
                public void onGpioError(Gpio gpio, int error) {
                    Log.w(TAG, gpio + ": Error event " + error);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mButtonGpio != null){
            Log.i(TAG, "Closing Button GPIO pin");
            try{
                mButtonGpio.close();
            }catch (IOException e){
                Log.e(TAG,"Error on PeriheralIO API", e);
            }finally {
                mButtonGpio = null;
            }
        }
    }
}

package com.example.sylvia.motomoddevelopment;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.motorola.mod.ModDevice;
import com.motorola.mod.ModManager;


public class Multimeter extends AppCompatActivity {

    Button Cbtn;
    Button Vbtn;
    Button Rbtn;
    Button Connectbtn;
    TextView Unit;
    TextView Note;

    int con = 0;
    int state = 0;
    int rawdata = 0;
    double reading = 0;
    int short_circuit = 0;
    private SoundPool soundPool;
    private int beep;
    private int stream;

    private static final int RAW_PERMISSION_REQUEST_CODE = 100;

    /**
     * Instance of MDK Personality Card interface
     */
    private Personality personality;

    /** Handler for events from mod device */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Personality.MSG_MOD_DEVICE:
                    /** Mod attach/detach */
                    ModDevice device = personality.getModDevice();
                    // onModDevice(device);
                    break;
                case Personality.MSG_RAW_DATA:
                    /** Mod raw data */
                    byte[] buff = (byte[]) msg.obj;
                    int length = msg.arg1;
                    onRawData(buff, length);
                    break;
                case Personality.MSG_RAW_IO_READY:
                    /** Mod RAW I/O ready to use */
                    onRawInterfaceReady();
                    break;
                case Personality.MSG_RAW_IO_EXCEPTION:
                    /** Mod RAW I/O exception */
                    // onIOException();
                    break;
                case Personality.MSG_RAW_REQUEST_PERMISSION:
                    /** Request grant RAW_PROTOCOL permission */
                    onRequestRawPermission();
                default:
                    // Log.i(Constants.TAG, "MainActivity - Un-handle events: " + msg.what);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter);

        Cbtn = (Button) findViewById(R.id.Cbtn);
        Vbtn = (Button) findViewById(R.id.Vbtn);
        Rbtn = (Button) findViewById(R.id.Rbtn);
        Connectbtn = (Button) findViewById(R.id.Connectbtn);
        Unit = (TextView) findViewById(R.id.Unit);
        Note = (TextView) findViewById(R.id.Note);

        final MediaPlayer Beep = MediaPlayer.create(this,R.raw.beep6);

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView Reading = (TextView) findViewById(R.id.Reading);
                                Reading.setText(String.valueOf(reading));
                                reading = reading +1;
                                if (state ==1){
                                    //reading = (0.2055 * rawdata - 0.0037)*1000;
                                }
                                else if (state ==2) {
                                    //reading = 11.1* rawdata - 0.2016;
                                }
                                else if (state ==3) {
                                    //reading = 3.3 * 1000 / rawdata - 1000;
                                }
                                else if (state ==4) {
                                    //reading = 3.3 * 1000 / rawdata - 1000;
                                   // if (reading < 0.01){
                                   //     Beep.start();
                                   // }
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();


        Cbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Unit.setText("mA");

                Cbtn.setBackgroundColor(getResources().getColor(R.color.Clicked));
                Vbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                Rbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                Connectbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));

                TextView Note = (TextView) findViewById(R.id.Note);
                Note.setText("Please note the current limit is 400mA and make sure that switch is at position C/V.");

                Connectbtn.setVisibility(View.GONE);

                state = 1;

             //   personality.getRaw().executeRaw(Constants.RAW_CMD_C);
            }
        });

        Vbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Unit.setText("mV");


                Cbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                Vbtn.setBackgroundColor(getResources().getColor(R.color.Clicked));
                Rbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                Connectbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));

                TextView Note = (TextView) findViewById(R.id.Note);
                Note.setText("Please note the voltage limit is 36VDC and make sure that switch is at position C/V.");

                Connectbtn.setVisibility(View.GONE);

                state = 2;

              //  personality.getRaw().executeRaw(Constants.RAW_CMD_V);

            }
        });

        Rbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Unit.setText("Ohm");

                Cbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                Vbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                Rbtn.setBackgroundColor(getResources().getColor(R.color.Clicked));


                Note.setText("Please make sure that switch is at position R.");

                Connectbtn.setVisibility(View.VISIBLE);

                state = 3;

               // personality.getRaw().executeRaw(Constants.RAW_CMD_R);
            }
        });

        Connectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button Connectbtn = (Button) findViewById(R.id.Connectbtn);
                if (con ==0) {
                    con=1;
                    Connectbtn.setBackgroundColor(getResources().getColor(R.color.Clicked));
                    state = 4;
                }
                else{
                    con=0;
                    Connectbtn.setBackgroundColor(getResources().getColor(R.color.Unclicked));
                    state = 3;
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        initPersonality();
        // personality.getRaw().executeRaw(Constants.RAW_CMD_START);
    }

    /** Got data from mod device RAW I/O */
    public void onRawData(byte[] buffer, int length) {
        /** Parse raw data to header and payload */
        int cmd = buffer[Constants.CMD_OFFSET] & ~Constants.TEMP_RAW_COMMAND_RESP_MASK & 0xFF;
        int payloadLength = buffer[Constants.SIZE_OFFSET];

        /** Checking the size of buffer we got to ensure sufficient bytes */
        if (payloadLength + Constants.CMD_LENGTH + Constants.SIZE_LENGTH != length) {
            return;
        }

        /** Parser payload data */
        byte[] payload = new byte[payloadLength];
        System.arraycopy(buffer, Constants.PAYLOAD_OFFSET, payload, 0, payloadLength);
        parseResponse(cmd, payloadLength, payload);
    }

    /** Parse the data from mod device */
    private void parseResponse(int cmd, int size, byte[] payload) {
        if (cmd == Constants.TEMP_RAW_COMMAND_INFO) {
            /** Got information data from personality board */

            /**
             * Checking the size of payload before parse it to ensure sufficient bytes.
             * Payload array shall at least include the command head data, and exactly
             * same as expected size.
             */
            if (payload == null
                    || payload.length != size
                    || payload.length < Constants.CMD_INFO_HEAD_SIZE) {
                return;
            }

            int version = payload[Constants.CMD_INFO_VERSION_OFFSET];
            int reserved = payload[Constants.CMD_INFO_RESERVED_OFFSET];
            int latencyLow = payload[Constants.CMD_INFO_LATENCYLOW_OFFSET] & 0xFF;
            int latencyHigh = payload[Constants.CMD_INFO_LATENCYHIGH_OFFSET] & 0xFF;
            int max_latency = latencyHigh << 8 | latencyLow;

            StringBuilder name = new StringBuilder();
            for (int i = Constants.CMD_INFO_NAME_OFFSET; i < size - Constants.CMD_INFO_HEAD_SIZE; i++) {
                if (payload[i] != 0) {
                    name.append((char) payload[i]);
                } else {
                    break;
                }
            }
            Log.i(Constants.TAG, "command: " + cmd
                    + " size: " + size
                    + " version: " + version
                    + " reserved: " + reserved
                    + " name: " + name.toString()
                    + " latency: " + max_latency);
        } else if (cmd == Constants.TEMP_RAW_COMMAND_DATA) {
            /** Got sensor data from personality board */

            /** Checking the size of payload before parse it to ensure sufficient bytes. */
            if (payload == null
                    || payload.length != size
                    || payload.length != Constants.CMD_DATA_SIZE) {
                return;
            }

            int dataLow = payload[Constants.CMD_DATA_LOWDATA_OFFSET] & 0xFF;
            int dataHigh = payload[Constants.CMD_DATA_HIGHDATA_OFFSET] & 0xFF;

            /** The raw temperature sensor data */
            int data = dataHigh << 8 | dataLow;

            /** The temperature */
            double temp = ((0 - 0.03) * data) + 128;


            Log.i(Constants.TAG, "temp: " + temp);

            rawdata = data;
        }
    }

    /** RAW I/O of attached mod device is ready to use */
    public void onRawInterfaceReady() {
        /**
         *  Personality has the RAW interface, query the information data via RAW command, the data
         *  will send back from MDK with flag TEMP_RAW_COMMAND_INFO and TEMP_RAW_COMMAND_CHALLENGE.
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                personality.getRaw().executeRaw(Constants.RAW_CMD_INFO);
            }
        }, 500);
    }

    /*
     * Beginning in Android 6.0 (API level 23), users grant permissions to apps while
     * the app is running, not when they install the app. App need check on and request
     * permission every time perform an operation.
     */
    public void onRequestRawPermission() {
        requestPermissions(new String[]{ModManager.PERMISSION_USE_RAW_PROTOCOL},
                RAW_PERMISSION_REQUEST_CODE);
    }

    /** Initial MDK Personality interface */
    private void initPersonality() {
        if (null == personality) {
            personality = new RawPersonality(this, Constants.VID_DEVELOPER, Constants.PID_DEVELOPER);
            personality.registerListener(handler);
        }
    }

    /** Clean up MDK Personality interface */
    private void releasePersonality() {

        /** Clean up MDK Personality interface */
        if (null != personality) {
            personality.getRaw().executeRaw(Constants.RAW_CMD_STOP);
            personality.onDestroy();
            personality = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releasePersonality();
    }

}

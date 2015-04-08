package camera.mah.com.camera;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.neurosky.thinkgear.*;

/**
 * Created by Muhamet Ademi on 2015-02-22.
 *
 * @class: CameraActivity.java
 * @author: Muhamet Ademi
 * @desc: Camera activity which is the main activity for the Camera application.
 */
public class CameraActivity extends ActionBarActivity {

    // Camera instance & variables
    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public boolean cameraOccupied = false;

    // GPS
    LocationManager locationManager;
    GpsLocationListener locationListener;
    Double lat, lng;

    // Bluetooth
    BluetoothAdapter bluetoothAdapter;
    TGDevice tgDevice;
    final boolean rawEnabled = false;

    // Sync states
    boolean isSynced = false;
    boolean connectionSuccess = false;
    int attention = 0;

    // User state
    String userId = "0";

    int testCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Add a click listener to the capture button
        FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
        frame.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       if (!locationListener.hasFoundLocation()) {
                            Toast.makeText(getApplicationContext(), "We have not fetched GPS yet. Please wait", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (cameraOccupied == false) {
                            cameraOccupied = true;
                            mCamera.takePicture(null, null, mPicture);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Upload still in progress", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Define a location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new GpsLocationListener();

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // Create an camera instance
        mCamera = getCameraInstance();

        // Create our preview view and set it as the contents of our view.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Define a bluetooth adapter and try to establish a connection
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            // Create the TG Device
            tgDevice = new TGDevice(bluetoothAdapter, handler);
        }

        // Show user id dialog
        promptUserSessionId();

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            Log.d("MINDKIT", "Connecting");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            Log.d("MINDKIT", "Connected");
                            Toast.makeText(getApplicationContext(), "Connected. Please wait until we filter out brainwave data.", Toast.LENGTH_LONG).show();
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            Log.d("MINDKIT", "Cant find");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            Log.d("MINDKIT", "Not paired");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            Log.d("MINDKIT", "Disconnected");
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    Log.d("MINDKIT", "Poor signal " + msg.arg1);
                    // tv.append("PoorSignal: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_DATA:
                    Log.d("MINDKIT", "Raw data " + msg.arg1);
                    //tv.append("Got raw: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_HEART_RATE:
                    Log.d("MINDKIT", "Heart rate " + msg.arg1);
                    // tv.append("Heart rate: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
                    Log.d("MINDKIT", "Attention " + msg.arg1);

                    // Check whether the brainwave data is successfully filtered
                    // and that the value is above 0 - furthermore, it must not have been synced
                    if (msg.arg1 > 0 && !isSynced) {

                        // Initial connection must be false, to continue with this code
                        if (!connectionSuccess) {
                            // Invert the state of connection success to determine connection state
                            connectionSuccess = !connectionSuccess;

                            // Prompt the user with a dialog with a small quiz to determine threshold
                            showBrainSyncDialog();

                            // Allow 30 seconds for the processing to occur and determine a threshold for the user
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("MINDKITSYNC", "Finished");
                                    isSynced = true;
                                }
                            }, 30000);

                        }

                        // Check whether the existing attention value is higher than the registered
                        // peak level stored in a global variable. Override, if higher.
                        if (msg.arg1 > attention) {
                            attention = msg.arg1;
                        }

                        // Log the attention peak
                        Log.d("MINDKITSYNC", "Attention peak is now " + attention);
                    }

                    // Store the value of the brainwave
                    float percent = (msg.arg1 / 100.0f);

                    if(testCounter == 0) {
                        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        new HttpBrainwaveAsyncTask().execute(String.valueOf(percent), userId, timeStamp);
                    }

                    testCounter ++;

                    if(testCounter == 5)
                    {
                        testCounter = 0;
                    }

                    // Send the brainwave value to the REST API
                    //String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    //new HttpBrainwaveAsyncTask().execute(String.valueOf(percent), userId, timeStamp);

                    // Check whether the current attention value is higher than the threshold defined
                    // Threshold is calculated by taking the peak with 10 deducted from the integer
                    // Furthermore, the device must have been synced / calibrated.
                    if (msg.arg1 > (attention) && isSynced) {
                        Log.d("MINDKIT", "Trigger Camera");
                        FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
                        frame.performClick();
                    }
                    // tv.append("Attention: " + msg.arg1 + "\n");
                    //Log.v("HelloA", "Attention: " + att + "\n");
                    break;
                case TGDevice.MSG_MEDITATION:
                    break;
                case TGDevice.MSG_BLINK:
                    Log.d("MINDKIT", " Blink " + msg.arg1);
                    // tv.append("Blink: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
                    //tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
                default:
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        tgDevice.close();
        super.onDestroy();
    }

    public void doStuff(View view) {
        if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            tgDevice.connect(rawEnabled);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_trigger) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {

        Camera c = null;

        try {
            // Retrieve a camera instance
            c = Camera.open();

            // Retrieve the camera default parameters
            Camera.Parameters param = c.getParameters();

            // Customize the picture quality
            param.setJpegQuality(50);

            // Retrieve a list of supported camera resolution
            List<Camera.Size> sizes = param.getSupportedPictureSizes();

            // Declare a temporary variable to hold resolution data
            Camera.Size cSize;

            for (Camera.Size size : sizes) {

                // Print out the supported screen resolutions
                Log.d("PICTURE", "Available resolution: "+size.width+" "+size.height);

                // Check whether 1280 by 720 is supported, if so, select
                if(size.width == 1280 && size.height == 720)
                {
                    param.setPictureSize(1280,720);
                }

            }

            // Assign the parameters to the camera
            c.setParameters(param);


        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }

        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("PictureCallback", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("PictureCallback", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("PictureCallback", "Error accessing file: " + e.getMessage());
            }

            try {
                ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, String.valueOf(locationListener.getLatitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, String.valueOf(locationListener.getLatitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, String.valueOf(locationListener.getLongitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, String.valueOf(locationListener.getLongitude()));
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Show toast with info about the new picture
            Toast.makeText(CameraActivity.this, "Picture taken\nTime: " + pictureFile.getName() + "\nLatitude: " + locationListener.getLatitude() + "\nLongitude: " + locationListener.getLongitude(), Toast.LENGTH_SHORT).show();

            // Start the camera preview again - to resume for further shots.
            camera.startPreview();

            // Convert the image to a base 64 string
            String imageString = Base64.encodeToString(data, Base64.DEFAULT);


            // Initialize a HTTP async task and transfer the data
            new HttpAsyncTask().execute(userId, pictureFile.getName(), imageString, String.valueOf(locationListener.getLatitude()), String.valueOf(locationListener.getLongitude()));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Adjust the state of the camera
                    cameraOccupied = false;                }
            }, 15000);
        }
    };

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // Ensure that SD card is mounted!!
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BrainCamera");

        // Create the directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("BrainCamera", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void appendLog(String text) {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void showBrainSyncDialog() {
        // Create a handler
        Handler dhandler = new Handler();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // Assign a title and an appropriate message
        alert.setTitle("User Calibration");
        alert.setMessage("Solve this task to determine a threshold\n(1423+684) * 2\nYou have 30 seconds to solve the problem");

        // Assign an input area to use for the answer
        final EditText input = new EditText(this);

        // Set the view for the alert dialog
        alert.setView(input);

        // Assign a button for the
        alert.setPositiveButton("Solved", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Log.d("MATH", "SOLVED for " + value);
                Toast.makeText(getApplicationContext(), "We have now synced and calibrated the device to suit your brain levels.", Toast.LENGTH_SHORT);
            }
        });


        AlertDialog dialog = alert.create();

        dialog.show();

        // Access the button and set it to invisible
        final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setVisibility(View.INVISIBLE);

        // Post the task to set it visible in 5000ms
        dhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(View.VISIBLE);
            }
        }, 30000);

    }

    private void promptUserSessionId() {

        // Declare an instance of AlertDialog.Builder
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // Assign a title and an appropriate message
        alert.setTitle("User Id");
        alert.setMessage("Please select an identifier for your user session:");

        // Assign an input area to use for the answer
        final EditText input = new EditText(this);

        // Set the view for the alert dialog
        alert.setView(input);

        // Assign a button for the
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userId = input.getText().toString();
            }
        });


        AlertDialog dialog = alert.create();

        dialog.show();

    }
}
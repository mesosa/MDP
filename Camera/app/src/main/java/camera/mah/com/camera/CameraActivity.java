package camera.mah.com.camera;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    // GPS
    LocationManager locationManager;
    LocationListener locationListener;
    Double lat, lng;

    // private Handler handler = new Handler();
    BluetoothAdapter bluetoothAdapter;
    TGDevice tgDevice;
    final boolean rawEnabled = false;

    // status

    boolean cameraOccupied = false;

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
                        if(cameraOccupied == false) {
                            cameraOccupied = true;
                            mCamera.takePicture(null, null, mPicture);
                        }
                    }
                }
        );

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                // Log for debugging purposes
                Log.d("Longitude:", longitude);
                Log.d("Latitude:", latitude);
                // Show toast on the main display with GPS position data
                //Toast.makeText(CameraActivity.this, "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // Create an camera instance
        mCamera = getCameraInstance();

        // Create our preview view and set it as the contents of our view.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        /*handler.postDelayed(runnable, 10000);*/

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }else {
            /* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
        }
    }

    /*
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
            frame.performClick();
            handler.postDelayed(this, 10000);
        }
    };
    */

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
//signal = msg.arg1;
                    Log.d("MINDKIT", "Poor signal " + msg.arg1);
                    // tv.append("PoorSignal: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_DATA:
//raw1 = msg.arg1;
                    Log.d("MINDKIT", "Raw data " + msg.arg1);
//tv.append("Got raw: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_HEART_RATE:
                    Log.d("MINDKIT", "Heart rate " + msg.arg1);
                    // tv.append("Heart rate: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
//att = msg.arg1;
                    Log.d("MINDKIT", "Attention " + msg.arg1);
                    if(msg.arg1 > 95)
                    {
                        Log.d("MINDKIT", "Trigger Camera");
                        FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
                        frame.performClick();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                            }
                        }, 5000);
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
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
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
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // Get location data
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
/*
            Log.d("GPSTEST", String.valueOf(location.getLongitude()));

            if (location == null) {
                lat = 13.6972 * 1E6;
                lng = 100.5150 * 1E6;
                Log.d("GPS data:", String.valueOf(lat) + " | " + String.valueOf(lng));
            } else { // otherwise, use the real location
                lat = location.getLatitude() * 1E6;
                lng = location.getLongitude() * 1E6;
                Log.d("GPS data:", String.valueOf(lat) + " | " + String.valueOf(lng));
            }
*/

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
/*
            try {
                ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, location.convert(location.getLatitude(), location.FORMAT_SECONDS));
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, location.convert(location.getLatitude(), location.FORMAT_SECONDS));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, location.convert(location.getLongitude(), location.FORMAT_SECONDS));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, location.convert(location.getLongitude(), location.FORMAT_SECONDS));
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Show toast with info about the new picture
            Toast.makeText(CameraActivity.this, "Picture taken\nTime: " + pictureFile.getName() + "\nLatitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
*/
            // Start the camera preview again - to resume for further shots.
            camera.startPreview();

            // Adjust the state of the camera
            cameraOccupied = false;


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

}

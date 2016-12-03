package com.se.developershub.flashlight;
/*
@Author Anthony Chan
17 November 2016
Goal of project: Help other aspiring Android Developers to learn how to connect an app to hardware within smartphone
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final int CAMERA_PERMISSION = 1001; //camera permission code
    boolean flashOn;
    String torchID;

    CameraManager cameraManager;
    Camera camera; //http://stackoverflow.com/questions/28065930/android-camera-android-hardware-camera-deprecated
    Camera.Parameters params;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     //savedInstanceState stores information user may have seen on latest usage
        setContentView(R.layout.activity_main); //setContentView connects this java class to the activity_main layout file
        Boolean hasFlash; //default to assume phone does not have flash
        flashOn = false; //initialize flashOn boolean

        /*
            API 6.0 permission checker
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
        /*
            Determine whether or not the device has flashlight functionality
         */
        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash){
            //The device cannot use this application.
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("There's a problem");
            alertDialog.setMessage("Unfortunately, this device does not support this flashlight!");
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.show();
        }
        getCamera();
        /*
            Below, we will declare a @Button widget, and a @Camera Instance Object.

            Widgets are View objects which users(you) interact with to alter and modify what is seen in the Activity.
            The CameraManager object is the connection between this application and the phone hardware, if available.
         */
        Button lightToggleBTN = (Button)findViewById(R.id.toggleBTN);
        /*
            When using 'things' outside of the scope of the android application, the application
            must ask for PERMISSION to use such.
            After Marshmallow 6.0, all permissions must be manually accepted by the user. pre-6.0,
            this is done automatically. For security reasons, it is better for users to manually
            accept permissions.
            GOTO app > manifests > AndroidManifest.xml to see how to ask for the CAMERA_SERVICE permission.
         */
        cameraManager = (CameraManager) getApplicationContext().getSystemService(CAMERA_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                String[] list = cameraManager.getCameraIdList();
                torchID = list[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        /*
            We need to do more than just declare a Button Widget to make it work. It needs to have a method which
            determines what will be done when it is 'clicked' or 'tapped' - a method that handles a user interaction.
            For this demo, we will use the onClickListener handler.
         */
        assert lightToggleBTN != null;
        lightToggleBTN.setOnClickListener(new View.OnClickListener() {
            /*
                Within this onClick() method, we can decide what happens when a user touches the Button.
                Give any of these commented-out codebits a try!
                //Toast.makeText(MainActivity.this, "Hello There!", Toast.LENGTH_SHORT).show();
                //lightToggleBTN.setText(getString(R.string.ex_click));
            */
            @Override
            public void onClick(View v) {
                ToggleFlashLight(flashOn);
            }
        });
    }
    /*
        Currently, This application only is compatible with API level 21+
        todo understand & implement pre-marshmallow compatibility for flashlight
     */
    @TargetApi(Build.VERSION_CODES.M)
    void ToggleFlashLight(boolean FlashLightIsOn){
        if(FlashLightIsOn){
            //Turn Flashlight Off
            try {
                cameraManager.setTorchMode(torchID, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            flashOn = false;
        }else{
            //Turn Flashlight On
            try {
                cameraManager.setTorchMode(torchID, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            flashOn = true;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please grant camera permission", Toast.LENGTH_SHORT).show();
                }
        }
    }
//////////////////////////////////////////////
    /*
        for lower API's than 21
     */
    public void getCamera(){
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Failed to Open", e.getMessage());
            }
        }
    }
    /*
	 * Turning On flash
	 */
    private void ToggleFlashLights(boolean FlashLightIsOn) {
        if (!FlashLightIsOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            flashOn = true;

        }else{
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            flashOn = false;


        }

    }

}
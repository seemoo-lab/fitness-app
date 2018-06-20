package seemoo.fitbit.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import seemoo.fitbit.R;

public abstract class RequestPermissionsActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_APP_SETTINGS = 1;

    protected void requestPermissionsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            } else {
                showDialogOnMissingPermission();
            }
            //If the location-permission was already granted, we want to check the External-Storage-Permission as well.
        } else {
            requestPermissionsExternalStorage();
        }
    }

    //Asks user for permissions: write to external storage

    protected void requestPermissionsExternalStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else{
                showDialogOnMissingPermission();
            }
        }
    }

    private void showDialogOnMissingPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.permission_Dialog_explanation)
                .setTitle(R.string.permission_Dialog_title);


        builder.setPositiveButton(R.string.permission_Dialog_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                goToSettings();
            }
        });
        builder.setNegativeButton(R.string.permission_Dialog_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * Bring the user directly to the app-settings to grant the permissions needed for the functionality
     */
    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }
}

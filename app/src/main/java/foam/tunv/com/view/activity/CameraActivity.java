package foam.tunv.com.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import foam.tunv.com.R;
import foam.tunv.com.databinding.ActivityCameraBinding;

import static foam.tunv.com.common.Constants.DATE_FORMAT_YYYYMMDD_HHMMSS;
import static foam.tunv.com.common.Constants.DIR_FOLDER_IMAGE;
import static foam.tunv.com.common.Constants.FILE_JPG;
import static foam.tunv.com.common.Constants.SDCARD_ROOT_APP;
import static foam.tunv.com.common.Constants.SHARED_PREFERENCES_CAMERA_PATH_FILE;


public class CameraActivity extends AppCompatActivity {

    public final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    public final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    public final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private final String TAG = MainActivity.class.getSimpleName();
    private ActivityCameraBinding mCameraBinding;
    private String mPathDir;
    private final int REQUEST_CAMERA_PERMISSION = 1;
    private final String FRAGMENT_DIALOG = "dialog";
    private int mCurrentFlash;
    private Handler mBackgroundHandler;
    private final int mSizeImage = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        //default
        mCameraBinding.viewCamera.setFlash(CameraView.FLASH_OFF);

        if (mCameraBinding.viewCamera != null) {
            mCameraBinding.viewCamera.addCallback(mCallback);
        }

        mCameraBinding.takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraBinding.viewCamera.takePicture();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraBinding.viewCamera.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mCameraBinding.viewCamera.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_flash:
                if (mCameraBinding.viewCamera != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraBinding.viewCamera.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                return true;
            case R.id.switch_camera:
                if (mCameraBinding.viewCamera != null) {
                    int facing = mCameraBinding.viewCamera.getFacing();
                    mCameraBinding.viewCamera.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(final CameraView cameraView, final byte[] data) {
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        new handlerImage(cameraView, data).execute();
                    } catch (Exception e) {
                        Log.w(TAG, "Cannot write to " + e);
                    }
                }
            });
        }
    };


    private class handlerImage extends AsyncTask<Void, Void, String> {
        private CameraView mCameraView;
        private byte[] mData;

        public handlerImage(CameraView cameraView, byte[] data) {
            this.mCameraView = cameraView;
            this.mData = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            String path = savePicture(mCameraView, mData);
            return path;
        }

        @Override
        protected void onPostExecute(String s) {
            //connect file to gallery
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{s}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i(TAG, "Scanned " + path);
                        }
                    });
            Intent nextScreen = new Intent(CameraActivity.this, MainActivity.class);
            nextScreen.putExtra(SHARED_PREFERENCES_CAMERA_PATH_FILE, s);
            setResult(Activity.RESULT_OK, nextScreen);
            finish();
        }
    }


    private String savePicture(final CameraView cameraView, final byte[] data) {
        String timeStamp = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_HHMMSS).format(new Date());
        File file = new File(SDCARD_ROOT_APP + DIR_FOLDER_IMAGE, timeStamp + FILE_JPG);

        Bitmap b = cutImage(data, cameraView);
        Bitmap sale = scaleDown(b, mSizeImage, true);
        File imgFile = new File(String.valueOf(file.getAbsoluteFile()));
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(imgFile);
            bos = new BufferedOutputStream(fos);
            sale.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception error) {
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
            }
        }
        return imgFile.getAbsolutePath();
    }


    private Bitmap cutImage(byte[] data, CameraView cameraView) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.reset();
        if (bitmap.getWidth() > bitmap.getHeight()) {
            matrix.postRotate(90);
        }
        Bitmap cutBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        int bitWidth = cutBitmap.getWidth();
        int bitHeight = cutBitmap.getHeight();
        // 3. Size of camera preview on screen
        int preWidth = cameraView.getWidth();
        int preHeight = cameraView.getHeight();

        int startx = cameraView.getXCropImage() * bitWidth / preWidth;
        int starty = cameraView.getYCropImage() * bitHeight / preHeight;

        int size = mSizeImage * bitWidth / preWidth;
        int endx = size;
        int endy = size;
        return Bitmap.createBitmap(cutBitmap, startx, starty, endx, endy);
    }

    public Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

}

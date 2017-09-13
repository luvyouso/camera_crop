package foam.tunv.com.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import foam.tunv.com.R;
import foam.tunv.com.common.Constants;
import foam.tunv.com.databinding.ActivityMainBinding;
import foam.tunv.com.utils.Utils;

import static foam.tunv.com.utils.Utils.hasSelfPermission;


public class MainActivity extends AppCompatActivity {

    private static final int OPEN_REQUEST_CODE = 41;
    private static final int CAMERA_REQUEST_CODE = 10;
    private final int PERMISSION_REQUEST_CODE = 9999999;
    private final String TAG = MainActivity.class.getSimpleName();
    private final String PNG = "png";
    private final String PNG_CAPITAL = "PNG";
    private final String JPG = "jpg";
    private final String JPG_CAPITAL = "JPG";
    private final String TYPE_DATA = "image/*";

    private ActivityMainBinding mMainBinding;
    private String mPathDir;
    private ArrayList<String> mPathOutputArrayList; //TODO handler note
    private ArrayList<Integer> mCountOutputArrayList; //TODO handler note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mPathOutputArrayList = new ArrayList<>();
        mCountOutputArrayList = new ArrayList<>();


        mMainBinding.btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainBinding.tvResult.setText("");
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        mMainBinding.btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainBinding.tvResult.setText("");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(mPathDir);
                intent.setDataAndType(uri, TYPE_DATA);
                startActivityForResult(intent, OPEN_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!activityRequestPermissions(PERMISSION_REQUEST_CODE)) {
            initFile();
        }

    }

    private boolean activityRequestPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = Utils.getSettingPermissions(this);
            boolean isRequestPermission = false;
            for (String permission : permissions) {
                if (!hasSelfPermission(this, permission)) {
                    isRequestPermission = true;
                    break;
                }
            }
            if (isRequestPermission) {
                requestPermissions(permissions.toArray(new String[0]), requestCode);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {

            // 許可されたパーミッションがあるかを確認する
            boolean isSomethingGranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    isSomethingGranted = true;
                    break;
                }
            }

            if (isSomethingGranted) {
                // 設定を変更してもらえた場合、処理を継続する
                initFile();

            } else {
                // 設定を変更してもらえなかった場合、終了
                Toast.makeText(this, "権限取得エラー", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String inputFile = null;
        mCountOutputArrayList.clear();
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    inputFile = data.getStringExtra(Constants.SHARED_PREFERENCES_CAMERA_PATH_FILE);
                    break;
                case OPEN_REQUEST_CODE:
                    if (data != null) {
                        try {
                            inputFile = Utils.getFilePath(this, data.getData());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
            if (inputFile != null && (inputFile.endsWith(JPG) || inputFile.endsWith(JPG_CAPITAL))) {
                Toast.makeText(this, inputFile, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "File no support, \nSupport file JPG", Toast.LENGTH_LONG).show();
            }
        } else { //TODO

        }
    }

    /**
     * Create folder
     */
    private void initFile() {
        File folder = new File(Constants.SDCARD_ROOT_APP);

        if (!folder.exists()) {
            folder.mkdirs();
        }
        mPathDir = folder.getAbsolutePath();
        File folderImage = new File(mPathDir + Constants.DIR_FOLDER_IMAGE);
        if (!folderImage.exists()) {
            folderImage.mkdirs();
        }

    }
}

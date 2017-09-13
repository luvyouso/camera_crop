package foam.tunv.com.common;


import android.os.Environment;

public class Constants {

    //param type itran.c
    public static final String ITRAN_PARAM_I = "-i";
    public static final String ITRAN_PARAM_F = "-f";
    public static final String ITRAN_PARAM_O = "-o";
    public static final String ITRAN_PARAM_B = "-b";
    public static final String ITRAN_PARAM_DIL = "dil";
    public static final String ITRAN_PARAM_ERO = "ero";
    public static final String ITRAN_PARAM_CNT = "cnt";
    public static final String ITRAN_PARAM_PPM = "-ppm";

    // KEYs
    public static final String SHARED_PREFERENCES_CAMERA_PATH_FILE = "briswell_com_foam_shared_preference_path_image_file";
    public static final String SHARED_PREFERENCES_CAMERA_PATH_FOLDER = "briswell_com_foam_shared_preference_path_folder";

    public static final String DIR_FOLDER_IMAGE = "/ImageFoam";
    public static final String DIR_FOLDER_APP_NAME = "/Foam";
    public static final String SDCARD_ROOT_APP = Environment.getExternalStorageDirectory().toString() + DIR_FOLDER_APP_NAME;

    //name file output
    public static final String FILE_S = "/s";
    public static final String FILE_PGM = ".pgm";
    public static final String FILE_PPM = ".ppm";
    public static final String FILE_JPG = ".jpg";
    public static final String FILE_S0 = SDCARD_ROOT_APP + "/s0.pgm"; //sdcard/Foam/s0.pgm
    //format date
    public static final String DATE_FORMAT_YYYYMMDD_HHMMSS = "yyyyMMdd_HHmmss";

    public static final int MAXWIDTH = 2560;
    public static final int MAXHEIGHT = 1920;
}

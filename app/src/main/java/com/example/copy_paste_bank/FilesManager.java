package com.example.copy_paste_bank;

import static android.service.controls.ControlsProviderService.TAG;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import android.Manifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class FilesManager extends MainActivity implements View.OnClickListener{
    private Context mContext;

    public static ImageView mImgPrev;
    public static Uri currUri;

    public FilesManager(Context mContext){
        this.mContext = mContext;
    }

    public void setImgPicker(ImageView mImg, View mView){
        mView.setTag("pick");
        mView.setOnClickListener(this);
        FilesManager.mImgPrev = mImg;
    }

    public String getImage(String sImage, ImageView mImgPrev) {
        if (!sImage.isEmpty()) {
            Uri mUri = Uri.fromFile(new File(sImage));
            try {
                if (isBlockedPath(this, sImage)) {
                    mImgPrev.setImageURI(mUri);
                    return  sImage;
                }
                else {
                    Log.d("PhotoPicker", "noooooo hayyyyyyyyyy: " + sImage);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sImage;
    }

    public String SavePhoto(Bitmap bmp, String fName, Uri oldFile, Context contex, ContentResolver resolver){

        //Creamos el directorio para los archivos
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/.accdata/");
        boolean isDiralloway = true;
        if(!path.exists()){
            isDiralloway = path.mkdir();
        }
        //------------------------------------------

        //Si se crea correctamente entonces procede a escribir
        if(isDiralloway) {
            File file = new File(path, fName);
            FileOutputStream stream = null;

//            Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy 11100------------------------: " );

            try {
                stream = new FileOutputStream(file);

                // Use the compress method on the BitMap object to write image to the OutputStream
                if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)){
                    throw new RuntimeException("Could Save Bit map");
                }
                else {
                    return file.getAbsolutePath();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    stream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

//    public File csvExport(List<String[]> list) throws IOException {
//        // Definimos la class
//        CsvWriterSimple write = new CsvWriterSimple();
//
//        //Creamos el directorio para los archivos
//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/.accdata/");
//        boolean isDiralloway = true;
//        if(!path.exists()){
//            isDiralloway = path.mkdir();
//        }
//        //------------------------------------------
//
//        //Si se crea correctamente entonces procede a escribir
//        if(isDiralloway) {
//            LocalDate currdate = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                currdate = LocalDate.now();
//            }
//            String name = (currdate == null? "CowData_Save.csv" : "RegistroDatos_"+currdate.toString()+".csv" );
//            File file = new File(path, name);
//            write.writeToCsvFile(list, file);
//            return file;
//        }
//        //-----------------------------------------------------------
//        return null;
//    }

    public boolean csvImport(String dir) throws IOException, CsvValidationException {
        Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy ------------------------: "+ dir );

        //Se detecta si el archivo existe
        File file = new File(dir);
        if(file.exists()){
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                //System.out.println(nextLine[0] + nextLine[1] + "etc...");
                Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy ------------------------: "+ nextLine[0] );
            }
        }
        //-----------------------------------------------------------
        return true;
    }

    public static void DeleteFile(File file) {

        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                File currFile = new File(file, children[i]);
                String name = currFile.getName();
                if(name.endsWith(".csv")) {
                    //Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy ------------------------: " + name);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        boolean threis = currFile.exists();
                        if(threis) {
                            currFile.delete();
                        }
                    }
                }
            }
        }
    }


    public void RemoveFile(String dir, ContentResolver resolver) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
           // Log.d("PhotoPicker", " =======================Aquiiiiiiiiii Hayyyyyy 11100------------------------: " );
            File file = new File(dir);
            boolean threis = file.exists();
            if(threis) {
                file.delete();
            }
        }
    }
    public boolean nameCompare(String a, String b) {
        // Paths that should rarely be exposed
        if (a.startsWith(b)){
            return true;
        }
        return false;
    }

    boolean isBlockedPath(Context ctx, String dir) {
        // Paths that should rarely be exposed
        return dir.startsWith("content://media/" + MediaStore.VOLUME_EXTERNAL_PRIMARY) || dir.startsWith("/storage/emulated/0/Documents/");
    }

    @Override
    public void onClick(View view) {
        Object itemTag = view.getTag();
        if (Objects.equals((String)itemTag, "pick")) {
            if (true){
                // Launch the photo picker and let the user choose only images.
                //fmang.FilesManager();
                pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
            }
            else{
                Basic.msg("Error Permiso Denegado!");
            }
        }
    }

    // Registers a photo picker activity launcher in single-select mode.
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    FilesManager.mImgPrev.setImageURI(uri);
                    FilesManager.currUri = uri;
                }
                else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

}

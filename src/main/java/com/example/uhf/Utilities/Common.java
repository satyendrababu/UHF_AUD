package com.example.uhf.Utilities;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import com.example.uhf.tools.MyContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by SATYENDRA on 3/30/2019.
 */

public class Common {

    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static String URL_CONTEXT;
    public static String userName = "";
    public static final String FILE_NAME = "IpConfig.txt";
    public static String USER_ID;

    public String readFile()
    {
        StringBuilder stringBuilder= new StringBuilder();
        File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);
        try {
            FileInputStream fin = new FileInputStream(getFilePath());
            InputStreamReader inputStreamReader = new InputStreamReader(fin);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);

            }
            Log.e("Unilever File"," "+line);
            fin.close();
            inputStreamReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();

    }
    /*public boolean saveFile(String value)
    {
        boolean val = false;
        File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(value.getBytes());
            fileOutputStream.close();
            val = true;
            Log.e("Unilever save file"," ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return val;
    }*/

    public boolean saveFile(String value)
    {
        boolean val = false;
        /*File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);

        if (!file.exists()) {
            file.mkdir();
        }*/
        try {
            //File gpxfile = new File(file, FILE_NAME);
            FileWriter writer = new FileWriter(getFilePath());
            writer.append(value);
            writer.flush();
            writer.close();
            val = true;
        } catch (Exception e) {
            Log.e("Unilever Exception","  "+e.getMessage());
        }
        return val;
    }
    private String getFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(MyContext.getContext());
        File fileDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(fileDir, FILE_NAME);
        return file.getPath();
    }

}

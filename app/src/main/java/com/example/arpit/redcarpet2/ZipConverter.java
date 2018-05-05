package com.example.arpit.redcarpet2;

/**
 * Created by Arpit on 05/01/2018.
 */
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipConverter {
    private static final int BUF_SIZE = 8192 ;
    private static String TAG= ZipConverter.class.getName().toString();
    private static String prnt_Path ="";


    public static boolean zip( String sourcePath, String destinationPath, String destinationFileName, Boolean includeParentFolder)  {
        new File(destinationPath ).mkdirs();
        FileOutputStream fileOutputStream ;
        ZipOutputStream zipOutputStream =  null;
        try{
            if (!destinationPath.endsWith("/")) destinationPath+="/";
            String dest = destinationPath + destinationFileName;
            File file = new File(dest);
            if (!file.exists()) file.createNewFile();

            fileOutputStream = new FileOutputStream(file);
            zipOutputStream =  new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

            if (includeParentFolder)
                prnt_Path=new File(sourcePath).getParent() + "/";
            else
                prnt_Path=sourcePath;

            zipFile(zipOutputStream, sourcePath);

        }
        catch (IOException ioe){
            Log.d(TAG,ioe.getMessage());
            return false;
        }finally {
            if(zipOutputStream!=null)
                try {
                    zipOutputStream.close();
                } catch(IOException e) {

                }
        }

        return true;

    }

    private static void zipFile(ZipOutputStream zipOutputStream, String sourcePath) throws  IOException{

        File files = new File(sourcePath);
        File[] fileList = files.listFiles();

        String entryPath="";
        BufferedInputStream input;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipFile(zipOutputStream, file.getPath());
            } else {
                byte data[] = new byte[BUF_SIZE];
                FileInputStream fileInputStream = new FileInputStream(file.getPath());
                input = new BufferedInputStream(fileInputStream, BUF_SIZE);
                entryPath=file.getAbsolutePath().replace( prnt_Path,"");

                ZipEntry entry = new ZipEntry(entryPath);
                zipOutputStream.putNextEntry(entry);

                int count;
                while ((count = input.read(data, 0, BUF_SIZE)) != -1) {
                    zipOutputStream.write(data, 0, count);
                }
                input.close();
            }
        }



    }

}

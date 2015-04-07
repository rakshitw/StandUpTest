package com.xrci.standup.utility;

import android.util.Log;

import com.xrci.standup.PostData;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;

/**
 * Created by q4KV89ZB on 03-04-2015.
 */
public class PhoneDetailFile {
    public  static String url = "http://64.49.234.131:8080/standup/rest/mobileMeticulousActivity/postActivityFile";
    public static void executeMultipartPost(String filePath) throws Exception {
        try {

            FileBody fileBody=null;
            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                System.out.println("file path:"+filePath);
                File file=new File(filePath);
                fileBody=new FileBody(file);


            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);

            // File file= new File("/mnt/sdcard/forest.png");
            // FileBody bin = new FileBody(file);
            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            if (fileBody != null) {
                reqEntity.addPart("file", fileBody);
                String response = PostData.postMultipart(url, reqEntity);
                Log.i("check", "response in phonedetail is " + response);
                if (!response.equals(PostData.EXCEPTION)
                        || !response.equals(PostData.INVALID_RESPONSE) || !response.equals(PostData.EXCEPTION)) {
                    file.delete();

                }

            }
        } catch (Exception e) {
            // handle exception here
            Log.i(e.getClass().getName(),"Exception in PhoneDetailFile" + e.getMessage());
        }
    }
}

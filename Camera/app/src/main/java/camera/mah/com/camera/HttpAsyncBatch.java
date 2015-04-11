package camera.mah.com.camera;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhamet Ademi on 2015-03-20.
 *
 * @class: HttpBrainwaveAsyncTask.java
 * @author: Muhamet Ademi
 * @desc: HttpBrainwaveAsyncTask which handles the HTTP communication to the REST API
 */
public class HttpAsyncBatch extends AsyncTask<String, Integer, Double> {

    @Override
    protected Double doInBackground(String... params) {

        // Forward the input params to the post method
        postData();

        return null;
    }

    protected void onPostExecute(Double result) {
        // TODO Auto-generated method stub
    }

    protected void onProgressUpdate(Integer... progress) {
        // TODO Auto-generated method stub
    }

    public String parseFile()
    {
        String res = "";
        File file = new File("sdcard/log.file");

        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(file));

            while ((sCurrentLine = br.readLine()) != null) {
                res += sCurrentLine + ";";
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null)br.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return res;
    }


    public void postData() {

        // Create a a new http client and a post request
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Config.getIP() + "?func=batch");

        try {
            // Declare a list of name and value pairs
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // Add POST items to the list
            nameValuePairs.add(new BasicNameValuePair("file", parseFile()));

            // Assign the list to the HTTP request body
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute the HTTP POST request
            HttpResponse response = httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }

}
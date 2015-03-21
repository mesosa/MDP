package camera.mah.com.camera;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhamet Ademi on 2015-03-20.
 *
 * @class: HttpAsyncTask.java
 * @author: Muhamet Ademi
 * @desc: HttpAsyncTask which handles the HTTP communication to the REST API
 */
public class HttpAsyncTask extends AsyncTask<String, Integer, Double> {

    @Override
    protected Double doInBackground(String... params) {

        // Forward the input params to the post method
        postData(params[0], params[1], params[2], params[3]);

        return null;
    }

    protected void onPostExecute(Double result) {
        // TODO Auto-generated method stub
    }

    protected void onProgressUpdate(Integer... progress) {
        // TODO Auto-generated method stub
    }


    public void postData(String filename, String image, String latitude, String longitude) {

        // Create a a new http client and a post request
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://192.168.1.191/index.php");

        try {

            // Declare a list of name and value pairs
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // Add POST items to the list
            nameValuePairs.add(new BasicNameValuePair("filename", filename));
            nameValuePairs.add(new BasicNameValuePair("image", image));
            nameValuePairs.add(new BasicNameValuePair("latitude", latitude));
            nameValuePairs.add(new BasicNameValuePair("longitude", longitude));

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
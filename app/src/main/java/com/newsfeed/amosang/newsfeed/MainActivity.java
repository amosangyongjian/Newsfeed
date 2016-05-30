package com.newsfeed.amosang.newsfeed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String jsonURL = "http://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=25&q=http://www.abc.net.au/news/feed/51120/rss.xml";

    private ArrayList<NewsObj>newsObjs = new ArrayList<>();
    private NewsAdapter na;
    private ListView lv;
    private ProgressDialog progressDialog;
    private ImageButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if(getSupportActionBar()!=null){
            setSupportActionBar(toolbar);
        }

        initializeUI();
    }

    private void initializeUI(){
        refreshButton = (ImageButton)findViewById(R.id.refresh);
        na = new NewsAdapter(new ArrayList<NewsObj>(),this);
        lv = (ListView)findViewById(R.id.lv_newslist);
        lv.setAdapter(na);
        //check if there is internet
        if(checkConnection()==true){
            String directory = getFilesDir().getAbsolutePath();
            String filename = "cache";
            fileDelete(directory,filename);
            new AsyncLoadData().execute();
        }else{
            try{
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(openFileInput("cache")));
                String in=null;
                while((in=inputReader.readLine())!=null){
                    String[] split = in.split(Pattern.quote("|"));
                    String title = split[0];
                    String thumburl = split[1];
                    String url = split[2];
                    String publishedDate = split[3];
                    NewsObj aObj = new NewsObj(title,thumburl,url,publishedDate);
                    newsObjs.add(aObj);
                }
                na.setNews(newsObjs);
                na.notifyDataSetChanged();
            }catch (IOException e){
                e.printStackTrace();
            }
        }


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncLoadData().execute();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String articleURL = newsObjs.get(position).getUrl();
                Bundle data = new Bundle();
                data.putString("url", articleURL);
                Intent webPage = new Intent();
                webPage.setClass(getApplicationContext(), WebPage.class);
                webPage.putExtras(data);
                startActivity(webPage);
//                Toast.makeText(MainActivity.this, articleURL, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //asynctask to load data
    private class AsyncLoadData extends AsyncTask<Void,Void,String>{

        @Override
        protected void onPostExecute(String jsonString) {
            super.onPostExecute(jsonString);
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            Log.d("RECEIVEJSONOBJECTLEVEL0", jsonString);
//            constructJSON(jsonString);
            na.setNews(constructJSON(jsonString));
            na.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Retrieving News");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String jsonString = getJSONString();
            return jsonString;
        }
    }

    public String getJSONString(){
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(jsonURL);
        try{
            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            //Log.d("RECEIVEJSONCODE",String.valueOf(statusCode));
            if(statusCode==200){
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream inputStream = httpEntity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line=bufferedReader.readLine())!=null){
                    stringBuilder.append(line);
                }
            }else{
                Log.e(getClass().getSimpleName(),"Download failed");
            }

        }catch (ClientProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    @Override
    public void onBackPressed() {

    }

    public List<NewsObj> constructJSON(String jsonIN){


        try{
            //add more levels to extract json
            JSONObject jsonObject1 = new JSONObject(jsonIN);
            String responseData = jsonObject1.getString("responseData");
//            Log.d("RECEIVEJSONOBJECTLEVEL1",responseData);

            JSONObject jsonObject2 = new JSONObject(responseData);
            String feed = jsonObject2.getString("feed");
//            Log.d("RECEIVEJSONOBJECTLEVEL2",feed);

            JSONObject jsonObject3 = new JSONObject(feed);
            String entries = jsonObject3.getString("entries");
//            Log.d("RECEIVEJSONOBJECTLEVEL3", entries);

            JSONArray jsonArray1 = new JSONArray(entries);
            for(int i=0; i<jsonArray1.length();i++){
                JSONObject mediaGroups = jsonArray1.getJSONObject(i);
                String mediaItems=null;

                if(mediaGroups.has("mediaGroups")){
                    mediaItems = mediaGroups.optString("mediaGroups");
                }

                String title = mediaGroups.getString("title");
                String url = mediaGroups.getString("link");
                String description = mediaGroups.getString("contentSnippet");
                String publishedDate = mediaGroups.getString("publishedDate");
                //Log.d("RECEIVEJSONOBJECTLEVEL4", mediaGroups.toString());
//                main information for news article

//                //for further thumbnail sizes
                JSONArray jsonArray2 = new JSONArray();
                if(mediaItems!=null){
                    jsonArray2 = new JSONArray(mediaItems);
                }

                for(int j=0;j<jsonArray2.length();j++){
                    JSONObject contents = jsonArray2.getJSONObject(j);
                    String contentItems = contents.getString("contents");
//                    Log.d("RECEIVEJSONOBJECTARRAY2",contentItems);

                    JSONArray jsonArray3 = new JSONArray(contentItems);
                    for(int k=0;k<jsonArray3.length();k++){
                        JSONObject items = jsonArray3.getJSONObject(k);
                        String imgurl = items.getString("url");
                        //Log.d("RECEIVEJSONOBJECTARRAY3",imgurl);
                        String thumbnails = items.getString("thumbnails");
                        if(k==2){
                            JSONArray jsonArray4 = new JSONArray(thumbnails);
                            for(int l=0;l<jsonArray4.length();l++){
                                JSONObject thumbnails1 = jsonArray4.getJSONObject(l);
                                String thumburl = items.getString("url");
//                                Log.d("RECEIVEJSONOBJECTTB", thumburl);
                                NewsObj newsObj = new NewsObj(title, thumburl,url, publishedDate);
                                cache(newsObj);
                                newsObjs.add(newsObj);

                            }
                        }
                    }
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
            Log.d("RECEIVEJSONERROR",e.toString());
        }
        return newsObjs;
    }

    private Boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        else{
            Toast.makeText(getApplicationContext(),"Newsfeed requires internet access in order to function",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void cache(NewsObj aObj){
        String title = aObj.getTitle();
        String thumburl = aObj.getImgurl();
        String url = aObj.getUrl();
        String publishedDate = aObj.getDate();
        String combined = title +"|"+thumburl+"|"+url+"|"+publishedDate+"\n";
        try{
            FileOutputStream fileOutputStream = openFileOutput("cache",MODE_APPEND);
            fileOutputStream.write(combined.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void fileDelete(String directory,String filename){
        File file = new File(directory,filename);
        if(file.exists()){
            file.delete();
        }else{
            Log.d("FDEMA", "File Doesn't Exist");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

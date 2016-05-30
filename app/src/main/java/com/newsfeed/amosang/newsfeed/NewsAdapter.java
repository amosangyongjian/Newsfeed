package com.newsfeed.amosang.newsfeed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by amosang on 27/05/16.
 */
public class NewsAdapter extends ArrayAdapter<NewsObj>{
    private List<NewsObj> news;
    private Context context;

    public NewsAdapter(List<NewsObj> news,Context context) {
        super(context, android.R.layout.simple_list_item_1,news);
        this.news = news;
        this.context = context;
    }

    public List<NewsObj> getNews() {
        return news;
    }

    public void setNews(List<NewsObj> news) {
        this.news = news;
    }

    public int getCount(){
        if(news!=null){
            return news.size();
        }
        return 0;
    }
    public NewsObj getItem(int position){
        if(news!=null){
            return news.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if(news!=null){
            return news.get(position).hashCode();
        }
        return 0;
    }

    static class DataHandler{
        ImageView newsIcon;
        TextView newsTitle;
        TextView newsDate;
        TextView imgURL;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        if(position==0){

            NewsObj currNews = news.get(position);
            DataHandler dh;
            if(convertView==null){
                LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.firstnews,parent,false);
                dh = new DataHandler();
                dh.newsTitle = (TextView)row.findViewById(R.id.newsTitle);
                dh.newsDate = (TextView)row.findViewById(R.id.newsDate);
                dh.newsIcon = (ImageView)row.findViewById(R.id.newsIcon);
                row.setTag(dh);
            }else{
                dh = (DataHandler)row.getTag();
            }
            NewsObj no = (NewsObj)this.getItem(position);
            row.setTag(R.id.newsIcon,row.findViewById(R.id.newsIcon));
            new AsyncDownloadTask().execute(row,no.getImgurl());
//            Picasso.with(context).load(no.getImgurl()).into(dh.newsIcon);
            dh.newsTitle.setText(no.getTitle());
            dh.newsDate.setText(no.getDate());

        }else{

            NewsObj currNews = news.get(position);
            DataHandler dh;
            if(convertView==null){
                LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.newslist,parent,false);
                dh = new DataHandler();
                dh.newsTitle = (TextView)row.findViewById(R.id.newsTitle);
                dh.newsDate = (TextView)row.findViewById(R.id.newsDate);
                dh.newsIcon = (ImageView)row.findViewById(R.id.newsIcon);
                row.setTag(dh);
            }else{
                dh = (DataHandler)row.getTag();
            }
            NewsObj no = (NewsObj)this.getItem(position);
            row.setTag(R.id.newsIcon, row.findViewById(R.id.newsIcon));
            new AsyncDownloadTask().execute(row,no.getImgurl());
//            Picasso.with(context).load(no.getImgurl()).into(dh.newsIcon);
            dh.newsTitle.setText(no.getTitle());
            dh.newsDate.setText(no.getDate());

        }
        return row;
    }

    public class NewsIcon{
        View view;
        NewsIcon(View view){
            this.view = view;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }
    }

    private class AsyncDownloadTask extends AsyncTask<Object, String, Bitmap>{

        private View view;
        private Bitmap bitmap = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap!=null&&view!=null){
                ImageView newsIcon = (ImageView)view.getTag(R.id.newsIcon);
                newsIcon.setImageBitmap(bitmap);
            }
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            view = (View)params[0];
            String uri = (String)params[1];
            try{
                InputStream inputStream = new URL(uri).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }catch (Exception e){
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}

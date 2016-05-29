package com.newsfeed.amosang.newsfeed;

/**
 * Created by amosang on 27/05/16.
 */
public class NewsObj {

    private String title;
    private String date;
    private String url;
    private String imgurl;

    public NewsObj(String title, String imgurl,String url,String date) {
        this.url = url;
        this.title = title;
        this.date = date;
        this.imgurl = imgurl;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

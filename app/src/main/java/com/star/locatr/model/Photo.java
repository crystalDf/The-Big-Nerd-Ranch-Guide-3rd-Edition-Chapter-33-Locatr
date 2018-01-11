package com.star.locatr.model;


import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("id")
    private String mId;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("url_s")
    private String mUrlS;

    @SerializedName("owner")
    private String mOwner;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUrlS() {
        return mUrlS;
    }

    public void setUrlS(String urlS) {
        mUrlS = urlS;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }
}

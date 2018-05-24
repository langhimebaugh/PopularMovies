package com.himebaugh.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoTrailer implements Parcelable {

    private String id;
    private String iso_639_1;
    private String iso_3166_1;
    private String key;
    private String name;
    private String site;
    private int size;
    private String type;

    public VideoTrailer() {
    }

    public VideoTrailer(String id, String iso6391, String iso31661, String key, String name, String site, int size, String type) {
        this.id = id;
        this.iso_639_1 = iso6391;
        this.iso_3166_1 = iso31661;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIso6391() {
        return iso_639_1;
    }

    public void setIso6391(String iso6391) {
        this.iso_639_1 = iso6391;
    }

    public String getIso31661() {
        return iso_3166_1;
    }

    public void setIso31661(String iso31661) {
        this.iso_3166_1 = iso31661;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    protected VideoTrailer(Parcel in) {
        id = in.readString();
        iso_639_1 = in.readString();
        iso_3166_1 = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        size = in.readInt();
        type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(iso_639_1);
        dest.writeString(iso_3166_1);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeInt(size);
        dest.writeString(type);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VideoTrailer> CREATOR = new Parcelable.Creator<VideoTrailer>() {
        @Override
        public VideoTrailer createFromParcel(Parcel in) {
            return new VideoTrailer(in);
        }

        @Override
        public VideoTrailer[] newArray(int size) {
            return new VideoTrailer[size];
        }
    };
}

package com.sdc.shareandcare;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

public class Post implements Parcelable {
    private String url, note, description , id;

    public Post() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    protected Post(Parcel in) {
        note = in.readString();
        url = in.readString();
        id = in.readString();
        description = in.readString(); // Added this line

    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(note);
        dest.writeString(id);
        dest.writeString(description);
    }
    public String getId() {
        return id;
    }
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    // Added method to get the Firebase key
    public String getFirebaseKey() {
        return id;
    }

    // Added 2 functions
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
package id.apwdevs.moviecatalogue.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ShortListModel implements Parcelable {
    public static final Parcelable.Creator<ShortListModel> CREATOR = new Parcelable.Creator<ShortListModel>() {
        @Override
        public ShortListModel createFromParcel(Parcel source) {
            return new ShortListModel(source);
        }

        @Override
        public ShortListModel[] newArray(int size) {
            return new ShortListModel[size];
        }
    };
    private int photoRes;
    private String overview;
    private String releaseDate;
    private String titleMovie;

    public ShortListModel() {
    }

    private ShortListModel(Parcel in) {
        this.photoRes = in.readInt();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.titleMovie = in.readString();
    }

    public int getPhotoRes() {
        return photoRes;
    }

    public void setPhotoRes(int photoRes) {
        this.photoRes = photoRes;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitleMovie() {
        return titleMovie;
    }

    public void setTitleMovie(String titleMovie) {
        this.titleMovie = titleMovie;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.photoRes);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeString(this.titleMovie);
    }
}

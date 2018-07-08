package xyz.alviksar.orchidarium.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Orchid implements Parcelable {
    private int id;
    private String code;
    private String name;
    private int age;
    private float potSizeInches;
    private double retailPrice;
    private String nicePhoto;
    private String[] realPhotos;
    private int isVisibleForSale;

    public Orchid() {
        setIsVisibleForSale(false);
    }

    public Orchid(String code, String name, int age, float potSize, double price) {
        this.code = code;
        this.name = name;
        this.age = age;
        this.potSizeInches = potSize;
        this.retailPrice = price;
    }

    // Constructor from Parcel
    private Orchid(Parcel in) {
        id = in.readInt();
        code = in.readString();
        name = in.readString();
        age = in.readInt();
        potSizeInches = in.readFloat();
        retailPrice = in.readDouble();
        nicePhoto = in.readString();
        in.readStringArray(realPhotos);
        isVisibleForSale = in.readInt();
    }

    public static final Creator<Orchid> CREATOR = new Creator<Orchid>() {
        @Override
        public Orchid createFromParcel(Parcel in) {
            return new Orchid(in);
        }

        @Override
        public Orchid[] newArray(int size) {
            return new Orchid[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getPotSizeInches() {
        return potSizeInches;
    }

    public void setPotSizeInches(float potSizeInches) {
        this.potSizeInches = potSizeInches;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getNicePhoto() {
        return nicePhoto;
    }

    public void setNicePhoto(String nicePhoto) {
        this.nicePhoto = nicePhoto;
    }

    public String[] getRealPhotos() {
        return realPhotos;
    }

    public void setRealPhotos(String[] realPhotos) {
        this.realPhotos = realPhotos;
    }

    public boolean getIsVisibleForSale() {
        return isVisibleForSale == 1;
    }

    public void setIsVisibleForSale(boolean state) {
        if (state)
            this.isVisibleForSale = 1;
        else
            this.isVisibleForSale = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeInt(age);
        parcel.writeFloat(potSizeInches);
        parcel.writeDouble(retailPrice);
        parcel.writeString(nicePhoto);
        parcel.writeStringArray(realPhotos);
        parcel.writeInt(isVisibleForSale);
    }


}

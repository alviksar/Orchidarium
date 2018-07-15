package xyz.alviksar.orchidarium.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class OrchidEntity implements Parcelable {

    // String to pass an orchid entity to activity as parcelable
    public static final String EXTRA_ORCHID = "extra_orchid";

    // Four ages of orchids
    public static final int AGE_TWO_YEARS_BEFORE = 3;
    public static final int AGE_ONE_YEARS_BEFORE = 2;
    public static final int AGE_FLOWERING = 1;
    public static final int AGE_BLOOMING = 0;
    public static final int AGE_UNKNOWN = -1;

    private String id;
    private String code;
    private String name;
    private int age;
    private String potSize;
    private double retailPrice;
    private String description;
    private String nicePhoto;
    private List<String> realPhotos;
    private int isVisibleForSale;
    // The time when an orchid was shown for order
    private long forSaleTime = 0;
    private String currencySymbol;

    public OrchidEntity() {
        realPhotos = new ArrayList<>();
        setIsVisibleForSale(false);
    }

    // Constructor from Parcel
    private OrchidEntity(Parcel in) {
        id = in.readString();
        code = in.readString();
        name = in.readString();
        age = in.readInt();
        potSize = in.readString();
        retailPrice = in.readDouble();
        setDescription(in.readString());
        nicePhoto = in.readString();
        in.readList(realPhotos, null);
        isVisibleForSale = in.readInt();
        forSaleTime = in.readLong();
        currencySymbol = in.readString();
    }

    public static final Creator<OrchidEntity> CREATOR = new Creator<OrchidEntity>() {
        @Override
        public OrchidEntity createFromParcel(Parcel in) {
            return new OrchidEntity(in);
        }

        @Override
        public OrchidEntity[] newArray(int size) {
            return new OrchidEntity[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getPotSize() {
        return potSize;
    }

    public void setPotSize(String potSize) {
        this.potSize = potSize;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNicePhoto() {
        return nicePhoto;
    }

    public void setNicePhoto(String nicePhoto) {
        this.nicePhoto = nicePhoto;
    }

    public List<String> getRealPhotos() {
        return realPhotos;
    }

    public void setRealPhotos(List<String> realPhotos) {
        this.realPhotos = realPhotos;
    }

    public boolean getIsVisibleForSale() {
        return isVisibleForSale == 1;
    }

    public void setIsVisibleForSale(boolean state) {
        if (state) {
            this.isVisibleForSale = 1;
            forSaleTime = System.currentTimeMillis();
        }
        else {
            this.isVisibleForSale = 0;
        }
    }


    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public long getForSaleTime() {
        return forSaleTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeInt(age);
        parcel.writeString(potSize);
        parcel.writeDouble(retailPrice);
        parcel.writeString(description);
        parcel.writeString(nicePhoto);
        parcel.writeList(realPhotos);
        parcel.writeInt(isVisibleForSale);
        parcel.writeLong(forSaleTime);
        parcel.writeString(currencySymbol);
    }

}

package in.softment.ecde.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.FieldValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductModel implements Serializable {

    public String id = "";
    public String cat_id = "";
    public String uid = "";
    public String title = "";
    public String description = "";
    public Integer price = 0;
    public Integer quantity = 0;
    public String sub_cat_id = "";
    public boolean isProductNew = false;
    public boolean deliverProduct = false;
    public boolean sameDayDeliver = false;
    public Integer maxDeliverDay = 0;
    public Map<String,String> images = new HashMap<>();
    public Date date = new Date();
    public String sellerName = "";
    public String sellerImage = "";
    public String sellerToken = "";
    public Date adLastDate = new Date();
    public String storeAbout = "";
    public double latitude = 0.0;
    public double longitude = 0.0;
    public String storeImage = "";
    public String storeCity = "";
    public String storeAddress = "";
    public String storeName = "";
    public String phoneNumber = "";


    public static ArrayList<ProductModel> latestproductModels = new ArrayList<>();
    public static ArrayList<ProductModel> myproductsModels = new ArrayList<>();
    public static ArrayList<ProductModel> singleCatProductModels = new ArrayList<>();


    public String getStoreAbout() {
        return storeAbout;
    }

    public void setStoreAbout(String storeAbout) {
        this.storeAbout = storeAbout;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStoreImage() {
        return storeImage;
    }

    public void setStoreImage(String storeImage) {
        this.storeImage = storeImage;
    }

    public String getStoreCity() {
        return storeCity;
    }

    public void setStoreCity(String storeCity) {
        this.storeCity = storeCity;
    }


    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getAdLastDate() {
        return adLastDate;
    }

    public void setAdLastDate(Date adLastDate) {
        this.adLastDate = adLastDate;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSub_cat_id() {
        return sub_cat_id;
    }

    public void setSub_cat_id(String sub_cat_id) {
        this.sub_cat_id = sub_cat_id;
    }

    public boolean isProductNew() {
        return isProductNew;
    }

    public void setProductNew(boolean productNew) {
        isProductNew = productNew;
    }

    public boolean isDeliverProduct() {
        return deliverProduct;
    }

    public void setDeliverProduct(boolean deliverProduct) {
        this.deliverProduct = deliverProduct;
    }

    public boolean isSameDayDeliver() {
        return sameDayDeliver;
    }

    public void setSameDayDeliver(boolean sameDayDeliver) {
        this.sameDayDeliver = sameDayDeliver;
    }

    public Integer getMaxDeliverDay() {
        return maxDeliverDay;
    }

    public void setMaxDeliverDay(Integer maxDeliverDay) {
        this.maxDeliverDay = maxDeliverDay;
    }



    public String getSellerToken() {
        return sellerToken;
    }

    public void setSellerToken(String sellerToken) {
        this.sellerToken = sellerToken;
    }

    public String getSellerImage() {
        return sellerImage;
    }

    public void setSellerImage(String sellerImage) {
        this.sellerImage = sellerImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public Map<String, String> getImages() {
        return images;
    }

    public void setImages(Map<String, String> images) {
        this.images = images;
    }

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }


}

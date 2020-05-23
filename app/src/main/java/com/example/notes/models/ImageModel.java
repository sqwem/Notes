package com.example.notes.models;

public class ImageModel {

    String imageUrl;
    String imageId;

    public ImageModel() {
    }

    public ImageModel(String imageUrl, String imageId) {
        this.imageUrl = imageUrl;
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}

package com.example.mp.projectmp.util;

public class ImageSingletonClass {

    private static ImageSingletonClass sSoleInstance;

    private byte[] image;
    //private constructor.
    private ImageSingletonClass(){
       
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    } 

    public static ImageSingletonClass getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new ImageSingletonClass();
        }

        return sSoleInstance;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
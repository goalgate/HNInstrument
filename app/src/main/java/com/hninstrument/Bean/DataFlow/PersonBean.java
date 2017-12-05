package com.hninstrument.Bean.DataFlow;

import android.graphics.Bitmap;

/**
 * Created by zbsz on 2017/12/5.
 */

public class PersonBean {


    public PersonBean() {
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFaceReconition(String faceReconition) {
        this.faceReconition = faceReconition;
    }

    public void setHeadPhoto(Bitmap headPhoto) {
        this.headPhoto = headPhoto;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getFaceReconition() {
        return faceReconition;
    }

    public Bitmap getHeadPhoto() {
        return headPhoto;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    String cardId;
    String name;
    String faceReconition;
    Bitmap headPhoto;
    Bitmap photo;
}

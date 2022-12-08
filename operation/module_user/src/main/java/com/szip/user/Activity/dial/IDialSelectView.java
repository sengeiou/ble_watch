package com.szip.user.Activity.dial;

public interface IDialSelectView {
    void setView(String id,String pictureId,String fileName);
    void setDialView(String dialId,String pictureId,String fileName);
    void setDialProgress(int max);
}

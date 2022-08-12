package com.szip.user.Activity.dial;

public interface IDialSelectView {
    void setView(String id,String pictureId);
    void setDialView(String dialId,String pictureId);
    void setDialProgress(int max);
}

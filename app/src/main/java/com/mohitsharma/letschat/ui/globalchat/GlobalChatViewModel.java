package com.mohitsharma.letschat.ui.globalchat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GlobalChatViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GlobalChatViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
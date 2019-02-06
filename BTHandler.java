package com.example.stemcomp2019;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.lang.ref.WeakReference;

public class BTHandler extends Handler {
    private final WeakReference<BTActivity> mActivity;

    public BTHandler(BTActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {

        final BTActivity activity = mActivity.get();

        switch (msg.what) {
            case Constants.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case Constants.STATE_CONNECTED:
                        activity.btStatus("Connected");
                        break;
                    case Constants.STATE_CONNECTING:
                        activity.btStatus("Connecting");
                        break;
                    case Constants.STATE_NONE:
                        activity.btStatus("Not Connected");
                        break;
                    case Constants.STATE_ERROR:
                        activity.btStatus("Error");
                        break;
                }
                break;
            case Constants.MESSAGE_READ:
                String readMessage = (String) msg.obj;
                activity.btMessage(readMessage.substring(0, readMessage.length() - 2));
                break;
        }
    }
}
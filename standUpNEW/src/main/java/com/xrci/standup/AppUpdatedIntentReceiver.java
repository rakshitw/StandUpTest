package com.xrci.standup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by q4KV89ZB on 07-04-2015.
 */
public class AppUpdatedIntentReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, StepService.class);
            context.startService(pushIntent);
        }
    }
}
/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiocean.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmListenerService;
import com.mobiocean.util.DeBug;



public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        DeBug.ShowLog(TAG, "From: " + from);
        DeBug.ShowLog(TAG, "Message: " + message);

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        String Body = message;
        if(TextUtils.isEmpty(Body))
        	return;
        DeBug.ShowLog(TAG, "Received message: "+Body);
        //displayMessage(context, message);
        // notifies user
//        DeBug.ShowToast(getApplicationContext(), ""+Body);
     //   Toast.makeText(getApplicationContext(), ""+Body, Toast.LENGTH_SHORT).show();
         String Url="";//URL_HEADER+"/Parents/GiveStngMsgToStu.aspx?StuMobile="+CallHelper.Ds.structPC.stPhoneNo;
      //  ServerUtilities.callURLWebservice(context, Url, 2222);
        
    	Intent msgIntent = new Intent(this, GcmServerIntentService.class);
		Bundle b = new Bundle();
		msgIntent.putExtra("seq", 2222);
		msgIntent.putExtra("regkeyGCM", "");
		msgIntent.putExtras(b);
		startService(msgIntent);
        
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */

}

/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.team254.cheezdroid;

import android.app.Activity;
import android.os.Bundle;

public class SelfieActivity extends Activity implements RobotEventListener {

    RobotEventBroadcastReceiver rebr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rebr = new RobotEventBroadcastReceiver(this, this);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, SelfieModeFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void shotTaken() {

    }

    @Override
    public void wantsVisionMode() {
        finish();
    }

    @Override
    public void wantsIntakeMode() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(rebr);
    }
}

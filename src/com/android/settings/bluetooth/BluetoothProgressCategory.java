/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.bluetooth;

import com.android.settings.ProgressCategory;
import com.android.settings.R;

import android.content.Context;
import android.util.AttributeSet;

import android.view.View;


public class BluetoothProgressCategory extends ProgressCategory {
	private boolean mProgress = false;
    public BluetoothProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs, R.string.bluetooth_no_devices_found);
    }

	//Fixed google bug:"NO devices found..." when pairing a device
	@Override
    public void onBindView(View view) {
        final View progressBar = view.findViewById(R.id.scanning_progress);

        progressBar.setVisibility(mProgress ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setProgress(boolean progressOn) {
        mProgress = progressOn;
        notifyChanged();
    }
}

/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.util.Log;

public class HdmiTopPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private SeekBar mSeekBar;

    private int mOldHdmiTop;

    private boolean mRestoredOldState;
	private int mScreenHdmiTopDim =0;
	private static final int MAXIMUM_HDMITOP = 50;

    private ContentObserver mHdmiTopObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onHdmiTopChanged();
        }
    };

    public HdmiTopPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_dialog_hdmitop);
        setDialogIcon(R.drawable.ic_settings_display);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_HDMITOP), true,
                mHdmiTopObserver);

        mRestoredOldState = false;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(50);
        mOldHdmiTop = getHdmiTop(0);
        mSeekBar.setProgress(mOldHdmiTop);

        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setHdmiTop(progress + 0);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setHdmiTop(mSeekBar.getProgress() + 0);
    }

    private int getHdmiTop(int defaultValue) {
        int brightness = defaultValue;
        try {
            brightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_HDMITOP);
        } catch (SettingNotFoundException snfe) {
        }
	System.out.printf("getHdmiTop hdmitop: %d\n",brightness);
        return brightness;
    }

    private void onHdmiTopChanged() {
        int brightness = getHdmiTop(50);
        mSeekBar.setProgress(brightness - 0);
	//System.out.printf("onHdmiTopChanged hdmitop\n");
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        final ContentResolver resolver = getContext().getContentResolver();

        if (positiveResult) {
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_HDMITOP,
                    mSeekBar.getProgress() + 0);
        } else {
            restoreOldState();
        }

        resolver.unregisterContentObserver(mHdmiTopObserver);
    }

    private void restoreOldState() {
        if (mRestoredOldState) return;

        setHdmiTop(mOldHdmiTop);
        mRestoredOldState = true;
    }

    private void setHdmiTop(int brightness) {
		//brightness = 10;//hdmi status();
		String s = ""+ brightness;
		System.out.printf("setHdmiTop hdmitop: %d,s: %s\n",brightness,s);
		SystemProperties.set("persist.sys.hdmi.top",s);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) return superState;

        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.progress = mSeekBar.getProgress();
        myState.oldProgress = mOldHdmiTop;

        // Restore the old state when the activity or dialog is being paused
        restoreOldState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mOldHdmiTop = myState.oldProgress;
        setHdmiTop(myState.progress + 0);
    }

    private static class SavedState extends BaseSavedState {

        int progress;
        int oldProgress;

        public SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
            oldProgress = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
            dest.writeInt(oldProgress);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}


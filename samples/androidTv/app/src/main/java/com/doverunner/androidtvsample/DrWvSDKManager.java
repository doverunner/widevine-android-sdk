package com.doverunner.androidtvsample;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.source.MediaSource;

import com.doverunner.widevine.exception.WvException;
import com.doverunner.widevine.exception.WvLicenseServerException;
import com.doverunner.widevine.model.ContentData;
import com.doverunner.widevine.model.DownloadState;
import com.doverunner.widevine.model.WvEventListener;
import com.doverunner.widevine.sdk.DrWvSDK;
import com.doverunner.widevine.track.DownloaderTracks;

import java.util.concurrent.Callable;

public class DrWvSDKManager {
    private Context context = null;
    private Activity activity = null;
    private DrWvSDK wvSDK = null;
    private WvEventListener drmListener = new WvEventListener() {
        @Override
        public void onFailed(@NonNull ContentData contentData, @Nullable WvLicenseServerException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "onFailed", e.getMessage());
        }

        @Override
        public void onFailed(@NonNull ContentData contentData, @Nullable WvException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "onFailed", e.getMessage());
        }

        @Override
        public void onPaused(@NonNull ContentData contentData) {

        }

        @Override
        public void onRemoved(@NonNull ContentData contentData) {

        }

        @Override
        public void onRestarting(@NonNull ContentData contentData) {

        }

        @Override
        public void onStopped(@NonNull ContentData contentData) {

        }

        @Override
        public void onProgress(@NonNull ContentData contentData, float percent, long downloadedBytes) {

        }

        @Override
        public void onCompleted(@NonNull ContentData contentData) {

        }
    };

    public static DrWvSDKManager getInstance(Activity activity) {
        LazyHolder.INSTANCE.activity = activity;
        LazyHolder.INSTANCE.context = activity.getApplicationContext();
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final DrWvSDKManager INSTANCE = new DrWvSDKManager();
    }

    public void createSDK(ContentData contentData) {
        wvSDK = DrWvSDK.createWvSDK(
                this.context,
                contentData);
        DrWvSDK.addWvEventListener(drmListener);
    }

    public void downloadLicense(Callable<Void> onSuccess) {
        wvSDK.downloadLicense(null, () -> {
                    try {
                        onSuccess.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                },
                e -> {
                    e.printStackTrace();
                    Utils.showSimpleDialog(activity, "content data is not correct", e.getMessage());
                    return null;
                });
    }

    public void download(DownloaderTracks tracks) {
        try {
            wvSDK.download(tracks);
        } catch (WvException.ContentDataException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "content data error", e.getMessage());
        } catch (WvException.DownloadException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "download exception", e.getMessage());
        } catch (WvException.DrmException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "drm exception", e.getMessage());
        }
    }

    public MediaSource getMediaSource() {
        MediaSource mediaSource = null;
        try {
            mediaSource = wvSDK.getMediaSource();
        } catch (WvException.DetectedDeviceTimeModifiedException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "modified device time", e.getMessage());
        } catch (WvException.ContentDataException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "content data error", e.getMessage());
        } finally {
            return mediaSource;
        }
    }

    public DownloadState getDownloadState() {
        return wvSDK.getDownloadState();
    }

    public void defaultDownload() {
        wvSDK.getContentTrackInfo((tracks) -> {
            download(tracks);
            return null;
        }, e -> {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "PallyConException", e.getMessage());
            return null;
        });
    }

    public void removeLicense() {
        try {
            wvSDK.removeLicense();
        } catch (WvException.DrmException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "DrmException", e.getMessage());
        }
    }

    public void remove() {
        try {
            wvSDK.remove();
        } catch (WvException.ContentDataException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "content data error", e.getMessage());
        } catch (WvException.DownloadException e) {
            e.printStackTrace();
            Utils.showSimpleDialog(activity, "DownloadException", e.getMessage());
        }
    }

    public void release() {
        wvSDK.release();
    }
}

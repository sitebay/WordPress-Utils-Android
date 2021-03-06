package org.sitebay.android.editor;


import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.sitebay.aztec.plugins.IMediaToolbarButton;
import org.sitebay.aztec.toolbar.AztecToolbar;
import org.sitebay.aztec.toolbar.IToolbarAction;

public class MediaToolbarCameraButton implements IMediaToolbarButton {
    private IMediaToolbarClickListener mClickListener;
    private Context mContext;
    private IToolbarAction mAction = MediaToolbarAction.CAMERA;
    private AztecToolbar mToolbar;


    public MediaToolbarCameraButton(AztecToolbar aztecToolbar) {
        mToolbar = aztecToolbar;
        mContext = mToolbar.getContext();
    }

    @Override
    public void setMediaToolbarButtonClickListener(IMediaToolbarClickListener mediaToolbarClickListener) {
        mClickListener = mediaToolbarClickListener;
    }

    @NotNull
    @Override
    public IToolbarAction getAction() {
        return mAction;
    }

    @NotNull
    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void toggle() {
        if (mClickListener != null) {
            mClickListener.onClick(mToolbar.findViewById(getAction().getButtonId()));
        }
    }

    @Override
    public boolean matchesKeyShortcut(int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public void inflateButton(ViewGroup viewGroup) {
        LayoutInflater.from(getContext()).inflate(R.layout.media_toobar_camera_button, viewGroup);
    }

    @Override
    public void toolbarStateAboutToChange(AztecToolbar aztecToolbar, boolean enable) {
        aztecToolbar.findViewById(mAction.getButtonId()).setEnabled(enable);
    }
}

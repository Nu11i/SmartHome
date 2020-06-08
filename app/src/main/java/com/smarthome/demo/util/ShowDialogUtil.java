package com.smarthome.demo.util;

import android.content.Context;
import com.smarthome.demo.model.FamilyDialogModel;

public class ShowDialogUtil {
    private FamilyDialogModel customDialog;
    public ShowDialogUtil() {

    }

    public void show(final Context context, String message, final OnBottomClickListener onBottomClickListener) {
        customDialog = new FamilyDialogModel(context);
        customDialog.setMessage(message);
        customDialog.setYesOnClickListener("确定", new FamilyDialogModel.onYesOnClickListener() {
            @Override
            public void onYesClick() {
                if (onBottomClickListener != null) {
                    onBottomClickListener.positive();
                }
                customDialog.dismiss();
            }
        });

        customDialog.setNoOnClickListener("取消", new FamilyDialogModel.onNoClickListener() {
            @Override
            public void onNoClick() {
                if (onBottomClickListener != null) {
                    onBottomClickListener.negative();
                }
                customDialog.dismiss();
            }
        });
        customDialog.show();

    }
    public interface OnBottomClickListener {
        void positive();

        void negative();

    }
}

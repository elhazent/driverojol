package com.elhazent.picodiploma.online.view;

import android.content.DialogInterface;

public interface LoginRegisView {
    void showloading();
    void hideloading();
    void showdialog(DialogInterface dialogInterface);
    void hidedialog(DialogInterface dialogInterface);
    void showToast(String msg);
    void showError(String msg);
}

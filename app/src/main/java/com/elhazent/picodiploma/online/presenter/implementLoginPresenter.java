package com.elhazent.picodiploma.online.presenter;

import android.content.DialogInterface;

public interface implementLoginPresenter {
    void ProsessRegister(final String name, final String phone, String email, final String password, final DialogInterface dialog);
    void ProsessLogin(String device, String email, String password, final DialogInterface dialog);
}

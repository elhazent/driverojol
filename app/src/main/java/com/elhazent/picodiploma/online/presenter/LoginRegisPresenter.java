package com.elhazent.picodiploma.online.presenter;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.elhazent.picodiploma.online.LoginRegisterActivity;
import com.elhazent.picodiploma.online.model.ResponseLoginRegis;
import com.elhazent.picodiploma.online.network.InitRetrofit;
import com.elhazent.picodiploma.online.view.LoginRegisView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRegisPresenter implements implementLoginPresenter{

    LoginRegisView loginRegisView;

    public LoginRegisPresenter(LoginRegisView loginRegisView) {
        this.loginRegisView = loginRegisView;
    }

    @Override
    public void ProsessRegister(String name, String phone, String email, String password, final DialogInterface dialog) {
        loginRegisView.showloading();
        InitRetrofit.getInstance().registerUser(name, phone, email, password).enqueue(new Callback<ResponseLoginRegis>() {
            @Override
            public void onResponse(Call<ResponseLoginRegis> call, Response<ResponseLoginRegis> response) {
                loginRegisView.hideloading();
                if (response.isSuccessful()) {
                    String result = response.body().getResult();
                    String msg = response.body().getMsg();
                    if (result.equals("true")) {
                        loginRegisView.showToast(msg);
                        loginRegisView.hidedialog(dialog);
                    } else {
                        loginRegisView.showToast(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseLoginRegis> call, Throwable t) {
                loginRegisView.showError("gagal format" + t.getLocalizedMessage());
                loginRegisView.hideloading();
            }
        });
    }

    @Override
    public void ProsessLogin(String device, String email, String password, DialogInterface dialog) {

    }
}

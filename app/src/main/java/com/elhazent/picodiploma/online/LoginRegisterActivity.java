package com.elhazent.picodiploma.online;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elhazent.picodiploma.online.helper.HeroHelper;
import com.elhazent.picodiploma.online.helper.SessionManager;
import com.elhazent.picodiploma.online.model.DataLogin;
import com.elhazent.picodiploma.online.model.ResponseLoginRegis;
import com.elhazent.picodiploma.online.network.InitRetrofit;
import com.elhazent.picodiploma.online.presenter.LoginRegisPresenter;
import com.elhazent.picodiploma.online.view.LoginRegisView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRegisterActivity extends AppCompatActivity implements LoginRegisView {
    @BindView(R.id.txt_rider_app)
    TextView txtRiderApp;
    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.btnRegister)
    Button btnRegister;
    @BindView(R.id.rootlayout)
    RelativeLayout rootlayout;
    private DataLogin datalogin;
    LoginRegisPresenter presenter;
    private SessionManager session;

    FirebaseAuth auth;
    DatabaseReference root;
    private ProgressDialog loading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginregister);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        loading = new ProgressDialog(this);
        presenter = new LoginRegisPresenter(this);
    }

    @OnClick({R.id.btnSignIn, R.id.btnRegister})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSignIn:
                login();
                break;
            case R.id.btnRegister:
                register();
                break;
        }
    }
    private void register() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage(getString(R.string.messageregister));
        LayoutInflater inflater = getLayoutInflater();
        View viewReg = inflater.inflate(R.layout.layout_register, null, false);
        final ViewHolderRegister holderRegister = new ViewHolderRegister(viewReg);
        builder.setView(viewReg);
        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cek validasi
                String email = holderRegister.edtEmail.getText().toString().trim();
                String password = holderRegister.edtPassword.getText().toString().trim();
                String name = holderRegister.edtName.getText().toString().trim();
                String phone = holderRegister.edtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    holderRegister.edtEmail.setError(getString(R.string.requireemail));
                } else if (TextUtils.isEmpty(password)) {
                    holderRegister.edtPassword.setError(getString(R.string.requirepassword));
                } else if (TextUtils.isEmpty(name)) {
                    holderRegister.edtName.setError(getString(R.string.requirename));
                } else if (TextUtils.isEmpty(phone)) {
                    holderRegister.edtPhone.setError(getString(R.string.requirephone));
                } else {
                    ProsessRegister(name, phone, email, password, dialog);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }



    private void ProsessRegister(final String name, final String phone, String email, final String password, final DialogInterface dialog) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Proses register", "Loading....");
        InitRetrofit.getInstance().registerUser(name, phone, email, password).enqueue(new Callback<ResponseLoginRegis>() {
            @Override
            public void onResponse(Call<ResponseLoginRegis> call, Response<ResponseLoginRegis> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String result = response.body().getResult();
                    String msg = response.body().getMsg();
                    if (result.equals("true")) {
                        Toast.makeText(LoginRegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseLoginRegis> call, Throwable t) {
                Toast.makeText(LoginRegisterActivity.this, "Failure, Message : " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                progressDialog.dismiss();
            }
        });

        auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            FirebaseUser userF = auth.getCurrentUser();
                            assert userF != null;
                            String userId = userF.getUid();

                            root = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("id", userId);
                            map.put("username", name);
                            map.put("pass",password);
                            map.put("status","offline");

                            root.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dialog.dismiss();
                                }
                            });
                        }else {
                            Toast.makeText(LoginRegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private void login() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login");
        builder.setMessage(getString(R.string.messageregister));
        LayoutInflater inflater = getLayoutInflater();
        View viewLog = inflater.inflate(R.layout.layout_login, null, false);
        final ViewHolderLogin holderLogin = new ViewHolderLogin(viewLog);
        builder.setView(viewLog);
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cek validasi
                String email = holderLogin.edtEmail.getText().toString().trim();
                String password = holderLogin.edtPassword.getText().toString().trim();
                String device = HeroHelper.getDeviceUUID(LoginRegisterActivity.this);
                if (TextUtils.isEmpty(email)) {
                    holderLogin.edtEmail.setError(getString(R.string.requireemail));
                } else if (TextUtils.isEmpty(password)) {
                    holderLogin.edtPassword.setError(getString(R.string.requirepassword));
                } else {
                    ProsessLogin(device, email, password, dialog);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void ProsessLogin(String device, String email, String password, final DialogInterface dialog) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Proses Login", "Loading....");
        InitRetrofit.getInstance().loginUser(device, password, email).enqueue(new Callback<ResponseLoginRegis>() {
            @Override
            public void onResponse(Call<ResponseLoginRegis> call, Response<ResponseLoginRegis> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String result = response.body().getResult();
                    String msg = response.body().getMsg();
                    if (result.equals("true")) {
                        Toast.makeText(LoginRegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        String token = response.body().getToken();
                        datalogin = response.body().getData();
                        session = new SessionManager(LoginRegisterActivity.this);
                        session.createLoginSession(token);
                        session.setIduser(datalogin.getIdUser());
                        startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseLoginRegis> call, Throwable t) {
                Toast.makeText(LoginRegisterActivity.this, "Failure, Message : " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d("LOGIN", "onFailure: " + t.getLocalizedMessage());
                dialog.dismiss();
                progressDialog.dismiss();
            }
        });

        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));
                            Toast.makeText(LoginRegisterActivity.this, "FIREBASE LOGIN", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(LoginRegisterActivity.this, "Authentication Failed..!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void showloading() {

    }

    @Override
    public void hideloading() {

    }

    @Override
    public void showdialog(DialogInterface dialogInterface) {

    }

    @Override
    public void hidedialog(DialogInterface dialogInterface) {

    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String msg) {

    }

    static
    class ViewHolderRegister {
        @BindView(R.id.edtEmail)
        MaterialEditText edtEmail;
        @BindView(R.id.edtPassword)
        MaterialEditText edtPassword;
        @BindView(R.id.edtName)
        MaterialEditText edtName;
        @BindView(R.id.edtPhone)
        MaterialEditText edtPhone;

        ViewHolderRegister(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static
    class ViewHolderLogin {
        @BindView(R.id.edtEmail)
        MaterialEditText edtEmail;
        @BindView(R.id.edtPassword)
        MaterialEditText edtPassword;

        ViewHolderLogin(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

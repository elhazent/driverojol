package com.elhazent.picodiploma.online;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.elhazent.picodiploma.online.helper.HeroHelper;
import com.elhazent.picodiploma.online.helper.MyContants;
import com.elhazent.picodiploma.online.helper.SessionManager;
import com.elhazent.picodiploma.online.model.ResponseWaitingDriver;
import com.elhazent.picodiploma.online.network.InitRetrofit;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaitingDriverActivity extends AppCompatActivity {

    @BindView(R.id.buttoncancel)
    Button buttoncancel;
    @BindView(R.id.pulsator)
    PulsatorLayout pulsator;
    private int idbooking;
    private Timer timer;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        ButterKnife.bind(this);
        pulsator.start();
        idbooking = getIntent().getIntExtra(MyContants.IDBOOKING, 0);
        timer = new Timer();
        cetStatusBooking();
    }

    private void cetStatusBooking() {
        InitRetrofit.getInstance().cekStatusOrder(String.valueOf(idbooking)).enqueue(new Callback<ResponseWaitingDriver>() {
            @Override
            public void onResponse(Call<ResponseWaitingDriver> call, Response<ResponseWaitingDriver> response) {
                String result = response.body().getResult();
                String msg = response.body().getMsg();
                if (result.equals("true")) {
                    String idDriver = response.body().getDriver();
                    Intent intent = new Intent(WaitingDriverActivity.this, DetailDriverActivity.class);
                    intent.putExtra(MyContants.IDDRIVER, idDriver);
                    startActivity(intent);
                    finish();
                    Toast.makeText(WaitingDriverActivity.this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WaitingDriverActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseWaitingDriver> call, Throwable t) {
                Toast.makeText(WaitingDriverActivity.this, "Failure : " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.buttoncancel)
    public void onViewClicked() {
        session = new SessionManager(this);
        final String device = HeroHelper.getDeviceUUID(this);
        final String token = session.getToken();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel ");
        builder.setMessage("Apakah anda yakin untuk cancel orderan ini ?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InitRetrofit.getInstance().cancelBooking(String.valueOf(idbooking), device, token).enqueue(new Callback<ResponseWaitingDriver>() {
                    @Override
                    public void onResponse(Call<ResponseWaitingDriver> call, Response<ResponseWaitingDriver> response) {
                        if (response.isSuccessful()) {
                            String result = response.body().getResult();
                            String msg = response.body().getMsg();
                            if (result.equals("true")) {
                                Toast.makeText(WaitingDriverActivity.this, msg, Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(WaitingDriverActivity.this, msg, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseWaitingDriver> call, Throwable t) {
                        Toast.makeText(WaitingDriverActivity.this, "Failure : " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        cetStatusBooking();
                    }
                }, 0, 3000);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }
}

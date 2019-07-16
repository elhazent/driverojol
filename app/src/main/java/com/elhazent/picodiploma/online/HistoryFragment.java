package com.elhazent.picodiploma.online;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elhazent.picodiploma.online.helper.HeroHelper;
import com.elhazent.picodiploma.online.helper.SessionManager;
import com.elhazent.picodiploma.online.model.DataItem;
import com.elhazent.picodiploma.online.model.ResponseHistory;
import com.elhazent.picodiploma.online.network.InitRetrofit;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class HistoryFragment extends Fragment {


    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    Unbinder unbinder;
    int i;
    private SessionManager session;
    private List<DataItem> dataHistory;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public HistoryFragment(int i) {
        this.i = i;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_proses, container, false);
        unbinder = ButterKnife.bind(this, view);
        getDatahistory();
        return view;
    }

    private void getDatahistory() {
        session = new SessionManager(getActivity());
        String token = session.getToken();
        String idUser = session.getIdUser();
        String device = HeroHelper.getDeviceUUID(getActivity());
        InitRetrofit.getInstance().getDataHistory(token, device,String.valueOf(i), idUser).enqueue(new Callback<ResponseHistory>() {
            @Override
            public void onResponse(Call<ResponseHistory> call, Response<ResponseHistory> response) {
                if (response.isSuccessful()){
                    String result = response.body().getResult();
                    String msg = response.body().getMsg();
                    if (result.equals("true")){
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                        dataHistory = response.body().getData();
                        CustomRecyclerAdapter adapter = new CustomRecyclerAdapter(dataHistory, getActivity());
                        recyclerview.setAdapter(adapter);
                        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
                    } else {
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseHistory> call, Throwable t) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

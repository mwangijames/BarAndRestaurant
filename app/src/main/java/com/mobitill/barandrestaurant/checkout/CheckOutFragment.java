package com.mobitill.barandrestaurant.checkout;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobitill.barandrestaurant.MainApplication;
import com.mobitill.barandrestaurant.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class CheckOutFragment extends Fragment implements CheckOutContract.View {

    private static final String TAG = CheckOutFragment.class.getSimpleName();
    private static final String ID = "orderId";

    @Inject CheckOutPresenter mCheckOutPresenter;
    public CheckOutContract.Presenter mPresenter;

    private String orderId;
    private Unbinder mUnbinder;

    @BindView(R.id.btn_checked_out_ok)
    Button OK;

    public CheckOutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderId = getArguments().getString(ID);
        DaggerCheckOutComponent.builder()
                .checkOutPresenterModule(new CheckOutPresenterModule(this))
                .baseComponent(((MainApplication) getActivity().getApplication()).mBaseComponent())
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.check_out_fragment, container, false);
       mUnbinder = ButterKnife.bind(this,view);
        return view;
    }
    @OnClick(R.id.btn_checked_out_ok)
    public void OkButtonClick(){
        //startActivity(ReceiptsDetailActivity.newIntent(getActivity(),orderId));
        mPresenter.checkout(orderId);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    public static CheckOutFragment newInstance(String orderId) {

        Bundle args = new Bundle();
        args.putString(ID,orderId);
        CheckOutFragment fragment = new CheckOutFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void setPresenter(@NonNull CheckOutContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }


}
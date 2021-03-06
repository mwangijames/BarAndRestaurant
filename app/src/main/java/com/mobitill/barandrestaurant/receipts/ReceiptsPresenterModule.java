package com.mobitill.barandrestaurant.receipts;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

/**
 * Created by andronicus on 5/22/2017.
 */
@Module
public class ReceiptsPresenterModule {
    private final ReceiptsContract.View view;

    public ReceiptsPresenterModule(ReceiptsContract.View view) {
        this.view = view;
    }

    @Provides
    @NonNull
    ReceiptsContract.View provideReceiptsContractView(){
        return view;
    }
}

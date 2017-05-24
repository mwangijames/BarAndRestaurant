package com.mobitill.barandrestaurant.data.order.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.mobitill.barandrestaurant.data.order.model.Order;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;


import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.mobitill.barandrestaurant.data.order.source.local.OrderPersistenceContract.*;

@Singleton
public class OrderLocalDataSource implements OrderDataSource {

    private static final String TAG = OrderLocalDataSource.class.getSimpleName();

    @NonNull
    private final BriteDatabase mDatabaseHelper;

    @NonNull
    private Func1<Cursor, Order> mOrderMapperFunction;

    @Inject
    public OrderLocalDataSource(@NonNull BriteDatabase briteDatabaseHelper) {
        mDatabaseHelper = checkNotNull(briteDatabaseHelper);
        mOrderMapperFunction = this::getOrder;
    }

    private Order getOrder(@NonNull Cursor c) {
        String orderId = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_ID));
        String name = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_NAME));
        String waiterId = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_WAITER));
        return new Order(orderId, name, waiterId);
    }

    @Override
    @RxLogObservable
    public Observable<List<Order>> getAll() {
        String[] projection = {
                OrderEntry.COLUMN_NAME_ID,
                OrderEntry.COLUMN_NAME_NAME,
                OrderEntry.COLUMN_NAME_WAITER,
        };

        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), OrderEntry.TABLE_NAME);
        rx.Observable<List<Order>> observableV1 = mDatabaseHelper.createQuery(OrderEntry.TABLE_NAME, sql)
                .mapToList(mOrderMapperFunction).take(50, TimeUnit.MILLISECONDS);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public Observable<Order> getOne(String id) {
        checkNotNull(id);
        String[] projection = {
                OrderEntry.COLUMN_NAME_ID,
                OrderEntry.COLUMN_NAME_NAME,
                OrderEntry.COLUMN_NAME_WAITER,
        };

        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?",
                TextUtils.join(",", projection), OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_ID);
        rx.Observable<Order> orderObservableV1 = mDatabaseHelper.createQuery(OrderEntry.TABLE_NAME,
                sql, id).mapToOneOrDefault(mOrderMapperFunction, null);
        Observable<Order> orderObservableV2 = RxJavaInterop.toV2Observable(orderObservableV1);
        return orderObservableV2;
    }

    @Override
    public long save(Order item) {
        checkNotNull(item);
        ContentValues contentValues = new ContentValues();
        contentValues.put(OrderEntry.COLUMN_NAME_ID, item.getId());
        contentValues.put(OrderEntry.COLUMN_NAME_NAME, item.getName());
        contentValues.put(OrderEntry.COLUMN_NAME_WAITER, item.getWaiterId());
        return mDatabaseHelper.insert(OrderEntry.TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public int delete(String id) {
        String selection = OrderEntry.COLUMN_NAME_ID + "LIKE ?";
        String selectionArgs[] = {id};
        return mDatabaseHelper.delete(OrderEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Order item) {
        ContentValues values = new ContentValues();
        values.put(OrderEntry.COLUMN_NAME_NAME, item.getName());
        values.put(OrderEntry.COLUMN_NAME_WAITER, item.getWaiterId());
        String selection = OrderEntry.COLUMN_NAME_ID + " LIKE?";
        String[] selectionArgs = {item.getId()};
        return mDatabaseHelper.update(OrderEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void deleteAll() {
        mDatabaseHelper.delete(OrderEntry.TABLE_NAME, null);
    }
}


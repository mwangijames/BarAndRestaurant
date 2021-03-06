package com.mobitill.barandrestaurant.data.waiter.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.mobitill.barandrestaurant.data.waiter.WaitersDataSource;
import com.mobitill.barandrestaurant.data.waiter.waitermodels.response.Waiter;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.mobitill.barandrestaurant.data.waiter.source.local.WaitersPersistenceContract.WaitersEntry;

/**
 * Created by james on 4/27/2017.
 */
@Singleton
public class WaitersLocalDataSource implements WaitersDataSource {

    private static final String TAG = WaitersLocalDataSource.class.getSimpleName();

    @NonNull
    private final BriteDatabase mDatabaseHelper;

    @NonNull
    private Func1<Cursor, Waiter> mWaiterMapperFunction;

    @Inject
    public WaitersLocalDataSource(@NonNull BriteDatabase briteDatabaseHelper){
        mDatabaseHelper = checkNotNull(briteDatabaseHelper);
        mWaiterMapperFunction = this::getWaiter;

    }

    private Waiter getWaiter(@NonNull Cursor c) {
        String waiterId = c.getString(c.getColumnIndexOrThrow(WaitersEntry.COLUMN_NAME_ID));
        String name = c.getString(c.getColumnIndexOrThrow(WaitersEntry.COLUMN_NAME_NAME));
        String phone = c.getString(c.getColumnIndexOrThrow(WaitersEntry.COLUMN_NAME_PHONE));
        String password = c.getString(c.getColumnIndexOrThrow(WaitersEntry.COLUMN_NAME_PASSWORD));
        return new Waiter(waiterId, name, phone,password);
    }

    @Override
    @RxLogObservable
    public Observable<List<Waiter>> getAll() {
        String[] projection = {
                WaitersEntry.COLUMN_NAME_ID,
                WaitersEntry.COLUMN_NAME_NAME,
                WaitersEntry.COLUMN_NAME_PHONE,
                WaitersEntry.COLUMN_NAME_PASSWORD
        };

        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), WaitersEntry.TABLE_NAME);
        rx.Observable<List<Waiter>> observableV1 = mDatabaseHelper.createQuery(WaitersEntry.TABLE_NAME, sql)
                .mapToList(mWaiterMapperFunction).take(50, TimeUnit.MILLISECONDS);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Waiter>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public Observable<Waiter> getOne(String id) {
        checkNotNull(id);
        String[] projection = {
                WaitersEntry.COLUMN_NAME_ID,
                WaitersEntry.COLUMN_NAME_NAME,
                WaitersEntry.COLUMN_NAME_PHONE,
                WaitersEntry.COLUMN_NAME_PASSWORD
        };

        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?",
                TextUtils.join(",", projection), WaitersEntry.TABLE_NAME, WaitersEntry.COLUMN_NAME_ID);
        rx.Observable<Waiter> waiterObservableV1 = mDatabaseHelper
                .createQuery(WaitersEntry.TABLE_NAME, sql, id)
                .mapToOneOrDefault(mWaiterMapperFunction, null)
                .take(1);
        Observable<Waiter> waiterObservableV2 = RxJavaInterop.toV2Observable(waiterObservableV1);
        return waiterObservableV2;
    }

    @Override
    public Waiter save(Waiter item) {
        checkNotNull(item);
        ContentValues contentValues = new ContentValues();
        contentValues.put(WaitersEntry.COLUMN_NAME_ID, item.getId());
        contentValues.put(WaitersEntry.COLUMN_NAME_NAME, item.getName());
        contentValues.put(WaitersEntry.COLUMN_NAME_PHONE, item.getPhone());
        contentValues.put(WaitersEntry.COLUMN_NAME_PASSWORD, item.getPassword());
        long rowId = mDatabaseHelper.insert(WaitersEntry.TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return getLastCreated();
    }

    @Override
    public int delete(String id) {
        String selection = WaitersEntry.COLUMN_NAME_ID + "LIKE ?";
        String selectionArgs[] = {id};
        return mDatabaseHelper.delete(WaitersEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Waiter item) {
        ContentValues values = new ContentValues();
        values.put(WaitersEntry.COLUMN_NAME_ID, item.getName());
        values.put(WaitersEntry.COLUMN_NAME_NAME, item.getPhone());
        values.put(WaitersEntry.COLUMN_NAME_PHONE, item.getPhone());
        values.put(WaitersEntry.COLUMN_NAME_PASSWORD, item.getPhone());
        String selection = WaitersEntry.COLUMN_NAME_ID + " LIKE?";
        String[] selectionArgs = {item.getId()};
        return mDatabaseHelper.update(WaitersEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void deleteAll() {
        mDatabaseHelper.delete(WaitersEntry.TABLE_NAME, null);
    }

    @Override
    public Waiter getLastCreated() {
        String selectQuery = "SELECT * FROM " + WaitersEntry.TABLE_NAME + " sqlite_sequence";
        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        cursor.moveToLast();
        return getWaiter(cursor);
    }

    @Override
    public Observable<Waiter> getWaiterFromPhoneAndPassword(String phone,String password) {
        String[] projection = {
                WaitersEntry.COLUMN_NAME_ID,
                WaitersEntry.COLUMN_NAME_NAME,
                WaitersEntry.COLUMN_NAME_PHONE,
                WaitersEntry.COLUMN_NAME_PASSWORD
        };

        String sql = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                TextUtils.join(",", projection), WaitersEntry.TABLE_NAME, WaitersEntry.COLUMN_NAME_PHONE,WaitersEntry.COLUMN_NAME_PASSWORD);

        rx.Observable<Waiter> waiterObservableV1 = mDatabaseHelper.createQuery(WaitersEntry.TABLE_NAME,
                sql, phone,password).mapToOneOrDefault(mWaiterMapperFunction, new Waiter());

        Observable<Waiter> waiterObservableV2 = RxJavaInterop.toV2Observable(waiterObservableV1);

        return waiterObservableV2;
    }

}

package com.mobitill.barandrestaurant.data.order.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mobitill.barandrestaurant.data.order.model.Order;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.mobitill.barandrestaurant.data.order.source.local.OrderPersistenceContract.OrderEntry;

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
        mOrderMapperFunction = c -> OrderLocalDataSource.this.getOrder(c);
    }

    String[] projection = {
            OrderEntry.COLUMN_NAME_ENTRY_ID,
            OrderEntry.COLUMN_NAME_DISPLAY_ID,
            OrderEntry.COLUMN_NAME_NAME,
            OrderEntry.COLUMN_NAME_WAITER_ID,
            OrderEntry.COLUMN_NAME_SYNCED,
            OrderEntry.COLUMN_NAME_COUNTERA_SYNCED,
            OrderEntry.COLUMN_NAME_COUNTERB_SYNCED,
            OrderEntry.COLUMN_NAME_CHECKED_OUT,
            OrderEntry.COLUMN_NAME_TIME_STAMP,
            OrderEntry.COLUMN_NAME_FLAGGED_FOR_CHECKOUT,
            OrderEntry.COLUMN_NAME_PAYMENT_METHOD,
            OrderEntry.COLUMN_NAME_AMOUNT,
            OrderEntry.COLUMN_NAME_TRANSACTION_ID,
            OrderEntry.COLUMN_NAME_PROCESS_STATE,
            OrderEntry.COLUMN_NAME_DATE
    };

    private Order getOrder(@NonNull Cursor c) {
        String entryId = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_ENTRY_ID));
        String displayId = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_DISPLAY_ID));
        String name = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_NAME));
        String waiterId = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_WAITER_ID));
        Integer synced = c.getInt(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_SYNCED));
        Integer checkedOut = c.getInt(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_CHECKED_OUT));
        Integer checkOutFlagged = c.getInt(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_FLAGGED_FOR_CHECKOUT));
        Long timestamp = c.getLong(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_TIME_STAMP));
        String paymentMethod = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_PAYMENT_METHOD));
        String amount = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_AMOUNT));
        String transactionId = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_TRANSACTION_ID));
        Integer processState = c.getInt(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_PROCESS_STATE));
        Integer counterASync = c.getInt(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_COUNTERA_SYNCED));
        Integer counterBSync = c.getInt(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_COUNTERA_SYNCED));
        String date = c.getString(c.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_DATE));
        return new Order(entryId, displayId, name, waiterId, synced, checkedOut, timestamp,
                checkOutFlagged,paymentMethod,amount,transactionId,processState,counterASync,counterBSync,date);

    }

    @Override
    public Observable<List<Order>> getAll() {

        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), OrderEntry.TABLE_NAME);
        rx.Observable<List<Order>> observableV1 = mDatabaseHelper.createQuery(OrderEntry.TABLE_NAME, sql)
                .mapToList(mOrderMapperFunction).take(2000, TimeUnit.MILLISECONDS);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public Observable<Order> getOne(String id) {
        checkNotNull(id);
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?",
                TextUtils.join(",", projection), OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_ENTRY_ID);
        rx.Observable<Order> orderObservableV1 = mDatabaseHelper.createQuery(OrderEntry.TABLE_NAME,
                sql, id).mapToOneOrDefault(mOrderMapperFunction, null).take(1);
        Observable<Order> orderObservableV2 = RxJavaInterop.toV2Observable(orderObservableV1);
        return orderObservableV2;
    }



    @Override
    public Order save(Order item) {
        checkNotNull(item);
        ContentValues contentValues = new ContentValues();
        contentValues.put(OrderEntry.COLUMN_NAME_ENTRY_ID, item.getEntryId());
        contentValues.put(OrderEntry.COLUMN_NAME_NAME, item.getName());
        contentValues.put(OrderEntry.COLUMN_NAME_WAITER_ID, item.getWaiterId());
        contentValues.put(OrderEntry.COLUMN_NAME_SYNCED, item.getSynced());
        contentValues.put(OrderEntry.COLUMN_NAME_CHECKED_OUT, item.getCheckedOut());
        contentValues.put(OrderEntry.COLUMN_NAME_TIME_STAMP, item.getTimeStamp());
        contentValues.put(OrderEntry.COLUMN_NAME_FLAGGED_FOR_CHECKOUT, item.getCheckoutFlagged());
        contentValues.put(OrderEntry.COLUMN_NAME_PAYMENT_METHOD, item.getPaymentMethod());
        contentValues.put(OrderEntry.COLUMN_NAME_AMOUNT, item.getAmount());
        contentValues.put(OrderEntry.COLUMN_NAME_TRANSACTION_ID, item.getTransactionId());
        contentValues.put(OrderEntry.COLUMN_NAME_PROCESS_STATE, item.getProcessState());
        contentValues.put(OrderEntry.COLUMN_NAME_COUNTERA_SYNCED, item.getCounterASync());
        contentValues.put(OrderEntry.COLUMN_NAME_COUNTERB_SYNCED, item.getCounterBSync());
        contentValues.put(OrderEntry.COLUMN_NAME_DATE, item.getDate());
        long rowId = mDatabaseHelper.insert(OrderEntry.TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return getLastCreated();
    }

    @Override
    public Order getLastCreated(){
        String selectQuery = "SELECT * FROM " + OrderEntry.TABLE_NAME + " sqlite_sequence";
        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        cursor.moveToLast();
        Order orderCursor = getOrder(cursor);
        return orderCursor;
    }

    @Override
    public int delete(String id) {
        String selection = OrderEntry.COLUMN_NAME_ENTRY_ID + " LIKE ? ";
        String selectionArgs[] = {id};
        return mDatabaseHelper.delete(OrderEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Order item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(OrderEntry.COLUMN_NAME_ENTRY_ID, item.getEntryId());
        contentValues.put(OrderEntry.COLUMN_NAME_DISPLAY_ID,item.getDisplayId());
        contentValues.put(OrderEntry.COLUMN_NAME_NAME, item.getName());
        contentValues.put(OrderEntry.COLUMN_NAME_WAITER_ID, item.getWaiterId());
        contentValues.put(OrderEntry.COLUMN_NAME_SYNCED, item.getSynced());
        contentValues.put(OrderEntry.COLUMN_NAME_CHECKED_OUT, item.getCheckedOut());
        contentValues.put(OrderEntry.COLUMN_NAME_TIME_STAMP, item.getTimeStamp());
        contentValues.put(OrderEntry.COLUMN_NAME_FLAGGED_FOR_CHECKOUT, item.getCheckoutFlagged());
        contentValues.put(OrderEntry.COLUMN_NAME_PAYMENT_METHOD, item.getPaymentMethod());
        contentValues.put(OrderEntry.COLUMN_NAME_AMOUNT, item.getAmount());
        contentValues.put(OrderEntry.COLUMN_NAME_TRANSACTION_ID, item.getTransactionId());
        contentValues.put(OrderEntry.COLUMN_NAME_PROCESS_STATE, item.getProcessState());
        contentValues.put(OrderEntry.COLUMN_NAME_COUNTERA_SYNCED, item.getCounterASync());
        contentValues.put(OrderEntry.COLUMN_NAME_COUNTERB_SYNCED, item.getCounterBSync());
        contentValues.put(OrderEntry.COLUMN_NAME_DATE, item.getDate());
        String selection = OrderEntry.COLUMN_NAME_ENTRY_ID + " LIKE?";
        String[] selectionArgs = {item.getEntryId()};
        return mDatabaseHelper.update(OrderEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }

    @Override
    public void deleteAll() {
        mDatabaseHelper.delete(OrderEntry.TABLE_NAME, null);
    }

    @Override
    public Order getOrderFromRowId(Long rowId) {
        checkNotNull(rowId);
         String sql = String.format("SELECT * FROM %s WHERE ROWID = %d LIMIT 1",
                 OrderEntry.TABLE_NAME, rowId);
        Cursor cursor = mDatabaseHelper.query(sql, (String[]) null);
        Order order = getOrder(cursor);
        cursor.close();
        return order;
    }

    @Override
    public Observable<List<Order>> getOrdersWithSynced(Integer isSynced) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = %d", TextUtils.join(",", projection),
                OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_SYNCED, isSynced);
        rx.Observable<List<Order>> observableV1 =
                mDatabaseHelper
                        .createQuery(OrderEntry.TABLE_NAME, sql)
                        .mapToList(mOrderMapperFunction)
                        .take(1000, TimeUnit.MILLISECONDS);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public Observable<List<Order>> getOrdersWithCounterASynced(Integer isCounterASynced) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = %d", TextUtils.join(",", projection),
                OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_COUNTERA_SYNCED, isCounterASynced);
        rx.Observable<List<Order>> observableV1 =
                mDatabaseHelper
                        .createQuery(OrderEntry.TABLE_NAME, sql)
                        .mapToList(mOrderMapperFunction)
                        .take(1000, TimeUnit.MILLISECONDS);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public Observable<List<Order>> getOrdersWithCounterBSynced(Integer isCounterBSynced) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = %d", TextUtils.join(",", projection),
                OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_COUNTERB_SYNCED, isCounterBSynced);
        rx.Observable<List<Order>> observableV1 =
                mDatabaseHelper
                        .createQuery(OrderEntry.TABLE_NAME, sql)
                        .mapToList(mOrderMapperFunction)
                        .take(1000, TimeUnit.MILLISECONDS);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public Observable<List<Order>> getOrdersForCheckout(Integer checkout, Integer checkoutFlagged) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = %d AND %s = %d"
                , TextUtils.join(",", projection),
                OrderEntry.TABLE_NAME,
                OrderEntry.COLUMN_NAME_CHECKED_OUT, checkout,
                OrderEntry.COLUMN_NAME_FLAGGED_FOR_CHECKOUT, checkoutFlagged);
        rx.Observable<List<Order>> observableV1 =
                mDatabaseHelper
                        .createQuery(OrderEntry.TABLE_NAME, sql)
                        .mapToList(mOrderMapperFunction)
                        .take(1000, TimeUnit.MILLISECONDS);

        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public ArrayList<String> getOrdersWithTimestamp() {
        String sql = " SELECT date(timestamp) FROM order_table  GROUP BY date(timestamp) ORDER BY timestamp ";

        Cursor cursor = mDatabaseHelper.query(sql, (String[]) null);
        ArrayList<String> timeStamps = new ArrayList<>();
        while (cursor.moveToNext()) {
            String state = cursor.getString(0);
            timeStamps.add(state);
        }
        cursor.close();

        return timeStamps;
    }

    public void updateProcessState(String entryId,int state){

        String selectQuery = " UPDATE " + OrderEntry.TABLE_NAME + " set  " + OrderEntry.COLUMN_NAME_PROCESS_STATE + " = "+state+" WHERE " + OrderEntry.COLUMN_NAME_ENTRY_ID + String.format(" = '%s'",entryId) ;
//        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        mDatabaseHelper.execute(selectQuery);
//        cursor.close();

    }

    public int getProcessState(String entryId) {
        String selectQuery = " SELECT " + OrderEntry.COLUMN_NAME_PROCESS_STATE + " FROM " + OrderEntry.TABLE_NAME + " WHERE " + OrderEntry.COLUMN_NAME_ENTRY_ID + String.format(" = '%s'", entryId);
        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        if (cursor.moveToNext()) {
            int state = cursor.getInt(0);
            return state;
        } else {
            return 0;

        }
    }

    public void updateSyncState(String entryId,int state){
        String selectQuery = " UPDATE " + OrderEntry.TABLE_NAME + " set  " + OrderEntry.COLUMN_NAME_SYNCED + " = "
                +state+" WHERE " + OrderEntry.COLUMN_NAME_ENTRY_ID + String.format(" = '%s'",entryId) ;
//        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        mDatabaseHelper.execute(selectQuery);
//        cursor.close();

    }

    public void updateCounterASyncState(String counter, String entryId,int state){
        String selectQuery = " UPDATE " + OrderEntry.TABLE_NAME + " set  " + OrderEntry.COLUMN_NAME_COUNTERA_SYNCED + " = "
                +state+" WHERE " + OrderEntry.COLUMN_NAME_ENTRY_ID + String.format(" = '%s'",entryId) ;
//        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        mDatabaseHelper.execute(selectQuery);
//        cursor.close();

    }

    public void updateCounterBSyncState(String counter,String entryId,int state){
        String selectQuery = " UPDATE " + OrderEntry.TABLE_NAME + " set  " + OrderEntry.COLUMN_NAME_COUNTERB_SYNCED + " = "
                +state+" WHERE " + OrderEntry.COLUMN_NAME_ENTRY_ID + String.format(" = '%s'",entryId) ;
//        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        mDatabaseHelper.execute(selectQuery);
//        cursor.close();

    }
    public int getSyncState(String entryId){
        String selectQuery = " SELECT "+OrderEntry.COLUMN_NAME_SYNCED +" FROM "+ OrderEntry.TABLE_NAME
                +" WHERE " + OrderEntry.COLUMN_NAME_ENTRY_ID + String.format(" = '%s'",entryId) ;
        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        if(cursor.moveToNext()) {
            int state = cursor.getInt(0);
            return state;
        }else{
            return 0;

        }
    }
    public int getSyncStateA(){
        String selectQuery = " SELECT case WHEN ( " + OrderEntry.COLUMN_NAME_COUNTERA_SYNCED + " == 0) THEN 1 else 0 end " +
                " FROM order_table WHERE " +OrderEntry.COLUMN_NAME_COUNTERA_SYNCED + " = 0 LIMIT 1 ";
        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        if(cursor.moveToNext()) {
            int counter = cursor.getInt(0);
            return counter;
        }else{
            return 0;

        }
    }
    public int getSyncStateB(){
        String selectQuery = " SELECT case WHEN ( " + OrderEntry.COLUMN_NAME_COUNTERB_SYNCED + " == 0) THEN 1 else 0 end " +
                " FROM order_table WHERE " +OrderEntry.COLUMN_NAME_COUNTERB_SYNCED + " = 0 LIMIT 1 ";
        Cursor cursor = mDatabaseHelper.query(selectQuery, (String[]) null);
        if(cursor.moveToNext()) {
            int counter = cursor.getInt(0);
            return counter;
        }else{
            return 0;



        }
    }
    public List<String> getDate(){
        String selectQuery =  "SELECT date FROM order_table GROUP BY date ORDER BY date DESC";
        Cursor cursor = mDatabaseHelper.query(selectQuery,(String[]) null);
        List<String> orderDates = new ArrayList<>();
            while (cursor.moveToNext()){
                String date = cursor.getString(0);
                orderDates.add(date);
            }
        return orderDates;
    }
    public Observable<List<Order>> getOrdersPerDate(String date) {
        if (date == null){
            return Observable.empty();
        }
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ? " ,
                TextUtils.join(",", projection), OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_DATE);
        rx.Observable<List<Order>> observableV1 = mDatabaseHelper.createQuery(OrderEntry.TABLE_NAME, sql,date)
                .mapToList(mOrderMapperFunction);
        // convert observable from rxjava1 observable to rxjava2 observable
        Observable<List<Order>> observableV2 = RxJavaInterop.toV2Observable(observableV1);
        return observableV2;
    }

    @Override
    public List<Order> getOrdersForEachDate(String date) {
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ? " ,
                TextUtils.join(",", projection), OrderEntry.TABLE_NAME, OrderEntry.COLUMN_NAME_DATE);
       Cursor cursor = mDatabaseHelper.query(sql,date);
       List<Order> orderList = new ArrayList<>();
        try {
            while (cursor.moveToNext()){
                Order order = getOrder(cursor);
                orderList.add(order);
            }
             return orderList;
        } finally {
            cursor.close();

        }
    }
}


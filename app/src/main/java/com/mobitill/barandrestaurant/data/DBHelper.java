package com.mobitill.barandrestaurant.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.mobitill.barandrestaurant.data.order.source.local.OrderPersistenceContract.OrderEntry;
import static com.mobitill.barandrestaurant.data.orderItem.source.local.OrderItemPersistenceContract.OrderItemEntry;
import static com.mobitill.barandrestaurant.data.product.source.local.ProductPersistenceContract.ProductEntry;
import static com.mobitill.barandrestaurant.data.waiter.source.local.WaitersPersistenceContract.WaitersEntry;

/**
 * Created by james on 4/30/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "BarAndRestaurant.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String BOOLEAN_TYPE = " INTEGER";
    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = " ,";

    private static final String SQL_CREATE_WAITER_ENTRIES =
            "CREATE TABLE " + WaitersEntry.TABLE_NAME + " (" +
                    WaitersEntry._ID + INTEGER_TYPE + " PRIMARY KEY," +
                    WaitersEntry.COLUMN_NAME_ID + TEXT_TYPE + " UNIQUE," +
                    WaitersEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    WaitersEntry.COLUMN_NAME_PHONE + TEXT_TYPE + COMMA_SEP +
                    WaitersEntry.COLUMN_NAME_PASSWORD + TEXT_TYPE + " )";

    //NewProduct Table
    private static final String SQL_CREATE_PRODUCT_ENTRIES =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                    ProductEntry._ID + INTEGER_TYPE + " PRIMARY KEY," +
                    ProductEntry.COLUMN_NAME_ID + TEXT_TYPE + " UNIQUE," +
                    ProductEntry.COLUMN_NAME_BARCODE + TEXT_TYPE + COMMA_SEP +
                    ProductEntry.COLUMN_NAME_IDENTIFIER + TEXT_TYPE + COMMA_SEP +
                    ProductEntry.COLUMN_NAME_PRODUCT_NAME + TEXT_TYPE + COMMA_SEP +
                    ProductEntry.COLUMN_NAME_PRICE + TEXT_TYPE + COMMA_SEP +
                    ProductEntry.COLUMN_NAME_VAT + TEXT_TYPE + COMMA_SEP +
                    ProductEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + " )";

    //Orders Table
    private static final String SQL_CREATE_ORDER_ENTRIES =
            "CREATE TABLE " + OrderEntry.TABLE_NAME + " (" +
                    OrderEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + " UNIQUE," +
                    OrderEntry.COLUMN_NAME_DISPLAY_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT," +
                    OrderEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_WAITER_ID + TEXT_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_SYNCED + BOOLEAN_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_COUNTERA_SYNCED + BOOLEAN_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_COUNTERB_SYNCED + BOOLEAN_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_CHECKED_OUT + BOOLEAN_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_FLAGGED_FOR_CHECKOUT + BOOLEAN_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_PAYMENT_METHOD + TEXT_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_AMOUNT + TEXT_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_TRANSACTION_ID + TEXT_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_PROCESS_STATE+INTEGER_TYPE+COMMA_SEP +
                    OrderEntry.COLUMN_NAME_TIME_STAMP + INTEGER_TYPE + COMMA_SEP +
                    OrderEntry.COLUMN_NAME_DATE + TEXT_TYPE +
                    " )";

    //OrderItems Table
    private static final String SQL_CREATE_ORDER_ITEM_ENTRIES =
            "CREATE TABLE " + OrderItemEntry.TABLE_NAME + " (" +
                    OrderItemEntry._ID + INTEGER_TYPE + " PRIMARY KEY," +
                    OrderItemEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_PRODUCT_ID + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_ORDER_ID + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_COUNTER + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_SYNCED + BOOLEAN_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_CHECKED_OUT + BOOLEAN_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_PRODUCT_NAME + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_PRODUCT_PRICE + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    OrderItemEntry.COLUMN_NAME_TIME_STAMP + INTEGER_TYPE + " )";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCT_ENTRIES);
        db.execSQL(SQL_CREATE_WAITER_ENTRIES);
        db.execSQL(SQL_CREATE_ORDER_ENTRIES);
        db.execSQL(SQL_CREATE_ORDER_ITEM_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // not required as at version 1
    }
}

package com.mobitill.barandrestaurant.data.order.source.local;

import android.provider.BaseColumns;

/**
 * Created by andronicus on 5/16/2017.
 */

public final class OrderPersistenceContract{

    //To prevent someone from accidentally instantiating the contract class
    //give it an empty constructor
    private OrderPersistenceContract() {
    }

    /**Inner class that defines table contents*/
    public static abstract class OrderEntry implements BaseColumns {
        public static final String TABLE_NAME = "order";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_WAITER = "name";
    }
}
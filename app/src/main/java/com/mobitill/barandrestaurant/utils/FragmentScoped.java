package com.mobitill.barandrestaurant.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by james on 4/21/2017.
 */
@Documented
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentScoped {
}

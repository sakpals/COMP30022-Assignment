package net.noconroy.itproject.application;

import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.MatrixError;

/**
 * Created by matt on 16/08/17.
 */

public abstract class Callback<T> implements ApiCallback<T> {
    public abstract void onGood(T item);
    public abstract void onBad(Exception e);

    @Override
    public void onSuccess(T i) {
        onGood(i);
    }

    @Override
    public void onNetworkError(Exception e) {
        onBad(e);
    }

    @Override
    public void onMatrixError(MatrixError e) {
        onBad(new Exception(e.getMessage()));
    }

    @Override
    public void onUnexpectedError(Exception e) {
        onBad(e);
    }
}

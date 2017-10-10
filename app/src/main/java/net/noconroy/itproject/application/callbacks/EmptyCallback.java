package net.noconroy.itproject.application.callbacks;

import android.app.Activity;
import android.support.annotation.Nullable;

/**
 * Created by matt on 10/10/17.
 */

public abstract class EmptyCallback extends NetworkCallback<Void> {
    /**
     * @param a Activity to perform UI actions in, can be null
     */
    public EmptyCallback(Activity a) {
        super(null, a);
    }

    @Override
    public void onSuccess(Void _){};
    public abstract void onSuccess();
}

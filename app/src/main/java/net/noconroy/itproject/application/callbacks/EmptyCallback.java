package net.noconroy.itproject.application.callbacks;

import android.support.annotation.Nullable;

/**
 * Created by matt on 10/10/17.
 */

public abstract class EmptyCallback extends NetworkCallback<Void> {
    public EmptyCallback() {
        super(null);
    }
}

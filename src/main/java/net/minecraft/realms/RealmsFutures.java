package net.minecraft.realms;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public final class RealmsFutures
{
    private RealmsFutures() {}

    public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback)
    {
        Futures.addCallback(future, callback, MoreExecutors.directExecutor());
    }
}

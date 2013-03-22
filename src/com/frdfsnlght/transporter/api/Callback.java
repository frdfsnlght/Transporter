/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter.api;

/**
 * Represents a task to be executed after an asynchronous API call has completed.
 * <p>
 * There are many API methods that must make calls to remote servers. These
 * calls are performed asynchronously to prevent blocking the main thread in
 * the calling server. Subclasses of this class can be used to provide code
 * that should be called when the asynchronous call is complete.
 * <p>
 * Typical use of this class is by declaring an anonymous instance and
 * overriding either/both the optional <code>onSuccess</code> or optional <code>onFailure</code>
 * methods. The method bodies for these methods can then call user code
 * to handle the results of the asynchronous call.
 * <p>
 * For example, to find out what environment a world on a remote server is
 * configured with, you could use the following code:
 * <p>
 * <pre>
 * {@code
 * RemoteWorld world = remoteServer.getRemoteWorld("world");
 * world.getEnvironment(new Callback<Environment>() {
 *     public void onSuccess(Environment env) {
 *         System.out.println("the remote environment is " + env.toString());
 *     }
 *     public void onFailure(RemoteException e) {
 *         System.err.println("exception: " + e.getMessage());
 *     }
 * });
 * }
 * </pre>
 * <p>
 * Overriding either method is optional. If the <code>onSuccess</code> method is
 * not overridden, the asynchronous return value is ignored. If the <code>onFailure</code>
 * method is not overridden, any exceptions are ignored.
 * <p>
 * If neither method is overridden,
 * then there's no point even including the subclass because you're not interested in
 * the result, so just specify <code>null</code> instead of providing a callback.
 *
 * @param <T> the return type of the callback
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class Callback<T> {

    /**
     * The time the callback object was created, in milliseconds.
     */
    protected long createTime;

    /**
     * The default constructor.
     */
    public Callback() {
        createTime = System.currentTimeMillis();

    }

    /**
     * Returns the time when this callback was created.
     *
     * @return the time, in milliseconds
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Returns the current age of this callback.
     *
     * @return the number of milliseconds since this callback was created
     */
    public long getAge() {
        return System.currentTimeMillis() - createTime;
    }

    /**
     * Called after successful completion of an asynchronous call.
     * <p>
     * This method is called on the main server thread.
     *
     * @param t     the return value of the asynchronous call
     */
    public void onSuccess(T t) {}

    /**
     * Called when an exception occurs during the asynchronous call.
     * <p>
     * Usually, the exception comes from the remote side of the call,
     * but it can also come from the local side, for example, when the
     * call times out.
     * <p>
     * This method is called on the main server thread.
     *
     * @param e the exception
     */
    public void onFailure(RemoteException e) {}

}

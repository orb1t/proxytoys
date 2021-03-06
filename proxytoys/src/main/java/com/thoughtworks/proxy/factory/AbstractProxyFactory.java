/*
 * (c) 2003-2005, 2009, 2010 ThoughtWorks Ltd
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03-May-2004
 */
package com.thoughtworks.proxy.factory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import com.thoughtworks.proxy.Invoker;
import com.thoughtworks.proxy.ProxyFactory;


/**
 * An abstract implementation of a ProxyFactory.
 * <p>
 * Precondition for the derived implementation is the support of an interface for the invocation handler, that has the
 * same methods with the same signature as {@link java.lang.reflect.InvocationHandler}. Additionally it supports the
 * method {@link #getInvoker} of the proxy instance.
 * </p>
 *
 * @author Aslak Helles&oslash;y
 * @since 0.1
 */
abstract class AbstractProxyFactory implements ProxyFactory {
    
    private static final long serialVersionUID = 9212825902080759794L;
    
    /**
     * The getInvoker method.
     */
    public static final Method getInvoker;

    static {
        try {
            getInvoker = InvokerReference.class.getMethod("getInvoker");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e.toString());
        }
    }

    /**
     * Generic implementation of a invocation handler with a JDK compatible method and signature.
     * <p>
     * This is a serendipitous class - it can be extended, and the subclass made to implement either
     * <tt>{@link java.lang.reflect.InvocationHandler}</tt> or the CGLIB
     * <tt>{@link net.sf.cglib.proxy.InvocationHandler}</tt> because they both conveniently have exactly the same
     * <tt>invoke</tt> method with the same signature.
     * </p>
     * <p>
     * Clever, eh?
     * </p>
     *
     * @since 0.1
     */
    static class CoincidentalInvocationHandlerAdapter implements Serializable {
        private static final long serialVersionUID = -7406561726778120065L;
        private Invoker invoker;

        /**
         * Construct a CoincidentalInvocationHandlerAdapter.
         *
         * @param invocationInterceptor the invocation handler.
         * @since 0.1
         */
        public CoincidentalInvocationHandlerAdapter(final Invoker invocationInterceptor) {
            this.invoker = invocationInterceptor;
        }

        /**
         * Invoke a method on an object.
         *
         * @param proxy  the proxy on that the method was originally called
         * @param method the method
         * @param args   the arguments of the call
         * @return the return value of the call
         * @throws Throwable if calling code throws or the call failed
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
         *      java.lang.Object[])
         * @see net.sf.cglib.proxy.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
         *      java.lang.Object[])
         */
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.equals(AbstractProxyFactory.getInvoker)) {
                return invoker;
            }
            try {
                return invoker.invoke(proxy, method, args);
            } catch (UndeclaredThrowableException e) {
                throw e.getUndeclaredThrowable();
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    /**
     * {@inheritDoc} The implementation of this method relies on the implementation of the derived factory to add the
     * interface {@link InvokerReference} to every proxy instance.
     *
     * @since 0.1
     */
    public Invoker getInvoker(final Object proxy) {
        final InvokerReference ih = InvokerReference.class.cast(proxy);
        return ih.getInvoker();
    }
}
package com.morkim.tectonic.usecase;

@SuppressWarnings("unchecked")
public class Unwrapper<E extends Exception> {

    private E e;

    public Unwrapper unwrap(E e) {
        this.e = e;
        return this;
    }

    public <T extends Exception> Unwrapper cause(Class<T> cause) throws T {
        if (e.getCause().getClass() == cause) throw (T) e.getCause();
        return this;
    }

    public void wrapper() throws E {
        throw e;
    }
}

package com.morkim.tectonic.simplified;

import com.google.common.util.concurrent.SettableFuture;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThreadTest {

    @BeforeClass
    public static void setupClass() {

    }

    @Before
    public void setup() {
        UseCase.clearAll();
    }

    @Test
    public void general_thread_interrupt_behavior() {

        assertFalse(Thread.currentThread().isInterrupted());
        // you can do this from another thread by saying: someThread.interrupt();
        Thread.currentThread().interrupt();
        // this method does _not_ clear the interrupt flag
        assertTrue(Thread.currentThread().isInterrupted());
        // but this one _does_ and should probably not be used
        assertTrue(Thread.interrupted());
        assertFalse(Thread.currentThread().isInterrupted());
        Thread.currentThread().interrupt();
        assertTrue(Thread.currentThread().isInterrupted());
        try {
            // this throws immediately because the thread is _already_ interrupted
            Thread.sleep(1);
            fail("will never get here");
        } catch (InterruptedException e) {
            // and when the InterruptedException is throw, it clears the interrupt
            assertFalse(Thread.currentThread().isInterrupted());
            // we should re-interrupt the thread so other code can use interrupt status
            Thread.currentThread().interrupt();
        }
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    public void terminate_thread_waiting_to_finish() {

        ThreadManager threadManager = new ThreadManagerImpl();
        threadManager.start(new ThreadManager.UseCaseExecution() {
            @Override
            public void run() throws InterruptedException {

            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadManager.stop();

        assertFalse(threadManager.isRunning());
    }

    @Test
    public void restart_thread_waiting_to_finish() {

        ThreadManager threadManager = new ThreadManagerImpl();
        threadManager.start(new ThreadManager.UseCaseExecution() {
            @Override
            public void run() throws InterruptedException {

            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadManager.restart();

        assertTrue(threadManager.isRunning());
    }

    @Test
    public void restart_terminated_thread() {

        ThreadManager threadManager = new ThreadManagerImpl();
        threadManager.start(new ThreadManager.UseCaseExecution() {
            @Override
            public void run() throws InterruptedException {

            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadManager.stop();
        threadManager.restart();

        assertFalse(threadManager.isRunning());
    }

    @Test
    public void unfinished_thread__still_running() {

        ThreadManager threadManager = new ThreadManagerImpl();
        threadManager.start(new ThreadManager.UseCaseExecution() {
            @Override
            public void run() throws InterruptedException {

            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(threadManager.isRunning());
    }

    @Test
    public void terminate_thread_waiting_for_action() {

        ThreadManager threadManager = new ThreadManagerImpl();
        threadManager.start(new ThreadManager.UseCaseExecution() {
            @Override
            public void run() throws InterruptedException {
                SettableFuture future = SettableFuture.create();
                try {
                    future.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadManager.stop();

        assertFalse(threadManager.isRunning());
    }

    @Test
    public void restart_thread_waiting_for_action() {

        ThreadManager threadManager = new ThreadManagerImpl();
        threadManager.start(new ThreadManager.UseCaseExecution() {
            @Override
            public void run() throws InterruptedException {
                SettableFuture future = SettableFuture.create();
                try {
                    future.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadManager.restart();

        assertTrue(threadManager.isRunning());
    }
}
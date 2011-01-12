/*
 * InterruptibleThread.java
 *
 * Copyright ?2009 Research In Motion Limited.  All rights reserved.
 */

package blackberry.web.widget.threading;

import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connection;

public class InterruptibleThread extends Thread {
    private Object _lock;   // we don't want to use "this" as the lock object since
                            // subclasses may potentially synchronize on that for
                            // long periods of time, preventing interruption
    private boolean _dead;
    private boolean _interrupted;
    private int _softInterrupt;

    /**
     * Default constructor for classes that wish to extend InterruptibleThread.
     */
    public InterruptibleThread() {
        _lock = new Object();
    }

    /**
     * Construct a new InterruptibleThread with a given name.
     *
     * @param name the name of the new thread.
     */
    public InterruptibleThread( String name ) {
        super( name );
        _lock = new Object();
    }

    /**
     * Construct a new InterruptibleThread with a Runnable.
     *
     * @param r The Runnable that will be run in the new thread.
     */
    public InterruptibleThread( Runnable r ) {
        super( r );
        _lock = new Object();
    }

    /**
     * Construct a new InterruptibleThread with a Runnable and a given name.
     *
     * @param r The Runnable that will be run in the new thread.
     * @param name the name of the new thread.
     */
    public InterruptibleThread( Runnable r, String name ) {
        super( r, name );
        _lock = new Object();
    }

    /**
     * Get the currently running <code>InterruptibleThread</code>, or null if the current
     * thread is not an instance of <code>InterruptibleThread</code>.
     */
    public static InterruptibleThread getCurrent() {
        Thread thread = Thread.currentThread();
        if (thread instanceof InterruptibleThread) {
            return (InterruptibleThread)thread;
        }
        return null;
    }

    /**
     * Interrupt this thread. In addition to calling <code>super.interrupt()</code>, which will break
     * the thread out of any <code>wait()</code> calls with an <code>InterruptedException</code>, this method
     * sets the interrupted flag (which can be checked using <code>isInterrupted()</code> and
     * <code>throwIfInterrupted()</code> or the static versions <code>isCurrentInterrupted()</code>
     * and <code>throwIfCurrentInterrupted()</code>) and closes any registered <code>InputStream</code>
     * and <code>Connection</code> objects which should trigger <code>IOException</code>s if read from.
     */
    /* @Override */ public void interrupt() {
        synchronized (_lock) {
            if (_interrupted) {
                // already interrupted
                return;
            }

            _interrupted = true;
        }
        super.interrupt();
    }

    /**
     * Do a soft interrupt on this thread. This basically does nothing except set a thread-specific interrupt
     * level that can be queried by <code>getInterruptLevel()</code> and reset by <code>reset()</code>. If
     * the thread was previously soft-interrupted, the higher of the two levels is kept.
     *
     * @param level The level of the soft interrupt. Higher values indicate more urgent (less "soft") interrupts.
     *              A value of zero clears the soft interrupt.
     *
     * @throws IllegalArgumentException if <code>level</code> is less than zero.
     */
    public void interrupt( int level ) throws IllegalArgumentException {
        if (level < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (_lock) {
            if (level == 0 || level > _softInterrupt) {
                _softInterrupt = level;
            }
        }
    }

    /**
     * A better alternative to <code>join()</code>. This implementation is similar to the
     * <code>join()</code> method in <code>Thread</code>, but uses a call to <code>wait()</code>
     * instead of a busy loop to avoid hogging the CPU.
     */
    public void waitJoin() throws InterruptedException {
        synchronized (_lock) {
            if (_dead) {
                return;
            }
            _lock.wait();
        }
    }

    /**
     * Subclasses should override this method with their specific run code instead of
     * overriding the <code>run()</code> method. This method is equivalent to the
     * <code>Thread.run()</code> method. By default this calls <code>super.run()</code>.
     */
    public void joinableRun() {
        super.run();
    }

    /**
     * A run implementation that includes a hook for <code>waitJoin()</code>. This implementation
     * is final so that the hook cannot be removed by subclasses. Subclasses that wish to extend
     * this class with their own <code>run()</code> method should override the <code>joinableRun()</code>
     * method instead. If this thread is created with a <code>Runnable</code>, it will be run normally.
     */
    /* @Override */ public final void run() {
        try {
            joinableRun();
        } finally {
            synchronized (_lock) {
                // notify callers of waitJoin() that the thread is dead
                _dead = true;
                _lock.notifyAll();
            }
        }
    }

    /**
     * Reset the thread. This method will clear the interrupt and soft interrupt flags and unregister any registered objects.
     */
    public void reset() {
        synchronized (_lock) {
            _interrupted = false;
            _softInterrupt = 0;
        }
    }

    /**
     * Reset the currently-running <code>InterruptibleThread</code>, if there is one. If the current
     * thread is not an <code>InterruptibleThread</code>, no action is performed.
     */
    public static void resetCurrent() {
        InterruptibleThread current = getCurrent();
        if (current != null) {
            current.reset();
        }
    }

    /**
     * Check if the thread has been interrupted.
     *
     * @return true if the instance has been interrupted, false otherwise. Unlike the Java
     *         <code>isInterrupted()</code> method, calling this method will NOT clear the flag.
     */
    public boolean isInterrupted() {
        synchronized (_lock) {
            return _interrupted;
        }
    }

    /**
     * Check if the thread has been softly interrupted.
     *
     * @return the level of the soft interruption. A value greater than zero indicates a
     *         soft interrupt.
     *
     * @throws Interruption if the thread has been interrupted.
     */
    public int getInterruptLevel() throws Interruption {
        synchronized (_lock) {
            if (_interrupted) {
                throw new Interruption();
            }
            return _softInterrupt;
        }
    }

    /**
     * Check if the currently running <code>InterruptibleThread</code> has been interrupted.
     *
     * @return true if the currently running thread is an <code>InterruptibleThread</code> AND
     *         it has been interrupted. Returns false otherwise.
     */
    public static boolean isCurrentInterrupted() {
        InterruptibleThread thread = getCurrent();
        if (thread != null) {
            return thread.isInterrupted();
        }
        return false;
    }

    /**
     * Check if the thread has been interrupted; if it has, throw an <code>Interruption</code>.
     *
     * @throws Interruption if the thread has been interrupted.
     */
    public void throwIfInterrupted() /*throws Interruption*/ {
        synchronized (_lock) {
            if (_interrupted) {
                throw new Interruption();
            }
        }
    }

    /**
     * Check if the currently running <code>InterruptibleThread</code> has been interrupted; if
     * it has, throw an <code>Interruption</code>.
     *
     * @return true if the currently running thread is an <code>InterruptibleThread</code> AND
     *         it has not yet been interrupted. false if the currently running thread is
     *         NOT an <code>InterruptibleThread</code>. This is provided to allow users to
     *         optionally avoid future calls to this method, since a false return value means
     *         the thread cannot be interrupted.
     * @throws Interruption if the currently running thread is an <code>InterruptibleThread</code>
     *         AND it has been interrupted.
     */
    public static boolean throwIfCurrentInterrupted() throws Interruption {
        // this could be written using calls to getCurrent() and throwIfInterrupted(),
        // but this call is likely to be called often inside loops and such, so inline
        // those calls for optimization
        Thread t = Thread.currentThread();
        if (t instanceof InterruptibleThread) {
            InterruptibleThread it = (InterruptibleThread)t;
            synchronized (it._lock) {
                if (it._interrupted) {
                    throw new Interruption();
                }
            }
            return true;
        }
        return false;
    }
}

# ReentrantLock Part 1 — `tryLock()` and `lockInterruptibly()`

## 1. What is `ReentrantLock`?

`ReentrantLock` is a lock implementation in Java:

```java
import java.util.concurrent.locks.ReentrantLock;
```

It works similarly to `synchronized`:
**only one thread can access a shared resource at a time.**

### With `synchronized`

```java
synchronized (lockObject) {
    use(resource);
}
```

### With `ReentrantLock`

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();

try {
    use(resource);
} finally {
    lock.unlock();
}
```

The main difference is that `ReentrantLock` gives us **more control and flexibility**.

---

## 2. The main risk of `ReentrantLock`

When using:

```java
lock.lock();
```

Java does **not automatically unlock** it for us.

If an exception happens before:

```java
lock.unlock();
```

the lock may remain locked forever.

Bad example:

```java
lock.lock();

someOperations();

lock.unlock();
```

If `someOperations()` throws an exception, `unlock()` will never run.

---

## 3. Always use `try-finally`

A safer way:

```java
lock.lock();

try {
    someOperations();
} finally {
    lock.unlock();
}
```

The `finally` block will still run even if an exception happens.

So remember this rule:

> After `lock()` succeeds, always put `unlock()` inside `finally`.

---

## 4. Why use `ReentrantLock` instead of only `synchronized`?

`ReentrantLock` provides extra features that are not available, or are harder to achieve, with `synchronized`.

For example:

```java
lock.isLocked();
```

This lets us check whether the lock is currently held.

There are also APIs that help inspect waiting threads, which can be useful for:

- debugging
- testing
- monitoring lock state

---

## 5. Fair Lock

By default:

```java
new ReentrantLock();
```

does not guarantee that the thread that waited first will get the lock first.

We can create a **fair lock**:

```java
ReentrantLock lock = new ReentrantLock(true);
```

Conceptually:

```text
Thread 1 ----\
Thread 2 -----\
Thread 3 ------> LOCK -> RESOURCE
Thread 4 -----/
```

With fairness enabled, the lock tries to give access to threads that have been waiting longer.

### Note

Fair locks may be more predictable, but they usually add some performance overhead.

---

# `lockInterruptibly()`

## 6. Problem with `lock()`

Suppose we call:

```java
lock.lock();
```

but another thread is already holding the lock.

The current thread will wait:

```text
Thread
   |
   v
wait for lock...
   |
   v
wait...
   |
   v
wait...
```

While waiting, it is difficult to tell that thread to stop waiting.

---

## 7. `lockInterruptibly()`

`ReentrantLock` supports:

```java
lock.lockInterruptibly();
```

Unlike `lock()`, a thread waiting for the lock can be **interrupted**.

Example:

```java
try {
    lock.lockInterruptibly();

    try {
        useResource();
    } finally {
        lock.unlock();
    }

} catch (InterruptedException e) {
    cleanUpAndExit();
}
```

If the thread is waiting and another thread calls:

```java
thread.interrupt();
```

the waiting thread can receive:

```java
InterruptedException
```

and stop waiting.

---

## 8. When is `lockInterruptibly()` useful?

An important use case is:

**Deadlock detection / recovery**

Example:

```text
Thread A
   |
   v
waiting for lock
   |
   | watchdog detects a problem
   v
interrupt()
   |
   v
InterruptedException
   |
   v
cleanup / exit
```

Instead of letting the thread wait forever, we can interrupt it and handle the problem.

---

# `tryLock()`

## 9. How does `lock()` behave?

```java
lock.lock();

try {
    useResource();
} finally {
    lock.unlock();
}
```

If the lock is busy:

```text
lock available?
      |
     NO
      |
      v
THREAD WAITS
```

`lock()` may block the thread until the lock becomes available.

---

## 10. How does `tryLock()` behave?

```java
if (lock.tryLock()) {

    try {
        useResource();
    } finally {
        lock.unlock();
    }

} else {
    // do something else
}
```

`tryLock()` tries to acquire the lock **immediately**.

It returns:

```text
true  -> lock acquired
false -> lock is busy
```

Important:

> `tryLock()` does not wait for the lock to become available.

---

## 11. Scenario 1 — Lock is available

```java
if (lock.tryLock()) {
    try {
        useResource();
    } finally {
        lock.unlock();
    }
}
```

If the lock is free:

```text
tryLock()
   |
   v
 true
   |
   v
useResource()
   |
   v
unlock()
```

The thread acquires the lock and uses the resource.

---

## 12. Scenario 2 — Lock is unavailable

Suppose Thread A is already holding the lock.

Thread B runs:

```java
if (lock.tryLock()) {
    ...
} else {
    doSomethingElse();
}
```

Result:

```text
Thread A
   |
   v
holds LOCK


Thread B
   |
   v
tryLock()
   |
   v
 false
   |
   v
doSomethingElse()
```

Thread B is **not blocked**.

---

## 13. The most important idea about `tryLock()`

`tryLock()` behaves like this:

```text
LOCK FREE -> return true immediately
LOCK BUSY -> return false immediately
```

It returns quickly instead of waiting like `lock()`.

---

## 14. When should we use `tryLock()`?

It is useful in systems where threads should not be blocked for a long time.

Examples:

- Video / image processing
- High-speed systems
- Low-latency systems
- Trading systems
- User Interface applications
- Real-time applications

Example with a UI thread:

```text
UI Thread
   |
   v
tryLock()
   |
   +-- true  -> use resource
   |
   +-- false -> skip / try again later

UI stays responsive
```

If we use `lock()`, the UI thread may freeze while waiting for the lock.

---

# Quick Comparison


| Method                | If the lock is busy | Can waiting be interrupted?               | Result                          |
| --------------------- | ------------------- | ----------------------------------------- | ------------------------------- |
| `lock()`              | Waits               | Not normally used to stop waiting         | Wait until lock is acquired     |
| `lockInterruptibly()` | Waits               | Yes                                       | Can throw`InterruptedException` |
| `tryLock()`           | Does not wait       | Not needed because it returns immediately | Returns`true` / `false`         |

---

# Complete Example

```java
import java.util.concurrent.locks.ReentrantLock;

public class Example {

    private final ReentrantLock lock = new ReentrantLock();

    public void process() {

        if (lock.tryLock()) {
            try {
                System.out.println("Using resource...");
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("Resource is busy, do something else...");
        }
    }
}
```

---

# Easy Way to Remember

```text
lock()
= "I will wait until I get the lock."


lockInterruptibly()
= "I will wait, but you can interrupt me."


tryLock()
= "I will try to get the lock right now.
   If I cannot get it, I will not wait."
```

---

# Key Knowledge from This Lesson

```text
ReentrantLock
│
├── lock()
│   └── acquires the lock and may wait
│
├── unlock()
│   └── must be called manually
│
├── lockInterruptibly()
│   └── waits for the lock but can be interrupted
│
├── tryLock()
│   └── tries to acquire the lock and returns immediately
│
├── Query methods
│   └── inspect the lock state
│
└── Fairness
    └── new ReentrantLock(true)
```

## One-Sentence Summary

> `ReentrantLock` is similar to `synchronized`, but more flexible: it supports lock state inspection, fairness, interruptible waiting, and non-blocking lock attempts with `tryLock()`.

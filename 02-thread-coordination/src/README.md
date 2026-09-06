# Thread Coordination in Java

## 1. Why Do We Need Thread Coordination?

When we create multiple threads, they run **independently**.

This means:

- We cannot fully control which thread runs first.
- We cannot guarantee the exact order of execution.
- One thread may finish before another, even if we started them in a certain order.

For example:

```java
threadA.start();
threadB.start();
```

Starting `threadA` before `threadB` does **not** guarantee that `threadA` finishes first.

The operating system and JVM scheduler decide when each thread gets CPU time.

---

## 2. Dependency Between Threads

Sometimes one thread depends on the result of another thread.

Example:

```text
Thread A
   |
   | produces output
   v
Thread B
```

Suppose:

- **Thread A** performs a calculation.
- **Thread B** needs the result of Thread A.

The important question is:

> How does Thread B know when Thread A has finished?

Without coordination, Thread B may try to read the result too early.

---

## 3. Naive Solution: Keep Checking

A simple idea is to let Thread B continuously check whether Thread A has finished.

Example:

```java
void waitForThreadA() {
    while (!threadA.isFinished()) {
        // keep checking
    }
}
```

This is called **busy waiting** or **spinning**.

The thread repeatedly asks:

```text
Is Thread A finished?
No.
Is Thread A finished?
No.
Is Thread A finished?
No.
...
```

### Why is this bad?

Because Thread B is still using CPU time while doing no useful work.

```text
Thread A:  [----------- doing useful work -----------]

Thread B:  check check check check check check check...
           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                    wasted CPU cycles
```

This solution is inefficient, especially if Thread A takes a long time.

---

## 4. Desired Solution

Instead of continuously checking, we want Thread B to **wait without wasting CPU**.

The ideal behavior is:

```text
Thread B checks that Thread A is still running
        |
        v
Thread B waits / sleeps

Thread A continues working

Thread A finishes
        |
        v
Thread B wakes up and continues
```

Conceptually:

```text
Time ------------------------------------------------------>

Thread A:      [========== doing work ==========][finished]

Thread B:      [check] [        waiting         ][continue]
```

While Thread B is waiting, it does not need to repeatedly execute a loop and waste CPU cycles.

Java provides `Thread.join()` for this kind of coordination.

---

# 5. `Thread.join()`

`join()` means:

> Wait for another thread to finish before continuing the current thread.

Important point:

```java
threadA.join();
```

means:

> The **current thread** waits until `threadA` finishes.

It does **not** mean that `threadA` waits.

For example, if `main` executes:

```java
threadA.join();
```

then the **main thread** waits for `threadA`.

---

## 6. Main `join()` Methods

Java provides several versions of `join()`:

```java
public final void join()
```

Wait indefinitely until the target thread finishes.

Example:

```java
threadA.join();
```

---

```java
public final void join(long millis)
```

Wait for the target thread for at most the given number of milliseconds.

Example:

```java
threadA.join(2000);
```

The current thread waits for Thread A for **at most 2 seconds**.

Two possible situations:

```text
1. Thread A finishes before 2 seconds
   -> join() returns immediately when Thread A finishes.

2. Thread A is still running after 2 seconds
   -> join() returns because the timeout has expired.
```

---

```java
public final void join(long millis, int nanos)
```

This is similar to timed `join()`, but allows a more precise timeout using milliseconds and nanoseconds.

Example:

```java
threadA.join(2000, 500000);
```

---

# 7. Basic Example

```java
public class Main {

    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            System.out.println("Worker started");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Worker finished");
        });

        worker.start();

        System.out.println("Main is waiting...");

        worker.join();

        System.out.println("Main continues after worker finishes");
    }
}
```

Expected execution:

```text
Worker started
Main is waiting...

... approximately 3 seconds ...

Worker finished
Main continues after worker finishes
```

The important part is:

```java
worker.join();
```

The main thread pauses there until `worker` terminates.

---

# 8. Example: Calculating Factorials with Multiple Threads

Suppose we want to calculate several factorials in parallel.

```java
List<Long> inputNumbers = Arrays.asList(
        0L,
        3435L,
        35435L,
        2324L,
        4656L,
        23L,
        5556L
);

List<FactorialThread> threads = new ArrayList<>();

for (long inputNumber : inputNumbers) {
    threads.add(new FactorialThread(inputNumber));
}
```

At this point we have created the thread objects, but they have not started yet.

Start all of them:

```java
for (Thread thread : threads) {
    thread.start();
}
```

Now all factorial calculations can run concurrently.

---

## 9. The Problem Without `join()`

Imagine we immediately check the results:

```java
for (int i = 0; i < inputNumbers.size(); i++) {

    FactorialThread factorialThread = threads.get(i);

    if (factorialThread.isFinished()) {
        System.out.println(
            "Factorial of " + inputNumbers.get(i)
            + " is " + factorialThread.getResult()
        );
    } else {
        System.out.println(
            "The calculation for " + inputNumbers.get(i)
            + " is still in progress"
        );
    }
}
```

There is no guarantee that every worker thread is finished when the main thread reaches this loop.

Possible result:

```text
Factorial of 0 is ...
The calculation for 3435 is still in progress
The calculation for 35435 is still in progress
Factorial of 23 is ...
...
```

This happens because:

```text
Main Thread
    |
    | starts worker threads
    |
    | immediately starts checking results
    v

Worker Threads
    |
    | some are still calculating
    v
```

The main thread is faster than some of the worker threads.

---

# 10. Waiting for Every Worker with `join()`

We can wait for the worker threads before reading their results:

```java
for (Thread thread : threads) {
    thread.join();
}
```

Then:

```java
for (int i = 0; i < inputNumbers.size(); i++) {
    FactorialThread factorialThread = threads.get(i);

    System.out.println(
        "Factorial of " + inputNumbers.get(i)
        + " is " + factorialThread.getResult()
    );
}
```

Execution becomes:

```text
Main Thread
    |
    | start Thread 1
    | start Thread 2
    | start Thread 3
    | ...
    |
    | join Thread 1
    | join Thread 2
    | join Thread 3
    | ...
    |
    | all required threads have finished
    v
Read results
```

---

# 11. Important Detail: `join()` Does Not Make Threads Sequential

Consider:

```java
for (Thread thread : threads) {
    thread.start();
}

for (Thread thread : threads) {
    thread.join();
}
```

This does **not** mean:

```text
Thread 1 runs
then Thread 2 runs
then Thread 3 runs
```

All threads were already started first, so they can execute concurrently:

```text
Time ---------------------------------------------------->

Thread 1: [==============]
Thread 2: [=========================]
Thread 3: [=======]
Thread 4: [================]

Main:     start all -> [ waiting with join() ] -> continue
```

The `join()` loop only ensures that the main thread does not continue until the required worker threads have completed.

---

# 12. Timed `join()`

Sometimes we do not want to wait forever.

For example:

```java
for (Thread thread : threads) {
    thread.join(2000);
}
```

This means:

> Wait for each thread for at most 2 seconds.

After `join(2000)` returns, the thread might still be alive.

So we should check its state afterward.

Example:

```java
for (int i = 0; i < inputNumbers.size(); i++) {

    FactorialThread factorialThread = threads.get(i);

    if (factorialThread.isFinished()) {
        System.out.println(
            "Factorial of " + inputNumbers.get(i)
            + " is " + factorialThread.getResult()
        );
    } else {
        System.out.println(
            "The calculation for " + inputNumbers.get(i)
            + " is still in progress"
        );
    }
}
```

This is useful when one calculation may take an extremely long time.

---

# 13. Why Use a Timeout?

Imagine these inputs:

```java
List<Long> inputNumbers = Arrays.asList(
        100000000L,
        3435L,
        35435L,
        2324L,
        4656L,
        23L,
        5556L
);
```

The first calculation may take much longer than the others.

If we use:

```java
thread.join();
```

the main thread may wait for a very long time.

Instead:

```java
thread.join(2000);
```

allows us to say:

> Give the calculation some time, but do not block forever.

After the timeout, we can report that the calculation is still in progress.

---

# 14. `setDaemon(true)` in the Example

The example also contains code similar to:

```java
thread.setDaemon(true);
thread.start();
```

A **daemon thread** is a background thread that does not prevent the JVM from shutting down.

The JVM exits when there are no non-daemon user threads left.

Example:

```text
Main thread finishes
        |
        v
Only daemon threads remain
        |
        v
JVM may terminate
```

This can be useful for worker threads that we do not want to keep the entire program alive forever.

Important:

```java
thread.setDaemon(true);
```

must be called **before**:

```java
thread.start();
```

Otherwise Java throws `IllegalThreadStateException`.

---

# 15. `join()` vs Busy Waiting

### Busy waiting

```java
while (!thread.isFinished()) {
    // continuously checking
}
```

Problems:

- Wastes CPU cycles.
- Continuously executes instructions.
- Scales poorly with many threads.

### `join()`

```java
thread.join();
```

Benefits:

- The waiting thread does not need to continuously poll.
- Easier to read and understand.
- Designed specifically for thread completion coordination.

Conceptually:

```text
Busy Waiting

Thread A: [---------- work ----------]
Thread B: check-check-check-check-check
          ^ CPU wasted


Using join()

Thread A: [---------- work ----------]
Thread B: [          waiting         ] -> continue
```

---

# 16. Important: `join()` Blocks the Current Thread

Suppose this code runs inside the main thread:

```java
worker.join();
```

Then:

```text
worker thread  ---> continues running
main thread    ---> waits
```

It is easy to accidentally think that `join()` pauses the worker thread.

It does not.

A good way to remember it is:

```text
currentThread waits FOR targetThread
```

So:

```java
threadA.join();
```

means:

```text
Current Thread
     |
     | waits for
     v
Thread A
```

---

# 17. `join()` and `InterruptedException`

`join()` can throw:

```java
InterruptedException
```

Therefore you normally need either:

```java
public static void main(String[] args) throws InterruptedException {
    thread.join();
}
```

or:

```java
try {
    thread.join();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

The second approach is generally preferable inside application code because it preserves the thread's interrupted status.

---

# 18. Complete Simplified Example

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        List<Long> inputNumbers = Arrays.asList(
                100000000L,
                3435L,
                35435L,
                2324L,
                4656L,
                23L,
                5556L
        );

        List<FactorialThread> threads = new ArrayList<>();

        // Create worker threads
        for (long inputNumber : inputNumbers) {
            threads.add(new FactorialThread(inputNumber));
        }

        // Start all workers first
        for (Thread thread : threads) {
            thread.setDaemon(true);
            thread.start();
        }

        // Wait for each worker for at most 2 seconds
        for (Thread thread : threads) {
            thread.join(2000);
        }

        // Read the results
        for (int i = 0; i < inputNumbers.size(); i++) {

            FactorialThread factorialThread = threads.get(i);

            if (factorialThread.isFinished()) {
                System.out.println(
                        "Factorial of " + inputNumbers.get(i)
                        + " is " + factorialThread.getResult()
                );
            } else {
                System.out.println(
                        "The calculation for " + inputNumbers.get(i)
                        + " is still in progress"
                );
            }
        }
    }
}
```

---

# 19. Mental Model

The main idea of thread coordination is:

```text
Without coordination

Thread A ---------> result

Thread B -----> tries to use result too early
```

With `join()`:

```text
Thread A ---------> result

Thread B --- wait ---------> use result
```

Or, from the main thread's perspective:

```text
Main
 |
 | create workers
 |
 | start workers
 v

Worker 1 -----------\
Worker 2 -------------+--> calculations happen concurrently
Worker 3 -----------/

Main
 |
 | join()
 | wait
 |
 v

All required workers finished
 |
 v
Read/use results
```

---

---

# 20. Why `Thread.join(..)` Gives Us More Control

Using `Thread.join(..)` gives us better control over independent threads.

It helps us:

- coordinate threads that depend on each other,
- safely collect and aggregate results,
- avoid reading incomplete results,
- avoid waiting forever when a worker thread takes too long.

Example:

```java
thread.start();
thread.join(2000);
```

This means:

> Start the worker thread, then let the current thread wait for it for at most 2 seconds.

After the timeout, the worker may already be finished, or it may still be running.

Therefore we usually check its state:

```java
thread.join(2000);

if (thread.isAlive()) {
    System.out.println("Thread is still running");
} else {
    System.out.println("Thread finished");
}
```

This is especially useful when some computations may take an unexpectedly long time.

---

# 21. Safely Collecting and Aggregating Results

Imagine that several threads calculate independent results:

```text
Worker 1 ---> result 1
Worker 2 ---> result 2
Worker 3 ---> result 3
```

The main thread wants to combine them:

```text
result 1 + result 2 + result 3
```

The main thread must not aggregate the results before the workers have finished.

A common pattern is:

```java
for (Thread thread : threads) {
    thread.start();
}

for (Thread thread : threads) {
    thread.join();
}

// Safe point: all joined threads have completed
collectResults();
```

Conceptually:

```text
Worker 1: [------ work ------]
Worker 2: [---------- work ----------]
Worker 3: [--- work ---]

Main:     start all -> [ wait / join ] -> collect results
```

The key point is that `join()` creates a clear coordination point before the results are consumed.

---

# 22. Runaway or Long-Running Threads

A **runaway thread** is a thread that takes much longer than expected or does not finish when we expect it to.

For example:

```java
thread.join();
```

can theoretically block the current thread forever if the worker never terminates.

A safer approach in many applications is to use a timeout:

```java
thread.join(2000);
```

Now the current thread waits for at most 2 seconds.

After that:

```java
if (thread.isAlive()) {
    // worker did not finish in time
}
```

This lets the program decide what to do next instead of waiting forever.

---

# 23. Do Not Use `Thread.stop()`

Older Java APIs contain:

```java
thread.stop();
```

but `Thread.stop()` is deprecated and unsafe.

It can terminate a thread while shared data is in an inconsistent state.

Instead, prefer **cooperative cancellation**, usually with interruption:

```java
thread.interrupt();
```

The worker should cooperate by checking its interruption status:

```java
while (!Thread.currentThread().isInterrupted()) {
    // do work
}
```

or by reacting correctly to `InterruptedException`:

```java
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

So a safer timeout pattern is:

```java
thread.start();

thread.join(2000);

if (thread.isAlive()) {
    thread.interrupt();
}
```

Important:

> `interrupt()` is a request to stop or cancel. It does not forcibly kill the thread.

The worker code must be designed to respond to interruption.

---

# 24. General Design Rules for Thread Coordination

## Do not rely on execution order

This code:

```java
threadA.start();
threadB.start();
```

does not guarantee:

```text
Thread A finishes first
Thread B finishes second
```

It only starts them in that order.

The scheduler still decides when they actually execute.

## Always coordinate when one thread depends on another

If Thread B needs Thread A's result:

```text
Thread A ----> result ----> Thread B
```

there must be some coordination mechanism.

For simple thread completion, `join()` is one such mechanism.

## Design for the worst case

Do not assume:

```text
"This computation should only take 100 ms."
```

Real systems can be affected by:

- large input,
- CPU load,
- I/O delays,
- network delays,
- locks,
- bugs,
- external services,
- unexpected data.

Therefore, when indefinite waiting would be dangerous, use bounded waiting:

```java
thread.join(timeout);
```

## Threads may take an unreasonable amount of time

A thread may:

- finish normally,
- finish slowly,
- fail,
- become blocked,
- wait for another resource,
- never complete because of a bug.

Your code should have a strategy for these cases.

## Prefer timed waiting when indefinite blocking is unacceptable

Instead of:

```java
thread.join();
```

you may choose:

```java
thread.join(2000);
```

when the application cannot afford to wait forever.

However, a timeout is not automatically better in every situation.

Use an unlimited `join()` when it is logically correct to wait until completion, and use a timed `join()` when the application needs a deadline or fallback behavior.

---

# 25. Recommended Pattern

A simplified pattern for starting, waiting, timing out, and cancelling workers is:

```java
for (Thread thread : threads) {
    thread.start();
}

for (Thread thread : threads) {
    thread.join(2000);

    if (thread.isAlive()) {
        thread.interrupt();
    }
}
```

Then collect results only from successfully completed workers.

For example:

```java
for (FactorialThread thread : threads) {
    if (!thread.isAlive() && thread.isFinished()) {
        System.out.println(thread.getResult());
    } else {
        System.out.println("Calculation did not finish in time");
    }
}
```

---

# 26. Updated Mental Model

Think about `join()` like this:

```text
                    target thread
                         |
                         v
Thread A:      [---------- working ----------][finished]
                                                   |
                                                   |
Current:       ---- join(A) ---- waiting ---------+---- continue
```

For timed `join()`:

```text
Thread A:      [---------------- working ----------------]

Current:       -- join(A, 2000) -- wait -- timeout --> continue
                                      |
                                      v
                              Thread A may still run
```

So after timed `join()`, always remember:

> Returning from `join(timeout)` does not necessarily mean the target thread finished.

---

# 27. Summary

- Do not rely on the order in which threads execute or finish.
- Use thread coordination whenever one thread depends on another.
- `Thread.join()` lets the current thread wait for another thread.
- `Thread.join(timeout)` prevents the current thread from waiting indefinitely.
- `join()` helps safely collect and aggregate worker results.
- After a timed `join()`, check whether the target thread is still alive.
- Design concurrent code for slow, blocked, or unexpectedly long-running workers.
- Do not use deprecated `Thread.stop()`.
- Prefer cooperative cancellation with `interrupt()` when a worker needs to be cancelled.
- A timeout is useful when indefinite waiting is unacceptable.

The core idea is:

> **Start independent work concurrently, coordinate before consuming dependent results, and avoid waiting forever when the application requires a time limit.**

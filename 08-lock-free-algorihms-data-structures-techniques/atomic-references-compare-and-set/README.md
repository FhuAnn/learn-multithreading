# What we learn in this lecture
- AtomicReference<T>.
- CAS - CompareAndSet operation.
- Build lock free data structure.
- Compare performance with the blocking implementation.

# CAS - CompareAndSet
- Avaible in all atomic class.
- Compiles intro an atomic hardware operation.
- Many other atomic methods are internally implemented using CAS.

# Summary
- AtomicReference<T> - wraps a reference to an object, allows us to perform atomic operations on that reference, including the compareAndSet().
- CompareAnset(..) - atomic operations available in all atomic classes.
- Implemented a lock free data structure - stack, using the AtomicReference<> and compareAndSet.
- Lock Free Stack, outperformed the blocking stack implementation x 3 (200%);

# Practicing with benmark:
```text
             10 seconds
                 │
     ┌───────────┴───────────┐
     │                       │
push thread 1           pop thread 1
push thread 2           pop thread 2
     │                       │
     └────── shared Stack ───┘
                 │
                 ↓
          operations count
```
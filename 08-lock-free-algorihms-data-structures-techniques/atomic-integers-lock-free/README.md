# What we learn in this lecture
- AtomicInteger.
- Lock Free E-commerce Inventory Counter.

# AtomicINteger
int initialValue = 0;
AtomicInteger actomicINtegere = new AtomicInteger(initialValue);

//atomiccally increment the integer by one
atomicInteger.incrementAndGet(); // return the new value
atomicInteger.getAndIncrement(); // return the previous value

# AtomicInteger - Pros & Cons
- Pros:
  - Simplicity
  - No need to for locks or synchronization.
  - No race conditions or data races.
- Cons:
  - Only the operation itself is atomic.
  - There's still race condition between 2 separate atomic operations.

# Summary and Key takeaways
- AtomicInteger is a great tool for concurrent counting, without the complexity of using a lock.
- AtomicInteger should be used only when atomic operations are needed.
- It's on par and sometimes more performant than a regular integer with a lock as protection.
- If used only by a single thread, a regular integer is preferred. ( The reason for that is to achieve this atomicity, AtomicInteger uses a compare-and-swap (CAS) operation to avoid a data race, which is more expensive than a simple read/write operation on a regular integer. )
- If long use the AtomicLong, which provides the same capabilities as AtomicInteger.
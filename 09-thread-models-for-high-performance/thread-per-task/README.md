What we learn in this lecture?
- Introduction to Thread-Per-Task Threading Model
- Practical Example of an IO-bound Application.
- Performance Analysis And Conclusion.
![imgs/img.png](imgs/img.png)
![imgs/img_1.png](imgs/img_1.png)
![img.png](imgs/img31.png)



# Benefits
- Improvement for:
  - Throughput.
  - Hardware Utilization.
- Processed tasks concurrently and completed them faster than in the thread-per-core model.
![img.png](imgs/thread-issue.png)
![img.png](imgs/thread-issue-continued.png)
![img.png](imgs/Thread-code.png)
![img.png](imgs/hundred-io-blocking-in-a-task.png)
- Threading: a situation where most of the CPU is spent on the OS managing the system.
![img.png](imgs/summary.png)
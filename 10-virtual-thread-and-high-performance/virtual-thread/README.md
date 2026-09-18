![img_1.png](img_1.png)
![img.png](img.png)
![img_2.png](img_2.png)
![img_3.png](img_3.png)
![img_4.png](img_4.png)
![img_5.png](img_5.png)

# Watch practice 
![img_6.png](img_6.png)
# Summary
- Performance benefits of virtual threads for long blocking call.
- Virtual threads are a perfect choice for IO bound applications.
- using virtual threads:
  - We get similar performance as thread-per-core + Non-blocking IO.
  - We get the easse of programming, testing and debugging pff thread-perr-task+blockAPI
- Virtual threads' mounting + unmounting
  - Has a bit of overhead
  - Not s much as a context switch.
package com.yz.springbootdemo.service;


import org.springframework.stereotype.Service;

@Service
public class UserService {


    /*
     * @Description 死循环方法（cpu 99%或者发现）
     * @Param
     * @return
     **/
    public String findAll(boolean t) {
        int a=0;
        while (t) {//cpu会一直调度，消耗的CPU
        }
        return "循环完毕";
    }

    /**
     * 死锁方法
     * @return
     */
    public String findAll2() {
        Object o1 = new Object();
        Object o2 = new Object();

        new Thread(() -> { //线程1
            synchronized (o1) { //1锁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (o2) {
                    System.out.println("线程1执行");
                }
            }
        }).start();


        new Thread(() -> {//线程2
            synchronized (o2) { //2锁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (o1) {
                    System.out.println("线程2执行");
                }
            }
        }).start();

        // Lock lock
        return "死锁方法调用完毕";
    }
}

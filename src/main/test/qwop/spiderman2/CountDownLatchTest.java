package qwop.spiderman2;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch这个类能够使一个线程等待其他线程完成各自的工作后再执行。
 * 例如，应用程序的主线程希望在负责启动框架服务的线程已经启动所有的框架服务之后再执行。
 CountDownLatch是通过一个计数器来实现的，计数器的初始值为线程的数量。
    每当一个线程完成了自己的任务后，计数器的值就会减1。当计数器值到达0时，
    它表示所有的线程已经完成了任务，然后在闭锁上等待的线程就可以恢复执行任务。
 * <Short overview of features> <Features detail>
 * 
 * @author qwop
 * @date May 13, 2017
 * @version [The version number, May 13, 2017]
 * @see [Related classes/methods]
 * @since [Products/Module version]
 */
public class CountDownLatchTest {
    public static void main(String[] args) throws Exception {
        new Driver().main();
    }
}

class Driver { // ...
    private static final int N = 10;

    void main() throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);

        for (int i = 0; i < N; ++i) // create and start threads
            new Thread(new Worker(startSignal, doneSignal)).start();

        doSomethingElse(); // don't let run yet
        startSignal.countDown(); // let all threads proceed
        doSomethingElse();
        doneSignal.await(); // wait for all to finish
    }

    private void doSomethingElse() {
       System.out.println(" do something else ");
    }
}

class Worker implements Runnable {
    private final CountDownLatch startSignal;

    private final CountDownLatch doneSignal;

    Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }

    public void run() {
        try {
            startSignal.await();
            doWork();
            doneSignal.countDown();
        }
        catch (InterruptedException ex) {
        } // return;
    }

    void doWork() throws InterruptedException {
       for ( int i = 0; i < 10 ; i++ ) {
            System.out.println( Thread.currentThread().getName() +  " working " + i );
            Thread.sleep( 1000 );
        }
    }
}

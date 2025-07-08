import java.util.concurrent.*;

public class test {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> {
            System.out.println("Task dang chay: " + Thread.currentThread().getName());
        });

                executor.submit(() -> {
            System.out.println("Task dang chay: " + Thread.currentThread().getName());
        });

                executor.submit(() -> {
            System.out.println("Task dang chay: " + Thread.currentThread().getName());
        });

                executor.submit(() -> {
            System.out.println("Task dang chay: " + Thread.currentThread().getName());
        });

        executor.shutdown(); // Kết thúc executor sau khi hoàn tất
    }
}

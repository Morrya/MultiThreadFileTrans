package v1;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransmission {
    public static void main(String[] args) throws Exception {
        String uploadUrl = "D:\\study file\\Third\\operate system\\proj_school\\upload\\";
        String server = "127.0.0.1";
        int port = 8000;
        String savePath = "D:\\study file\\Third\\operate system\\proj_school\\download\\" + server + "\\";// 默认文件保存路径，可在main函数中设置
        //server
        new Server(port, savePath).start();
        //client
        System.out.println("1: 测试单线程耗时，2：测试多线程耗时");
        Scanner scan = new Scanner(System.in);
        try {
            int choice = Integer.parseInt(scan.nextLine());
            if (choice == 1) {
                // 单线程测试
                Client clients0 = new Client(uploadUrl, server, port);
                clients0.start();
            } else {
                // 多线程测试
                int MAX_T = 4;// Maximum number of threads in thread pool
                Client clients[] = new Client[MAX_T];
                // creates a thread pool with MAX_T no. of threads as the fixed pool size(Step 2)
                ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
                for (int i = 0; i < MAX_T; i++) {
                    clients[i] = new Client(uploadUrl, server, port);
                    pool.execute(clients[i]);
                }
                pool.shutdown();
            }
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException");
        }
        scan.close();

    }
}

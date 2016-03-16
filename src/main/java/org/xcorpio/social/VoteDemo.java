package org.xcorpio.social;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;

public class VoteDemo {

    public static void main(String[] args) {
        String url = "http://www.sjz95580.com/Vote/Vote.ashx?requestMethod=DoVote&openID=";
        String configId = "c3beaa9e-b059-42ad-8971-22be86df73df";
        int sendCount = 10;

        int threadCount = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < sendCount; ++i) {
            threadPool.execute(new VoteTask(url, configId));
        }

        try {
            threadPool.shutdown();
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("vote :" + VoteTask.counter.get());
    }

}

class VoteTask implements Runnable {
    public static AtomicInteger counter = new AtomicInteger(0);
    private String url;
    private String configId;

    public VoteTask(String url, String configId) {
        this.url = url;
        this.configId = configId;
    }

    public void run() {
        String openId = RandomStringUtils.random(32, true, true);
        StringBuilder sb = new StringBuilder();
        sb.append(url).append(openId).append("&type=1&configID=").append(configId);
        String res = post(sb.toString(), "");
        System.out.println(res);
        if (res.contains("true")) {
            counter.incrementAndGet();
        }
        
    }

    public String post(String strURL, String params) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.append(params);
            out.flush();
            out.close();

            int length = (int) connection.getContentLength();
            InputStream is = connection.getInputStream();
            if (length != -1) {
                byte[] data = new byte[length];
                byte[] buf = new byte[512];
                int readLen = 0;
                int destPos = 0;
                while ((readLen = is.read(buf)) > 0) {
                    System.arraycopy(buf, 0, data, destPos, readLen);
                    destPos += readLen;
                }
                is.close();
                connection.disconnect();
                return new String(data, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
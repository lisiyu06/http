package http;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: lisiyu
 * Created: 2020/2/18
 */
public class Request {

    // 请求方法：get 或 post
    private String method;
    // 请求地址，对应服务端提供的服务路径
    private String url;
    // http 版本
    private String version;
    // 请求头
    private Map<String, String> headers = new HashMap<>();
    // 请求参数
    private Map<String, String> parameters = new HashMap<>();

    // 类似单例的写法，提供私有构造方法
    private Request() {

    }


    /**
     * 通过客户端发送的 http 请求数据，转换为 request 请求类
     * 包装请求方法，url，http 版本号，请求头，请求参数
     * @param inputStream
     * @return
     */
    public static Request buildRequest(InputStream inputStream) {
        Request request = new Request();

        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(inputStream,  "UTF-8"));
            System.out.println("===============开始解析HTTP请求===============");
            String requestLine = input.readLine();
            // 处理请求行
            request.parseResquestLine(requestLine);
            // 处理请求头
            String header;
            while ((header = input.readLine()) != null && header.length() != 0) {
                String[] parts = header.split(":");
                // 去掉字符串两端的多余的空格, 且无论两端的空格有多少个都会去掉
                // 中间的那些空格不会被去掉
                request.headers.put(parts[0].trim(), parts[1].trim());
                System.out.printf("请求头: %s=%s\n", parts[0].trim(), parts[1].trim());
            }
            // 如果是 post 方法提交，并且有 Content-Length，
            // 表示请求数据中包含请求体,需要处理请求体
            if ("POST".equalsIgnoreCase(request.method)
                    && request.headers.containsKey("Content-Length")) {
                int len = Integer.parseInt(request.headers.get("Content-Length"));
                char[] chars = new char[len];
                input.read(chars, 0, len);
                request.parseParameters(new String(chars));
            }
            System.out.print("请求参数：");
            for (Map.Entry<String, String> entry : request.parameters.entrySet()) {
                System.out.printf("%s=%s, ", entry.getKey(), entry.getValue());
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("处理请求数据错误", e);
        }

        return request;
    }

    // 解析请求行

    /**
     * 解析请求行为 method, url, version
     * @param requestLine
     */
    public void parseResquestLine(String requestLine) {
        if (requestLine == null) {
            return;
        }
        String[] parts = requestLine.split(" ");
        method = parts[0];
        url = parts[1];
        // 如果urL包含 “?”，表示有请求参数，需要处理
        int index = url.indexOf("?");
        if (index != -1) {
//        if (parts[1].contains("?")) {
            // 包含请求参数时，? 前的是urL，之后的是请求参数
            parseParameters(url.substring(index + 1));
            url = url.substring(0, index);

        }
        version = parts[2];
        System.out.printf("请求方法: %s, url: %s, 版本号: %s\n", method, url, version);
    }

    /**
     * 请求解析参数
     * 格式为 k1=v1&k2=v2&……
     * @param parameters
     */
    private void parseParameters(String parameters) {
        String[] parts = parameters.split("&");
        if (parts != null && parts.length != 0) {
            // part 对应 k1=v1
            for (String part : parts) {
                String[] params = part.split("=");
                this.parameters.put(params[0], params[1]);
            }
        }
    }


    /**
     * 根据请求头中的 key 获取 value
     * @param key
     * @return
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * 根据请求参数中的 key 获取 value
     * @param key
     * @return
     */
    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }
}

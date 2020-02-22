package http;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: lisiyu
 * Created: 2020/2/18
 */


/**
 * http 响应数据
 * 1. 构建response对象，将输出流设置到该对象属性
 * 2. 把这个对象相关数据设置进去，包括响应行，响应头，响应体
 * 3. 调用刷新，输出流打印数据返回给客户端
 */

public class Response {

    // 按行打印
    private PrintWriter writer;
    // http 版本号
    private String version = "HTTP/1.1";
    // 状态码
    private int status;
    // 状态码描述
    private String message;
    // 响应头
    private Map<String, String> headers = new HashMap<>();
    // 响应体
    private StringBuilder body = new StringBuilder();

    private Response() {

    }

    public static Response buildResponse(OutputStream outputStream) {
        Response response = new Response();
        response.writer = new PrintWriter(outputStream, true);
        return response;

    }


    /**
     * 添加响应头
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * 按行把请求体数据打印设置到 body 中
     * 注意这里不是输出流真是的打印数据到客户端
     * @param line
     */
    public void println(String line) {
        body.append(line + "\n");
    }

    /**
     * 输出流打印并刷新响应数据，返回给客户端
     */
    public void flush() {
        // 打印响应行
        writer.println(version + " " + status + " " + message);

        // 打印响应头
        // 设置响应格式 Content-Type (浏览器获取响应数据以后，按照什么类型来渲染或处理数据)
        writer.println("Content-Type: text/html; charset=UTF-8");
        if (body.length() != 0) {
            // Content-Length 字段是需要根据请求体字符串转换为字节数组，根据字节数组长度来设置
            // String.getBytes() 可以将某一个字符串转换为字节数组
            writer.println("Content-Length: " + body.toString().getBytes().length);
        }
        // 打印业务代码设置的响应头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            writer.println(entry.getKey() + ": " + entry.getValue());
        }

        // 打印空行
        writer.println();

        // 打印响应体
        if (body.length() != 0) {
            writer.println(body);
        }

        // 刷新输出流：
        // 1. 初始化 PrintWriter 时，第二个参数为 true，设置自动刷新
        // 2. PrintWriter.flush()
//        writer.flush();



    }


    // 构建 200 正常响应的响应行
    public void build200() {
        status = 200;
        message = "OK";
    }

    // 构建 404 找不到资源
    public void build404() {
        status = 404;
        message = "Not Found";
    }

    // 构建 307 重定向
    public void build303() {
        status = 307; // 301,302,307 都可以
        message = "Send Redirect";
    }

    // 构建 405 不支持的方法
    public void build405() {
        status = 405;
        message = "Method Not Allowed";
    }

    // 构建 500 服务器错误
    public void build500() {
        status = 500;
        message = "Internal Server Error";
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

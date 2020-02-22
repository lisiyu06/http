package http;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: lisiyu
 * Created: 2020/2/18
 */
public class HttpTask implements Runnable {

    //    private Socket socket;
    private Request request;

    private Response response;

    // 服务端保存 session 的数据结构（线程安全的 map），还可以保存在其他地方，如 redis 中间件
    private static ConcurrentMap<String, String> SESSION = new ConcurrentHashMap<>();

    public HttpTask(Socket socket) {
//        this.socket = socket;
        try {
            // 通过客户端发送报文的输入流（请求数据）创建 HTTP 请求对象
            request = Request.buildRequest(socket.getInputStream());
            response = Response.buildResponse(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("客户端连接的IO流出错", e);
        }
    }

    @Override
    public void run() {
        try {
            // 1. 根据解析出的 request 对象中属性，来进行逻辑处理
            // 2. 在不同逻辑中将要返回的数据设置到 response 对象中
            // 3. 刷新响应信息，返回给客户端

            // 调整业务逻辑
            // 1. url 为根路径. 访问首页
            if ("/".equals(request.getUrl())) {
                response.build200();
                response.println("<h2>Http服务器首页</h2>");
                return;
            }
            // 2. 根据 url 在 webapp 文件夹下去找是否存在这个资源，如果存在，就返回该资源内容

            // 改造：读取项目中的 login.html 文件内容，并返回给客户端
            // 怎么读取文件？ 1. 绝对路径 2. 相对路径

            // 相对路径读取步骤：
            // (1). html 文件所在的 webapp 需要设置为 resource 资源文件夹，
                  // ---> 将webapp中所有的文件复制到编译的输出文件
            // (2). 通过ClassLoader.getResourceAsStream()获取文件的输入流(相对路径)
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("." + request.getUrl());// http 请求行 url 以 / 开始
            // 如果存在，就设置该资源内容到 response 对象中
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String content;
                while ((content = br.readLine()) != null) {
                    response.println(content);
                }
                response.setStatus(200);
                response.setMessage("OK");
            } else if ("/login".equals(request.getUrl())) {
                // 1. 只接受 post 请求方法，否则返回 405
                if (!"post".equalsIgnoreCase(request.getMethod())) {
                    response.build405();
                    response.println("不支持的请求方法" + request.getMethod());
                } else {
                    // 2. 校验用户名密码，校验通过返回（我们这里省略校验）
                    response.build200();
                    response.println("请求的数据：" +
                            "username=" + request.getParameter("username") +
                            ", password=" + request.getParameter("password"));
                    // Sesion: 将用户信息保存到服务器，并且返回给客户端
                    String sessionID = UUID.randomUUID().toString();
                    SESSION.put(sessionID, request.getParameter("username") +
                            "," + request.getParameter("password"));
//                    // 第一种：自己定义 header 头(需要客户端代码如 html. js 代码中实现 header 解析和设置)
//                    response.addHeader("SESSIONID", sessionID);
                    // 第二种：设置到 Cookie 中，之后访问每个 url 都会在请求中包含 Cookie=sessionID
                    response.addHeader("Set-Cookie", sessionID);
                }
            // 敏感 url，没有登录的时候，不能访问
            } else if ("/sensitive".equals(request.getUrl())) {
                String sessionID = request.getHeader("SESSIONID");
                String userInfo = SESSION.get(sessionID);
                System.out.println("============获取到的用户信息：" + userInfo);
            } else { // 以上所有路径都找不到，说明我们服务器不提供该 url 的服务，返回404
                response.build404();
                response.println("找不到资源");
            }
        } catch (Exception e) { // 捕获所有异常，表示的是服务器异常，需要返回 500
            e.printStackTrace();
            response.build500();
            response.println("服务器出错");
        } finally { // 始终需要刷新响应数据
            // 返回数据给客户端
            response.flush();
        }
    }

}

package http;

import java.io.File;
import java.io.InputStream;

/**
 * Author: lisiyu
 * Created: 2020/2/19
 */
public class Test {
    public static void main(String[] args) {
        // 现在的写法是把编译的文件夹作为相对路径，这是不对的
//        File f = new File("../../webapp.login.html");
        // 应该以编译输出文件夹作为相对路径，不推荐 new File() 这种做法
//        File f = new File("../login.html");
//        System.out.println(f.exists());


        // class.getResourceAsStream() 是以具体的这个类编译出来的 class 文件路径作为相对路径
        // class 的做法不推荐使用
//        InputStream is = Test.class.getClassLoader().getResourceAsStream("../login.html");
//        System.out.println(is);
//        InputStream is2 = Test.class.getClassLoader().getResourceAsStream("login.html");
//        System.out.println(is2);


        /**
         * 正确做法
         */

        // 推荐使用 ClassLoader().getResourceAsStream() 作为资源文件输入流读取操作
        // 用类加载器加载文件资源
        // ClassLoader 以编译输出文件夹根目录作为相对的标准位置
        // class.getResourceAsStream() 是以具体的这个类编译出来的 class 文件路径作为相对路径
        InputStream is = Test.class.getClassLoader().getResourceAsStream("/login.html");
        System.out.println(is);
        InputStream is2 = Test.class.getClassLoader().getResourceAsStream("login2.html");
        System.out.println(is2);
    }


}

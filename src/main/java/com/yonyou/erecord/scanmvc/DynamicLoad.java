package com.yonyou.erecord.scanmvc;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Stack;

public class DynamicLoad {

  public static void loadJar(String jarpath) throws Exception {
    // 系统类库路径
    File libPath = new File(jarpath);
    // 获取所有的.jar和.zip文件
    File[] jarFiles = libPath.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".jar") || name.endsWith(".zip");
      }
    });

    if (jarFiles != null) {
      // 从URLClassLoader类中获取类所在文件夹的方法
      // 对于jar文件，可以理解为一个存放class文件的文件夹
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      boolean accessible = method.isAccessible(); // 获取方法的访问权限
      try {
        if (accessible == false) {
          method.setAccessible(true); // 设置方法的访问权限
        }
        // 获取系统类加载器
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for (File file : jarFiles) {
          URL url = file.toURI().toURL();
          try {
            method.invoke(classLoader, url);
            // LOG.debug("读取jar文件[name={}]", file.getName());
          } catch (Exception e) {
            // LOG.error("读取jar文件[name={}]失败", file.getName());
          }
        }
      } finally {
        method.setAccessible(accessible);
      }
    }
  }

  public static void loadClass(String classpath) throws Exception {
    // 设置class文件所在根路径
    // 例如/usr/java/classes下有一个test.App类，则/usr/java/classes即这个类的根路径，而.class文件的实际位置是/usr/java/classes/test/App.class
    File clazzPath = new File(classpath);
    // 记录加载.class文件的数量
    int clazzCount = 0;
    if (clazzPath.exists() && clazzPath.isDirectory()) {
      // 获取路径长度
      int clazzPathLen = clazzPath.getAbsolutePath().length() + 1;
      Stack<File> stack = new Stack<>();
      stack.push(clazzPath);
      // 遍历类路径
      while (stack.isEmpty() == false) {
        File path = stack.pop();
        File[] classFiles = path.listFiles(new FileFilter() {
          public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(".class");
          }
        });
        for (File subFile : classFiles) {
          if (subFile.isDirectory()) {
            stack.push(subFile);
          } else {
            if (clazzCount++ == 0) {
              Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
              boolean accessible = method.isAccessible();
              try {
                if (accessible == false) {
                  method.setAccessible(true);
                }
                // 设置类加载器
                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                // 将当前类路径加入到类加载器中
                method.invoke(classLoader, clazzPath.toURI().toURL());
              } finally {
                method.setAccessible(accessible);
              }
            }
            // 文件名称
            String className = subFile.getAbsolutePath();
            className = className.substring(clazzPathLen, className.length() - 6);
            className = className.replace(File.separatorChar, '.');
            // 加载Class类
            Class.forName(className);
            // LOG.debug("读取应用程序类文件[class={}]", className);
          }
        }
      }
    }
  }

}

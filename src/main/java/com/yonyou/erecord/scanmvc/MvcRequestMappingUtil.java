package com.yonyou.erecord.scanmvc;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import org.springframework.web.bind.annotation.RequestMapping;

public class MvcRequestMappingUtil {

  @SuppressWarnings("resource")
  public static void main(String[] ags) {

    System.out.println("请输入扫描的包名");
    Scanner scan = new Scanner(System.in);
    // 填写包名"com.yonyou.erecord"
    String packagesname = scan.nextLine();
    try {
      System.out.println("请输入war包中的lib路径");
      Scanner scan2 = new Scanner(System.in);
      // 动态加载jar包"C:\\Users\\Administrator\\Desktop\\web\\WEB-INF\\lib"
      String jarpath = scan2.nextLine();
      DynamicLoad.loadJar(jarpath);

      System.out.println("请输入war包中的classes路径");
      Scanner scan3 = new Scanner(System.in);
      // 动态加载class "C:\\Users\\Administrator\\Desktop\\web\\WEB-INF\\classes"
      String classpath = scan3.nextLine();
      DynamicLoad.loadClass(classpath);

      // controller类包含的requestmapping
      Map<String, List<String>> data = new HashMap<String, List<String>>();
      // requestmapping对应的method
      Map<String, String> mappmethod = new HashMap<String, String>();
      MvcRequestMappingUtil.getMvcRequestMappingList(data, mappmethod, packagesname);

      System.out.println("请输入导出excel的路径");
      Scanner scan4 = new Scanner(System.in);
      // 导出excel"E://test.xls"
      File f = new File(scan4.nextLine());
      FileOutputStream out = new FileOutputStream(f);
      ExportPath export = new ExportPath(data, mappmethod);
      String[] fieldname = {"controller", "requestmapping", "method"};
      export.getExcel(fieldname, "sheetname", out);
      System.out.println("操作成功");
    } catch (Exception e) {
      System.out.println("导出失败" + e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void getMvcRequestMappingList(Map<String, List<String>> data,
      Map<String, String> mappmethod, String packagename) throws Exception {
    List<String> listController = new ArrayList<String>();
    // 获取虚拟机中已经加载的类
    Field f = ClassLoader.class.getDeclaredField("classes");
    f.setAccessible(true);
    Vector classes = (Vector) f.get(ClassLoader.getSystemClassLoader());
    Iterator<Class> it = classes.iterator();
    while (it.hasNext()) {
      Class cl = it.next();
      if (cl.getName().startsWith(packagename)) {
        listController.add(cl.getName());
      }
    }
    // 扫描指定包下的controller类
    for (int i = 0; i < listController.size(); i++) {
      List<String> listMapping = new ArrayList<String>();
      listMapping = getMvcRequestMappingList(listController.get(i), mappmethod);
      data.put(listController.get(i), listMapping);
    }
  }

  public static List<String> getMvcRequestMappingList(String className,
      Map<String, String> mappmethod) {
    List<String> retVal = new ArrayList<String>();
    getMvcRequestMappingListByClass(retVal, className, mappmethod);
    return retVal;
  }

  public static void getMvcRequestMappingListByClass(List<String> retVal, String className,
      Map<String, String> mappmethod) {
    try {
      Class<?> cls = Class.forName(className);
      Annotation[] classAnnotations = cls.getAnnotations();// 得到类级别的所有注解
      int classRequestMappingCount = 0;// 类级别的RequestMapping统计
      for (Annotation classAnnotation : classAnnotations) {
        //筛选出注解是requestmapping 的类
        if (classAnnotation instanceof RequestMapping) {
          classRequestMappingCount = classRequestMappingCount + 1;
          //获取类的映射路径使用反射中invoke方法
          Method annotationMethod = classAnnotation.getClass().getDeclaredMethod("value", null);
          String[] annotationValues = (String[]) annotationMethod.invoke(classAnnotation, null);
          for (String classRequestMappingPath : annotationValues) {
            getMvcRequestMappingListByMethod(retVal, cls, classRequestMappingPath, mappmethod);
          }
        }
      }
      if (classRequestMappingCount == 0) {// 如果没有类级别的RequestMapping
        getMvcRequestMappingListByMethod(retVal, cls, "", mappmethod);
      }
    } catch (Exception e) {
      System.out.println("导出失败" + e);
    }
  }

  // 获取controller类中requestmapping映射地址以及映射地址对应的方法名
  public static void getMvcRequestMappingListByMethod(List<String> retVal, Class<?> cls,
      String classRequestMappingPath, Map<String, String> mappmethod) throws Exception {
    // 获取某个controller类的所有方法
    Method[] methods = cls.getDeclaredMethods();
    for (Method method : methods) {
      // 筛选出注解是RequestMapping的方法
      if (method.isAnnotationPresent(RequestMapping.class)) {
        Annotation methodAnnotation = method.getAnnotation(RequestMapping.class);
        Method methodAnnotationMethod =
            methodAnnotation.getClass().getDeclaredMethod("value", null);
        String[] values = (String[]) methodAnnotationMethod.invoke(methodAnnotation, null);
        for (String methodRequestMappingPath : values) {
          //将类的映射地址和方法的映射地址拼接
          methodRequestMappingPath = classRequestMappingPath.concat(methodRequestMappingPath)
              .replace("*", "").replace("//", "/");
          //添加该类对应的所有方法的路径到list中retVal
          retVal.add(methodRequestMappingPath.replaceFirst("/", ""));
          mappmethod.put(methodRequestMappingPath, method.getName() + "()");
        }
      }
    }
  }

}

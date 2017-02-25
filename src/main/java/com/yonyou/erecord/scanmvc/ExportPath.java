package com.yonyou.erecord.scanmvc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 用于excel表单导出
 * 
 */
public class ExportPath {
  private Map<String, List<String>> data;// controller数据
  private Map<String, String> mappmethod;// method
  private HSSFWorkbook workbook;

  public ExportPath(Map<String, List<String>> data, Map<String, String> mappmethod) {
    this.data = data;
    this.mappmethod = mappmethod;
  }

  /**
   * 对list数据源将其里面的数据导入到excel表单
   * 
   * @param fieldName[] 导出到excel文件里的表头名
   * @param sheetName工作表的名称
   * @param output 输出流
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void getExcel(String[] fieldName, String sheetName, OutputStream output) {
    workbook = new HSSFWorkbook();
    HSSFSheet sheet = workbook.createSheet();// 产生工作表对象
    formatSheet(sheet, fieldName);// 设置工作表单元格格式
    workbook.setSheetName(0, sheetName);

    HSSFRow row = sheet.createRow(0);// 产生一行
    HSSFCell cell;// 产生单元格

    // 设置单元格样式
    HSSFCellStyle titleStyle = workbook.createCellStyle();
    HSSFFont font = (HSSFFont) workbook.createFont();
    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    titleStyle.setFont(font);

    // 写入各个字段的名称
    for (int i = 0; i < fieldName.length; i++) {
      // 创建第一行各个字段名称的单元格
      cell = row.createCell(i);
      // 给单元格内容赋值
      cell.setCellValue(fieldName[i]);
      cell.setCellStyle(titleStyle);
    }
    List<String> contro = new ArrayList<String>();
    Set<String> set = data.keySet();
    Iterator<String> it = set.iterator();
    while (it.hasNext()) {
      contro.add(it.next());
    }
    int num = 0;// 计数器 用于写入下一条数据，记录上一次行数
    for (int j = 0; j < data.size(); j++) {
      List<String> list = new ArrayList();
      list = data.get(contro.get(j));
      for (int l = 0; l < list.size(); l++) {
        row = sheet.createRow(num + 1);
        row.createCell(0).setCellValue(contro.get(j));
        row.createCell(1).setCellValue(list.get(l).toString());
        row.createCell(2).setCellValue(mappmethod.get("/" + list.get(l)));
        num++;
      }
    }
    try {
      output.flush();
      workbook.write(output);
    } catch (Exception e) {
      System.out.println("导出失败"+e);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          System.out.println("导出失败"+e);
        }
      }
    }

  }

  /**
   * 设置单元格大小
   * 
   * @param sheet
   * @param fieldName
   * @return
   */
  public HSSFSheet formatSheet(HSSFSheet sheet, String[] fieldName) {
    for (int i = 0; i < fieldName.length; i++) {
      sheet.setColumnWidth(i, 70 * 256);
    }
    return sheet;
  }

}

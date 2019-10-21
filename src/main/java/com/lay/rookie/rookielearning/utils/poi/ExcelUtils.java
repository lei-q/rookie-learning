package com.lay.rookie.rookielearning.utils.poi;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel操作工具类
 * @author ChaiXY
 */
@Component
public class ExcelUtils {

    public static final String OFFICE_EXCEL_XLS = "xls";
    public static final String OFFICE_EXCEL_XLSX = "xlsx";
    
    public static String excelPath = "C:\\Users\\leizhuang\\Desktop\\套牌.xlsx";
    public static Integer sheetNo = 0;
    public static String colnumNo = "0";// 可以多列 0 2 3 多列空格分隔
    public static Integer rownumNo = 0;
    
//    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException {
//    	List<String> readExcel = readExcel();
//    	readExcel.forEach(v -> System.out.println(v));
//    }
    
    /**
     * 	读取指定Sheet页的内容
     */
    public List<String> readExcel()
            throws EncryptedDocumentException, InvalidFormatException, IOException {
        List<String> list = new ArrayList<>();
        Workbook workbook = getWorkbook(excelPath);
        if (workbook != null) {
            if (sheetNo == null) {
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet == null) {
                        continue;
                    }
                    List<String> readExcelSheet = readExcelSheet(sheet);
                    if(readExcelSheet != null && readExcelSheet.size() > 0) {
                    	list.addAll(readExcelSheet);
                    }
                }
            } else {
                Sheet sheet = workbook.getSheetAt(sheetNo);
                if (sheet != null) {
                	list = readExcelSheet(sheet);
                }
            }
        }
        return list;
    }
    
    /**
     * 	根据文件路径获取Workbook对象
     * @param filepath 文件全路径
     */
    public Workbook getWorkbook(String filepath)
    		throws EncryptedDocumentException, InvalidFormatException, IOException {
    	InputStream is = null;
    	Workbook wb = null;
    	if (StringUtils.isBlank(filepath)) {
    		throw new IllegalArgumentException("文件路径不能为空");
    	} else {
    		String suffiex = getSuffiex(filepath);
    		if (StringUtils.isBlank(suffiex)) {
    			throw new IllegalArgumentException("文件后缀不能为空");
    		}
    		if (OFFICE_EXCEL_XLS.equals(suffiex) || OFFICE_EXCEL_XLSX.equals(suffiex)) {
    			try {
    				is = new FileInputStream(filepath);
    				wb = WorkbookFactory.create(is);
    			} finally {
    				if (is != null) {
    					is.close();
    				}
    				if (wb != null) {
    					wb.close();
    				}
    			}
    		} else {
    			throw new IllegalArgumentException("该文件非Excel文件");
    		}
    	}
    	return wb;
    }
    
    /**
     * 	获取后缀
     * @param filepath filepath 文件全路径
     */
    private String getSuffiex(String filepath) {
        if (StringUtils.isBlank(filepath)) {
            return "";
        }
        int index = filepath.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return filepath.substring(index + 1, filepath.length());
    }

    private List<String> readExcelSheet(Sheet sheet) {
    	List<String> sb = new ArrayList<>();
        
        if(sheet != null){
            int rowNos = sheet.getLastRowNum();// 得到excel的总记录条数
            for (int i = rownumNo; i <= rowNos; i++) {// 遍历行
                Row row = sheet.getRow(i);
                if(row != null){
                	if(StringUtils.isNoneBlank(colnumNo)) {
                		List<String> rowData = new ArrayList<String>();
                		String[] split = colnumNo.trim().split(" ");
                		for (String string : split) {
                			Cell cell = row.getCell(Integer.valueOf(string));
                			if(cell != null){
                				cell.setCellType(CellType.STRING);
                				if(!StringUtils.isBlank(cell.getStringCellValue()))
            					    rowData.add(cell.getStringCellValue());
                			}
						}
                		sb.add(StringUtils.join(rowData, " "));
                	}else {
                		int columNos = row.getLastCellNum();// 表头总共的列数
                		for (int j = 0; j < columNos; j++) {
                			Cell cell = row.getCell(j);
                			if(cell != null){
                				cell.setCellType(CellType.STRING);
                				sb.add(cell.getStringCellValue().trim());
                			}
                		}
                	}
                }
            }
        }
        
        return sb;
    }
}
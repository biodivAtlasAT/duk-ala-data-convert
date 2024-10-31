package duk.at.models

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

class Biom (val ifile: String, val ofile: String){

    fun convert() {
        try {
            val file = FileInputStream(File(ifile))
            val workbook: Workbook = XSSFWorkbook(file)
            val sheet: Sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                if (row.rowNum == 0)
                    continue
                for (cell in row) {
                    val value = when (cell.cellType) {
                        CellType.STRING -> cell.stringCellValue
                        CellType.NUMERIC -> cell.numericCellValue
                        CellType.BOOLEAN -> cell.booleanCellValue
                        else -> ""
                    }
                    //if(cell.columnIndex == 1)
                    print("$value\t")
                }
                println()
                println()
            }

            file.close()
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
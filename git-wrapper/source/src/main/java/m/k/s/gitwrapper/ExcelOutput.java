/**
 * Licensed to Open-Ones Group under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Open-Ones Group licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package m.k.s.gitwrapper;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import rocky.common.CHARA;
import rocky.common.CommonUtil;
import rocky.poi.PoiUtil;

/**
 * Output data to Excel file
 * @author lengocthach
 *
 */
public class ExcelOutput implements IOutput {
    
    /** Logging. */
    private static final Logger LOG = Logger.getLogger(ExcelOutput.class);

    /** Template in class path . */
    private static final String TEMPLATE_RESOURCE = "/OutputTemplate.xls";
    
    /** Workbook of output Excel. */
    private Workbook wb;

    /** Default output sheet. */
    private static final int DEFAULT_SHEET_NO = 0;
    
    /** Default sheet of the workbook to be written . */
    private Sheet sheet;

    /** Tracking current row no. */
    private int curRowNo = 0;

    /**
     * 
     */
    public ExcelOutput() {
        try {
            wb = PoiUtil.loadWorkbookByResource(TEMPLATE_RESOURCE);
            sheet = wb.getSheetAt(DEFAULT_SHEET_NO);
        } catch (IOException ex) {
            LOG.error("Could not create the ExcelOutput with template '" + TEMPLATE_RESOURCE + "'", ex);
        }
    }
    /**
     * [Explain the description for this method here].
     * @param commitName
     * @param commitDate
     * @param authorName
     * @param authorEmail
     * @param filePath
     * @param message
     * @see m.k.s.gitwrapper.IOutput#write(java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void write(String commitName, Date commitDate, String authorName, String authorEmail, String filePath,
            String message) {
        curRowNo++;

       // Row row = sheet.getRow(curRowNo);
        String strCommitDate = CommonUtil.formatDate(commitDate, "yyyy/MM/dd HH:mm");
        
        PoiUtil.setContent(sheet, curRowNo, 0, curRowNo);
        PoiUtil.setContent(sheet, curRowNo, 1, commitName);
        
        PoiUtil.setContent(sheet, curRowNo, 2, strCommitDate);
        PoiUtil.setContent(sheet, curRowNo, 3, authorName);
        PoiUtil.setContent(sheet, curRowNo, 4, authorEmail);
        PoiUtil.setContent(sheet, curRowNo, 5, filePath);
        
        message = (message == null) ? CHARA.BLANK : message.trim();
        
        PoiUtil.setContent(sheet, curRowNo, 6, message);
    }
    
    /**
     * Write all content of the workbook to file.
     * @param outFilePath
     */
    public void writeFile(String outFilePath) {
        try {
            PoiUtil.writeExcelFile(wb, outFilePath);
        } catch (IOException ex) {
            LOG.error("Could not write the Excel '" + outFilePath + "'", ex);
        }
    }

}

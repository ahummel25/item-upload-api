package com.wwt.itemuploadapi

import grails.transaction.Transactional
import com.wwt.itemuploadapi.oracle.types.ItemRecType
import oracle.jdbc.OracleCallableStatement
import oracle.jdbc.OracleConnection
import oracle.jdbc.OracleTypes
import oracle.sql.ARRAY
import oracle.sql.ArrayDescriptor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.commons.lang.WordUtils

import javax.sql.DataSource
import java.lang.reflect.Method
import java.lang.reflect.Field
import java.util.Map.Entry
import java.sql.Connection
import java.sql.SQLException

@Transactional
class CreationService {

    DataSource dataSource

    final ORACLE_PKG = 'wwt_item_gen_creation_pkg'

    def callApi(List<ItemRecType> itemRecType, userName) {

        Connection connection = dataSource.getConnection()
        OracleCallableStatement ocs = null
        def returnObj = ['code': 'S', 'message': null]
        def returnStatus
        def returnMessage

        try {
            ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor("APPS.WWT_ITEM_CREATION_TBLTYPE", connection.unwrap(OracleConnection.class))
            ARRAY arrayElementsToPass = new ARRAY(descriptor, connection.unwrap(OracleConnection.class), (Object[]) itemRecType.toArray())
            ocs = (OracleCallableStatement) connection.prepareCall("{call apps.${ORACLE_PKG}.create_items(?,?,?,?)}")
            ocs.setArray(1, arrayElementsToPass)
            ocs.setString(2, userName)
            ocs.registerOutParameter(3, OracleTypes.VARCHAR)
            ocs.registerOutParameter(4, OracleTypes.VARCHAR)

            ocs.execute()

            returnStatus = ocs.getObject(3)
            returnMessage = ocs.getObject(4)

            returnObj = ['code': returnStatus, 'message': returnMessage]
        }
        catch (SQLException se) {
            returnObj = ['code': 'E', 'message': se]
        }
        catch (Exception e) {
            returnObj = ['code': 'E', 'message': e]
        }
        finally {
            if (connection != null) {
                connection.close()
            }
            if (ocs != null) {
                ocs.close()
            }
        }
        return returnObj
    }

    static Object getCellType(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue()
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return (int) cell.getNumericCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue()
            default:
                return ""
        }
    }

    static List<Method> getMethodsFromHeaderColumns(Sheet sheet) {
        final DataLoadRecType = "com.wwt.itemuploadapi.oracle.types.ItemRecType"
        List<Method> methods = new ArrayList<Method>()
        Class<ItemRecType> recType = (Class<ItemRecType>) Class.forName(DataLoadRecType)
        Class[] args1 = new Class[1]
        Map<String, Integer> colMapByName = new HashMap<String, Integer>()
        int lastColNum = sheet.getRow(0).getLastCellNum();
        def fileHeaders = [:]

        if (sheet.getRow(0).cellIterator().hasNext()) {
            for (int j = 0; j < lastColNum; j++) {
                def initCappedWords = WordUtils.capitalizeFully(sheet.getRow(0).getCell(j).toString())
                def formattedEntry = initCappedWords.replaceAll("\\s+","") //remove white space between words
                fileHeaders.put(sheet.getRow(0).getCell(j), formattedEntry)
            }

            for (Entry<String, String> entry : fileHeaders.entrySet()) {
                def fieldName = entry.getValue().substring(0,1).toLowerCase() + entry.getValue().substring(1)
                Field field = recType.getDeclaredField(fieldName)
                args1[0] = field.getType()
                Method method = recType.getDeclaredMethod("set" + entry.getValue(), args1)
                methods.add(method)
            }
        }
        return methods
    }

        static List<ItemRecType> buildRectypeFromFile(Sheet sheet) {
            List<ItemRecType> itemRecTypeArray = new ArrayList<ItemRecType>()
            int lastRowNum = sheet.getLastRowNum()
            List<Method> columnSetterMethods = getMethodsFromHeaderColumns(sheet)

            for (int rowIterator = 1; rowIterator <= lastRowNum; rowIterator++) {
                ItemRecType itemRecType = new ItemRecType()
                Row row = sheet.getRow(rowIterator)

                for (int cellIndex = 0; cellIndex < columnSetterMethods.size(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex)
                    if (cell != null) {
                        Method method = columnSetterMethods.get(cellIndex)
                        if (method != null) {
                            Object value = getCellType(cell)
                            Object formattedValue = ((value.toString().length() == 1 && method.getName().equals("setOrganizationCode")) ? '0' + value : value)
                            if (method.getName().equals("setOrganizationCode") || method.getName().equals("setSegment1")) {
                                formattedValue = formattedValue.toString()
                            }
                            method.invoke(itemRecType, formattedValue)
                        }
                    }
                }
                itemRecTypeArray.add(itemRecType)
            }
            return itemRecTypeArray
        }

        def create(InputStream excelFile, userName) {
            println "Heree"
            Workbook workbook = WorkbookFactory.create(excelFile)
            Sheet sheet = workbook.getSheetAt(0)
            excelFile.close()

            return callApi(buildRectypeFromFile(sheet), userName)
        }
    }
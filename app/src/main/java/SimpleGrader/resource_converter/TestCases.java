package SimpleGrader.resource_converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.security.GeneralSecurityException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import SimpleGrader.InconsistentArrayLengthException;
import SimpleGrader.Test;

public final class TestCases {

    private static final String TESTCASES_DIR = "/testCases.xlsx";
    private static final String TESTCASE_SHEET = "Main";
    private static final String IGNORE_CASE = "Example";
    private static final String BLANK_VALUE = "Empty";

    private static Object getNilValue(String className, String str) throws ClassNotFoundException {
        switch (className.toLowerCase()) {
            case "string":
                return "";
            case "integer":
            case "int":
                return Integer.valueOf(0);
            case "double":
                return Double.valueOf(0.0);
            case "float":
                return Float.valueOf(0.0f);
            case "boolean":
            case "bool":
                return Boolean.valueOf(false);
            case "character":
            case "char":
                return Character.valueOf('\0'); // Is this a good idea?
        }
        throw new ClassNotFoundException("No defined class of: " + str);
    }

    private static Object getValueOf(String className, String str) throws ClassNotFoundException {
        if (str.toLowerCase().equals(BLANK_VALUE.toLowerCase()))
            return getNilValue(className, str);
        switch (className.toLowerCase()) {
            case "string":
                return String.valueOf(str);
            case "integer":
            case "int":
                return Integer.valueOf(Double.valueOf(str).intValue());
            case "double":
                return Double.valueOf(str);
            case "float":
                return Float.valueOf(str);
            case "boolean":
            case "bool":
                return Boolean.valueOf(str);
            case "character":
            case "char":
                return Character.valueOf(str.charAt(0));
        }
        throw new ClassNotFoundException("No defined class of: " + str);

    }

    private static HashMap<String, ArrayList<Object[]>> generateTestCaseMap(Sheet sheet) {
        HashMap<String, ArrayList<Object[]>> finalMap = new HashMap<String, ArrayList<Object[]>>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {

            String testName;
            ArrayList<Object[]> QA = new ArrayList<Object[]>(2); // Q then A, in that order
            QA.add(null); // incase it is AQ & not QA
            QA.add(null);

            Row testNameRow = rowIterator.next(); // Move once to check for valid test name
            Cell testNameCell = testNameRow.getCell(0); // Get the first cell as it should be the name of the case entry
            int rowNum = testNameRow.getRowNum() + 1; // Excel starts at 1 doe

            try {
                if (testNameCell != null) {

                    testName = testNameCell.toString(); // get testName cell as string

                    // if testName matches a file enum or is null throw exception
                    if (testName == null || TestFileEnum.contains(testName.toUpperCase())) {
                        continue;
                        // throw new MalformedCaseEntryException("Warning: Malformed test entry: Row " +
                        // rowNum);
                    }

                    testName = testName.toUpperCase();

                    if (testName.equals(IGNORE_CASE.toUpperCase())) // Ignore test case
                        continue;

                    for (int i = 0; i < 2; i++) { // We have a test name so now continue and get the Qs & As
                        if (!rowIterator.hasNext()) // file ended abruptly
                            throw new MalformedCaseEntryException("Warning: File ended abruptly");

                        Row row = rowIterator.next();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        rowNum = row.getRowNum() + 1;

                        String indexCell = cellIterator.next().toString().toUpperCase(); // all enums are upper
                        int index = TestFileEnum.valueOf(indexCell).getIndex(); // Is it a Q or A

                        String classType = cellIterator.next().toString(); // name of the class type

                        ArrayList<Object> entryList = new ArrayList<Object>(); // list to put actual values into

                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            String cellValue = cell.toString().replace("\\n", "\n");
                            entryList.add(getValueOf(classType, cellValue));
                        }

                        QA.set(index, entryList.toArray());
                    }

                    if (TestFileEnum.isChecked())
                        finalMap.put(testName, QA);
                }
            } catch (UncheckedValuesException e) {
                System.out.println("Warning: Missing Questions or Answers: Row " + (rowNum - 2));
            } catch (MalformedCaseEntryException e) {
                System.out.println(e.getMessage());
            } catch (NoSuchElementException | IllegalArgumentException e) {
                System.out.println("Warning: Malformed test entry: Row " + rowNum);
                System.out.println(e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("Warning: Unknown class type: Row " + rowNum);
            }
            TestFileEnum.reset();
        }
        return finalMap;
    }

    private static Workbook decryptBook(String location, String password) throws IOException, GeneralSecurityException {
        InputStream testCaseSheet = TestCases.class.getResourceAsStream(location);
        if (testCaseSheet == null)
            throw new IOException("Failed to read test case file!");

        POIFSFileSystem fileSystem = new POIFSFileSystem(testCaseSheet);
        EncryptionInfo info = new EncryptionInfo(fileSystem);
        Decryptor decryptor = Decryptor.getInstance(info);
        if (!decryptor.verifyPassword(password)) {
            throw new EncryptedDocumentException("Unable to process test cases: document is encrypted.");
        }
        InputStream dataStream = decryptor.getDataStream(fileSystem);
        return WorkbookFactory.create(dataStream);
    }

    public static HashMap<String, Test> getMap(String password) // HashMap<String, ArrayList<Object>>
            throws EncryptedDocumentException, IOException, InconsistentArrayLengthException {
        HashMap<String, Test> finalMap = new HashMap<String, Test>();
        try {
            Workbook wb = decryptBook(TESTCASES_DIR, password);
            Sheet sheet = wb.getSheet(TESTCASE_SHEET);
            HashMap<String, ArrayList<Object[]>> arrayMap = generateTestCaseMap(sheet);
            wb.close();

            for (HashMap.Entry<String, ArrayList<Object[]>> entry : arrayMap.entrySet()) { // too lazy eck dee
                finalMap.put(entry.getKey(), new Test(entry.getValue()));
            }

            return finalMap;
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException("Failed to decrypt test cases file");
        }
    }

}
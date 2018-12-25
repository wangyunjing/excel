package test;

import com.wyj.excel.ExcelOptions;
import com.wyj.excel.ExportExcel;
import com.wyj.excel.ImportExcel;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by wyj on 17-12-21.
 */
public class Main {

    String tmpDir;

    File file;

    @Test
    public void before() throws ExecutionException, InterruptedException, FileNotFoundException {
        tmpDir = System.getProperty("java.io.tmpdir");
        file = new File(tmpDir + File.separator + "export" + File.separator + "myname.xlsx");
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        System.out.println("文件路径:" + file.getAbsolutePath());

        testExportExcel();
        testImportExcel();
    }

    public void testExportExcel() throws ExecutionException, InterruptedException, FileNotFoundException {
        List<Person> personList = new ArrayList<>();
        personList.add(new Person(new Name("w1", 1, new Date(), true), "  person1", new Introduction(1, 1, "Introduction1")));
        personList.add(new Person(new Name("w2", 2, new Date(), false), " person2", new Introduction(2, 2, "Introduction2")));
        personList.add(new Person(new Name("w3 ", 3, new Date(), true), "person3", new Introduction(3, 3, "Introduction3")));
        personList.add(new Person(new Name("w4", 4, new Date(), false), "person4", new Introduction(4, 4, "Introduction4")));
        try (OutputStream outputStream = new FileOutputStream(file)) {
            ExcelOptions options0 = ExcelOptions.Builder.<Person>create()
                    .setTitleRow(2)
                    .setSheetName("test姓名1")
                    .build();
            ExportExcel.execute(outputStream, Person.class, personList, options0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testImportExcel() {
        ExcelOptions options0 = ExcelOptions.Builder.<Person>create()
                .setTitleRow(2)
                .setSheetIdx(0)
                .setSheetName("test姓名1")
                .build();
        List<Person> names = ImportExcel.execute(file, Person.class, options0);
        System.out.println(names);
    }

}

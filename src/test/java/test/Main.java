package test;

import com.wyj.excel.ExportExcel;
import com.wyj.excel.ExportExcelOptions;
import com.wyj.excel.ImportExcel;
import com.wyj.excel.ImportExcelOptions;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
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
//        testImportExcel();
    }

    public void testExportExcel() throws ExecutionException, InterruptedException, FileNotFoundException {
        List<Person> personList = new ArrayList<>();
        personList.add(new Person(new Name("w1", 1, new Date(), true), "  person1", new Introduction(1, 1, "Introduction1")));
        personList.add(new Person(new Name("w2", 2, new Date(), false), " person2", new Introduction(2, 2, "Introduction2")));
        personList.add(new Person(new Name("w3 ", 3, new Date(), true), "person3", new Introduction(3, 3, "Introduction3")));
        personList.add(new Person(new Name("w4", 4, new Date(), false), "person4", new Introduction(4, 4, "Introduction4")));

        List<Name> names = new ArrayList<>();
        names.add(new Name("name1", 1, new Date(), true));
        names.add(new Name("name2", 2, new Date(), true));
        names.add(new Name("name3", 3, new Date(), true));
        names.add(new Name("name4", 4, new Date(), true));

        /**
         * 生成多个sheet
         */
        ExportExcelOptions options0 = ExportExcelOptions.Builder.<Person>create()
                .setTitleRow(2)
                .setSheetName("Test")
                .setPreserveNodes(true)
                .setAvoidSheetNameConflict(true)
                .build();
        ExportExcel exportExcel = ExportExcel.build(file, options0);
        try {
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    exportExcel.nextSheet(Person.class, personList);
                } else {
                    exportExcel.nextSheet(Name.class, names);
                }
            }
            exportExcel.finish();
        } catch (Exception e) {
            ExportExcel.exceptionHandle(exportExcel);
            throw new RuntimeException(e);
        } finally {
            ExportExcel.afterCompletion(exportExcel);
        }

        /**
         * 生成一个sheet
         */
        ExportExcel.execute(file, Person.class, personList, options0);
    }

    public void testImportExcel() {
        ImportExcelOptions options0 = ImportExcelOptions.Builder.<Person>create()
                .setTitleRow(2)
                .setSheetIdx(0)
                .setSheetName("test姓名1")
                .build();
        List<Person> names = ImportExcel.execute(file, Person.class, options0);
        System.out.println(names);
    }

}

package test;

import com.wyj.core.excel.ExportExcel;
import com.wyj.core.excel.ImportExcel;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by wyj on 17-12-21.
 */
public class Main {

	String tmpDir;

	File file;

	@Before
	public void before() {
		tmpDir = System.getProperty("java.io.tmpdir");
		file = new File(tmpDir + File.separator + "export" + File.separator + "myname.xls");
		System.out.println("文件路径:" + file.getAbsolutePath());
	}

	@Test
	public void testExportExcel() throws ExecutionException, InterruptedException {
		List<Person> personList = new ArrayList<>();
		personList.add(new Person(new Name("w1", 1), "person1"));
		personList.add(new Person(new Name("w2", 2), "person2"));
		personList.add(new Person(new Name("w3", 3), "person3"));
		personList.add(new Person(new Name("w4", 4), "person4"));


		Future<Void> future = ExportExcel.asyncExport(file, Person.class, personList);
		future.get();
		System.out.println("Main end!");
		System.out.println("end!");
	}

	@Test
	public void testImportExcel() {
		List<Person> names = ImportExcel.syncImport(file, Person.class);
		System.out.println(names);
	}

}

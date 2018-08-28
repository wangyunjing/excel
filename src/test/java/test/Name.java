package test;

import com.wyj.excel.annotation.Excel;

/**
 * Created by wyj on 17-12-21.
 */
public class Name {
	@Excel(name = "姓名", order = 1)
	private String name;
	@Excel(name = "年龄", order = 2)
	private int age;

	public Name() {
	}

	public Name(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Name{" +
				"name='" + name + '\'' +
				", age=" + age +
				'}';
	}
}

package test;

import com.wyj.excel.annotation.Excel;

/**
 * Created by wyj on 17-12-21.
 */
public class Name {
	@Excel(name = "姓名", order = 1)
	private String name;
	@Excel(name = "年龄", order = 2)
	private Integer age;

	public Name() {
	}

	public Name(String name, Integer age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
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

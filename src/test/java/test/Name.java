package test;

import com.wyj.excel.annotation.Excel;

import java.util.Date;

/**
 * Created by wyj on 17-12-21.
 */
public class Name {
	@Excel(name = "姓名", order = 1)
	private String name;
	@Excel(name = "年龄", order = 2)
	private int age;

	@Excel(name = "时间", order = 3, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date date;

	@Excel(name = "标记", order = 4)
	private boolean flag;

	public Name() {
	}

	public Name(String name, int age, Date date, boolean flag) {
		this.name = name;
		this.age = age;
		this.date = date;
		this.flag = flag;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "Name{" +
				"name='" + name + '\'' +
				", age=" + age +
				", date=" + date +
				'}';
	}
}

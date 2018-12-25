package test;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.annotation.Nesting;

/**
 * Created by wyj on 17-12-21.
 */
public class Person {
	@Nesting
	private Name name;

	@Excel(name = "人", order = 100)
	private String person;

//	@Excel(name = "描述", order = 50)
	private Introduction introduction;

	public Person() {
	}

	public Person(Name name, String person, Introduction introduction) {
		this.name = name;
		this.person = person;
		this.introduction = introduction;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public Introduction getIntroduction() {
		return introduction;
	}

	public void setIntroduction(Introduction introduction) {
		this.introduction = introduction;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name=" + name +
				", person='" + person + '\'' +
				'}';
	}
}

package test;

import com.example.excel.annotation.Excel;
import com.example.excel.annotation.Nesting;

/**
 * Created by wyj on 17-12-21.
 */
public class Person {
	@Nesting
	private Name name;
	@Excel(name = "人", order = 100)
	private String person;

	public Person() {
	}

	public Person(Name name, String person) {
		this.name = name;
		this.person = person;
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

	@Override
	public String toString() {
		return "Person{" +
				"name=" + name +
				", person='" + person + '\'' +
				'}';
	}
}

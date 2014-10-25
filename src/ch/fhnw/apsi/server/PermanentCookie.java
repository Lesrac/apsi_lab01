package ch.fhnw.apsi.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PermanentCookie {

	private String name;
	private String value;
	private String path;
	private LocalDateTime expireDate;
	private int maxAge;
	private double version = 1;

	public PermanentCookie(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setMaxAge(int age) {
		expireDate = LocalDateTime.now().plusSeconds(age);
		maxAge = age;
	}

	public int getMaxAge() {
		return maxAge;
	}
	
	public boolean hasExpired() {
		if (expireDate != null)
			return LocalDateTime.now().isAfter(expireDate);
		return false;
	}

	public String getExpireDate() {
		return expireDate.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	public double getVersion() {
		return version;
	}

}

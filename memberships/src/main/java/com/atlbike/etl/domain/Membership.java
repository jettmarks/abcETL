package com.atlbike.etl.domain;

import java.util.Date;

public class Membership {
	private String membership_name;
	private Integer nationbuilder_signup_id;
	private Integer membership_id;
	private String first_name;
	private String last_name;
	private String email;
	private String phone_number;
	private String mobile_number;
	private String status;
	private Date expires_on;
	private Date started_on;
	private String status_reason;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMembership_name() {
		return membership_name;
	}

	public void setMembership_name(String membership_name) {
		this.membership_name = membership_name;
	}

	public Date getExpires_on() {
		return expires_on;
	}

	public void setExpires_on(Date expires_on) {
		this.expires_on = expires_on;
	}

	public Date getStarted_on() {
		return started_on;
	}

	public void setStarted_on(Date started_on) {
		this.started_on = started_on;
	}

	public String getStatus_reason() {
		return status_reason;
	}

	public void setStatus_reason(String status_reason) {
		this.status_reason = status_reason;
	}

	public Integer getNationbuilder_signup_id() {
		return nationbuilder_signup_id;
	}

	public void setNationbuilder_signup_id(Integer nationbuilder_signup_id) {
		this.nationbuilder_signup_id = nationbuilder_signup_id;
	}

	public String getPhone_number() {
		return (phone_number != null) ? phone_number : "";
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getMobile_number() {
		return mobile_number;
	}

	public void setMobile_number(String mobile_number) {
		this.mobile_number = mobile_number;
	}

	public Integer getMembership_id() {
		return membership_id;
	}

	public void setMembership_id(Integer membership_id) {
		this.membership_id = membership_id;
	}

	@Override
	public String toString() {
		return "Renewal [membership_name=" + membership_name + ", first_name="
				+ first_name + ", last_name=" + last_name + ", email=" + email
				+ ", status=" + status + ", expires_on=" + expires_on
				+ ", started_on=" + started_on + "]";
	}

}

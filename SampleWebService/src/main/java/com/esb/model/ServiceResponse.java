package com.esb.model;

public class ServiceResponse {
	private Boolean success = false;
	private Integer reasonCode = null;
	private String resultDesc = null;
	private Object resultObj = null;

	public ServiceResponse(Integer reasonCode, String resultDesc, Object resultObj) {
		this.reasonCode = reasonCode;
		if (this.reasonCode == 0 || this.reasonCode == 1) {
			this.success = true;
		}
		this.resultDesc = resultDesc;
		this.resultObj = resultObj;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Integer getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(Integer reasonCode) {
		this.reasonCode = reasonCode;
		if (this.reasonCode == 0 || this.reasonCode == 1) {
			this.success = true;
		}
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public Object getResultObj() {
		return resultObj;
	}

	public void setResultObj(Object resultObj) {
		this.resultObj = resultObj;
	}
}

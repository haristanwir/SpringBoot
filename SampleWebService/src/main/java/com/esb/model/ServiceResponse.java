package com.esb.model;

import lombok.Data;

@Data
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

	public void setReasonCode(Integer reasonCode) {
		this.reasonCode = reasonCode;
		if (this.reasonCode == 0 || this.reasonCode == 1) {
			this.success = true;
		} else {
			this.success = false;
		}
	}

}

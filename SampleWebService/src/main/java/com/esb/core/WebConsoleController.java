package com.esb.core;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebConsoleController {

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String listFlows(Model model) {
		model.addAttribute("servlets", new String[] { "EmployeeInputFlow", "EmployeeTimerFlow" });
		return "index";
	}
}

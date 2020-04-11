package com.github.zedd7.zhorse.enums;

import java.util.ArrayList;
import java.util.List;

/**
Author: Jobisingh
**/
public enum GenderSubCommandEnum {

	STALLION("stallion"),
	MARE("mare"),
    GELDING("gelding");

	private String name;

	GenderSubCommandEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static List<String> getNameList() {
		List<String> subCommandNameList = new ArrayList<String>();
		for (GenderSubCommandEnum subCommand : GenderSubCommandEnum.values()) {
			subCommandNameList.add(subCommand.getName());
		}
		return subCommandNameList;
	}

}

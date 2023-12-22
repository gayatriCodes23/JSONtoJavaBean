package com.concerto.crud.common.bean;

import java.util.List;

public interface Entity {

	String getEntityName();

	List<Field> getFields();

	boolean isSubBean();
}

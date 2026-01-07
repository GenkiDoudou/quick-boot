package com.su60.quickboot.data.datascope;

public enum DataScopeType {
	ALL,                // 全部数据(所有的数据权限)
	DEPT,               // (部门的, 部门id不为空的)
	// (自己,需要查看自己的,并且如果有部门的权限,部门列表中不包含自己所在部门的)
	SELF;
}

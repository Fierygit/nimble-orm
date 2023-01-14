package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

/**
 * 用于测速软删除时，设置软删除的deleted字段值为id的软删除字段
 */
@Data
@Table("t_student")
public class StudentDeleteSetIdDO2 {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;

	/**这个是用于验证的*/
    @Column(value = "deleted")
	private Long deleted;
	
	@Column("name")
	private String name;

}

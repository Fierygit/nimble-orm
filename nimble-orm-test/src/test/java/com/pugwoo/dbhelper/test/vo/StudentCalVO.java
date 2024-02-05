package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

/**
 * 关联上schoolDO
 * @author NICK
 */
@Data
@Table(value = "t_student")
public class StudentCalVO {

	// 计算列示例，生成的select字段为：CONCAT(name,'hi') AS nameWithHi
	@Column(value = "nameWithHi", computed = "CONCAT(name,'hi')")
	private String nameWithHi;

}

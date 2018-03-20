package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.MustProvideConstructorException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;

public abstract class P5_DeleteOp extends P4_InsertOrUpdateOp {
	
	/////// 拦截器
	protected <T> void doInterceptBeforeDelete(Class<?> clazz, Object t) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeDelete(clazz, t);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	protected void doInterceptBeforeDelete(Class<?> clazz, String sql, Object[] args) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeDeleteCustom(clazz, sql, args);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	protected <T> void doInterceptAfterDelete(final Class<?> clazz, final Object t, final int rows) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = interceptors.size() - 1; i >= 0; i--) {
					interceptors.get(i).afterDelete(clazz, t, rows);
				}
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}
	protected void doInterceptAfterDelete(final Class<?> clazz, final String sql, final Object[] args, final int rows) {
		Runnable runnable = new Runnable() {
			public void run() {
				for (int i = interceptors.size() - 1; i >= 0; i--) {
					interceptors.get(i).afterDeleteCustom(clazz, sql, args, rows);
				}
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}
	///////////

	@Override
	public <T> int deleteByKey(T t) throws NullKeyValueException {
		Field softDelete = DOInfoReader.getSoftDeleteColumn(t.getClass());
		
		List<Object> values = new ArrayList<Object>();

		doInterceptBeforeDelete(t.getClass(), t);
		
		String sql = null;
		
		if(softDelete == null) { // 物理删除
			sql = SQLUtils.getDeleteSQL(t, values);
		} else { // 软删除
			Column softDeleteColumn = softDelete.getAnnotation(Column.class);
			sql = SQLUtils.getSoftDeleteSQL(t, softDeleteColumn, values);
		}
		
		int rows = jdbcExecuteUpdate(sql, values.toArray());
		doInterceptAfterDelete(t.getClass(), t, rows);
		
		return rows;
	}
		
	@Override
	public <T> int deleteByKey(Class<T> clazz, Object keyValue) 
			throws NullKeyValueException, MustProvideConstructorException {
		if(keyValue == null) {
			throw new NullKeyValueException();
		}

		Field keyField = DOInfoReader.getOneKeyColumn(clazz);
		
		try {
			T t = (T) clazz.newInstance();
			DOInfoReader.setValue(keyField, t, keyValue);
			return deleteByKey(t);
		} catch (InstantiationException e) {
			throw new MustProvideConstructorException();
		} catch (IllegalAccessException e) {
			throw new MustProvideConstructorException();
		}
	}
	
	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请直接运维人员truncate表。
			throw new InvalidParameterException("delete postSql is blank. it's very dangerous"); 
		}
		
		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除

		String sql = null;
		if(softDelete == null) { // 物理删除
			sql = SQLUtils.getCustomDeleteSQL(clazz, postSql);
		} else { // 软删除
			sql = SQLUtils.getCustomSoftDeleteSQL(clazz, postSql);
		}

		doInterceptBeforeDelete(clazz, sql, args);
		int rows = namedJdbcExecuteUpdate(sql, args);
		doInterceptAfterDelete(clazz, sql, args, rows);
		
		return rows;
	}
	
}

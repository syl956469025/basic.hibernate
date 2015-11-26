package org.konghao.basic.dao;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.konghao.basic.model.Pager;
import org.konghao.basic.model.SystemContext;

/**
 * @author shi
 *
 */
@SuppressWarnings("unchecked")
public class BaseDao<T> implements IBaseDao<T> {
	
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Inject
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * 创建一个Class的对象类获取泛型的class
	 */
	private Class<T> clz;
	public Class<T> getClz() {
		if (clz == null) {
			//获取泛型的class对象
			clz = (Class<T>)((ParameterizedType)(this.getClass().getGenericSuperclass())).getActualTypeArguments()[0];
		}
		return clz;

	}
	public void setClz(Class<T> clz) {
		this.clz = clz;
	}

	protected Session getSession(){
		return sessionFactory.openSession();
	}
	
	private String initSort(String hql){
		String sort = SystemContext.getSort(); 
		String order = SystemContext.getOrder();
		if (null != sort && !"".equals(sort)) {
			hql += " order by "+sort;
			if (!"desc".equals(order)) {
				hql += " asc";
			}else {
				hql +=" desc";
			}
		}
		return hql;
	}
	@SuppressWarnings("rawtypes")
	private void setAliasParameter(Query query , Map<String,Object> alias){
		if (null != alias) {
			Set<String> keys = alias.keySet();
			for (String key : keys) {
				Object val = alias.get(key);
				if (val instanceof Collection) {
					//查询条件是列表
					query.setParameterList(key, (Collection)val);
				}else {
					query.setParameter(key, val);
				}
			}
		}
	}
	
	private void setParameter(Query query ,Object[] args){
		if (null !=args && args.length>0) {
			int index = 0;
			for (Object arg :args) {
				query.setParameter(index++, arg);
			}
		}
	}
	
	private String setCountQuery(String hql,boolean isHql){
		String e = hql.substring(hql.indexOf("from"));
		String c = "select count(*) "+e;
		if (isHql) {
			c.replace("fetch", "");
		}
		return c;
	}
	@SuppressWarnings("rawtypes")
	private void setPageQuery(Query query, Pager page){
		Integer pageSize = SystemContext.getPageSize();
		Integer pageOffset = SystemContext.getPageOffset();
		page.setSize(pageSize);
		page.setOffset(pageOffset);
		query.setFirstResult(pageOffset).setMaxResults(pageSize);
	}
	
	@Override
	public T add(T t) {
		getSession().save(t);
		return t;
	}
	@Override
	public void update(T t) {
		getSession().update(t);
	}
	@Override
	public void delete(int id) {
		getSession().delete(this.load(id));
	}

	@Override
	public T load(int id) {
		return (T) getSession().load(getClz(), id);
	}
	@Override
	public List<T> list(String hql, Object[] args) {
		return this.listByAlias(hql, args, null);
	}
	@Override
	public List<T> list(String hql, Object arg) {
		return this.listByAlias(hql, new Object[] {arg}, null);
	}
	@Override
	public List<T> list(String hql) {
		return this.list(hql, null);
	}
	@Override
	public List<T> listByAlias(String hql, Object[] args, Map<String, Object> alias) {
		hql = initSort(hql);
		Query query =  getSession().createQuery(hql);
		setAliasParameter(query,alias);
		setParameter(query,args);
		return query.list();
	}
	@Override
	public List<T> listByAlias(String hql, Map<String, Object> alias) {
		return this.listByAlias(hql, null, alias);
	}
	@Override
	public Pager<T> find(String hql, Object[] args) {
		return this.findByAlias(hql, args, null);
	}
	@Override
	public Pager<T> find(String hql, Object arg) {
		return this.findByAlias(hql, new Object [] {arg}, null);
	}
	@Override
	public Pager<T> find(String hql) {
		return this.findByAlias(hql, null);
	}
	@Override
	public Pager<T> findByAlias(String hql, Object[] args, Map<String, Object> alias) {
		hql = initSort(hql);
		String chql = setCountQuery(hql,true);
		Query query =  getSession().createQuery(hql);
		Query cquery =  getSession().createQuery(chql);
		Pager<T> pages = new Pager<T>();
		//设置别名参数
		setAliasParameter(query,alias);
		setAliasParameter(cquery,alias);
		//设置参数
		setParameter(query,args);
		setParameter(cquery,args);
		//设置分页
		setPageQuery(cquery, pages);
		List<T> datas = query.list();
		long total = (Long) cquery.uniqueResult();
		pages.setDatas(datas);
		pages.setTotal(total);
		return pages;
	}
	@Override
	public Pager<T> findByAlias(String hql, Map<String, Object> alias) {
		return this.findByAlias(hql, null, alias);
	}
	@Override
	public Object queryObject(String hql, Object[] args) {
		return this.queryObject(hql, args, null);
	}
	@Override
	public Object queryObject(String hql, Object arg) {
		return this.queryObject(hql, new Object [] {arg}, null);
	}
	@Override
	public Object queryObject(String hql) {
		return this.queryObject(hql, null);
	}
	@Override
	public void updateByHql(String hql, Object[] args) {
		Query query = getSession().createQuery(hql);
		setParameter(query, args);
		query.executeUpdate();
	}
	@Override
	public void updateByHql(String hql, Object arg) {
		this.updateByHql(hql, new Object [] {arg});
	}
	@Override
	public void updateByHql(String hql) {
		this.updateByHql(hql, null);
	}
	@Override
	public List<Object> listBySql(String sql, Object[] args, Class<Object> ciz,
			boolean hasEntity) {
		return this.listByAliasSql(sql, args, null, ciz, hasEntity);
	}
	@Override
	public List<Object> listBySql(String sql, Object arg, Class<Object> ciz,
			boolean hasEntity) {
		return this.listByAliasSql(sql, new Object [] {arg}, null, ciz, hasEntity);
	}
	@Override
	public List<Object> listBySql(String sql, Class<Object> ciz, boolean hasEntity) {
		return this.listByAliasSql(sql, null, ciz, hasEntity);
	}
	@Override
	public List<Object> listByAliasSql(String sql, Object[] args,
			Map<String, Object> alias, Class<Object> ciz, boolean hasEntity) {
		sql = initSort(sql);
		SQLQuery query = getSession().createSQLQuery(sql);
		setAliasParameter(query, alias);
		setParameter(query, args);
		if (hasEntity) {
			query.addEntity(ciz);
		}else {
			query.setResultTransformer(Transformers.aliasToBean(ciz));
		}
		return query.list();
	}
	@Override
	public List<Object> listByAliasSql(String sql, Map<String, Object> alias,
			Class<Object> ciz, boolean hasEntity) {
		return this.listByAliasSql(sql, null, alias, ciz, hasEntity);
	}
	@Override
	public Pager<Object> findBySql(String sql, Object[] args, Class<Object> ciz,
			boolean hasEntity) {
		return this.findByAliasSql(sql, args, null, ciz, hasEntity);
	}
	@Override
	public Pager<Object> findBySql(String sql, Object arg, Class<Object> ciz,
			boolean hasEntity) {
		return this.findByAliasSql(sql, new Object [] {arg}, null, ciz, hasEntity);
	}
	@Override
	public Pager<Object> findBySql(String sql, Class<Object> ciz, boolean hasEntity) {
		return this.findByAliasSql(sql, null, ciz, hasEntity);
	}
	@Override
	public Pager<Object> findByAliasSql(String sql, Object[] args,
			Map<String, Object> alias, Class<Object> ciz, boolean hasEntity) {
		sql = initSort(sql);
		String csql = setCountQuery(sql, false);
		Pager<Object> pages =  new Pager<Object>();
		SQLQuery query = getSession().createSQLQuery(sql);
		SQLQuery sq = getSession().createSQLQuery(csql);
		setAliasParameter(query, alias);
		setAliasParameter(sq, alias);
		setParameter(query, args);
		setParameter(sq, args);
		setPageQuery(query, pages);
		if (hasEntity) {
			query.addEntity(ciz);
		}else {
			query.setResultTransformer(Transformers.aliasToBean(ciz));
		}
		long total = (Long) sq.uniqueResult();
		pages.setTotal(total);
		List<Object> list = query.list();
		pages.setDatas(list);
		return pages;
	}
	@Override
	public Pager<Object> findByAliasSql(String sql, Map<String, Object> alias,
			Class<Object> ciz, boolean hasEntity) {
		return this.findByAliasSql(sql, null, alias, ciz, hasEntity);
	}

	@Override
	public Object queryObject(String hql, Object[] args,
			Map<String, Object> alias) {
		Query query = getSession().createQuery(hql);
		setAliasParameter(query, alias);
		setParameter(query, args);
		return query.uniqueResult();
	}

	@Override
	public Object queryObjectByAlias(String hql, Map<String, Object> alias) {
		return this.queryObject(hql, null, alias);
	}

}

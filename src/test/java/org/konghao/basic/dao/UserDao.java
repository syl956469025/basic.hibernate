package org.konghao.basic.dao;

import org.konghao.basic.model.User;
import org.springframework.stereotype.Repository;

@Repository("userDao")
public class UserDao extends BaseDao<User> implements IBaseDao<User> {

}

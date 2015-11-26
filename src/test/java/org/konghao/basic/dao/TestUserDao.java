package org.konghao.basic.dao;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/bean.xml")
public class TestUserDao {
	
	@Inject
	private IUserDao userDao;

}

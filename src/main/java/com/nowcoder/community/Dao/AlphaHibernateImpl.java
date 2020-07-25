package com.nowcoder.community.Dao;

import org.springframework.stereotype.Repository;

@Repository("alphaHibernate")
public class AlphaHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}

package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 @Mapper注解的作用
 1.把mapper这个DAO交給Spring管理
 2.不用再写mapper映射文件
 3.MapStruct可以帮助我们自动根据一个添加@Mapper注解的接口生成一个实现类
 */

@Mapper
public interface DiscussPostMapper {



    /**
     * 获取帖子,按页获取,
     * @param userId 用户的id
     * @param offset 每一页的起始行的行号
     * @param limit 每一页最多显示几个数据
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 查询帖子的行数
     * @Param注解用于给参数取别名,有的参数比较长,可以用@Param取一个别名
     * 如果sql中要动态用到这个参数,比如 <if> ,并且 <if> 条件判断只用到这一个参数,则必须加别名.
     * 但是上面一个函数用三个参数,那就可以不用取别名了.
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

}

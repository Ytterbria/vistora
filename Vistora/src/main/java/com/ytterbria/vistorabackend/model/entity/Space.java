package com.ytterbria.vistorabackend.model.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 空间
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别: 0-普通空间, 1-专业版,2-企业版
     */
    private Integer spaceLevel;

    /**
     * 空间已用容量
     */
    private Long spaceSize;

    /**
     * 空间已用数量
     */
    private Long spaceCount;

    /**
     * 空间最大容量
     */
    private Long maxSize;

    /**
     * 空间最大数量
     */
    private Long maxCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 最近编辑时间
     */
    private Date editTime;

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    private Boolean isDeleted;


    private static final long serialVersionUID = 1L;
}
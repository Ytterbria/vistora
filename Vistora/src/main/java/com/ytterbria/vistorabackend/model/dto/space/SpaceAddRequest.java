package com.ytterbria.vistorabackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceAddRequest implements Serializable {

    /**
     * 序列化ID
     */
    @Serial
    private static final long serialVersionUID = 1101151587992316026L;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-个人空间，1-团队空间
     */
    private Integer spaceType;
}

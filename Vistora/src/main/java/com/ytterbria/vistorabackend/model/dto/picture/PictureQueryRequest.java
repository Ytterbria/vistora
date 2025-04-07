package com.ytterbria.vistorabackend.model.dto.picture;

import com.ytterbria.vistorabackend.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PictureQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 3250300459708804244L;

    private Long id;

    private String name;

    private String introduction;

    private String category;

    private List<String> tags;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private String searchText;

    private Long userId;

}

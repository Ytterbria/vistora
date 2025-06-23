package com.ytterbria.vistorabackend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PictureUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4014836601764405571L;

    private Long id;

    private String name;

    private String introduction;

    private List<String> tags;
}

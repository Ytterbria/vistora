package com.ytterbria.vistorabackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceTagAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 2367947030059402086L;
}

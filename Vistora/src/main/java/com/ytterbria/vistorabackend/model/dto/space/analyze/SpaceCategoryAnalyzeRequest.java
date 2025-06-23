package com.ytterbria.vistorabackend.model.dto.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceCategoryAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6357148684364558446L;
}

package com.ytterbria.vistorabackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ytterbria.vistorabackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.ytterbria.vistorabackend.model.dto.space.analyze.SpaceCategoryAnalyzeResponse;
import com.ytterbria.vistorabackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.ytterbria.vistorabackend.model.dto.space.analyze.SpaceUsageAnalyzeResponse;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.User;

import java.util.List;

public interface SpaceAnalyzeService extends IService<Space> {

    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

}

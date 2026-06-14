package io.github.genkidoudou.web.ai.prompt.service;



import io.github.genkidoudou.common.api.PageInfo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptBo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptionVo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptQueryBo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptVo;



import java.util.List;



/**

 * 提示词管理服务。

 */

public interface AiPromptService {



    PageInfo<AiPromptVo> page(AiPromptQueryBo query);



    AiPromptVo getInfo(Long promptId);



    Long add(AiPromptBo req);



    void update(AiPromptBo req);



    void removeBatch(List<Long> promptIds);

    /**
     * 下拉选项列表（未删除，按更新时间倒序）。
     *
     * @return 选项集合
     */
    List<AiPromptOptionVo> listOptions();

}


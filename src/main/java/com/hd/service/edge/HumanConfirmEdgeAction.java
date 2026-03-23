package com.hd.service.edge;

import com.hd.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.action.EdgeAction;
import org.springframework.stereotype.Component;

/**
 * 人工确认结果判断：根据 humanConfirmed 决定流程走向
 */
@Component
public class HumanConfirmEdgeAction implements EdgeAction<AnalysisTaskState> {

    @Override
    public String apply(AnalysisTaskState state) {
        if (state.isHumanConfirmed()) {
            return "continue";  // 继续执行后续节点
        }
        return "end";  // 直接结束流程
    }
}

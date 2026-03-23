package com.hd.service.agent;

import com.hd.domain.dto.AnalysisTaskState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Scanner;

/**
 * 人工确认 Agent：等待人工介入确认后继续流程
 */
@Component
@Slf4j
public class HumanConfirmAgent implements NodeAction<AnalysisTaskState> {

    private final Scanner scanner;

    public HumanConfirmAgent() {
        this.scanner = new Scanner(System.in);
    }

    public AnalysisTaskState confirm(AnalysisTaskState state) {
        log.info("\n===== 等待人工确认 =====");
        log.info("当前报告草稿:\n{}", state.getReportDraft());
        log.info("\n请输入指令 (APPROVE - 确认继续 / REJECT - 拒绝重试):");
        while (true) {
            String input = scanner.nextLine().trim().toUpperCase();
            if ("APPROVE".equals(input)) {
                log.info("人工确认通过，继续流程...");
                state.setHumanConfirmed(true);
                return state;
            } else if ("REJECT".equals(input)) {
                log.info("人工拒绝，终止流程");
                state.setHumanConfirmed(false);
                return state;
            } else {
                log.warn("无效指令，请输入 APPROVE 或 REJECT:");
            }
        }
    }

    @Override
    public Map<String, Object> apply(AnalysisTaskState analysisTaskState) throws Exception {
        AnalysisTaskState result = this.confirm(analysisTaskState);
        return result.toMap();
    }
}

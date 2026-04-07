package com.hd.service.agent;

import com.hd.domain.dto.AnalysisTaskState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.action.AsyncNodeActionWithConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * 数据分析 Agent：对原始数据进行统计分析，提炼关键结论（如 Top3、增长率）
 */
@Component
@Slf4j
public class DataAnalysisAgent implements AsyncNodeActionWithConfig<AnalysisTaskState> {

    private final ChatClient dataAnalysisClient;

    public DataAnalysisAgent(@Qualifier("dataAnalysisClient") ChatClient dataAnalysisClient) {
        this.dataAnalysisClient = dataAnalysisClient;
    }

    public AnalysisTaskState analyze(AnalysisTaskState state, RunnableConfig config) {
        // 从config中获取userId
        String userId = (String) config.metadata("userId").orElse("anonymous");
        log.info("用户 {} 正在执行数据分析", userId);
        
        String userQuery = state.getUserQuery();
        String rawData = state.getRawData();
        String targetIndicator = state.getTargetIndicator();
        String analysisDimension = state.getAnalysisDimension();

        // 构建提示词：让 AI 分析原始数据，生成结构化结论
        String prompt = String.format("""
                以下是原始数据（%s按%s统计）：
                %s
                
                请完成以下分析：
                1. 找出%s排名前4的%s；
                2. 计算前4名的总%s占比；
                3. 分析前4名领先的可能原因（结合电商行业常识）；
                4. 结论简洁明了，分点说明（不要超过500字）。
                
                用户原始需求：%s
                """, targetIndicator, analysisDimension, rawData, targetIndicator, analysisDimension, targetIndicator, userQuery);


        String analysisResult = dataAnalysisClient.prompt().user(prompt).call().content();
        log.info("数据分析结论：" + analysisResult);

        return state.withAnalysisResult(analysisResult);
    }


    @Override
    public CompletableFuture<Map<String, Object>> apply(AnalysisTaskState state, RunnableConfig config) {
        AnalysisTaskState result = this.analyze(state, config);
        return CompletableFuture.completedFuture(result.toMap());
    }
}

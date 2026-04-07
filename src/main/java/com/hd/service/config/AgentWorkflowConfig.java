package com.hd.service.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hd.domain.dto.AnalysisTaskState;
import com.hd.service.agent.*;
import com.hd.service.edge.HumanConfirmEdgeAction;
import com.hd.service.edge.RetryEdgeAction;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 多智能体流程配置：用 LangGraph4j 定义节点流转规则
 */
@Configuration
public class AgentWorkflowConfig {

    @Autowired
    private ChatModel chatModel;

    @Bean
    public CompiledGraph<AnalysisTaskState> analysisWorkflow(
            TaskDecompositionAgent decompositionAgent,
            DataQueryAgent dataQueryAgent,
            DataAnalysisAgent dataAnalysisAgent,
            ReportGenerationAgent reportGenerationAgent,
            HumanConfirmAgent humanConfirmAgent,
            ResultValidationAgent validationAgent,
            RetryEdgeAction retryEdgeAction,
            HumanConfirmEdgeAction humanConfirmEdgeAction,
            AnalysisTaskStateFactory stateFactory
    ) throws GraphStateException {

        // 1. 创建状态图构建器（指定状态类型）
        StateGraph<AnalysisTaskState> graphBuilder = new StateGraph<>(stateFactory);

        // 2. 添加节点（每个智能体对应一个节点）
        graphBuilder.addNode("decompose", node_async(decompositionAgent)); // 任务拆解
        graphBuilder.addNode("query", node_async(dataQueryAgent)); // 数据查询
        graphBuilder.addNode("analyze", dataAnalysisAgent); // 数据分析
        graphBuilder.addNode("generateReport", node_async(reportGenerationAgent)); // 报告生成
        graphBuilder.addNode("humanConfirm", node_async(humanConfirmAgent)); // 人工介入节点
        graphBuilder.addNode("validate", node_async(validationAgent)); // 结果校验

        // 3. 定义流程流转规则（边）
        // 起始节点 → 任务拆解节点
        graphBuilder.addEdge(START, "decompose");

        // 任务拆解 → 数据查询 → 数据分析 → 报告生成 → 人工确认 → 结果校验
        graphBuilder.addEdge("decompose", "query");
        graphBuilder.addEdge("query", "analyze");
        graphBuilder.addEdge("analyze", "generateReport");
        graphBuilder.addEdge("generateReport", "humanConfirm");

        // 人工确认 → 分支判断（通过则继续，拒绝则结束）
        graphBuilder.addConditionalEdges(
                "humanConfirm",
                AsyncEdgeAction.edge_async(humanConfirmEdgeAction),
                Map.of(
                        "continue", "validate",
                        "end", END
                )
        );

        // 结果校验 → 分支判断（符合则结束，不符合则重试任务拆解）
        graphBuilder.addConditionalEdges(
                "validate",
                AsyncEdgeAction.edge_async(retryEdgeAction),
                Map.of(
                        "retry", "decompose",
                        "end", END
                )
        );

        return graphBuilder.compile();
    }

    @Bean("dataAnalysisClient")
    public ChatClient chatClent(ChatModel chatModel) {
        // 调用 AI 模型获取分析结果（显式启用联网搜索）
//        DashScopeChatOptions options = DashScopeChatOptions.builder()
//                .withEnableSearch(true)
//                .build();
        return ChatClient.builder(chatModel).build();
    }
}

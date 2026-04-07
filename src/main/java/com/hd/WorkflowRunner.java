package com.hd;

import com.hd.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/**
 * 控制台交互式运行入口（支持Scanner输入）
 */
public class WorkflowRunner {

    public static void main(String[] args) {
        // 启动Spring容器
        ApplicationContext context = SpringApplication.run(MutilAgentApplication.class, args);
        
        // 获取工作流Bean
        @SuppressWarnings("unchecked")
        CompiledGraph<AnalysisTaskState> workflow = context.getBean(CompiledGraph.class);
        
        // 构造初始状态
        String userQuery = "分析2025年Q3电商订单数据，生成销量Top4品类及增长原因，给出后续网店选品建议";
        AnalysisTaskState initialState = new AnalysisTaskState();
        initialState.setUserQuery(userQuery);
        
        // 构造运行配置（传递用户身份等参数）
        RunnableConfig config = RunnableConfig.builder()
                .threadId("thread-" + System.currentTimeMillis())  // 流程实例ID
                .addMetadata("userId", "user_10086")               // 用户ID
                .build();
        
        // 执行工作流（会在HumanConfirmAgent处阻塞等待输入）
        System.out.println("\n==================== 开始执行多智能体流程 ====================");
        AnalysisTaskState finalState = workflow.invoke(initialState.toMap(), config)
                .orElse(new AnalysisTaskState());
        
        // 输出最终报告
        System.out.println("\n==================== #最终数据分析报告# ====================");
        System.out.println(finalState.getFinalReport());
    }
}

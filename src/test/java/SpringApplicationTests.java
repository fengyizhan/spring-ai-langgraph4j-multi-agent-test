import com.hd.MutilAgentApplication;
import com.hd.domain.dto.AnalysisTaskState;
import jakarta.transaction.Transactional;
import org.bsc.langgraph4j.CompiledGraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MutilAgentApplication.class)
@Transactional
public class SpringApplicationTests {

    @Autowired
    private CompiledGraph<AnalysisTaskState> analysisWorkflow;

    @Test
    void testAnalysisWorkflow() {
        // 1. 构造用户需求（初始状态）
        String userQuery = "分析2025年Q3电商订单数据，生成销量Top4品类及增长原因，给出后续网店选品建议";
        AnalysisTaskState initialState = new AnalysisTaskState();
        initialState.setUserQuery(userQuery);

        // 2. 执行多智能体流程（流程会在 HumanConfirmAgent 中阻塞等待控制台输入）
        AnalysisTaskState finalState = analysisWorkflow.invoke(initialState.toMap())
                .orElse(new AnalysisTaskState());

        // 3. 输出最终报告
        System.out.println("\n==================== #最终数据分析报告# ====================");
        System.out.println(finalState.getFinalReport());
    }
}

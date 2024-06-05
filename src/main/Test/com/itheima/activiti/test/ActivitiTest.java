package com.itheima.activiti.test;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ActivitiTest {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    //部署测试
    @Test
    public void deployTest() {
        //创建部署
        Deployment deployment = this.repositoryService.createDeployment()
                //添加资源文件
                .addClasspathResource("bpmn/diagram.bpmn")
                .addClasspathResource("bpmn/diagram.png")
                //设置流程名字
                .name("Process_1")
                .deploy();

        System.out.println(deployment);
    }

    //运行实例测试
    @Test
    public void runTest() {
        //运行实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_1", "bk2");
        System.out.println(processInstance);
    }

    //任务查询测试
    @Test
    public void taskQueryTest() {
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee("张三")
                //业务key
                .processInstanceBusinessKey("bk2")
                .singleResult();
        System.out.println(task);
    }

    @Test
    public void testAgree() {
        Task task = this.taskService.createTaskQuery()
                .processDefinitionKey("Process_1")
                .taskAssignee("李四")
                .processInstanceBusinessKey("businessKey_01").singleResult();

        if (null != task) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("approvalStatus", "同意");
            variables.put("approvalNote", "123");

            this.taskService.complete(task.getId(), variables);
            System.out.println("任务完成...");
        }
    }

    //任务完成测试
    @Test
    public void taskCompleteTest() {
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee("张三")
                //业务key
                .processInstanceBusinessKey("bk2")
                .singleResult();
        System.out.println(task);
        taskService.complete(task.getId());
    }

    //任务拒绝测试
    @Test
    public void taskRejectTest() {
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee("张三")
                //业务key
                .processInstanceBusinessKey("bk2")
                .singleResult();
        System.out.println(task);
        String reason = "不接受";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalStatus", "不同意");
        variables.put("approvalNode", reason);
        runtimeService.setVariables(task.getExecutionId(), variables);
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), reason);
    }

    //查询历史任务
    @Test
    public void getHistory() {
        HistoricTaskInstanceQuery instanceQuery = this.historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables() //在任务查询结果中包括全局任务变量
                .orderByHistoricTaskInstanceEndTime().desc() //按照结束时间倒序排序
                .finished() //只查询已完成的流程
                .taskAssignee("李四"); //设置负责人
        //自定义流程变量  条件查询
//        instanceQuery.processVariableValueGreaterThan("days", "1");

        List<HistoricTaskInstance> list = instanceQuery.list();

        for (HistoricTaskInstance history : list) {
            System.out.println("Id: " + history.getId());
            System.out.println("ProcessInstanceId: " + history.getProcessInstanceId());
            System.out.println("StartTime: " + history.getStartTime());
            System.out.println("Name: " + history.getName());
            Map<String, Object> processVariables = history.getProcessVariables();
            System.out.println(processVariables.get("days").toString());
            System.out.println(processVariables.get("reason").toString());
        }

    }

}

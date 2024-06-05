package com.itheima.activiti.test;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ActivitiHomework {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    private final String AGENT1 = "employee";

    private final String AGENT2 = "manager";

    private final String AGENT3_1 = "account1";

    private final String AGENT3_2 = "account2";

    private final int NUMBER = 10000;

    private final String REASONEMPLOYEE = "加班补贴";

    private final String REASONREJECT = "没钱了";



    //部署流程实例
    @Test
    public void deployProcess(){
        //部署
        Deployment deployment = repositoryService.createDeployment()
                //添加资源文件
                .addClasspathResource("bpmn/diagramHomework.bpmn")
                .addClasspathResource("bpmn/diagramHomework.png")
                //设置流程名字
                .name("Process_1")
                .deploy();

        System.out.println(deployment);
    }

    //启动流程实例
    @Test
    public void runTest(){
        //运行实例
        Map<String,Object> map = new HashMap<>();
        map.put("agent1",AGENT1);
        map.put("agent2",AGENT2);
        map.put("agent3_1",AGENT3_1);
        map.put("agent3_2",AGENT3_2);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_1","bkTest",map);
        System.out.println(processInstance);
    }

    //员工提交申请单
    @Test
    public void employeeTest(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT1)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("number", NUMBER);
            variables.put("reasonEmployee", REASONEMPLOYEE);
            this.taskService.complete(task.getId(),variables);
            System.out.printf("%s的%s号请求单申请完成",task.getAssignee(),task.getId());
        }
    }

    //经理审批（同意）
    @Test
    public void manageComplete(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT2)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("note", "agree");
            this.taskService.complete(task.getId(),variables);
            System.out.printf("%s的%s号请求批准完成--%s",AGENT1,task.getId(),task.getAssignee());
        }
    }

    //经理审批（拒绝）
    @Test
    public void manageReject(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT2)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("reason", REASONREJECT);
            runtimeService.setVariables(task.getExecutionId(), variables);
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), REASONREJECT);
            System.out.printf("%s的%s号请求单被%s驳回",AGENT1,task.getId(),task.getAssignee());
        }
    }

    //财务主管审批（同意）
    @Test
    public void account1Complete(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT3_1)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("note", "agree");
            try {
                taskService.complete(task.getId(), variables);
            }catch (ActivitiException activitiException){
                //网关拦截
                System.out.println("申请金额不符合要求");
                return;
            }
            System.out.printf("%s的%s号请求批准完成--%s",AGENT1,task.getId(),task.getAssignee());
        }
    }

    //经理审批（拒绝）
    @Test
    public void account1Reject(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT3_1)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("reason", REASONREJECT);
            runtimeService.setVariables(task.getExecutionId(), variables);
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), REASONREJECT);
            System.out.printf("%s的%s号请求单被%s驳回",AGENT1,task.getId(),task.getAssignee());
        }
    }

    //财务专员审批（同意）
    @Test
    public void account2Complete(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT3_2)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("note", "agree");
            this.taskService.complete(task.getId(),variables);
            System.out.printf("%s的%s号请求批准完成--%s",AGENT1,task.getId(),task.getAssignee());
        }
    }

    //经理审批（拒绝）
    @Test
    public void account2Reject(){
        //查询任务
        Task task = taskService.createTaskQuery()
                //key
                .processDefinitionKey("Process_1")
                //代理人
                .taskAssignee(AGENT3_2)
                //业务key
                .processInstanceBusinessKey("bkTest")
                .singleResult();
        System.out.println(task);
        //添加属性
        if (!ObjectUtils.isEmpty(task)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("reason", REASONREJECT);
            runtimeService.setVariables(task.getExecutionId(), variables);
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), REASONREJECT);
            System.out.printf("%s的%s号请求单被%s驳回",AGENT1,task.getId(),task.getAssignee());
        }
    }
}

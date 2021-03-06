package org.hgq.delegate;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.hgq.domain.base.CollectionDerate;
import org.hgq.domain.base.CollectionDerateExample;
import org.hgq.mapper.base.CollectionDerateMapper;
import org.hgq.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @description:
 * @author: huangguoqiang
 * @create: 2022-04-07 15:28
 **/

@Service
//public class WithholdService implements JavaDelegate {
public class WithholdService implements TaskListener {

    @Autowired
    private OrderService orderService;


    @Autowired
    private TaskService taskService;
    @Autowired
    HistoryService historyService;

    @Autowired
    CollectionDerateMapper collectionDerateMapper;

   /* @Override
    public void execute(DelegateExecution execution) throws Exception {

        String processBusinessKey = execution.getProcessBusinessKey();
        String processDefinitionId = execution.getProcessDefinitionId();
        String businessKey = execution.getBusinessKey();
        String processInstanceId = execution.getProcessInstanceId();
        System.out.println("processBusinessKey: " + processBusinessKey);
        System.out.println("processDefinitionId: " + processDefinitionId);
        System.out.println("businessKey: " + businessKey);
        System.out.println("processInstanceId: " + processInstanceId);

        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                String order = orderService.getOrder();
                System.out.println("org.hgq.delegate.Withhold.execute: " + order+ "LocalDate.now(): " + LocalTime.now() +" Thread: " +Thread.currentThread());

                if(orderService.increment()== 40){
                    //${withhold==1}
                    //????????????  withhold==1 ????????????  withhold==0 ????????????
                    Map<String, Object> variables = new HashMap<>();
                    //withhold==1 ????????????  withhold==0 ????????????
                    variables.put("withhold", 1);
                    execution.setVariables(variables);


                    System.out.println("????????????");
                    this.cancel();
                }

            }
        };
        timer.schedule(timerTask,2000l,2000l);

    }*/

    @Override
    public void notify(DelegateTask delegateTask) {
        String id = delegateTask.getId();
        String processDefinitionId = delegateTask.getProcessDefinitionId();

        String processInstanceId = delegateTask.getProcessInstanceId();
        System.out.println("id: " + id);
        System.out.println("processDefinitionId: " + processDefinitionId + " Thread: " + Thread.currentThread());
        System.out.println("processInstanceId: " + processInstanceId);

        if (TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())) {

            //??????????????????
            Object operator_id = delegateTask.getVariable("operator_id");
            delegateTask.setAssignee(operator_id.toString());

            Timer timer = new Timer();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {

                    //?????????????????????????????????id ??????????????????

                    String order = orderService.getOrder();
                    System.out.println("org.hgq.delegate.Withhold.execute: " + order + "LocalDate.now(): " + LocalTime.now() + " Thread: " + Thread.currentThread());

                    if (orderService.increment() == 20) {
                        //${withhold==1}
                        //????????????  withhold==1 ????????????  withhold==0 ????????????
                        Map<String, Object> variables = new HashMap<>();
                        //withhold==1 ????????????  withhold==0 ????????????
                        variables.put("withhold", 0);
                        taskService.complete(id, variables);


                        //???????????????
                        //???????????????
                        CollectionDerate updateDerate = new CollectionDerate();
                        //1-??????????????? 2-??????????????????????????? 3-??????????????? 4-??????????????????????????? 5-???????????? 6-???????????? 99-????????? :status
                        updateDerate.setStatus(2);
                        Instant now = Instant.now();
                        updateDerate.setAuditTime(now);
                        updateDerate.setUpdateTime(now);

                        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
                        HistoricProcessInstance historicProcessInstance = historicProcessInstanceQuery.processInstanceId(processInstanceId).singleResult();
                        String businessKey = historicProcessInstance.getBusinessKey();
                        CollectionDerateExample example = new CollectionDerateExample();
                        example.createCriteria().andIdEqualTo(Long.valueOf(businessKey));
                        collectionDerateMapper.updateByExampleSelective(updateDerate, example);


                        System.out.println("????????????");
                        this.cancel();
                    }

                }
            };
            timer.schedule(timerTask, 2000l, 2000l);
        }


    }
}

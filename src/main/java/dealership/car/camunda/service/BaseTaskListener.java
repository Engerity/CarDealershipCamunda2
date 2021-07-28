package dealership.car.camunda.service;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component("baseTaskListener")
public abstract class BaseTaskListener extends BaseDelegate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        super.execute(delegateTask);
    }

}

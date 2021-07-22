package dealership.car.camunda.engine;

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;

public abstract class LoggingBpmnParseListener extends AbstractBpmnParseListener {

    protected final LoggingListeners.LoggingExecutionListener loggingExecutionListener;
    protected final LoggingListeners.LoggingTransitionListener loggingTransitionListener;
    protected final LoggingListeners.LoggingUserTaskExecutionListener loggingUserTaskExecutionListener;
    protected final LoggingListeners.LoggingStartEndEventExecutionListener loggingStartEndEventExecutionListener;

    public LoggingBpmnParseListener() {
        LoggingListeners loggingListeners = new LoggingListeners();
        loggingExecutionListener = loggingListeners.executionListener;
        loggingTransitionListener = loggingListeners.transitionListener;
        loggingUserTaskExecutionListener = loggingListeners.userTaskExecutionListener;
        loggingStartEndEventExecutionListener = loggingListeners.startEndEventExecutionListener;
    }

}
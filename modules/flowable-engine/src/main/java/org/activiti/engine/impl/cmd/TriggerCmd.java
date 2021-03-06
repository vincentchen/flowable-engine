/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.cmd;

import java.util.Map;

import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.Activiti5Util;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TriggerCmd extends NeedsActiveExecutionCmd<Object> {

  private static final long serialVersionUID = 1L;

  protected Map<String, Object> processVariables;
  protected Map<String, Object> transientVariables;

  public TriggerCmd(String executionId, Map<String, Object> processVariables) {
    super(executionId);
    this.processVariables = processVariables;
  }
  
  public TriggerCmd(String executionId, Map<String, Object> processVariables, Map<String, Object> transientVariables) {
    this(executionId, processVariables);
    this.transientVariables = transientVariables;
  }

  protected Object execute(CommandContext commandContext, ExecutionEntity execution) {
    if (Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, execution.getProcessDefinitionId())) {
      Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
      activiti5CompatibilityHandler.trigger(executionId, processVariables, transientVariables);
      return null;
    }
    
    if (processVariables != null) {
      execution.setVariables(processVariables);
    }
    
    if (transientVariables != null) {
      execution.setTransientVariables(transientVariables);
    }
    
    Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
        ActivitiEventBuilder.createSignalEvent(ActivitiEventType.ACTIVITY_SIGNALED, execution.getCurrentActivityId(), null, 
            null, execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId()));
    
    commandContext.getAgenda().planTriggerExecutionOperation(execution);
    return null;
  }

  @Override
  protected String getSuspendedExceptionMessage() {
    return "Cannot trigger an execution that is suspended";
  }

}

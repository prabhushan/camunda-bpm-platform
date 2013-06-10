package org.camunda.bpm.cockpit.plugin.base.resources;

import java.util.Iterator;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ActivityInstanceDto;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessInstanceDto;
import org.camunda.bpm.cockpit.plugin.base.query.parameter.ProcessInstanceQueryParameter;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;

public class ProcessInstanceResource extends AbstractPluginResource {

  public static final String PATH = "/process-instance";

  public ProcessInstanceResource(String engineName) {
    super(engineName);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessInstanceDto> getProcessInstances(@QueryParam("processDefinitionId") String processDefinitionId,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults) {

    ProcessInstanceQueryParameter param = null;

    if (firstResult == null && maxResults == null) {
      param = new ProcessInstanceQueryParameter();
    } else if (firstResult != null && maxResults != null) {
      param = new ProcessInstanceQueryParameter(firstResult, maxResults);
    } else {
      throw new RuntimeException("Either both query parameters 'firstResult' and 'maxResults' have to set or not.");
    }

    param.setProcessDefinitionId(processDefinitionId);

    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration();
    if (processEngineConfiguration.getHistoryLevel() == 0) {
      param.setHistoryEnabled(false);
    }

    return getQueryService().executeQuery("selectRunningProcessInstancesIncludingIncidents", param);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}/activity-instances")
  public List<ActivityInstanceDto> getActivityInstances(@PathParam("id") String processInstanceId) {
    List<Execution> executions = getProcessEngine()
        .getRuntimeService()
          .createExecutionQuery()
          .processInstanceId(processInstanceId)
          .list();

    // filter non active executions
    Iterator<Execution> iterator = executions.iterator();
    while (iterator.hasNext()) {
      ExecutionEntity e = (ExecutionEntity) iterator.next();

      boolean filtered = false;

      filtered |= !e.isActive();

      filtered |= e.getActivityId() == null;

      if (filtered) {
        iterator.remove();
      }
    }

    return ActivityInstanceDto.wrapAll(executions);
  }
}

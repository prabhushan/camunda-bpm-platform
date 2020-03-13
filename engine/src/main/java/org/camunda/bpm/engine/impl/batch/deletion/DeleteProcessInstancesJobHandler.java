/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.batch.deletion;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.cmd.batch.DeleteProcessInstanceBatchCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration.DeploymentMappingInfo;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstancesJobHandler extends AbstractBatchJobHandler<DeleteProcessInstanceBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_DELETION);

  @Override
  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_DELETION;
  }

  protected DeleteProcessInstanceBatchConfigurationJsonConverter getJsonConverterInstance() {
    return DeleteProcessInstanceBatchConfigurationJsonConverter.INSTANCE;
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected DeleteProcessInstanceBatchConfiguration createJobConfiguration(DeleteProcessInstanceBatchConfiguration configuration, List<String> processIdsForJob) {
    return new DeleteProcessInstanceBatchConfiguration(processIdsForJob, null, configuration.getDeleteReason(), configuration.isSkipCustomListeners(), configuration.isSkipSubprocesses(), configuration.isFailIfNotExists());
  }

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    DeleteProcessInstanceBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    boolean initialLegacyRestrictions = commandContext.isRestrictUserOperationLogToAuthenticatedUsers();
    commandContext.disableUserOperationLog();
    commandContext.setRestrictUserOperationLogToAuthenticatedUsers(true);
    try {
      RuntimeService runtimeService = commandContext.getProcessEngineConfiguration().getRuntimeService();
      if(batchConfiguration.isFailIfNotExists()) {
        runtimeService.deleteProcessInstances(batchConfiguration.getIds(), batchConfiguration.deleteReason, batchConfiguration.isSkipCustomListeners(), true, batchConfiguration.isSkipSubprocesses());
      } else {
        runtimeService.deleteProcessInstancesIfExists(batchConfiguration.getIds(), batchConfiguration.deleteReason, batchConfiguration.isSkipCustomListeners(), true, batchConfiguration.isSkipSubprocesses());
      }
    } finally {
      commandContext.enableUserOperationLog();
      commandContext.setRestrictUserOperationLogToAuthenticatedUsers(initialLegacyRestrictions);
    }

    commandContext.getByteArrayManager().delete(configurationEntity);
  }

  @Override
  protected boolean doCreateJobs(BatchEntity batch, DeleteProcessInstanceBatchConfiguration configuration) {
    List<DeploymentMappingInfo> idMappings = configuration.getIdMappings();
    if (idMappings == null || idMappings.isEmpty()) {
      // create mapping for legacy seed jobs
      List<String> ids = new ArrayList<>();
      idMappings = new ArrayList<>();
      DeleteProcessInstanceBatchCmd.createDeploymentMappings(Context.getCommandContext(), idMappings, configuration.getIds(), ids);
      configuration.setIds(ids);
      configuration.setIdMappings(idMappings);
    }
    return super.doCreateJobs(batch, configuration);
  }
}

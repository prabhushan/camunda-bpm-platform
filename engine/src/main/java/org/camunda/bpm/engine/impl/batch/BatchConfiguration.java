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
package org.camunda.bpm.engine.impl.batch;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


public class BatchConfiguration {

  protected List<String> ids;
  protected List<DeploymentMappingInfo> idMappings;
  protected boolean failIfNotExists;

  public BatchConfiguration(List<String> ids) {
    this(ids, true);
  }

  public BatchConfiguration(List<String> ids, boolean failIfNotExists) {
    this(ids, null, failIfNotExists);
  }

  public BatchConfiguration(List<String> ids, List<DeploymentMappingInfo> mappings) {
    this(ids, mappings, true);
  }

  public BatchConfiguration(List<String> ids, List<DeploymentMappingInfo> mappings, boolean failIfNotExists) {
    this.ids = ids;
    this.idMappings = mappings;
    this.failIfNotExists = failIfNotExists;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  public List<DeploymentMappingInfo> getIdMappings() {
    return idMappings;
  }

  public void setIdMappings(List<DeploymentMappingInfo> idMappings) {
    this.idMappings = idMappings;
  }

  public boolean isFailIfNotExists() {
    return failIfNotExists;
  }

  public void setFailIfNotExists(boolean failIfNotExists) {
    this.failIfNotExists = failIfNotExists;
  }

  public static class DeploymentMappingInfo {
    public static String NULL_ID = "$NULL";

    private String deploymentId;
    private int count;

    public DeploymentMappingInfo(String deploymentId, int count) {
      this.deploymentId = deploymentId == null ? NULL_ID : deploymentId;
      this.count = count;
    }

    public String getDeploymentId() {
      return NULL_ID.equals(deploymentId) ? null : deploymentId;
    }

    public int getCount() {
      return count;
    }

    public List<String> getIds(List<String> ids){
      return ids.subList(0, count);
    }

    public void removeIds(int numberOfIds) {
      count -= numberOfIds;
    }

    @Override
    public String toString() {
      return new StringJoiner(";")
          .add(deploymentId)
          .add(String.valueOf(count))
          .toString();
    }

    public static List<String> toStringList(List<DeploymentMappingInfo> infoList) {
      return infoList == null ? null : infoList.stream().map(DeploymentMappingInfo::toString).collect(Collectors.toList());
    }

    public static List<DeploymentMappingInfo> fromStringList(List<String> infoList) {
      return infoList.stream().map(DeploymentMappingInfo::fromString).collect(Collectors.toList());
    }

    public static DeploymentMappingInfo fromString(String info) {
      String[] parts = info.split(";");
      if (parts.length != 2) {
        throw new IllegalArgumentException("DeploymentMappingInfo must consist of two parts separated by semi-colons, but was: " + info);
      }
      return new DeploymentMappingInfo(parts[0], Integer.valueOf(parts[1]));
    }

    public static void addIds(Collection<String> idsToAdd, List<String> targetIdList, List<DeploymentMappingInfo> mappings, String deploymentId) {
      targetIdList.addAll(idsToAdd);
      mappings.add(new DeploymentMappingInfo(deploymentId, idsToAdd.size()));
    }
  }

}

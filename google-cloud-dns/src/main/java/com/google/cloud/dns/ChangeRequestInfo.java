/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.dns;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.dns.model.Change;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A class representing an atomic update to a collection of {@link RecordSet}s within a {@code
 * Zone}.
 *
 * @see <a href="https://cloud.google.com/dns/api/v1/changes">Google Cloud DNS documentation</a>
 */
public class ChangeRequestInfo implements Serializable {

  static final Function<Change, ChangeRequestInfo> FROM_PB_FUNCTION =
      new Function<Change, ChangeRequestInfo>() {
        @Override
        public ChangeRequestInfo apply(Change pb) {
          return ChangeRequestInfo.fromPb(pb);
        }
      };
  private static final long serialVersionUID = -6029143477639439169L;
  private final List<RecordSet> additions;
  private final List<RecordSet> deletions;
  private final String generatedId;
  private final Long startTimeMillis;
  private final ChangeRequestInfo.Status status;

  /**
   * This enumerates the possible states of a change request.
   *
   * @see <a href="https://cloud.google.com/dns/api/v1/changes#resource">Google Cloud DNS
   * documentation</a>
   */
  public enum Status {
    PENDING,
    DONE
  }

  /**
   * A builder for {@code ChangeRequestInfo}.
   */
  public abstract static class Builder {

    /**
     * Sets a collection of {@link RecordSet}s which are to be added to the zone upon executing this
     * {@code ChangeRequestInfo}.
     */
    public abstract Builder additions(List<RecordSet> additions);

    /**
     * Sets a collection of {@link RecordSet}s which are to be deleted from the zone upon executing
     * this {@code ChangeRequestInfo}.
     */
    public abstract Builder deletions(List<RecordSet> deletions);

    /**
     * Adds a {@link RecordSet} to be <strong>added</strong> to the zone upon executing this {@code
     * ChangeRequestInfo}.
     */
    public abstract Builder add(RecordSet recordSet);

    /**
     * Adds a {@link RecordSet} to be <strong>deleted</strong> to the zone upon executing this
     * {@code ChangeRequestInfo}.
     */
    public abstract Builder delete(RecordSet recordSet);

    /**
     * Clears the collection of {@link RecordSet}s which are to be added to the zone upon executing
     * this {@code ChangeRequestInfo}.
     */
    public abstract Builder clearAdditions();

    /**
     * Clears the collection of {@link RecordSet}s which are to be deleted from the zone upon
     * executing this {@code ChangeRequestInfo}.
     */
    public abstract Builder clearDeletions();

    /**
     * Removes a single {@link RecordSet} from the collection of records to be
     * <strong>added</strong> to the zone upon executing this {@code ChangeRequestInfo}.
     */
    public abstract Builder removeAddition(RecordSet recordSet);

    /**
     * Removes a single {@link RecordSet} from the collection of records to be
     * <strong>deleted</strong> from the zone upon executing this {@code ChangeRequestInfo}.
     */
    public abstract Builder removeDeletion(RecordSet recordSet);

    /**
     * Associates a service-generated id to this {@code ChangeRequestInfo}.
     */
    abstract Builder generatedId(String generatedId);

    /**
     * Sets the time when this change request was started by a server.
     */
    abstract Builder startTimeMillis(long startTimeMillis);

    /**
     * Sets the current status of this {@code ChangeRequest}.
     */
    abstract Builder status(ChangeRequest.Status status);

    /**
     * Creates a {@code ChangeRequestInfo} instance populated by the values associated with this
     * builder.
     */
    public abstract ChangeRequestInfo build();
  }

  static class BuilderImpl extends Builder {
    private List<RecordSet> additions;
    private List<RecordSet> deletions;
    private String generatedId;
    private Long startTimeMillis;
    private ChangeRequestInfo.Status status;

    BuilderImpl() {
      this.additions = new LinkedList<>();
      this.deletions = new LinkedList<>();
    }

    BuilderImpl(ChangeRequestInfo info) {
      this.additions = Lists.newLinkedList(info.additions());
      this.deletions = Lists.newLinkedList(info.deletions());
      this.generatedId = info.generatedId;
      this.startTimeMillis = info.startTimeMillis;
      this.status = info.status;
    }

    @Override
    public Builder additions(List<RecordSet> additions) {
      this.additions = Lists.newLinkedList(checkNotNull(additions));
      return this;
    }

    @Override
    public Builder deletions(List<RecordSet> deletions) {
      this.deletions = Lists.newLinkedList(checkNotNull(deletions));
      return this;
    }

    @Override
    public Builder add(RecordSet recordSet) {
      this.additions.add(checkNotNull(recordSet));
      return this;
    }

    @Override
    public Builder delete(RecordSet recordSet) {
      this.deletions.add(checkNotNull(recordSet));
      return this;
    }

    @Override
    public Builder clearAdditions() {
      this.additions.clear();
      return this;
    }

    @Override
    public Builder clearDeletions() {
      this.deletions.clear();
      return this;
    }

    @Override
    public Builder removeAddition(RecordSet recordSet) {
      this.additions.remove(recordSet);
      return this;
    }

    @Override
    public Builder removeDeletion(RecordSet recordSet) {
      this.deletions.remove(recordSet);
      return this;
    }

    @Override
    public ChangeRequestInfo build() {
      return new ChangeRequestInfo(this);
    }

    @Override
    Builder generatedId(String generatedId) {
      this.generatedId = checkNotNull(generatedId);
      return this;
    }

    @Override
    Builder startTimeMillis(long startTimeMillis) {
      this.startTimeMillis = startTimeMillis;
      return this;
    }

    @Override
    Builder status(ChangeRequestInfo.Status status) {
      this.status = checkNotNull(status);
      return this;
    }
  }

  ChangeRequestInfo(BuilderImpl builder) {
    this.additions = ImmutableList.copyOf(builder.additions);
    this.deletions = ImmutableList.copyOf(builder.deletions);
    this.generatedId = builder.generatedId;
    this.startTimeMillis = builder.startTimeMillis;
    this.status = builder.status;
  }

  /**
   * Returns an empty builder for the {@code ChangeRequestInfo} class.
   */
  public static Builder builder() {
    return new BuilderImpl();
  }

  /**
   * Creates a builder populated with values of this {@code ChangeRequestInfo}.
   */
  public Builder toBuilder() {
    return new BuilderImpl(this);
  }

  /**
   * Returns the list of {@link RecordSet}s to be added to the zone upon submitting this change
   * request.
   */
  public List<RecordSet> additions() {
    return additions;
  }

  /**
   * Returns the list of {@link RecordSet}s to be deleted from the zone upon submitting this change
   * request.
   */
  public List<RecordSet> deletions() {
    return deletions;
  }

  /**
   * Returns the service-generated id for this change request.
   */
  public String generatedId() {
    return generatedId;
  }

  /**
   * Returns the time when this change request was started by the server.
   */
  public Long startTimeMillis() {
    return startTimeMillis;
  }

  /**
   * Returns the status of this change request. If the change request has not been applied yet, the
   * status is {@code PENDING}.
   */
  public ChangeRequestInfo.Status status() {
    return status;
  }

  Change toPb() {
    Change pb = new Change();
    // set id
    if (generatedId() != null) {
      pb.setId(generatedId());
    }
    // set timestamp
    if (startTimeMillis() != null) {
      pb.setStartTime(ISODateTimeFormat.dateTime().withZoneUTC().print(startTimeMillis()));
    }
    // set status
    if (status() != null) {
      pb.setStatus(status().name().toLowerCase());
    }
    // set a list of additions
    pb.setAdditions(Lists.transform(additions(), RecordSet.TO_PB_FUNCTION));
    // set a list of deletions
    pb.setDeletions(Lists.transform(deletions(), RecordSet.TO_PB_FUNCTION));
    return pb;
  }

  static ChangeRequestInfo fromPb(Change pb) {
    Builder builder = builder();
    if (pb.getId() != null) {
      builder.generatedId(pb.getId());
    }
    if (pb.getStartTime() != null) {
      builder.startTimeMillis(DateTime.parse(pb.getStartTime()).getMillis());
    }
    if (pb.getStatus() != null) {
      // we are assuming that status indicated in pb is a lower case version of the enum name
      builder.status(ChangeRequest.Status.valueOf(pb.getStatus().toUpperCase()));
    }
    if (pb.getDeletions() != null) {
      builder.deletions(Lists.transform(pb.getDeletions(), RecordSet.FROM_PB_FUNCTION));
    }
    if (pb.getAdditions() != null) {
      builder.additions(Lists.transform(pb.getAdditions(), RecordSet.FROM_PB_FUNCTION));
    }
    return builder.build();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this
        || obj != null
        && obj.getClass().equals(ChangeRequestInfo.class)
        && toPb().equals(((ChangeRequestInfo) obj).toPb());
  }

  @Override
  public int hashCode() {
    return Objects.hash(additions, deletions, generatedId, startTimeMillis, status);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("additions", additions)
        .add("deletions", deletions)
        .add("generatedId", generatedId)
        .add("startTimeMillis", startTimeMillis)
        .add("status", status)
        .toString();
  }
}

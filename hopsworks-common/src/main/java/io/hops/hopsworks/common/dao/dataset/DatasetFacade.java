/*
 * Changes to this file committed after and not including commit-id: ccc0d2c5f9a5ac661e60e6eaf138de7889928b8b
 * are released under the following license:
 *
 * This file is part of Hopsworks
 * Copyright (C) 2018, Logical Clocks AB. All rights reserved
 *
 * Hopsworks is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Hopsworks is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 * Changes to this file committed before and including commit-id: ccc0d2c5f9a5ac661e60e6eaf138de7889928b8b
 * are released under the following license:
 *
 * Copyright (C) 2013 - 2018, Logical Clocks AB and RISE SICS AB. All rights reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS  OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.hops.hopsworks.common.dao.dataset;

import io.hops.hopsworks.common.dao.AbstractFacade;
import io.hops.hopsworks.persistence.entity.project.Project;
import io.hops.hopsworks.persistence.entity.dataset.Dataset;
import io.hops.hopsworks.persistence.entity.dataset.DatasetType;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Stateless
public class DatasetFacade extends AbstractFacade<Dataset> {

  @PersistenceContext(unitName = "kthfsPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public DatasetFacade() {
    super(Dataset.class);
  }

  /**
   * Finds a dataset by id
   * <p/>
   * @param id
   * @return
   */
  public Dataset find(Integer id) {
    return em.find(Dataset.class, id);
  }

  public Optional<Dataset> findByPublicDsIdProject(String publicDsId, Project project) {
    TypedQuery<Dataset> query = em.createNamedQuery("Dataset.findByPublicDsIdProject", Dataset.class)
      .setParameter("publicDsId", publicDsId)
      .setParameter("project", project);
    try {
      return Optional.of(query.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
  
  public Optional<Dataset> findByPublicDsId(String publicDsId) {
    TypedQuery<Dataset> query = em.createNamedQuery("Dataset.findByPublicDsId", Dataset.class)
      .setParameter("publicDsId", publicDsId);
    try {
      return Optional.of(query.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  /**
   * Find by project and dataset name
   * <p/>
   * @param project
   * @param name
   * @return
   */
  public Dataset findByProjectAndName(Project project, String name) {
    if(project == null || name == null){
      throw new IllegalArgumentException("Project and/or dataset name were not provided.");
    }
    try {
      return em.createNamedQuery("Dataset.findByProjectAndName", Dataset.class)
        .setParameter("project", project).setParameter("name", name).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Finds all data sets in a project.
   * <p/>
   * @param project
   * @return
   */
  public List<Dataset> findByProject(Project project) {
    TypedQuery<Dataset> query = em.createNamedQuery("Dataset.findByProject", Dataset.class)
      .setParameter("project", project);
    return query.getResultList();
  }

  public List<DataSetDTO> findPublicDatasets() {
    TypedQuery<Dataset> query = em.createNamedQuery("Dataset.findAllPublic", Dataset.class);
    List<Dataset> datasets = query.getResultList();

    List<DataSetDTO> ds = new ArrayList<>();
    for (Dataset d : datasets) {
      DataSetDTO dto = new DataSetDTO();
      dto.setDescription(d.getDescription());
      dto.setName(d.getName());
      ds.add(dto);
    }
    return ds;
  }
  
  public List<Dataset> findAllPublicDatasets() {
    TypedQuery<Dataset> query = em.createNamedQuery("Dataset.findAllPublic", Dataset.class);
    return query.getResultList();   
  }
  
  public List<Dataset> findPublicDatasetsByState(int publicDs) {
    TypedQuery<Dataset> query = em.createNamedQuery("Dataset.findPublicByState", Dataset.class)
      .setParameter("publicDs", publicDs);
    return query.getResultList();
  }

  public void persistDataset(Dataset dataset) {
    em.persist(dataset);
  }

  public void flushEm() {
    em.flush();
  }

  public void merge(Dataset dataset) {
    em.merge(dataset);
    em.flush();
  }

  public void removeDataset(Dataset dataset) {
    Dataset ds = em.find(Dataset.class, dataset.getId());
    if (ds != null) {
      em.remove(ds);
    }
  }
  
  public CollectionInfo findAllDatasetByProject(Integer offset, Integer limit, Set<? extends FilterBy> filter,
    Set<? extends SortBy> sort, Project project) {
    String queryStr = buildQuery("SELECT d FROM Dataset d ", filter, sort, "d.project = :project ");
    String queryCountStr = buildQuery("SELECT COUNT(DISTINCT d.id) FROM Dataset d ", filter, null,
      "d.project = :project ");
    Query query = em.createQuery(queryStr, Dataset.class).setParameter("project", project);
    Query queryCount = em.createQuery(queryCountStr, Dataset.class).setParameter("project", project);
    setFilter(filter, query);
    setFilter(filter, queryCount);
    setOffsetAndLim(offset, limit, query);
    return new CollectionInfo((Long) queryCount.getSingleResult(), query.getResultList());
  }
  
  private void setFilter(Set<? extends AbstractFacade.FilterBy> filter, Query q) {
    if (filter == null || filter.isEmpty()) {
      return;
    }
    for (FilterBy aFilter : filter) {
      setFilterQuery(aFilter, q);
    }
  }
  
  private void setFilterQuery(AbstractFacade.FilterBy filterBy, Query q) {
    switch (Filters.valueOf(filterBy.getValue())) {
      case NAME:
      case NAME_NEQ:
      case NAME_LIKE:
        q.setParameter(filterBy.getField(), filterBy.getParam());
        break;
      case ACCEPTED:
      case PUBLIC:
      case SHARED:
      case SEARCHABLE:
        q.setParameter(filterBy.getField(), getBooleanValue(filterBy.getParam()));
        break;
      case TYPE:
        q.setParameter(filterBy.getField(), getEnumValue(filterBy.getField(), filterBy.getValue(), DatasetType.class));
        break;
      default:
        break;
    }
  }
  
  public enum Sorts {
    ID("ID", "d.id ", "ASC"),
    NAME("NAME", "LOWER(d.name) ", "ASC"),
    SEARCHABLE("SEARCHABLE", "d.searchable ", "ASC"),
    PUBLIC("PUBLIC", "d.public ", "ASC"),
    TYPE("TYPE", "d.dsType ", "ASC");
    
    private final String value;
    private final String sql;
    private final String defaultParam;
    
    private Sorts(String value, String sql, String defaultParam) {
      this.value = value;
      this.sql = sql;
      this.defaultParam = defaultParam;
    }
    
    public String getValue() {
      return value;
    }
    
    public String getSql() {
      return sql;
    }
    
    public String getDefaultParam() {
      return defaultParam;
    }
    
    public String getJoin(){
      return null;
    }
    
    @Override
    public String toString() {
      return value;
    }
    
  }
  
  public enum Filters {
    NAME("NAME", "LOWER(d.name) LIKE LOWER(CONCAT(:name, '%')) ", "name", " "),
    NAME_NEQ("NAME_NEQ", "LOWER(d.name) NOT LIKE LOWER(CONCAT(:name_not_eq, '%')) ", "name_not_eq", " "),
    NAME_LIKE("NAME_LIKE", "LOWER(d.name) LIKE LOWER(CONCAT('%', :name_like, '%')) ", "name_like", " "),
    ACCEPTED("ACCEPTED", "true =:accepted ", "accepted", "true"),//return all if true
    SHARED("SHARED", "NOT :shared ", "shared", "true"),//return none if true
    SEARCHABLE("SEARCHABLE", "d.searchable =:searchable ", "searchable", "0"),
    TYPE("TYPE", "d.dsType =:dsType ", "dsType", "DATASET"),
    PUBLIC("PUBLIC", "d.public =:public ", "public", "0");
    
    private final String value;
    private final String sql;
    private final String field;
    private final String defaultParam;
    
    private Filters(String value, String sql, String field, String defaultParam) {
      this.value = value;
      this.sql = sql;
      this.field = field;
      this.defaultParam = defaultParam;
    }
    
    public String getDefaultParam() {
      return defaultParam;
    }
    
    public String getValue() {
      return value;
    }
    
    public String getSql() {
      return sql;
    }
    
    public String getField() {
      return field;
    }
    
    @Override
    public String toString() {
      return value;
    }
    
  }
}

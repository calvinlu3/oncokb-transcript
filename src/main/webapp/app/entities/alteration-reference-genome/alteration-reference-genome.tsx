import React, { useState, useEffect } from 'react';
import { connect } from 'app/shared/util/typed-inject';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IAlterationReferenceGenome } from 'app/shared/model/alteration-reference-genome.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

import { IRootStore } from 'app/stores';
export interface IAlterationReferenceGenomeProps extends StoreProps, RouteComponentProps<{ url: string }> {}

export const AlterationReferenceGenome = (props: IAlterationReferenceGenomeProps) => {
  const alterationReferenceGenomeList = props.alterationReferenceGenomeList;
  const loading = props.loading;

  useEffect(() => {
    props.getEntities({});
  }, []);

  const handleSyncList = () => {
    props.getEntities({});
  };

  const { match } = props;

  return (
    <div>
      <h2 id="alteration-reference-genome-heading" data-cy="AlterationReferenceGenomeHeading">
        Alteration Reference Genomes
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh List
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create new Alteration Reference Genome
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {alterationReferenceGenomeList && alterationReferenceGenomeList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>Reference Genome</th>
                <th>Alteration</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {alterationReferenceGenomeList.map((alterationReferenceGenome, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${alterationReferenceGenome.id}`} color="link" size="sm">
                      {alterationReferenceGenome.id}
                    </Button>
                  </td>
                  <td>{alterationReferenceGenome.referenceGenome}</td>
                  <td>
                    {alterationReferenceGenome.alteration ? (
                      <Link to={`alteration/${alterationReferenceGenome.alteration.id}`}>{alterationReferenceGenome.alteration.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`${match.url}/${alterationReferenceGenome.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`${match.url}/${alterationReferenceGenome.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`${match.url}/${alterationReferenceGenome.id}/delete`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Alteration Reference Genomes found</div>
        )}
      </div>
    </div>
  );
};

const mapStoreToProps = ({ alterationReferenceGenomeStore }: IRootStore) => ({
  alterationReferenceGenomeList: alterationReferenceGenomeStore.entities,
  loading: alterationReferenceGenomeStore.loading,
  getEntities: alterationReferenceGenomeStore.getEntities,
});

type StoreProps = ReturnType<typeof mapStoreToProps>;

export default connect(mapStoreToProps)(AlterationReferenceGenome);

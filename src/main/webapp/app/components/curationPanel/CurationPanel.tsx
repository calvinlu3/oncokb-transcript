import { connect } from 'app/shared/util/typed-inject';
import { IRootStore } from 'app/stores/createStore';
import React, { useEffect } from 'react';
import { Sidebar } from 'react-pro-sidebar';
import { matchPath, useLocation } from 'react-router-dom';
import './curation-panel.scss';
import FdaSubmissionPanel from './FdaSubmissionPanel';

// Paths that should show the curation panel
const includedPaths = ['/fda-submission/:id/curate'];

const CurationPanel: React.FunctionComponent<StoreProps> = props => {
  const location = useLocation();
  const matchedPath = includedPaths.filter(path => matchPath(location.pathname, { path, exact: true }))[0];

  useEffect(() => {
    const showOnMatchedPath = !!matchedPath;
    props.toggleCurationPanel(showOnMatchedPath);
  }, [location.pathname]);

  return (
    <div className="curation-sidebar-wrapper">
      <Sidebar width={`${props.curationPanelWidth}px`}>
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
          <FdaSubmissionPanel />
        </div>
      </Sidebar>
    </div>
  );
};

const mapStoreToProps = ({ layoutStore }: IRootStore) => ({
  toggleCurationPanel: layoutStore.toggleCurationPanel,
  curationPanelWidth: layoutStore.curationPanelWidth,
});

type StoreProps = ReturnType<typeof mapStoreToProps>;

export default connect(mapStoreToProps)(CurationPanel);

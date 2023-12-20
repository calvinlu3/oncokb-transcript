import React from 'react';
import './oncokb-sidebar.scss';
import { observer } from 'mobx-react';
import { IRootStore } from 'app/stores';
import { componentInject } from 'app/shared/util/typed-inject';
import { FaChevronRight, FaChevronLeft } from 'react-icons/fa';

export interface IOncoKBSidebarProps extends StoreProps {
  children: React.ReactNode;
}

const OncoKBSidebar = ({ children, showOncoKBSidebar, toggleOncoKBSidebar, oncoKBSidebarWidth }: IOncoKBSidebarProps) => {
  return showOncoKBSidebar ? (
    <div style={{ width: oncoKBSidebarWidth }} className="generic-sidebar-expanded">
      <div style={{ marginTop: '2rem', marginLeft: '1rem', display: 'flex' }}>
        <FaChevronRight cursor={'pointer'} style={{ marginTop: '6px' }} onClick={() => toggleOncoKBSidebar(false)} />
        {children}
      </div>
    </div>
  ) : (
    <div className="generic-sidebar-collapsed">
      <FaChevronLeft
        cursor={'pointer'}
        style={{ position: 'absolute', right: '38', top: '38' }}
        onClick={() => toggleOncoKBSidebar(true)}
      />
    </div>
  );
};

const mapStoreToProps = ({ layoutStore }: IRootStore) => ({
  showOncoKBSidebar: layoutStore.showOncoKBSidebar,
  toggleOncoKBSidebar: layoutStore.toggleOncoKBSidebar,
  oncoKBSidebarWidth: layoutStore.oncoKBSidebarWidth,
});

type StoreProps = Partial<ReturnType<typeof mapStoreToProps>>;

export default componentInject(mapStoreToProps)(observer(OncoKBSidebar));
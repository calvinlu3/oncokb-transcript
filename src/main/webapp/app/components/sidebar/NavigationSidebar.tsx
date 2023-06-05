import React from 'react';
import './navigation-sidebar.scss';
import { observer } from 'mobx-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AUTHORITIES, PAGE_ROUTE } from 'app/config/constants';
import { IRootStore } from 'app/stores/createStore';
import { componentInject } from 'app/shared/util/typed-inject';
import { NavLink } from 'react-router-dom';
import { Sidebar, Menu, MenuItem, SubMenu } from 'react-pro-sidebar';
import {
  faBars,
  faBuilding,
  faSearch,
  faPills,
  faFileAlt,
  faLongArrowAltLeft,
  faUserShield,
  faUsers,
  faRedo,
  faWrench,
  faSignOutAlt,
} from '@fortawesome/free-solid-svg-icons';
import { SidebarHeader } from './SidebarHeader';
import { hasAnyAuthority } from 'app/stores';

const AccountMenuItemsAuthenticated: React.FunctionComponent<{
  isAdmin: boolean;
}> = props => (
  <>
    {props.isAdmin ? (
      <>
        <MenuItem component={<NavLink to={PAGE_ROUTE.ADMIN_USER_MANAGEMENT} />}>User management</MenuItem>
        <MenuItem component={<NavLink to={'/admin/cache-management'} />}>Cache management</MenuItem>
      </>
    ) : null}
  </>
);

export const NavigationSidebar = (props: StoreProps) => {
  return (
    <Sidebar
      collapsed={props.isSideBarCollapsed}
      width={'240px'}
      breakPoint={'md'}
      style={{ position: 'fixed' }}
      rootStyles={{ backgroundColor: '#F3F3F3' }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
        {/* <SidebarHeader
          style={{ marginBottom: '24px', marginTop: '16px' }}
          rtl={false}
          toggleSidebar={props.toggleSideBar}
          isCollapsed={props.isSideBarCollapsed}
        /> */}
        {/* <Menu>
          <MenuItem icon={<FontAwesomeIcon size="lg" icon={faBars} />} onClick={props.toggleSideBar} />
        </Menu> */}
        <div style={{ flex: 1, marginBottom: '32px' }}>
          <div className="ps-sidebar-custom-header" style={{ padding: '0 24px', marginBottom: '8px' }}>
            <span style={{ fontWeight: 600, letterSpacing: '0.5px', opacity: 0.7 }}>General</span>
          </div>
          <Menu>
            <MenuItem icon={<FontAwesomeIcon size="lg" icon={faSearch} />} component={<NavLink to={PAGE_ROUTE.SEARCH} />}>
              Search
            </MenuItem>
            <MenuItem icon={<b style={{ fontSize: '1.5em' }}>G</b>} component={<NavLink to={PAGE_ROUTE.GENE} />}>
              Gene
            </MenuItem>
            <MenuItem icon={<b style={{ fontSize: '1.5em' }}>A</b>} component={<NavLink to={PAGE_ROUTE.ALTERATION} />}>
              Alteration
            </MenuItem>
            <MenuItem icon={<FontAwesomeIcon size="lg" icon={faFileAlt} />} component={<NavLink to={PAGE_ROUTE.ARTICLE} />}>
              Article
            </MenuItem>
            <MenuItem icon={<FontAwesomeIcon size="lg" icon={faPills} />} component={<NavLink to={PAGE_ROUTE.DRUG} />}>
              Drug
            </MenuItem>
            <MenuItem icon={<FontAwesomeIcon size="lg" icon={faBuilding} />} component={<NavLink to={PAGE_ROUTE.CDX} />}>
              CDx
            </MenuItem>
            <MenuItem icon={<b style={{ fontSize: '1.2em' }}>FDA</b>} component={<NavLink to={PAGE_ROUTE.FDA_SUBMISSION} />}>
              Submission
            </MenuItem>
            <MenuItem icon={<b style={{ fontSize: '1.2em' }}>CT</b>} component={<NavLink to={PAGE_ROUTE.CT_GOV_CONDITION} />}>
              Condition
            </MenuItem>
          </Menu>
          <div className="ps-sidebar-custom-header" style={{ padding: '0 24px', marginBottom: '8px', marginTop: '32px' }}>
            <span style={{ fontWeight: 600, letterSpacing: '0.5px', opacity: 0.7 }}>Account</span>
          </div>
          <Menu>
            <SubMenu label="Admin" icon={<FontAwesomeIcon size="lg" icon={faUserShield} />}>
              <AccountMenuItemsAuthenticated isAdmin={props.isAdmin} />
            </SubMenu>
            <MenuItem icon={<FontAwesomeIcon size="lg" icon={faWrench} />} component={<NavLink to={'/account/settings'} />}>
              Settings
            </MenuItem>
            <MenuItem icon={<FontAwesomeIcon size="lg" icon={faSignOutAlt} />} component={<NavLink to={'/logout'} />}>
              Sign out
            </MenuItem>
          </Menu>
        </div>
      </div>
    </Sidebar>
  );
};

const mapStoreToProps = ({ layoutStore, authStore }: IRootStore) => ({
  isSideBarCollapsed: layoutStore.isSideBarCollapsed,
  toggleSideBar: layoutStore.toggleSideBar,
  isAuthenticated: authStore.isAuthenticated,
  isAdmin: hasAnyAuthority(authStore.account.authorities, [AUTHORITIES.ADMIN]),
});

type StoreProps = {
  isSideBarCollapsed?: boolean;
  toggleSideBar?: () => void;
  isAuthenticated?: boolean;
  isAdmin?: boolean;
};

export default componentInject(mapStoreToProps)(observer(NavigationSidebar));

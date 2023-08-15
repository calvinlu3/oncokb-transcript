import React from 'react';
import './navigation-sidebar.scss';
import { observer } from 'mobx-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AUTHORITIES, PAGE_ROUTE } from 'app/config/constants';
import { IRootStore } from 'app/stores/createStore';
import { componentInject } from 'app/shared/util/typed-inject';
import { NavLink } from 'react-router-dom';
import { Sidebar, Menu, MenuItem } from 'react-pro-sidebar';
import { faBuilding, faSearch, faPills, faFileAlt, faAngleLeft, faAngleDoubleLeft } from '@fortawesome/free-solid-svg-icons';
import { hasAnyAuthority } from 'app/stores';
import DefaultTooltip from 'app/shared/tooltip/DefaultTooltip';
import { Button } from 'reactstrap';

type MenuItemCollapsibleProps = {
  isCollapsed: boolean;
  text: string;
  icon: JSX.Element;
  nav: JSX.Element;
};

const MenuItemCollapsible: React.FunctionComponent<MenuItemCollapsibleProps> = props => {
  const menuItem = (
    <MenuItem icon={props.icon} component={props.nav}>
      {props.text}
    </MenuItem>
  );
  if (props.isCollapsed) {
    return (
      <DefaultTooltip overlay={props.text} placement="right" mouseEnterDelay={0}>
        {menuItem}
      </DefaultTooltip>
    );
  }
  return menuItem;
};

export const NavigationSidebar: React.FunctionComponent<StoreProps> = props => {
  return (
    <Sidebar collapsed={props.isNavSidebarCollapsed} width={`${props.navigationSidebarWidth}px`}>
      <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
        <div style={{ flex: 1, marginBottom: '32px' }}>
          <Menu>
            <div>
              <FontAwesomeIcon icon={faAngleDoubleLeft} />
            </div>
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'Search'}
              icon={<FontAwesomeIcon size="lg" icon={faSearch} />}
              nav={<NavLink to={PAGE_ROUTE.SEARCH} />}
            />
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'Gene'}
              icon={<b style={{ fontSize: '1.5em' }}>G</b>}
              nav={<NavLink to={PAGE_ROUTE.GENE} />}
            />
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'Article'}
              icon={<FontAwesomeIcon size="lg" icon={faFileAlt} />}
              nav={<NavLink to={PAGE_ROUTE.ARTICLE} />}
            />
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'Drug'}
              icon={<FontAwesomeIcon size="lg" icon={faPills} />}
              nav={<NavLink to={PAGE_ROUTE.DRUG} />}
            />
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'CDx'}
              icon={<FontAwesomeIcon size="lg" icon={faBuilding} />}
              nav={<NavLink to={PAGE_ROUTE.CDX} />}
            />
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'Submission'}
              icon={<b style={{ fontSize: '1.2em' }}>FDA</b>}
              nav={<NavLink to={PAGE_ROUTE.FDA_SUBMISSION} />}
            />
            <MenuItemCollapsible
              isCollapsed={props.isNavSidebarCollapsed}
              text={'Condition'}
              icon={<b style={{ fontSize: '1.2em' }}>CT</b>}
              nav={<NavLink to={PAGE_ROUTE.CT_GOV_CONDITION} />}
            />
          </Menu>
        </div>
      </div>
    </Sidebar>
  );
};

const mapStoreToProps = ({ layoutStore, authStore }: IRootStore) => ({
  isNavSidebarCollapsed: layoutStore.isNavigationSidebarCollapsed,
  navigationSidebarWidth: layoutStore.navigationSidebarWidth,
  isAuthenticated: authStore.isAuthenticated,
  isAdmin: hasAnyAuthority(authStore.account.authorities, [AUTHORITIES.ADMIN]),
});

type StoreProps = {
  isNavSidebarCollapsed?: boolean;
  navigationSidebarWidth?: number;
  isAuthenticated?: boolean;
  isAdmin?: boolean;
};

export default componentInject(mapStoreToProps)(observer(NavigationSidebar));

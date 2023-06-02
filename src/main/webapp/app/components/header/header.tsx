import './header.scss';

import React from 'react';
import { Navbar, Nav, NavbarToggler, Collapse, NavbarBrand, Container } from 'reactstrap';
import oncokbLogo from 'oncokb-styles/dist/images/logo/oncokb-white.svg';
import { AccountMenu } from '../menus';
import { NavLink } from 'react-router-dom';
import { PAGE_ROUTE } from 'app/config/constants';
import OptimizedImage from 'app/oncokb-commons/components/image/OptimizedImage';
import { action, makeObservable, observable } from 'mobx';
import OncoKBBreadcrumb from 'app/shared/breadcrumb/OncoKBBreadcrumb';
import { GeneralSearch } from 'app/shared/search/GeneralSearch';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
}

class Header extends React.Component<IHeaderProps> {
  isNavMenuExpanded = false;

  constructor(props: IHeaderProps) {
    super(props);
    makeObservable(this, {
      isNavMenuExpanded: observable,
      toggleNavMenu: action.bound,
    });
  }

  toggleNavMenu() {
    this.isNavMenuExpanded = !this.isNavMenuExpanded;
  }

  render() {
    return (
      <header className="sticky-top header">
        <Navbar dark expand="lg" className="navbar">
          <Container fluid>
            <NavbarBrand as={NavLink} to={PAGE_ROUTE.HOME}>
              <OptimizedImage height={25} src={oncokbLogo} alt={'OncoKB'} />
            </NavbarBrand>
            <GeneralSearch />
          </Container>
        </Navbar>
        {/* <OncoKBBreadcrumb /> */}
      </header>
    );
  }
}

export default Header;

import './header.scss';

import React from 'react';
import { Navbar, Nav, NavbarBrand, Container } from 'reactstrap';
import oncokbLogo from 'oncokb-styles/dist/images/logo/oncokb-white.svg';
import { NavLink } from 'react-router-dom';
import { PAGE_ROUTE } from 'app/config/constants';
import OptimizedImage from 'app/oncokb-commons/components/image/OptimizedImage';
import { action, makeObservable, observable } from 'mobx';
import { faBars } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AccountMenu } from '../menus';
import { IUser } from 'app/shared/model/user.model';

export interface IHeaderProps {
  toggleNavigationSidebar: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
  account: IUser;
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
            <div className="d-flex flex-row align-items-center">
              <div className="header-navbar-toggle ml-3 mr-3" onClick={() => this.props.toggleNavigationSidebar()}>
                <FontAwesomeIcon size="lg" icon={faBars} color="white" />
              </div>
              <NavbarBrand as={NavLink} to={PAGE_ROUTE.HOME}>
                <OptimizedImage height={25} src={oncokbLogo} alt={'OncoKB'} />
              </NavbarBrand>
            </div>
            <Nav className="ml-auto mr-4" navbar>
              <AccountMenu isAuthenticated={this.props.isAuthenticated} isAdmin={this.props.isAdmin} account={this.props.account} />
            </Nav>
          </Container>
        </Navbar>
      </header>
    );
  }
}

export default Header;

import React from 'react';
import './account.scss';
import MenuItem from 'app/components/menus/menu-item';
import { DropdownMenu, DropdownToggle, UncontrolledDropdown } from 'reactstrap';
import OptimizedImage from 'app/oncokb-commons/components/image/OptimizedImage';
import { IUser } from 'app/shared/model/user.model';

const AccountMenuItemsAuthenticated: React.FunctionComponent<{
  isAdmin: boolean;
}> = props => (
  <>
    {props.isAdmin ? (
      <>
        <MenuItem icon="users" to="/admin/user-management">
          User management
        </MenuItem>
      </>
    ) : null}
    <MenuItem icon="wrench" to="/account/settings">
      Settings
    </MenuItem>
    <MenuItem icon="sign-out-alt" to="/logout">
      Sign out
    </MenuItem>
  </>
);

const AccountMenuItems: React.FunctionComponent = props => (
  <>
    <MenuItem id="login-item" icon="sign-in-alt" to="/login">
      Sign in
    </MenuItem>
  </>
);

export const AccountMenu: React.FunctionComponent<{
  isAuthenticated: boolean;
  isAdmin: boolean;
  account: IUser;
}> = props =>
  props.isAuthenticated && (
    <UncontrolledDropdown nav inNavbar id="account-menu">
      <DropdownToggle nav caret className="d-flex align-items-center">
        <div className="mr-2">
          {props.account.imageUrl ? (
            <OptimizedImage src={props.account.imageUrl} alt={'User'} className="account-menu-image" />
          ) : (
            <div className="account-menu-profile-circle">
              <p className="account-menu-profile-text">{props.account.firstName[0]}</p>
            </div>
          )}
        </div>
        <div className="account-menu-container d-flex flex-column">
          <div className="account-menu-name">
            {props.account.firstName} {props.account.lastName}
          </div>
          <div className="account-menu-email">{props.account.email}</div>
        </div>
      </DropdownToggle>
      <DropdownMenu right>
        <AccountMenuItemsAuthenticated isAdmin={props.isAdmin} />
      </DropdownMenu>
    </UncontrolledDropdown>
  );

export default AccountMenu;

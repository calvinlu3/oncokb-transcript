import styled from '@emotion/styled';
import OptimizedImage from 'app/oncokb-commons/components/image/OptimizedImage';
import React from 'react';
import oncokbLogo from 'oncokb-styles/dist/images/logo/oncokb.svg';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBars } from '@fortawesome/free-solid-svg-icons';

interface SidebarHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: React.ReactNode;
}

const StyledSidebarHeader = styled.div`
  height: 64px;
  min-height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;

  > div {
    width: 100%;
    overflow: hidden;
  }
`;

export const SidebarHeader: React.FC<SidebarHeaderProps> = ({ children, ...rest }) => {
  return (
    <StyledSidebarHeader {...rest} style={{ marginBottom: '16px', marginTop: '20px' }}>
      <div>
        <div className="curation-sidebar-header">Curation Panel</div>
      </div>
    </StyledSidebarHeader>
  );
};

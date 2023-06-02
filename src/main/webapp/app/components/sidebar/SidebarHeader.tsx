import styled from '@emotion/styled';
import OptimizedImage from 'app/oncokb-commons/components/image/OptimizedImage';
import React from 'react';
import oncokbLogo from 'oncokb-styles/dist/images/logo/oncokb.svg';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBars } from '@fortawesome/free-solid-svg-icons';

interface SidebarHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: React.ReactNode;
  rtl: boolean;
  toggleSidebar: () => void;
  isCollapsed: boolean;
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

export const SidebarHeader: React.FC<SidebarHeaderProps> = ({ children, rtl, toggleSidebar, isCollapsed, ...rest }) => {
  return (
    <StyledSidebarHeader {...rest}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: isCollapsed ? 'center' : 'space-between' }}>
        {/* {!isCollapsed ? <OptimizedImage height={25} src={oncokbLogo} alt={'OncoKB'} /> : undefined} */}
        <div onClick={toggleSidebar} style={{ cursor: 'pointer' }}>
          <FontAwesomeIcon size="lg" icon={faBars} />
        </div>
      </div>
    </StyledSidebarHeader>
  );
};

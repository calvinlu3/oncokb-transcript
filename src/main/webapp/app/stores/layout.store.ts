import { action, observable, makeObservable } from 'mobx';

const SIDEBAR_COLLAPSED_WIDTH = 80;
const SIDEBAR_EXPANDED_WIDTH = 200;

export class LayoutStore {
  public isNavigationSidebarCollapsed = false;
  public navigationSidebarWidth = SIDEBAR_EXPANDED_WIDTH;
  public showCurationPanel = false;
  public curationPanelWidth = 350;

  constructor() {
    makeObservable(this, {
      isNavigationSidebarCollapsed: observable,
      toggleNavigationSidebar: action.bound,
      navigationSidebarWidth: observable,
      showCurationPanel: observable,
      toggleCurationPanel: action.bound,
      curationPanelWidth: observable,
    });
  }

  toggleNavigationSidebar() {
    this.isNavigationSidebarCollapsed = !this.isNavigationSidebarCollapsed;
    this.navigationSidebarWidth = this.isNavigationSidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH;
  }

  toggleCurationPanel(value: boolean) {
    this.showCurationPanel = value;
  }
}

export default LayoutStore;

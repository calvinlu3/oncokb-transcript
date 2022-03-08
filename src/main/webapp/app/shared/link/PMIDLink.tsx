import { inject, observer } from 'mobx-react';
import React from 'react';

type PMIDLinkProps = {
  pmid: string;
};

class PMIDLink extends React.Component<PMIDLinkProps> {
  render() {
    return (
      <span>
        <a href={`https://pubmed.ncbi.nlm.nih.gov/?term=${this.props.pmid}`} target="_blank" rel="noopener noreferrer">
          PMID: {this.props.pmid}
        </a>
      </span>
    );
  }
}

export default PMIDLink;

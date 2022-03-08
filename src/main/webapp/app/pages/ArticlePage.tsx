import { inject, observer } from 'mobx-react';
import React from 'react';
import { RouteComponentProps } from 'react-router';
import { Article, ArticleResourceApi } from '../shared/api/generated/api';
import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { action, computed, makeObservable, observable } from 'mobx';
import { remoteData } from 'cbioportal-frontend-commons';
import { notifyError } from 'app/oncokb-commons/components/util/NotificationUtils';
import { Else, If, Then } from 'react-if';
import LoadingIndicator from '../oncokb-commons/components/loadingIndicator/LoadingIndicator';
import PMIDLink from 'app/shared/link/PMIDLink';

interface MatchParams {
  pmid: string;
}

class ArticlePage extends React.Component<RouteComponentProps<MatchParams>> {
  readonly article = remoteData<Article>({
    invoke: async () => {
      const pmid = this.props.match.params.pmid;
      const axiosInstance: AxiosInstance = axios.create({ withCredentials: true });
      const articleResourceApi = new ArticleResourceApi(null, '', axiosInstance);
      return (await articleResourceApi.getArticleUsingGET(pmid)).data;
    },
    onError: (error: Error) => notifyError(error, 'Error fetching article information'),
    default: undefined,
  });

  constructor(props: RouteComponentProps<MatchParams>) {
    super(props);
  }

  render() {
    return (
      <If condition={this.article.isPending}>
        <Then>
          <LoadingIndicator isLoading={true} />
        </Then>
        <Else>
          {this.article.isComplete && (
            <div>
              <PMIDLink pmid={this.article.result.pmid} />
              <h3>{this.article.result.title}</h3>
              <div>
                {this.article.result.authors}&emsp;
                {this.article.result.journal}&emsp;
                {this.article.result.pubDate}
              </div>
            </div>
          )}
        </Else>
      </If>
    );
  }
}

export default inject('routerStore')(observer(ArticlePage));

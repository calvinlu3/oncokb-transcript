import { action, observable, makeObservable } from 'mobx';
import axios, { AxiosResponse } from 'axios';
import { IUser } from 'app/shared/model/user.model';
import BaseStore from 'app/shared/util/base-store';
import { IRootStore } from 'app/shared/stores';

export class ActivateStore extends BaseStore {
  public activationSuccess = false;
  public activationFailure = false;

  activateAction = this.readHandler(this.activateActionGen);

  constructor(protected rootStore: IRootStore) {
    super(rootStore);

    makeObservable(this, {
      activationSuccess: observable,
      activationFailure: observable,
      activateAction: action,
      reset: action.bound,
    });
  }

  reset() {
    this.activationSuccess = false;
    this.activationFailure = false;
  }

  *activateActionGen(key) {
    try {
      const result: AxiosResponse = yield axios.get('/api/activate?key=' + key);
      this.activationSuccess = true;
      return result;
    } catch (e) {
      this.activationFailure = true;
      throw e;
    }
  }
}

export default ActivateStore;

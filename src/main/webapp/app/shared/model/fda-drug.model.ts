import { IDrug } from 'app/shared/model/drug.model';

export interface IFdaDrug {
  id?: number;
  applicationNumber?: string;
  brandName?: string | null;
  genericName?: string | null;
  drug?: IDrug | null;
}

export const defaultValue: Readonly<IFdaDrug> = {};

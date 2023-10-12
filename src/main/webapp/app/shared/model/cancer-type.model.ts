import { IBiomarkerAssociation } from 'app/shared/model/biomarker-association.model';
import { IClinicalTrialsGovCondition } from 'app/shared/model/clinical-trials-gov-condition.model';
import { TumorForm } from 'app/shared/model/enumerations/tumor-form.model';

export interface ICancerType {
  id?: number;
  code?: string | null;
  color?: string | null;
  level?: number;
  mainType?: string;
  subtype?: string | null;
  tissue?: string | null;
  tumorForm?: TumorForm;
  children?: ICancerType[] | null;
  biomarkerAssociations?: IBiomarkerAssociation[] | null;
  parent?: ICancerType | null;
  clinicalTrialsGovConditions?: IClinicalTrialsGovCondition[] | null;
}

export const defaultValue: Readonly<ICancerType> = {};

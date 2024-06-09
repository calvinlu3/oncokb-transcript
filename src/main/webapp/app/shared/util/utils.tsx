import React from 'react';
import { ICancerType } from 'app/shared/model/cancer-type.model';
import { IAlteration } from '../model/alteration.model';
import { v4 as uuidv4 } from 'uuid';
import { IGene } from 'app/shared/model/gene.model';
import { IEnsemblGene } from 'app/shared/model/ensembl-gene.model';
import { ENTITY_ACTION, ENTITY_TYPE, REFERENCE_IDENTIFIERS } from 'app/config/constants/constants';
import EntityActionButton from '../button/EntityActionButton';
import { SORT } from './pagination.constants';
import { PaginationState } from '../table/OncoKBAsyncTable';
import { IUser } from '../model/user.model';
import { CancerType } from '../model/firebase/firebase.model';
import _ from 'lodash';
import { ParsedRef, parseReferences } from 'app/oncokb-commons/components/RefComponent';
import { IDrug } from 'app/shared/model/drug.model';
import { IRule } from 'app/shared/model/rule.model';
import { INTEGER_REGEX, SINGLE_NUCLEOTIDE_POS_REGEX, UUID_REGEX } from 'app/config/constants/regex';
import { ProteinExonDTO } from 'app/shared/api/generated/curation';
import { IQueryParams } from './jhipster-types';

export const getCancerTypeName = (cancerType: ICancerType | CancerType, omitCode = false): string => {
  if (!cancerType) return '';
  if (!cancerType.subtype) return cancerType.mainType;

  let name = cancerType.subtype;
  if (!omitCode) name += ` (${cancerType.code})`;
  return name;
};

export const getCancerTypesName = (cancerTypes: ICancerType[] | CancerType[], omitCode = false, seperator = ', '): string => {
  return cancerTypes.map(cancerType => getCancerTypeName(cancerType, omitCode)).join(seperator);
};

export const getCancerTypesNameWithExclusion = (
  cancerTypes: ICancerType[] | CancerType[],
  excludedCancerTypes: ICancerType[] | CancerType[],
  omitCode = false,
): string => {
  let name = getCancerTypesName(cancerTypes, omitCode);
  if (excludedCancerTypes.length > 0) {
    name += ` {excluding ${getCancerTypesName(excludedCancerTypes, omitCode)}}`;
  }
  return name;
};

export const getGeneName = (gene: IGene): string => {
  return `${gene.entrezGeneId}: ${gene.hugoSymbol}`;
};

export const getGeneNameFromAlteration = (alteration: IAlteration) => {
  return alteration.genes.map(gene => gene.hugoSymbol).join('::');
};

export const getGeneNamesFromAlterations = (alterations: IAlteration[]) => {
  return _.uniq(alterations.map(alteration => getGeneNameFromAlteration(alteration)));
};

export const getGeneNamesStringFromAlterations = (alterations: IAlteration[]) => {
  return getGeneNamesFromAlterations(alterations).join(', ');
};

export const getTreatmentName = (drugs: IDrug[], rule?: IRule): string => {
  if (rule == null) {
    return drugs.map(drug => drug.name).join(', ');
  } else {
    const drugMap = drugs.reduce((map, next) => {
      map[next.id.toString()] = next;
      return map;
    }, {});
    return rule.rule
      .split(',')
      .map(treatment => {
        return treatment
          .split('+')
          .map(drugId => drugMap[drugId.trim()]?.name)
          .join(' + ');
      })
      .join(', ');
  }
};

export const getAlterationName = (alterations: IAlteration[]): string => {
  return alterations
    .map(alteration => alteration.name)
    .sort()
    .join(', ');
};

export const generateUuid = () => {
  return uuidv4();
};

export function getSectionClassName(theFirst = false) {
  return `${theFirst ? 'pb-3' : 'border-top py-3'}`;
}

export function filterByKeyword(value: string | undefined | null, keyword: string, splitKeywords = false): boolean {
  let keywords = [keyword];
  if (splitKeywords) {
    keywords = keyword.split(' ');
  }
  return keywords.filter(k => value?.toLowerCase().includes(k.trim())).length === keywords.length;
}

export const getGenomicLocation = (ensemblGene: IEnsemblGene) => {
  let chromosome = '';
  if (ensemblGene.seqRegion.chromosome !== ensemblGene.seqRegion.name) {
    chromosome = `(${ensemblGene.seqRegion.chromosome})`;
  }
  return `Chromosome ${ensemblGene.seqRegion.name}${chromosome}: ${ensemblGene.start}-${ensemblGene.end} ${
    ensemblGene.strand === 1 ? 'forward' : 'reverse'
  } strand`;
};

export const getPaginationFromSearchParams = (search: string) => {
  const params = new URLSearchParams(search);
  const page = params.get('page');
  let sort = params.get(SORT);
  let order = undefined;
  if (sort) {
    const sortSplit = sort.split(',');
    sort = sortSplit[0];
    order = sortSplit[1];
  }
  if (page && sort && order) {
    return {
      activePage: parseInt(page, 10),
      sort,
      order,
    } as PaginationState<any>;
  }
  return undefined;
};

export const getEntityTableActionsColumn = (entityType: ENTITY_TYPE) => {
  const actionsColumn = {
    id: 'actions',
    Header: 'Actions',
    Cell(cell: { original }) {
      const entityId = entityType === ENTITY_TYPE.USER ? cell.original.login : cell.original.id;
      return (
        <div>
          <EntityActionButton
            color="info"
            size="sm"
            entityId={entityId}
            entityType={entityType}
            entityAction={ENTITY_ACTION.VIEW}
            showText={false}
          />
          <EntityActionButton
            color="primary"
            size="sm"
            entityId={entityId}
            entityType={entityType}
            entityAction={ENTITY_ACTION.EDIT}
            showText={false}
          />
          <EntityActionButton
            color="danger"
            size="sm"
            entityId={entityId}
            entityType={entityType}
            entityAction={ENTITY_ACTION.DELETE}
            showText={false}
          />
        </div>
      );
    },
    minWidth: 150,
    maxWidth: 150,
    sortable: false,
  };
  return actionsColumn;
};

export function getUserFullName(user: IUser) {
  let name;
  if (user.firstName && user.lastName) {
    name = `${user.firstName} ${user.lastName}`;
  } else if (user.firstName) {
    name = user.firstName;
  } else {
    name = user.lastName;
  }
  return name;
}

export function formatDate(date: Date, dayOnly?: boolean) {
  const timeFormat = {
    year: '2-digit',
    month: '2-digit',
    day: '2-digit',
  } as any;
  if (!dayOnly) {
    timeFormat.hour = '2-digit';
    timeFormat.minute = '2-digit';
    timeFormat.hour12 = true;
  }
  return new Intl.DateTimeFormat('en-US', timeFormat).format(date);
}

export async function isPromiseOk(promise: Promise<any>) {
  try {
    await promise;
    return { ok: true };
  } catch (error) {
    return { ok: false, error };
  }
}

// splits alteration name separated by / into multiple alterations
export function expandAlterationName(name: string) {
  const regex = new RegExp('^([A-Z])\\s*([0-9]+)\\s*([A-Z])\\s*((?:/\\s*[A-Z]\\s*)*)$', 'i');
  const parts = regex.exec(name);

  if (!parts) {
    return [name];
  }

  const alterations: string[] = [];
  const firstPart = parts[1] + parts[2];
  alterations.push(firstPart + parts[3]);
  const rest = parts[4].substring(1);
  const alleles = rest ? rest.split('/') : [];
  for (const allele of alleles) {
    alterations.push(firstPart + allele.trim());
  }
  return alterations;
}

// splits alteration name into alteration, excluding and comment
// if alteration is separated by /, applies the same excluding and comment to separated alterations
export function parseAlterationName(alterationName: string): { alteration: string; excluding: string[]; comment: string; name: string }[] {
  let regex = new RegExp('\\[(.*)\\]', 'i');
  const nameSection = regex.exec(alterationName);
  let name = '';
  if (nameSection?.length > 1) {
    name = nameSection[1];
  }

  const alterationNameWithoutVariantName = alterationName.replace(nameSection?.[0], '');

  regex = new RegExp('({ *excluding[^}]+})', 'i');
  const excludingSection = regex.exec(alterationName);
  let alterationNameWithoutVariantNameAndExcluding = alterationNameWithoutVariantName;
  const excluding: string[] = [];
  if (excludingSection?.length > 1) {
    alterationNameWithoutVariantNameAndExcluding = alterationNameWithoutVariantName.replace(excludingSection[1], '');

    excludingSection[1] = excludingSection[1].slice(1, -1); // remove curly braces
    excludingSection[1] = excludingSection[1].replace(/excluding/i, '');
    const excludedNames = excludingSection[1].split(';');
    for (const ex of excludedNames) {
      excluding.push(...expandAlterationName(ex.trim()));
    }
  }

  const parentheses = [];
  let comment = '';
  for (const c of alterationName) {
    if (c === '(') {
      if (parentheses.length > 0) {
        comment += c;
      }
      parentheses.push(c);
    } else if (c === ')') {
      parentheses.pop();
      if (parentheses.length > 0) {
        comment += c;
      }
    } else if (parentheses.length > 0) {
      comment += c;
    }

    if (parentheses.length === 0 && comment.length > 0) {
      break;
    }
  }

  const parsedAlteration = alterationNameWithoutVariantNameAndExcluding.replace('(' + comment + ')', '');

  const alterationNames = expandAlterationName(parsedAlteration.trim());

  return alterationNames.map(alteration => ({
    alteration,
    excluding,
    comment,
    name,
  }));
}

export function findIndexOfFirstCapital(str: string) {
  for (let i = 0; i < str.length; i++) {
    if (str[i] >= 'A' && str[i] <= 'Z') {
      return i;
    }
  }
  return -1;
}

export function isNumeric(value: string) {
  return INTEGER_REGEX.test(value);
}

/**
 * @param hexColor A string representing a hex color
 * @param alpha A decimal in the range [0.0, 1.0]
 * @returns A string representing a hex color with transparency
 */
export function getHexColorWithAlpha(hexColor: string, alpha: number) {
  const alphaHex = `0${Math.round(255 * alpha).toString(16)}`.slice(-2).toUpperCase();
  return `${hexColor}${alphaHex}`;
}

export function extractPositionFromSingleNucleotideAlteration(alteration: string) {
  const regex = SINGLE_NUCLEOTIDE_POS_REGEX;
  const match = regex.exec(alteration);
  if (match) {
    return match[1];
  } else {
    return null;
  }
}

export const findAndSplitReferenceInString = (input: string): string[] => {
  if (input.length === 0) {
    return [input];
  }

  const startSeqs = REFERENCE_IDENTIFIERS;
  const results = [];
  let startIndex = -1;
  let lastIndex = 0;
  let nestingLevel = 0; // Keep track of nested parenthesis

  for (let i = 0; i < input.length; i++) {
    // Check for the opening parenthesis
    if (startIndex === -1 && input[i] === '(') {
      // Look ahead to check for any of the start sequences
      for (const seq of startSeqs) {
        if (input.substring(i + 1, i + 1 + seq.length) === seq) {
          if (i > lastIndex) {
            // Add the part before the current match
            results.push(input.substring(lastIndex, i));
          }
          startIndex = i;
          nestingLevel = 1; // Initialize nesting level
          break;
        }
      }
    } else if (startIndex !== -1) {
      // If start sequence found, manage nesting level
      if (input[i] === '(') {
        nestingLevel++;
      } else if (input[i] === ')') {
        nestingLevel--;
        if (nestingLevel === 0) {
          results.push(input.substring(startIndex, i + 1));
          lastIndex = i + 1; // Update the last processed index
          startIndex = -1; // Reset start index to look for new matches
        }
      }
    }
  }

  // If there is an unfinished match and we have finished looking through entire string
  if (startIndex !== -1 && nestingLevel > 0) {
    let lastResult = results.pop() || '';
    lastResult += input.substring(startIndex);
    results.push(lastResult);
    return results;
  }

  // Add any remaining part of the string after the last match
  if (lastIndex < input.length) {
    results.push(input.substring(lastIndex));
  }

  return results;
};

export function parseTextForReferences(text: string) {
  let content: Array<ParsedRef> = [];

  const parts = findAndSplitReferenceInString(text);
  parts.forEach((part: string) => {
    if (REFERENCE_IDENTIFIERS.find(identifier => part.substring(1, 1 + identifier.length) === identifier)) {
      const parsedRef = parseReferences(part, true);
      parsedRef.filter(ref => ref.link).forEach(ref => content.push(ref));
    }
  });

  content = _.uniqBy(content, 'content');
  return content;
}

export function getReferenceFullName(reference: ParsedRef) {
  if (!reference.prefix) {
    return reference.content;
  }
  return `${reference.prefix}${reference.content}`;
}

export function isEqualIngoreCase(a: string, b: string) {
  return a.toLowerCase() === b.toLowerCase();
}

export function getExonRanges(exons: ProteinExonDTO[]) {
  const exonRanges: string[] = [];
  let startExon = 0;
  let endExon = 0;
  for (let i = 0; i < exons.length; i++) {
    const exon = exons[i];
    if (startExon === 0) {
      startExon = endExon = exon.exon;
    }

    if (i + 1 === exons.length || exons[i + 1].exon - 1 !== endExon) {
      if (startExon === endExon) {
        exonRanges.push(startExon.toString());
      } else {
        exonRanges.push(`${startExon}~${endExon}`);
      }
      startExon = endExon = 0;
    } else {
      endExon++;
    }
  }
  return exonRanges;
}

export function isUuid(str: string) {
  return UUID_REGEX.test(str);
}

export const parseSort = (sort: IQueryParams['sort']) => {
  return sort.map(sortMethod => `&sort=${sortMethod}`).join('');
};

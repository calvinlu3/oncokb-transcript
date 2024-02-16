import React, { useMemo, useState } from 'react';
import { Mutation, VusObjList } from '../model/firebase/firebase.model';
import { SimpleConfirmModal } from './SimpleConfirmModal';
import { Button, Input } from 'reactstrap';
import { FaRegTrashAlt } from 'react-icons/fa';
import FieldErrorMessage from '../form/FieldErrorMessage';
import { addVusValidation } from '../util/firebase/firebase-utils';

export interface IAddVusModalProps {
  mutationList: Mutation[];
  vusList: VusObjList;
  show: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

type VusNameOption = {
  name: string;
  error?: string;
};

const AddVusModal = (props: IAddVusModalProps) => {
  const [vusOptions, setVusOptions] = useState<VusNameOption[]>([{ name: '' }]);

  const allowDelete = vusOptions.length > 1;

  const canSubmit = useMemo(() => {
    const hasAtleastOne = vusOptions.filter(o => o.name !== '').length > 0;
    if (!hasAtleastOne) {
      return false;
    }
    return !(vusOptions.filter(o => o.error).length > 0);
  }, [vusOptions]);

  return (
    <SimpleConfirmModal
      title={'Add Variant(s) of Unknown Significance'}
      show={props.show}
      onCancel={() => {
        setVusOptions([{ name: '' }]);
        props.onCancel();
      }}
      confirmDisabled={!canSubmit}
      body={
        <>
          <div>
            {vusOptions.map((vusOption, index) => (
              <div>
                <div className="d-flex align-items-center mt-3" key={`vus-${index}`}>
                  <div className="mr-3" style={{ flexGrow: 1 }}>
                    <Input
                      value={vusOption.name}
                      id={`vus-${index}`}
                      name={`vus-${index}`}
                      type="text"
                      placeholder="Enter variant name"
                      onChange={e => {
                        setVusOptions(prevVal => {
                          const newVusOptions = [...prevVal];
                          const error = addVusValidation(props.mutationList, props.vusList, e.target.value);
                          newVusOptions[index] = {
                            name: e.target.value,
                            error,
                          };
                          return newVusOptions;
                        });
                      }}
                    />
                  </div>
                  <FaRegTrashAlt
                    size={16}
                    className={allowDelete ? 'icon-button' : 'icon-button-disabled'}
                    onClick={() => {
                      setVusOptions(prevVal => {
                        const newVusOptions = [...prevVal];
                        newVusOptions.splice(index, 1);
                        return newVusOptions;
                      });
                    }}
                  />
                </div>
                {vusOption.error && <FieldErrorMessage message={vusOption.error} />}
              </div>
            ))}
          </div>
          <div>
            <Button
              className="mt-3"
              color="primary"
              outline
              size="sm"
              onClick={() => {
                setVusOptions(prevVal => {
                  const newVusOptions = [...prevVal];
                  newVusOptions.push({ name: '' });
                  return newVusOptions;
                });
              }}
            >
              Add new variant
            </Button>
          </div>
        </>
      }
    />
  );
};

export default AddVusModal;

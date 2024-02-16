import React from 'react';
import { FaExclamationCircle } from 'react-icons/fa';

export interface IFieldErrorMessage {
  message: string;
}

const FieldErrorMessage = (props: IFieldErrorMessage) => {
  return (
    <div className="d-flex align-items-center text-danger mt-2">
      <FaExclamationCircle className="mr-2" size={'16px'} />
      <span>{props.message}</span>
    </div>
  );
};

export default FieldErrorMessage;

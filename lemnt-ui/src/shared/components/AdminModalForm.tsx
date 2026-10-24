import { EditOutlined } from '@ant-design/icons';
import { ModalForm } from '@ant-design/pro-components';
import type { ModalFormProps } from '@ant-design/pro-components';
import type { ReactNode } from 'react';

type AdminModalFormProps<Values> = ModalFormProps<Values> & {
  description?: ReactNode;
};

/** 统一表单弹窗的外观，业务字段、提交与关闭逻辑由调用页控制。 */
export function AdminModalForm<Values extends object>({
  title,
  description,
  width = 640,
  modalProps,
  submitter,
  ...props
}: AdminModalFormProps<Values>) {
  const submitterConfig = typeof submitter === 'object' ? submitter : {};
  return (
    <ModalForm<Values>
      {...props}
      width={width}
      title={
        <div className="admin-dialog-title">
          <span className="admin-dialog-icon" aria-hidden="true">
            <EditOutlined />
          </span>
          <div>
            <span className="admin-dialog-heading">{title}</span>
            {description && <p className="admin-dialog-description">{description}</p>}
          </div>
        </div>
      }
      scrollToFirstError={props.scrollToFirstError ?? true}
      modalProps={{
        ...modalProps,
        className: ['admin-form-modal', modalProps?.className].filter(Boolean).join(' '),
      }}
      submitter={
        submitter === false
          ? false
          : {
              ...submitterConfig,
              searchConfig: {
                submitText: '保存',
                resetText: '取消',
                ...submitterConfig.searchConfig,
              },
            }
      }
    />
  );
}

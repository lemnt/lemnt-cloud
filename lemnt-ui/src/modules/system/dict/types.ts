export interface CreateDictTypeRequest {
  dictName: string;
  dictType: string;
  status: string;
  remark?: string;
}
export interface DictType extends CreateDictTypeRequest {
  dictId: number;
}
export interface CreateDictDataRequest {
  dictSort: number;
  dictLabel: string;
  dictValue: string;
  dictType: string;
  cssClass?: string;
  listClass?: string;
  isDefault: string;
  status: string;
  remark?: string;
}
export interface DictData extends CreateDictDataRequest {
  dictCode: number;
}
export interface DictTypeQuery {
  pageNum?: number;
  pageSize?: number;
  dictName?: string;
  dictType?: string;
  status?: string;
}
export interface DictDataQuery {
  pageNum?: number;
  pageSize?: number;
  dictType: string;
  dictLabel?: string;
  status?: string;
}

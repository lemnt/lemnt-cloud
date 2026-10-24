export interface ApiResponse<T> {
  code: number;
  msg?: string;
  data: T;
  rows?: T extends unknown[] ? T : never;
  total?: number;
}

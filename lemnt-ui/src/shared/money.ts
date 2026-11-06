/** 金额以文本输入，使用整数运算转换为分，避免浮点舍入。 */
export function yuanToFen(v: string): number {
  if (!/^\d+(\.\d{1,2})?$/.test(v)) throw Error('金额最多两位小数且不能为负');
  const [whole, fraction = ''] = v.split('.');
  const fen = BigInt(whole) * 100n + BigInt(fraction.padEnd(2, '0'));
  if (fen > 2147483647n) throw Error('金额超出范围');
  return Number(fen);
}
/** 整数分转换为两位元文本。 */
export function fenToYuan(v: unknown): string {
  if (typeof v !== 'number' || !Number.isInteger(v) || v < 0 || v > 2147483647)
    throw Error('分金额不合法');
  const fen = BigInt(v);
  return `${fen / 100n}.${String(fen % 100n).padStart(2, '0')}`;
}

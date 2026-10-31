/** 会员图片上传复用文件服务；每次只显式发送一次，不自动重试。 */
function createImageClient({ baseUrl, getMemberToken, uploadFile }) {
  return {
    async upload(path) {
      const token = getMemberToken();
      if (!token) throw Error("请先登录");
      if (!/^https:\/\/[^?#]+$/.test(baseUrl))
        throw Error("请配置HTTPS服务地址");
      const response = await uploadFile({
        url: baseUrl.replace(/\/$/, "") + "/system/files/app/upload",
        filePath: path,
        name: "file",
        header: { Authorization: "Bearer " + token },
        timeout: 30000,
      });
      if (token !== getMemberToken())
        throw Error("会员已切换，不能使用原上传结果");
      let result;
      try {
        result = JSON.parse(response.data);
      } catch {
        throw Error("上传结果未确认");
      }
      if (response.statusCode !== 200 || result.code !== 200)
        throw Error(result.detail || result.msg || "上传失败");
      if (
        typeof result.data?.url !== "string" ||
        !/^https:\/\/[^\s]+$/.test(result.data.url)
      )
        throw Error("上传地址无效");
      return result.data.url;
    },
  };
}
/** 取消选择不算失败；顺序上传保留此前成功结果。 */
async function selectImages({ count, append, client }) {
  const chosen = await new Promise((resolve, reject) =>
    wx.chooseMedia({
      count,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: resolve,
      fail: (error) =>
        /cancel/.test(error.errMsg || "")
          ? resolve({ tempFiles: [] })
          : reject(Error("选择图片失败")),
    }),
  );
  if (!Array.isArray(chosen.tempFiles) || chosen.tempFiles.length > count)
    throw Error("最多9张图片");
  for (const file of chosen.tempFiles) {
    if (!file.size || file.size > 10 * 1024 * 1024)
      throw Error("每张图片不能超过10MiB");
    append(await client.upload(file.tempFilePath));
  }
}
/** 仅将当前附件中合法的HTTPS地址用于预览，提交仍由评价客户端完整校验。 */
function pictureUrls(value) {
  return value
    .split(/\r?\n/)
    .map((v) => v.trim())
    .filter((v) => /^https:\/\/[^\s]+$/.test(v));
}
/** 移除一次附件引用，保持其他地址和顺序，不删除存储对象。 */
function removePicture(value, url) {
  const values = value
    .split(/\r?\n/)
    .map((v) => v.trim())
    .filter(Boolean);
  const index = values.indexOf(url);
  if (index >= 0) values.splice(index, 1);
  return values.join("\n");
}
module.exports = {
  createImageClient,
  selectImages,
  pictureUrls,
  removePicture,
};

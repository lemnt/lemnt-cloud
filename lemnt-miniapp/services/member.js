function id(v) {
  if (typeof v === "number" && Number.isSafeInteger(v)) v = String(v);
  if (typeof v !== "string" || !/^\d{1,19}$/.test(v) || /^0/.test(v))
    throw Error("会员标识不合法");
  return v;
}
function integer(v) {
  if (typeof v !== "number" || !Number.isSafeInteger(v))
    throw Error("会员数值超出安全范围");
  return v;
}
function object(v) {
  if (!v || typeof v !== "object" || Array.isArray(v))
    throw Error("会员响应不合法");
  return v;
}
function text(v) {
  if (typeof v !== "string") throw Error("会员文本响应不合法");
  return v;
}
function list(v, decode) {
  if (!Array.isArray(v)) throw Error("会员列表响应不合法");
  return v.map(decode);
}
function profile(v) {
  object(v);
  if (![0, 1].includes(v.status)) throw Error("会员状态不合法");
  return {
    id: id(v.id),
    memberNo: text(v.memberNo),
    nickname: text(v.nickname),
    mobile: v.mobile == null ? null : text(v.mobile),
    email: v.email == null ? null : text(v.email),
    avatar: v.avatar == null ? null : text(v.avatar),
    birthday: v.birthday == null ? null : text(v.birthday),
    sex: v.sex == null ? 0 : integer(v.sex),
    status: v.status,
    points: integer(v.points),
    experience: integer(v.experience),
    levelId: v.levelId == null ? null : id(v.levelId),
  };
}
function address(v) {
  object(v);
  if (typeof v.defaultStatus !== "boolean") throw Error("地址响应不合法");
  return {
    id: id(v.id),
    memberId: id(v.memberId),
    name: text(v.name),
    mobile: text(v.mobile),
    areaId: id(v.areaId),
    detailAddress: text(v.detailAddress),
    defaultStatus: v.defaultStatus,
  };
}
function tokens(v) {
  object(v);
  if (
    typeof v.accessToken !== "string" ||
    !/^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(v.accessToken) ||
    integer(v.expiresIn) <= 0
  )
    throw Error("会员登录响应不合法");
  return {
    memberId: id(v.memberId),
    accessToken: v.accessToken,
    expiresIn: v.expiresIn,
  };
}
function password(v) {
  if (
    typeof v !== "string" ||
    v.length < 10 ||
    encodeURIComponent(v).replace(/%[A-F\d]{2}/gi, "x").length > 72
  )
    throw Error("密码至少10字符且不超过72字节");
  return v;
}
function mobile(v) {
  if (typeof v !== "string" || !/^\+?\d{7,20}$/.test(v))
    throw Error("手机号格式不合法");
  return v;
}
function createMemberClient({
  request,
  baseUrl,
  ready = false,
  getSession,
  setSession,
}) {
  const same = (s) =>
    (getSession()?.accessToken || null) === (s?.accessToken || null);
  async function call(
    path,
    method = "GET",
    data,
    privateCall = true,
    override,
    fullResponse = false,
  ) {
    if (!ready) throw Error("会员服务尚未启用，请联系服务方");
    if (!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))
      throw Error("请配置合法HTTPS服务地址");
    const session = override || getSession();
    if (privateCall && !session) throw Error("请先登录会员");
    const response = await request({
      url: baseUrl.replace(/\/$/, "") + "/member/app" + path,
      method,
      data,
      timeout: 15000,
      header: session ? { Authorization: "Bearer " + session.accessToken } : {},
    });
    if (!override && !same(session)) throw Error("会员已切换，请重新操作");
    if (response.statusCode === 401 || response.data?.code === 401) {
      if (same(session)) setSession(null);
      throw Error("登录已失效，请重新登录");
    }
    if (response.statusCode !== 200 || response.data?.code !== 200)
      throw Error(
        response.data?.detail ||
          response.data?.msg ||
          "会员请求未确认成功，请刷新核验",
      );
    return fullResponse ? response.data : response.data.data;
  }
  async function login(path, data) {
    const before = getSession();
    const result = tokens(await call(path, "POST", data, false));
    if (!same(before)) throw Error("登录状态已变更");
    setSession(result);
    return result;
  }
  function owned(v) {
    if (v.memberId !== getSession()?.memberId)
      throw Error("响应不属于当前会员");
    return v;
  }
  async function page(path, pageNum, kind) {
    if (!Number.isSafeInteger(pageNum) || pageNum < 1)
      throw Error("页码不合法");
    const raw = object(
      await call(path, "GET", { pageNum, pageSize: 20 }, true, undefined, true),
    );
    return {
      total: integer(raw.total),
      records: list(raw.rows, (v) => {
        object(v);
        const value = { id: id(v.id), memberId: id(v.memberId) };
        owned(value);
        return kind === "sign"
          ? {
              ...value,
              signDate: text(v.signDate),
              day: integer(v.day),
              point: integer(v.point),
              experience: integer(v.experience),
            }
          : {
              ...value,
              delta: integer(v.delta),
              balance: integer(v.balance),
              ...(v.assetType === "POINT" && v.pointBizType != null
                ? { pointBizType: integer(v.pointBizType) }
                : {}),
              ...(v.assetType === "POINT" && v.title != null
                ? { title: text(v.title) }
                : {}),
              ...(v.assetType === "POINT" && v.description != null
                ? { description: text(v.description) }
                : {}),
              reason: text(v.reason),
              createTime: text(v.createTime),
            };
      }),
    };
  }
  const yes = (v) => {
    if (v !== true) throw Error("操作未确认成功，请刷新核验");
    return true;
  };
  return {
    passwordLogin: (phone, pwd) =>
      login("/auth/login", { mobile: mobile(phone), password: password(pwd) }),
    async refresh() {
      const before = getSession();
      if (!before) throw Error("请先登录");
      try {
        const result = tokens(
          await call("/auth/refresh-token", "POST", undefined, true),
        );
        if (result.memberId !== before.memberId || !same(before))
          throw Error("刷新身份不一致");
        setSession(result);
        return result;
      } catch (e) {
        if (same(before)) setSession(null);
        throw e;
      }
    },
    async logout() {
      const before = getSession();
      setSession(null);
      if (before)
        await call("/auth/logout", "POST", undefined, false, before).then(yes);
    },
    async profile() {
      const p = profile(await call("/user"));
      if (p.id !== getSession()?.memberId) throw Error("会员资料身份不匹配");
      return p;
    },
    updateProfile: (v) =>
      call("/user", "PUT", {
        nickname: v.nickname,
        email: v.email || null,
        avatar: v.avatar || null,
        sex: v.sex,
        birthday: v.birthday || null,
      }).then(yes),
    async changePassword(oldPassword, newPassword) {
      const before = getSession();
      await call("/user/password", "PUT", {
        oldPassword: password(oldPassword),
        newPassword: password(newPassword),
      }).then(yes);
      if (same(before)) setSession(null);
    },
    addresses: () =>
      call("/addresses").then((v) => list(v, (x) => owned(address(x)))),
    saveAddress: (key, v) =>
      call("/addresses" + (key ? "/" + id(key) : ""), key ? "PUT" : "POST", {
        name: text(v.name),
        mobile: mobile(v.mobile),
        areaId: id(v.areaId),
        detailAddress: text(v.detailAddress),
        defaultStatus: !!v.defaultStatus,
      }).then((v) => owned(address(v))),
    deleteAddress: (key) => call("/addresses/" + id(key), "DELETE").then(yes),
    levels: () =>
      call("/levels").then((v) =>
        list(v, (x) => {
          object(x);
          return {
            id: id(x.id),
            name: text(x.name),
            experience: integer(x.experience),
            discountPercent: integer(x.discountPercent),
          };
        }),
      ),
    signConfigs: () =>
      call("/signin/config").then((v) =>
        list(v, (x) => {
          object(x);
          return {
            day: integer(x.day),
            point: integer(x.point),
            experience: integer(x.experience),
          };
        }),
      ),
    summary: () =>
      call("/signin/summary").then((v) => {
        object(v);
        if (typeof v.todaySignIn !== "boolean") throw Error("签到汇总不合法");
        return {
          total: integer(v.totalDay),
          continuous: integer(v.continuousDay),
          signedToday: v.todaySignIn,
        };
      }),
    sign: () =>
      call("/signin", "POST").then((v) => {
        object(v);
        return owned({
          id: id(v.id),
          memberId: id(v.memberId),
          point: integer(v.point),
          experience: integer(v.experience),
          day: integer(v.day),
          signDate: text(v.signDate),
        });
      }),
    records: (kind, pageNum) => {
      if (!["points", "experience", "signin"].includes(kind))
        throw Error("记录类型不合法");
      return page(
        kind === "points"
          ? "/point-records"
          : kind === "experience"
            ? "/experience-records"
            : "/signin/records",
        pageNum,
        kind === "signin" ? "sign" : "asset",
      );
    },
  };
}
module.exports = { createMemberClient, id, password, profile, tokens };

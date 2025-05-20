import { createI18n } from 'vue-i18n';

// 语言包
const messages = {
  'zh-CN': {
    common: {
      appName: '购物平台',
      home: '首页',
      products: '商品',
      login: '登录',
      register: '注册',
      logout: '退出登录',
      search: '搜索',
      welcome: '欢迎',
      profile: '个人中心',
      cart: '购物车',
      orders: '我的订单',
      settings: '设置',
      submit: '提交',
      cancel: '取消',
      confirm: '确认',
      save: '保存',
      edit: '编辑',
      delete: '删除',
      back: '返回'
    },
    user: {
      username: '用户名',
      password: '密码',
      confirmPassword: '确认密码',
      email: '邮箱',
      phone: '手机号码',
      country: '国家',
      nickname: '昵称',
      gender: '性别',
      birthday: '生日',
      male: '男',
      female: '女',
      secret: '保密',
      role: '注册角色',
      buyer: '买家',
      seller: '卖家',
      agreement: '我已阅读并同意',
      userAgreement: '《用户服务协议》',
      hasAccount: '已有账号？',
      loginNow: '立即登录',
      registerNow: '立即注册'
    },
    countries: {
      china: '中国',
      usa: '美国',
      uk: '英国',
      japan: '日本',
      korea: '韩国'
    },
    validation: {
      required: '请输入{field}',
      email: '请输入正确的邮箱格式',
      phone: '请输入正确的手机号码格式',
      passwordMismatch: '两次输入的密码不一致',
      agreementRequired: '请同意用户服务协议',
      invalidCountry: '无效的国家选择',
      invalidPhone: '请输入正确的{country}手机号码格式',
      phonePlaceholder: {
        CN: '请输入11位手机号码',
        US: '请输入10位手机号码',
        GB: '请输入10位手机号码',
        JP: '请输入10-11位手机号码',
        KR: '请输入9-10位手机号码'
      }
    },
    footer: {
      aboutUs: '关于我们',
      companyInfo: '公司介绍',
      contactUs: '联系我们',
      joinUs: '加入我们',
      helpCenter: '帮助中心',
      shoppingGuide: '购物指南',
      payment: '支付方式',
      afterSales: '售后服务',
      merchantServices: '商家服务',
      merchantSettlement: '商家入驻',
      merchantCenter: '商家中心',
      marketingCenter: '营销中心',
      allRightsReserved: '版权所有'
    },
    product: {
      searchPlaceholder: '请输入商品名称',
      minPrice: '最低价格',
      maxPrice: '最高价格',
      noData: '暂无商品数据',
      sales: '已售 {count} 件',
      addToCart: '加入购物车',
      addToCartSuccess: '已添加到购物车',
      addToCartError: '添加到购物车失败',
      fetchError: '获取商品列表失败',
      sort: {
        default: '默认排序',
        priceAsc: '价格从低到高',
        priceDesc: '价格从高到低',
        salesDesc: '销量从高到低'
      }
    }
  },
  'en-US': {
    common: {
      appName: 'Shopping Mall',
      home: 'Home',
      products: 'Products',
      login: 'Login',
      register: 'Register',
      logout: 'Logout',
      search: 'Search',
      welcome: 'Welcome',
      profile: 'Profile',
      cart: 'Cart',
      orders: 'My Orders',
      settings: 'Settings',
      submit: 'Submit',
      cancel: 'Cancel',
      confirm: 'Confirm',
      save: 'Save',
      edit: 'Edit',
      delete: 'Delete',
      back: 'Back'
    },
    user: {
      username: 'Username',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      email: 'Email',
      phone: 'Phone',
      country: 'Country',
      nickname: 'Nickname',
      gender: 'Gender',
      birthday: 'Birthday',
      male: 'Male',
      female: 'Female',
      secret: 'Secret',
      role: 'Register as',
      buyer: 'Buyer',
      seller: 'Seller',
      agreement: 'I have read and agree to the',
      userAgreement: 'User Agreement',
      hasAccount: 'Already have an account?',
      loginNow: 'Login Now',
      registerNow: 'Register Now'
    },
    countries: {
      china: 'China',
      usa: 'United States',
      uk: 'United Kingdom',
      japan: 'Japan',
      korea: 'South Korea'
    },
    validation: {
      required: 'Please enter {field}',
      email: 'Please enter a valid email address',
      phone: 'Please enter a valid phone number',
      passwordMismatch: 'The passwords do not match',
      agreementRequired: 'Please agree to the user agreement',
      invalidCountry: 'Invalid country selection',
      invalidPhone: 'Please enter a valid {country} phone number',
      phonePlaceholder: {
        CN: 'Please enter 11-digit phone number',
        US: 'Please enter 10-digit phone number',
        GB: 'Please enter 10-digit phone number',
        JP: 'Please enter 10-11 digit phone number',
        KR: 'Please enter 9-10 digit phone number'
      }
    },
    footer: {
      aboutUs: 'About Us',
      companyInfo: 'Company Info',
      contactUs: 'Contact Us',
      joinUs: 'Join Us',
      helpCenter: 'Help Center',
      shoppingGuide: 'Shopping Guide',
      payment: 'Payment',
      afterSales: 'After Sales',
      merchantServices: 'Merchant Services',
      merchantSettlement: 'Merchant Settlement',
      merchantCenter: 'Merchant Center',
      marketingCenter: 'Marketing Center',
      allRightsReserved: 'All Rights Reserved'
    },
    product: {
      searchPlaceholder: 'Enter product name',
      minPrice: 'Min Price',
      maxPrice: 'Max Price',
      noData: 'No products found',
      sales: '{count} sold',
      addToCart: 'Add to Cart',
      addToCartSuccess: 'Added to cart',
      addToCartError: 'Failed to add to cart',
      fetchError: 'Failed to fetch products',
      sort: {
        default: 'Default',
        priceAsc: 'Price Low to High',
        priceDesc: 'Price High to Low',
        salesDesc: 'Best Selling'
      }
    }
  },
  'ja-JP': {
    common: {
      appName: 'ショッピングモール',
      home: 'ホーム',
      products: '商品',
      login: 'ログイン',
      register: '会員登録',
      logout: 'ログアウト',
      search: '検索',
      welcome: 'ようこそ',
      profile: 'プロフィール',
      cart: 'カート',
      orders: '注文履歴',
      settings: '設定'
    },
    user: {
      username: 'ユーザー名',
      password: 'パスワード',
      confirmPassword: 'パスワード（確認）',
      email: 'メールアドレス',
      phone: '電話番号',
      country: '国',
      nickname: 'ニックネーム',
      gender: '性別',
      birthday: '生年月日',
      male: '男性',
      female: '女性',
      secret: '非公開',
      role: '会員種別',
      buyer: '購入者',
      seller: '出店者',
      agreement: '利用規約に同意する',
      userAgreement: '利用規約',
      hasAccount: 'アカウントをお持ちの方',
      loginNow: 'ログインする',
      registerNow: '新規会員登録'
    },
    countries: {
      china: '中国',
      usa: 'アメリカ',
      uk: 'イギリス',
      japan: '日本',
      korea: '韓国'
    },
    validation: {
      required: '{field}を入力してください',
      email: '有効なメールアドレスを入力してください',
      phone: '有効な電話番号を入力してください',
      passwordMismatch: 'パスワードが一致しません',
      agreementRequired: '利用規約に同意してください',
      invalidCountry: '無効な国の選択です',
      invalidPhone: '有効な{country}の電話番号を入力してください',
      phonePlaceholder: {
        CN: '11桁の電話番号を入力してください',
        US: '10桁の電話番号を入力してください',
        GB: '10桁の電話番号を入力してください',
        JP: '10-11桁の電話番号を入力してください',
        KR: '9-10桁の電話番号を入力してください'
      }
    },
    footer: {
      aboutUs: '会社概要',
      companyInfo: '会社情報',
      contactUs: 'お問い合わせ',
      joinUs: '採用情報',
      helpCenter: 'ヘルプセンター',
      shoppingGuide: 'ご利用ガイド',
      payment: 'お支払い方法',
      afterSales: 'アフターサービス',
      merchantServices: '出店者向けサービス',
      merchantSettlement: '出店申込',
      merchantCenter: '出店者センター',
      marketingCenter: 'マーケティングセンター',
      allRightsReserved: '無断転載禁止'
    },
    product: {
      searchPlaceholder: '商品名を入力',
      minPrice: '最低価格',
      maxPrice: '最高価格',
      noData: '商品が見つかりません',
      sales: '売れ数：{count}件',
      addToCart: 'カートに入れる',
      addToCartSuccess: 'カートに追加しました',
      addToCartError: 'カートに追加できませんでした',
      fetchError: '商品の取得に失敗しました',
      sort: {
        default: 'デフォルト',
        priceAsc: '価格が安い順',
        priceDesc: '価格が高い順',
        salesDesc: '売れ筋順'
      }
    }
  },
  'ko-KR': {
    common: {
      appName: '쇼핑몰',
      home: '홈',
      products: '상품',
      login: '로그인',
      register: '회원가입',
      logout: '로그아웃',
      search: '검색',
      welcome: '환영합니다',
      profile: '프로필',
      cart: '장바구니',
      orders: '주문내역',
      settings: '설정'
    },
    user: {
      username: '사용자 이름',
      password: '비밀번호',
      confirmPassword: '비밀번호 확인',
      email: '이메일',
      phone: '전화번호',
      country: '국가',
      nickname: '닉네임',
      gender: '성별',
      birthday: '생년월일',
      male: '남성',
      female: '여성',
      secret: '비공개',
      role: '회원 유형',
      buyer: '구매자',
      seller: '판매자',
      agreement: '이용약관에 동의합니다',
      userAgreement: '이용약관',
      hasAccount: '이미 계정이 있으신가요?',
      loginNow: '로그인하기',
      registerNow: '회원가입하기'
    },
    countries: {
      china: '중국',
      usa: '미국',
      uk: '영국',
      japan: '일본',
      korea: '한국'
    },
    validation: {
      required: '{field}을(를) 입력해주세요',
      email: '유효한 이메일 주소를 입력해주세요',
      phone: '유효한 전화번호를 입력해주세요',
      passwordMismatch: '비밀번호가 일치하지 않습니다',
      agreementRequired: '이용약관에 동의해주세요',
      invalidCountry: '잘못된 국가 선택입니다',
      invalidPhone: '올바른 {country} 전화번호 형식을 입력해주세요',
      phonePlaceholder: {
        CN: '11자리 전화번호를 입력해주세요',
        US: '10자리 전화번호를 입력해주세요',
        GB: '10자리 전화번호를 입력해주세요',
        JP: '10-11자리 전화번호를 입력해주세요',
        KR: '9-10자리 전화번호를 입력해주세요'
      }
    },
    footer: {
      aboutUs: '회사 소개',
      companyInfo: '회사 정보',
      contactUs: '문의하기',
      joinUs: '채용 정보',
      helpCenter: '고객 센터',
      shoppingGuide: '쇼핑 가이드',
      payment: '결제 방법',
      afterSales: '애프터 서비스',
      merchantServices: '판매자 서비스',
      merchantSettlement: '판매자 등록',
      merchantCenter: '판매자 센터',
      marketingCenter: '마케팅 센터',
      allRightsReserved: '모든 권리 보유'
    },
    product: {
      searchPlaceholder: '상품명 입력',
      minPrice: '최저가',
      maxPrice: '최고가',
      noData: '상품이 없습니다',
      sales: '{count}개 판매',
      addToCart: '장바구니 담기',
      addToCartSuccess: '장바구니에 담았습니다',
      addToCartError: '장바구니 담기 실패',
      fetchError: '상품 목록 불러오기 실패',
      sort: {
        default: '기본 정렬',
        priceAsc: '가격 낮은순',
        priceDesc: '가격 높은순',
        salesDesc: '판매량순'
      }
    }
  }
};

// 国家和语言的映射关系
export const countryLocaleMap = {
  'CN': 'zh-CN',
  'US': 'en-US',
  'GB': 'en-US',
  'JP': 'ja-JP',
  'KR': 'ko-KR'
};

// 创建 i18n 实例
const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: localStorage.getItem('locale') || 'zh-CN', // 默认语言
  fallbackLocale: 'en-US', // 备用语言
  messages
});

export default i18n; 
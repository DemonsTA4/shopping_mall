<template>
  <div class="product-edit">
    <h1 class="page-title">{{ isEdit ? '编辑商品' : '添加商品' }}</h1>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="loading || submitting" 
      >
        <h2 class="section-title">基本信息</h2>

        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择商品分类" style="width: 100%;">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入商品描述"
            :rows="4"
          />
        </el-form-item>

        <h2 class="section-title">销售信息</h2>

        <el-form-item label="销售价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :step="0.01"
            placeholder="请输入销售价格"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="原价" prop="originalPrice">
          <el-input-number
            v-model="form.originalPrice"
            :min="0"
            :precision="2"
            :step="0.01"
            placeholder="请输入商品原价 (可选)"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            :precision="0"
            :step="1"
            placeholder="请输入库存数量"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio> <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="是否推荐" prop="isFeatured">
           <el-switch v-model="form.isFeatured" />
        </el-form-item>


        <h2 class="section-title">商品图片</h2>

        <el-form-item label="主图" prop="imageUrl">
          <el-upload
            class="image-uploader"
            action="#" 
            :http-request="customUploadMainImage" 
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeImageUpload"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="preview-image" alt="主图预览"/>
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="图片集" prop="images">
          <el-upload
            class="image-list-uploader"
            action="#"
            :http-request="customUploadGalleryImage"
            :file-list="galleryFileList" 
            list-type="picture-card"
            accept="image/*"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleGalleryImageRemove"
            :before-upload="beforeImageUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <h2 class="section-title">商品详情</h2>
        <el-form-item label="详情内容" prop="detail">
          <el-input
            v-model="form.detail"
            type="textarea"
            placeholder="请输入商品详情，支持HTML内容"
            :rows="8"
          />
        </el-form-item>

        <h2 class="section-title">其他信息</h2>
        <el-form-item label="商品参数" prop="params">
          <div v-for="(param, index) in form.params" :key="index" class="param-item">
            <el-input
              v-model="form.params[index]"
              placeholder="请输入参数"
              class="param-input"
            >
              <template #append>
                <el-button @click="removeParam(index)" :disabled="form.params.length <= 1 && index === 0">
                  删除
                </el-button>
              </template>
            </el-input>
          </div>
          <el-button type="primary" text @click="addParam">+ 添加参数</el-button>
        </el-form-item>

        <el-form-item label="商品规格" prop="specs">
          <div v-for="(spec, index) in form.specs" :key="index" class="param-item">
            <el-input
              v-model="form.specs[index]"
              placeholder="请输入规格"
              class="param-input"
            >
              <template #append>
                <el-button @click="removeSpec(index)" :disabled="form.specs.length <= 1 && index === 0">
                  删除
                </el-button>
              </template>
            </el-input>
          </div>
          <el-button type="primary" text @click="addSpec">+ 添加规格</el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '更新商品' : '立即创建' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" style="width: 100%"/>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getCategories, getProductDetail, createProduct, updateProduct /*, uploadProductImage API is not used directly here */ } from '@/api/product'; // 假设API路径正确

const route = useRoute();
const router = useRouter();
const formRef = ref(null); // el-form的引用
const loading = ref(false); // 页面加载状态（例如编辑时获取详情）
const submitting = ref(false); // 表单提交状态
const categories = ref([]);

// --- 文件处理相关的 refs ---
const mainImageFileFromInput = ref(null); // 存储主图的实际 File 对象
const otherImageFilesFromInput = ref([]);   // 存储其他图片的实际 File 对象列表

// --- el-upload UI 控制相关的 refs ---
const galleryFileList = ref([]); // 用于附图 el-upload 的 :file-list，控制UI显示
const dialogImageUrl = ref('');   // 用于图片预览对话框
const dialogVisible = ref(false); // 控制图片预览对话框的显示

const isEdit = computed(() => !!route.params.id);

const form = reactive({
  name: '',
  categoryId: null,
  description: '',
  price: null,
  originalPrice: null,
  stock: null,
  status: 1,       // 默认上架
  imageUrl: '',    // 用于主图预览URL (Base64 或 编辑时加载的服务器URL)
  images: [],      // 用于存储图片URL列表 (编辑时加载的服务器URL，或新增时收集的Base64预览URL)
  detail: '',
  isFeatured: false, // 默认不推荐
  params: [''],     // 至少有一个空参数输入框
  specs: ['']       // 至少有一个空规格输入框
});

// 前端表单验证规则
const rules = reactive({
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' } // 根据DTO调整
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' } // 根据DTO调整
  ],
  price: [
    { required: true, message: '请输入销售价格', trigger: 'blur' },
    { type: 'number', message: '价格必须是数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') { callback(new Error('请输入销售价格')); return; }
        if (typeof value === 'number' && value > 0) { callback(); }
        else { callback(new Error('商品价格必须大于0')); }
      }, trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', message: '库存必须是数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') { callback(new Error('请输入库存数量')); return; }
        if (typeof value === 'number' && value >= 0) { callback(); }
        else { callback(new Error('商品库存不能小于0')); }
      }, trigger: 'blur' }
  ],
  // imageUrl 的校验将主要通过检查 mainImageFileFromInput.value 是否有文件来进行
});

const fetchCategories = async () => {
  try {
    const res = await getCategories();
    categories.value = res.data || [];
  } catch (error) {
    console.error('获取分类失败:', error);
    ElMessage.error('获取分类列表失败');
  }
};

const fetchProductDetail = async () => {
  if (!isEdit.value) return;
  loading.value = true;
  try {
    const res = await getProductDetail(route.params.id);
    const productData = res.data;
    Object.assign(form, productData); // 使用 Object.assign 填充表单

    // 更新主图预览 (form.imageUrl 已经被 Object.assign 填充)
    // 更新附图 el-upload 的 fileList (用于显示)
    if (productData.images && Array.isArray(productData.images)) {
      galleryFileList.value = productData.images.map((url, index) => ({
        name: `已上传图片${index + 1}`,
        url: url, // 这是后端返回的真实图片URL
        status: 'success',
        uid: Date.now() + index // 或者使用更可靠的唯一ID
      }));
      // form.images 也被 Object.assign 填充了，它现在是URL列表
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    ElMessage.error('获取商品信息失败');
  } finally {
    loading.value = false;
  }
};

// 图片上传前校验 (大小、类型等)
const beforeImageUpload = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  const isLt2M = rawFile.size / 1024 / 1024 < 2; // 限制小于2MB
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 自定义主图上传处理 (el-upload :http-request)
const customUploadMainImage = (options) => {
  const file = options.file; // 这是 File 对象
  mainImageFileFromInput.value = file; // 关键: 存储 File 对象以供提交
  // 生成本地预览URL
  const reader = new FileReader();
  reader.onload = (e) => {
    form.imageUrl = e.target.result; // 更新 form.imageUrl 以用于预览
  };
  reader.readAsDataURL(file);
  // options.onSuccess(); // 如果 http-request 不实际上传，可以调用 onSuccess让 el-upload 知道选择完成
  return Promise.resolve(); // :http-request 需要返回一个 Promise
};

// 自定义附图上传处理 (el-upload :http-request)
const customUploadGalleryImage = (options) => {
  const file = options.file;
  // 将 File 对象添加到列表中以供提交
  otherImageFilesFromInput.value.push(file);
  // 更新 galleryFileList 以供 el-upload 显示预览 (它需要包含url属性)
  // 注意: el-upload 会自动为其内部列表添加文件，我们这里主要是为了获取raw file
  // :on-change 也可以用来获取文件并更新 otherImageFilesFromInput 和 galleryFileList
  // 这里我们假设 el-upload 的 :file-list="galleryFileList" 和 :on-remove 会处理UI部分
  // 但为了确保 otherImageFilesFromInput 同步，我们在此处添加。
  // 然而，更好的做法可能是在 :on-change 中处理，并手动管理 galleryFileList。
  // 为了简化，我们假设 el-upload 的 :file-list 和 :on-remove 能正确管理 galleryFileList 的显示。
  // 我们主要关心 otherImageFilesFromInput
  
  // 这里不直接修改 galleryFileList，让 :on-change (如果使用) 或 :on-remove 处理 galleryFileList
  // :http-request 的主要目的是获取 file
  // options.onSuccess();
  return Promise.resolve();
};

// 处理附图列表的移除 (el-upload :on-remove)
const handleGalleryImageRemove = (uploadFile, currentUploadFiles) => {
  // uploadFile 是被移除的el-upload的包装对象，uploadFile.raw 是原始File (如果存在)
  // currentUploadFiles 是移除后剩余的el-upload的包装对象列表
  const fileToRemove = uploadFile.raw || otherImageFilesFromInput.value.find(f => f.uid === uploadFile.uid || f.name === uploadFile.name);
  if (fileToRemove) {
    otherImageFilesFromInput.value = otherImageFilesFromInput.value.filter(f => f !== fileToRemove);
  }
  galleryFileList.value = currentUploadFiles; // 更新UI列表
  // 如果 form.images 也用于追踪要保留的旧图片URL (编辑模式)，也需要更新
  if (isEdit.value && uploadFile.status === 'success' && uploadFile.url && !uploadFile.raw) { // 删的是已上传的旧图
      form.images = form.images.filter(url => url !== uploadFile.url);
  }
};

// 处理附图列表变化 (el-upload :on-change) - 推荐用这个来管理 File 对象和预览列表
const handleGalleryImageChange = (uploadFile, currentUploadFiles) => {
    // uploadFile.raw 是新添加或状态改变的文件的原始File对象
    // currentUploadFiles 是el-upload当前维护的文件列表 (包装对象)

    // 更新我们自己维护的原始File对象列表
    otherImageFilesFromInput.value = currentUploadFiles
        .map(f => f.raw) // 获取每个包装对象的原始File
        .filter(f => f instanceof File); // 确保是File对象

    // 更新用于el-upload显示的列表 (它会自动管理这个，但如果我们想同步或做额外操作可以在这里)
    galleryFileList.value = currentUploadFiles;

    console.log('附图列表已更新, File对象数量:', otherImageFilesFromInput.value.length);
};


// 附图预览 (el-upload :on-preview)
const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url; // uploadFile.url 应该是预览URL (Base64或服务器URL)
  dialogVisible.value = true;
};


const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    const realMultipartFormData = new FormData();

    const productMetaData = {
      name: form.name,
      categoryId: form.categoryId,
      description: form.description,
      price: form.price !== null && form.price !== undefined && form.price !== '' ? parseFloat(form.price) : null,
      originalPrice: form.originalPrice !== null && form.originalPrice !== undefined && form.originalPrice !== '' ? parseFloat(form.originalPrice) : null,
      stock: form.stock !== null && form.stock !== undefined && form.stock !== '' ? parseInt(form.stock, 10) : null,
      status: form.status,
      detail: form.detail,
      isFeatured: form.isFeatured,
      params: form.params ? form.params.filter(p => p && p.trim() !== '') : [],
      specs: form.specs ? form.specs.filter(s => s && s.trim() !== '') : [],
      imageUrl: (isEdit.value && !mainImageFileFromInput.value && form.imageUrl) ? form.imageUrl : null,
      images: (isEdit.value && (!otherImageFilesFromInput.value || otherImageFilesFromInput.value.length === 0) && form.images && form.images.length > 0) ? form.images.filter(img => img && String(img).trim() !== '') : []
    };
    console.log('Submitting productMetaData:', JSON.parse(JSON.stringify(productMetaData)));
    realMultipartFormData.append('productData', new Blob([JSON.stringify(productMetaData)], { type: 'application/json' }));

    if (mainImageFileFromInput.value) {
      realMultipartFormData.append('mainImageFile', mainImageFileFromInput.value);
    }

    if (otherImageFilesFromInput.value && otherImageFilesFromInput.value.length > 0) {
      otherImageFilesFromInput.value.forEach(file => {
        realMultipartFormData.append('otherImageFiles', file);
      });
    }

    if (!isEdit.value && !mainImageFileFromInput.value && !productMetaData.imageUrl) {
        ElMessage.error('请上传商品主图');
        submitting.value = false;
        return;
    }

    if (isEdit.value) {
      await updateProduct(route.params.id, realMultipartFormData);
      ElMessage.success('更新成功');
    } else {
      await createProduct(realMultipartFormData);
      ElMessage.success('添加成功');
    }
    router.push('/seller/products');
  } catch (error) {
    console.error('保存商品失败:', error);
    let errorMessage = '保存失败，请重试';
    if (error?.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error?.response?.data?.errors && Array.isArray(error.response.data.errors)) {
      errorMessage = error.response.data.errors.map(e => `${e.defaultMessage || e.message}`).join('; ');
    } else if (error?.message) {
      errorMessage = error.message;
    }
    ElMessage.error(errorMessage);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  fetchCategories();
  if (isEdit.value) {
    fetchProductDetail();
  }
});

const addParam = () => { form.params.push(''); };
const removeParam = (index) => { if (form.params.length > 1 || form.params[index] !== '') form.params.splice(index, 1); if(form.params.length === 0) form.params.push(''); };
const addSpec = () => { form.specs.push(''); };
const removeSpec = (index) => { if (form.specs.length > 1 || form.specs[index] !== '') form.specs.splice(index, 1); if(form.specs.length === 0) form.specs.push(''); };

</script>

<style lang="scss" scoped>
// 建议将 SCSS 变量导入或定义在此文件的 <style> 标签顶部或全局 SCSS 文件中
// 例如: @import '@/assets/styles/variables.scss';

.product-edit {
  .page-title {
    font-size: 20px; // 示例大小
    margin-bottom: 20px;
  }
  .form-card {
    margin-bottom: 20px;
  }
  .section-title {
    font-size: 16px; // 示例大小
    margin: 20px 0 10px;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee; // 示例颜色
    &:first-of-type { // 使用 :first-of-type 避免对所有h2生效
      margin-top: 0;
    }
  }

  .image-uploader {
    width: 150px; // 根据你的CSS调整
    height: 150px;
    border: 1px dashed #d9d9d9; // 示例颜色
    border-radius: 6px; // 示例圆角
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;

    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-uploader-icon {
      font-size: 28px;
      color: #8c939d; // 示例颜色
    }
  }
  .image-list-uploader ::v-deep(.el-upload--picture-card) { // 使用 ::v-deep 或 :deep() 穿透作用域
      width: 100px;
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
  }
   .image-list-uploader ::v-deep(.el-upload-list__item) {
      width: 100px;
      height: 100px;
  }


  .param-item {
    margin-bottom: 10px;
    display: flex;
  }
  .param-input {
    flex-grow: 1;
    margin-right: 10px; /* 给删除按钮一些空间 */
  }
}
</style>
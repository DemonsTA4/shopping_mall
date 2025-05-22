<template>
  <div class="category-manage-container">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showAddDialog">
        添加分类
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="categories"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="分类图标" width="100">
        <template #default="{ row }">
			{{ console.log('当前行分类名称 (row.name):', row.name, '类型:', typeof row.name) }}
			    {{ console.log('尝试获取图标 for ' + row.name + ':', getCategoryIcon(row.name)) }}
          <el-image
            v-if="getCategoryIcon(row.name)" :src="getCategoryIcon(row.name)"
            fit="contain"
            class="category-icon"
            :alt="row.name"
          />
          <el-icon v-else><Picture /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          <span>{{ $formatTime(row.createTime) }}</span>
          </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类图标" prop="icon">
          <el-upload
            class="icon-uploader"
            action="#"
            :http-request="uploadIcon"
            :show-file-list="false"
            accept="image/*"
          >
            <img v-if="form.icon" :src="form.icon" class="icon-preview" />
            <el-icon v-else class="icon-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="icon-tip">建议尺寸: 60x60px</div>
        </el-form-item>
        
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :step="1"
            style="width: 120px"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Picture, Plus } from '@element-plus/icons-vue';
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus,
  uploadCategoryIcon
} from '@/api/category';
import foodIcon from '@/assets/images/food.png';
import computerIcon from '@/assets/images/computer.png';
import clothingIcon from '@/assets/images/clothing.png';
import earphoneIcon from '@/assets/images/earphone.png';
import phoneIcon from '@/assets/images/phone.png';

const categoryIconMap = {
  '电子产品': earphoneIcon, // 您指定 earphoneIcon 代表电子产品
  '服装': clothingIcon,
  '食品': foodIcon,
  '手机': phoneIcon,
  '电脑': computerIcon,
  // 您可以添加更多的分类和对应的图标
  // 例如，如果后端返回的分类名和文件名有一定规律，也可以动态构造
};

// 列表数据
const loading = ref(false);
const categories = ref([]);

// 对话框数据
const dialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const formRef = ref(null);

// 表单数据
const form = reactive({
  name: '',
  description: '',
  sort: 0,
  status: 1
});

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
};

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true;
  try {
    const res = await getCategories();
    categories.value = res.data || [];
  } catch (error) {
    console.error('获取分类列表失败:', error);
    ElMessage.error('获取分类列表失败');
  } finally {
    loading.value = false;
  }
};

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 显示编辑对话框
const handleEdit = (row) => {
  isEdit.value = true;
  Object.assign(form, row);
  dialogVisible.value = true;
};

// 处理删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该分类吗？删除后不可恢复。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await deleteCategory(row.id);
    ElMessage.success('删除成功');
    fetchCategories();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  Object.assign(form, {
    name: '',
    description: '',
    sort: 0,
    status: 1
  });
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
    if (isEdit.value) {
      await updateCategory(form.id, form);
      ElMessage.success('更新成功');
    } else {
      await createCategory(form);
      ElMessage.success('添加成功');
    }
    
    dialogVisible.value = false;
    fetchCategories();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存分类失败:', error);
      ElMessage.error('保存失败');
    }
  } finally {
    submitting.value = false;
  }
};


// 处理状态变更
const handleStatusChange = async (row) => {
  try {
    await updateCategoryStatus(row.id, { status: row.status });
    ElMessage.success('状态更新成功');
  } catch (error) {
    console.error('更新状态失败:', error);
    ElMessage.error('更新失败');
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1;
  }
};

function getCategoryIcon(categoryName) {
  return categoryIconMap[categoryName] || defaultCategoryIcon;
}

onMounted(() => {
  fetchCategories();
});
</script>

<style lang="scss" scoped>
.category-manage-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }
  }

  .category-icon {
    width: 40px;
    height: 40px;
  }

  .icon-uploader {
    width: 100px;
    height: 100px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: #409EFF;
    }

    .icon-preview {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }

    .icon-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100%;
      height: 100%;
      text-align: center;
      line-height: 100px;
    }
  }

  .icon-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }
}
</style> 
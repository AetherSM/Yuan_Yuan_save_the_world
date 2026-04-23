<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const productId = ref(null)

const productName = ref('')
const description = ref('')
const categoryId = ref(1)
const price = ref(10)
const originalPrice = ref()
const stock = ref(10)
const mainImage = ref('')
const shippingAddress = ref('')
const status = ref(1)
const message = ref('')
const loading = ref(false)
const uploadLoading = ref(false)
const categories = ref([])

const imageUrl = computed(() => {
  if (!mainImage.value) return ''
  // 如果是本地上传的相对路径，确保它能通过代理访问
  // 这里可以根据实际情况添加后端域名，目前通过代理访问相对路径即可
  return mainImage.value
})

const uploadHeaders = computed(() => ({
  token: localStorage.getItem('token') || ''
}))

const handleUploadSuccess = (res) => {
  uploadLoading.value = false
  if (res.code === 1) {
    mainImage.value = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '图片上传失败')
  }
}

const beforeUpload = (file) => {
  const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  if (!isJPGorPNG) {
    ElMessage.error('只能上传 JPG/PNG/WEBP 格式的图片!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  uploadLoading.value = true
  return true
}

const handleUploadError = (err) => {
  uploadLoading.value = false
  console.error('Upload error:', err)
  // 如果是 401，跳转登录
  try {
    const errorData = JSON.parse(err.message)
    if (errorData.status === 401) {
      ElMessage.error('登录已失效，请重新登录')
      router.push('/login')
      return
    }
  } catch (e) {}
  ElMessage.error('图片上传失败，请检查网络或登录状态')
}

onMounted(async () => {
  await loadCategories()
  if (route.query.id) {
    isEdit.value = true
    productId.value = route.query.id
    await loadProduct()
  }
})

const loadCategories = async () => {
  try {
    const { data } = await http.get('/categories')
    if (data && data.code === 1) {
      categories.value = data.data || []
    }
  } catch (e) {
    console.error('加载分类失败:', e)
  }
}

const loadProduct = async () => {
  try {
    const { data } = await http.get(`/products/${productId.value}`)
    if (data && data.code === 1) {
      const p = data.data
      productName.value = p.productName
      description.value = p.description
      categoryId.value = p.categoryId
      price.value = p.price
      originalPrice.value = p.originalPrice
      stock.value = p.stock
      mainImage.value = p.mainImage
      shippingAddress.value = p.shippingAddress || ''
      status.value = p.status
    }
  } catch (e) {
    message.value = '加载商品失败'
  }
}

const submit = async () => {
  loading.value = true
  message.value = ''
  try {
    const body = {
      categoryId: categoryId.value,
      productName: productName.value,
      description: description.value,
      price: price.value,
      originalPrice: originalPrice.value,
      stock: stock.value,
      mainImage: mainImage.value,
      shippingAddress: shippingAddress.value,
      status: status.value
    }
    
    let res
    if (isEdit.value) {
      res = await http.put(`/products/${productId.value}`, body)
    } else {
      res = await http.post('/products', body)
    }
    
    const data = res.data
    if (data && data.code === 1) {
      message.value = isEdit.value ? '更新成功' : '发布成功'
      if (!isEdit.value) {
        productName.value = ''
        description.value = ''
        price.value = 10
        originalPrice.value = undefined
        stock.value = 10
        mainImage.value = ''
        shippingAddress.value = ''
        status.value = 1
      } else {
        setTimeout(() => router.back(), 1000)
      }
    } else {
      message.value = data?.msg || (isEdit.value ? '更新失败' : '发布失败')
    }
  } catch (e) {
    message.value = '请求失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form">
    <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
    <div class="row"><label>名称</label><el-input v-model="productName" placeholder="请输入商品名称" /></div>
    <div class="row"><label>描述</label><el-input v-model="description" type="textarea" :rows="4" placeholder="请输入商品描述" /></div>
    <div class="row">
      <label>商品分类</label>
      <el-select v-model="categoryId" placeholder="请选择商品分类">
        <el-option
          v-for="item in categories"
          :key="item.categoryId"
          :label="item.categoryName"
          :value="item.categoryId"
        />
      </el-select>
    </div>
    <div class="row"><label>价格</label><el-input-number v-model="price" :min="0" :step="0.01" /></div>
    <div class="row"><label>原价</label><el-input-number v-model="originalPrice" :min="0" :step="0.01" /></div>
    <div class="row"><label>库存</label><el-input-number v-model="stock" :min="0" /></div>
    <div class="row">
      <label>商品主图</label>
      <el-upload
        class="avatar-uploader"
        action="/common/upload"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :headers="uploadHeaders"
        name="file"
      >
        <img v-if="imageUrl" :src="imageUrl" class="avatar" />
        <el-icon v-else-if="uploadLoading" class="avatar-uploader-icon is-loading"><Loading /></el-icon>
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <div class="upload-tip">点击图片区域上传，支持 png/jpg/webp</div>
    </div>
    <div class="row"><label>发货地址</label><el-input v-model="shippingAddress" placeholder="请输入发货地址" /></div>
    <div class="row"><label>状态</label>
      <el-select v-model="status" placeholder="请选择状态"><el-option :value="1" label="上架" /><el-option :value="0" label="下架" /></el-select>
    </div>
    <el-button type="primary" class="btn" :loading="loading" @click="submit">{{ isEdit ? '保存修改' : '发布' }}</el-button>
    <div class="msg" v-if="message">{{ message }}</div>
    <div class="tip">注意：仅商家账号可操作</div>
  </div>
</template>

<style scoped>
.form{width:100%;padding:16px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.row{display:flex;flex-direction:column;gap:6px;margin-bottom:12px}
.btn{margin-top:6px}
.msg{margin-top:12px;color:#42b883}
.tip{margin-top:8px;color:#9ca3af}

.avatar-uploader{
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color .3s;
}
.avatar-uploader:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}
.is-loading {
  animation: rotating 2s linear infinite;
}
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: contain;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

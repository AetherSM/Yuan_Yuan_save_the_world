<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const products = ref([])
const selectedProduct = ref(null)
const reviews = ref([])
const loading = ref(false)
const error = ref('')
const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyContent = ref('')
const sellerId = Number(localStorage.getItem('userId') || 0)

// 加载商家的商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    const { data } = await http.get('/products', { 
      params: { sellerId }
    })
    if (data && data.code === 1) {
      products.value = data.data || []
    } else {
      error.value = data?.msg || '加载商品失败'
    }
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}

// 加载商品评价
const loadReviews = async (productId) => {
  loading.value = true
  try {
    const { data } = await http.get(`/products/${productId}/reviews`)
    if (data && data.code === 1) {
      reviews.value = data.data || []
      // 加载每个评价的回复
      for (const review of reviews.value) {
        const replyRes = await http.get(`/reviews/${review.reviewId}/replies`)
        if (replyRes.data && replyRes.data.code === 1) {
          review.replies = replyRes.data.data || []
        }
      }
    } else {
      error.value = data?.msg || '加载评价失败'
    }
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}

// 选择商品查看评价
const selectProduct = (product) => {
  selectedProduct.value = product
  loadReviews(product.productId)
}

// 打开回复对话框
const openReplyDialog = (review) => {
  currentReview.value = review
  replyContent.value = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    const { data } = await http.post(`/reviews/${currentReview.value.reviewId}/replies`, {
      content: replyContent.value
    })
    if (data && data.code === 1) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      // 重新加载评价
      if (selectedProduct.value) {
        loadReviews(selectedProduct.value.productId)
      }
    } else {
      ElMessage.error(data?.msg || '回复失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

// 返回商品列表
const backToProducts = () => {
  selectedProduct.value = null
  reviews.value = []
}

onMounted(loadProducts)
</script>

<template>
  <div class="page">
    <!-- 商品列表视图 -->
    <div v-if="!selectedProduct">
      <div class="page-card toolbar">
        <div class="toolbar-left">
          <div class="title">评价管理</div>
          <div class="muted">查看并回复商品评价</div>
        </div>
        <div class="toolbar-right">
          <el-button type="info" plain @click="loadProducts" :loading="loading">刷新</el-button>
        </div>
      </div>

      <div v-if="error" class="page-card">
        <el-alert type="error" :closable="false" :title="error" />
      </div>

      <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

      <div v-else-if="products.length === 0" class="page-card">
        <el-empty description="暂无商品" />
      </div>

      <div v-else class="list">
        <div v-for="product in products" :key="product.productId" class="card" @click="selectProduct(product)">
          <div class="row">
            <div class="product-name">{{ product.productName }}</div>
            <el-tag :type="product.status === 1 ? 'success' : 'info'">
              {{ product.status === 1 ? '上架中' : '已下架' }}
            </el-tag>
          </div>
          <div class="row">
            <div class="price">¥{{ product.price }}</div>
            <div class="info">库存: {{ product.stock }} | 销量: {{ product.salesCount }}</div>
          </div>
          <div class="action">
            <el-button type="primary" link>查看评价 →</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 评价列表视图 -->
    <div v-else>
      <div class="page-card toolbar">
        <div class="toolbar-left">
          <el-button type="info" plain @click="backToProducts">← 返回商品列表</el-button>
          <div class="title">{{ selectedProduct.productName }} - 评价列表</div>
        </div>
        <div class="toolbar-right">
          <el-button type="info" plain @click="loadReviews(selectedProduct.productId)" :loading="loading">刷新</el-button>
        </div>
      </div>

      <div v-if="error" class="page-card">
        <el-alert type="error" :closable="false" :title="error" />
      </div>

      <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

      <div v-else-if="reviews.length === 0" class="page-card">
        <el-empty description="暂无评价" />
      </div>

      <div v-else class="reviews-list">
        <div v-for="review in reviews" :key="review.reviewId" class="review-card">
          <div class="review-header">
            <div class="user-info">
              <span class="user-id">用户ID: {{ review.userId }}</span>
              <el-rate :model-value="review.rating" disabled show-score />
            </div>
            <span class="time">{{ review.createTime }}</span>
          </div>
          <div class="review-content">{{ review.content }}</div>
          
          <!-- 回复列表 -->
          <div v-if="review.replies && review.replies.length > 0" class="replies">
            <div v-for="reply in review.replies" :key="reply.replyId" class="reply-item">
              <span class="reply-label">商家回复:</span>
              <span class="reply-content">{{ reply.content }}</span>
              <span class="reply-time">{{ reply.createTime }}</span>
            </div>
          </div>
          
          <div class="review-actions">
            <el-button 
              v-if="!review.replies || review.replies.length === 0" 
              size="small" 
              type="primary" 
              @click="openReplyDialog(review)"
            >
              回复
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="500px">
      <el-form label-width="80px">
        <el-form-item label="评价内容">
          <div class="review-preview">{{ currentReview?.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">提交回复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 20px; }
.page-card { 
  background: #fff; 
  border-radius: 12px; 
  padding: 20px; 
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title { font-size: 18px; font-weight: 600; }
.muted { color: #6b7280; font-size: 14px; }
.list { display: flex; flex-direction: column; gap: 12px; }
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.product-name { font-weight: 600; font-size: 16px; }
.price { color: #f56c6c; font-weight: 600; font-size: 18px; }
.info { color: #6b7280; font-size: 14px; }
.action { text-align: right; margin-top: 8px; }

.reviews-list { display: flex; flex-direction: column; gap: 16px; }
.review-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.user-info { display: flex; align-items: center; gap: 12px; }
.user-id { color: #6b7280; font-size: 14px; }
.time { color: #999; font-size: 13px; }
.review-content { 
  color: #333; 
  font-size: 15px; 
  line-height: 1.6; 
  margin-bottom: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}
.replies {
  background: #f0f9ff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}
.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}
.reply-label { color: #409eff; font-weight: 600; font-size: 14px; }
.reply-content { color: #333; font-size: 14px; flex: 1; }
.reply-time { color: #999; font-size: 12px; }
.review-actions { text-align: right; }
.review-preview {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  color: #666;
}
.dialog-footer { text-align: right; }
</style>
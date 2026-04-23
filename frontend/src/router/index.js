import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import ErrandList from '../views/ErrandList.vue'
import CreateErrand from '../views/CreateErrand.vue'
import RunnerTasks from '../views/RunnerTasks.vue'
import MerchantProducts from '../views/MerchantProducts.vue'
import MerchantOrders from '../views/MerchantOrders.vue'
import MerchantReviews from '../views/MerchantReviews.vue'
import MerchantCoupons from '../views/MerchantCoupons.vue'
import MerchantMessages from '../views/MerchantMessages.vue'
import My from '../views/My.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import ProductCreate from '../views/ProductCreate.vue'
import Addresses from '../views/Addresses.vue'
import Coupons from '../views/Coupons.vue'
import Cart from '../views/Cart.vue'
import ShoppingRecords from '../views/ShoppingRecords.vue'
import OrderDetail from '../views/OrderDetail.vue'
import AdminLayout from '../views/AdminLayout.vue'
import AdminUsers from '../views/AdminUsers.vue'
import AdminNotices from '../views/AdminNotices.vue'
import AdminProducts from '../views/AdminProducts.vue'
import AdminErrands from '../views/AdminErrands.vue'
import AdminComplaints from '../views/AdminComplaints.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminOrders from '../views/AdminOrders.vue'
import AdminRoleApplications from '../views/AdminRoleApplications.vue'
import AdminRefunds from '../views/AdminRefunds.vue'

const routes = [
  { path: '/', redirect: '/shop' },
  { path: '/login', component: Login },
  { path: '/errands', component: ErrandList, meta: { requiresAuth: true } },
  { path: '/errands/runner', component: RunnerTasks, meta: { requiresAuth: true } },
  { path: '/errands/create', component: CreateErrand, meta: { requiresAuth: true } },
  { path: '/merchant/products', component: MerchantProducts, meta: { requiresAuth: true } },
  { path: '/merchant/product/create', component: ProductCreate, meta: { requiresAuth: true } }, // Reuse for create/edit
  { path: '/merchant/orders', component: MerchantOrders, meta: { requiresAuth: true } },
  { path: '/merchant/reviews', component: MerchantReviews, meta: { requiresAuth: true } },
  { path: '/merchant/coupons', component: MerchantCoupons, meta: { requiresAuth: true } },
  { path: '/merchant/messages', component: MerchantMessages, meta: { requiresAuth: true } },
  { path: '/my', component: My, meta: { requiresAuth: true } },
  { path: '/orders', redirect: '/my?tab=orders' },
  { path: '/wallet', redirect: '/my?tab=wallet' },
  { path: '/addresses', component: Addresses, meta: { requiresAuth: true } },
  { path: '/coupons', component: Coupons, meta: { requiresAuth: true } }
  ,
  { path: '/shop', component: ProductList, meta: { requiresAuth: true } },
  { path: '/shop/create', component: ProductCreate, meta: { requiresAuth: true } },
  { path: '/shop/:id', component: ProductDetail, meta: { requiresAuth: true } },
  { path: '/cart', component: Cart, meta: { requiresAuth: true } }
  ,
  { path: '/records', component: ShoppingRecords, meta: { requiresAuth: true } },
  { path: '/order/:orderNo', component: OrderDetail, meta: { requiresAuth: true } },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', component: AdminDashboard },
      { path: 'users', component: AdminUsers },
      { path: 'notices', component: AdminNotices },
      { path: 'products', component: AdminProducts },
      { path: 'orders', component: AdminOrders },
      { path: 'role-applications', component: AdminRoleApplications },
      { path: 'errands', component: AdminErrands },
      { path: 'complaints', component: AdminComplaints },
      { path: 'refunds', component: AdminRefunds },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userType = Number(localStorage.getItem('userType') || 1)
  
  // 未登录且需要认证
  if (to.path !== '/login' && to.meta?.requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 商家限制访问的页面（领取优惠券、购物记录等用户功能）
  const userOnlyPaths = ['/coupons', '/records', '/cart']
  if (userType === 3 && userOnlyPaths.includes(to.path)) {
    next('/merchant/products')
    return
  }
  
  // 跑腿员限制访问的页面
  const runnerRestrictedPaths = ['/merchant']
  if (userType === 2 && runnerRestrictedPaths.some(path => to.path.startsWith(path))) {
    next('/errands')
    return
  }
  
  // 普通用户限制访问商家页面
  if (userType === 1 && to.path.startsWith('/merchant')) {
    next('/shop')
    return
  }
  
  // 管理员权限检查
  if (to.meta?.requiresAdmin) {
    if (userType !== 0) {
      next('/shop')
      return
    }
  }
  
  next()
})

export default router

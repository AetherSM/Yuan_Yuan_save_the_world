import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import ErrandList from '../views/ErrandList.vue'
import CreateErrand from '../views/CreateErrand.vue'
import RunnerTasks from '../views/RunnerTasks.vue'
import MerchantProducts from '../views/MerchantProducts.vue'
import MerchantOrders from '../views/MerchantOrders.vue'
import My from '../views/My.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import ProductCreate from '../views/ProductCreate.vue'
import Addresses from '../views/Addresses.vue'
import Coupons from '../views/Coupons.vue'
import Cart from '../views/Cart.vue'
import ShoppingRecords from '../views/ShoppingRecords.vue'
import AdminLayout from '../views/AdminLayout.vue'
import AdminUsers from '../views/AdminUsers.vue'
import AdminProducts from '../views/AdminProducts.vue'
import AdminErrands from '../views/AdminErrands.vue'
import AdminComplaints from '../views/AdminComplaints.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminOrders from '../views/AdminOrders.vue'
import AdminRoleApplications from '../views/AdminRoleApplications.vue'

const routes = [
  { path: '/', redirect: '/shop' },
  { path: '/login', component: Login },
  { path: '/errands', component: ErrandList, meta: { requiresAuth: true } },
  { path: '/errands/runner', component: RunnerTasks, meta: { requiresAuth: true } },
  { path: '/errands/create', component: CreateErrand, meta: { requiresAuth: true } },
  { path: '/merchant/products', component: MerchantProducts, meta: { requiresAuth: true } },
  { path: '/merchant/product/create', component: ProductCreate, meta: { requiresAuth: true } }, // Reuse for create/edit
  { path: '/merchant/orders', component: MerchantOrders, meta: { requiresAuth: true } },
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
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', component: AdminDashboard },
      { path: 'users', component: AdminUsers },
      { path: 'products', component: AdminProducts },
      { path: 'orders', component: AdminOrders },
      { path: 'role-applications', component: AdminRoleApplications },
      { path: 'errands', component: AdminErrands },
      { path: 'complaints', component: AdminComplaints },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.meta?.requiresAuth && !token) {
    next('/login')
  } else {
    if (to.meta?.requiresAdmin) {
      const userType = Number(localStorage.getItem('userType') || 1)
      if (userType !== 0) {
        next('/shop')
        return
      }
    }
    next()
  }
})

export default router

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">管理后台</div>
      <nav class="menu">
        <router-link to="/admin/users" class="menu-item">用户管理</router-link>
        <router-link to="/admin/products" class="menu-item">商品管理</router-link>
        <!-- 其他菜单项 -->
      </nav>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 200px;
  background-color: #333;
  color: #fff;
  padding: 20px;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
}
.menu-item {
  display: block;
  color: #fff;
  text-decoration: none;
  padding: 10px 0;
  margin-bottom: 10px;
}
.menu-item.router-link-active {
  background-color: #555;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
